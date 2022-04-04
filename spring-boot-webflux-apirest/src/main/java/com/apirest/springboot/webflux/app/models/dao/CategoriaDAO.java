package com.apirest.springboot.webflux.app.models.dao;

import com.apirest.springboot.webflux.app.models.documents.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CategoriaDAO extends ReactiveMongoRepository<Categoria, String> {

    Mono<Categoria> findByNombre(String nombre);

}
