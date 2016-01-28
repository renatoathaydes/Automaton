def cleanup = { clickOn '#clean-fields-button' }

// ensure the UI is cleaned before and after the script runs
cleanup()

try {

	clickOn 'swing-enter-something-field'
	type 'Typing on a Swing text field'

	clickOn '#fx-enter-something-field'

	// must use enterText to type special characters
	enterText 'javafx@email.com'
	waitForFxEvents()

	// mixed JavaFX/Swing applications may use the mixed driver, sfxer
	assertThat sfxer[ 'swing-enter-something-field' ], hasText( 'Typing on a Swing text field' )
	assertThat sfxer[ '#fx-enter-something-field' ], hasText( 'javafx@email.com' )

	cleanup()

	assertThat sfxer[ 'swing-enter-something-field' ], hasText( '' )
	assertThat sfxer[ '#fx-enter-something-field' ], hasText( '' )

} finally {
	cleanup()
}
