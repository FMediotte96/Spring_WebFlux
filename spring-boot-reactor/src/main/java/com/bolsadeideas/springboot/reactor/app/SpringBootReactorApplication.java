package com.bolsadeideas.springboot.reactor.app;

import com.bolsadeideas.springboot.reactor.app.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootReactorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        flatMapExample();
    }

    public void flatMapExample() throws Exception {
        List<String> usersList = new ArrayList<>();
        usersList.add("Andres Guzman");
        usersList.add("Facundo Mediotte");
        usersList.add("María Fulana");
        usersList.add("Diego Maradona");
        usersList.add("Juan Mengano");
        usersList.add("Bruce Lee");
        usersList.add("Bruce Willis");

        Flux.fromIterable(usersList)
            .map(name -> new User(name.split(" ")[0].toUpperCase(), name.split(" ")[1].toUpperCase()))
            .flatMap(user -> {
                if("bruce".equalsIgnoreCase(user.getName())) {
                    return Mono.just(user);
                } else {
                    return Mono.empty();
                }
            })
            .map(user -> {
                String name = user.getName().toLowerCase();
                user.setName(name);
                return user;
            })
            .subscribe(it -> LOGGER.info(it.toString()));
    }

    public void iterableExample() throws Exception {
        List<String> usersList = new ArrayList<>();
        usersList.add("Andres Guzman");
        usersList.add("Facundo Mediotte");
        usersList.add("María Fulana");
        usersList.add("Diego Maradona");
        usersList.add("Juan Mengano");
        usersList.add("Bruce Lee");
        usersList.add("Bruce Willis");

        Flux<String> names = Flux.fromIterable(usersList);
        /*Flux.just("Andres Guzman", "Facundo Mediotte", "María Fulana", "Diego Maradona", "Juan Mengano", "Bruce Lee", "Bruce Willis");*/

        Flux<User> users = names.map(name -> new User(name.split(" ")[0].toUpperCase(), name.split(" ")[1].toUpperCase()))
            .filter(it -> "bruce".equalsIgnoreCase(it.getName()))
            .doOnNext(user -> {
                if (user == null) {
                    throw new IllegalArgumentException("Names cannot be empty");
                }
                System.out.println(user.getName().concat(" ").concat(user.getLastName()));
            })
            .map(user -> {
                String name = user.getName().toLowerCase();
                user.setName(name);
                return user;
            });

        users.subscribe(it -> LOGGER.info(it.toString()),
            err -> LOGGER.error(err.getMessage()),
            () -> LOGGER.info("The observable's execution has finalized successfully!"));
    }

}
