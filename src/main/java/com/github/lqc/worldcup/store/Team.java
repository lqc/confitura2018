package com.github.lqc.worldcup.store;

import org.immutables.value.Value;

@Value.Immutable
public abstract class Team {

	public static Team of(String name) {
		return ImmutableTeam.of(name);
	}

	/**
	 * @return full name of the team
	 */
	public abstract String getName();

}
