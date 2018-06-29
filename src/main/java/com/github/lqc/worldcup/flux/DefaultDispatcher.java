package com.github.lqc.worldcup.flux;

import java.util.UUID;
import java.util.function.Consumer;

import cyclops.data.TrieMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.UnicastProcessor;


/**
 * Default implemention of {@link Dispatcher}.
 *
 * The dispatcher is single threaded and always calls reducers on @
 */
@Component
public class DefaultDispatcher implements Dispatcher {

	private static final Logger log = LoggerFactory.getLogger(DefaultDispatcher.class);

	private TrieMap<String, Consumer<Action>> reducers = TrieMap.empty();

	private UnicastProcessor<Object> actions = UnicastProcessor.create();

	private DefaultDispatcher() {
		this.actions
				.publishOn(FluxSchedulers.dispatcherThread())
				.subscribe(o -> {
					if (o instanceof Action) {
						processAction((Action) o);
					} else if (o instanceof LazyDispatch) {
						var ld = (LazyDispatch) o;
						ld.dispatchAction(this::processAction);
					}

				});
	}

	/**
	 * Apply action to all reducers.
	 */
	private void processAction(Action action) {
		long s = System.nanoTime();
		this.reducers.forEach(reducer -> reducer._2().accept(action));
		long e = System.nanoTime();
		log.info("Reduction took {} ms", (e - s) / 1_000_000.0);
	}

	@Override
	public void dispatch(Action action) {
		this.actions.onNext(action);
	}

	@Override
	public void dispatch(LazyDispatch action) {
		this.actions.onNext(action);
	}

	@Override
	public String register(Consumer<Action> reducer) {
		var id = UUID.randomUUID().toString();
		log.info("Registering reducer: {} under id {}", reducer, id);
		reducers = reducers.put(id, reducer);
		return id;
	}

	@Override
	public void unregister(String token) {
		reducers = reducers.remove(token);
	}
}
