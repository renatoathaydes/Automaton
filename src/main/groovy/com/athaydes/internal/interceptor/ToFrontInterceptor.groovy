package com.athaydes.internal.interceptor

import com.athaydes.automaton.FXApp
import javafx.scene.Node
import javafx.stage.Stage

class ToFrontInterceptor extends DelegatingMetaClass {

	private Stage latestStage

	ToFrontInterceptor( final Class theClass ) {
		super( theClass )
		initialize();
	}

	def invokeMethod( def object, String name, Object[] args ) {
		latestStage = updateLatestStageIfPossible( args )
		if ( latestStage && !latestStage.focused ) {
			FXApp.doInFXThreadBlocking { latestStage.toFront() }
			sleep 50
		}
		super.invokeMethod( object, name, args )
	}

	private Stage updateLatestStageIfPossible( Object[] args ) {
		def maybeNode = argAsNode( args.length > 0 ? args.first() : null )
		if ( maybeNode ) {
			def window = maybeNode.scene.window
			if ( window instanceof Stage ) {
				latestStage = window as Stage
			}
		}
		latestStage
	}

	Node argAsNode( arg ) {
		switch ( arg ) {
			case Node: return arg
			case Collection: if ( arg.first() instanceof Node ) {
				return arg.first()
			}
		}
		return null
	}

}
