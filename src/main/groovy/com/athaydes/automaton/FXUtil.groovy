package com.athaydes.automaton

import groovy.transform.CompileStatic
import javafx.scene.Node
import javafx.scene.Parent
import javafx.stage.Window

/**
 * Useful JavaFX functions.
 */
@CompileStatic
class FXUtil {

	/**
	 * Navigates the tree under the given root, calling the given action for each Node.
	 * To stop navigating, the visitor may return true.
	 * @param root of tree to be navigated
	 * @param action to be called on each visited Node. Return true to stop navigating.
	 * @return true if action returned true for any Node
	 */
	static boolean navigateBreadthFirst( Node node, Closure visitor, followPopups = true ) {
		List<Node> nextLevel = [ node ]
		while ( nextLevel ) {
			List<Node> grandChildren = [ ]
			for ( Node child in nextLevel ) {
				if ( visitor( child ) ) return true
				grandChildren += subItemsOf( child )
			}
			nextLevel = grandChildren
		}
		if ( followPopups ) {
			for ( popup in ( getAllPopups() - node.scene.window ) ) {
				def abort = navigateBreadthFirst( popup.scene.root, visitor, false )
				if ( abort ) return true
			}
		}
		return false
	}

	private static List<Window> getAllPopups() {
		def windows = Window.impl_getWindows()
		windows.toList()
	}

	private static List<Node> subItemsOf( Node node ) {
		if ( node instanceof Parent ) {
			node.childrenUnmodifiable
		} else {
			Collections.emptyList()
		}
	}

}
