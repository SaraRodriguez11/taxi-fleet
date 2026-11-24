# Sistema de Administración y Control Contable de Flota de Taxis

Aplicación full stack para controlar producción, ahorros, gastos y Pico y Placa de una flota de taxis, con
reportes semanales y mensuales exportables a PDF.

```
taxi-fleet/
├── database/    Scripts SQL para pgAdmin (rol y base de datos)
├── backend/     Java 17 · Spring Boot 3.5 · JPA · Flyway · JWT   → se abre en IntelliJ IDEA
└── frontend/    React 19 · Vite · TypeScript · Tailwind 4        → se abre en Visual Studio Code
```

## Cómo ejecutarlo (orden: base → backend → frontend)

**1. Base de datos (PostgreSQL local + pgAdmin)**
En pgAdmin, Query Tool sobre la base `postgres`:
1. Ejecuta `database/01_crear_rol.sql` (crea el rol `taxi` con clave `taxi`).
2. Ejecuta **solo** `database/02_crear_base_de_datos.sql` (`CREATE DATABASE taxi_fleet OWNER taxi;`; debe correr sola).
Las tablas las crea Flyway al arrancar el backend.

**2. Backend en IntelliJ** → ver `backend/README.md`. Corre en http://localhost:8080.

**3. Frontend en VS Code** → ver `frontend/README.md`. Corre en http://localhost:5173.

Variable opcional del backend: `JWT_SECRET` (32+ caracteres; cámbiala fuera de desarrollo).

## Flujo de uso

1. **Crear cuenta** (correo y contraseña) e iniciar sesión. Cada propietario solo ve sus propios datos.
2. **Conductores** y **Taxis** → registra tus vehículos; aparecen en **Inicio** ("Mis vehículos") con el Pico y Placa de hoy.
3. **Control diario** → "Generar control del año" crea todos los días con el Pico y Placa marcado. Cambia el estado
   con el selector (rojo = pendiente, verde = entregado), agrega o elimina días.
4. **Reportes** → mensual o semanal, todo el año; descarga el PDF.

### Seguridad
Contraseñas con BCrypt, sesión con JWT (8 h), todas las consultas filtradas por propietario, y los reportes se generan en el navegador. Los datos anteriores a las cuentas (si existían) quedan asignados
a un propietario deshabilitado `datos-anteriores@flota.local`.

## Arquitectura del backend

Capas con dependencias en un solo sentido: `web → service → repository → domain`.

| Capa | Responsabilidad |
|---|---|
| `web` | Controladores REST delgados, validación de entrada, `GlobalExceptionHandler` (RFC 9457 `ProblemDetail`) |
| `service` | Casos de uso y transacciones. Mapea a DTO **dentro** de la transacción (`open-in-view=false`) |
| `domain` | Entidades con comportamiento (`DailyRecord.register` calcula el neto y valida invariantes) |
| `repository` | Spring Data JPA, `@EntityGraph` contra N+1, consultas agregadas con proyecciones a `record` |
| `dto` | `record` inmutables; los montos son `BigDecimal` (nunca `double` con dinero) |

### Patrones y prácticas aplicadas

- **Strategy** — `PicoPlacaRule` abstrae la normativa (`RotatingDigitRule` implementa la rotación de Bogotá); `ReportPeriod` (enum) decide a qué semana/mes pertenece una fecha.
- **Decorator** — `HolidayExemptionRule` envuelve cualquier regla base para eximir festivos, consultando un `HolidayCalendar` (`ColombianHolidayCalendar` calcula los festivos de cualquier año, incluida la Semana Santa).
- **Modelo de dominio rico** — el neto (`recibida − ahorro − gastos`) y la regla "el neto no puede ser negativo" viven en la entidad, y además están protegidos por `CHECK` en la base de datos (defensa en profundidad).
- **Configuración tipada** — `FleetProperties` (`@ConfigurationProperties` con `record`): la norma de Pico y Placa se cambia en `application.yml`, sin recompilar.
- **Migraciones versionadas** — Flyway gobierna el esquema; Hibernate solo valida (`ddl-auto=validate`).
- **Concurrencia optimista** — `@Version` + `version` en el request: si dos administradores editan el mismo día, el segundo recibe `409` en vez de pisar datos.
- **Idempotencia** — generar el año dos veces no duplica días (restricción única `taxi_id + record_date` y omisión de existentes).
- **Rendimiento** — secuencias con `allocationSize=50` + `batch_size=50` para insertar 365 filas en lote; paginación en servidor; índice por fecha para reportes; `SUM` en la base de datos para el panel.
- **Bajas lógicas** — taxis y conductores se desactivan, no se borran: el historial contable se conserva.
- **Lógica pura y testeable** — `ReportAggregator`, `ComplianceEvaluator` y las reglas de Pico y Placa no dependen de Spring; sus pruebas unitarias corren sin base de datos.

