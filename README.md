# Proyecto de Microservicios con Observabilidad

Proyecto Java Spring Boot con arquitectura de microservicios y observabilidad integrada mediante ELK Stack.

## Arquitectura

```
┌─────────────────────┐     ┌──────────────────┐     ┌─────────────────────┐
│ kpis-microservice   │ ──▶ │ sales-service    │     │ accountable-service │
│   (Puerto 8080)     │     │  (Puerto 9096)  │     │   (Puerto 9095)     │
└──────────┬──────────┘     └────────┬─────────┘     └──────────┬──────────┘
           │                          │                          │
           └──────────────────────────┼──────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────┐
│           ELK Stack                      │
│  ┌─────────┐  ┌──────────┐  ┌────────┐  │
│  │Elasticse.|  │ Logstash │  │ Kibana │  │
│  │  :9200   │◀─│  :5000   │  │ :5601  │  │
│  └─────────┘  └──────────┘  └────────┘  │
└─────────────────────────────────────────┘
```

### Microservicios

| Servicio | Puerto | Base de Datos | Descripción |
|----------|--------|---------------|-------------|
| kpis-microservice | 8080 | PostgreSQL | Agrega KPIs de sales y accountable por área |
| sales-service | 9096 | MariaDB | Proporciona KPIs de ventas |
| accountable-service | 9095 | PostgreSQL | Proporciona KPIs contables |

### Infraestructura

| Servicio | Puerto | Imagen |
|----------|--------|--------|
| mariadb | 3306 | mariadb:11.4 |
| postgres-kpis | 5432 | postgres:16-alpine |
| postgres-accountable | 5436 | postgres:16-alpine |
| elasticsearch | 9200 | elasticsearch:8.11.0 |
| logstash | 5000, 5044, 9600 | logstash:8.11.0 |
| kibana | 5601 | kibana:8.11.0 |

---

## Perfiles de Spring

El proyecto utiliza **Spring Profiles** para adaptar la configuración según el entorno:

### Perfil `local`

Desarrollo local sin contenedores.

```bash
./gradlew bootRun
```

**Configuración:**
- Base de datos: conecta a `localhost:6432` (PostgreSQL)
- Logs: archivo rolling en `logs/app.log`

**Logback:** `logback-local.xml`
- Escribe a archivo con patrón simple
- Logger específico para `KpiService` con `additivity="false"` para evitar duplicados

### Perfil `docker`

Ejecución en contenedores Docker.

```bash
docker compose up -d
```

**Configuración:**
- Base de datos: conecta a `postgres-kpis:5432` (nombre del contenedor)
- Logs: envía directamente a Logstash via TCP
- Variable: `SPRING_PROFILES_ACTIVE=docker`

**Logback:** `logback-docker.xml`
- Usa `LogstashTcpSocketAppender` para envío directo a Logstash
- Puerto: 5000 (TCP)
- Host: `logstash` (configurable via `LOGSTASH_HOST`)

### Perfil default (production)

Cualquier entorno que no sea `local` ni `docker`.

**Logback:** `logback-structured.xml`
- Logs estructurados JSON a consola

---

## Configuración de Logback

### Ubicación de archivos

```
src/main/resources/
├── logback-spring.xml          # Configuración principal (selección de perfil)
├── logback/
│   ├── logback-local.xml       # Perfil local
│   ├── logback-docker.xml      # Perfil docker
│   └── logback-structured.xml  # Perfil default
```

### Perfil docker (logback-docker.xml)

```xml
<appender name="LOGSTASH_TCP" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
    <destination>${logstashHost}:${logstashPort}</destination>
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <customFields>{"service":"${springAppName}"}</customFields>
    </encoder>
</appender>
```

**Características:**
- Envío directo a Logstash (sin Filebeat)
- Codec JSON automático
- Campo `service` personalizado para identificar el microservicio
- Incluye contexto MDC automáticamente

### Configuración de Logstash

El pipeline de Logstash (`observability/logstash/pipeline/logstash.conf`) recibe los eventos por TCP:

```conf
input {
  tcp {
    port => 5000
    codec => json
  }
}

filter {
  if [service] {
    mutate {
      add_field => { "service_name" => "%{[service]}" }
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "app-%{[service_name]}-%{+YYYY.MM.dd}"
  }
}
```

---

## Uso de MDC (Mapped Diagnostic Context)

El `KpiService` utiliza **MDC** para agregar contexto de trazabilidad a los logs:

### Implementación

```java
public KpiResponse buildKpis(KpiRequest request) {
    try {
        // ... validación ...
        
        MDC.put("user", user);  // Agrega usuario al contexto
        log.info("User validated: {}", user);
        
        // ... llamadas async ...
        
        return new KpiResponse(user, area, accountable, sales);
    } finally {
        MDC.clear();  // Limpia el contexto al finalizar
    }
}
```

### Por qué usar MDC?

1. **Trazabilidad**: Cada log incluye el usuario que ejecutó la solicitud
2. **Correlación**: Facilita requests en Kibana
3. **Contexto automático**: El `LogstashEncoder` incluye las variables MDC en el JSON de forma automática

### Ejemplo de log generado

```json
{
  "@timestamp": "2026-03-28T12:00:00.000Z",
  "level": "INFO",
  "logger": "KpiService",
  "message": "User validated: john@example.com",
  "service": "kpis-microservice",
  "user": "john@example.com"
}
```

---

## Ejecutar el Proyecto

### Desarrollo local

```bash
# Iniciar PostgreSQL (opcional)
docker compose up -d postgres-kpis

# Ejecutar aplicación
cd kpis-microservice
./gradlew bootRun
```

### Producción (Docker)

```bash
# Iniciar todos los servicios
docker compose up -d

# Ver logs de kpis-microservice
docker compose logs -f kpis-microservice
```

### Ver logs en Kibana

1. Acceder a `http://localhost:5601`
2. Crear index pattern: `app-*`
3. Buscar por `service_name: kpis-microservice`

---

## Endpoints

### kpis-microservice

```bash
curl -X POST http://localhost:8080/kpis \
  -H "Content-Type: application/json" \
  -d '{"user": "admin", "area": "NORTE"}'
```

### sales-service

```bash
curl -X POST http://localhost:9096/sales-kpis \
  -H "Content-Type: application/json" \
  -d '{"area": "NORTE"}'
```

### accountable-service

```bash
curl -X POST http://localhost:9095/accountable-kpis \
  -H "Content-Type: application/json" \
  -d '{"area": "NORTE"}'
```
