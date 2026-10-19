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
public class UserBehaviorLogPO {

    /** 日誌唯一編號 */
    private Long id;

    /** 關聯至 users.id */
    private Long userId;

    /** 行為類型: search(搜尋), notify_set(設定通知), detail_view(查看詳情) */
    private String actionType;

    /** 當時觸發行為的科別 (例如搜尋「感冒」對應「耳鼻喉科」) */
    private String medicalCategory;

    /** 關聯至當時查看或設定的機構流水號 (hospital.id) */
    private Long hospitalId;

    /** 紀錄產生的時間 */
    private LocalDateTime createdAt;
}
