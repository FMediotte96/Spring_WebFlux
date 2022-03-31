package com.bolsadeideas.springboot.webflux.app;

import com.bolsadeideas.springboot.webflux.app.models.documents.Categoria;
import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;
import com.bolsadeideas.springboot.webflux.app.models.services.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.Date;

@SpringBootApplication
public class SpringBootWebfluxApplication implements CommandLineRunner {

    private final ProductoService service;

    private final ReactiveMongoTemplate mongoTemplate;

    @Autowired
    public SpringBootWebfluxApplication(ProductoService service, ReactiveMongoTemplate mongoTemplate) {
        this.service = service;
        this.mongoTemplate = mongoTemplate;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootWebfluxApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBootWebfluxApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        mongoTemplate.dropCollection("productos").subscribe();
        mongoTemplate.dropCollection("categorias").subscribe();

        Categoria electronico = new Categoria("Electrónico");
        Categoria deporte = new Categoria("Deporte");
        Categoria computacion = new Categoria("Computación");
        Categoria muebles = new Categoria("Muebles");

        Flux.just(electronico, deporte, computacion, muebles)
            .flatMap(service::saveCategoria)
            .doOnNext(c -> LOGGER.info("Categoria creada: {} Id: {}", c.getNombre(), c.getId()))
            .thenMany(Flux.just(
                    new Producto("TV Panasonic Pantalla LCD", 456.89, electronico),
                    new Producto("Sony Camara HD Digital", 177.89, electronico),
                    new Producto("Apple iPod", 46.89, electronico),
                    new Producto("Sony Notebook", 846.89, computacion),
                    new Producto("Hewlett Packard Multifuncional", 200.89, computacion),
                    new Producto("Bianchi Bicicleta", 70.89, deporte),
                    new Producto("HP Notebook Omen 17", 2500.89, computacion),
                    new Producto("Mica Cómoda 5 cajones", 150.89, muebles),
                    new Producto("TV Sony Bravia OLED 4K Ultra HD", 2255.89, electronico)
                )
                .flatMap(producto -> {
                    producto.setCreateAt(new Date());
                    return service.save(producto);
                }))
            .subscribe(producto -> LOGGER.info("Insert: {} {}",producto.getId(), producto.getNombre()));
    }
}
