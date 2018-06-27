package com.github.lqc.worldcup.action;

import java.time.ZonedDateTime;

import org.immutables.value.Value;

@Value.Immutable()
abstract class MatchEnded implements MatchAction {

	@Value.Derived
	public Object getType() {
		return MatchEnded.class;
	}

	public abstract ZonedDateTime getEndTime();

}
