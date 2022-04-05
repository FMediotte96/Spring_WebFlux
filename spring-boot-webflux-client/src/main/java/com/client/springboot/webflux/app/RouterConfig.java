package com.client.springboot.webflux.app;

import com.client.springboot.webflux.app.handler.ProductoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> rutas(ProductoHandler handler) {
        return route(GET("/api/client"), handler::listar)
            .andRoute(GET("/api/client/{id}"), handler::ver)
            .andRoute(POST("/api/client"), handler::crear)
            .andRoute(PUT("/api/client/{id}"), handler::editar)
            .andRoute(DELETE("/api/client/{id}"), handler::eliminar)
            .andRoute(POST("/api/client/upload/{id}"), handler::upload);
    }
}
