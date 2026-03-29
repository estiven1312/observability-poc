# accountable-service

Servicio Spring Boot con endpoint `POST /accountable-kpis` que consulta KPIs por `area` en PostgreSQL usando `JdbcTemplate`.

## Endpoint

- URL: `POST /accountable-kpis`
- Request body:

```json
{
  "area": "FINANCE"
}
```

- Response body:

```json
[
  {
    "area": "FINANCE",
    "acceptedInvoices": 1200.50,
    "rejectedInvoices": 300.00
  }
]
```

## Inicializacion de PostgreSQL en contenedor

El script `docker/postgres/init/01-init-accountable.sql` se ejecuta automaticamente al crear el contenedor por primera vez (cuando el volumen esta vacio) y hace lo siguiente:

- Crea el esquema `accounting`
- Crea la tabla `accounting.invoices`
- Crea indices por `area` y `status`
- Crea la vista `public.invoices` para compatibilidad con la consulta actual
- Inserta mas de 100 filas (actualmente 255)

## Levantar con Docker Compose

```bash
docker compose up -d
```

## Reiniciar la data inicial (volver a correr el script)

```bash
docker compose down -v
docker compose up -d
```

## Probar endpoint

```bash
curl -X POST 'http://localhost:8080/accountable-kpis' \
  -H 'Content-Type: application/json' \
  -d '{"area":"FINANCE"}'
```
