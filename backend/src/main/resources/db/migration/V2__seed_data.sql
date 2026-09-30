INSERT INTO categoria (id, nome, ordem) VALUES
    (gen_random_uuid(), 'enfeite', 1),
    (gen_random_uuid(), 'doces', 2),
    (gen_random_uuid(), 'variedades', 3);

INSERT INTO produto (id, nome, descricao, preco, quantidade, categoria_id, imagem, ativo, disponivel_na_cesta) VALUES
    (gen_random_uuid(), 'Vela Numérica Decorada', 'Vela de aniversário em formato numérico, acabamento com glitter dourado, 8cm de altura.', 12.90, 25, (SELECT id FROM categoria WHERE nome = 'enfeite'), NULL, true, true),
    (gen_random_uuid(), 'Topo de Bolo Personalizado', 'Topo de bolo em MDF com nome personalizado, até 12 caracteres.', 24.50, 10, (SELECT id FROM categoria WHERE nome = 'enfeite'), NULL, true, false),
    (gen_random_uuid(), 'Brigadeiro Gourmet', 'Brigadeiro gourmet de chocolate belga, unidade de 20g.', 3.50, 100, (SELECT id FROM categoria WHERE nome = 'doces'), NULL, true, true),
    (gen_random_uuid(), 'Bolo de Pote Red Velvet', 'Bolo de pote sabor red velvet com cream cheese, pote de 200g.', 15.00, 0, (SELECT id FROM categoria WHERE nome = 'doces'), NULL, true, true),
    (gen_random_uuid(), 'Balão Metalizado Número', 'Balão metalizado formato número, 40cm, ar ou hélio.', 18.90, 30, (SELECT id FROM categoria WHERE nome = 'variedades'), NULL, true, false),
    (gen_random_uuid(), 'Kit Descartável Festa', 'Kit com pratos, copos e talheres descartáveis para 10 pessoas.', 45.00, 15, (SELECT id FROM categoria WHERE nome = 'variedades'), NULL, true, true);

INSERT INTO cesta_pronta (id, nome, descricao, preco, itens, imagem, ativo) VALUES
    (gen_random_uuid(), 'Cesta Doce Encontro', 'Cesta pensada para presentear com doces variados.', 39.90, '2 brigadeiros gourmet, 1 bolo de pote red velvet (200g), 4 docinhos sortidos, embalagem para presente', NULL, true),
    (gen_random_uuid(), 'Cesta Festa Completa', 'Cesta com o essencial para decorar uma festa pequena.', 89.90, '10 balões metalizados número, kit descartável para 10 pessoas, 1 topo de bolo personalizado, 2 velas numéricas decoradas', NULL, true);
