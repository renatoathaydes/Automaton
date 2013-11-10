package com.athaydes.automaton.samples

import com.athaydes.automaton.Swinger
import com.athaydes.automaton.samples.apps.SwingLoginPage
import org.junit.After
import org.junit.Before
import org.junit.Test

import static com.athaydes.automaton.assertion.AutomatonMatcher.hasText
import static org.junit.Assert.assertThat

/**
 * @author Renato
 */
class SwingLoginSample {

	SwingLoginPage loginPage

	@Before
	void before( ) {
		loginPage = new SwingLoginPage()
	}

	@After
	void after( ) {
		loginPage.destroy()
	}

	@Test
	void "When trying to login without filling password, appropriate message is issued"( ) {
		def swinger = Swinger.getUserWith( loginPage.jFrame )

		swinger.clickOn( 'combo' )
				.clickOn( 'text:Users' )
				.clickOn( 'type:JTextField' )
				.type( 'automaton' )
				.clickOn( 'ok' ).pause( 250 )

		assertThat swinger[ 'message-area' ], hasText( 'Please enter your password' )
	}

}
