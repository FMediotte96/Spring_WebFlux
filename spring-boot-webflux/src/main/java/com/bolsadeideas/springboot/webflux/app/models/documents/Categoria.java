package com.bolsadeideas.springboot.webflux.app.models.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "categorias")
public class Categoria {

    @Id
    private String id;

    private String nombre;

    public Categoria() {
    }

    public Categoria(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
}
