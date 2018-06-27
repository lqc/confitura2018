package com.github.lqc.worldcup.flux;

/**
 * Representation of an action that fill be forwarder to the Dispatcher.
 */
public interface Action {

	/**
	 * Type of an action can be any unique token. By default we use the class of the action itself,
	 * but you can also just use plain strings (in JS Symbols are commonly used).
	 */
	default Object getType() {
		return getClass();
	}

}
