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
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.stage.Stage
import org.junit.Before
import org.junit.Test

import javax.swing.JButton
import java.awt.Point
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import static com.athaydes.automaton.Speed.VERY_FAST
import static com.google.code.tempusfugit.temporal.Duration.seconds
import static com.google.code.tempusfugit.temporal.Timeout.timeout
import static com.google.code.tempusfugit.temporal.WaitFor.waitOrTimeout
import static javafx.collections.FXCollections.observableArrayList

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
		sleep 500

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
		sleep 500

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
		sleep 500

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
		sleep 500

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

	void comboBoxItemCanBePicked( Closure selectOla ) {
		def future = new LinkedBlockingDeque( 1 )
		def box = new ComboBox(
				id: 'combo',
				items: observableArrayList( 'hej', 'hi', 'ola' ) )

		Platform.runLater {
			def hbox = new HBox( padding: [ 40 ] as Insets )
			hbox.children.add box
			FXApp.scene.root = hbox
			future << true
		}

		assert future.poll( 4, TimeUnit.SECONDS )
		sleep 500

		selectOla( box )

		waitOrTimeout condition { box.selectionModel.selectedIndex == 2 },
				timeout( seconds( 4 ) )
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
	void comboBoxItemCanBePicked() {
		comboBoxItemCanBePicked { ComboBox box ->
			Platform.runLater( { box.selectionModel.select( 2 ) } )
		}
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
				stage.scene = new Scene( root, 350, 500 )
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

	@Test
	void popupNodeCanBeFound_ByText() {
		def sceneReady = new CountDownLatch( 1 )
		def popupButtonPressed = new CountDownLatch( 1 )

		def dialog = null
		final b1 = new Button( id: 'b1', text: 'Click me' )
		final b2 = new Button( id: 'b2', text: 'PopupButton' )
		b2.onAction = { event -> popupButtonPressed.countDown() } as EventHandler

		Platform.runLater {
			def hbox = new HBox( padding: [ 40 ] as Insets )
			hbox.children.add b1

			FXApp.scene.root = hbox
			dialog = new Stage()
			dialog.initOwner( null )
			final dialogVbox = new VBox( 20 )
			dialogVbox.children << b2

			Scene dialogScene = new Scene( dialogVbox, 200, 150 )
			dialog.scene = dialogScene
			b1.onAction = { event -> dialog.show() } as EventHandler
			sceneReady.countDown()
		}

		try {
			assert sceneReady.await( 4, TimeUnit.SECONDS )
			sleep 250

			withDriver().clickOn( b1 ).pause( 1500 ).clickOn( '#b2' )

			assert popupButtonPressed.await( 2, TimeUnit.SECONDS )
		} finally {
			FXApp.doInFXThreadBlocking { dialog.close() }
		}
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

	@Test
	void testEnterText() {
		final textAreaText = 'hello'
		final tf1Text = 'renato@#athaydes.com//hi$'
		final tf2Text = '#@$%^&*()__-='

		def future = new LinkedBlockingDeque( 1 )
		def textArea = new TextArea( maxWidth: 200, maxHeight: 150 )
		def tf1 = new TextField( id: 'tf1' )
		def tf2 = new TextField( id: 'tf2' )

		Platform.runLater {
			def hbox = new HBox( padding: [ 40 ] as Insets )
			hbox.children.addAll textArea, tf1, tf2
			FXApp.scene.root = hbox
			future << true
		}

		assert future.poll( 4, TimeUnit.SECONDS )
		sleep 250

		withDriver().clickOn( 'tf1' ).enterText( tf1Text )
				.clickOn( textArea ).enterText( textAreaText )
				.clickOn( 'tf2' ).enterText( tf2Text )

		withDriver().waitForFxEvents()

		assert tf1.text == tf1Text
		assert tf2.text == tf2Text
		assert textArea.text == textAreaText

	}

	@Test
	void menuItemsCanBePicked() {
		def future = new LinkedBlockingDeque( 1 )

		def menuBar = new MenuBar()
		def menu = new Menu( 'This is a Menu' )

		def item1 = new MenuItem( 'First' )
		def item2 = new MenuItem( 'Second' )
		def item3 = new MenuItem( 'Third' )

		final clicksOn3 = new AtomicInteger( 0 )

		item3.setOnAction( { ActionEvent mouseEvent ->
			clicksOn3.incrementAndGet()
		} )

		Platform.runLater {
			menu.items.addAll( item1, item2, item3 )
			def hbox = new HBox( padding: [ 40 ] as Insets )
			hbox.children.add menuBar.with { menus.add( menu ); return it }
			FXApp.scene.root = hbox
			future << true
		}

		assert future.poll( 4, TimeUnit.SECONDS )
		sleep 500

		withDriver().clickOn( menuBar ).pause( 250 ).clickOn( 'text:Third' )

		waitOrTimeout condition { clicksOn3.intValue() == 1 }, timeout( seconds( 2 ) )
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
		withDriver = { SwingerFxer.getUserWith( new JButton(), FXApp.scene.root ) }
	}

}
