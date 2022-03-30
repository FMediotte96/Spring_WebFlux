package com.bolsadeideas.springboot.webflux.app.controllers;

import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;
import com.bolsadeideas.springboot.webflux.app.models.services.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@SessionAttributes("producto")
@Controller
public class ProductoController {

    private final ProductoService service;

    @Autowired
    public ProductoController(ProductoService service) {
        this.service = service;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductoController.class);

    @GetMapping({"/listar", "/"})
    public Mono<String> listar(Model model) {
        Flux<Producto> productos = service.findAllWithNameUpperCase();

        productos.subscribe(prod -> LOGGER.info(prod.getNombre()));

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");
        return Mono.just("listar");
    }

    @GetMapping("/form")
    public Mono<String> crear(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("titulo", "Formulario de producto");
        return Mono.just("form");
    }

    @GetMapping("/form/{id}")
    public Mono<String> editar(@PathVariable String id, Model model) {
        Mono<Producto> productoMono = service.findById(id)
            .doOnNext(p -> LOGGER.info("Producto: {}", p.getNombre()))
            .defaultIfEmpty(new Producto());

        model.addAttribute("titulo","Editar Producto");
        model.addAttribute("producto",productoMono);

        return Mono.just("form");
}

    @PostMapping("/form")
    public Mono<String> guardar(Producto producto, SessionStatus status) {
        status.setComplete();
        return service.save(producto)
            .doOnNext(p -> LOGGER.info("Producto guardado: {} Id: {}", p.getNombre(), p.getId()))
            .thenReturn("redirect:/listar");
    }

    @GetMapping("/listar-datadriver")
    public String listarDataDriver(Model model) {
        Flux<Producto> productos = service.findAllWithNameUpperCase().delayElements(Duration.ofSeconds(1));

        productos.subscribe(prod -> LOGGER.info(prod.getNombre()));

        model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 1));
        model.addAttribute("titulo", "Listado de productos");
        return "listar";
    }

    @GetMapping("/listar-full")
    public String listarFull(Model model) {
        Flux<Producto> productos = service.findAllWithNameUpperCaseAndRepeat();

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");
        return "listar";
    }

    @GetMapping("/listar-chunked")
    public String listarChunked(Model model) {
        Flux<Producto> productos = service.findAllWithNameUpperCaseAndRepeat();

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");
        return "listar-chunked";
    }
}
