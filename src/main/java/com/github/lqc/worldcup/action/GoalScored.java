package com.github.lqc.worldcup.action;

import java.time.ZonedDateTime;

import org.immutables.value.Value;

@Value.Immutable
abstract class GoalScored implements MatchAction {

	public abstract String getScoringTeam();

	public abstract ZonedDateTime getScoreTime();

}
