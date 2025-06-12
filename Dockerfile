# --- Builder 이미지
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY gradlew gradlew.bat ./
RUN chmod +x gradlew

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src ./src
RUN ./gradlew clean bootJar -x test

# --- Runtime 이미지
FROM eclipse-temurin:21-jdk
WORKDIR /app
# secrets 파일 복사 (없으면 빈 파일이라도 만들어 주세요)
# COPY config/application.yml ./config/application-secrets.yml
# 로그 디렉터리 생성 및 권한 부여
RUN mkdir -p /var/moongsan/log && chown -R 1000:1000 /var/moongsan/log
# Builder에서 생성된 JAR 복사
COPY --from=builder /app/build/libs/*.jar app.jar
# 기동 시 로그를 파일에 남기면서 출력
CMD ["sh", "-c", "java -jar app.jar | tee -a /var/moongsan/log/be_moongsan.log"]
