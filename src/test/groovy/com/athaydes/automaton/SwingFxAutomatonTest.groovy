package com.athaydes.automaton

import org.junit.Before
import org.junit.Test

/**
 *
 * User: Renato
 */
class SwingFxAutomatonTest {

	def swingFx = new SwingJavaFXTest();

	@Before
	public void setup( ) {
		swingFx.setup()
	}

	@Test
	public void shouldBeAbleToUseSwingSelectors( ) {
		//Swinger.userWith( swingFx.jFrame ).clickOn();
	}

}
