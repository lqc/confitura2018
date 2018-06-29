Server-side service with Flux/Redux
===================================

This is a toy project exploring usefulness of
the Flux/Redux in a server scenario.

Building
--------

JDK10 and Maven is required to build this project.


```
mvn clean package
```

Running app
-----------

It's a Spring Boot app, so just run the JAR.

Available endpoints:

* GET ```http://localhost:8080/hello``` -- Hello world
* GET ```http://localhost:8080/clock``` -- Clock
* GET ```http://localhost:8080/matches``` -- JSON with all matches
* GET ```http://localhost:8080/scores``` -- JSON stream with all scores
* POST ```http://localhost:8080/scores``` -- post a new score
* ```ws://localhost:8080/ws``` -- Websocket view with the above

Running test
------------

Uhmmm... there are no tests ;) Did I say it's a toy project ?

Stress test
-----------

Post 1000 req/s for 5 seconds:

```
echo "POST http://localhost:8080/scores" \
    | vegeta attack -duration=5s -workers 20 -rate 1000 -header "Content-Type: application/json" -body body.json \
    | tee results.bin \
    | vegeta report
```
