package com.github.lqc.worldcup.store.model;

import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableMatch.class)
public abstract class Match {

	public abstract String getId();

	@Value.Auxiliary
	public abstract String getGuestTeam();

	@Value.Auxiliary
	public abstract String getHostTeam();

	@Value.Auxiliary
	@JsonDeserialize(contentAs = ImmutableMatchScore.class)
	public abstract Optional<ImmutableMatchScore> getScore();

}
