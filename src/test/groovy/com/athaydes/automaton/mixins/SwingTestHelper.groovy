package com.athaydes.automaton.mixins

import com.athaydes.automaton.HasSwingCode
import com.google.code.tempusfugit.temporal.Condition

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
		waitOrTimeout( { getJFrame()?.visible } as Condition, timeout( seconds( 5 ) ) )
		getJFrame().toFront()
		sleep 1000
	}

}
