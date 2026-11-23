# 1. Patrones de Diseño y Justificación Arquitectónica

Este documento identifica los patrones arquitectónicos y de diseño implementados en la solución **Almara**, su ubicación exacta dentro del código fuente y la justificación técnica de cada decisión.

---

## 1.1 Estilo Arquitectónico Base: MVC Modular en Capas

Almara adopta un estilo **Monolito Modular con separación MVC (Modelo–Vista–Controlador) en capas**, dividido en cinco dominios funcionales independientes dentro de `almara-backend/src/main/java/com/almara/modules/`:

| Capa MVC / Arquitectónica | Responsabilidad | Ubicación en el Repositorio |
|---|---|---|
| **Vista (View)** | Interfaces de usuario desacopladas que consumen la API REST y WebSockets. | `almara-frontend/web/` (Admin e Institucional) y `almara-frontend/app/` (Ciudadano). |
| **Controlador (Controller)** | Exposición de endpoints REST (`/api/v1/...`), validación de entrada y delegación a servicios. | `modules/*/controllers/` |
| **Servicio / Aplicación (Service)** | Orquestación de casos de uso, reglas de negocio, privacidad ($k$-anonimato) y coordinación de eventos. | `modules/*/services/` |
| **Modelo (Model)** | Entidades JPA/dominio, objetos de valor y máquinas de estado. | `modules/*/models/` |
| **Acceso a Datos (Repository)** | Abstracción de persistencia sobre PostgreSQL + PostGIS y Redis. | `modules/*/repositories/` |

**Justificación arquitectónica:**
- **Alta cohesión y bajo acoplamiento:** Cada módulo (`admin`, `aggregation`, `emotion`, `geo`, `institutional`) encapsula sus propios controladores, modelos, servicios y repositorios, evitando dependencias circulares (Principio de Aciclicidad - ADP).
- **Escalabilidad evolutiva:** Permite desarrollar y desplegar de forma ágil hoy, manteniendo fronteras limpias para extraer módulos como microservicios en el futuro si el volumen de eventos ciudadanos crece.

---

## 1.2 Catálogo de Patrones de Diseño Implementados

### 1. Strategy (Comportamiento — GoF)
- **Dónde se implementa:**
  - `modules/aggregation/services/strategies/`: `EstrategiaResolucionEspacial`, `EstrategiaResolucionBase`, `EstrategiaZoomMacro`, `EstrategiaZoomMedio`, `EstrategiaZoomDetalle`.
  - `modules/geo/services/`: `GpsToH3ResolutionStrategy`, `ManualCatalogResolutionStrategy`.
- **Justificación arquitectónica:**
  - En el mapa emocional interactivo (HU-05), el nivel de zoom del usuario determina dinámicamente la resolución espacial de las celdas hexagonales H3 (macro, medio o detalle) sin condicionales rígidos.
  - En la captura de ubicación (HU-04), permite intercambiar de forma transparente la resolución por coordenadas GPS frente a la selección manual por catálogo de zonas urbanas de Popayán.

### 2. State + Factory Method (Comportamiento y Creacional — GoF)
- **Dónde se implementa:**
  - `modules/admin/models/state/`: `EstadoEmocion`, `EstadoEmocionActiva`, `EstadoEmocionInactiva`.
  - `modules/admin/models/state/institutional/`: `EstadoCuentaInstitucional`, `EstadoPendienteAprobacionState`, `EstadoActivaState`, `EstadoSuspendidaState`, `EstadoInactivaState`, `FabricaEstadoInstitucional`.
- **Justificación arquitectónica:**
  - Encapsula las reglas de transición válidas del ciclo de vida de las emociones del catálogo y de las cuentas institucionales (ej. una cuenta *Pendiente* puede pasar a *Activa*, una *Activa* a *Suspendida*, evitando transiciones ilegales).
  - La fábrica (`FabricaEstadoInstitucional`) desacopla la instanciación del estado concreto a partir del valor persistido en base de datos.

### 3. Observer y Publish-Subscribe (Comportamiento / Arquitectura Orientada a Eventos)
- **Dónde se implementa:**
  - `modules/admin/patterns/observer/`: `SujetoConfiguracionUmbralMapa`, `ObservadorCambioUmbralMapa`, `ObservadorCacheProyeccionMapa`.
  - `modules/aggregation/websocket/`: `OyenteEventosEmocionMapa`, `PublicadorSincronizacionMapa`, `ManejadorWebSocketMapa`, `MensajeWebSocketMapa`.
