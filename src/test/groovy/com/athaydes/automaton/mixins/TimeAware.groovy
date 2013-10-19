package com.athaydes.automaton.mixins

import com.athaydes.automaton.HasMixins

/**
 *
 * User: Renato
 */
@Category( HasMixins )
class TimeAware {

	long runWithTimer( Runnable action ) {
		def startT = System.currentTimeMillis()
		action.run()
		System.currentTimeMillis() - startT
	}

	void beforeTimeRelyingTest( ) {
		// let's try to avoid GC during the tests
		System.gc()
	}

}
