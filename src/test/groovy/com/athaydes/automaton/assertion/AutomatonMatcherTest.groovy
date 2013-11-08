package com.athaydes.automaton.assertion

import com.athaydes.automaton.HasSwingCode
import com.athaydes.automaton.Swinger
import com.athaydes.automaton.mixins.SwingTestHelper
import groovy.swing.SwingBuilder
import spock.lang.Specification

import javax.swing.*
import java.awt.*

import static com.athaydes.automaton.assertion.AutomatonMatcher.hasText
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
		assertThat( swinger[ selector ], hasText( expectedText ) )

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
		assertThat( swinger[ selector ], hasText( expectedText ) )

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

	@Override
	JFrame getJFrame( ) { jFrame }
}
