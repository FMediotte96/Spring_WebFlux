package com.bolsadeideas.springboot.reactor.app;

import com.bolsadeideas.springboot.reactor.app.models.User;
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
        Flux<User> names = Flux.just("Andres", "Facundo", "María", "Diego", "Juan")
                .map(name -> new User(name.toUpperCase(), null))
                .doOnNext(user -> {
                    if (user == null) {
                        throw new IllegalArgumentException("Names cannot be empty");
                    }
                    System.out.println(user.getName());
                })
                .map(user -> {
                    String name = user.getName().toLowerCase();
                    user.setName(name);
                    return user;
                });

        names.subscribe(it -> LOGGER.info(it.toString()),
                err -> LOGGER.error(err.getMessage()),
                () -> LOGGER.info("The observable's execution has finalized successfully!"));
    }
}
