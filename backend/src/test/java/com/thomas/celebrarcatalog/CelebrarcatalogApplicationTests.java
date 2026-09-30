package com.thomas.celebrarcatalog;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Sobe o contexto completo e por isso depende de um PostgreSQL acessivel
 * (`docker compose up postgres`), usado tambem para validar as migrations e o
 * mapeamento das entidades contra o schema real.
 *
 * <p>O segredo do JWT vem daqui porque a aplicacao se recusa a iniciar sem ele; e um
 * valor de teste, nao serve para nenhum ambiente real.
 */
@SpringBootTest
@TestPropertySource(properties =
        "app.jwt.secret=segredo-de-teste-com-mais-de-32-bytes-de-tamanho")
class CelebrarcatalogApplicationTests {

	@Test
	void contextLoads() {
	}

}
