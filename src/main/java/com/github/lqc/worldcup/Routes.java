package com.github.lqc.worldcup;


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
				.route(RequestPredicates.GET("/hello").and(RequestPredicates.accept(MediaType.TEXT_PLAIN)),
						greetingHandler::hello);
	}

	@Bean
	public RouterFunction<ServerResponse> clockRoute(TimerHandler clockHandler) {
		return RouterFunctions
				.route(RequestPredicates.GET("/clock")
								.and(RequestPredicates.accept(MediaType.APPLICATION_STREAM_JSON)),
						clockHandler::timerStream);
	}

}
