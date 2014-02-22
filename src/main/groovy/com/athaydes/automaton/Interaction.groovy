package com.athaydes.automaton

import com.athaydes.internal.Config

import java.awt.*
import java.awt.event.KeyEvent
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * @author Renato
 */
@Singleton( lazy = true )
public class Interaction {

	final impl

	private Interaction() {
		impl = Config.instance.interactiveMode ?
				new UserInteraction() :
				new NoInteraction()
	}

	private class NoInteraction {
		void await( action ) {}
	}

	private class UserInteraction {

		def keyWait = new ArrayBlockingQueue( 1 )
		def onWait = false

		UserInteraction() {
			KeyboardFocusManager.currentKeyboardFocusManager
					.addKeyEventDispatcher( [
					dispatchKeyEvent: { KeyEvent e ->
						if ( onWait ) {
							e.consume()
							keyWait.add( e )
							onWait = false
							return true
						}
						return false
					} ] as KeyEventDispatcher );
		}

		void await( String action ) {
			println "Press any key to continue. Next action: $action"
			onWait = true
			keyWait.poll( 15, TimeUnit.MINUTES )
		}
	}

	void await( String action ) {
		impl.await action
	}

}
