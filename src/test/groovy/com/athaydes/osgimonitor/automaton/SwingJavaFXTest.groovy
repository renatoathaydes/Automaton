package com.athaydes.osgimonitor.automaton

import com.athaydes.automaton.SwingAutomaton
import com.athaydes.automaton.SwingUtil
import groovy.swing.SwingBuilder
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.effect.Reflection
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import javafx.scene.text.Font
import javafx.scene.text.Text
import org.junit.Before
import org.junit.Test

import javax.swing.*
import java.util.concurrent.ArrayBlockingQueue

import static java.util.concurrent.TimeUnit.SECONDS
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE

/**
 *
 * User: Renato
 */
class SwingJavaFXTest {

	JFrame jFrame
	JFXPanel jfxPanel

	@Before
	void setup( ) {
		def blockUntilReady = new ArrayBlockingQueue( 1 )
		new SwingBuilder().edt {
			jfxPanel = new JFXPanel()
			jFrame = frame( title: 'Frame', size: [ 600, 500 ], show: false,
					defaultCloseOperation: DISPOSE_ON_CLOSE ) {
				menuBar() {
					menu( name: 'menu-button', text: "File", mnemonic: 'F' ) {
						menuItem( name: 'item-exit', text: "Exit", mnemonic: 'X', actionPerformed: { dispose() } )
					}
				}
				splitPane( name: 'pane1' ) {
					scrollPane( name: 'pane1-1', constraints: "left", minimumSize: [ 250, -1 ] ) {
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
				jfxPanel.scene = createScene()
				blockUntilReady << true
			}
		}
		assert blockUntilReady.poll( 5, SECONDS )
		println "Gui ready!"
	}

	@Test
	void "Automaton should be able to test applications using both Swing and JavaFX"( ) {
		use( SwingStringSelector ) {
			SwingStringSelector.jFrame = jFrame
			SwingAutomaton.user.clickOn( 'text-area' )
					.type( 'Hello, I am the Swing Automaton!' )
		}

		sleep 4000
	}

	private static Scene createScene( ) {
		println "Creating Scene"
		def root = new Group()
		def scene = new Scene( root, Color.BLACK )
		Text text = new Text( x: 40, y: 100, font: new Font( 'Arial', 35 ),
				text: 'This is JavaFX', fill: new LinearGradient( 0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
				new Stop( 0, Color.CYAN ), new Stop( 1, Color.DODGERBLUE )
		), effect: new Reflection() )
		root.children << text
		scene
	}

	@Category( SwingAutomaton )
	class SwingStringSelector {
		static JFrame jFrame

		SwingAutomaton clickOn( String name ) {
			this.clickOn SwingUtil.lookup( name, jFrame )
			this
		}
	}

	static main( String[] args ) {
		new SwingJavaFXTest().setup()
	}

}
