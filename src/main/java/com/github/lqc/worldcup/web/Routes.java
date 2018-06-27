package com.github.lqc.worldcup.web;


import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class Routes {

	@Bean
	public RouterFunction<ServerResponse> helloRoute(GreetingHandler greetingHandler) {
		return RouterFunctions
				.route(GET("/hello")
								.and(RequestPredicates.accept(MediaType.TEXT_PLAIN)),
						greetingHandler::hello);
	}

	@Bean
	public RouterFunction<ServerResponse> clockRoute(TimerHandler clockHandler) {
		return RouterFunctions
				.route(GET("/clock")
								.and(RequestPredicates.accept(MediaType.APPLICATION_STREAM_JSON)),
						clockHandler::timerStream);
	}

	@Bean
	public RouterFunction<ServerResponse> listMatchesRoute(ScoreHandler scoreHandler) {
		return RouterFunctions
				.route(GET("/matches")
								.and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
						scoreHandler::listMatches);
	}

	@Bean
	public RouterFunction<ServerResponse> listScoresRoute(ScoreHandler scoreHandler) {
		return RouterFunctions
				.route(GET("/scores")
								.and(RequestPredicates.accept(MediaType.APPLICATION_STREAM_JSON)),
						scoreHandler::listScores);
	}

	@Bean
	public RouterFunction<ServerResponse> addScoreRoute(ScoreHandler scoreHandler) {
		return RouterFunctions
				.route(POST("/scores")
								.and(RequestPredicates.accept(MediaType.APPLICATION_JSON))
								.and(RequestPredicates.contentType(MediaType.APPLICATION_JSON)),
						scoreHandler::addScore);
	}

}
