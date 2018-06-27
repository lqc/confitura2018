package com.github.lqc.worldcup.store;

import java.util.Optional;

import com.github.lqc.worldcup.store.model.ImmutableMatch;
import com.github.lqc.worldcup.store.model.ImmutableMatchScore;
import cyclops.control.Option;
import cyclops.data.LazySeq;

public class MatchStoreSelectors {

	/**
	 * @return list of all matches (computed lazily, no copying).
	 */
	public static LazySeq<ImmutableMatch> listAllMatches(ImmutableMatchStoreState state) {
		return state.getMatches()
				.lazySeq()
				.map(tuple -> tuple._2());
	}

	/**
	 * @return list of all matches (computed lazily, no copying).
	 */
	public static LazySeq<ImmutableMatch> listMatchesWithScores(ImmutableMatchStoreState state) {
		return state.getMatches()
				.lazySeq()
				.map(tuple -> tuple._2())
				.filter(m -> m.getScore().isPresent());
	}

	/**
	 * @return score for given match
	 */
	public static Option<ImmutableMatchScore> scoreForMatch(ImmutableMatchStoreState state, String matchId) {
		return match(state, matchId)
				.flatMap(m -> Option.fromOptional(m.getScore()));
	}

	/**
	 * @return score for given match
	 */
	public static Option<ImmutableMatch> match(ImmutableMatchStoreState state, String matchId) {
		return state.getMatches()
				.get(matchId);
	}

	/**
	 * @return all scores for given team
	 */
	public static LazySeq<ImmutableMatchScore> listTeamScores(ImmutableMatchStoreState state, String team) {
		return listAllMatches(state)
				.filter(m -> m.getGuestTeam().equals(team) || m.getHostTeam().equals(team))
				.map(m -> m.getScore())
				.filterNot(Optional::isPresent)
				.map(Optional::get);
	}

	private MatchStoreSelectors() {
	}


}
