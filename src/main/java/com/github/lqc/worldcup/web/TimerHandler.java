package com.github.lqc.worldcup.web;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class TimerHandler {
	private final ZoneId UTC = ZoneId.of("UTC");

	private static final Logger log = LoggerFactory.getLogger(TimerHandler.class);

	private final AtomicLong counter = new AtomicLong(1);

	public Mono<ServerResponse> timerStream(ServerRequest request) {
		long clockId = counter.getAndIncrement();
		var response = ServerResponse.ok()
					.contentType(MediaType.APPLICATION_STREAM_JSON)
					.body(timerStream(clockId), ZonedDateTime.class);
		log.info("Clock #{} started", clockId);
		return response;
	}

	public Flux<ZonedDateTime> timerStream(long clockId) {
		return Flux.interval(Duration.ZERO, Duration.ofSeconds(1))
				.publishOn(Schedulers.single())
				.map(t -> ZonedDateTime.now(UTC))
				.doOnCancel(() -> log.info("Clock #{} stopped", clockId))
				.log();
	}

}

