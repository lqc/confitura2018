module com.github.lqc.worldcup {
	requires slf4j.api;

	requires spring.beans;
	requires spring.core;
	requires spring.context;
	requires spring.web;
	requires spring.boot;
	requires spring.boot.autoconfigure;

	requires spring.webflux;
	requires reactor.core;

	// needed by spring.boot :(, fixed in 2.1
	requires java.sql;

	exports com.github.lqc.worldcup;
	opens  com.github.lqc.worldcup to spring.core;

}
