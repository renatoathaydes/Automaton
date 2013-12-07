package com.athaydes.automaton

import com.athaydes.automaton.geometry.PointOperators
import com.athaydes.automaton.mixins.TimeAware
import groovy.util.logging.Slf4j
import javafx.application.Application
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.stage.Stage
import org.junit.Before
import org.junit.Test

import java.awt.*
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

import static com.athaydes.automaton.Speed.VERY_FAST
import static com.google.code.tempusfugit.temporal.Duration.seconds
import static com.google.code.tempusfugit.temporal.Timeout.timeout
import static com.google.code.tempusfugit.temporal.WaitFor.waitOrTimeout

/**
 *
 * User: Renato
 */
@Slf4j
@Mixin( TimeAware )
class FXBaseForTests implements HasMixins {

	@Before
	void setup() {
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
			optionalDoMoveTo( rect )
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

		waitOrTimeout condition { future.size() == 1 }, timeout( seconds( 2 ) )
		assert future.poll() instanceof ActionEvent
	}

	void testDoubleClickOn( Closure doDoubleClickOn ) {
		def future = new LinkedBlockingDeque( 1 )
		def buttonToClick = new Button( id: 'b', text: 'Click Me', prefWidth: 50, prefHeight: 50 )
		buttonToClick.onMouseClicked = [ handle: {
			if ( it.clickCount == 2 ) buttonToClick.textFill = Color.RED
		} ] as EventHandler

		Platform.runLater {
			def hbox = new HBox( padding: [ 40 ] as Insets )
			hbox.children.add buttonToClick
			FXApp.scene.root = hbox
			future << true
		}

		assert future.poll( 4, TimeUnit.SECONDS )
		sleep 250

		doDoubleClickOn( buttonToClick )

		waitOrTimeout condition { buttonToClick.textFill == Color.RED }, timeout( seconds( 5 ) )

	}

	void testDrag( Closure doDrag ) {
		def blockUntilTestSceneSetup = new LinkedBlockingDeque( 1 )

		def rect = new Rectangle( id: 'rec', fill: Color.BLACK, width: 10, height: 10 )

		rect.onMouseDragged = { MouseEvent e ->
			rect.relocate( e.sceneX - ( rect.width / 2 ), e.sceneY - ( rect.height / 2 ) )
			e.consume()
		} as EventHandler

		def target = new Rectangle( id: 'rec', fill: Color.RED, width: 10, height: 10 )

		Platform.runLater {
			def box = new Pane( width: 200, height: 150 )
			box.children.addAll rect, target
			rect.relocate( 50, 20 )
			target.relocate( 150, 50 )

			FXApp.scene.root = box
			blockUntilTestSceneSetup << true
		}

		assert blockUntilTestSceneSetup.poll( 4, TimeUnit.SECONDS )
		sleep 250

		doDrag( rect, target )

		waitOrTimeout condition {
			rect.localToScene( 0, 0 ) == target.localToScene( 0, 0 )
		}, timeout( seconds( 2 ) )
	}

	void testType( Closure getDriver ) {
		def future = new LinkedBlockingDeque( 1 )
		def textArea = new TextArea( maxWidth: 200, maxHeight: 150 )

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
	void testMoveTo_Node() {
		testMoveTo withDriver
	}

	@Test
	void testMoveToNodes() {
		testMoveTo withDriver, { node -> withDriver().moveToNodes( [ node ] ) }
	}

	@Test
	void testCenterOf() {
		testCenterOf withDriver
	}

	@Test
	void testClickOn_Node() {
		testClickOn { Node n ->
			def automaton = withDriver()
			assert automaton == automaton.clickOn( n )
		}
	}

	@Test
	void testClickOnNodes() {
		testClickOn { Node n ->
			def automaton = withDriver()
			assert automaton == automaton.clickOnNodes( [ n ] )
		}
	}

	@Test
	void testDoubleClickOn_Node() {
		testDoubleClickOn { Node n ->
			def automaton = withDriver()
			assert automaton == automaton.doubleClickOn( n )
		}
	}

	@Test
	void testDoubleClickOnNodes() {
		testDoubleClickOn { Node n ->
			def automaton = withDriver()
			assert automaton == automaton.doubleClickOnNodes( [ n ] )
		}
	}

	@Test
	void testDrag_Node() {
		testDrag { Node n1, Node target ->
			def automaton = withDriver()
			assert automaton == automaton.drag( n1 ).onto( target )
		}
	}

	@Test
	void testType() {
		testType withDriver
	}

	@Test
	void testGetSceneActualPosition() {
		def future = new LinkedBlockingDeque( 1 )
		def root = null

		Automaton.user.moveTo( 20, 20, VERY_FAST )

		def app = new Application() {
			@Override
			void start( Stage stage ) throws Exception {
				root = new VBox( spacing: 0 )
				root.onMouseEntered = new EventHandler<MouseEvent>() {
					@Override
					void handle( MouseEvent event ) {
						future << event
					}
				}
				root.children.setAll new Rectangle( 30, 40, Color.BLUEVIOLET )
				stage.scene = new Scene( root, 35, 50 )
				future << true
			}
		}

		FXApp.startApp( app )

		assert future.poll( 4, TimeUnit.SECONDS )
		sleep 250

		def windowPos = FXAutomaton.getWindowPosition( root )

		def sceneActualPosition = FXAutomaton.getScenePosition( root )
		def margins = new Point( 5, 2 )

		use( PointOperators ) {
			Automaton.user.moveTo(
					windowPos + sceneActualPosition + margins, VERY_FAST )
		}

		assert future.poll() instanceof MouseEvent
	}

}

abstract class FxDriverWithSelectorsTest extends SimpleFxDriverTest {

