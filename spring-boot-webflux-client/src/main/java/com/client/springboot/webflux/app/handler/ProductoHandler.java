package com.client.springboot.webflux.app.handler;

import com.client.springboot.webflux.app.models.Producto;
import com.client.springboot.webflux.app.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
            ).switchIfEmpty(ServerResponse.notFound().build())
            .onErrorResume(error -> {
                WebClientResponseException errorResponse = (WebClientResponseException) error;
                if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                    Map<String, Object> body = new HashMap<>();
                    body.put("error", "No existe el producto: ".concat(Objects.requireNonNull(errorResponse.getMessage())));
                    body.put("timestamp", new Date());
                    body.put("status", errorResponse.getStatusCode().value());
                    return ServerResponse.status(HttpStatus.NOT_FOUND).body(fromValue(body));
                }
                return Mono.error(errorResponse);
            });
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
            if (errorResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
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

        return producto.flatMap(p -> service.update(p, id))
            .flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(p.getId())))
                .contentType(APPLICATION_JSON)
                .body(fromValue(p))
            ).onErrorResume(error -> {
                WebClientResponseException errorResponse = (WebClientResponseException) error;
                if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return ServerResponse.notFound().build();
                }
                return Mono.error(errorResponse);
            });
    }

    public Mono<ServerResponse> eliminar(ServerRequest request) {
        String id = request.pathVariable("id");
        return service.delete(id).then(ServerResponse.noContent().build())
            .onErrorResume(error -> {
                WebClientResponseException errorResponse = (WebClientResponseException) error;
                if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return ServerResponse.notFound().build();
                }
                return Mono.error(errorResponse);
            });
    }

    public Mono<ServerResponse> upload(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
            .cast(FilePart.class)
            .flatMap(file -> service.upload(file, id))
            .flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(id)))
                .contentType(APPLICATION_JSON)
                .body(fromValue(p))
            ).onErrorResume(error -> {
                WebClientResponseException errorResponse = (WebClientResponseException) error;
                if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return ServerResponse.notFound().build();
                }
                return Mono.error(errorResponse);
            });
    }
}
