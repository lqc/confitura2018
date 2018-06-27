package com.github.lqc.worldcup.action;

import java.time.ZonedDateTime;
import java.util.Objects;

import com.github.lqc.worldcup.flux.Action;
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


}
