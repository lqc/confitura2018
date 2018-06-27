package com.github.lqc.worldcup.store;

import com.github.lqc.worldcup.store.model.ImmutableMatch;
import cyclops.data.ImmutableMap;
import org.immutables.value.Value;

@Value.Immutable
public abstract class MatchStoreState {

	public abstract ImmutableMap<String, ImmutableMatch> getMatches();

}
