# Almara — Backend (`almara-backend`)

Servicio central del sistema Almara ("Urban Emotional Intelligence"), desarrollado con **Java 21 LTS** y **Spring Boot 3.x**.

El proyecto está diseñado bajo los principios de la asignatura de **Arquitectura de Software**:
- **Arquitectura Modular / Hexagonal** organizada por capacidades de negocio (*Bounded Contexts*).
- **Privacy by Design**: Las coordenadas exactas nunca se persisten; se convierten a celdas hexagonales H3 y se descartan. Solo se exponen datos agregados si se supera el umbral de $k$-anonimato.
- **Event-Driven & Realtime**: Procesamiento de eventos en tiempo real con motor CEP y distribución vía WebSocket (STOMP).
- **Idempotencia y Trazabilidad**: Auditoría inmutable de operaciones críticas.

## Estructura de Paquetes (`com.almara`)

```
src/main/java/com/almara/
├── AlmaraApplication.java (Punto de entrada Spring Boot)
├── common/                (Seguridad JWT, filtros, manejo global de errores)
└── modules/
    ├── emotion/           (HU-01 a HU-03: Registro anónimo de emoción, intensidad y comentario)
    ├── geo/               (HU-04, HU-14: Integración H3, centroides y polígonos GeoJSON)
    ├── aggregation/       (HU-05, HU-08, CEP: Cálculo porcentual, tendencias y k-anonimato)
    ├── realtime/          (HU-07: Canales WebSocket/STOMP para actualización del mapa en vivo)
    ├── admin/             (HU-11 a HU-15: Catálogo de emociones, umbrales y config global)
    ├── institutional/     (HU-16 a HU-22: Clientes corporativos, planes, auth y analítica)
    └── audit/             (RNF-13: Bitácora inmutable de auditoría)
```

## Recursos (`src/main/resources`)

- `application.yml.example`: Plantilla de configuración con conexiones a PostgreSQL, Redis y seguridad JWT.
- `db/migration/`: Scripts versionados de base de datos (Flyway/Liquibase).

## Requisitos Previos

1. **Java 21 LTS**
2. **Maven 3.9+** o wrapper
3. Servicios de infraestructura activos (`almara-infra`):
   ```bash
   cd ../almara-infra
   docker compose up -d
   ```
