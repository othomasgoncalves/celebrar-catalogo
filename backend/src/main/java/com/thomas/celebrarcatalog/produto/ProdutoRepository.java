package com.thomas.celebrarcatalog.produto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProdutoRepository extends JpaRepository<Produto, UUID> {

    List<Produto> findAllByAtivoTrue();

    List<Produto> findAllByAtivoTrueAndCategoriaId(UUID categoriaId);

    List<Produto> findAllByAtivoTrueAndDisponivelNaCestaTrue();

    List<Produto> findAllByAtivoTrueAndDisponivelNaCestaTrueAndCategoriaId(UUID categoriaId);
}
