package com.athaydes.automaton.selector

import javax.swing.*
import javax.swing.tree.TreeNode
import java.awt.*
import java.util.List

import static com.athaydes.automaton.SwingUtil.*

abstract class SwingerSelectorBase extends Closure<List<Component>>
		implements AutomatonSelector<Component> {

	SwingerSelectorBase() {
		super( new Object() )
	}

	@Override
	List<Component> call( Object... args ) {
		apply( * toExpectedTypes( args ) )
	}

	protected static toExpectedTypes( Object... args ) {
		assert args.length == 3
		assert args[ 0 ] instanceof String
		assert args[ 1 ] instanceof Component
		assert args[ 2 ] instanceof Integer
		[ null, args[ 0 ] as String, args[ 1 ] as Component, args[ 2 ] as Integer ]
	}

	abstract List<Component> apply( String prefix, String selector, Component component, int limit )

	protected boolean navigateEveryThing( Component component, Closure visitor ) {
		def stop = navigateBreadthFirst( component ) { Component comp ->
			def stop = visitor( comp )

			if ( !stop )
				switch ( comp ) {
					case JTree:
						def tree = comp as JTree
						navigateBreadthFirst( tree ) { TreeNode node ->
							def nodeComp = treeNode2FakeComponent( tree, node )
							stop = visitor( nodeComp )
						}
						break
					case JTable:
						def table = comp as JTable
						navigateBreadthFirst( table ) { data, int row, int col ->
							def cell = tableCell2FakeComponent( table, data, row, col )
							stop = visitor( cell )
						}
						break
					case JComboBox:
						def combo = comp as JComboBox
						for ( index in 0..<combo.itemCount ) {
							def itemComp = comboBoxItem2FakeComponent( combo, index )
							stop = visitor( itemComp )
						}
						break
				}
			return stop
		}

		if ( !stop ) {
			return visitSubWindows( callMethodIfExists( component, 'getOwnedWindows' ) as Window[], visitor )
		}
		return stop
	}

	protected boolean visitSubWindows( Window[] subWindows, Closure visitor ) {
		for ( w in subWindows ) {
			if ( w instanceof Component ) {
				def stop = navigateEveryThing( w, visitor )
				if ( stop ) return stop
			}
		}
		return false
	}

}

abstract class SimpleSwingerSelector extends SwingerSelectorBase {

	@Override
	List<Component> apply( String prefix, String selector, Component component, int limit = Integer.MAX_VALUE ) {
		final List<Component> res = [ ]
		navigateEveryThing( component ) { Component comp ->
			if ( matches( selector, comp ) )
				res << comp
			res.size() >= limit
		}
		return res
	}

	List<Component> apply( String selector, Component component, int limit = Integer.MAX_VALUE ) {
		apply( null, selector, component, limit )
	}

	abstract boolean matches( String selector, Component component )

}

abstract class CompositeSwingerSelector extends SimpleSwingerSelector {

	final List<MapEntry> selectors_queries

	CompositeSwingerSelector( List<MapEntry> selectors_queries ) {
		this.selectors_queries = selectors_queries
	}

}

class IntersectSwingerSelector extends CompositeSwingerSelector {

	IntersectSwingerSelector( List<MapEntry> selectors_queries ) {
		super( selectors_queries )
	}

	@Override
	boolean matches( String query, Component component ) {
		getSelectors_queries().every { it.key.matches( it.value, component ) }
	}

}

class UnionSwingerSelector extends CompositeSwingerSelector {

	UnionSwingerSelector( List<MapEntry> selectors_queries ) {
		super( selectors_queries )
	}

	@Override
	boolean matches( String query, Component component ) {
		getSelectors_queries().any { it.key.matches( it.value, component ) }
	}

}

