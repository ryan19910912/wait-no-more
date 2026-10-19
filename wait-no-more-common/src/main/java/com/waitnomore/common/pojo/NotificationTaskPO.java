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
public class NotificationTaskPO {

    /** 通知任務唯一編號 */
    private Long id;

    /** 用戶唯一流水號 (關聯至 users.id) */
    private Long userId;

    /** 醫院唯一流水號 (關聯至 hospital.id) */
    private Long hospitalId;

    /** 診間識別代碼 */
    private String roomId;

    /** 使用者設定的目標通知號碼 */
    private Integer targetNumber;

    /** 是否已發送通知 */
    private Boolean isSent;

    /** 任務建立時間 */
    private LocalDateTime createdAt;
}
