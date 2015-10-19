package com.athaydes.automaton.selector

import com.athaydes.automaton.FXUtil
import javafx.scene.Node

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
		apply( *toExpectedTypes( args ) )
	}

	protected static toExpectedTypes( Object... args ) {
		assert args.length == 3
		assert args[ 0 ] instanceof String
		assert args[ 1 ] instanceof Node
		assert args[ 2 ] instanceof Integer
		[ null, args[ 0 ] as String, args[ 1 ] as Node, args[ 2 ] as Integer ]
	}

}

abstract class SimpleFxSelector extends FxSelectorBase {

	boolean followPopups() { false }

	@Override
	List<Node> apply( String prefix, String selector, Node root, int limit = Integer.MAX_VALUE ) {
		def res = [ ]
		FXUtil.navigateBreadthFirst( root, { Node node ->
			if ( matches( selector, node ) ) res << node
			res.size() >= limit
		}, followPopups() )
		return res
	}

	List<Node> apply( String selector, Node root, int limit = Integer.MAX_VALUE ) {
		apply( null, selector, root, limit )
	}

}

abstract class CompositeFxSelector extends SimpleFxSelector {

	final List<MapEntry> selectors_queries

	CompositeFxSelector( List<MapEntry> selectors_queries ) {
		this.selectors_queries = selectors_queries
	}

}

class IntersectFxSelector extends CompositeFxSelector {

	IntersectFxSelector( List<MapEntry> selectors_queries ) {
		super( selectors_queries )
	}

	@Override
	boolean matches( String query, Node node ) {
		getSelectors_queries().every { it.key.matches( it.value, node ) }
	}

}

class UnionFxSelector extends CompositeFxSelector {

	UnionFxSelector( List<MapEntry> selectors_queries ) {
		super( selectors_queries )
	}

	@Override
	boolean matches( String query, Node node ) {
		getSelectors_queries().any { it.key.matches( it.value, node ) }
	}

}

