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
public class HospitalLiveStatusPO {

    /** 關聯至 hospital.id 流水號 */
    private Long hospitalId;

    /** 診間識別代碼或醫師名稱 (ex: "101", "內科第一診") */
    private String roomId;

    /** 目前叫號號碼 */
    private Integer currentNumber;

    /** 目前候診總人數 */
    private Integer waitingCount;

    /** 該診間目前是否看診中 */
    private Boolean isOpen;

    /** 最後更新時間 */
    private LocalDateTime lastUpdated;
}
