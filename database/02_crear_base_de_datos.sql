-- =====================================================================
-- PASO 2 de 2 · Base de datos taxi_fleet
-- Ejecutar en pgAdmin en el Query Tool de la base "postgres".
-- IMPORTANTE: ejecutar SOLO esta sentencia, sola en la ventana
-- (PostgreSQL no permite CREATE DATABASE junto con otras sentencias).
--
-- Si la base taxi_fleet ya existe de pruebas anteriores y su dueño no es "taxi",
-- bórrala primero (se pierden los datos de prueba) ejecutando SOLA esta línea:
--     DROP DATABASE IF EXISTS taxi_fleet WITH (FORCE);
-- y luego ejecuta el CREATE DATABASE de abajo.
-- =====================================================================

CREATE DATABASE taxi_fleet OWNER taxi;

-- Las tablas NO se crean a mano: las crea la aplicación (Flyway) al arrancar el backend.
