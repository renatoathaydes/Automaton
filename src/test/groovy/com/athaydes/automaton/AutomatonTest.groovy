package com.athaydes.automaton

import com.athaydes.automaton.mixins.SwingTestHelper
import com.athaydes.automaton.mixins.TimeAware
import groovy.swing.SwingBuilder
import org.junit.After
import org.junit.Test

import javax.swing.*
import java.awt.*
import java.awt.event.MouseEvent
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
@Mixin( [ SwingTestHelper, TimeAware ] )
class AutomatonTest implements HasSwingCode {

	JFrame jFrame

	@After
	void cleanup() {
		jFrame?.dispose()
	}

	@Test
	void testMoveToXY() {
		beforeTimeRelyingTest()

		Automaton.user.moveTo 50, 50, VERY_FAST
		assert MouseInfo.pointerInfo.location == new Point( 50, 50 )

		def veryFastDelta = runWithTimer {
			Automaton.user.moveTo 100, 150, VERY_FAST
		}
		assert MouseInfo.pointerInfo.location == new Point( 100, 150 )

		def fastDelta = runWithTimer {
			Automaton.user.moveTo 50, 50, FAST
		}
		assert MouseInfo.pointerInfo.location == new Point( 50, 50 )

		def mediumDelta = runWithTimer {
			Automaton.user.moveTo 100, 150, MEDIUM
		}
		assert MouseInfo.pointerInfo.location == new Point( 100, 150 )

		def slowDelta = runWithTimer {
			Automaton.user.moveTo 50, 50, SLOW
		}
		assert MouseInfo.pointerInfo.location == new Point( 50, 50 )

		assert slowDelta > mediumDelta
		assert mediumDelta > fastDelta
		assert fastDelta > veryFastDelta
		assert slowDelta < 5000
		assert veryFastDelta > 50

		def defaultDelta = runWithTimer {
			Automaton.user.moveTo 100, 150
		}
		def tolerance = 50 // ms
		def expectedDelta = defaultFrom slowDelta, mediumDelta, fastDelta, veryFastDelta
		assertEquals defaultDelta, expectedDelta, tolerance

		// test method chaining
		Automaton.user.moveTo( 50, 50 ).moveTo( 100, 100 ).moveTo( 150, 50 )
		assert MouseInfo.pointerInfo.location == new Point( 150, 50 )

	}

	@Test
	void testMoveToPoint() {
		beforeTimeRelyingTest()

		Automaton.user.moveTo new Point( 60, 60 ), VERY_FAST
		assert MouseInfo.pointerInfo.location == new Point( 60, 60 )
	}

	@Test
	void testMoveToPointWhenPointMoves() {
		beforeTimeRelyingTest()

		def target = new Point( 200, 150 )
		Automaton.user.moveTo( 20, 50 )

		Thread.start { Automaton.user.moveTo target, MEDIUM }
		sleep 500
		target.setLocation( 300, 100 )

		waitOrTimeout( condition { MouseInfo.pointerInfo.location == target },
				timeout( seconds( 5 ) ) )
	}

	@Test
	void testMoveToClosure() {
		beforeTimeRelyingTest()

		def target = new Point( 200, 150 )
		Automaton.user.moveTo( 20, 50 )

		Thread.start { Automaton.user.moveTo( { new Point( target ) }, MEDIUM ) }
		sleep 500
		target.setLocation( 300, 100 )

		waitOrTimeout( condition { MouseInfo.pointerInfo.location == target },
				timeout( seconds( 5 ) ) )
	}

	static long defaultFrom( long slow, long medium, long fast, long veryFast ) {
		switch ( Automaton.DEFAULT ) {
			case SLOW: return slow
			case MEDIUM: return medium
			case FAST: return fast
			case VERY_FAST: return veryFast
			default: throw new RuntimeException( "Unknown speed: ${Automaton.DEFAULT}" )
		}
	}

	@Test
	void testMoveBy() {
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
	void testDragBy() {
		beforeTimeRelyingTest()

		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', location: [ 250, 50 ] as Point,
					size: [ 300, 20 ] as Dimension, show: true )
		}
		waitForJFrameToShowUp()
		def initialLocation = jFrame.locationOnScreen
		SwingAutomaton.user.moveTo( jFrame )

		def assertDraggedBy = { x, y ->
			waitOrTimeout( condition {
				jFrame.locationOnScreen.x == initialLocation.x + x &&
						jFrame.locationOnScreen.y == initialLocation.y + y
			}, timeout( seconds( 1 ) ) )
			initialLocation = jFrame.locationOnScreen
			sleep 500 // to avoid maximizing window in some OS's
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
		Automaton.user.dragBy( 150, 0 ).dragBy( 0, 150 )
				.dragBy( -150, 0 ).dragBy( 0, -150 )
		assertDraggedBy 0, 0
	}

	@Test
	void testDragFromTo() {
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', locationRelativeTo: null,
					size: [ 300, 30 ] as Dimension, show: true )
		}
		waitForJFrameToShowUp()
		def initialLocation = jFrame.locationOnScreen
		def deltaX = 50
		def deltaY = 30
		def fromX = initialLocation.x + 150
		def fromY = initialLocation.y + 20

		Automaton.user.dragFrom( fromX, fromY )
				.onto( fromX + deltaX, fromY + deltaY )

		sleep 250
		assert initialLocation.x + deltaX == jFrame.locationOnScreen.x
		assert initialLocation.y + deltaY == jFrame.locationOnScreen.y
	}

	@Test
	void testClick() {
		def future = new LinkedBlockingDeque<MouseEvent>( 3 )
		JButton btn
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 100, 100 ] as Dimension, show: true ) {
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
	void testDoubleClick() {
		def future = new LinkedBlockingDeque( 2 )
		JButton btn
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 100, 100 ] as Dimension, show: true ) {
				btn = button( text: 'Click Me', name: 'the-button',
						mouseClicked: { MouseEvent e -> future.add e } )
			}
		}

		waitForJFrameToShowUp()
		assert btn != null
		SwingAutomaton.user.moveTo( btn ).doubleClick()

		// wait up to 3 secs for the button to be clicked
		2.times {
			def event = future.poll( 3, TimeUnit.SECONDS )
			assert event != null
			assert event.button == MouseEvent.BUTTON1
		}
	}

	@Test
	void testRightClick() {
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
	void testPause() {
		beforeTimeRelyingTest()
		def user = Automaton.user
		def t = runWithTimer {
			assert user == user.pause( 100 )
		}
		assertEquals( 100, t, 20 )
	}

	@Test
	void testType() {
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
	void testPressSimultaneously() {
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

	@Test
	void pressSimulaneouslyShouldTriggerClickEventWhenMnemonic() {
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 300 ] as Dimension, show: true ) {
				menuBar() {
					menu( name: 'menu-button', text: "File", mnemonic: 'F' ) {
						menuItem( name: 'item-exit', text: "Exit", mnemonic: 'X',
								actionPerformed: { jFrame.dispose() } )
					}
				}
			}
		}

		waitForJFrameToShowUp()

		Automaton.user
				.pressSimultaneously( VK_ALT, VK_F )
				.pressSimultaneously( VK_X )

		waitOrTimeout( condition { ( !jFrame.visible ) }, timeout( seconds( 3 ) ) )

	}

	@Override
	JFrame getJFrame() { jFrame }
}
