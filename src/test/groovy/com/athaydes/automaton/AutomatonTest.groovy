package com.athaydes.automaton

import com.google.code.tempusfugit.temporal.Condition
import groovy.swing.SwingBuilder
import org.junit.After
import org.junit.Test

import javax.swing.*
import java.awt.*
import java.awt.event.MouseEvent
import java.util.concurrent.Callable
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

import static com.athaydes.automaton.Speed.*
import static com.google.code.tempusfugit.temporal.Duration.seconds
import static com.google.code.tempusfugit.temporal.Timeout.timeout
import static com.google.code.tempusfugit.temporal.WaitFor.waitOrTimeout
import static java.awt.event.KeyEvent.*
import static org.junit.Assert.assertEquals

/**
 *
 * User: Renato
 */
class AutomatonTest {

	JFrame jFrame

	@After
	void cleanup( ) {
		jFrame?.dispose()
	}

	@Test
	void testMoveTo( ) {
		beforeTimeRelyingTest()

		Automaton.user.moveTo 0, 0, VERY_FAST
		assert MouseInfo.pointerInfo.location == new Point( 0, 0 )

		def veryFastDelta = runWithTimer {
			Automaton.user.moveTo 50, 100, VERY_FAST
		}
		assert MouseInfo.pointerInfo.location == new Point( 50, 100 )

		def fastDelta = runWithTimer {
			Automaton.user.moveTo 0, 0, FAST
		}
		assert MouseInfo.pointerInfo.location == new Point( 0, 0 )

		def mediumDelta = runWithTimer {
			Automaton.user.moveTo 50, 100, MEDIUM
		}
		assert MouseInfo.pointerInfo.location == new Point( 50, 100 )

		def slowDelta = runWithTimer {
			Automaton.user.moveTo 0, 0, SLOW
		}
		assert MouseInfo.pointerInfo.location == new Point( 0, 0 )

		assert slowDelta > mediumDelta
		assert mediumDelta > fastDelta
		assert fastDelta > veryFastDelta
		assert slowDelta < 5000
		assert veryFastDelta > 50

		def defaultDelta = runWithTimer {
			Automaton.user.moveTo 50, 100
		}
		def tolerance = 25 // ms
		def expectedDelta = defaultFrom slowDelta, mediumDelta, fastDelta, veryFastDelta
		assertEquals defaultDelta, expectedDelta, tolerance

		// test method chaining
		Automaton.user.moveTo( 0, 0 ).moveTo( 50, 50 ).moveTo( 100, 0 )
		assert MouseInfo.pointerInfo.location == new Point( 100, 0 )

	}

	static long defaultFrom( long slow, long medium, long fast, long veryFast ) {
		switch ( Automaton.DEFAULT ) {
			case SLOW: return slow
			case MEDIUM: return medium
			case FAST: return fast
			case VERY_FAST: return veryFast
		}
	}

	@Test
	void testMoveBy( ) {
		beforeTimeRelyingTest()

		Automaton.user.moveTo 50, 50, VERY_FAST

		def veryFastDelta = runWithTimer {
			Automaton.user.moveBy 50, 50, VERY_FAST
		}
		assert MouseInfo.pointerInfo.location == new Point( 100, 100 )

		def fastDelta = runWithTimer {
			Automaton.user.moveBy( -50, 50, FAST )
		}
		assert MouseInfo.pointerInfo.location == new Point( 50, 150 )

		def mediumDelta = runWithTimer {
			Automaton.user.moveBy( -50, -50, MEDIUM )
		}
		assert MouseInfo.pointerInfo.location == new Point( 0, 100 )

		def slowDelta = runWithTimer {
			Automaton.user.moveBy( 50, -50, SLOW )
		}
		assert MouseInfo.pointerInfo.location == new Point( 50, 50 )

		assert slowDelta > mediumDelta
		assert mediumDelta > fastDelta
		assert fastDelta > veryFastDelta
		assert slowDelta < 5000
		assert veryFastDelta > 50

		def defaultDelta = runWithTimer {
			Automaton.user.moveBy 50, 50
		}
		def tolerance = 25
		def expectedDelta = defaultFrom slowDelta, mediumDelta, fastDelta, veryFastDelta
		assertEquals defaultDelta, expectedDelta, tolerance

		// test method chaining
		def currLocation = MouseInfo.pointerInfo.location
		Automaton.user.moveBy( 10, 10 ).moveBy( -20, -20 ).moveBy( 10, 10 )
		assert MouseInfo.pointerInfo.location == currLocation
	}

