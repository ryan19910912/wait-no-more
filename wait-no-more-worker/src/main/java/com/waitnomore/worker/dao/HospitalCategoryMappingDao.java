package com.waitnomore.worker.dao;

import com.waitnomore.common.pojo.HospitalCategoryMappingPO;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class HospitalCategoryMappingDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * 全量批次插入醫院與科別的關聯
     * 建議在執行前先呼叫 truncateAll() 清空舊資料，或是使用 INSERT IGNORE
     *
     * @param mappingList 包含所有醫院與科別對應關係的 PO 清單
     */
    @Transactional
    public void batchInsert(List<HospitalCategoryMappingPO> mappingList) {
        if (mappingList == null || mappingList.isEmpty()) {
            return;
        }

        // 使用 INSERT IGNORE 避免重複寫入時報錯（複合主鍵衝突）
        String sql = "INSERT IGNORE INTO hospital_category_mapping (hospital_id, category_id) " +
            "VALUES (:hospitalId, :categoryId)";

        // SqlParameterSourceUtils 可以直接將 PO List 轉為批次參數，非常高效
        jdbcTemplate.batchUpdate(sql, SqlParameterSourceUtils.createBatch(mappingList));

        log.info("【成功】關聯表批次寫入完成，共 {} 筆記錄", mappingList.size());
    }

    public void deleteByHospitalIds(List<Long> hospitalIds) {
        String sql = "DELETE FROM hospital_category_mapping WHERE hospital_id IN (:ids)";
        jdbcTemplate.update(sql, Map.of("ids", hospitalIds));
    }
    @Transactional(rollbackFor = Exception.class)
    public void replaceMappings(List<Long> hospitalIds, List<HospitalCategoryMappingPO> mappings) {
        for (int i = 0; i < hospitalIds.size(); i += 1000) {
            deleteByHospitalIds(hospitalIds.subList(i, Math.min(i + 1000, hospitalIds.size())));
        }
        batchInsert(mappings);
    }
}
