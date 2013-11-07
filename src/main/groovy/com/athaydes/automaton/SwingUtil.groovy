package com.athaydes.automaton

import javax.swing.*
import javax.swing.tree.TreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath
import java.awt.*
import java.util.List

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
		def res = lookupAll( name, root, 1 )
		res ? res[ 0 ] : null
	}

	/**
	 * Breadth-first search for all Components with the given name
	 * @param name of components being searched for
	 * @param root to be looked into
	 * @param limit maximum number of components to be returned
	 * @return all components with the given name or an empty list if none
	 */
	static List<Component> lookupAll( String name, Component root, int limit = Integer.MAX_VALUE ) {
		final List<Component> res = [ ]
		navigateBreadthFirst( root ) { if ( it?.name == name ) res << it; res.size() >= limit }
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
		def res = textAll( textToFind, root, 1 )
		res ? res[ 0 ] : null
	}

	/**
	 * Finds all Components with the given text under the given root.
	 * It also works for <code>JTrees</code> and <code>JTables</code>
	 * @param textToFind
	 * @param root
	 * @param limit maximum number of components to be returned
	 * @return all components with the given text or an empty list if none
	 * @see #text(String, Component)
	 */
	static List<Component> textAll( String textToFind, Component root, int limit = Integer.MAX_VALUE ) {
		final List<Component> res = [ ]
		navigateBreadthFirst( root ) { comp ->
			switch ( comp ) {
				case JTree:
					jTreeText( comp as JTree, textToFind, res, limit )
					break
				case JTable:
					jTableText( comp as JTable, textToFind, res, limit )
					break
				case Component:
					if ( callMethodIfExists( comp, 'getText' ) == textToFind )
						res << ( comp as Component )
			}
			res.size() >= limit
		}
		return res
	}

	private static boolean jTableText( JTable table, String textToFind, List<Component> res, int limit ) {
		navigateBreadthFirst( table ) { data, row, col ->
			if ( data as String == textToFind ) {
				res << ( new FakeComponent( data, {
					row < 0 ?
						table.tableHeader.locationOnScreen :
						table.locationOnScreen
				}, {
					row < 0 ?
						table.tableHeader.getHeaderRect( col ) :
						table.getCellRect( row, col, true )
				} ) as Component )

			}
			res.size() >= limit
		}
	}

	private static boolean jTreeText( tree, String textToFind, List<Component> res, int limit ) {
		navigateBreadthFirst( tree ) { TreeNode node ->
			if ( node as String == textToFind ) {
				res << treeNode2FakeComponent( tree, node )
			}
			res.size() >= limit
		}
	}

	private static Component treeNode2FakeComponent( JTree tree, TreeNode node ) {
		new FakeComponent( node,
				{ tree.locationOnScreen },
				{ tree.getPathBounds( new TreePath( pathOf( node ) ) ) } )
	}

	/**
	 * Breadth-first search for any Component with the given type
	 * @param selector class simple name or qualified name
	 * @param root to be looked into
	 * @return component if found, null otherwise
	 */
	static Component type( String selector, Component root ) {
		def res = typeAll( selector, root, 1 )
		res ? res[ 0 ] : null
	}

	/**
	 * Breadth-first search for all Components with the given type
	 * @param selector class simple name or qualified name
	 * @param root to be looked into
	 * @param limit maximum number of components to be returned
	 * @return all components with the given type or an empty list if none
	 */
	static List<Component> typeAll( String selector, Component root, int limit = Integer.MAX_VALUE ) {
		final isQualified = selector.contains( '.' )
		final List<Component> res = [ ]
		navigateBreadthFirst( root ) { comp ->
			if ( comp.class."${isQualified ? 'name' : 'simpleName'}" == selector )
				res << comp
			res.size() >= limit
		}
		res
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
		def prevTwoLevels = [ ]
		while ( nextLevel ) {
			if ( visit( nextLevel, action ) ) return true
			nextLevel = nextLevel.inject( [ ] ) { acc, c -> acc + subItemsOf( c ) } - prevTwoLevels.flatten()
			prevTwoLevels << nextLevel
			if ( prevTwoLevels.size() > 2 ) prevTwoLevels.remove( 0 )
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
		navigateBreadthFirst( tree.model.root as TreeNode, tree.model, action )
	}

	/**
	 * Navigates the given tree, calling the given action for each node, including the startNode.
	 * To stop navigating, action may return true
	 * @param startNode node to start navigation from
	 * @param model JTree model
	 * @param action to be called on each visited node. Return true to stop navigating.
	 * @return true if action returned true for any node
	 * @see #collectNodes(JTree, String [])
	 */
	static boolean navigateBreadthFirst( TreeNode startNode, TreeModel model, Closure action ) {
		if ( model ) {
			def nextLevel = [ startNode ]
			while ( nextLevel ) {
				if ( visit( nextLevel, action ) ) return true
				nextLevel = nextLevel.collect { node ->
					( 0..<model.getChildCount( node ) ).collect { i ->
						model.getChild( node, i )
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

	/**
	 * @param tree to navigate, collecting each Node as a fake Component
	 * (see <code>{@link FakeComponent}</code>)
	 * @param path to search
	 * @return all nodes corresponding to the given path, or an empty List if the full-path does not exist
	 */
	static List<Component> collectNodes( JTree tree, String... path ) {
		def result = [ ]
		def runningPath = path.toList()
		def parent = tree.model.root as TreeNode
		def foundNode = null

		while ( runningPath ) {

			def target = runningPath.remove( 0 )
			foundNode = null
			navigateBreadthFirst( parent, tree.model ) { TreeNode node ->
				if ( node == parent ) return false
				def onSameLevel = node.parent == parent
				if ( onSameLevel && node as String == target ) foundNode = node
				!onSameLevel || foundNode
			}
			if ( !foundNode ) break
			result << treeNode2FakeComponent( tree, foundNode )
			parent = foundNode
		}
		if ( foundNode ) result
		else [ ]
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

	/**
	 * A fake Component which can be located by any SwingAutomaton.
	 * The TreeNode wrapped by this component can be accessed via the <code>getRealObject</code>.
	 */
	static class FakeComponent extends Component {

		final realObject
		final Closure<Point> parentLocationOnScreen
		final Closure<Rectangle> getItemBounds

		protected FakeComponent( realObject,
		                         Closure<Point> parentLocationOnScreen,
		                         Closure<Rectangle> getItemBounds ) {
			this.realObject = realObject
			this.parentLocationOnScreen = parentLocationOnScreen
			this.getItemBounds = getItemBounds
		}

		def getRealObject( ) { realObject }

		Point getLocationOnScreen( ) {
			def parentLocation = parentLocationOnScreen()
			def bounds = getItemBounds()
			new Point(
					( bounds.location.x + parentLocation.x ) as int,
					( bounds.location.y + parentLocation.y ) as int )
		}

		int getWidth( ) { getItemBounds().width.intValue() }

		int getHeight( ) { getItemBounds().height.intValue() }

	}

}
