package com.client.springboot.webflux.app.handler;

import com.client.springboot.webflux.app.models.Producto;
import com.client.springboot.webflux.app.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class ProductoHandler {

    private final ProductoService service;

    @Autowired
    public ProductoHandler(ProductoService service) {
        this.service = service;
    }

    public Mono<ServerResponse> listar(ServerRequest request) {
        return ServerResponse.ok()
            .contentType(APPLICATION_JSON)
            .body(service.findAll(), Producto.class);
    }

    public Mono<ServerResponse> ver(ServerRequest request) {
        String id = request.pathVariable("id");
        return service.findById(id)
            .flatMap(p -> ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(fromValue(p))
            ).switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> crear(ServerRequest request) {
        Mono<Producto> producto = request.bodyToMono(Producto.class);

        return producto.flatMap(p -> {
            if (p.getCreateAt() == null) {
                p.setCreateAt(new Date());
            }
            return service.save(p);
        }).flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(p.getId())))
            .contentType(APPLICATION_JSON)
            .body(fromValue(p))
        ).onErrorResume(error -> {
            WebClientResponseException errorResponse = (WebClientResponseException) error;
            if(errorResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return ServerResponse.badRequest()
                    .contentType(APPLICATION_JSON)
                    .body(fromValue(errorResponse.getResponseBodyAsString()));
            }
            return Mono.error(errorResponse);
        });
    }

    public Mono<ServerResponse> editar(ServerRequest request) {
        Mono<Producto> producto = request.bodyToMono(Producto.class);
        String id = request.pathVariable("id");

        return producto.flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(id)))
            .contentType(APPLICATION_JSON)
            .body(service.update(p, id), Producto.class)
        );
    }

    public Mono<ServerResponse> eliminar(ServerRequest request) {
        String id = request.pathVariable("id");
        return service.eliminar(id).then(ServerResponse.noContent().build());
    }

}
