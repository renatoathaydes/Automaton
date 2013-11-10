package com.athaydes.automaton.samples.apps

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.effect.Reflection
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import javafx.scene.text.Font
import javafx.scene.text.Text

/**
 * 
 * User: Renato
 */
class JavaFxSampleScene extends Scene {

	Text fxText
	ColorPicker leftPicker
	ColorPicker rightPicker

	JavaFxSampleScene( ) {
		super( new Group(), Color.BLACK )
		println "Creating Scene"
		leftPicker = new ColorPicker( value: Color.CYAN, id: 'left-color-picker' )
		rightPicker = new ColorPicker( value: Color.DODGERBLUE, id: 'right-color-picker' )
		[ leftPicker, rightPicker ].each { it.onAction = colorPickerHandler() }
		def pickers = new HBox( spacing: 10, minHeight: 70, alignment: Pos.CENTER )
		def pickerLabel = new Label( text: 'Text Colors:', textFill: Color.WHITE )
		pickers.children << pickerLabel << leftPicker << rightPicker
		fxText = new Text( id: 'fx-text', x: 40, y: 100, font: new Font( 'Arial', 35 ),
				text: 'This is JavaFX', fill: javaFxCoolTextFill(), effect: new Reflection() )
		def inputText = new TextField( id: 'fx-input', translateX: 75, translateY: 170 )
		( root as Group ).children << pickers << fxText << inputText
	}

	private EventHandler<ActionEvent> colorPickerHandler( ) {
		[ handle: { fxText.fill = javaFxCoolTextFill() } ] as EventHandler<ActionEvent>
	}

	private LinearGradient javaFxCoolTextFill( ) {
		new LinearGradient( 0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
				new Stop( 0, leftPicker.value ), new Stop( 1, rightPicker.value ) )
	}

}
