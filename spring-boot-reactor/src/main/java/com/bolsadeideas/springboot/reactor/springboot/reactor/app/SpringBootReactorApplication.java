package com.bolsadeideas.springboot.reactor.springboot.reactor.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootReactorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Flux<String> names = Flux.just("Andres", "Facundo", "María", "Diego", "Juan")
                .doOnNext(it -> {
                    if (it.isEmpty()) {
                        throw new RuntimeException("Names cannot be empty");
                    }
                    System.out.println(it);
                });

        names.subscribe(LOGGER::info,
                err -> LOGGER.error(err.getMessage()),
                () -> LOGGER.info("The observable's execution has finalized successfully!"));
    }
}
