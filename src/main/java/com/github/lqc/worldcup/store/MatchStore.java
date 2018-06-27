package com.github.lqc.worldcup.store;

import java.io.IOException;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.github.lqc.worldcup.action.ImmutableGoalScored;
import com.github.lqc.worldcup.action.ImmutableMatchStarted;
import com.github.lqc.worldcup.flux.Action;
import com.github.lqc.worldcup.flux.Dispatcher;
import com.github.lqc.worldcup.store.model.ImmutableMatch;
import com.github.lqc.worldcup.store.model.ImmutableMatchScore;
import cyclops.control.Option;
import cyclops.data.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Store that keeps match scores.
 */
@Component
public class MatchStore extends BaseStore<ImmutableMatchStoreState> {

	private static final Logger log = LoggerFactory.getLogger(MatchStore.class);

	@Autowired
	public MatchStore(Dispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected ImmutableMatchStoreState emptyState() {
		return ImmutableMatchStoreState.builder()
				.matches(HashMap.empty())
				.build();
	}

	@Override
	protected ImmutableMatchStoreState reduce(ImmutableMatchStoreState state, Action action) {
		final Object actionType = action.getType();
		var matches = state.getMatches();
		if (actionType == ImmutableMatchStarted.class) {
			var matchStartedAction = (ImmutableMatchStarted) action;
			return matches.get(matchStartedAction.getMatchId())
					.map(m -> m.withScore(ImmutableMatchScore.builder()
							.hostTeamScore(0)
							.guestTeamScore(0)
							.build()))
					.map(m -> matches.put(m.getId(), m))
					.map(state::withMatches)
					.orElse(state);
		} else if (actionType == ImmutableGoalScored.class) {
			var scoreAction = (ImmutableGoalScored) action;
			return matches.get(scoreAction.getMatchId())
					.flatMap(m -> Option.fromOptional(m.getScore())
							.map(score -> updateScore(m, scoreAction.getScoringTeam(), score))
							.map(m::withScore))
					.map(m -> matches.put(m.getId(), m))
					.map(state::withMatches)
					.orElse(state);
		}
		// no action
		return state;
	}

	private ImmutableMatchScore updateScore(ImmutableMatch match, String scoringTeam, ImmutableMatchScore score) {
		if (scoringTeam.equals(match.getHostTeam())) {
			return score.withHostTeamScore(score.getHostTeamScore() + 1);
		} else if (scoringTeam.equals(match.getGuestTeam())) {
			return score.withGuestTeamScore(score.getGuestTeamScore() + 1);
		} else {
			throw new IllegalStateException("Could not score for team " + scoringTeam +
					". it does not play in " + match);
		}
	}

	@Override
	protected ImmutableMatchStoreState rehydrate() {
		var objectMapper = new ObjectMapper();
		objectMapper.registerModule(new Jdk8Module());

		var state = super.rehydrate();
		try {
			MappingIterator<Object> iterator = objectMapper
					.readerFor(ImmutableMatch.class)
					.readValues(getClass().getResource("/matches.json"));
			var matches = state.getMatches();
			while(iterator.hasNext()) {
				var match = (ImmutableMatch) iterator.nextValue();
				matches = matches.put(match.getId(), match);
			}
			return state.withMatches(matches);
		} catch(IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
