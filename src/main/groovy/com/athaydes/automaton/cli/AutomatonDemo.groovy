package com.athaydes.automaton.cli

import groovy.swing.SwingBuilder

import javax.swing.*
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import java.awt.*

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE

/**
 * @author Renato
 */
@Singleton
class AutomatonDemo {

	enum DemoOption {
		SWING, JAVAFX
	}

	void runDemo( String option ) {
		def demoOption = optionFor( option ?: 'swing' )
		if ( !demoOption ) return
		switch ( demoOption ) {
			case DemoOption.SWING: swingDemo()
				break
			case DemoOption.JAVAFX: javaFxDemo()
		}
	}

	void swingDemo() {
		JTree theTree
		JLabel statusLabel
		JTextArea outputLabel
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
							def scriptPane = null
							label( name: 'script-label', text: 'Enter an Automaton script:',
									toolTipText: 'My name is script-label' )
							scrollPane( name: 'pane1-2a', constraints: "top", minimumSize: [ -1, 200 ] as Dimension ) {
								scriptPane = textPane( name: 'text-area', editable: true, text: initialScript(),
										toolTipText: 'My name is text-area',
										font: new Font( 'monospaced', Font.PLAIN, 12 ) )
							}
							runButton = button( name: 'run-button', text: 'Run Automaton script',
									toolTipText: 'My name is run-button',
									actionPerformed: {
										runButton.setEnabled( false )
										runScript( scriptPane.text, outputLabel ) {
											runButton.setEnabled( true )
										}
									} )
							scrollPane {
								outputLabel = textArea( name: 'output-label', editable: false,
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

	void javaFxDemo() {
		println "JavaFX demo"
	}

	DemoOption optionFor( String option ) {
		try {
			return DemoOption.valueOf( option.toUpperCase() )
		} catch ( IllegalArgumentException ignored ) {
			println "ERROR: Unknown demo option: $option"
			println "Valid options are ${DemoOption.values()*.name()*.toLowerCase()}"
			return null
		}
	}

	void runScript( String text, def output, Closure onCompletion ) {
		def writer = [ write: { s -> output.text += s } ]
		Thread.start {
			AutomatonScriptRunner.instance.runScript( text, writer )
			onCompletion.run()
		}
	}

	String initialScript() {
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
