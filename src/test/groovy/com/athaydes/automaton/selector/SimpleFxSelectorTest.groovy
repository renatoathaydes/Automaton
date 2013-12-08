package com.athaydes.automaton.selector

import com.athaydes.automaton.FXApp
import com.athaydes.automaton.samples.apps.JavaFxSampleScene
import javafx.scene.Node
import javafx.scene.Scene
import javafx.stage.Stage
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
		Stage stage = FXApp.initialize()
		FXApp.doInFXThreadBlocking {
			scene = new JavaFxSampleScene()
			stage.scene = scene
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

			boolean followPopups() { false }
		}

		when:
		def result = fxSelector.apply( selector, scene.root )

		then:
		result as Set == scene.root.lookupAll( "#$selector" ) as Set

		where:
		selector << [ 'left-color-picker', 'right-color-picker',
				'fx-text', 'fx-input' ]
	}

}
