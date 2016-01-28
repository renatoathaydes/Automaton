package com.athaydes.automaton

import com.athaydes.automaton.mixins.SwingTestHelper
import groovy.swing.SwingBuilder
import spock.lang.Specification
import spock.lang.Unroll

import javax.swing.JFrame
import javax.swing.JTabbedPane
import javax.swing.JTable
import javax.swing.JTree
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableColumn
import java.awt.Component
import java.awt.Dimension
import java.awt.Point
import java.awt.Rectangle

/**
 *
 * User: Renato
 */
@Mixin( SwingTestHelper )
class SwingUtilTest extends Specification implements HasSwingCode {

    JFrame jFrame

    JFrame getJFrame() { jFrame }

    def testNavigateBreadthFirstJTableWholeTree() {
        given:
        def tModel = [
                [ firstCol: 'item 1 - Col 1', secCol: 'item 1 - Col 2' ],
                [ firstCol: 'item 2 - Col 1', secCol: 'item 2 - Col 2' ],
                [ firstCol: 'item 3 - Col 1', secCol: 'item 3 - Col 2' ]
        ]

        and:
        JTable jTable = null
        new SwingBuilder().build {
            jFrame = frame( title: 'Frame', size: [ 300, 300 ] as Dimension,
                    location: [ 150, 50 ] as Point, show: false ) {
                scrollPane {
                    jTable = table {
                        tableModel( list: tModel ) {
                            propertyColumn( header: 'Col 1', propertyName: 'firstCol' )
                            propertyColumn( header: 'Col 2', propertyName: 'secCol' )
                        }
                    }
                }
            }
        }

        and:
        def visited = [ ]

        when:
        def res = SwingUtil.navigateBreadthFirst( jTable ) { item, row, col ->
            def value = row < 0 ?
                    (item as TableColumn).headerValue :
                    (item as DefaultTableCellRenderer).text
            visited << [ value, row, col ]
            false
        }

        then:
        visited == [
                [ 'Col 1', -1, 0 ], [ 'Col 2', -1, 1 ],
                [ 'item 1 - Col 1', 0, 0 ], [ 'item 2 - Col 1', 1, 0 ], [ 'item 3 - Col 1', 2, 0 ],
                [ 'item 1 - Col 2', 0, 1 ], [ 'item 2 - Col 2', 1, 1 ], [ 'item 3 - Col 2', 2, 1 ]
        ]
        !res // action never returned true
    }

    def testNavigateBreadthFirstJTablePartialTree() {
        given:
        def tModel = [
                [ firstCol: 'item 1 - Col 1' ],
                [ firstCol: 'item 2 - Col 1' ],
        ]

        and:
        JTable jTable = null
        new SwingBuilder().build {
            jFrame = frame( title: 'Frame', size: [ 300, 300 ] as Dimension,
                    location: [ 150, 50 ] as Point, show: false ) {
                scrollPane {
                    jTable = table {
                        tableModel( list: tModel ) {
                            propertyColumn( header: 'Col 1', propertyName: 'firstCol' )
                        }
                    }
                }
            }
        }

        and:
        def visited = [ ]

        when:
        def res = SwingUtil.navigateBreadthFirst( jTable ) { item, row, col ->
            def value = row < 0 ?
                    (item as TableColumn).headerValue :
                    (item as DefaultTableCellRenderer).text
            visited << [ value, row, col ]
            value == 'item 1 - Col 1'
        }

        then:
        visited == [
                [ 'Col 1', -1, 0 ],
                [ 'item 1 - Col 1', 0, 0 ]
        ]
        res
    }

    @Unroll
    def testCollectNodes() {
        given:
        JTree mTree = null
        new SwingBuilder().build {
            frame( title: 'Frame', size: [ 300, 300 ] as Dimension, show: false ) {
                mTree = tree( rootVisible: false )
            }
        }

        when:
        def result = SwingUtil.collectNodes( mTree, path as String[] ).collect { it as String }

        then:
        result.empty == resultShouldBeEmpty
        if ( !result.empty ) result == path

        where:
        path                   | resultShouldBeEmpty
        [ ]                    | true
        [ 'hi' ]               | true
        [ '1', '2', '3' ]      | true
        [ 'blue' ]             | true
        [ 'soccer' ]           | true
        [ 'pizza' ]            | true
        [ 'colors', 'soccer' ] | true
        [ 'sports', 'red' ]    | true
        [ 'colors' ]           | false
        [ 'sports' ]           | false
        [ 'food' ]             | false
        [ 'colors', 'blue' ]   | false
        [ 'colors', 'red' ]    | false
        [ 'sports', 'soccer' ] | false
        [ 'food', 'pizza' ]    | false
    }

    def testFakeComponentForTreeNode() {
        given:
        def swingObject = 'SWING'
        def bounds = new Rectangle( 5, 6, 7, 8 )
        def parentAbsLocation = new Point( 20, 30 )

        when:
        Component component = new FakeComponent( swingObject, { parentAbsLocation }, { bounds } )

        then:
        component.getRealObject() == swingObject
        component.locationOnScreen == new Point( 25, 36 )
        component.width == 7
        component.height == 8
    }

    def testFakeComponentForTabbedPane() {
        given:
        def tabIndex = 3
        def tabbedPane = Mock( JTabbedPane )
        tabbedPane.locationOnScreen >> new Point( 20, 30 )
        tabbedPane.getBoundsAt( tabIndex ) >> new Rectangle( 1, 2, 3, 4 )
        tabbedPane.getTitleAt( tabIndex ) >> 'TABBEDPANE'

        when:
        Component component = SwingUtil.tabbedPane2FakeComponent( tabbedPane, tabIndex )

        then:
        component.text == 'TABBEDPANE'
        component.locationOnScreen == new Point( 21, 32 )
        component.width == 3
        component.height == 4
    }

}
