package com.github.lqc.worldcup.web;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.github.lqc.worldcup.action.ActionCreators;
import com.github.lqc.worldcup.flux.Dispatcher;
import com.github.lqc.worldcup.store.MatchStore;
import com.github.lqc.worldcup.store.MatchStoreSelectors;
import com.github.lqc.worldcup.store.model.ImmutableMatch;
import cyclops.data.HashMap;
import cyclops.data.LazySeq;
import cyclops.data.Vector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ScoreHandler {

	private final MatchStore scoreStore;
	private final Dispatcher dispatcher;

	@Autowired
	public ScoreHandler(MatchStore scoreStore, Dispatcher dispatcher) {
		this.scoreStore = scoreStore;
		this.dispatcher = dispatcher;
	}

	public Mono<ServerResponse> listScores(ServerRequest request) {
		var response = ServerResponse.ok()
				.contentType(MediaType.APPLICATION_STREAM_JSON)
				.body(watchAllMatches(), ParameterizedTypeReference.forType(List.class));
		return response;
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
					for(var m : matchesWithScores) {
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
									.syncBody("Request accepted.");
						})
						.orElseGet(() -> ServerResponse.badRequest()
								.contentType(MediaType.TEXT_PLAIN)
								.syncBody("Match does not exist or team is not participating: " + input)));

	}




}
