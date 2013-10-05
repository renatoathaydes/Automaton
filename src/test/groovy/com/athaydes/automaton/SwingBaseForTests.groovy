package com.athaydes.automaton

import groovy.swing.SwingBuilder
import org.junit.After
import org.junit.Test

import javax.swing.*
import java.awt.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

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

	void testMoveTo( Closure doMove ) {
		JButton btn = null
		def blockUntilReady = new ArrayBlockingQueue( 1 )
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 300 ] as Dimension, show: true ) {
				btn = button( text: 'Click Me', name: 'the-button' )
			}
			blockUntilReady << true
		}

		assert blockUntilReady.poll( 5, TimeUnit.SECONDS )

		doMove btn

		def mouseLocation = MouseInfo.pointerInfo.location
		def btnLocation = btn.locationOnScreen

		assert mouseLocation.x > btnLocation.x
		assert mouseLocation.x < btnLocation.x + btn.width
		assert mouseLocation.y > btnLocation.y
		assert mouseLocation.y < btnLocation.y + btn.height

	}

	void testClickOn( Closure doClick ) {
		def blockUntilReady = new ArrayBlockingQueue( 1 )
		def buttonClickedFuture = new ArrayBlockingQueue( 1 )
		JMenu mainMenu = null
		JMenuItem itemExit = null

		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 300 ] as Dimension, show: true ) {
				menuBar() {
					mainMenu = menu( name: 'menu-button', text: "File", mnemonic: 'F' ) {
						itemExit = menuItem( name: 'item-exit', text: "Exit", mnemonic: 'X',
								actionPerformed: { buttonClickedFuture.add true } )
					}
				}
			}
			blockUntilReady << true
		}

		assert blockUntilReady.poll( 5, TimeUnit.SECONDS )

		doClick( mainMenu, itemExit )

		// wait up to 2 secs for the button to be clicked
		assert buttonClickedFuture.poll( 2, TimeUnit.SECONDS )

	}

}

abstract class SimpleSwingDriverTest extends SwingBaseForTests {

	Closure withDriver

	@Test
	void testMoveTo_Component( ) {
		testMoveTo { Component c -> withDriver().moveTo c }
	}

	@Test
	void testClickOn_Component( ) {
		testClickOn { Component c1, Component c2 ->
			withDriver().clickOn( c1 )
					.pause( 250 ).clickOn( c2 )
		}
	}

}

abstract class SwingDriverWithSelectorsTest extends SimpleSwingDriverTest {

	@Test
	void testMoveTo_Name( ) {
		testMoveTo { Component c -> withDriver().moveTo( 'the-button' ) }
	}

	@Test
	void testClickOn_Name( ) {
		testClickOn { Component _1, Component _2 ->
			withDriver().clickOn( 'menu-button' )
					.pause( 250 ).clickOn( 'item-exit' )
		}
	}

}

class SwingAutomatonTest extends SimpleSwingDriverTest {

	{ withDriver = { SwingAutomaton.user } }

}

class SwingerTest extends SwingDriverWithSelectorsTest {

	{ withDriver = { Swinger.getUserWith( jFrame ) } }

}