package com.apirest.springboot.webflux.app.models.services;

import com.apirest.springboot.webflux.app.models.documents.Categoria;
import com.apirest.springboot.webflux.app.models.documents.Producto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoService {

    Flux<Producto> findAll();

    Flux<Producto> findAllWithNameUpperCase();

    Flux<Producto> findAllWithNameUpperCaseAndRepeat();

    Mono<Producto> findById(String id);

    Mono<Producto> save(Producto producto);

    Mono<Void> delete(Producto producto);

    Flux<Categoria> findAllCategoria();

    Mono<Categoria> findCategoriaById(String id);

    Mono<Categoria> saveCategoria(Categoria categoria);

    Mono<Producto> findByNombre(String nombre);

}
