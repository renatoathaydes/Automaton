package com.athaydes.automaton.samples;

import com.athaydes.automaton.SwingerFxer;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TextField;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;

import static com.athaydes.automaton.SwingUtil.lookup;
import static junit.framework.Assert.assertEquals;

/**
 * User: Renato
 */
@Ignore( "Cannot run during normal tests because Swing-embedded JavaFX App can" +
		" only be launched once in a JVM" )
public class SwingJavaFXSampleAppTestInJava {

	SwingJavaFXSampleAppTest swingJavaFx = new SwingJavaFXSampleAppTest();

	@Before
	public void setup() throws Exception {
		swingJavaFx.setup();
	}

	@After
	public void cleanup() {
		swingJavaFx.cleanup();
	}

	@Test
	public void javaCodeCanAlsoDriveSwingJavaFxApps() {
		JFrame frame = swingJavaFx.getjFrame();
		JFXPanel fxPanel = swingJavaFx.getJfxPanel();
		String swingTextAreaText = "Hello, I am Swing...";
		String fxInputText = "Hello, JavaFX...";

		SwingerFxer.userWith( frame, fxPanel.getScene().getRoot() )
				.clickOn( "text-area" )
				.type( swingTextAreaText ).pause( 1000 )
				.clickOn( "#left-color-picker" )
				.pause( 1000 ).moveBy( 60, 40 ).click()
				.pause( 1000 ).clickOn( "#fx-input" )
				.type( fxInputText ).moveBy( 100, 0 )
				.pause( 500 );

		JTextArea jTextArea = ( JTextArea ) lookup( "text-area", frame );
		TextField textField = ( TextField ) fxPanel.getScene().lookup( "#fx-input" );

		assertEquals( swingTextAreaText, jTextArea.getText() );
		assertEquals( fxInputText, textField.getText() );
		assertEquals( "0x999999ff", swingJavaFx.getTextLeftColor().toString() );
	}

}
