package com.github.lqc.worldcup.store.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableMatchScore.class)
public abstract class MatchScore {

	public abstract int getGuestTeamScore();

	public abstract int getHostTeamScore();


}
