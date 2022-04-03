package com.apirest.springboot.webflux.app.controllers;

import com.apirest.springboot.webflux.app.models.documents.Producto;
import com.apirest.springboot.webflux.app.models.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService service;

    @Autowired
    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    public Mono<ResponseEntity<Flux<Producto>>> listar() {
        return Mono.just(
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.findAll())
        );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Producto>> ver(@PathVariable String id) {
        return service.findById(id).map(p ->
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(p)
        ).defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
