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
public class UserPO {

    /** 系統內部用戶唯一識別碼 */
    private Long id;

    /** LINE 平台唯一識別碼 (用於 LINE Login 登入) */
    private String lineUid;

    /** 用戶暱稱 */
    private String displayName;

    /** 用戶頭像網址 */
    private String avatarUrl;

    /** Web Push 通知端點 URL */
    private String pushEndpoint;

    /** Web Push 加密公鑰 (p256dh) */
    private String pushP256dh;

    /** Web Push 驗證密鑰 (auth) */
    private String pushAuth;

    /** 帳號建立時間 */
    private LocalDateTime createdAt;
}