### API

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/register` · `/login` · GET `/me` | Cuenta y sesión (JWT) |
| GET/POST | `/api/taxis` · `/api/drivers` | Listar / crear |
| GET/PUT/DELETE | `/api/taxis/{id}` · `/api/drivers/{id}` | Detalle / editar / dar de baja |
| GET | `/api/taxis/{id}/daily-records?year=&month=&page=&size=` | Control diario paginado |
| POST | `/api/taxis/{id}/daily-records/generate?year=&expectedProduction=` | Genera el año |
| POST | `/api/taxis/{id}/daily-records` | Agrega un día suelto |
| POST | `/api/taxis/{id}/daily-records/recalculate-pico-placa?year=&expectedProduction=` | Recalcula el Pico y Placa de los días ya creados |
| PUT | `/api/daily-records/{id}` | Registra producción, ahorro y gastos |
| PATCH | `/api/daily-records/{id}/status` | Cambia solo el estado |
| DELETE | `/api/daily-records/{id}` | Elimina un día |
| GET | `/api/pico-placa?date=` | Dígitos restringidos en una fecha (hoy por defecto) |
| GET | `/api/dashboard/summary?from=&to=` | Totales de la flota |
| GET | `/api/reports/weekly?year=` · `/monthly?year=` | Reportes consolidados |

## Arquitectura del frontend

- `api/` — cliente HTTP único (`client.ts`), funciones por recurso (`resources.ts`) y hooks de TanStack Query (`hooks.ts`) con claves centralizadas e invalidación de datos derivados.
- `pages/` — una página por módulo; `components/ui.tsx` — piezas visuales reutilizables.
- `lib/pdf.ts` — exportación con jsPDF + autoTable, cargada bajo demanda (`import()`) para no inflar el bundle inicial.
- Tablas grandes: paginación en servidor (50 filas) y filtro por mes en lugar de renderizar 365 filas.
- Los errores de validación del servidor se muestran por campo.

## Pico y Placa de taxis (Bogotá)

Los taxis rotan por **pares de último dígito** (1-2, 3-4, 5-6, 7-8, 9-0). El par restringido avanza un paso
cada día de **lunes a sábado**, de forma cíclica; los domingos no hay restricción y no avanzan el ciclo. Los
festivos que caen de lunes a sábado no tienen restricción, pero **sí cuentan como un paso** del ciclo. Horario
oficial: 5:30 a. m. a 9:00 p. m.

La regla se calcula desde un día ancla (5-nov-2026: restringen 5 y 6), así que funciona para cualquier fecha sin
tabla. Se contrastó con el calendario publicado de octubre de 2026 y con boletines de bogota.gov.co del 25-feb,
19-jun, 28-jul y 25-sep de 2026 (ver `PicoPlacaRuleTest`). Los festivos de Colombia se calculan solos
(`ColombianHolidayCalendar`).

**Si la Secretaría de Movilidad cambia la rotación**, ajusta `fleet.pico-placa` en
`backend/src/main/resources/application.yml` (fecha ancla, dígitos de ese día y orden de los grupos). Después usa
el botón "Actualizar Pico y Placa" del Control diario para corregir los días ya creados. Si generas años lejanos,
verifica antes que la norma siga igual: la regla extrapola el ciclo actual.

## Siguientes pasos sugeridos (ejercicio)

1. **Seguridad y roles** — Spring Security con JWT; roles `ADMIN` y `DRIVER` (vista exclusiva para conductores, como pide el PRD de escalabilidad).
2. **Pruebas de integración** — Testcontainers con PostgreSQL para repositorios y para `DailyRecordService` (conflicto de versión, generación idempotente).
3. **Pruebas del frontend** — Vitest + Testing Library para `DailyRow` y la exportación a PDF.
4. **Contenedores** — Dockerfile multi-stage del backend y del frontend, y CI con GitHub Actions.
5. **Auditoría** — quién y cuándo modificó cada registro contable (Spring Data Auditing).
