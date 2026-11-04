-- V3: cada usuario queda vinculado al campesino o comprador que representa (NULL para ADMIN).
-- El token JWT lleva este id, asi un usuario solo puede actuar en nombre propio.
ALTER TABLE usuarios ADD COLUMN actor_id uuid NULL;
