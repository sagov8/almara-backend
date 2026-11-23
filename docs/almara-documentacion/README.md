# Documentación Arquitectónica — Proyecto Almara

Bienvenido a la documentación arquitectónica unificada de **Almara (Urban Emotional Intelligence)**, plataforma para la captura anónima, procesamiento en tiempo real y visualización analítica del clima emocional urbano en la ciudad de Popayán.

Este directorio centraliza todos los entregables obligatorios de arquitectura y diseño con rutas verificadas e imágenes en formato `.png`.

---

## Entregables Consolidados (PDF y Excel)

- **Documento Unificado en PDF:** [`Documentacion_Arquitectura_Almara.pdf`](Documentacion_Arquitectura_Almara.pdf) — Contiene portada, arquitectura MVC, patrones de diseño, Modelo C4 completo (C1–C4), diagramas de componentes por módulo e índice de historias de usuario.
- **Historias de Usuario en Excel (por hojas):** [`Historias_de_Usuario_Almara.xlsx`](Historias_de_Usuario_Almara.xlsx) — Libro formateado con una hoja de `Índice` con hipervínculos y 21 hojas individuales (`HU-01` a `HU-24`).

---

## Índice de Documentos en Markdown (Lectura Directa en GitHub)

| # | Apartado | Documento | Contenido Principal |
|---|---|---|---|
| **1** | **Patrones de Diseño** | [01-patrones-de-diseno.md](01-patrones-de-diseno.md) | Arquitectura MVC modular, catálogo de patrones GoF y arquitectónicos implementados en código real y su justificación técnica. |
| **2** | **Modelo C4** | [02-modelo-c4.md](02-modelo-c4.md) | Niveles C1 (Contexto), C2 (Contenedores), C3 (Componentes) y C4 (Despliegue) con diagramas `.png` y descripción detallada. |
| **3** | **Diagrama de Componentes** | [03-diagrama-de-componentes.md](03-diagrama-de-componentes.md) | Vista de componentes de alto nivel y desglose por cada módulo funcional (`admin`, `aggregation`, `emotion`, `geo`, `institutional`) con `.png`. |
| **4** | **Historias de Usuario** | [04-historias-de-usuario.md](04-historias-de-usuario.md) | Especificación completa de las 21 historias de usuario (**HU-01 a HU-24**) extraídas del archivo oficial de entrega, con criterios de aceptación, RNF y mockups. |

---

## Resumen de la Estructura MVC del Proyecto

El proyecto refleja con claridad el patrón **MVC (Modelo-Vista-Controlador)** distribuido entre un backend modular en **Spring Boot (Java 21)** y dos clientes frontend desacoplados (**React + TypeScript** y **React Native + Expo**):

```text
almara/
├── almara-backend/src/main/java/com/almara/
│   ├── config/                         # Configuración transversal (CORS, seguridad, WebSocket, caché)
│   ├── common/                         # Excepciones globales y utilidades compartidas
│   └── modules/                        # 5 Módulos de dominio bajo arquitectura MVC + Capas
│       ├── admin/                      # controllers/, models/, services/, repositories/, dto/, patterns/, security/
│       ├── aggregation/                # controllers/, models/, services/, repositories/, websocket/
│       ├── emotion/                    # controllers/, models/, services/, repositories/
│       ├── geo/                        # controllers/, models/, services/, repositories/
│       └── institutional/              # controllers/, models/, services/, repositories/, dto/, ports/, adapters/, security/
├── almara-frontend/
│   ├── web/                            # Vista Web (Admin + Cliente Institucional SaaS — React + Vite + Tailwind)
│   └── app/                            # Vista Móvil/Web Ciudadana (React Native + Expo + Leaflet)
├── almara-infra/                       # Infraestructura contenerizada (PostgreSQL + PostGIS, Redis)
├── documentacion/                      # Documentación unificada de arquitectura (este directorio)
├── diagramas-uml/                      # Fuentes editables (.puml, .drawio, .svg) de diagramas UML y C4
└── docs/                               # Especificaciones adicionales, planes de implementación y capturas
```

---

## Verificación de Integridad

- Todas las imágenes referenciadas en esta carpeta se encuentran almacenadas localmente en [`documentacion/imagenes/`](imagenes/) en formato `.png` (y mockups en `imagenes/mockups/`) con nombres normalizados sin espacios para garantizar compatibilidad total en GitHub.
- Los archivos fuente editables (`.puml` y `.drawio`) se conservan en [`../diagramas-uml/`](../diagramas-uml/) para trazabilidad y mantenimiento futuro.
