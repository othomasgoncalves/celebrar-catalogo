INSERT INTO usuario (email, senha_hash, role)
VALUES (
    'admin@celebrar.local',
    '$2a$12$k.FhHRmT3OkjyoNwAWh8HeF6n5v6nlGYoDEF4Q4KpUZhgcaT8HjC6',
    'ROLE_ADMIN'
)
ON CONFLICT (email) DO NOTHING;