- **Justificación arquitectónica:**
  - **Sincronización en tiempo real (HU-07):** Cuando un ciudadano registra una emoción, el backend responde en menos de 1 segundo y publica un evento interno que dispara la agregación y la difusión por WebSocket hacia todos los mapas conectados.
  - **Coherencia de caché de privacidad:** Cuando un administrador actualiza el umbral de $k$-anonimato, el patrón *Observer* invalida automáticamente la caché de proyecciones del mapa sin acoplar el módulo `admin` con `aggregation`.

### 4. Adapter + Ports & Adapters / Arquitectura Hexagonal (Estructural — GoF)
- **Dónde se implementa:**
  - `modules/institutional/ports/`: `PasarelaPagoPort`, `ServicioNotificacionPort`.
  - `modules/institutional/adapters/payment/`: `AdaptadorStripePasarelaPago`, `AdaptadorPasarelaPagoSimulada`.
  - `modules/institutional/adapters/notification/`: `AdaptadorNotificacionSmtp`, `AdaptadorNotificacionLog`.
  - `modules/admin/patterns/adapter/`: `AdaptadorEspacialPostgisH3`, `AdaptadorEspacialPostgisH3Impl`.
- **Justificación arquitectónica:**
  - Aísla el dominio de negocio de proveedores externos (Stripe, servidor SMTP, motor geoespacial PostGIS/H3).
  - Permite alternar entre pasarela de pagos real (**Stripe**) y pasarela simulada para pruebas locales o académicas sin modificar una sola línea de la lógica de suscripciones SaaS.

### 5. DTO — Data Transfer Object (Patrón Empresarial)
- **Dónde se implementa:**
  - `modules/admin/dto/`, `modules/institutional/dto/` y payloads tipados en controladores REST y WebSocket (`MensajeWebSocketMapa`).
- **Justificación arquitectónica:**
  - Evita exponer directamente entidades JPA en la API pública.
  - Refuerza la seguridad y el anonimato ciudadano (HU-01), aceptando estrictamente los campos permitidos y descartando cualquier dato personal identificable (PII).

### 6. Repository (Patrón Empresarial — Spring Data JPA)
- **Dónde se implementa:**
  - `modules/*/repositories/` en los cinco módulos del backend.
- **Justificación arquitectónica:**
  - Separa la lógica de negocio del acceso a datos SQL/PostGIS, facilitando consultas espaciales personalizadas, pruebas unitarias con *mocks* y mantenibilidad.

### 7. Cache-Aside y Rate Limiting con Redis (Patrones de Rendimiento y Resiliencia)
- **Dónde se implementa:**
  - Capa de servicios de `aggregation` (caché de celdas agregadas) y `emotion` (ventana de bloqueo/cooldown por token de sesión temporal anónimo).
- **Justificación arquitectónica:**
  - Garantiza tiempos de respuesta inferiores a 300 ms en consultas del mapa y detalle de zona (HU-05, HU-06).
  - Previene spam y envíos duplicados manteniendo el 100% de anonimato ciudadano (HU-01).

---

## 1.3 Resumen de Trazabilidad Patrón ↔ Historia de Usuario

| Patrón | Categoría | Módulos Principales | Historias de Usuario Soportadas |
|---|---|---|---|
| **MVC Modular** | Arquitectónico | Todos los módulos | HU-01 a HU-24 |
| **DTO** | Empresarial | `emotion`, `admin`, `institutional` | HU-01, HU-02, HU-03, HU-11 a HU-24 |
| **Strategy** | Comportamiento (GoF) | `geo`, `aggregation` | HU-04, HU-05 |
| **State + Factory** | Comportamiento / Creacional | `admin` | HU-11, HU-12, HU-14, HU-15 |
| **Observer / Pub-Sub** | Comportamiento / Eventos | `aggregation`, `admin` | HU-01, HU-07, HU-13 |
| **Adapter + Ports** | Estructural / Hexagonal | `institutional`, `admin` | HU-16 a HU-24 |
| **Repository** | Empresarial | Todos los módulos | HU-01 a HU-24 |
| **Cache-Aside / Rate Limiting** | Rendimiento / Seguridad | `emotion`, `aggregation` | HU-01, HU-05, HU-06 |
