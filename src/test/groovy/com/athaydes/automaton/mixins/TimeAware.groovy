package com.athaydes.automaton.mixins

import com.athaydes.automaton.HasMixins
import com.google.code.tempusfugit.temporal.Condition

import java.util.concurrent.Callable

/**
 *
 * User: Renato
 */
@Category( HasMixins )
class TimeAware {

	long runWithTimer( Runnable action ) {
		beforeTimeRelyingTest()
		def startT = System.currentTimeMillis()
		action.run()
		System.currentTimeMillis() - startT
	}

	void beforeTimeRelyingTest( ) {
		// let's try to avoid GC during the tests
		System.gc()
	}

	Condition condition( Callable<Boolean> cond ) {
		[ isSatisfied: { cond() } ] as Condition
	}


}
