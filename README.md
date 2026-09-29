# Sistema de Información de Trazabilidad — Club de la Unión

Sistema web que registra los consumos de alimentos y bebidas cargados contra la **acción** de cada socio del Club de la Unión
de Guayaquil, notifica al socio en el momento del cargo, y ofrece tableros de métricas y reportes a la gerencia y a la
administración.

Proyecto de grado de Ingeniería de Sistemas — Universidad El Bosque.

---

## Qué resuelve

Antes de que exista un sistema, el cargo ocurre en el punto de venta y el socio no se entera hasta recibir su estado de
cuenta, de modo que una inconsistencia se descubre tarde y es difícil de reconstruir. El sistema ataca eso con tres
capacidades:

- **Trazabilidad del cargo** — cada consumo queda registrado con su ambiente, mesa, mesero, desglose monetario y líneas de
  producto, asociado a un socio identificable.
- **Notificación inmediata** — al registrarse un consumo, el socio recibe aviso por Web Push y correo, y le queda una
  notificación consultable.
- **Analítica y rendición de cuentas** — tableros de facturación, ambientes, productos y afluencia; reportes en PDF; y una
  bitácora de auditoría de los eventos de seguridad.

Sobre eso se añade el cumplimiento de la normativa de protección de datos: la identificación y los datos de contacto de los
socios se cifran en reposo, y la aplicación exige aceptar una política de tratamiento de datos antes de permitir su uso.

---

## Tecnologías

| Componente | Tecnología |
|---|---|
| Backend | Spring Boot 4.0.3 · Java 21 · empaquetado WAR |
| Base de datos | Microsoft SQL Server |
| Bitácora de auditoría | Elasticsearch |
| Seguridad | Spring Security · JWT (JJWT 0.11.5) · AES-256-GCM |
| Reportes | OpenPDF · JFreeChart |
| Frontend | Angular 21 · componentes standalone · PWA |
| Gráficas | Apache ECharts |
| Gestor de paquetes del frontend | pnpm |

---

## Requisitos previos

| Requisito | Versión | Nota |
|---|---|---|
| **JDK** | **21** | Obligatorio; con versiones anteriores no compila |
| Node.js | ≥ 20 | |
| **pnpm** | ≥ 10 | Obligatorio: un hook `preinstall` bloquea npm |
| SQL Server | — | En `localhost:1433`, con la base `system_traceability_database` creada |
| Elasticsearch | — | En `localhost:9200`. Sin él la aplicación arranca, pero pierde toda la auditoría |

Maven no hace falta instalarlo: el proyecto incluye su propio wrapper.

> **No hay Docker ni Docker Compose en el repositorio.** SQL Server y Elasticsearch deben instalarse aparte.

---

## Variables de entorno obligatorias

**Cinco propiedades se inyectan sin valor por defecto. La aplicación no arranca si falta cualquiera de ellas.**

| Variable | Para qué | Restricción |
|---|---|---|
| `DB_PASSWORD` | Contraseña de SQL Server | — |
| `JWT_SECRET` | Clave de firma de los tokens de sesión | Mínimo 32 bytes |
| `APP_ENCRYPTION_KEY` | Clave de cifrado de datos personales | Exactamente 32 bytes en Base64 |
| `MAIL_PASSWORD` | Contraseña de la cuenta SMTP | — |
| `VAPID_PRIVATE_KEY` | Clave privada de notificaciones push | Debe corresponder a la pública configurada |

Opcionales, con valor por defecto: `DB_USERNAME`, `ELASTIC_URI`, `EXTERNAL_SOCIOS_URL`.

Generar una clave de cifrado válida:

```bash
openssl rand -base64 32
```

> **`APP_ENCRYPTION_KEY` es irrecuperable.** El sistema no admite rotación de clave: perderla vuelve indescifrables de forma
> permanente la identificación, los teléfonos y los correos de todos los socios. Respáldela fuera de la aplicación antes del
> primer arranque con datos reales.

---

## Puesta en marcha

### 1. Migración, solo si la base ya tiene datos

```bash
sqlcmd -S localhost -U sa -d system_traceability_database -i TraceabilitySystemClubUnion/scripts/migracion-bd-existente.sql
```

Es **obligatorio** sobre bases preexistentes: Hibernate crea columnas nuevas con el ancho correcto pero nunca ensancha las
existentes, y los campos cifrados ya no caben. En instalaciones desde cero no hace falta.

### 2. Backend

```bash
cd TraceabilitySystemClubUnion
./mvnw spring-boot:run          # → http://localhost:8080
```

Documentación interactiva de la API en `http://localhost:8080/swagger-ui/index.html`.

### 3. Frontend

```bash
cd AngularTraceabilitySystem
pnpm install                    # NUNCA npm install
pnpm start                      # → http://localhost:4200
```

> Para desplegar fuera de desarrollo hay que editar `API_BASE` en
> `AngularTraceabilitySystem/src/app/core/config/api.config.ts` y recompilar: el proyecto no define archivos de entorno.

---

## Pruebas

```bash
cd TraceabilitySystemClubUnion && ./mvnw test
cd AngularTraceabilitySystem   && pnpm exec ng test --watch=false --browsers=ChromeHeadless
```

Líneas base: **backend 284 pruebas con 1 error preexistente** —la suite no es hermética y una prueba exige Elasticsearch en
ejecución— y **frontend 138 pruebas en verde**.

---

## Documentación

La documentación técnica completa está en **[`docs/`](docs/)**:

| Documento | Contenido |
|---|---|
| [01 · Arquitectura](docs/01-arquitectura.md) | Estructura, capas, tecnologías y dependencias externas |
| [02 · Modelo de datos](docs/02-modelo-de-datos.md) | Entidades, diagrama ER y cifrado en reposo |
| [03 · Flujos de negocio](docs/03-flujos-de-negocio.md) | Los siete recorridos extremo a extremo |
| [04 · Referencia de la API](docs/04-referencia-api.md) | Los 48 endpoints con roles y parámetros |
| [05 · Seguridad](docs/05-seguridad.md) | Autenticación, autorización, cifrado, auditoría |
| [06 · Configuración](docs/06-configuracion.md) | Propiedades, variables de entorno y despliegue |
| [07 · Pruebas y calidad](docs/07-pruebas-y-calidad.md) | Suites de pruebas y análisis estático |
| [08 · Hallazgos](docs/08-hallazgos.md) | Registro de defectos e inconsistencias detectados |

El detalle por clase y método vive junto al código, como Javadoc en el backend y TSDoc en el frontend. Generar el sitio de
Javadoc:

```bash
cd TraceabilitySystemClubUnion && ./mvnw javadoc:javadoc
```

---

## Licencia

GNU Affero General Public License v3.0. Véase [LICENSE](LICENSE).
