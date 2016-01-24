package com.athaydes.automaton.internal.interceptor

import com.athaydes.automaton.FXApp
import com.athaydes.automaton.FXAutomaton
import com.athaydes.automaton.HasSwingCode
import com.athaydes.automaton.Speed
import com.athaydes.automaton.mixins.SwingTestHelper
import com.google.code.tempusfugit.temporal.Condition
import groovy.swing.SwingBuilder
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import spock.lang.Specification

import javax.swing.*
import java.awt.*
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

import static com.google.code.tempusfugit.temporal.Duration.seconds
import static com.google.code.tempusfugit.temporal.Timeout.timeout
import static com.google.code.tempusfugit.temporal.WaitFor.waitOrTimeout

@Mixin( SwingTestHelper )
class ToFrontInterceptorTest extends Specification implements HasSwingCode {

	JFrame jFrame

	def "Can bring Stage to front when trying to click on a Node"() {
		def screenX = 50
		def screenY = 50

		given:
		"A JavaFX Window with a button b"
		FXApp.initialize()
		def clickCaptor = new LinkedBlockingDeque( 1 )
		def button = new Button( text: 'Click here', onAction: {
			event -> clickCaptor.add( true )
		} as EventHandler )

		FXApp.doInFXThreadBlocking {
			def vbox = new VBox( spacing: 20 )
			vbox.children.add( button )
			FXApp.scene.root = vbox
			FXApp.stage.with {
				x = screenX
				y = screenY
			}
		}
		FXAutomaton.user.waitForFxEvents()

		and:
		"A Swing Window which is right in front of the JavaFX Window"
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 500, 500 ] as Dimension,
					location: [ screenX, screenY ] as Point, show: true ) {
				label( 'Annoying Swing window in front of JavaFX Stage' )
			}
		}
		waitForJFrameToShowUp()

		when:
		"Automaton tries to click on the button b"
		FXAutomaton.user.clickOn( button, Speed.VERY_FAST )

		then:
		"The JavaFX Window is brought to front and the button is clicked just fine"
		waitOrTimeout( { clickCaptor.poll( 2, TimeUnit.SECONDS ) != null } as Condition,
				timeout( seconds( 3 ) ) )

		cleanup:
		jFrame?.dispose()
		try {
			FXApp.doInFXThreadBlocking { FXApp.stage.hide() }
		} catch ( e ) {
			e.printStackTrace()
		}

	}

	@Override
	JFrame getJFrame() {
		return jFrame
	}
}
