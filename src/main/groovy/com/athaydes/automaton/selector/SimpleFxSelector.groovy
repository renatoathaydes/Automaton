package com.athaydes.automaton.selector

import javafx.scene.Node
import javafx.stage.PopupWindow
import javafx.stage.Window

import static com.athaydes.automaton.SwingUtil.callMethodIfExists

/**
 * @author Renato
 */
abstract class FxSelectorBase extends Closure<List<Node>>
		implements AutomatonSelector<Node> {

	FxSelectorBase() {
		super( new Object() )
	}

	abstract boolean matches( String selector, Node node )

	@Override
	List<Node> call( Object... args ) {
		apply( * toExpectedTypes( args ) )
	}

	protected static toExpectedTypes( Object... args ) {
		assert args.length == 3
		assert args[ 0 ] instanceof String
		assert args[ 1 ] instanceof Node
		assert args[ 2 ] instanceof Integer
		[ null, args[ 0 ] as String, args[ 1 ] as Node, args[ 2 ] as Integer ]
	}

	protected boolean navigateBreadthFirst( Node node, Closure visitor, followPopups = true ) {
		def nextLevel = [ node ]
		while ( nextLevel ) {
			def grandChildren = [ ]
			for ( child in nextLevel ) {
				if ( visitor( child ) ) return true
				grandChildren += subItemsOf( child )
			}
			nextLevel = grandChildren
		}
		if ( followPopups ) {
			for ( popup in getAllPopups() ) {
				def abort = navigateBreadthFirst( popup.scene.root, visitor, false )
				if ( abort ) return true
			}
		}
		return false
	}

	private List<PopupWindow> getAllPopups() {
		def res = [ ]
		def windows = Window.impl_getWindows()

		while ( windows.hasNext() ) {
			Window window = windows.next();
			if ( window instanceof PopupWindow ) res << window
		}
		return res
	}

	private subItemsOf( node ) {
		callMethodIfExists( node, 'getChildrenUnmodifiable' )
	}

}

abstract class SimpleFxSelector extends FxSelectorBase {

	boolean followPopups() { false }

	@Override
	List<Node> apply( String prefix, String selector, Node root, int limit = Integer.MAX_VALUE ) {
		def res = [ ]
		navigateBreadthFirst( root, { Node node ->
			if ( matches( selector, node ) ) res << node
			res.size() >= limit
		}, followPopups() )
		return res
	}

	List<Node> apply( String selector, Node root, int limit = Integer.MAX_VALUE ) {
		apply( null, selector, root, limit )
	}

}
