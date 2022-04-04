package com.apirest.springboot.webflux.app;

import com.apirest.springboot.webflux.app.models.documents.Producto;
import com.apirest.springboot.webflux.app.models.services.ProductoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringBootWebfluxApirestApplicationTests {

    @Autowired
    private WebTestClient client;

    @Autowired
    private ProductoService service;

    @Test
    void listarTest() {
        client.get()
            .uri("/api/v2/productos")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(Producto.class)
            .consumeWith(response -> {
                List<Producto> productos = response.getResponseBody();
                assert productos != null;
                productos.forEach(p -> {
                    System.out.println(p.getNombre());
                });

                Assertions.assertTrue(productos.size() > 0);
            });
        //.hasSize(9);
    }

    @Test
    void verTest() {
        Producto producto = service.findByNombre("TV Panasonic Pantalla LCD").block();

        assert producto != null;
        client.get()
            .uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(Producto.class)
            .consumeWith(response -> {
                Producto p = response.getResponseBody();
                assert p != null;

                assertThat(p.getId()).isNotEmpty();
                assertThat(p.getId().length() > 0).isTrue();
                assertThat(p.getNombre()).isEqualTo("TV Panasonic Pantalla LCD");
            });
            /*.expectBody()
            .jsonPath("$.id").isNotEmpty()
            .jsonPath("$.nombre").isEqualTo("TV Panasonic Pantalla LCD");*/
    }

}
