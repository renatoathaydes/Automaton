package com.athaydes.automaton.selector

import com.athaydes.automaton.HasSwingCode
import com.athaydes.automaton.mixins.SwingTestHelper
import groovy.swing.SwingBuilder
import spock.lang.Specification

import javax.swing.*
import java.awt.*

/**
 * @author Renato
 */
@Mixin( SwingTestHelper )
class SwingerSelectorTest extends Specification implements HasSwingCode {

	JFrame jFrame

	JFrame getJFrame( ) { jFrame }

	def testByName( ) {
		given:
		JTree mboxTree = null
		def pane1 = null, pane1_1 = null, pane1_2 = null,
		    pane1_2a = null, pane1_2b = null, menuButton = null,
		    itemExit = null, tabs = null, tab1 = null, tab2 = null,
		    tab1Lbl = null, tab2Lbl = null, tabLbl1 = null, tabLbl2 = null

		and:
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 300 ] as Dimension, show: false ) {
				menuBar() {
					menuButton = menu( name: 'menu-button', text: "File", mnemonic: 'F' ) {
						itemExit = menuItem( name: 'item-exit', text: "Exit", mnemonic: 'X', actionPerformed: { dispose() } )
					}
				}
				pane1 = splitPane( name: 'pane1' ) {
					pane1_1 = scrollPane( name: 'pane1-1', constraints: "left",
							preferredSize: [ 160, -1 ] as Dimension ) {
						mboxTree = tree( name: 'mboxTree', rootVisible: false )
					}
					pane1_2 = splitPane( name: 'pane1-2', orientation: JSplitPane.VERTICAL_SPLIT, dividerLocation: 180 ) {
						pane1_2a = scrollPane( name: 'pane1-2a', constraints: "top" ) { table() }
						pane1_2b = scrollPane( name: 'pane1-2b', constraints: "bottom" ) { textArea() }
					}
				}
				tabs = tabbedPane( name: 'tabs' ) {
					tab1 = panel( name: 'tab-1' ) {
						tab1Lbl = label( name: 'tab-1-label', text: 'One' )
						tabLbl1 = label( name: 'tab-label', text: 'Three' )
					}
					tab2 = panel( name: 'tab-2' ) {
						tab2Lbl = label( name: 'tab-2-label', text: 'Two' )
						tabLbl2 = label( name: 'tab-label', text: 'Four' )
					}
				}
			}
		}

		def selector = SwingerSelectors.byName()

		expect:
		selector.apply( 'menu-button', jFrame ) == [ menuButton ]
		selector.apply( 'item-exit', jFrame ) == [ itemExit ]
		selector.apply( 'mboxTree', jFrame ) == [ mboxTree ]
		selector.apply( 'pane1', jFrame ) == [ pane1 ]
		selector.apply( 'pane1-1', pane1 ) == [ pane1_1 ]
		selector.apply( 'pane1-2', pane1 ) == [ pane1_2 ]
		selector.apply( 'pane1-2a', jFrame ) == [ pane1_2a ]
		selector.apply( 'pane1-2b', pane1_2 ) == [ pane1_2b ]
		selector.apply( 'tabs', jFrame ) == [ tabs ]
		selector.apply( 'tab-1', jFrame ) == [ tab1 ]
		selector.apply( 'tab-2', jFrame ) == [ tab2 ]
		selector.apply( 'tab-1-label', jFrame ) == [ tab1Lbl ]
		selector.apply( 'tab-2-label', jFrame ) == [ tab2Lbl ]
		selector.apply( 'tab-label', jFrame ) as Set == [ tabLbl1, tabLbl2 ] as Set
		selector.apply( 'tab-label', jFrame, 1 ).size() == 1
		selector.apply( 'tab-label', jFrame, 1 ).any { it == tabLbl1 || it == tabLbl2 }
	}

	def testByTextWithUniqueText( ) {
		given:
		def tModel = [ [ firstCol: 'item 1 - Col 1', secCol: true ] ]
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 400, 300 ] as Dimension, show: false ) {
				menuBar() {
					menu( text: "File", mnemonic: 'F' ) {
						menuItem( text: "Exit", mnemonic: 'X', actionPerformed: { dispose() } )
					}
				}
				hbox {
					scrollPane( constraints: "left" ) {
						vbox {
							label( text: 'A tree' )
							tree( rootVisible: false )
							scrollPane {
								table {
									tableModel( list: tModel ) {
										propertyColumn( header: 'Col 1', propertyName: 'firstCol', type: String )
										propertyColumn( header: 'Col 2', propertyName: 'secCol', type: Boolean )
									}
								}
							}
							comboBox( items: [ 'combo1', 'combo2' ] )
						}
					}
					splitPane( orientation: JSplitPane.VERTICAL_SPLIT, dividerLocation: 180 ) {
						scrollPane( constraints: "top" ) { button( text: 'Click' ) }
						scrollPane( constraints: "bottom" ) { textArea() }
					}
				}
			}
		}

		sleep 100

		expect:
		!SwingerSelectors.byText().apply( textToFind, jFrame, 1 ).empty

		where:
		textToFind << [ 'File', 'Exit', 'A tree', 'Click', 'colors', 'Col 1', 'item 1 - Col 1', 'combo1', 'combo2' ]
	}

	def testByTextWithRepeatingText( ) {
		given:
		def menu1 = null; def menuItem2 = null; def label2 = null; def text2 = null

		and:
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 400, 300 ] as Dimension, show: false ) {
				menuBar() {
					menu1 = menu( text: "target1", mnemonic: 'F' ) {
						menuItem2 = menuItem( name: 'target2', text: "target2", mnemonic: 'X', actionPerformed: { dispose() } )
					}
				}
				vbox() {
					label2 = label( text: 'target2' )
					text2 = textField( text: 'target2' )
				}
			}
		}

		def selector = SwingerSelectors.byText()

		expect:
		selector.apply( 'target1', jFrame ) == [ menu1 ]
		selector.apply( 'target1', jFrame, 1 ) == [ menu1 ]
		selector.apply( 'target1', jFrame, 2 ) == [ menu1 ]
		selector.apply( 'target2', jFrame, 1 ).size() == 1
		selector.apply( 'target2', jFrame ) as Set == [ menuItem2, label2, text2 ] as Set
	}

	def testByTypeWithUniqueTypes( ) {
		given:
		new SwingBuilder().edt {
			jFrame = frame( name: 'frame', title: 'Frame', size: [ 300, 300 ] as Dimension,
					location: [ 150, 50 ] as Point, show: false ) {
				vbox( name: 'vbox' ) {
					hbox( name: 'hbox' ) {
						label( name: 'label', text: 'lbl' )
						textArea( name: 'text-area', text: 'txt-area' )
					}
				}
			}
		}

		expect:
		SwingerSelectors.byType().apply( typeToFind, jFrame )*.name == ( expectedName ? [ expectedName ] : [ ] )

		where:
		typeToFind              | expectedName
		'JFrame'                | 'frame'
		'javax.swing.JFrame'    | 'frame'
		'JLabel'                | 'label'
		'javax.swing.JLabel'    | 'label'
		'JTextArea'             | 'text-area'
		'javax.swing.JTextArea' | 'text-area'
		'JTable'                | null
		'javax.swing.JTable'    | null
		'Non existing class'    | null
	}

	def testByTypeWithRepeatingTypes( ) {
		given:
		new SwingBuilder().edt {
			jFrame = frame( name: 'frame', title: 'Frame', size: [ 300, 300 ] as Dimension,
					location: [ 150, 50 ] as Point, show: false ) {
				vbox( name: 'A' ) {
					hbox( name: 'B' ) {
						label( name: 'label', text: 'lbl' )
						textArea( name: 'text-area', text: 'txt-area' )
					}
				}
			}
		}

		def selector = SwingerSelectors.byType()

		expect:
		selector.apply( 'Box', jFrame, 1 ).size() == 1
		selector.apply( 'Box', jFrame, 2 )*.name as Set == [ 'A', 'B' ] as Set
		selector.apply( 'Box', jFrame )*.name as Set == [ 'A', 'B' ] as Set
		selector.apply( 'JLabel', jFrame )*.name == [ 'label' ]
	}

}
