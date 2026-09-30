CREATE TABLE categoria (
    id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome  VARCHAR(60) NOT NULL UNIQUE,
    ordem INT NOT NULL DEFAULT 0
);

CREATE TABLE produto (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome                VARCHAR(120) NOT NULL,
    descricao           TEXT,
    preco               NUMERIC(10,2) NOT NULL CHECK (preco >= 0),
    quantidade          INT NOT NULL DEFAULT 0 CHECK (quantidade >= 0),
    categoria_id        UUID NOT NULL REFERENCES categoria (id),
    imagem              VARCHAR(255),
    ativo               BOOLEAN NOT NULL DEFAULT true,
    disponivel_na_cesta BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_produto_categoria_id ON produto (categoria_id);

CREATE TABLE cesta_pronta (
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome      VARCHAR(120) NOT NULL,
    descricao TEXT,
    preco     NUMERIC(10,2) NOT NULL CHECK (preco >= 0),
    itens     TEXT NOT NULL,
    imagem    VARCHAR(255),
    ativo     BOOLEAN NOT NULL DEFAULT true
);
