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
public class MedicalCategoryPO {

    /** 流水號 */
    private Integer id;

    /** 科別唯一識別碼 */
    private String categoryCodes;

    /** 科別名稱 (ex: 耳鼻喉科, 皮膚科) */
    private String name;

    /** 建立時間 */
    private LocalDateTime createdAt;
}
