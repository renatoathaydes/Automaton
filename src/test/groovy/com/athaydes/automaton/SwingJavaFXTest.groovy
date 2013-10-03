package com.athaydes.automaton

import groovy.swing.SwingBuilder
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.effect.Reflection
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import javafx.scene.text.Font
import javafx.scene.text.Text
import org.junit.Before
import org.junit.Test

import javax.swing.*
import java.awt.Dimension
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
		createAndRunSwingApp( blockUntilReady )
		assert blockUntilReady.poll( 5, SECONDS )
		println "Gui ready!"
	}

	static void createAndRunSwingApp( blockUntilReady ) {
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
				blockUntilReady << true
			}
		}
	}

	@Test
	void "Automaton should be able to test applications using both Swing and JavaFX"( ) {
		def fx = jfxPanel.scene.&lookup
		use( SwingStringSelector ) {
			SwingStringSelector.jFrame = jFrame
			SwingAutomaton.user.clickOn( 'text-area' )
					.type( 'Hello, I am the Swing Automaton!' ).pause( 1000 )

			FXAutomaton.user.clickOn( fx( '#left-color-picker' ) )
					.pause( 2000 ).moveBy( 60, 40 ).click()
		}

		sleep 4000
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

class JavaFxSampleScene extends Scene {

	Text fxText
	ColorPicker leftPicker
	ColorPicker rightPicker

	JavaFxSampleScene( ) {
		super( new Group(), Color.BLACK )
		println "Creating Scene"
		leftPicker = new ColorPicker( value: Color.CYAN, id: 'left-color-picker' )
		rightPicker = new ColorPicker( value: Color.DODGERBLUE, id: 'right-color-picker' )
		[ leftPicker, rightPicker ].each { it.onAction = colorPickerHandler() }
		def pickers = new HBox( spacing: 10, minHeight: 70, alignment: Pos.CENTER )
		def pickerLabel = new Label( text: 'Text Colors:', textFill: Color.WHITE )
		pickers.children << pickerLabel << leftPicker << rightPicker
		fxText = new Text( x: 40, y: 100, font: new Font( 'Arial', 35 ),
				text: 'This is JavaFX', fill: javaFxCoolTextFill(), effect: new Reflection() )
		def inputText = new TextField( translateX: 75, translateY: 170 )
		(root as Group).children << pickers << fxText << inputText
	}

	private EventHandler<ActionEvent> colorPickerHandler( ) {
		[ handle: { fxText.fill = javaFxCoolTextFill() } ] as EventHandler<ActionEvent>
	}

	private LinearGradient javaFxCoolTextFill( ) {
		new LinearGradient( 0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
				new Stop( 0, leftPicker.value ), new Stop( 1, rightPicker.value ) )
	}

}
