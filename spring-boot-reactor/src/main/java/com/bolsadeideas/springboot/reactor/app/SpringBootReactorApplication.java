package com.bolsadeideas.springboot.reactor.app;

import com.bolsadeideas.springboot.reactor.app.models.Comments;
import com.bolsadeideas.springboot.reactor.app.models.User;
import com.bolsadeideas.springboot.reactor.app.models.UserWithComments;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootReactorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        exampleBackPressure();
    }

    public void exampleBackPressure() {
        Flux.range(1, 10)
            .log()
            //.limitRate(5) //Esta forma es más acotada
            .subscribe(new Subscriber<>() {
                private Subscription s;
                private final Integer limit = 5;
                private Integer consumed = 0;

                @Override
                public void onSubscribe(Subscription s) {
                    this.s = s;
                    s.request(limit);
                }

                @Override
                public void onNext(Integer t) {
                    LOGGER.info(t.toString());
                    consumed++;
                    if(Objects.equals(consumed, limit)) {
                        consumed = 0;
                        s.request(limit);
                    }
                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onComplete() {

                }
            });
    }

    public void exampleInfiniteIntervalFromCreate() {
        Flux.create(emitter -> {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    private Integer count = 0;

                    @Override
                    public void run() {
                        emitter.next(++count);
                        if (count == 10) {
                            timer.cancel();
                            emitter.complete();
                        }

                        if (count == 5) {
                            timer.cancel();
                            emitter.error(new InterruptedException("Error, se ha detenido el flux en 5!"));
                        }
                    }
                }, 1000, 1000);
            })
            .subscribe(
                next -> LOGGER.info(next.toString()),
                error -> LOGGER.error(error.getMessage()),
                () -> LOGGER.info("Hemos terminado")
            );
    }

    public void exampleInfiniteInterval() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Flux.interval(Duration.ofSeconds(1))
            .doOnTerminate(latch::countDown)
            .flatMap(i -> {
                if (i >= 5) {
                    return Flux.error(new InterruptedException("Solo, hasta 5!"));
                }
                return Flux.just(i);
            })
            .map(i -> "Hola " + i)
            .retry(2)
            .subscribe(LOGGER::info, e -> LOGGER.error(e.getMessage()));

        latch.await();
    }

    public void exampleDelayElements() {
        Flux<Integer> range = Flux.range(1, 12)
            .delayElements(Duration.ofSeconds(1))
            .doOnNext(i -> LOGGER.info(i.toString()));

        range
            //.subscribe();
            .blockLast(); //no recomendable
    }

    public void exampleInterval() {
        Flux<Integer> range = Flux.range(1, 12);
        Flux<Long> delay = Flux.interval(Duration.ofSeconds(1));

        //Esto se ejecuta en segundo plano por el delay, no bloquea los procesos
        range.zipWith(delay, (r, d) -> r)
            .doOnNext(i -> LOGGER.info(i.toString()))
            //.subscribe(); //esto ejecuta en segundo plano
            .blockLast(); //esto ejecuta en el main thread
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
