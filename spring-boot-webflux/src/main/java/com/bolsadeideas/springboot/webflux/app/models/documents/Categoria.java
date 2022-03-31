package com.bolsadeideas.springboot.webflux.app.models.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Document(collection = "categorias")
public class Categoria {

    @Id
    @NotEmpty
    private String id;

    private String nombre;

    public Categoria() {
    }

    public Categoria(String nombre) {
        this.nombre = nombre;
    }
}
