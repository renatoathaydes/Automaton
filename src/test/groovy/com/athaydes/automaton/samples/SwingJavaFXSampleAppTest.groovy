package com.athaydes.automaton.samples

import com.athaydes.automaton.SwingerFxer
import javafx.embed.swing.JFXPanel
import javafx.scene.control.TextField
import javafx.scene.paint.Color
import javafx.scene.paint.LinearGradient
import javafx.scene.text.Text
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import javax.swing.*
import java.util.concurrent.ArrayBlockingQueue

import static com.athaydes.automaton.SwingUtil.lookup
import static java.util.concurrent.TimeUnit.SECONDS

/**
 *
 * User: Renato
 */
@Ignore( "Not executed as part of Automaton unit tests" )
class SwingJavaFXSampleAppTest {

	JFrame jFrame
	JFXPanel jfxPanel

	@Before
	void setup( ) {
		def blockUntilReady = new ArrayBlockingQueue( 1 )
		def app = new SwingWithFXSample()
		app.createAndRunSwingApp( blockUntilReady )
		assert blockUntilReady.poll( 5, SECONDS )
		jFrame = app.jFrame
		jfxPanel = app.jfxPanel
		println "Gui ready!"
	}

	@After
	void cleanup( ) {
		jFrame?.dispose()
	}

	@Test
	void "Automaton should be able to test applications using both Swing and JavaFX"( ) {
		final swingTextAreaText = "Hello, I am Swing..."
		final fxInputText = "Hello, JavaFX..."

		SwingerFxer.userWith( jFrame, jfxPanel.scene.root )
				.clickOn( "text-area" )
				.type( swingTextAreaText ).pause( 1000 )
				.clickOn( "#left-color-picker" )
				.pause( 1000 ).moveBy( 60, 40 ).click()
				.pause( 1000 ).clickOn( "#fx-input" )
				.type( fxInputText ).moveBy( 100, 0 )
				.pause( 500 )

		assert ( lookup( "text-area", jFrame ) as JTextArea ).text == swingTextAreaText
		assert ( jfxPanel.scene.lookup( "#fx-input" ) as TextField ).text == fxInputText
		assert textLeftColor.toString() == "0x999999ff"
	}

	Color getTextLeftColor( ) {
		def javaFxText = jfxPanel.scene.lookup( "#fx-text" ) as Text
		( javaFxText.fill as LinearGradient ).stops[ 0 ].color
	}

}

