package com.waitnomore.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalCategoryMappingPO {

    /** 關聯至 hospital.id (流水號) */
    private Long hospitalId;

    /** 關聯至 medical_categories.id (科別編號) */
    private Integer categoryId;
}
