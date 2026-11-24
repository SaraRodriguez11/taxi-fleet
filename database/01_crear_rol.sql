-- =====================================================================
-- PASO 1 de 2 · Rol (usuario) de la aplicación
-- Ejecutar en pgAdmin: clic derecho en el servidor "PostgreSQL" -> Query Tool
-- (conectado a la base "postgres" con el usuario "postgres"), pegar y pulsar F5.
-- Se puede ejecutar varias veces sin problema.
-- =====================================================================

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'taxi') THEN
        CREATE ROLE taxi WITH LOGIN PASSWORD 'taxi';
    END IF;
END
$$;

ALTER ROLE taxi WITH LOGIN PASSWORD 'taxi';
