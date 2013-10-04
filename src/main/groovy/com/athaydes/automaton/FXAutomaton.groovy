package com.athaydes.automaton

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.layout.VBox
import javafx.stage.Stage

import java.awt.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

/**
 *
 * User: Renato
 */
class FXAutomaton extends Automaton<FXAutomaton> {

	private static instance

	static synchronized FXAutomaton getUser( ) {
		if ( !instance ) instance = new FXAutomaton()
		instance
	}

	protected FXAutomaton( ) {}

	FXAutomaton clickOn( Node node, Speed speed = DEFAULT ) {
		if ( !node ) return this
		def center = centerOf node
		moveTo( center.x as int, center.y as int, speed ).click()
	}

	FXAutomaton moveTo( Node node, Speed speed = DEFAULT ) {
		def currPos = MouseInfo.pointerInfo.location
		def target = centerOf node
		move( currPos, target, speed )
	}

	Point centerOf( Node node ) {
		def windowPos = new Point( node.scene.window.x.intValue(), node.scene.window.y.intValue() )
		def scenePos = new Point( node.scene.x.intValue(), node.scene.y.intValue() )
		def boundsInScene = node.localToScene node.boundsInLocal
		def absX = windowPos.x + scenePos.x + boundsInScene.minX
		def absY = windowPos.y + scenePos.y + boundsInScene.minY
		[ ( absX + boundsInScene.width / 2 ).intValue(),
				( absY + boundsInScene.height / 2 ).intValue() ] as Point
	}

}

class FXApp extends Application {

	private static Stage stage
	private static stageFuture = new ArrayBlockingQueue<Stage>( 1 )

	static Scene getScene( ) { initialize().scene }

	static Stage initialize( ) {
		if ( !stage ) {
			println 'Initializing FXApp'
			Thread.start { launch FXApp }
			stage = stageFuture.poll 10, TimeUnit.SECONDS
			stageFuture = null
		}
        Platform.runLater{ stage.show() }
        sleep 500 // FIXME block until stage is shown
		stage
	}

	static void close( ) {
		println "FXAutomaton: Stopping FxApp"
		if ( stage ) Platform.runLater { stage.hide() }
		stageFuture?.clear()
	}

	static void startApp( Application app ) {
		initialize()
		Platform.runLater { app.start stage }
	}

	@Override
	void start( Stage primaryStage ) throws Exception {
		primaryStage.scene = new Scene( new VBox() )
		primaryStage.title = 'FXAutomaton Stage'
		primaryStage.show()
		primaryStage.toFront()
		stageFuture.add primaryStage
	}

}

class FXer extends Automaton<FXer> {

	Node node
	def delegate = FXAutomaton.user

	static FXer userWith( Node node ) {
		new FXer( node: node )
	}

    FXer clickOn( Node node, Speed speed = DEFAULT ) {
        delegate.clickOn( node, speed )
        this
    }

	FXer clickOn( String selector, Speed speed = DEFAULT ) {
		delegate.clickOn( node.lookup( selector ), speed )
		this
	}

    FXer moveTo( Node node, Speed speed = DEFAULT ) {
        delegate.moveTo( node, speed )
        this
    }

	FXer moveTo( String selector, Speed speed = DEFAULT ) {
		delegate.moveTo( node.lookup( selector ), speed )
		this
	}

    Point centerOf( Node node ) {
        delegate.centerOf( node )
    }


}
