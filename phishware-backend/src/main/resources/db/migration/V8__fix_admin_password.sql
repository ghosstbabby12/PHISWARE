-- ============================================================
-- PHISHWARE - Corrige el hash BCrypt del admin seed (V2 traía
-- un hash truncado de 52 caracteres y el login de admin fallaba
-- con "Credenciales inválidas").
-- password: Admin@phish2024 (BCrypt, cost 12)
-- ============================================================

UPDATE users
SET password_hash = '$2a$12$Qc0y.WCswpCRG8IBcYtluuu8EErvUmuLhQ2Rs4Nl2gFmsAai5PPcW'
WHERE username = 'admin'
  AND length(password_hash) <> 60;
