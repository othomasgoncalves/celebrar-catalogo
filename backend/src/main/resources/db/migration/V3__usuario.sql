CREATE TABLE usuario (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(160) NOT NULL UNIQUE,
    senha_hash    VARCHAR(72)  NOT NULL,
    role          VARCHAR(30)  NOT NULL,
    ativo         BOOLEAN      NOT NULL DEFAULT true,
    falhas_login  INT          NOT NULL DEFAULT 0,
    bloqueado_ate TIMESTAMPTZ,
    criado_em     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_usuario_email_upper ON usuario (UPPER(email));
