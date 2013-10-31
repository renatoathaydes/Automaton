package com.athaydes.automaton

import com.athaydes.automaton.mixins.SwingTestHelper
import groovy.swing.SwingBuilder
import org.junit.Test

import javax.swing.*
import java.awt.*

/**
 *
 * User: Renato
 */
@Mixin( SwingTestHelper )
class SwingUtilTest implements HasSwingCode {

	JFrame jFrame

	JFrame getJFrame( ) { jFrame }

	@Test
	void testNavigateBreadthFirstComponentsWholeTree( ) {

		def empty = { name -> [ getComponents: { [ ] as Component[] }, toString: { name } ] as Container }
		def c1a = empty 'c1a'
		def c2_1a = empty 'c2_1a'
		def c2_1b = empty 'c2_1b'
		def c2cp1 = empty 'c2cp1'
		def c2cp = [ getComponents: { [ c2cp1 ] as Component[] }, toString: { 'c2cp' } ] as Container
		def c2_1 = [ getMenuComponents: { [ c2_1a, c2_1b ] as Component[] }, toString: { 'c2_1' } ] as Container
		def c1 = [ getComponents: { [ c1a ] as Component[] }, toString: { 'c1' } ] as Container
		def c2 = [
				getContentPane: { c2cp }, getComponents: { [ c2_1 ] as Component[] }, toString: { 'c2' }
		] as Container
		def root = [ getComponents: { [ c1, c2 ] as Component[] }, toString: { 'root' } ] as Container

		def visited = [ ]
		def action = { Component c -> visited += c; false }

		def res = SwingUtil.navigateBreadthFirst root, action

		assert visited == [ root, c1, c2, c1a, c2_1, c2cp, c2_1a, c2_1b, c2cp1 ]
		assert !res // action never returned true
	}

	@Test
	void testNavigateBreadthFirstComponentsPartialTree( ) {

		def empty = { name -> [ getComponents: { [ ] as Component[] }, toString: { name } ] as Container }
		def c1 = empty 'c1'
		def c2 = empty 'c2'
		def root = [ getComponents: { [ c1, c2 ] as Component[] }, toString: { 'root' } ] as Container

		def visited = [ ]
		def action = { Component c -> visited += c; c.toString() == 'c1' }

		def res = SwingUtil.navigateBreadthFirst root, action

		assert visited == [ root, c1 ]
		assert res
	}

	@Test
	void testNavigateBreadthFirstJTreesWholeTree( ) {
		JTree mTree = null
		new SwingBuilder().edt {
			frame( title: 'Frame', size: [ 300, 300 ] as Dimension, show: false ) {
				mTree = tree( rootVisible: false )
			}
		}
		sleep 100

		def visited = [ ]
		def res = SwingUtil.navigateBreadthFirst( mTree ) {
			visited << it
			false
		}
		assert visited.collect { it as String } == [ mTree.model.root as String,
				'colors', 'sports', 'food',
				'blue', 'violet', 'red', 'yellow',
				'basketball', 'soccer', 'football', 'hockey',
				'hot dogs', 'pizza', 'ravioli', 'bananas' ]
		assert !res // action never returned true
	}

	@Test
	void testNavigateBreadthFirstJTreesPartialTree( ) {
		JTree mTree = null
		new SwingBuilder().edt {
			frame( title: 'Frame', size: [ 300, 300 ] as Dimension, show: false ) {
				mTree = tree( rootVisible: false )
			}
		}
		sleep 100

		def visited = [ ]
		def res = SwingUtil.navigateBreadthFirst( mTree ) {
			visited << it
			it.toString() == 'blue'
		}

		assert visited.collect { it as String } == [ mTree.model.root as String,
				'colors', 'sports', 'food', 'blue' ]
		assert res
	}

