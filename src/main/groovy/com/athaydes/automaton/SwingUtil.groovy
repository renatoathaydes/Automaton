package com.athaydes.automaton

import com.athaydes.automaton.geometry.PointOperators

import javax.swing.*
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath
import java.awt.*

/**
 *
 * User: Renato
 */
class SwingUtil {

	/**
	 * Breadth-first search for any Component with the given name
	 * @param name of component being searched for
	 * @param root to be looked into
	 * @return component if found, null otherwise
	 */
	static Component lookup( String name, Component root ) {
		Component res = null
		navigateBreadthFirst( root ) { it?.name == name ? res = it : false }
		return res
	}

	/**
	 * Finds any Component with the given text under the given root.
	 * It also works for <code>JTrees</code> and <code>JTables</code>
	 * (the returned Component is a fake with the same location
	 * as the found TreeNode or Table cell/header, if any, so that it
	 * can be used with <code>SwingAutomaton</code> methods)
	 * @param textToFind
	 * @param root
	 * @return item found or null
	 */
	static Component text( String textToFind, Component root ) {
		Component res = null
		navigateBreadthFirst( root ) { comp ->
			switch ( comp ) {
				case JTree:
					def tree = comp as JTree
					navigateBreadthFirst( tree ) { TreeNode node ->
						if ( node as String == textToFind ) {
							def bounds = tree.getPathBounds(
									new TreePath( pathOf( node ) ) )
							res = fakeComponentFor( tree.locationOnScreen, bounds )
						}
						res != null
					}
					break
				case JTable:
					def table = comp as JTable
					navigateBreadthFirst( table ) { data, row, col ->
						if ( data as String == textToFind ) {
							def bounds = ( row < 0 ?
								table.tableHeader.getHeaderRect( col ) :
								table.getCellRect( row, col, true ) )
							res = fakeComponentFor( row < 0 ?
								table.tableHeader.locationOnScreen :
								table.locationOnScreen, bounds )
						}
						res != null
					}
					break
				case Component:
					if ( callMethodIfExists( comp, 'getText' ) == textToFind )
						res = comp as Component
			}
			res != null
		}
		return res
	}

	/**
	 * Breadth-first search for any Component with the given type
	 * @param selector class simple name or qualified name
	 * @param root to be looked into
	 * @return component if found, null otherwise
	 */
	static Component type( String selector, Component root ) {
		def isQualified = selector.contains( '.' )
		Component res = null
		navigateBreadthFirst( root ) { comp ->
			if ( comp.class."${isQualified ? 'name' : 'simpleName'}" == selector )
				res = comp
			res != null
		}
		res
	}

	/**
	 * @param parentAbsLocation parent Component's absolute position
	 * @param bounds of the item for which a fake Component is required
	 * @return a fake Component which can be located by any SwingAutomaton
	 * method (eg. <code>clickOn( fakeComponentFor( tree.locationOnScreen, node.bounds ) )</code> )
	 */
	static Component fakeComponentFor( Point parentAbsLocation, Rectangle bounds ) {
		use( PointOperators ) {
			[ getLocationOnScreen: bounds.location + parentAbsLocation,
					getWidth: bounds.width,
					getHeight: bounds.height ] as Component
		}
	}

	/**
	 * Navigates the tree under the given root, calling the given action for each Component.
	 * To stop navigating, action may return true
	 * @param root of tree to be navigated
	 * @param action to be called on each visited Component. Return true to stop navigating.
	 * @return true if action returned true for any Component
	 */
	static boolean navigateBreadthFirst( Component root, Closure action ) {
		def nextLevel = [ root ]
		while ( nextLevel ) {
			if ( visit( nextLevel, action ) ) return true
			def subItems = [ ]
			nextLevel.each { subItems += subItemsOf( it ) }
			nextLevel = subItems
		}
		return false
	}

	/**
	 * Navigates the given tree, calling the given action for each node, including the root.
	 * To stop navigating, action may return true
	 * @param tree to be navigated
	 * @param action to be called on each visited node. Return true to stop navigating.
	 * @return true if action returned true for any node
	 */
	static boolean navigateBreadthFirst( JTree tree, Closure action ) {
		if ( tree.model ) {
			def nextLevel = [ tree.model.root ]
			while ( nextLevel ) {
				if ( visit( nextLevel, action ) ) return true
				nextLevel = nextLevel.collect { node ->
					( 0..<tree.model.getChildCount( node ) ).collect { i ->
						tree.model.getChild( node, i )
					}
				}.flatten()
			}
		}
		return false
	}

	static boolean navigateBreadthFirst( JTable table, Closure action ) {
		def cols = ( 0..<table.model.columnCount )
		def rows = ( 0..<table.model.rowCount )
		for ( col in cols ) {
			if ( action( table.model.getColumnName( col ), -1, col ) ) return true
		}
		for ( row in rows ) {
			for ( col in cols ) {
				if ( action( table.model.getValueAt( row, col ), row, col ) ) return true
			}
		}
		return false
	}

	private static subItemsOf( component ) {
		( component?.components?.toList() ?: [ ] ) +
				callMethodIfExists( component, 'getContentPane' ) +
				callMethodIfExists( component, 'getMenuComponents' ).toList()
	}

	private static Object[] pathOf( TreeNode node ) {
		def path = [ ]
		def parent = node
		while ( parent ) {
			path << parent
			parent = parent.parent
		}
		path.reverse()
	}

	private static visit( nextLevel, action ) {
		for ( item in nextLevel ) if ( action( item ) ) return true
		return false
	}

	/**
	 * Calls a method on the given Object with the given arguments
	 * @param object to call method on
	 * @param methodName to be called
	 * @param args argument to be passed to the method
	 * @return value returned by the method call, or the empty list if the method does not exist
	 */
	static callMethodIfExists( object, String methodName, Object... args ) {
		if ( object?.metaClass?.respondsTo( object, methodName ) )
			object."$methodName"( * args )
		else [ ]
	}

}
