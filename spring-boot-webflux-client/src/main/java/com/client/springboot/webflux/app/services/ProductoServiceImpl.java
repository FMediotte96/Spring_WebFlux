package com.client.springboot.webflux.app.services;

import com.client.springboot.webflux.app.models.Producto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

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
            .accept(APPLICATION_JSON)
            .exchangeToFlux(response -> response.bodyToFlux(Producto.class));
    }

    @Override
    public Mono<Producto> findById(String id) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        return client.get()
            .uri("/{id}", params)
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono(Producto.class);
        //.exchangeToMono(response -> response.bodyToMono(Producto.class));
    }

    @Override
    public Mono<Producto> save(Producto producto) {
        return client.post()
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON)
            .body(fromValue(producto))
            //.syncBody(producto) //alias a la linea anterior, pero esta deprecada
            .retrieve()
            .bodyToMono(Producto.class);
    }

    @Override
    public Mono<Producto> update(Producto producto, String id) {
        return client.put()
            .uri("/{id}", Collections.singletonMap("id", id))
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON)
            .body(fromValue(producto))
            .retrieve()
            .bodyToMono(Producto.class);
    }

    @Override
    public Mono<Void> eliminar(String id) {
        return client.delete()
            .uri("/{id}", Collections.singletonMap("id", id))
            .exchangeToMono(response -> response.bodyToMono(Void.class))
            .then();
    }
}
