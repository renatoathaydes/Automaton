package com.athaydes.automaton.samples

import groovy.swing.SwingBuilder
import javafx.application.Platform
import javafx.embed.swing.JFXPanel

import javax.swing.*
import java.awt.*
import java.util.concurrent.ArrayBlockingQueue

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE

/**
 *
 * User: Renato
 */
class SwingWithFXSample {

	JFrame jFrame
	JFXPanel jfxPanel

	void createAndRunSwingApp( ArrayBlockingQueue blockUntilReady ) {
		new SwingBuilder().edt {
			jfxPanel = new JFXPanel()
			jFrame = frame( title: 'Swing Frame', size: [ 600, 500 ] as Dimension, show: false,
					defaultCloseOperation: DISPOSE_ON_CLOSE ) {
				menuBar() {
					menu( name: 'menu-button', text: "File", mnemonic: 'F' ) {
						menuItem( name: 'item-exit', text: "Exit", mnemonic: 'X', actionPerformed: { dispose() } )
					}
				}
				splitPane( name: 'pane1' ) {
					scrollPane( name: 'pane1-1', constraints: "left", minimumSize: [ 250, -1 ] as Dimension ) {
						tree( name: 'mboxTree', rootVisible: false )
					}
					splitPane( name: 'pane1-2', orientation: JSplitPane.VERTICAL_SPLIT, dividerLocation: 180 ) {
						scrollPane( name: 'pane1-2a', constraints: "top" ) {
							textArea( name: 'text-area', editable: true )
						}
						scrollPane( name: 'pane1-2b', constraints: "top" ) {
							widget jfxPanel
						}
					}
				}
			}
			jFrame.visible = true
			Platform.runLater {
				jfxPanel.scene = new JavaFxSampleScene()
				if ( blockUntilReady != null ) blockUntilReady << true
			}
		}
	}

	static main( String[] args ) {
		new SwingWithFXSample().createAndRunSwingApp( null )
	}

}
