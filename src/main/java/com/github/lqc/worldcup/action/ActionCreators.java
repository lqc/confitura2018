package com.github.lqc.worldcup.action;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.function.Supplier;

import com.github.lqc.worldcup.flux.Action;
import com.github.lqc.worldcup.flux.Dispatcher.LazyDispatch;
import com.github.lqc.worldcup.store.ImmutableMatchStoreState;
import com.github.lqc.worldcup.store.MatchStoreSelectors;
import com.github.lqc.worldcup.store.model.ImmutableMatch;

public class ActionCreators {

	public static Action createMatchStarted(ImmutableMatch match) {
		return ImmutableMatchStarted.builder()
				.matchId(match.getId())
				.build();
	}

	public static Action createMatchEnded(ImmutableMatch match) {
		return ImmutableMatchEnded.builder()
				.matchId(match.getId())
				.endTime(ZonedDateTime.now())
				.build();
	}

	/** Sync version of action creator **/
	public static Action createGoalScored(ImmutableMatch match, String scoringTeam) {
		Objects.requireNonNull(scoringTeam);
		if (!scoringTeam.equals(match.getHostTeam()) && !scoringTeam.equals(match.getGuestTeam())) {
			throw new IllegalArgumentException("Team is not participating in match");
		}
		return ImmutableGoalScored.builder()
				.matchId(match.getId())
				.scoreTime(ZonedDateTime.now())
				.scoringTeam(scoringTeam)
				.build();
	}

	/**
	 * Lazy version of action creator.
	 */
	public static LazyDispatch createGoalScored(Supplier<ImmutableMatchStoreState> state,
			String matchId,
			String scoringTeam) {
		return (dispatch) -> {
			// we should never throw here, but instead dispatch a failure action
			MatchStoreSelectors
					.match(state.get(), matchId)
					.filter(match -> match.getHostTeam().equals(scoringTeam)
							|| match.getGuestTeam().equals(scoringTeam))
					.map(match -> ImmutableGoalScored.builder()
							.matchId(match.getId())
							.scoreTime(ZonedDateTime.now())
							.scoringTeam(scoringTeam)
							.build())
					.forEach(dispatch::accept);
		};
	}

}
