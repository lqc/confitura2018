package com.github.lqc.worldcup.flux;


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

	/**
	 * Register given {@code reducer} with this dispatcher instance
	 *
	 * @return handle that can be used to remove store
	 */
	Registration register(Reducer reducer);

	interface Registration {
		Mono<Void> unsubscribe();
	}

	interface Reducer {
		void reduce(Action action);
	}
}
