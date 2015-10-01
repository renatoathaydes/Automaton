package com.athaydes.automaton.selector

import com.athaydes.automaton.SwingUtil
import com.athaydes.automaton.swing.selectors.SwingNavigator

import javax.swing.JComboBox
import javax.swing.JList
import javax.swing.JTabbedPane
import javax.swing.JTable
import javax.swing.JTree
import javax.swing.tree.TreeNode
import java.awt.Component

import static com.athaydes.automaton.SwingUtil.comboBoxItem2FakeComponent
import static com.athaydes.automaton.SwingUtil.listItem2FakeComponent
import static com.athaydes.automaton.SwingUtil.tabbedPane2FakeComponent
import static com.athaydes.automaton.SwingUtil.tableCell2FakeComponent
import static com.athaydes.automaton.SwingUtil.treeNode2FakeComponent

abstract class SwingerSelectorBase extends Closure<List<Component>>
        implements AutomatonSelector<Component> {

    SwingerSelectorBase() {
        super( new Object() )
    }

    @Override
    List<Component> call( Object... args ) {
        apply( *toExpectedTypes( args ) )
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
        JTree tree // remember latest tree visited
        SwingNavigator.navigateBreadthFirst( component ) { comp ->
            boolean stop = false
            switch ( comp ) {
                case JTree:
                    tree = comp as JTree
                    stop = visitor( tree )
                    break
                case TreeNode:
                    def nodeComp = treeNode2FakeComponent( tree, comp )
                    stop = visitor( nodeComp )
                    break
                case JTable:
                    def table = comp as JTable

                    // must visit table with a visitor which takes row and column.
                    // this causes the cells to be visited twice, first here, then by the default navigator
                    stop = SwingUtil.navigateBreadthFirst( table ) { data, int row, int col ->
                        def cell = tableCell2FakeComponent( table, data, row, col )
                        visitor( cell )
                    }
                    break
                case JComboBox:
                    def combo = comp as JComboBox
                    if ( visitor( combo ) ) stop = true
                    else for ( index in 0..<combo.itemCount ) {
                        def itemComp = comboBoxItem2FakeComponent( combo, index )
                        stop = visitor( itemComp )
                        if ( stop ) break
                    }
                    break
                case JTabbedPane:
                    def jtab = comp as JTabbedPane
                    if ( visitor( jtab ) ) stop = true
                    else for ( index in 0..<jtab.tabCount ) {
                        def tabComp = tabbedPane2FakeComponent( jtab, index )
                        stop = visitor( tabComp )
                        if ( stop ) break
                    }
                    break
                case JList:
                    def jlist = comp as JList
                    if ( visitor( jlist ) ) stop = true
                    else for ( index in 0..<jlist.model.size ) {
                        def listComp = listItem2FakeComponent( jlist, index )
                        stop = visitor( listComp )
                        if ( stop ) break
                    }
                    break
                default:
                    try {
                        stop = visitor( comp )
                    } catch ( MissingMethodException e ) {
                        // closure could not handle parameter type
                    }
            }
            return stop
        }
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

