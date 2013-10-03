package com.athaydes.automaton

import groovy.swing.SwingBuilder
import org.junit.After
import org.junit.Test

import javax.swing.*
import java.awt.*
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

import static com.athaydes.automaton.SwingUtil.lookup

/**
 *
 * User: Renato
 */
abstract class SwingBaseForTests {

	JFrame jFrame

	@After
	void cleanup( ) {
		jFrame?.dispose()
	}

	void testMoveTo( Closure doMove, Closure doMethodChaining ) {
		JButton btn
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 300 ] as Dimension, show: true ) {
				btn = button( text: 'Click Me', name: 'the-button' )
			}
		}

		sleep 500
		assert btn != null
		assert jFrame != null
		doMove()
		def mouseLocation = MouseInfo.pointerInfo.location
		def btnLocation = btn.locationOnScreen

		def assertMouseOnComponent = {
			assert mouseLocation.x > btnLocation.x
			assert mouseLocation.x < btnLocation.x + btn.width
			assert mouseLocation.y > btnLocation.y
			assert mouseLocation.y < btnLocation.y + btn.height
		}

		assertMouseOnComponent()

		doMethodChaining()

		assertMouseOnComponent()
	}

	void testClickOn( Closure doClick ) {
		BlockingDeque future = new LinkedBlockingDeque( 1 )
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 300 ] as Dimension, show: true ) {
				menuBar() {
					menu( name: 'menu-button', text: "File", mnemonic: 'F' ) {
						menuItem( name: 'item-exit', text: "Exit", mnemonic: 'X',
								actionPerformed: { future.add true } )
					}
				}
			}
		}

		sleep 500

		doClick()

		// wait up to 1 sec for the button to be clicked
		assert future.poll( 1, TimeUnit.SECONDS )

	}

}

class SwingAutomatonTest extends SwingBaseForTests {

	@Test
	void testMoveTo( ) {
		testMoveTo(
				{ SwingAutomaton.user.moveTo( lookup( 'the-button', jFrame ) ) },
				{
					SwingAutomaton.user.moveTo( jFrame ).moveTo( 500, 500 )
							.moveTo( lookup( 'the-button', jFrame ) )
				} )
	}

	@Test
	void testClickOn( ) {
		testClickOn {
			// this tests function and method chaining
			SwingAutomaton.user.clickOn( lookup( 'menu-button', jFrame ) ).pause( 250 )
					.clickOn( lookup( 'item-exit', jFrame ) )
		}
	}

}

class SwingerTest extends SwingBaseForTests {

	@Test
	void testMoveTo( ) {
		testMoveTo(
				{ Swinger.userWith( jFrame ).moveTo( 'the-button' ) },
				{
					Swinger.userWith( jFrame ).moveTo( 500, 500 )
							.moveTo( 'the-button' )
				} )
	}

	@Test
	void testClickOn( ) {
		testClickOn {
			// this tests function and method chaining
			Swinger.userWith( jFrame ).clickOn( 'menu-button' ).pause( 250 )
					.clickOn( 'item-exit' )
		}
	}

}
