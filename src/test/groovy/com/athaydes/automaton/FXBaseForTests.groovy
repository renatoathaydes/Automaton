package com.athaydes.automaton

import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.stage.Stage
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

/**
 *
 * User: Renato
 */
class FXBaseForTests {

    static Stage stage

	@BeforeClass
	static void setup( ) {
		stage = FXApp.initialize()
	}

	@AfterClass
	static void cleanup( ) {
		FXApp.close()
	}

	void testMoveTo( Closure getDriver ) {
		def future = new LinkedBlockingDeque( 1 )

		def rect = new Rectangle( fill: Color.BLACK, width: 10, height: 10 )
		rect.onMouseEntered = [ handle: { rect.fill = Color.BLUE } ] as EventHandler
		rect.onMouseExited = [ handle: { rect.fill = Color.YELLOW } ] as EventHandler

		Platform.runLater {
			def hbox = new HBox( padding: [ 40 ] as Insets )
			hbox.children.add rect
			FXApp.scene.root = hbox
			future << true
		}

		assert future.poll( 4, TimeUnit.SECONDS )
		sleep 250

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
		def future = new LinkedBlockingDeque( 1 )

		def rect = new Rectangle( fill: Color.RED, width: 20, height: 20 )
		rect.onMouseEntered = [ handle: { rect.fill = Color.BLUE } ] as EventHandler
		rect.onMouseExited = [ handle: { rect.fill = Color.YELLOW } ] as EventHandler

		Platform.runLater {
			def hbox = new HBox( padding: [ 80 ] as Insets )
			hbox.children.add rect
			FXApp.scene.root = hbox
			future << true
		}

		assert future.poll( 4, TimeUnit.SECONDS )
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

	void testClickOn( Closure getDriver ) {
		def future = new LinkedBlockingDeque( 1 )
		def buttonToClick = new Button( text: 'Click Me', prefWidth: 50, prefHeight: 50 )
		buttonToClick.onAction = [ handle: { future << it } ] as EventHandler

		Platform.runLater {
			def hbox = new HBox( padding: [ 40 ] as Insets )
			hbox.children.add buttonToClick
			FXApp.scene.root = hbox
			future << true
		}

		assert future.poll( 4, TimeUnit.SECONDS )
		sleep 250

        getDriver().pause( 250 ).clickOn( buttonToClick ).pause 250

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

class FXAutomatonTest extends FXBaseForTests {

    @Test
    void testMoveTo() {
        testMoveTo { FXAutomaton.user }
    }

    @Test
    void testCenterOf() {
        testCenterOf { FXAutomaton.user }
    }

    @Test
    void testClickOn() {
        testClickOn { FXAutomaton.user }
    }

    @Test
    void testType() {
        testType { FXAutomaton.user }
    }

}

class FXerTest extends FXBaseForTests {

    @Test
    void testMoveTo() {
        testMoveTo { FXer.userWith( FXApp.scene.root ) }
    }

    @Test
    void testCenterOf() {
        testCenterOf { FXer.userWith( FXApp.scene.root ) }
    }

    @Test
    void testClickOn() {
        testClickOn { FXer.userWith( FXApp.scene.root ) }
    }

    @Test
    void testType() {
        testType { FXer.userWith( FXApp.scene.root ) }
    }

}