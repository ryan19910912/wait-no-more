package com.waitnomore.worker.dao;

import com.waitnomore.common.pojo.MedicalCategoryPO;
import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MedicalCategoryDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    // 永久緩存：Key 是名稱 (ex: "耳鼻喉科"), Value 是資料庫自增 ID
    private final Map<String, Integer> nameToIdCache = new ConcurrentHashMap<>();

    /**
     * 服務啟動後自動執行，將所有科別載入記憶體
     */
    @PostConstruct
    public void initCache() {
        refreshCache();
    }

    /**
     * 刷新緩存的邏輯
     */
    public void refreshCache() {
        try {
            log.info("正在初始化 Medical Category 永久緩存...");
            String sql = "SELECT id, category_codes, name FROM medical_categories";
            List<MedicalCategoryPO> list = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(MedicalCategoryPO.class));

            Map<String, Integer> freshMap = list.stream()
                .collect(Collectors.toMap(
                    MedicalCategoryPO::getName,
                    MedicalCategoryPO::getId,
                    (existing, replacement) -> existing
                ));

            nameToIdCache.clear();
            nameToIdCache.putAll(freshMap);
            log.info("Medical Category 緩存加載完成，共 {} 筆", nameToIdCache.size());
        } catch (Exception e) {
            log.error("初始化 Medical Category 緩存失敗!", e);
        }
    }

    /**
     * 直接從記憶體取得 ID，效能極高
     */
    public Integer getIdByName(String name) {
        if (name == null) return null;
        return nameToIdCache.get(name.trim());
    }

    /**
     * 取得唯讀的完整 Map
     */
    public Map<String, Integer> getAllCategoryMap() {
        return Collections.unmodifiableMap(nameToIdCache);
    }
}
