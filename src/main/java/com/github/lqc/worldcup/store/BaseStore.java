package com.github.lqc.worldcup.store;

import java.util.concurrent.atomic.AtomicReference;

import com.github.lqc.worldcup.flux.Action;
import com.github.lqc.worldcup.flux.Dispatcher;
import com.github.lqc.worldcup.flux.FluxSchedulers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

/**
 * Base implementation of a reactive store.
 *
 * @param <S> type of state
 */
public abstract class BaseStore<S> {

	private final ReplayProcessor<S> stateSubject;
	private final AtomicReference<S> state = new AtomicReference<>();

	protected BaseStore(Dispatcher dispatcher) {
		this.state.set(rehydrate());
		this.stateSubject = ReplayProcessor.cacheLastOrDefault(getState());

		dispatcher.register(this::handleAction);
	}

	public S getState() {
		return state.get();
	}

	/**
	 * Create initial state.
	 */
	protected abstract S emptyState();

	/**
	 * Load state from "hard" storage. By default, just create an empty state.
	 */
	protected S rehydrate() {
		return emptyState();
	}

	public Flux<S> stateChanges() {
		return stateSubject.publishOn(FluxSchedulers.rendererThread());
	}

	protected void handleAction(Action action) {
		S currentState = state.get();
		S nextState = this.reduce(state.get(), action);
		if (currentState != nextState) {
			state.set(nextState);
			stateSubject.onNext(nextState);
		}
	}

	/**
	 * @param state
	 * @param action
	 * @return
	 */
	protected abstract S reduce(S state, Action action);


}