	@Test
	void testLookup( ) {
		JTree mboxTree = null
		def pane1 = null, pane1_1 = null, pane1_2 = null,
		    pane1_2a = null, pane1_2b = null, menuButton = null,
		    itemExit = null

		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 300 ] as Dimension, show: false ) {
				menuBar() {
					menuButton = menu( name: 'menu-button', text: "File", mnemonic: 'F' ) {
						itemExit = menuItem( name: 'item-exit', text: "Exit", mnemonic: 'X', actionPerformed: { dispose() } )
					}
				}
				pane1 = splitPane( name: 'pane1' ) {
					pane1_1 = scrollPane( name: 'pane1-1', constraints: "left",
							preferredSize: [ 160, -1 ] as Dimension ) {
						mboxTree = tree( name: 'mboxTree', rootVisible: false )
					}
					pane1_2 = splitPane( name: 'pane1-2', orientation: JSplitPane.VERTICAL_SPLIT, dividerLocation: 180 ) {
						pane1_2a = scrollPane( name: 'pane1-2a', constraints: "top" ) { table() }
						pane1_2b = scrollPane( name: 'pane1-2b', constraints: "bottom" ) { textArea() }
					}
				}
			}
		}

		sleep 100

		assert menuButton == SwingUtil.lookup( 'menu-button', jFrame )
		assert itemExit == SwingUtil.lookup( 'item-exit', jFrame )
		assert mboxTree == SwingUtil.lookup( 'mboxTree', jFrame )
		assert pane1 == SwingUtil.lookup( 'pane1', jFrame )
		assert pane1_1 == SwingUtil.lookup( 'pane1-1', pane1 )
		assert pane1_2 == SwingUtil.lookup( 'pane1-2', pane1 )
		assert pane1_2a == SwingUtil.lookup( 'pane1-2a', jFrame )
		assert pane1_2b == SwingUtil.lookup( 'pane1-2b', pane1_2 )
	}

	@Test
	void testCallMethodIfExists( ) {
		assert 'HI' == SwingUtil.callMethodIfExists( 'hi', 'toUpperCase' )
		assert [ ] == SwingUtil.callMethodIfExists( 'hi', 'nonExistentMethod' )
		def list = [ 1 ]
		assert SwingUtil.callMethodIfExists( list, 'add', 2 ) // returns true
		assert list == [ 1, 2 ]
		assert SwingUtil.callMethodIfExists( list, 'addAll', [ 3, 4 ] ) // returns true
		assert list == [ 1, 2, 3, 4 ]
		assert null == SwingUtil.callMethodIfExists( list, 'add', 0, 5 ) // returns void / null
		assert list == [ 5, 1, 2, 3, 4 ]
		assert [ [ 5, 1 ], [ 3, 4 ] ] == SwingUtil.callMethodIfExists( list, 'collate', 2, 3, false )
		assert null == SwingUtil.callMethodIfExists( list, 'clear' ) // returns void / null
		assert list.isEmpty()
	}

	@Test
	void testText( ) {
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 300 ] as Dimension, show: false ) {
				menuBar() {
					menu( text: "File", mnemonic: 'F' ) {
						menuItem( text: "Exit", mnemonic: 'X', actionPerformed: { dispose() } )
					}
				}
				splitPane {
					scrollPane( constraints: "left", minimumSize: [ 150, -1 ] as Dimension ) {
						panel() {
							label( text: 'A tree' )
							tree( rootVisible: false )
						}
					}
					splitPane( orientation: JSplitPane.VERTICAL_SPLIT, dividerLocation: 180 ) {
						scrollPane( constraints: "top" ) { button( text: 'Click' ) }
						scrollPane( constraints: "bottom" ) { textArea() }
					}
				}
			}
		}

		sleep 100

		assert SwingUtil.text( 'File', jFrame )
		assert SwingUtil.text( 'Exit', jFrame )
		assert SwingUtil.text( 'A tree', jFrame )
		assert SwingUtil.text( 'Click', jFrame )
		assert SwingUtil.text( 'colors', jFrame )
	}

}
