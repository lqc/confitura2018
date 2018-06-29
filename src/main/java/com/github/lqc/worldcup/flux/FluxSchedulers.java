package com.github.lqc.worldcup.flux;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class FluxSchedulers {

	private static final Scheduler RENDERER = Schedulers.newParallel("Renderer");

	private static final Scheduler DISPATCHER = Schedulers.newSingle("Dispatcher");

	/**
	 * @return a single-thread scheduler used by {@link Dispatcher}
	 */
	public static Scheduler dispatcherThread() {
		return DISPATCHER;
	}

	/**
	 * @return work-pool scheduler used to notify "views" (or "sinks")
	 */
	public static Scheduler rendererThread() {
		return RENDERER;
	}


}
