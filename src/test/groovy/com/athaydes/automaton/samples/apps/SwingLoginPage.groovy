package com.athaydes.automaton.samples.apps

import com.athaydes.automaton.HasSwingCode
import com.athaydes.automaton.mixins.SwingTestHelper
import groovy.swing.SwingBuilder

import javax.swing.*
import java.awt.*

import static com.athaydes.automaton.samples.apps.LoginResult.*
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE

/**
 * @author Renato
 */
@Mixin( SwingTestHelper )
class SwingLoginPage implements HasSwingCode {

	JFrame jFrame

	SwingLoginPage( ) {
		JTextField userNameField = null
		JPasswordField userPassword = null
		JLabel messageLabel = null
		new SwingBuilder().edt {
			jFrame = frame( title: 'Swing Frame', size: [ 600, 180 ] as Dimension,
					defaultCloseOperation: DISPOSE_ON_CLOSE, show: true ) {
				gridLayout( rows: 5, cols: 2, hgap: 55, vgap: 10 )
				label( 'User Type:' )
				comboBox( name: 'combo', items: [ 'Admin', 'Users', 'Managers' ] )
				label( 'UserName:' )
				userNameField = textField()
				label( 'Password:' )
				userPassword = passwordField()
				button( name: 'ok', text: 'Login', actionPerformed: { e ->
					LoginResult result = login( userNameField.text,
							userPassword.password.toString() )
					messageLabel.text = result.message
					messageLabel.foreground = ( result == LOGIN_OK ?
						Color.BLACK : Color.RED )
				} )
				label()
				messageLabel = label( name: 'message-area' )
				label()
			}
		}
		waitForJFrameToShowUp()
	}

	LoginResult login( String username, String password ) {
		if ( username.trim().isEmpty() )
			return NO_USER_NAME
		if ( password.trim().isEmpty() )
			return NO_PASSWORD

		println "Passoword: $password"
		def credentialsOk = ( "automaton" == username &&
				"password" == password )

		if ( credentialsOk )
			return LOGIN_OK;
		else
			return BAD_CREDENTIALS
	}

	@Override
	JFrame getJFrame( ) {
		jFrame
	}

	void destroy( ) {
		jFrame?.dispose()
	}

	static main( args ) {
		new SwingLoginPage()
	}

}

enum LoginResult {
	LOGIN_OK( "You are logged in!" ),
	BAD_CREDENTIALS( "Bad credentials" ),
	NO_USER_NAME( "Please enter your user name" ),
	NO_PASSWORD( "Please enter your password" );

	final String message;

	LoginResult( String message ) {
		this.message = message;
	}

	public String getMessage( ) { message }

}
