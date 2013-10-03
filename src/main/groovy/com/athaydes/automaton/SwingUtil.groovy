package com.athaydes.automaton

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
	static Component lookup( String name, Container root ) {
		Component res = null
		navigateBreadthFirst( root ) { if ( it?.name == name ) res = it }
		return res
	}

	/**
	 * Navigates the tree under the given root, calling the given action for each Component.
	 * To stop navigating, action may return true
	 * @param root of tree to be navigated
	 * @param action to be called on each visited Component. Return true to stop navigating.
	 */
	static boolean navigateBreadthFirst( Container root, Closure<Component> action ) {
		def nextLevel = [ root ]
		while ( nextLevel ) {
			if ( visit( nextLevel, action ) ) return true
			def subItems = [ ]
			nextLevel.each { subItems += subItemsOf( it ) }
			nextLevel = subItems
		}
	}

	private static subItemsOf( component ) {
		( component?.components?.toList() ?: [ ] ) +
				callMethodIfExists( component, 'getContentPane' ) +
				callMethodIfExists( component, 'getMenuComponents' ).toList()
	}

	private static visit( nextLevel, action ) {
		for ( component in nextLevel ) if ( action( component ) ) return true
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
