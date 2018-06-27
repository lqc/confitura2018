module com.github.lqc.worldcup {
	requires static java.annotation;

	// Immutables
	requires static org.immutables.value;
	requires error.prone.annotations;

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

	// immutable data structures
	requires cyclops;
	requires org.reactivestreams;

	// fixtures
	requires jackson.annotations;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.datatype.jdk8;

	exports com.github.lqc.worldcup.web;
	exports com.github.lqc.worldcup.store;

	opens com.github.lqc.worldcup to spring.core, spring.beans, spring.context;
	opens com.github.lqc.worldcup.web to spring.core, spring.beans, spring.context, com.fasterxml.jackson.databind;
	opens com.github.lqc.worldcup.flux to spring.core, spring.beans, spring.context;

	opens com.github.lqc.worldcup.store to spring.core, com.fasterxml.jackson.databind;
	opens com.github.lqc.worldcup.store.model to com.fasterxml.jackson.databind;

}
