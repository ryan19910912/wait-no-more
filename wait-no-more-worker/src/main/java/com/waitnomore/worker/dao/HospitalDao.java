package com.waitnomore.worker.dao;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HospitalDao {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * 根據一組醫事機構代碼，抓回對應的資料庫自增 ID (Mapping 用)
     */
    public Map<String, Long> getIdMapByCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) return Map.of();

        String sql = "SELECT id, hosp_code FROM hospital WHERE hosp_code IN (:codes)";
        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql, Map.of("codes", codes));

        return rows.stream().collect(Collectors.toMap(
            row -> (String) row.get("hosp_code"),
            row -> (Long) row.get("id")
        ));
    }

    public void batchUpsert(java.util.List<com.waitnomore.common.pojo.HospitalPO> hospitals) {
        if (hospitals.isEmpty()) return;
        String sql = """
            INSERT INTO hospital (hosp_code, name, org_type, phone, address, is_active)
            VALUES (:hospCode, :name, :orgType, :phone, :address, :isActive)
            ON DUPLICATE KEY UPDATE name=VALUES(name), org_type=VALUES(org_type),
              phone=VALUES(phone), address=VALUES(address), is_active=VALUES(is_active)
            """;
        namedParameterJdbcTemplate.batchUpdate(sql,
            org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils.createBatch(hospitals));
    }
}
