FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY gradlew gradlew.bat ./

RUN chmod +x gradlew

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src ./src
RUN ./gradlew clean bootJar -x test

FROM eclipse-temurin:21-jdk
WORKDIR /app
RUN mkdir -p /var/moongsan/log && chown -R 1000:1000 /var/moongsan/log
COPY --from=builder /app/build/libs/*.jar app.jar
CMD ["sh", "-c", "java -jar app.jar | tee -a /var/moongsan/log/be_moongsan.log"]