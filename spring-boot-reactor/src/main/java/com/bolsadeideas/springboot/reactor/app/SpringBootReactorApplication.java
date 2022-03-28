package com.bolsadeideas.springboot.reactor.app;

import com.bolsadeideas.springboot.reactor.app.models.Comments;
import com.bolsadeideas.springboot.reactor.app.models.User;
import com.bolsadeideas.springboot.reactor.app.models.UserWithComments;
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
        exampleUserCommentsZipWithRanges();
    }

    public void exampleUserCommentsZipWithRanges() {
        Flux<Integer> ranges = Flux.range(0, 4);
        Flux.just(1, 2, 3, 4)
            .map(i -> (i * 2))
            .zipWith(ranges, (uno, dos) -> String.format("Primer Flux: %d, Segundo Flux: %d", uno, dos))
            .subscribe(LOGGER::info);
    }

    public void exampleUserCommentsZipWith2() {
        Mono<User> userMono = Mono.fromCallable(() -> new User("John", "Doe"));

        Mono<Comments> userCommentsMono = Mono.fromCallable(() -> {
            Comments comments = new Comments();
            comments.addComment("Hola pepe, que tal!");
            comments.addComment("Mañana voy a la playa!");
            comments.addComment("Estoy tomando el curso de spring con reactor");
            return comments;
        });

        Mono<UserWithComments> userWithComments = userMono
            .zipWith(userCommentsMono)
            .map(tuple -> {
                User u = tuple.getT1();
                Comments c = tuple.getT2();
                return new UserWithComments(u, c);
            });
        userWithComments.subscribe(uc -> LOGGER.info(uc.toString()));
    }

    public void exampleUserCommentsZipWith() {
        Mono<User> userMono = Mono.fromCallable(() -> new User("John", "Doe"));

        Mono<Comments> userCommentsMono = Mono.fromCallable(() -> {
            Comments comments = new Comments();
            comments.addComment("Hola pepe, que tal!");
            comments.addComment("Mañana voy a la playa!");
            comments.addComment("Estoy tomando el curso de spring con reactor");
            return comments;
        });

        Mono<UserWithComments> userWithComments = userMono.zipWith(userCommentsMono, UserWithComments::new);
        userWithComments.subscribe(uc -> LOGGER.info(uc.toString()));
    }

    public void exampleUserCommentsFlatMap() {
        Mono<User> userMono = Mono.fromCallable(() -> new User("John", "Doe"));

        Mono<Comments> userCommentsMono = Mono.fromCallable(() -> {
            Comments comments = new Comments();
            comments.addComment("Hola pepe, que tal!");
            comments.addComment("Mañana voy a la playa!");
            comments.addComment("Estoy tomando el curso de spring con reactor");
            return comments;
        });

        Mono<UserWithComments> userWithComments = userMono.flatMap(u -> userCommentsMono.map(c -> new UserWithComments(u, c)));
        userWithComments.subscribe(uc -> LOGGER.info(uc.toString()));
    }

    public void exampleConvertToMono() throws Exception {
        List<User> usersList = new ArrayList<>();
        usersList.add(new User("Andres", "Guzman"));
        usersList.add(new User("Facundo", "Mediotte"));
        usersList.add(new User("María", "Fulana"));
        usersList.add(new User("Diego", "Maradona"));
        usersList.add(new User("Juan", "Mengano"));
        usersList.add(new User("Bruce", "Lee"));
        usersList.add(new User("Bruce", "Willis"));

        Flux.fromIterable(usersList)
            .collectList()
            .subscribe(userList -> userList.forEach(item -> LOGGER.info(item.toString())));
    }

    public void exampleToString() throws Exception {
        List<User> usersList = new ArrayList<>();
        usersList.add(new User("Andres", "Guzman"));
        usersList.add(new User("Facundo", "Mediotte"));
        usersList.add(new User("María", "Fulana"));
        usersList.add(new User("Diego", "Maradona"));
        usersList.add(new User("Juan", "Mengano"));
        usersList.add(new User("Bruce", "Lee"));
        usersList.add(new User("Bruce", "Willis"));

        Flux.fromIterable(usersList)
            .map(user -> user.getName().toUpperCase().concat(" ").concat(user.getLastName().toUpperCase()))
            .flatMap(name -> {
                if (name.contains("bruce".toUpperCase())) {
                    return Mono.just(name);
                } else {
                    return Mono.empty();
                }
            })
            .map(String::toLowerCase)
            .subscribe(LOGGER::info);
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
                if ("bruce".equalsIgnoreCase(user.getName())) {
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
