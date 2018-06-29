package com.github.lqc.worldcup.web;


import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.method;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.time.Duration;
import java.time.ZonedDateTime;

import cyclops.data.HashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.HandlerAdapter;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;

@Configuration
public class Routes {

	@Bean
	public RouterFunction<ServerResponse> helloRoute(GreetingHandler greetingHandler) {
		return route(GET("/hello"),
				(request) -> ServerResponse.ok().contentType(MediaType.TEXT_PLAIN)
						.body(BodyInserters.fromObject("Hello, Spring!")));
	}

	@Bean
	public RouterFunction<ServerResponse> clockRoute(TimerHandler clockHandler) {
		return route(GET("/clock"),
				(request) -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_STREAM_JSON)
						.body(Flux.interval(Duration.ZERO, Duration.ofSeconds(1))
										.map(i -> ZonedDateTime.now()),
								ZonedDateTime.class)
		);
	}

	@Bean
	public RouterFunction<ServerResponse> matchesRoutes(MatchHandler matchHandler) {
		// @formatter:off
		return route(
					GET("/matches")
							.and(accept(MediaType.APPLICATION_JSON)),
					matchHandler::listMatches)
				.andRoute(
					POST("/matches/{matchId}/start")
							.and(accept(MediaType.APPLICATION_JSON))
							.and(contentType(MediaType.APPLICATION_JSON)),
					(request) -> matchHandler.startMatch(request.pathVariable("matchId")))
				.andNest(
						path("/scores"),
						route(
								method(HttpMethod.GET)
										.and(accept(MediaType.APPLICATION_STREAM_JSON)),
								matchHandler::listScores)
						.andRoute(
								method(HttpMethod.POST)
										.and(contentType(MediaType.APPLICATION_JSON))
										.and(accept(MediaType.APPLICATION_JSON)),
								matchHandler::addScore));
		// @formatter:on
	}

	@Bean
	public HandlerMapping webSocketMapping(MatchHandler matchHandler) {
		SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
		simpleUrlHandlerMapping.setUrlMap(HashMap.of("/ws",
				(WebSocketHandler) matchHandler::handleWebsocket).mapView());
		simpleUrlHandlerMapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return simpleUrlHandlerMapping;
	}

	@Bean
	HandlerAdapter wsHandlerAdapter() {
		return new WebSocketHandlerAdapter();
	}

}
