package com.athaydes.automaton.assertion

import com.athaydes.automaton.HasSwingCode
import com.athaydes.automaton.Swinger
import com.athaydes.automaton.mixins.SwingTestHelper
import groovy.swing.SwingBuilder
import javafx.scene.control.CheckBoxBuilder
import javafx.scene.control.ColorPicker
import spock.lang.Specification

import javax.swing.*
import java.awt.*

import static com.athaydes.automaton.assertion.AutomatonMatcher.*
import static javafx.scene.paint.Color.AQUA
import static javafx.scene.paint.Color.BLUE
import static org.hamcrest.CoreMatchers.not
import static org.junit.Assert.assertThat

/**
 * @author Renato
 */
@Mixin( SwingTestHelper )
class AutomatonMatcherTest extends Specification implements HasSwingCode {

	JFrame jFrame

	def "hasText matcher should pass when assertion is valid"( ) {
		given:
		def tModel = [
				[ firstCol: '1 - 1' ],
				[ firstCol: '2 - 1' ],
		]
		and:
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 200, 200 ] as Dimension, show: false ) {
				label( text: 'A label' )
				button( text: 'Click Me' )
				tree( rootVisible: false )
				scrollPane {
					table {
						tableModel( list: tModel ) {
							propertyColumn( header: 'Col 1', propertyName: 'firstCol' )
						}
					}
				}
			}
		}

		and:
		def swinger = Swinger.getUserWith( jFrame )

		expect:
		assertThat swinger[ selector ], hasText( expectedText )

		where:
		selector        | expectedText
		'text:A label'  | 'A label'
		'text:Click Me' | 'Click Me'
		'text:Col 1'    | 'Col 1'
		'text:1 - 1'    | '1 - 1'
		'text:2 - 1'    | '2 - 1'

	}

	def "hasText matcher should fail when assertion is not valid"( ) {
		given:
		def tModel = [
				[ firstCol: '1 - 1' ],
				[ firstCol: '2 - 1' ],
		]
		and:
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 200, 200 ] as Dimension, show: false ) {
				label( text: 'A label' )
				button( text: 'Click Me' )
				tree( rootVisible: false )
				scrollPane {
					table {
						tableModel( list: tModel ) {
							propertyColumn( header: 'Col 1', propertyName: 'firstCol' )
						}
					}
				}
			}
		}

		and:
		def swinger = Swinger.getUserWith( jFrame )

		when:
		assertThat swinger[ selector ], hasText( expectedText )

		then:
		thrown AssertionError

		where:
		selector        | expectedText
		'text:A label'  | 'A labl'
		'text:Click Me' | ''
		'text:Col 1'    | 'col 1'
		'text:1 - 1'    | '2 - 1'
		'text:2 - 1'    | '1 - 1'
	}

	def "hasValue should pass when assertion is valid"( ) {
		expect:
		assertThat component, hasValue( value )

		where:
		component                       | value
		new ColorPicker( AQUA )         | AQUA
		new JColorChooser( Color.CYAN ) | Color.CYAN
		new JButton( 'click' )          | 'click'
		new JCheckBox( 'a' )            | 'a'
		new JCheckBoxMenuItem( 'b' )    | 'b'
	}

	def "hasValue should fail when assertion is not valid"( ) {
		when:
		assertThat component, hasValue( value )

		then:
		thrown AssertionError

		where:
		component                       | value
		new ColorPicker( AQUA )         | BLUE
		new JColorChooser( Color.CYAN ) | Color.YELLOW
		new JButton( 'click' )          | 'no good'
		new JCheckBox( 'a' )            | 'z'
		new JCheckBoxMenuItem( 'b' )    | 'f'
	}

	def "selected should pass when assertion is valid"( ) {
		expect:
		assertThat component, selected()

		where:
		component << [
				new JCheckBox( 'hi', true ),
				new JCheckBoxMenuItem( 'ho', true ),
				CheckBoxBuilder.create().selected( true ).build()
		]
	}

	def "selected should fail when assertion is not valid"( ) {
		when:
		assertThat component, selected()

		then:
		thrown AssertionError

		where:
		component << [
				new JCheckBox( 'hi', false ),
				new JCheckBoxMenuItem( 'ho', false ),
				CheckBoxBuilder.create().selected( false ).build()
		]
	}

	def "visible should pass when assertion is valid"( ) {
		expect:
		assertThat component, visible()

		where:
		component << [
				new JCheckBox( 'hi' ),
				CheckBoxBuilder.create().build()
		]
	}

	def "visible should fail when assertion is not valid"( ) {
		when:
		assertThat component, visible()

		then:
		thrown AssertionError

		where:
		component << [
				new JCheckBox( text: 'hi', visible: false ),
				CheckBoxBuilder.create().visible( false ).build()
		]
	}

	def "showing should pass when assertion is valid"( ) {
		given:
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 200, 200 ] as Dimension, show: true ) {
				label( name: 'lbl', text: 'A label' )
				button( name: 'btn', text: 'Click Me', visible: false )
			}
		}

		and:
		Swinger swinger = Swinger.getUserWith( jFrame )

		expect:
		assertThat jFrame, showing()
		assertThat swinger[ 'lbl' ], showing()
		assertThat swinger[ 'btn' ], not( showing() )
	}

	def "showing should fail when assertion is not valid"( ) {
		given:
		new SwingBuilder().edt {
			jFrame = frame( name: 'frame', title: 'Frame', size: [ 200, 200 ] as Dimension, show: false ) {
				label( name: 'lbl', text: 'A label' )
				button( name: 'btn', text: 'Click Me', visible: false )
			}
		}

		and:
		def swinger = Swinger.getUserWith( jFrame )

		when:
		assertThat swinger[ componentName ], showing()

		then:
		thrown AssertionError

		where:
		componentName << [ 'frame', 'lbl', 'btn' ]
	}

	@Override
	JFrame getJFrame( ) { jFrame }
}
