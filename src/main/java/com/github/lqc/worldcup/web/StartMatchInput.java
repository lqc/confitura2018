package com.github.lqc.worldcup.web;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;


@Value.Immutable
@JsonDeserialize(as = ImmutableStartMatchInput.class)
public abstract class StartMatchInput {

	public abstract String getMatchId();


}
