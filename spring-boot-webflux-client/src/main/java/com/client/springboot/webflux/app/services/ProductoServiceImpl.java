package com.client.springboot.webflux.app.services;


import com.client.springboot.webflux.app.models.Producto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final WebClient client;

    @Autowired
    public ProductoServiceImpl(WebClient client) {
        this.client = client;
    }

    @Override
    public Flux<Producto> findAll() {
        return client.get()
            .accept(MediaType.APPLICATION_JSON)
            .exchangeToFlux(response -> response.bodyToFlux(Producto.class));
    }

    @Override
    public Mono<Producto> findById(String id) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        return client.get()
            .uri("/{id}", params)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(Producto.class);
            //.exchangeToMono(response -> response.bodyToMono(Producto.class));
    }

    @Override
    public Mono<Producto> save(Producto producto) {
        return null;
    }

    @Override
    public Mono<Producto> update(Producto producto, String id) {
        return null;
    }

    @Override
    public Mono<Void> eliminar(String id) {
        return null;
    }
}
