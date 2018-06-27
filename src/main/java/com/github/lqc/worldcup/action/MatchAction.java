package com.github.lqc.worldcup.action;

import com.github.lqc.worldcup.flux.Action;
import com.github.lqc.worldcup.store.model.ImmutableMatch;

interface MatchAction extends Action {

	String getMatchId();


}
