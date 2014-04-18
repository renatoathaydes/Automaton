package com.athaydes.automaton.cli.demo

import com.athaydes.automaton.FXApp
import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.Stage

/**
 *
 */
class JavaFxDemo extends Demo {

	void start() {
		println "JavaFX Demo"
		FXApp.startApp( new JavaFxDemoApp( this ) )
	}

}

class JavaFxDemoApp extends Application {

	final Demo demo

	JavaFxDemoApp( Demo demo ) {
		this.demo = demo
	}

	@Override
	void start( Stage stage ) throws Exception {
		stage.title = 'Automaton JavaFX Demo'

		SplitPane rootPane = new SplitPane( dividerPositions: 0.25f )
		rootPane.orientation = Orientation.HORIZONTAL

		def colors = new TreeItem<String>( 'colors' )
		def blue = new TreeItem<String>( 'blue' )
		def violet = new TreeItem<String>( 'violet' )
		def red = new TreeItem<String>( 'red' )
		def yellow = new TreeItem<String>( 'yellow' )
		colors.children.addAll( blue, violet, red, yellow )

		def sports = new TreeItem<String>( 'sports' )
		def basketball = new TreeItem<String>( 'basketball' )
		def soccer = new TreeItem<String>( 'soccer' )
		def football = new TreeItem<String>( 'football' )
		def hockey = new TreeItem<String>( 'hockey' )
		sports.children.addAll( basketball, soccer, football, hockey )

		def food = new TreeItem<String>( 'food' )
		def hotDogs = new TreeItem<String>( 'hot dogs' )
		def pizza = new TreeItem<String>( 'pizza' )
		def ravioli = new TreeItem<String>( 'ravioli' )
		def bananas = new TreeItem<String>( 'bananas' )
		food.children.addAll( hotDogs, pizza, ravioli, bananas )

		def rootItem = new TreeItem<String>( 'root' )
		rootItem.children.addAll( colors, sports, food )

		def tree = new TreeView<String>( rootItem )
		tree.id = 'box-tree'
		tree.showRoot = false

		def rightPane = new SplitPane( orientation: Orientation.VERTICAL, dividerPositions: 0.2f )

		def statusLabel = new Label( text: 'You have not selected anything yet',
				id: 'status-label',
				style: '-fx-font: 14 Arial; -fx-font-weight: bold; -fx-font-style: italic;' )

		tree.setOnMouseClicked( {
			statusLabel.text = 'You selected ' +
					selectedItem( tree.selectionModel.selectedItem ).toString()
		} as EventHandler )

		def rightBox = new VBox( spacing: 10 )

		def scriptLabel = new Label( id: 'script-label', text: 'Enter an Automaton script:' )

		def scriptTextArea = new TextArea( id: 'text-area',
				text: initialScript(),
				style: '-fx-font: 12 monospaced' )

		def outputTextArea = new TextArea( id: 'output-text', editable: false )
		VBox.setVgrow( outputTextArea, Priority.ALWAYS )

		def buttonsBox = new HBox()
		def runButton = new Button( text: 'Run Automaton script', id: 'run-button' )
		runButton.onAction = {
			runButton.disable = true
			demo.runScript( scriptTextArea.text, [ append: { String text ->
				Platform.runLater {
					outputTextArea.appendText( text )
				}
			} ] ) {
				Platform.runLater {
					runButton.disable = false
				}
			}
		} as EventHandler

		def clearButton = new Button( text: 'Clear output', id: 'clear-output' )
		clearButton.onAction = { outputTextArea.clear() } as EventHandler
		buttonsBox.children.addAll( runButton, clearButton )
		HBox.setMargin( runButton, new Insets( 0d, 0d, 0d, 20d ) )
		HBox.setMargin( clearButton, new Insets( 0d, 0d, 0d, 5d ) )

		rightBox.children.addAll( scriptLabel, scriptTextArea, buttonsBox, outputTextArea )
		rightPane.items.addAll( new StackPane( statusLabel ), rightBox )

		rootPane.items.addAll( new StackPane( tree ), rightPane )

		def scene = new Scene( rootPane, 500D, 600D )
		stage.scene = scene
		stage.show()
		stage.centerOnScreen()
	}

	List selectedItem( TreeItem item ) {
		ArrayList result = [ ]
		while ( item && item.value != 'root' ) {
			result += item.value
			item = item.parent
		}
		result.reverse()
	}

	static String initialScript() {
		'''\
		|doubleClickOn 'text:colors'
		|pause 500
		|clickOn 'text:yellow'
		|waitForFxEvents()
		|
		|assertThat fxer[ 'status-label' ],
		|           hasText( 'You selected [colors, yellow]' )
		|
		|println "Test passed OK"
		|'''.stripMargin()
	}

}
