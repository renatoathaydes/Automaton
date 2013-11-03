package com.athaydes.automaton

import com.athaydes.automaton.mixins.SwingTestHelper
import com.athaydes.automaton.mixins.TimeAware
import groovy.swing.SwingBuilder
import org.junit.After
import org.junit.Test

import javax.swing.*
import java.awt.*
import java.awt.event.KeyEvent
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
		JButton btn = null
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 50, 100 ] as Dimension, show: true ) {
				btn = button( text: 'Click Me', name: 'the-button',
						mouseClicked: { MouseEvent e -> future.add e } )
			}
		}

		waitForJFrameToShowUp()

		doDoubleClick( btn )

		// wait up to 2 secs for the button to be clicked
		2.times {
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
							name: 'e1', text: 'abcdefg',
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
	void testMoveTo_Text( ) {
		testMoveTo { Component c -> withDriver().moveTo( 'text:Click Me' ) }
	}

	@Test
	void testMoveTo_Type( ) {
		testMoveTo { Component c -> withDriver().moveTo( 'type:JButton' ) }
	}

	@Test
	void testMoveTo_JTreeNode( ) {
		JTree mTree = null
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 200, 200 ] as Dimension,
					location: [ 150, 50 ] as Point, show: true ) {
				splitPane( name: 'pane1' ) {
					splitPane( orientation: JSplitPane.VERTICAL_SPLIT, dividerLocation: 50 ) {
						button( text: 'Click Me', name: 'the-button' )
						mTree = tree( name: 'mboxTree', rootVisible: false )
					}
				}
			}
		}

		waitForJFrameToShowUp()

		def outsideFrame = new Point( 100, 25 )
		withDriver().moveTo( outsideFrame.x, outsideFrame.y, Speed.VERY_FAST )
				.moveTo( 'text:colors' )
		def currPos = MouseInfo.pointerInfo.location
		final screenBounds = new Rectangle( jFrame.locationOnScreen, jFrame.size )

		assert screenBounds.contains( currPos )
	}

	@Test
	void testMoveTo_TableCell( ) {
		def tModel = [
				[ firstCol: '1 - 1', secCol: '1 - 2' ],
				[ firstCol: '2 - 1', secCol: '2 - 2' ],
		]
		JTable jTable = null
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 200 ] as Dimension,
					location: [ 150, 50 ] as Point, show: true ) {
				scrollPane {
					jTable = table {
						tableModel( list: tModel ) {
							propertyColumn( header: 'Col 1', propertyName: 'firstCol' )
							propertyColumn( header: 'Col 2', propertyName: 'secCol' )
						}
					}
				}
			}
		}
		waitForJFrameToShowUp()

		withDriver().moveTo( 'text:1 - 1' )
				.doubleClick().doubleClick()
				.type( 'row 1 col 1' )
				.moveTo( 'text:1 - 2' )
				.doubleClick().doubleClick()
				.type( 'row 1 col 2' )
				.moveTo( 'text:2 - 1' )
				.doubleClick().doubleClick()
				.type( 'row 2 col 1' )
				.moveTo( 'text:2 - 2' )
				.doubleClick().doubleClick()
				.type( 'row 2 col 2' )
				.type( KeyEvent.VK_ENTER ).pause( 500 )

		assert jTable.model.getValueAt( 0, 0 ) == 'row 1 col 1'
		assert jTable.model.getValueAt( 0, 1 ) == 'row 1 col 2'
		assert jTable.model.getValueAt( 1, 0 ) == 'row 2 col 1'
		assert jTable.model.getValueAt( 1, 1 ) == 'row 2 col 2'

	}

	@Test
	void testMoveTo_TableHeader( ) {
		def tModel = [
				[ firstCol: '1 - 1', secCol: '1 - 2' ],
		]
		JTable jTable = null
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 200 ] as Dimension,
					location: [ 150, 50 ] as Point, show: true ) {
				vbox {
					label( text: 'There is a table below me' )
					scrollPane {
						jTable = table {
							tableModel( list: tModel ) {
								propertyColumn( header: 'Col 1', propertyName: 'firstCol' )
								propertyColumn( header: 'Col 2', propertyName: 'secCol' )
							}
						}
					}
				}

			}
		}
		waitForJFrameToShowUp()

		withDriver().drag( 'text:Col 1' )
				.onto( 'text:Col 2' ).pause( 500 )

		assert jTable.tableHeader.columnModel.getColumn( 0 ).headerValue == 'Col 2'
		assert jTable.tableHeader.columnModel.getColumn( 1 ).headerValue == 'Col 1'
	}

	@Test
	void testClickOn_Name( ) {
		testClickOn { Component _1, Component _2 ->
			withDriver().clickOn( 'menu-button' )
					.pause( 250 ).clickOn( 'item-exit' )
		}
	}

	@Test
	void testClickOn_Text( ) {
		testClickOn { Component _1, Component _2 ->
			withDriver().clickOn( 'text:File' )
					.pause( 250 ).clickOn( 'text:Exit' )
		}
	}

	@Test
	void testClickOn_Type( ) {
		testClickOn { Component _1, Component _2 ->
			withDriver().clickOn( 'type:JMenu' )
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
	void testDoubleClickOn_Text( ) {
		testDoubleClickOn { Component c ->
			withDriver().doubleClickOn( 'text:Click Me' )
		}
	}

	@Test
	void testDoubleClickOn_Type( ) {
		testDoubleClickOn { Component c ->
			withDriver().doubleClickOn( 'type:JButton' )
		}
	}

	@Test
	void testDragFromTo_Names( ) {
		testDragFromTo( { Component c1, Component c2 ->
			withDriver().doubleClickOn( 'e1' ).drag( 'e1' ).onto( 'e2' )
		} )
	}

	@Test
	void testDragFromTo_Texts( ) {
		testDragFromTo( { Component c1, Component c2 ->
			withDriver().doubleClickOn( 'text:abcdefg' ).drag( 'text:abcdefg' ).onto( 'e2' )
		} )
	}

	@Test
	void testDragFromTo_FromNameToPosition( ) {
		testDragFromTo( { Component c1, Component c2 ->
			def c2p = SwingAutomaton.centerOf( c2 )
			withDriver().doubleClickOn( 'e1' ).drag( 'e1' ).onto( c2p.x, c2p.y )
		} )
	}

	@Test
	void testDragFromTo_Type( ) {
		testDragFromTo( { Component c1, Component c2 ->
			withDriver().doubleClickOn( 'type:JEditorPane' ).drag( 'text:abcdefg' ).onto( 'e2' )
		} )
	}

}

class SwingAutomatonTest extends SimpleSwingDriverTest {

	{ withDriver = { SwingAutomaton.user } }

}

class SwingerTest extends SwingDriverWithSelectorsTest {

	{ withDriver = { Swinger.getUserWith( jFrame ) } }

	@Test
	void shouldUseMapToFindPrefixedNames( ) {
		def abcCalls = [ ]
		def efghCalls = [ ]
		def driver = withDriver()
		driver.specialPrefixes = [
				'abc:': { argForAbc, _ -> abcCalls << argForAbc },
				'efgh:': { argForEfgh, _ -> efghCalls << argForEfgh } ]
		driver.delegate = [ clickOn: { c, Speed _ -> } ]

		driver.clickOn( 'efgh:4567890123' )

		assert abcCalls == [ ]
		assert efghCalls == [ '4567890123' ]

		driver.clickOn( 'abc:123' )

		assert abcCalls == [ '123' ]
		assert efghCalls == [ '4567890123' ]
	}

	@Test
	void shouldUseFirstEntryInMapOnUnprefixedNames( ) {
		def abcCalls = [ ]
		def efghCalls = [ ]
		def driver = withDriver()
		driver.specialPrefixes = [
				'abc:': { argForAbc, _ -> abcCalls << argForAbc },
				'efgh:': { argForEfgh, _ -> efghCalls << argForEfgh } ]
		driver.delegate = [ clickOn: { c, Speed _ -> } ]

		assert 'abc:' == driver.specialPrefixes.keySet().first()

		driver.clickOn( '123' )

		assert abcCalls == [ '123' ]
		assert efghCalls == [ ]
	}

}

class SwingerFXer_SwingTest extends SwingDriverWithSelectorsTest {

	{ withDriver = { SwingerFxer.userWith( jFrame, null ) } }

}
