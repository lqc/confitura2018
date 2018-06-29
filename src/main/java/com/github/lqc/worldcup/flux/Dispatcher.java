package com.github.lqc.worldcup.flux;


import java.util.function.Consumer;

import reactor.core.publisher.Mono;

/**
 * Public interface for a Flux dispatcher.
 *
 */
public interface Dispatcher {

	/**
	 * Dispatch given action to all registered stores.
	 */
	void dispatch(Action action);

	void dispatch(LazyDispatch action);

	/**
	 * Register given {@code reducer} with this dispatcher.
	 *
	 * @return unique token
	 */
	String register(Consumer<Action> reducer);

	/**
	 * Remove registration from this dispatcher.
	 */
	void unregister(String token);

	@FunctionalInterface
	public interface LazyDispatch {
		void dispatchAction(Consumer<Action> dispatcher);
	}
}
