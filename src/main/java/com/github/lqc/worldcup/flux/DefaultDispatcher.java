package com.github.lqc.worldcup.flux;

import com.oath.cyclops.types.persistent.PersistentList;
import cyclops.data.Seq;
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

	private PersistentList<Reducer> reducers = Seq.empty();

	private UnicastProcessor<Action> actions = UnicastProcessor.create();

	private DefaultDispatcher() {
		this.actions
				.publishOn(FluxSchedulers.dispatcherThread())
				.log()
				.subscribeOn(FluxSchedulers.dispatcherThread())
				.subscribe(this::processAction);
	}

	/**
	 * Apply action to all reducers.
	 */
	private void processAction(Action action) {
		this.reducers.forEach(reducer -> reducer.reduce(action));
	}

	@Override
	public void dispatch(Action action) {
		this.actions.onNext(action);
	}

	@Override
	public Registration register(Reducer reducer) {
		log.info("Registering reducer: {}", reducer);
		reducers = reducers.plus(reducer);
		return null;
	}
}
