package com.github.lqc.worldcup.web;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;


@Value.Immutable
@JsonDeserialize(as = ImmutableGoalScoredInput.class)
public abstract class GoalScoredInput {

	public abstract String getMatchId();

	public abstract String getScoringTeam();




}
