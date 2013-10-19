package com.athaydes.automaton.mixins

import com.athaydes.automaton.HasSwingCode
import com.google.code.tempusfugit.temporal.Condition

import java.util.concurrent.Callable

import static com.google.code.tempusfugit.temporal.Duration.seconds
import static com.google.code.tempusfugit.temporal.Timeout.timeout
import static com.google.code.tempusfugit.temporal.WaitFor.waitOrTimeout

/**
 *
 * User: Renato
 */
@Category( HasSwingCode )
class SwingTestHelper {

	void waitForJFrameToShowUp( ) {
		waitOrTimeout condition { getJFrame()?.visible }, timeout( seconds( 5 ) )
	}

	static Condition condition( Callable<Boolean> cond ) {
		new Condition() {
			boolean isSatisfied( ) { cond() }
		}
	}

}
