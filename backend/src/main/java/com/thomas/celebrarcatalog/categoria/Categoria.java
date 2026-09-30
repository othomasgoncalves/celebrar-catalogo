package com.thomas.celebrarcatalog.categoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 60)
    private String nome;

    @Column(nullable = false)
    private int ordem;

    protected Categoria() {
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getOrdem() {
        return ordem;
    }
}
