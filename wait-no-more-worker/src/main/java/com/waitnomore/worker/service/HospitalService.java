package com.waitnomore.worker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.waitnomore.common.pojo.HospitalCategoryMappingPO;
import com.waitnomore.common.pojo.HospitalPO;
import com.waitnomore.worker.config.CustomProperties;
import com.waitnomore.worker.dao.HospitalCategoryMappingDao;
import com.waitnomore.worker.dao.HospitalDao;
import com.waitnomore.worker.dao.MedicalCategoryDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalDao hospitalDao;
    private final RestTemplate restTemplate;
    private final CustomProperties customProperties;
    private final MedicalCategoryDao medicalCategoryDao;
    private final HospitalCategoryMappingDao hospitalCategoryMappingDao;

    /**
     * 同步所有層級醫院資料 (CSV -> DB)
     */
    public void syncAllHospitals() {
        Map<String, String> urls = customProperties.getGov().getCsvUrls();
        if (urls == null || urls.isEmpty()) {
            throw new IllegalStateException("找不到政府 CSV 網址配置");
        }

        urls.forEach((orgType, url) -> {
            log.info("開始同步層級：{} ...", orgType);
            fetchAndProcessCsv(url, orgType);
        });
    }

    private void fetchAndProcessCsv(String urlString, String orgType) {
        List<HospitalPO> batchBuffer = new ArrayList<>();
        Map<String, List<Integer>> hospCodeToCatIdsMap = new HashMap<>();

        try (InputStream is = URI.create(urlString).toURL().openStream();
            BOMInputStream bomIn = BOMInputStream.builder().setInputStream(is).get();
            BufferedReader reader = new BufferedReader(new InputStreamReader(bomIn, StandardCharsets.UTF_8))
        ) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] cols = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (cols.length < 9) continue;

                try {
                    String hospCode = cleanQuotes(cols[0]);
                    String name = cleanQuotes(cols[1]);
                    String phone = cleanQuotes(cols[3]);
                    String rawAddr = cleanQuotes(cols[4]);

                    HospitalPO po = HospitalPO.builder()
                        .hospCode(hospCode)
                        .name(name)
                        .orgType(orgType)
                        .phone(phone)
                        .address(rawAddr)
                        .isActive(true)
                        .build();

                    batchBuffer.add(po);

                    if (batchBuffer.size() >= 500) {
                        hospitalDao.batchUpsert(batchBuffer);
                        log.info("【成功】批次寫入 {} 筆 {} 資料", batchBuffer.size(), orgType);
                        batchBuffer.clear();
                    }

                    String rawCategories = cleanQuotes(cols[8]);
                    List<Integer> catIds = Arrays.stream(rawCategories.split(","))
                        .map(String::trim)
                        .map(medicalCategoryDao::getIdByName)
                        .filter(Objects::nonNull)
                        .toList();
                    hospCodeToCatIdsMap.put(hospCode, catIds);

                } catch (Exception e) {
                    log.error("解析單行資料錯誤: {}", e.getMessage());
                }
            }

            if (!batchBuffer.isEmpty()) {
                hospitalDao.batchUpsert(batchBuffer);
            }

            this.syncAllMappings(hospCodeToCatIdsMap);

        } catch (Exception e) {
            throw new IllegalStateException("CSV 同步失敗: " + orgType, e);
        }
    }

    private void syncAllMappings(Map<String, List<Integer>> hospCodeToCatIdsMap) {
        log.info("開始同步醫院與科別關聯表...");
        Map<String, Long> codeToIdMap = hospitalDao.getIdMapByCodes(new ArrayList<>(hospCodeToCatIdsMap.keySet()));
        List<HospitalCategoryMappingPO> mappingBatch = new ArrayList<>();
        List<Long> allHospitalIds = new ArrayList<>(codeToIdMap.values());

        hospCodeToCatIdsMap.forEach((code, catIds) -> {
            Long dbHospitalId = codeToIdMap.get(code);
            if (dbHospitalId != null) {
                for (Integer catId : catIds) {
                    mappingBatch.add(new HospitalCategoryMappingPO(dbHospitalId, catId));
                }
            }
        });

        hospitalCategoryMappingDao.replaceMappings(allHospitalIds, mappingBatch);
        log.info("關聯表同步完成，總計寫入 {} 筆關聯", mappingBatch.size());
    }

    private String cleanQuotes(String input) {
        return input == null ? "" : input.replace("\"", "").trim();
    }
}
