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

	protected static FakeComponent tableCell2FakeComponent( JTable table, data, int row, int col ) {
		new FakeComponent( data, {
			row < 0 ?
					table.tableHeader.locationOnScreen :
					table.locationOnScreen
		}, {
			row < 0 ?
					table.tableHeader.getHeaderRect( col ) :
					table.getCellRect( row, col, true )
		} )
	}

	protected static Component comboBoxItem2FakeComponent( JComboBox combo, int index ) {
		def item = combo.getItemAt( index )
		def getList = { combo.ui.getAccessibleChild( combo, 0 ).list as JList }
		new FakeComponent( item,
				{ getList().locationOnScreen },
				{
					new Rectangle( getList().indexToLocation( index ),
							[ getList().width, ( getList().height / combo.itemCount ).intValue() ] as Dimension )
				} )
	}

	protected static Component treeNode2FakeComponent( JTree tree, TreeNode node ) {
		new FakeComponent( node,
				{ tree.locationOnScreen },
				{ tree.getPathBounds( new TreePath( pathOf( node ) ) ) } )
	}

    protected static Component tabbedPane2FakeComponent( JTabbedPane tabbedPane, int index ) {
        new FakeComponent(
                tabbedPane.getTitleAt( index ),
                { tabbedPane.locationOnScreen },
                { tabbedPane.getBoundsAt( index ) }
        )
    }

    protected static Component listItem2FakeComponent( JList list, int index ) {
        def item = list.model.getElementAt( index )
        def listComp = list.cellRenderer.getListCellRendererComponent( list, item, index, false, false )
		def listItemText
		if ( listComp instanceof JLabel ) {
			// the default renderer will return JLabel components - get text from that
			listItemText = listComp.text
		} else {
			// if the default renderer was not used, attempt to get the text from the item's toString
			listItemText = item.toString()
		}
        new FakeComponent( listItemText,
                { list.locationOnScreen },
                {
					list.ui.getCellBounds(list, index, index)
                } )
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

	/**
	 * Navigates the given table, calling the given action for each header and cell.
	 * To stop navigating, action may return true
	 * @param table to navigate through
	 * @param action to be called on each visited header/cell. Return true to stop navigating.
	 * @return true if action returned true for any header/cell
	 */
	static boolean navigateBreadthFirst( JTable table, Closure action ) {
		def cols = ( 0..<table.model.columnCount )
		def rows = ( 0..<table.model.rowCount )
		for ( col in cols ) {
			if ( action( table.model.getColumnName( col ), -1, col ) ) return true
		}
		for ( row in rows ) {
			for ( col in cols ) {
				if ( action( getRenderedTableValue(table, row, col ), row, col ) ) return true
			}
		}
		return false
	}

	/**
	 * Returns the text as rendered by the table cell's renderer component, if the renderer component
	 * has a getText() method. Returns the model value at the cell's position otherwise.
	 * @param table in question
	 * @param row of the value to get
	 * @param col of the value to get
	 * @return The rendered value or the model value if the renderer doesn't have a getText() method
	 */
	private static getRenderedTableValue(JTable table, int row, int col) {
		def value = table.model.getValueAt(row, col)
		def rendererComp = table.getCellRenderer(row, col)
				.getTableCellRendererComponent(table, value, false, false, row, col)
		def text = callMethodIfExists(rendererComp, 'getText')
		return text ?: value
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
		def methods = object?.metaClass?.respondsTo( object, methodName, args )
		if ( methods ) {
			try {
				return methods.first().invoke( object, args )
			} catch ( ignored ) {}
		}
		return [ ]
	}

	/**
	 * @return the bounds of the default screen
	 */
	static Rectangle defaultScreenBounds() {
		GraphicsEnvironment ge = GraphicsEnvironment.localGraphicsEnvironment
		ge.defaultScreenDevice.defaultConfiguration.bounds
	}

}

/**
 * A fake Component which can be located by any SwingAutomaton.
 * The TreeNode wrapped by this component can be accessed via the <code>getRealObject</code>.
 */
class FakeComponent extends Component {

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

    def getRealObject() { realObject }

    Point getLocationOnScreen() {
        def parentLocation = parentLocationOnScreen()
        def bounds = getItemBounds()
        try {
            new Point(
                    ( bounds.location.x + parentLocation.x ) as int,
                    ( bounds.location.y + parentLocation.y ) as int )
        } catch ( e ) {
            throw new GuiItemNotFound( "Component likely not visible on screen: $realObject", e )
        }
    }

    int getWidth() { getItemBounds().width.intValue() }

    int getHeight() { getItemBounds().height.intValue() }

    String getText() { realObject as String }

    def methodMissing( String name, def args ) {
        realObject."name"( args )
    }
}