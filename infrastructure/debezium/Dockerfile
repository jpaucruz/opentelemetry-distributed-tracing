FROM curlimages/curl:8.16.0 AS dependencies

ARG OTEL_AGENT_VERSION=2.30.0
ARG DEBEZIUM_VERSION=3.6.1.Final

RUN curl -fsSL \
    "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar" \
    -o /tmp/opentelemetry-javaagent.jar

RUN curl -fsSL \
    "https://repo1.maven.org/maven2/io/debezium/debezium-interceptor/${DEBEZIUM_VERSION}/debezium-interceptor-${DEBEZIUM_VERSION}.jar" \
    -o /tmp/debezium-interceptor.jar


FROM quay.io/debezium/connect:3.6.1.Final

COPY --from=dependencies \
    /tmp/opentelemetry-javaagent.jar \
    /kafka/opentelemetry-javaagent.jar

COPY --from=dependencies \
    /tmp/debezium-interceptor.jar \
    /kafka/connect/debezium-connector-postgres/debezium-interceptor.jar