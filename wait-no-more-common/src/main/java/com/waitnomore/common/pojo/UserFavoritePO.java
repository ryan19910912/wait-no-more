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
public class UserFavoritePO {

    /** 關聯至 users.id */
    private Long userId;

    /** 關聯至 hospital.id 流水號 */
    private Long hospitalId;

    /** 加入收藏時間 */
    private LocalDateTime createdAt;
}
