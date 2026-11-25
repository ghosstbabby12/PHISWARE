-- ============================================================
-- V10: Bloquea la contraseña SEMILLA del admin.
-- La contraseña de V2/V8 quedó documentada en el repositorio, así que
-- no puede seguir siendo válida en ningún despliegue.
--   - Si el admin todavía usa el hash semilla (o el hash truncado de V2),
--     se reemplaza por un valor que BCrypt nunca acepta: nadie puede entrar
--     con la contraseña publicada.
--   - Si la contraseña ya se cambió desde la app, NO se toca.
-- Para (re)activar el admin: definir ADMIN_PASSWORD en el entorno; el backend
-- la aplica al arrancar (ver AdminPasswordInitializer).
-- V8 no se edita a propósito: cambiar una migración aplicada rompe la
-- validación de checksums de Flyway en producción.
-- ============================================================
UPDATE users
SET password_hash = '!locked-seed-admin'
WHERE username = 'admin'
  AND (password_hash = '$2a$12$Qc0y.WCswpCRG8IBcYtluuu8EErvUmuLhQ2Rs4Nl2gFmsAai5PPcW'
       OR length(password_hash) <> 60);
