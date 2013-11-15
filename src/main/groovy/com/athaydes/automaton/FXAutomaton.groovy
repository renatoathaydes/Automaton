package com.athaydes.automaton

import groovy.util.logging.Slf4j
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

	/**
	 * Get the singleton instance of FXAutomaton, which is lazily created.
	 * @return FXAutomaton singleton instance
	 */
	static synchronized FXAutomaton getUser( ) {
		if ( !instance ) instance = new FXAutomaton()
		instance
	}

	protected FXAutomaton( ) {}

	FXAutomaton clickOn( Node node, Speed speed = DEFAULT ) {
		moveTo( node, speed ).click()
	}

	FXAutomaton doubleClickOn( Node node, Speed speed = DEFAULT ) {
		moveTo( node, speed ).doubleClick()
	}

	FXAutomaton moveTo( Node node, Speed speed = DEFAULT ) {
		moveTo( { centerOf( node ) }, speed )
	}

	FXDragOn<FXAutomaton> drag( Node node ) {
		def target = centerOf node
		new FXDragOn( this, target.x, target.y )
	}

	static Point centerOf( Node node ) {
		assert node != null, "Node could not be found"
		def windowPos = getWindowPosition( node )
		def scenePos = getScenePosition( node )

		def boundsInScene = node.localToScene node.boundsInLocal
		def absX = windowPos.x + scenePos.x + boundsInScene.minX
		def absY = windowPos.y + scenePos.y + boundsInScene.minY
		[ ( absX + boundsInScene.width / 2 ).intValue(),
				( absY + boundsInScene.height / 2 ).intValue() ] as Point
	}

	static Point getWindowPosition( Node node ) {
		new Point( node.scene.window.x.intValue(), node.scene.window.y.intValue() )
	}

	// required because of JavaFX bug: RT-34307
	private static Point getScenePosition( Node node ) {
		scenePos.x = Math.max( node.scene.x.intValue(), scenePos.x )
		scenePos.y = Math.max( node.scene.y.intValue(), scenePos.y )
		scenePos
	}

	private static scenePos = new Point()

}

@Slf4j
class FXApp extends Application {

	private static Stage stage
	private static stageFuture = new ArrayBlockingQueue<Stage>( 1 )

	static Scene getScene( ) {
		if ( stage ) stage.scene
		else throw new RuntimeException( "You must initialize FXApp before you can get the Scene" )
	}

	synchronized static Stage initialize( String... args ) {
		if ( !stage ) {
			log.debug 'Initializing FXApp'
			Thread.start { launch FXApp, args }
			sleep 500
			stage = stageFuture.poll 10, TimeUnit.SECONDS
			assert stage
			stageFuture = null
		}
		doInFXThreadBlocking {
			stage.scene = emptyScene()
			ensureShowing( stage )
		}
		log.debug "Stage now showing!"
		stage
	}

	private static void ensureShowing( Stage stage ) {
		stage.show()
		stage.toFront()
	}

	static doInFXThreadBlocking( Runnable toRun ) {
		if ( Platform.isFxApplicationThread() )
			toRun.run()
		else {
			def blockUntilDone = new ArrayBlockingQueue( 1 )
			Platform.runLater { toRun.run(); blockUntilDone << true }
			assert blockUntilDone.poll( 5, TimeUnit.SECONDS )
		}
	}

	static void startApp( Application app, String... args ) {
		initialize( args )
		Platform.runLater { app.start stage }
	}

	@Override
	void start( Stage primaryStage ) throws Exception {
		primaryStage.title = 'FXAutomaton Stage'
		stageFuture.add primaryStage
	}

	private static Scene emptyScene( ) {
		new Scene( new VBox(), 600, 500 )
	}

}

class FXer extends Automaton<FXer> {

	Node node
	def delegate = FXAutomaton.user

	/**
	 * Gets a new instance of <code>FXer</code> using the given
	 * top-level Node.
	 * <br/>
	 * The search space is limited to the given Node.
	 * @param node top level JavaFX Node to use
	 * @return a new FXer instance
	 */
	static FXer getUserWith( Node node ) {
		new FXer( node: node )
	}

	protected FXer( ) {}

	FXer clickOn( Node node, Speed speed = DEFAULT ) {
		delegate.clickOn( node, speed )
		this
	}

	FXer clickOn( String selector, Speed speed = DEFAULT ) {
		delegate.clickOn( this[ selector ], speed )
		this
	}

	FXer doubleClickOn( Node node, Speed speed = DEFAULT ) {
		moveTo( node, speed ).doubleClick()
	}

	FXer doubleClickOn( String selector, Speed speed = DEFAULT ) {
		moveTo( this[ selector ], speed ).doubleClick()
	}

	FXer moveTo( Node node, Speed speed = DEFAULT ) {
		delegate.moveTo( node, speed )
		this
	}

	FXer moveTo( String selector, Speed speed = DEFAULT ) {
		delegate.moveTo( this[ selector ], speed )
		this
	}

	FXDragOn<FXer> drag( Node node ) {
		def target = centerOf node
		new FXerDragOn( this, target.x, target.y )
	}

	FXDragOn<FXer> drag( String selector ) {
		def target = centerOf this[ selector ]
		new FXerDragOn( this, target.x, target.y )
	}

	Node getAt( String selector ) {
		def res = node.lookup( selector )
		if ( res ) res else
			throw new RuntimeException( "Could not locate Node: $selector" )
	}

	def <K extends Node> K getAt( Class<K> type ) {
		this[ type.simpleName ] as K
	}

	Point centerOf( Node node ) {
		delegate.centerOf( node )
	}

}

class FXDragOn<T extends Automaton<? extends Automaton>> extends DragOn<T> {

	protected FXDragOn( T automaton, fromX, fromY ) {
		super( automaton, fromX, fromY )
	}

	T onto( Node node, Speed speed = Automaton.DEFAULT ) {
		def center = FXAutomaton.centerOf( node )
		onto( center.x, center.y, speed )
	}

}

class FXerDragOn extends FXDragOn<FXer> {

	protected FXerDragOn( FXer fxer, fromX, fromY ) {
		super( fxer, fromX, fromY )
	}

	FXer onto( String selector, Speed speed = Automaton.DEFAULT ) {
		def node = automaton[ selector ]
		onto( node, speed )
	}
}
