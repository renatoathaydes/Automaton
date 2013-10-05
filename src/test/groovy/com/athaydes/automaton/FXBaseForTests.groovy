package com.athaydes.automaton

import groovy.util.logging.Slf4j
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.junit.Before
import org.junit.Test

import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

/**
 *
 * User: Renato
 */
@Slf4j
class FXBaseForTests {

	@Before
	void setup( ) {
		log.debug "Setting up FX Automaton Test"
		FXApp.initialize()
	}

	void testMoveTo( Closure getDriver, Closure optionalDoMoveTo = null ) {
		def blockUntilTestSceneSetup = new LinkedBlockingDeque( 1 )

		def rect = new Rectangle( id: 'rec', fill: Color.BLACK, width: 10, height: 10 )
		rect.onMouseEntered = [ handle: { rect.fill = Color.BLUE } ] as EventHandler
		rect.onMouseExited = [ handle: { rect.fill = Color.YELLOW } ] as EventHandler

		Platform.runLater {
			def hbox = new HBox( padding: [ 40 ] as Insets )
			hbox.children.add rect
			FXApp.scene.root = hbox
			blockUntilTestSceneSetup << true
		}

		assert blockUntilTestSceneSetup.poll( 4, TimeUnit.SECONDS )
		sleep 250

		if ( optionalDoMoveTo )
			optionalDoMoveTo()
		else
			getDriver().moveTo rect

		assert rect.fill == Color.BLUE

		getDriver().moveBy 0, rect.height as int
		assert rect.fill == Color.YELLOW

		getDriver().moveBy 0, -rect.height as int
		assert rect.fill == Color.BLUE

		getDriver().moveBy rect.width as int, 0
		assert rect.fill == Color.YELLOW
	}

	void testCenterOf( Closure getDriver ) {
		def blockUntilTestSceneSetup = new LinkedBlockingDeque( 1 )

		def rect = new Rectangle( fill: Color.RED, width: 20, height: 20 )
		rect.onMouseEntered = [ handle: { rect.fill = Color.BLUE } ] as EventHandler
		rect.onMouseExited = [ handle: { rect.fill = Color.YELLOW } ] as EventHandler

		Platform.runLater {
			def hbox = new HBox( padding: [ 80 ] as Insets )
			hbox.children.add rect
			FXApp.scene.root = hbox
			blockUntilTestSceneSetup << true
		}

		assert blockUntilTestSceneSetup.poll( 5, TimeUnit.SECONDS )
		sleep 250

		def center = getDriver().centerOf rect

		getDriver().moveTo center.x as int, center.y as int
		assert rect.fill == Color.BLUE

		getDriver().moveBy 0, rect.height as int
		assert rect.fill == Color.YELLOW

		getDriver().moveBy 0, -rect.height as int
		assert rect.fill == Color.BLUE

		getDriver().moveBy rect.width as int, 0
		assert rect.fill == Color.YELLOW
	}

	void testClickOn( Closure doClickOn ) {
		def future = new LinkedBlockingDeque( 1 )
		def buttonToClick = new Button( id: 'b', text: 'Click Me', prefWidth: 50, prefHeight: 50 )
		buttonToClick.onAction = [ handle: { future << it } ] as EventHandler

		Platform.runLater {
			def hbox = new HBox( padding: [ 40 ] as Insets )
			hbox.children.add buttonToClick
			FXApp.scene.root = hbox
			future << true
		}

		assert future.poll( 4, TimeUnit.SECONDS )
		sleep 250

		doClickOn( buttonToClick )
		sleep 250

		assert future.size() == 1
		assert future.poll() instanceof ActionEvent
	}

	void testType( Closure getDriver ) {
		def future = new LinkedBlockingDeque( 1 )
		def textArea = new TextArea()

		Platform.runLater {
			def hbox = new HBox( padding: [ 40 ] as Insets )
			hbox.children.add textArea
			FXApp.scene.root = hbox
			future << true
		}

		assert future.poll( 4, TimeUnit.SECONDS )
		sleep 250

		getDriver().clickOn( textArea ).type( 'I can type here' ).pause( 100 )

		assert textArea.text == 'I can type here'
	}

}

abstract class SimpleFxDriverTest extends FXBaseForTests {

	Closure withDriver

	@Test
	void testMoveTo_Node( ) {
		testMoveTo withDriver
	}

	@Test
	void testCenterOf( ) {
		testCenterOf withDriver
	}

	@Test
	void testClickOn_Node( ) {
		testClickOn { Node n -> withDriver().clickOn( n ) }
	}

	@Test
	void testType( ) {
		testType withDriver
	}

}

abstract class FxDriverWithSelectorsTest extends SimpleFxDriverTest {

	@Test
	void testMoveTo_Id( ) {
		testMoveTo(
				withDriver,
				{ withDriver().moveTo( '#rec' ) }
		)
	}

	@Test
	void testClickOn_Id( ) {
		testClickOn { withDriver().clickOn( '#b' ) }
	}

}

class FXAutomatonTest extends SimpleFxDriverTest {

	{ withDriver = { FXAutomaton.user } }

}

class FXerTest extends FxDriverWithSelectorsTest {

	{ withDriver = { FXer.userWith( FXApp.scene.root ) } }
}

class SwingerFXerFXTest extends FxDriverWithSelectorsTest {

	{ withDriver = { SwingerFxer.userWith( null, FXApp.scene.root ) } }

}