# Backend — IntelliJ IDEA

1. IntelliJ → **File ▸ Open** → elige la carpeta `backend` (donde está `pom.xml`) → "Open as Project" y "Trust".
2. Espera a que Maven descargue dependencias. **File ▸ Project Structure ▸ SDK = 17**.
3. (Opcional) Variables de entorno en la configuración **TaxiFleetApplication** (Run ▸ Edit Configurations ▸ Environment variables):
   - `JWT_SECRET` — clave de 32+ caracteres (obligatoria en producción).
4. Ejecuta **TaxiFleetApplication** (botón ▶). API en http://localhost:8080 · Swagger en `/swagger-ui.html`.
5. Pruebas: abre `pom.xml` ▸ Maven ▸ Lifecycle ▸ `test`.

La base de datos debe existir antes (ver `../database`). Flyway crea las tablas al arrancar.
