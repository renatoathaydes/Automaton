package com.athaydes.automaton

import com.athaydes.automaton.swing.selectors.SwingNavigator

import javax.swing.JComboBox
import javax.swing.JList
import javax.swing.JTabbedPane
import javax.swing.JTable
import javax.swing.JTree
import javax.swing.table.TableColumn
import javax.swing.tree.TreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath
import java.awt.Component
import java.awt.Dimension
import java.awt.GraphicsEnvironment
import java.awt.Point
import java.awt.Rectangle

/**
 * Utility functions for testing Swing UIs.
 */
class SwingUtil {

    static FakeComponent tableCell2FakeComponent( JTable table, data, int row, int col ) {
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

    static Component comboBoxItem2FakeComponent( JComboBox combo, int index ) {
        def item = combo.getItemAt( index )
        def getList = { combo.ui.getAccessibleChild( combo, 0 ).list as JList }
        new FakeComponent( combo,
                { getList().locationOnScreen },
                {
                    new Rectangle( getList().indexToLocation( index ),
                            [ getList().width, ( getList().height / combo.itemCount ).intValue() ] as Dimension )
                }, item )
    }

    static Component treeNode2FakeComponent( JTree tree, TreeNode node ) {
        new FakeComponent( node,
                { tree.locationOnScreen },
                { tree.getPathBounds( new TreePath( pathOf( node ) ) ) } )
    }

    static Component tabbedPane2FakeComponent( JTabbedPane tabbedPane, int index ) {
        def title = tabbedPane.getTitleAt( index )
        new FakeComponent(
                tabbedPane,
                { tabbedPane.locationOnScreen },
                { tabbedPane.getBoundsAt( index ) },
                title )
    }

    static Component listItem2FakeComponent( JList list, int index ) {
        def item = list.model.getElementAt( index )
        def listComp = list.cellRenderer.getListCellRendererComponent( list, item, index, false, false )

        // if the renderer component has a getText method - the default renderer returns a JLabel component) - get text
        // from that or attempt to get the text from the item's toString
        def listItemText = ReflectionHelper.callMethodIfExists( listComp, 'getText' ) ?: item.toString()

        new FakeComponent( listComp,
                { list.locationOnScreen },
                { list.ui.getCellBounds( list, index, index ) },
                listItemText )
    }

    /**
     * Navigates the tree under the given root, calling the given action for each Component.
     * To stop navigating, action may return true
     * @param root of tree to be navigated
     * @param action to be called on each visited Component. Return true to stop navigating.
     * @return true if action returned true for any Component
     */
    static boolean navigateBreadthFirst( Component root, Closure action ) {
        SwingNavigator.navigateBreadthFirst( root, action )
    }

    /**
     * Navigates the given tree, calling the given action for each node, including the root.
     * To stop navigating, action may return true
     * @param tree to be navigated
     * @param action to be called on each visited node. Return true to stop navigating.
     * @return true if action returned true for any node
     */
    static boolean navigateBreadthFirst( JTree tree, Closure action ) {
        SwingNavigator.visitTree( tree, action )
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
        return SwingNavigator.visitTreeNode( startNode, model, action )
    }

    /**
     * Visits the given table, calling the given action for each header and cell.
     *
     * The visited components are:
     * <ul>
     *     <li>headers: the actual TableColumns (row index is -1)</li>
     *     <li>cells: the TableCellRendererComponent of each cell (row and column indexes start at 0)</li>
     * </ul>
     *
     * To stop navigating, action may return true
     * @param table to navigate through
     * @param action to be called on each visited header/cell. Return true to stop navigating.
     * May take 1 argument (the cell being visited), 2, or 3 (row, column).
     * @return true if action returned true for any header/cell
     */
    static boolean navigateBreadthFirst( JTable table, Closure action ) {
        SwingNavigator.visitTable( table, action )
    }

    /**
     * Returns the text as rendered by the table cell's renderer component, if the renderer component
     * has a getText() method. Returns the model value at the cell's position otherwise.
     * @param table in question
     * @param row of the value to get
     * @param col of the value to get
     * @return The rendered value or the model value if the renderer doesn't have a getText() method
     */
    static getRenderedTableCellValue( JTable table, int row, int col ) {
        def value = table.model.getValueAt( row, col )
        def rendererComp = table.getCellRenderer( row, col )
                .getTableCellRendererComponent( table, value, false, false, row, col )
        def text = ReflectionHelper.callMethodIfExists( rendererComp, 'getText' )
        return text ?: value
    }

    /**
     * Returns the text as rendered by the table header's renderer component, if the renderer component
     * has a getText() method. Returns the header value otherwise.
     * @param table in question
     * @param column in question
     * @param col index of column
     * @return The rendered value or the model value if the renderer doesn't have a getText() method
     */
    static getRenderedTableHeaderValue( JTable table, TableColumn column, int col ) {
        def value = column.headerValue
        if ( column.headerRenderer ) { // default renderer for headers is null!
            def rendererComp = column.headerRenderer.getTableCellRendererComponent( table, value, false, false, -1, col )
            def text = ReflectionHelper.callMethodIfExists( rendererComp, 'getText' )
            return text ?: value
        } else {
            return value
        }
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

    private static Object[] pathOf( TreeNode node ) {
        def path = [ ]
        def parent = node
        while ( parent ) {
            path << parent
            parent = parent.parent
        }
        path.reverse()
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
    final dataValue

    protected FakeComponent( realObject,
                             Closure<Point> parentLocationOnScreen,
                             Closure<Rectangle> getItemBounds,
                             dataValue = null ) {
        this.realObject = realObject
        this.parentLocationOnScreen = parentLocationOnScreen
        this.getItemBounds = getItemBounds
        this.dataValue = dataValue
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

    String getText() {
        if ( dataValue ) {
            return dataValue as String
        }
        def text = ReflectionHelper.callMethodIfExists( realObject, 'getText' )
        if ( text ) {
            return text
        }
        realObject as String
    }

    def methodMissing( String name, Object... args ) {
        realObject."$name"( *args )
    }
}