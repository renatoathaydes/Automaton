package com.athaydes.automaton.selector

import com.athaydes.automaton.FXApp
import com.athaydes.automaton.samples.apps.JavaFxLoginPage
import javafx.scene.Node
import javafx.scene.Scene
import org.junit.AfterClass
import org.junit.BeforeClass
import spock.lang.Specification

/**
 * @author Renato
 */
class SimpleFxSelectorTest extends Specification {

	static Scene scene;

	@BeforeClass
	static void before() {
		FXApp.startApp( new JavaFxLoginPage() );
		FXApp.doInFXThreadBlocking {
			scene = FXApp.scene
		}
	}

	@AfterClass
	static void after() {
		scene = null
	}

	def "SimpleFXSelector can find many different kinds of Nodes"() {
		given:
		def fxSelector = new SimpleFxSelector() {
			boolean matches( String query, Node node ) {
				node.id == query
			}
		}

		when:
		def result = fxSelector.apply( selector, scene.root )

		then:
		result as Set == scene.root.lookupAll( "#$selector" ) as Set

		where:
		selector << [ 'login-panel', 'combo', 'user-name', 'user-password',
				'message-area', 'login-button' ]
	}

}
