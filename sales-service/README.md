# sales-service

Servicio Spring Boot con endpoint `POST /sales-kpis` que consulta KPIs de ventas por area usando `JdbcTemplate` y MariaDB.

## Requisitos

- Java 25
- Docker y Docker Compose

## Inicializacion de base de datos (Docker Compose)

MariaDB carga los scripts de inicializacion en:

- `docker/mariadb/init/01_schema.sql`
- `docker/mariadb/init/02_seed_sales_transactions.sql`

El script de seed crea **150 registros** en `sales_transactions`.

Si ya tienes el volumen creado y quieres volver a ejecutar la carga inicial:

```bash
docker compose down -v
docker compose up -d mariadb
```

## Ejecutar la aplicacion

```bash
./gradlew bootRun
```

La app levanta en `http://localhost:9096`.

## Endpoint

- Metodo: `POST`
- Ruta: `/sales-kpis`
- Body:

```json
{
  "area": "NORTE"
}
```

- Si envias `area` vacia o `null`, retorna KPIs de todas las areas.

Ejemplo con curl:

```bash
curl -X POST http://localhost:9096/sales-kpis \
  -H "Content-Type: application/json" \
  -d '{"area":"NORTE"}'
```
