package com.apirest.springboot.webflux.app.controllers;

import com.apirest.springboot.webflux.app.models.documents.Producto;
import com.apirest.springboot.webflux.app.models.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService service;

    @Value("${config.uploads.path}")
    private String path;

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

    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> crear(@Valid @RequestBody Mono<Producto> monoProducto) {
        Map<String, Object> respuesta = new HashMap<>();

        return monoProducto.flatMap(producto -> {
            if (producto.getCreateAt() == null) {
                producto.setCreateAt(new Date());
            }

            return service.save(producto).map(p -> {
                respuesta.put("producto", p);
                respuesta.put("mensaje", "Producto creado con éxito");
                respuesta.put("timestamp", new Date());

                return ResponseEntity.created(URI.create("/api/productos/".concat(p.getId())))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(respuesta);
            });
        }).onErrorResume(t ->
            Mono.just(t).cast(WebExchangeBindException.class)
                .flatMap(e -> Mono.just(e.getFieldErrors()))
                .flatMapMany(Flux::fromIterable)
                .map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
                .collectList()
                .flatMap(list -> {
                    respuesta.put("errors", list);
                    respuesta.put("timestamp", new Date());
                    respuesta.put("status", HttpStatus.BAD_REQUEST.value());

                    return Mono.just(ResponseEntity.badRequest().body(respuesta));
                })
        );
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Producto>> editar(@RequestBody Producto producto, @PathVariable String id) {
        return service.findById(id).flatMap(p -> {
            p.setNombre(producto.getNombre());
            p.setPrecio(producto.getPrecio());
            p.setCategoria(producto.getCategoria());
            return service.save(p);
        }).map(p ->
            ResponseEntity.created(URI.create("/api/productos/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(p)
        ).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> eliminar(@PathVariable String id) {
        return service.findById(id).flatMap(p ->
            service.delete(p).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
        ).defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/upload/{id}")
    public Mono<ResponseEntity<Producto>> upload(@PathVariable String id, @RequestPart FilePart file) {
        return service.findById(id).flatMap(p -> {
                String filename = file.filename().replace(" ", "")
                    .replace(":", "")
                    .replace("\\", "");
                p.setFoto(UUID.randomUUID() + "-" + filename);

                return file.transferTo(new File(path + p.getFoto())).then(service.save(p));
            }).map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @PostMapping("/v2")
    public Mono<ResponseEntity<Producto>> crearConFoto(Producto producto, @RequestPart FilePart file) {
        if (producto.getCreateAt() == null) {
            producto.setCreateAt(new Date());
        }

        String filename = file.filename().replace(" ", "")
            .replace(":", "")
            .replace("\\", "");
        producto.setFoto(UUID.randomUUID() + "-" + filename);

        return file.transferTo(new File(path + producto.getFoto())).then(service.save(producto))
            .map(p -> ResponseEntity.created(URI.create("/api/productos/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(p)
            );
    }
}
