package com.athaydes.automaton

import com.athaydes.automaton.mixins.SwingTestHelper
import com.athaydes.automaton.mixins.TimeAware
import groovy.swing.SwingBuilder
import org.junit.After
import org.junit.Test

import javax.swing.*
import java.awt.*
import java.awt.event.MouseEvent
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

import static com.google.code.tempusfugit.temporal.Duration.seconds
import static com.google.code.tempusfugit.temporal.Timeout.timeout
import static com.google.code.tempusfugit.temporal.WaitFor.waitOrTimeout

/**
 *
 * User: Renato
 */
@Mixin( [ SwingTestHelper, TimeAware ] )
abstract class SwingBaseForTests implements HasSwingCode {

	JFrame jFrame

	@After
	void cleanup( ) {
		jFrame?.dispose()
	}

	void testMoveTo( Closure doMove ) {
		JButton btn = null
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 300 ] as Dimension, show: true ) {
				btn = button( text: 'Click Me', name: 'the-button' )
			}
		}

		waitForJFrameToShowUp()

		doMove btn

		def mouseLocation = MouseInfo.pointerInfo.location
		def btnLocation = btn.locationOnScreen

		assert mouseLocation.x > btnLocation.x
		assert mouseLocation.x < btnLocation.x + btn.width
		assert mouseLocation.y > btnLocation.y
		assert mouseLocation.y < btnLocation.y + btn.height

	}

	void testClickOn( Closure doClick ) {
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
		}

		waitForJFrameToShowUp()

		doClick( mainMenu, itemExit )

		// wait up to 2 secs for the button to be clicked
		assert buttonClickedFuture.poll( 2, TimeUnit.SECONDS )

	}

	void testDoubleClickOn( Closure doDoubleClick ) {
		def future = new LinkedBlockingDeque<MouseEvent>( 2 )
		JButton btn
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 50, 100 ] as Dimension, show: true ) {
				btn = button( text: 'Click Me', name: 'the-button',
						mouseClicked: { MouseEvent e -> future.add e } )
			}
		}

		waitForJFrameToShowUp()

		doDoubleClick( btn )

		// wait up to 2 secs for the button to be clicked
		2.times{
			assert future.poll( 2, TimeUnit.SECONDS )
		}

	}

	void testDragFromTo( Closure doDragFromTo ) {
		def e1 = null
		def e2 = null
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', location: [ 250, 50 ] as Point,
					size: [ 190, 200 ] as Dimension, show: true ) {
				panel( layout: null ) {
					e1 = editorPane( location: [ 20, 50 ] as Point,
							size: [ 50, 20 ] as Dimension,
							name: 'e1', text: 'abcdefghijklmnopqrstuvxzwy',
							editable: false, dragEnabled: true )
					e2 = editorPane( location: [ 100, 50 ] as Point,
							size: [ 50, 20 ] as Dimension,
							name: 'e2',
							editable: true, dragEnabled: true )
				}
			}
		}

		waitForJFrameToShowUp()

		doDragFromTo( e1, e2 )

		waitOrTimeout condition { e1.text == e2.text }, timeout( seconds( 2 ) )
	}

	@Override
	JFrame getJFrame( ) { jFrame }

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

	@Test
	void testDoubleClickOn_Component( ) {
		testDoubleClickOn { Component c ->
			withDriver().doubleClickOn( c )
		}
	}

	@Test
	void testDragFromTo_Components( ) {
		testDragFromTo( { Component c1, Component c2 ->
			withDriver().clickOn( c1 ).clickOn( c1 ).drag( c1 ).onto( c2 )
		} )
	}

	@Test
	void testDragFromTo_FromComponentToPosition( ) {
		testDragFromTo( { Component c1, Component c2 ->
			def c2p = SwingAutomaton.centerOf( c2 )
			withDriver().clickOn( c1 ).clickOn( c1 ).drag( c1 ).onto( c2p.x, c2p.y )
		} )
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

	@Test
	void testDoubleClickOn_Name( ) {
		testDoubleClickOn { Component c ->
			withDriver().doubleClickOn( 'the-button' )
		}
	}

	@Test
	void testDragFromTo_Names( ) {
		testDragFromTo( { Component c1, Component c2 ->
			withDriver().clickOn( 'e1' ).clickOn( 'e1' ).drag( 'e1' ).onto( 'e2' )
		} )
	}

	@Test
	void testDragFromTo_FromNameToPosition( ) {
		testDragFromTo( { Component c1, Component c2 ->
			def c2p = SwingAutomaton.centerOf( c2 )
			withDriver().clickOn( 'e1' ).clickOn( 'e1' ).drag( 'e1' ).onto( c2p.x, c2p.y )
		} )
	}

}

class SwingAutomatonTest extends SimpleSwingDriverTest {

	{ withDriver = { SwingAutomaton.user } }

}

class SwingerTest extends SwingDriverWithSelectorsTest {

	{ withDriver = { Swinger.getUserWith( jFrame ) } }

}