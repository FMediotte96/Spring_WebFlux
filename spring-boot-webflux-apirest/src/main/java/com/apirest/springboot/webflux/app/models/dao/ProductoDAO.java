package com.apirest.springboot.webflux.app.models.dao;

import com.apirest.springboot.webflux.app.models.documents.Producto;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ProductoDAO extends ReactiveMongoRepository<Producto, String> {

    Mono<Producto> findByNombre(String nombre);

    @Query("{'nombre': ?0}")
    Mono<Producto> obtenerPorNombre(String nombre);

}
