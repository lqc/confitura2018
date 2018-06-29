package com.github.lqc.worldcup.web;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.github.lqc.worldcup.action.ActionCreators;
import com.github.lqc.worldcup.flux.Dispatcher;
import com.github.lqc.worldcup.flux.Dispatcher.LazyDispatch;
import com.github.lqc.worldcup.store.MatchStore;
import com.github.lqc.worldcup.store.MatchStoreSelectors;
import com.github.lqc.worldcup.store.model.ImmutableMatch;
import cyclops.data.HashMap;
import cyclops.data.LazySeq;
import cyclops.data.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketMessage.Type;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class MatchHandler {

	private static final Logger log = LoggerFactory.getLogger(MatchHandler.class);

	private final MatchStore scoreStore;
	private final Dispatcher dispatcher;

	@Autowired
	public MatchHandler(MatchStore scoreStore, Dispatcher dispatcher) {
		this.scoreStore = scoreStore;
		this.dispatcher = dispatcher;
	}

	public Mono<ServerResponse> listScores(ServerRequest request) {
		return Mono.just(request)
				.log()
				.flatMap(request_ -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_STREAM_JSON)
						.body(watchAllMatches(), ParameterizedTypeReference.forType(List.class)));
	}

	public Mono<ServerResponse> listMatches(ServerRequest request) {
		var response = ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(MatchStoreSelectors.listAllMatches(scoreStore.getState()), ImmutableMatch.class);
		return response;
	}

	/**
	 * @return flux that emits all currently known scores followed by live updates.
	 */
	private <T extends LazySeq<ImmutableMatch>> Flux<List<ImmutableMatch>> watchAllMatches() {
		// there is no actual threading here
		var seenRef = new AtomicReference<>(HashMap.empty());

		return scoreStore.stateChanges()
				.map(MatchStoreSelectors::listMatchesWithScores)
				.map(matchesWithScores -> {
					var seen = seenRef.get();
					var delta = Vector.<ImmutableMatch>empty();
					for (var m : matchesWithScores) {
						var score = m.getScore().get();
						if (seen.get(m.getId()).filter(score::equals).isPresent()) {
							continue;
						}
						// not seen
						seen = seen.put(m.getId(), score);
						delta = delta.plus(m);
					}
					seenRef.set(seen);
					return delta.toList();
				});
	}

	public Mono<ServerResponse> addScore(ServerRequest serverRequest) {
		return serverRequest
				.bodyToMono(GoalScoredInput.class)
				.flatMap(input -> MatchStoreSelectors
						.match(scoreStore.getState(), input.getMatchId())
						.filter(match -> match.getHostTeam().equals(input.getScoringTeam())
								|| match.getGuestTeam().equals(input.getScoringTeam()))
						.map(match -> {
							var action = ActionCreators.createGoalScored(match, input.getScoringTeam());
							dispatcher.dispatch(action);

							return ServerResponse.ok()
									.contentType(MediaType.TEXT_PLAIN)
									.syncBody("Request accepted");
						})
						.orElseGet(() -> ServerResponse.badRequest()
								.contentType(MediaType.TEXT_PLAIN)
								.syncBody("Match does not exist or team is not participating: " + input)));
	}

	public Mono<ServerResponse> startMatch(String matchId) {
		return Mono.just(matchId)
				.flatMap(input -> MatchStoreSelectors
						.match(scoreStore.getState(), input)
						.filter(match -> !match.getScore().isPresent())
						.map(match -> {
							var action = ActionCreators.createMatchStarted(match);
							dispatcher.dispatch(action);

							return ServerResponse.ok()
									.contentType(MediaType.TEXT_PLAIN)
									.syncBody("Request accepted");
						})
						.orElseGet(() -> ServerResponse.badRequest()
								.contentType(MediaType.TEXT_PLAIN)
								.syncBody("Match does not exist or already started" + input)));
	}

	public Mono<Void> handleWebsocket(WebSocketSession session) {

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Jdk8Module());
		Jackson2JsonDecoder decoder = new Jackson2JsonDecoder(mapper);
		Jackson2JsonEncoder encoder = new Jackson2JsonEncoder(mapper);

		Function<WebSocketMessage, Mono<Map<String, ?>>> decode = message -> {
			return decoder.decodeToMono(
					Flux.just(message.retain().getPayload()),
					ResolvableType.forClass(Map.class),
					null,
					null)
					.map(o -> (Map<String, ?>) o);
		};

		Mono<Void> input = session.receive()
				.log("WS receive")
				.flatMap(decode)
				.map((Map<String, ?> data) -> {
					switch ((String) data.get("type")) {
						case "GOAL_SCORED":
							return ActionCreators.createGoalScored(
									scoreStore::getState,
									(String) data.get("matchId"),
									(String) data.get("scoringTeam"));
						default:
							// do nothing
							return null;
					}
				})
				.filter(Objects::nonNull)
				.doOnNext((LazyDispatch action) -> dispatcher.dispatch(action))
				.then();

		Mono<Void> output = session.send(scoreStore.stateChanges()
				.log("WS send")
				.flatMap(state -> {
					var dataStream = encoder.encode(Mono.just(state.getMatches().mapView()),
							session.bufferFactory(),
							ResolvableType.forClass(Map.class),
							null,
							null);
					return DataBufferUtils.join(dataStream);
				})
				.map(dataBuffer -> new WebSocketMessage(Type.TEXT, dataBuffer))
				.doOnNext(msg -> log.info("Message: {}", msg))
		).then();

		return Mono.zip(input, output).then();
	}
}
