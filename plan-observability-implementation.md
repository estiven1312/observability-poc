# Observability Implementation Plan

## Objective
Implement observability across three Spring Boot microservices: `accountable-service`, `sales-service`, and `kpis-microservice`.

**Initial Backend:** Elastic Stack (ELK)  
**Final Backend:** Grafana Stack (LGTM) - migration planned for later

---

## Project Analysis Summary

| Service | Framework | Database | Port | Current Observability |
|---------|-----------|----------|------|----------------------|
| **accountable-service** | Spring Boot 4.0.5 | PostgreSQL | 9095 | Actuator dependency (no config) |
| **sales-service** | Spring Boot 4.0.5 | MariaDB | 9096 | Actuator dependency (no config) |
| **kpis-microservice** | Spring Boot 4.0.4 | PostgreSQL | 8080 | Actuator + Logstash encoder (ready for POC) |

---

## Phase 1: Single Service POC (kpis-microservice)

### 1.1 Add Observability Dependencies

Add to `kpis-microservice/build.gradle.kts`:

```kotlin
dependencies {
    // Existing dependencies...
    
    // Micrometer for metrics
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    
    // OpenTelemetry for distributed tracing
    implementation("io.opentelemetry:opentelemetry-api")
    implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
}
```

### 1.2 Configure application.yaml

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  endpoint:
    health:
      show-details: always
  metrics:
    tags:
      application: ${spring.application.name}

# OpenTelemetry configuration
otel:
  service:
    name: kpis-microservice
  exporter:
    otlp:
      endpoint: http://localhost:4317
```

### 1.3 Add Logstash Configuration

Create `logback-spring.xml` in each service (already done for kpis-microservice):

```xml
<configuration>
    <springProperty name="springAppName" source="spring.application.name"/>
    
    <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"service":"${springAppName}"}</customFields>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="JSON_CONSOLE"/>
    </root>
</configuration>
```

### 1.4 Create Docker Compose for ELK

Create `observability/docker-compose.elk.yml`:

```yaml
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data

  logstash:
    image: docker.elastic.co/logstash/logstash:8.11.0
    ports:
      - "5044:5044"
      - "9600:9600"
    volumes:
      - ./logstash/pipeline:/usr/share/logstash/pipeline
    depends_on:
      - elasticsearch

  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    ports:
      - "5601:5601"
    environment:
      ELASTICSEARCH_HOSTS: http://elasticsearch:9200
    depends_on:
      - elasticsearch

volumes:
  elasticsearch_data:
```

### 1.5 Create Logstash Pipeline

Create `observability/logstash/pipeline/logstash.conf`:

```conf
input {
  beats {
    port => 5044
  }
}

filter {
  json {
    source => "message"
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "%{[fields][service]}-%{+YYYY.MM.dd}"
  }
}
```

---

## Phase 2: Extend to All Services

### 2.1 Apply Same Changes to:

- **accountable-service** (add dependencies + config)
- **sales-service** (add dependencies + config)

### 2.2 Update Docker Compose

Add filebeat to collect logs from all services:

```yaml
  filebeat:
    image: docker.elastic.co/beats/filebeat:8.11.0
    volumes:
      - ./filebeat/filebeat.yml:/usr/share/filebeat/filebeat.yml
      - /var/lib/docker/containers:/var/lib/docker/containers:ro
    depends_on:
      - logstash
```

---

## Phase 3: Distributed Tracing

### 3.1 Add OpenTelemetry Collector

Create `observability/docker-compose.otel.yml`:

```yaml
services:
  otel-collector:
    image: otel/opentelemetry-collector-contrib
    ports:
      - "4317:4317"      # OTLP receiver
      - "4318:4318"      # OTLP HTTP
      - "8888:8888"      # Prometheus exposed metrics
      - "8889:8889"      # Prometheus exposed metrics
    volumes:
      - ./otel/collector-config.yml:/etc/otelcol-contrib/config.yaml
```

### 3.2 Update Services to Send Traces

Each service sends to OTel Collector which forwards to Elasticsearch.

---

## Phase 4: Migration to Grafana Stack (Later)

### 4.1 New Docker Compose

Replace ELK with:

```yaml
services:
  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml

  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
    volumes:
      - grafana_data:/var/lib/grafana

  loki:
    image: grafana/loki
    ports:
      - "3100:3100"

  tempo:
    image: grafana/tempo
    ports:
      - "4317:4317"
      - "16686:16686"

volumes:
  grafana_data:
```

### 4.2 Update Service Configurations

Change metrics/traces exporters from ELK to Prometheus/ Tempo endpoints.

---

## Implementation Order

| Step | Task | Service |
|------|------|---------|
| 1 | Add dependencies | kpis-microservice |
| 2 | Configure actuator + metrics | kpis-microservice |
| 3 | Set up ELK Docker Compose | Infrastructure |
| 4 | Test POC end-to-end | kpis-microservice |
| 5 | Add dependencies + config | accountable-service |
| 6 | Add dependencies + config | sales-service |
| 7 | Add distributed tracing | All services |
| 8 | (Future) Migrate to Grafana | All services |

---

## Key Files to Create/Modify

```
observability/
├── docker-compose.elk.yml          # Phase 1
├── docker-compose.grafana.yml       # Phase 4 (future)
├── logstash/
│   └── pipeline/
│       └── logstash.conf
├── otel/
│   └── collector-config.yml         # Phase 3
└── filebeat/
    └── filebeat.yml                 # Phase 2

accountable-service/
├── build.gradle.kts                 # Add dependencies
└── src/main/resources/
    ├── application.yaml             # Add config
    └── logback-spring.xml           # Add JSON logging

sales-service/
├── build.gradle.kts                 # Add dependencies
└── src/main/resources/
    ├── application.yaml             # Add config
    └── logback-spring.xml           # Add JSON logging

kpis-microservice/
├── build.gradle.kts                 # Add dependencies
└── src/main/resources/
    └── application.yaml             # Add config
```

---

## Quick Start Commands

```bash
# Start ELK stack
docker compose -f observability/docker-compose.elk.yml up -d

# Start a single service with observability
cd kpis-microservice
./gradlew bootRun

# Verify metrics
curl http://localhost:8080/actuator/prometheus

# Access Kibana
# http://localhost:5601
```
