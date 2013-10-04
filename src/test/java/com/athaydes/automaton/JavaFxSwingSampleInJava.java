package com.athaydes.automaton;

import javafx.embed.swing.JFXPanel;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;

/**
 * User: Renato
 */
public class JavaFxSwingSampleInJava {

	SwingJavaFXTest swingJavaFx = new SwingJavaFXTest();

	@Before
	public void setup() throws Exception {
		swingJavaFx.setup();
	}

	@Test
	public void javaCodeCanAlsoDriveSwingJavaFxApps() {
		JFrame frame = swingJavaFx.getjFrame();
		JFXPanel fxPanel = swingJavaFx.getJfxPanel();

		Swinger.userWith( frame ).clickOn( "text-area" )
				.type( "Hello, I am the Swing Automaton!" ).pause( 1000 );

		FXer.userWith( fxPanel.getScene().getRoot() )
				.clickOn( "#left-color-picker" )
				.pause( 2000 ).moveBy( 60, 40 ).click()
				.pause( 2000 ).clickOn( "#fx-input" )
				.type( "Running in Java!" ).moveBy( 100, 0 )
				.pause( 2000 );
	}

}
