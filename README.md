# Wait No More — 長庚看診進度

Java 21 / Spring Boot 3.2.3。範圍為院所基本資料與公開看診進度；已移除交通站點匯入。

## 啟動公開查詢 API

```powershell
mvn clean verify
mvn -pl wait-no-more-api -am install -DskipTests
java -jar wait-no-more-api/target/wait-no-more-api-1.0-SNAPSHOT.jar
```

API 不需要資料庫或 API Key。院所基本資料匯入仍由 worker 執行，需要獨立設定資料庫及政府 CSV URL。

```text
GET http://localhost:8080/api/hospitals/cgmh/branches
GET http://localhost:8080/api/hospitals/cgmh/1/progress?department=02&session=3
```

院區 1=台北、3=林口、8=高雄，其他代碼見 branches。department 是官方科別大類代碼：
02=內科、03=外科、04=牙科、05=婦產科、06=兒童專科、07=其它專科、08=中醫。
session 必填：1=上午、2=下午、3=晚間。各院區開放的科別不同；上游未接受條件時回傳 502。

資料源：https://register.cgmh.org.tw/Progress/1 。僅解析公開科別查詢，不使用身分資料查詢。
每組院區/科別/午別快取 60 秒，失敗也間隔 60 秒再試；不會因每位使用者刷新而反覆抓取。
第一次擷取失敗回傳 502；已有快取時回傳舊結果並標示 stale=true。
sourceUpdatedAt 是官方原始時間文字；fetchedAt 是本服務擷取時間，不代表院方更新時間。
currentNumber/nextNumber/followingNumber 保留字串（例如「過號 78」或空白），不推算候診人數或開診狀態。
回傳空 clinics 代表查詢沒有列出診間，並不等於整間醫院休診。
目前是即時查詢與記憶體快取，重啟會清空；尚未保存叫號歷史或提供到號通知。
官方頁面註明資料僅供參考；正式公開彙整前需向長庚確認再利用與存取授權。

## 院所基本資料 worker

`POST /internal/tasks/hospital/sync`，使用既有 custom.gov.csv-urls 設定。
新資料庫可參考 `wait-no-more-worker/src/main/resources/db/hospital-schema.sql`；不會自動執行此檔。
既有資料庫需要先比對 schema。科別資料需先載入 medical_categories，才能建立醫院與科別關聯。
座標補齊端點與交通站點匯入不在本版範圍，已移除；既有資料庫資料未做刪除。

## 本機查詢頁面

啟動 API 後開啟 http://localhost:8080/ 。前端由 Spring Boot 提供，不需 Node.js 或額外前端服務。
可選擇院區、院方提供的科別與上午/下午/晚間，顯示醫師、位置、目前與後續號碼。
每分鐘自動更新可關閉；分頁在背景時暫停更新。更換條件會清除舊結果，按查詢後顯示新結果。
網址會保存本次查詢條件，可直接分享或重新開啟。空白號碼顯示「尚未提供」，過號文字保留。
新增 GET /api/hospitals/cgmh/{hospitalCode}/departments，院區科別從官方頁面讀取，快取一小時。

## 私密設定與公開 GitHub

`application-local.yml`、`application-prod.yml`、`.env*`、IDE 設定與所有 `target/` 已加入 .gitignore。
既有私密設定保留在本機，不應提交；可公開的 worker `application.yml` 使用環境變數，沒有實際資料庫帳密。
啟動 worker 前，在同一個 PowerShell 視窗設定自己的值：

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/wait_no_more'
$env:DB_USERNAME = 'your-user'
$env:DB_PASSWORD = 'your-password'
java -jar wait-no-more-worker/target/wait-no-more-worker-1.0-SNAPSHOT.jar
```

上面的值只是示例，真實值不要寫進 README 或其他程式碼。Spring Boot 不會自動載入 .env。
長庚查詢 API 與網頁不使用資料庫，因此啟動 api 不需要設定這些變數。
本機若選用 local/prod profile，仍會使用原有私密設定；建置出的 JAR 可能包含該設定，勿公開上傳。
若帳密曾進入公開提交，即使之後忽略或刪除檔案，仍應更換該帳密。
