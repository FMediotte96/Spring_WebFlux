package com.apirest.springboot.webflux.app.models.dao;

import com.apirest.springboot.webflux.app.models.documents.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductoDAO extends ReactiveMongoRepository<Producto, String> {

}
