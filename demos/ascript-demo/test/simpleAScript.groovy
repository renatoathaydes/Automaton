// we can click on a Swing Component
clickOn 'swing-enter-something-field'
type 'Hi Swing'

// ... and on a JavaFX Node!
clickOn '#fx-enter-something-field'
type 'Hello JavaFX'

assertThat swinger[ 'swing-enter-something-field' ], hasText( 'Hi Swing' )
assertThat fxer[ '#fx-enter-something-field' ], hasText( 'Hello JavaFX' )
