package com.athaydes.automaton.cli.demo

import groovy.swing.SwingBuilder

import javax.swing.*
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import java.awt.*

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE

class SwingDemo extends Demo {

	void start() {
		JTree theTree
		JLabel statusLabel
		JTextArea outputTextArea
		JButton runButton

		new SwingBuilder().edt {
			frame( title: 'Automaton Demo - Swing', size: [ 600, 500 ] as Dimension,
					location: [ 200, 150 ] as Point, show: true,
					defaultCloseOperation: DISPOSE_ON_CLOSE ) {
				menuBar() {
					menu( name: 'menu-button', text: "File", mnemonic: 'F',
							toolTipText: 'My name is menu-button' ) {
						menuItem( name: 'item-exit', text: "Exit", mnemonic: 'X', actionPerformed: { dispose() } )
					}
				}
				splitPane( name: 'pane1' ) {
					scrollPane( name: 'pane1-1', constraints: "left", minimumSize: [ 150, -1 ] as Dimension ) {
						theTree = tree( name: 'box-tree', rootVisible: false,
								toolTipText: 'My name is box-tree' )
					}
					splitPane( name: 'pane1-2', orientation: JSplitPane.VERTICAL_SPLIT, dividerLocation: 120 ) {
						statusLabel = label( name: 'status-label', text: 'You have not selected a tree node yet',
								font: new Font( 'arial', Font.ITALIC | Font.BOLD, 14 ),
								toolTipText: 'My name is status-label' )
						vbox() {
							JTextPane scriptPane = null
							label( name: 'script-label', text: 'Enter an Automaton script:',
									toolTipText: 'My name is script-label' )
							scrollPane( name: 'pane1-2a', constraints: "top", minimumSize: [ -1, 200 ] as Dimension ) {
								scriptPane = textPane( name: 'text-area', editable: true, text: initialScript(),
										toolTipText: 'My name is text-area',
										font: new Font( 'monospaced', Font.PLAIN, 12 ) )
							}
							hbox {
								runButton = button( name: 'run-button', text: 'Run Automaton script',
										toolTipText: 'My name is run-button',
										actionPerformed: {
											runButton.setEnabled( false )
											runScript( scriptPane.text, outputTextArea ) {
												runButton.setEnabled( true )
											}
										} )
								button( name: 'clear-output', text: 'Clear output',
										toolTipText: 'My name is clear-output',
										actionPerformed: {
											outputTextArea.text = ''
										} )
							}
							scrollPane {
								outputTextArea = textArea( name: 'output-label', editable: false,
										toolTipText: 'My name is output-label' )
							}
						}

					}
				}
			}
			theTree.addTreeSelectionListener( new TreeSelectionListener() {
				@Override
				void valueChanged( TreeSelectionEvent e ) {
					statusLabel.text = "You selected " + e.newLeadSelectionPath.path.tail()
				}
			} )
		}
	}

	static String initialScript() {
		'''\
		|doubleClickOn 'text:colors'
		|pause 500
		|clickOn 'text:yellow'
		|
		|assertThat swinger[ 'status-label' ],
		|           hasText( 'You selected [colors, yellow]' )
		|
		|println "Test passed OK"
		|'''.stripMargin()
	}

}
