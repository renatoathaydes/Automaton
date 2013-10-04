package com.athaydes.automaton

import com.athaydes.automaton.samples.SwingJavaFXSampleAppTest
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

/**
 *
 * User: Renato
 */
@Ignore( "Cannot run sample app during unit tests as JavaFX breaks if more than 1 app is launched" )
class SwingerFxerTest {

	SwingJavaFXSampleAppTest swingFx

	@Before
	void setup( ) {
		swingFx = new SwingJavaFXSampleAppTest()
		swingFx.setup()
	}

	@After
	void cleanup( ) {
		swingFx.cleanup()
	}

	@Test
	void "SwingerFxer can run mixed Swing/JavaFX apps"( ) {
		SwingerFxer.userWith( swingFx.jFrame, swingFx.jfxPanel.scene.root )
				.clickOn( 'text-area' )
				.type( 'Hello, I am the Swing Automaton!' ).pause( 1000 )
				.clickOn( '#left-color-picker' )
				.pause( 2000 ).moveBy( 60, 40 ).click()
				.pause( 2000 ).clickOn( '#fx-input' )
				.type( 'Running in Groovy!' ).moveBy( 100, 0 )
				.pause( 2000 )
	}

}
