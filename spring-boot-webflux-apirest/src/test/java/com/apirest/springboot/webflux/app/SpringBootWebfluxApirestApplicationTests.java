package com.apirest.springboot.webflux.app;

import com.apirest.springboot.webflux.app.models.documents.Categoria;
import com.apirest.springboot.webflux.app.models.documents.Producto;
import com.apirest.springboot.webflux.app.models.services.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringBootWebfluxApirestApplicationTests {

    @Autowired
    private WebTestClient client;

    @Autowired
    private ProductoService service;

    @Value("${config.base.endpoint}")
    private String url;

    @Test
    void listarTest() {
        client.get()
            .uri(url)
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
            .uri(url + "/{id}", Collections.singletonMap("id", producto.getId()))
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

    @Test
    void crearTest() {
        Categoria categoria = service.findCategoriaByNombre("Muebles").block();
        Producto producto = new Producto("Mesa comedor", 100.00, categoria);

        client.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(producto), Producto.class)
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.producto.id").isNotEmpty()
            .jsonPath("$.producto.nombre").isEqualTo("Mesa comedor")
            .jsonPath("$.producto.categoria.nombre").isEqualTo("Muebles");
    }

    @Test
    void crear2Test() {
        Categoria categoria = service.findCategoriaByNombre("Muebles").block();
        Producto producto = new Producto("Mesa comedor", 100.00, categoria);

        client.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(producto), Producto.class)
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {})
            .consumeWith(response -> {
                Object o = Objects.requireNonNull(response.getResponseBody()).get("producto");
                Producto p = new ObjectMapper().convertValue(o, Producto.class);

                assert p != null;
                assertThat(p.getId()).isNotEmpty();
                assertThat(p.getNombre()).isEqualTo("Mesa comedor");
                assertThat(p.getCategoria().getNombre()).isEqualTo("Muebles");
            });
    }


    @Test
    void editarTest() {
        Producto producto = service.findByNombre("Sony Notebook").block();
        Categoria categoria = service.findCategoriaByNombre("Electrónico").block();

        Producto productoEditado = new Producto("Asus Notebook", 700.00, categoria);

        assert producto != null;
        client.put()
            .uri(url + "/{id}", Collections.singletonMap("id", producto.getId()))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(productoEditado), Producto.class)
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id").isNotEmpty()
            .jsonPath("$.nombre").isEqualTo("Asus Notebook")
            .jsonPath("$.categoria.nombre").isEqualTo("Electrónico");
    }

    @Test
    void eliminarTest() {
        Producto producto = service.findByNombre("Mica Cómoda 5 cajones").block();

        assert producto != null;
        client.delete()
            .uri(url + "/{id}", Collections.singletonMap("id", producto.getId()))
            .exchange()
            .expectStatus().isNoContent()
            .expectBody()
            .isEmpty();

        client.get()
            .uri(url + "/{id}", Collections.singletonMap("id", producto.getId()))
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .isEmpty();
    }
}
