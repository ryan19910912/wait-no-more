package com.waitnomore.common.pojo;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalPO {

    /** 系統內部流水號 (代理主鍵) */
    private Long id;

    /** 健保署醫事機構代碼 (唯一識別碼) */
    private String hospCode;

    /** 醫療機構名稱 */
    private String name;

    /** 機構層級: clinic/hospital_local/hospital_regional/medical_center */
    private String orgType;

    /** 聯絡電話 */
    private String phone;

    /** 地址 */
    private String address;

    /** 城市 */
    private String city;

    /** 區 */
    private String dist;

    /** 經度 (從地理座標 Point 拆分) */
    private Double longitude;

    /** 緯度 (從地理座標 Point 拆分) */
    private Double latitude;

    /** Google Maps 導航連結 */
    private String googleMapUrl;

    /** 是否營運中 (用於過濾歇業機構) */
    private Boolean isActive;

    /** 資料寫入時間 */
    private LocalDateTime createdAt;
}
