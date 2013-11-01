package com.athaydes.automaton

import com.athaydes.automaton.geometry.PointOperators

import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
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
	 * It works also for JTrees (the returned Component is a fake with the same location
	 * as the found TreeNode, if any, so that it can be used with SwingAutomaton methods)
	 * @param textToFind
	 * @param root
	 * @return item found or null
	 */
	static Component text( String textToFind, Component root ) {
		Component res = null
		navigateBreadthFirst( root ) { comp ->
			switch ( comp ) {
				case JTree:
					navigateBreadthFirst( comp as JTree ) { node ->
						if ( node as String == textToFind )
							res = fakeComponentFor( node, comp )
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
	 * @param node to be converted into a Component for location purposes
	 * @return a fake Component for the given node which can be used with any SwingAutomaton
	 * method (eg. <code>clickOn( fakeComponentFor( treeNode ) )</code> )
	 */
	static Component fakeComponentFor( DefaultMutableTreeNode node, JTree tree ) {
		def path = new TreePath( node.path )
		Rectangle nodeBounds = tree.getPathBounds( path )
		def parentAbsLocation = tree.locationOnScreen
		use( PointOperators ) {
			[ getLocationOnScreen: nodeBounds.location + parentAbsLocation,
					getWidth: nodeBounds.width,
					getHeight: nodeBounds.height ] as Component
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

	private static subItemsOf( component ) {
		( component?.components?.toList() ?: [ ] ) +
				callMethodIfExists( component, 'getContentPane' ) +
				callMethodIfExists( component, 'getMenuComponents' ).toList()
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
