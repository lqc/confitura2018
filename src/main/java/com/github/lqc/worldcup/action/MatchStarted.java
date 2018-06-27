package com.github.lqc.worldcup.action;

import org.immutables.value.Value;

@Value.Immutable()
abstract class MatchStarted implements MatchAction {

	@Value.Derived
	public Object getType() {
		return MatchStarted.class;
	}

}
