FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw \
    && ./mvnw --batch-mode dependency:go-offline

COPY src/ src/

RUN ./mvnw --batch-mode clean package -DskipTests \
    && JAR_FILE="$(find target \
        -maxdepth 1 \
        -type f \
        -name '*.jar' \
        ! -name '*.original' \
        | head -n 1)" \
    && test -n "${JAR_FILE}" \
    && cp "${JAR_FILE}" target/application.jar


FROM curlimages/curl:8.16.0 AS otel-agent

ARG OTEL_AGENT_VERSION=2.30.0

RUN curl -fsSL \
    "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar" \
    -o /tmp/opentelemetry-javaagent.jar


FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

COPY --from=builder \
    --chown=10001:10001 \
    /workspace/target/application.jar \
    /app/application.jar

COPY --from=otel-agent \
    --chown=10001:10001 \
    /tmp/opentelemetry-javaagent.jar \
    /app/opentelemetry-javaagent.jar

USER 10001:10001

EXPOSE 8081

ENTRYPOINT ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-jar", "/app/application.jar"]