	@Test
	void testMoveTo_Id() {
		testMoveTo(
				withDriver,
				{ node -> withDriver().moveTo( '#rec' ) }
		)
	}

	@Test
	void testMoveTo_Class() {
		testMoveTo(
				withDriver,
				{ node -> withDriver().moveTo( Rectangle ) }
		)
	}

	@Test
	void testClickOn_Id() {
		testClickOn { withDriver().clickOn( '#b' ) }
	}

	@Test
	void testClickOn_Class() {
		testClickOn { withDriver().clickOn( Button ) }
	}

	@Test
	void testDoubleClickOn_Id() {
		testDoubleClickOn { withDriver().doubleClickOn( '#b' ) }
	}

	@Test
	void testDoubleClickOn_Class() {
		testDoubleClickOn { withDriver().doubleClickOn( Button ) }
	}

	def testGetAt( Closure doGetAt ) {
		def future = new LinkedBlockingDeque( 1 )
		def textArea = new TextArea( id: 'ta', maxWidth: 200, maxHeight: 150 )

		Platform.runLater {
			def hbox = new HBox( padding: [ 40 ] as Insets )
			hbox.children.add textArea
			FXApp.scene.root = hbox
			future << true
		}

		assert future.poll( 4, TimeUnit.SECONDS )
		sleep 250

		assert doGetAt()
	}

	@Test
	void testGetAt_Selector() {
		testGetAt { withDriver().getAt( '#ta' ) }
	}

	@Test
	void testGetAt_Class() {
		testGetAt { withDriver().getAt( TextArea ) }
	}

	@Test
	void testGetAll_Selector() {
		def labelA = new Label( text: 'A', id: 'a' )
		def labelB = new Label( text: 'B', id: 'b' )
		[ labelA, labelB ].each { it.styleClass.add( 'sc' ) }
		def checkBoxC = new CheckBox( text: 'C', id: 'c' )

		def future = new LinkedBlockingDeque( 1 )

		Platform.runLater {
			def hbox = new HBox( padding: [ 40 ] as Insets )
			hbox.children.addAll labelA, labelB, checkBoxC
			FXApp.scene.root = hbox
			future << true
		}

		assert future.poll( 4, TimeUnit.SECONDS )
		sleep 250

		assert withDriver().getAll( '#a' ) as Set == [ labelA ] as Set
		assert withDriver().getAll( '.sc' ) as Set == [ labelA, labelB ] as Set
	}

	@Test
	void testGetAll_Class() {
		def labelA = new Label( text: 'A', id: 'a' )

		def labelB = new Label( text: 'B', id: 'b' )
		def checkBoxC = new CheckBox( text: 'C', id: 'c' )

		def future = new LinkedBlockingDeque( 1 )

		Platform.runLater {
			def hbox = new HBox( padding: [ 40 ] as Insets )
			hbox.children.addAll labelA, labelB, checkBoxC
			FXApp.scene.root = hbox
			future << true
		}

		assert future.poll( 4, TimeUnit.SECONDS )
		sleep 250

		assert withDriver().getAll( Label ) as Set == [ labelA, labelB ] as Set
		assert withDriver().getAll( CheckBox ) as Set == [ checkBoxC ] as Set
	}

}

class FXAutomatonTest extends SimpleFxDriverTest {

	{
		withDriver = { FXAutomaton.user }
	}

}

class FXerTest extends FxDriverWithSelectorsTest {

	{
		withDriver = { FXer.getUserWith( FXApp.scene.root ) }
	}
}

class SwingerFXerFXTest extends FxDriverWithSelectorsTest {

	{
		withDriver = { SwingerFxer.getUserWith( null, FXApp.scene.root ) }
	}

}