	@Test
	void testDragBy( ) {
		beforeTimeRelyingTest()

		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 30 ] as Dimension, show: true )
		}
		waitForJFrameToShowUp()
		def initialLocation = jFrame.locationOnScreen
		SwingAutomaton.user.moveTo( jFrame )

		def assertDraggedBy = { x, y ->
			assert jFrame.locationOnScreen.x == initialLocation.x + x
			assert jFrame.locationOnScreen.y == initialLocation.y + y
			initialLocation = jFrame.locationOnScreen
		}

		def veryFastDelta = runWithTimer {
			Automaton.user.dragBy 50, 50, VERY_FAST
		}
		assertDraggedBy 50, 50

		def fastDelta = runWithTimer {
			Automaton.user.dragBy( -50, 50, FAST )
		}
		assertDraggedBy( -50, 50 )

		def mediumDelta = runWithTimer {
			Automaton.user.dragBy( -50, -50, MEDIUM )
		}
		assertDraggedBy( -50, -50 )

		def slowDelta = runWithTimer {
			Automaton.user.dragBy( 50, -50, SLOW )
		}
		assertDraggedBy( 50, -50 )

		assert slowDelta > mediumDelta
		assert mediumDelta > fastDelta
		assert fastDelta > veryFastDelta
		assert slowDelta < 5000
		assert veryFastDelta > 50

		def defaultDelta = runWithTimer {
			Automaton.user.dragBy 50, 50
		}
		assertDraggedBy 50, 50

		def tolerance = 50 // ms
		def expectedDelta = defaultFrom slowDelta, mediumDelta, fastDelta, veryFastDelta
		assertEquals defaultDelta, expectedDelta, tolerance

		// test method chaining
		Automaton.user.dragBy( -50, 0 ).dragBy( 0, 50 )
				.dragBy( 50, 0 ).dragBy( 0, -50 )
		assertDraggedBy 0, 0
	}

	@Test
	void testClick( ) {
		def future = new LinkedBlockingDeque<MouseEvent>( 3 )
		JButton btn
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 300 ] as Dimension, show: true ) {
				btn = button( text: 'Click Me', name: 'the-button',
						mouseClicked: { MouseEvent e -> future.add e } )
			}
		}

		waitForJFrameToShowUp()
		assert btn != null
		SwingAutomaton.user.moveTo( btn ).click()

		// wait up to 3 secs for the button to be clicked
		def event = future.poll( 3, TimeUnit.SECONDS )
		assert event != null
		assert event.button == MouseEvent.BUTTON1

		// test method chaining
		Automaton.user.click().click().click()
		3.times { assert future.poll( 3, TimeUnit.SECONDS ) }

	}

	@Test
	void testRightClick( ) {
		def future = new LinkedBlockingDeque<MouseEvent>( 3 )
		JTextArea jta
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 300 ] as Dimension, show: true ) {
				jta = textArea( text: 'Click Me', name: 'the-button',
						mouseClicked: { MouseEvent e -> future.add e } )
			}
		}

		waitForJFrameToShowUp()
		assert jta != null
		SwingAutomaton.user.moveTo( jta ).rightClick()

		// wait up to 3 secs for the button to be clicked
		def event = future.poll( 3, TimeUnit.SECONDS )
		assert event != null
		assert event.button == MouseEvent.BUTTON3

		// test method chaining
		Automaton.user.rightClick().rightClick().rightClick()
		3.times { assert future.poll( 3, TimeUnit.SECONDS ) }

	}

	@Test
	void testPause( ) {
		beforeTimeRelyingTest()
		def user = Automaton.user
		def t = runWithTimer {
			assert user == user.pause( 100 )
		}
		assertEquals( 100, t, 20 )
	}

	@Test
	void testType( ) {
		JTextArea jta
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 300 ] as Dimension, show: true ) {
				jta = textArea()
			}
		}

		waitForJFrameToShowUp()
		assert jta != null
		assert jta.text == ''

		SwingAutomaton.user.moveTo( jta ).click().type( 'I can type here' ).pause( 100 )
		assert jta.text == 'I can type here'

		5.times { Automaton.user.type( VK_BACK_SPACE ) }
		Automaton.user.type( VK_ENTER ).type( VK_TAB ).type( '1234567890' ).pause( 100 )
		assert jta.text == 'I can type\n\t1234567890'

	}

	@Test
	void testPressSimultaneously( ) {
		JTextArea jta
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 300 ] as Dimension, show: true ) {
				jta = textArea()
			}
		}

		waitForJFrameToShowUp()
		assert jta != null
		assert jta.text == ''

		Automaton.user.type( 'a' ).pressSimultaneously( VK_SHIFT, VK_A ).pause( 100 )
		assert jta.text == 'aA'
	}

	static long runWithTimer( Runnable action ) {
		def startT = System.currentTimeMillis()
		action.run()
		System.currentTimeMillis() - startT
	}

	private void beforeTimeRelyingTest( ) {
		// let's try to avoid GC during the tests
		System.gc()
	}

	private void waitForJFrameToShowUp( ) {
		waitOrTimeout condition { jFrame?.visible }, timeout( seconds( 5 ) )
	}

	static Condition condition( Callable<Boolean> cond ) {
		new Condition() {
			boolean isSatisfied( ) { cond() }
		}
	}

}
