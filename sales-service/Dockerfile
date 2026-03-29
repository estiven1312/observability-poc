FROM eclipse-temurin:25-jdk-alpine AS compiler
WORKDIR /app

COPY gradlew ./
COPY gradlew.bat ./
COPY gradle ./gradle
COPY settings.gradle.kts ./
COPY build.gradle.kts ./
COPY src ./src

RUN chmod +x gradlew
RUN ./gradlew clean bootJar -x test --no-daemon

FROM eclipse-temurin:25-jdk-alpine AS extractor
WORKDIR /layers
COPY --from=compiler /app/build/libs/*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=extractor /layers/dependencies/ ./
COPY --from=extractor /layers/snapshot-dependencies/ ./
COPY --from=extractor /layers/spring-boot-loader/ ./
COPY --from=extractor /layers/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
