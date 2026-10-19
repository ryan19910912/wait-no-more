# 第一階段：編譯階段 (Build Stage)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 1. 複製所有模組的 pom.xml
COPY pom.xml .
COPY wait-no-more-common/pom.xml wait-no-more-common/
COPY wait-no-more-worker/pom.xml wait-no-more-worker/
COPY wait-no-more-api/pom.xml wait-no-more-api/

# 下載依賴項目
RUN mvn dependency:go-offline -B

# 2. 複製所有模組的源碼 (包含 api)
COPY wait-no-more-common/src wait-no-more-common/src
COPY wait-no-more-worker/src wait-no-more-worker/src
COPY wait-no-more-api/src wait-no-more-api/src

# 3. 執行全域安裝
# 這裡建議先跑一次 install，確保 common 被安裝到本地 repo，供 worker 和 api 使用
RUN mvn clean install -DskipTests

# 4. 如果這個 Dockerfile 是專門給 worker 用的：
# 確保最後生成的 jar 是來自 worker 模組
# (這步其實在上面 install 完就已經在各模組的 target 底下了)

# 第二階段：執行階段 (Run Stage)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 從第一階段複製打包好的 worker jar 檔
COPY --from=build /app/wait-no-more-worker/target/wait-no-more-worker-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]