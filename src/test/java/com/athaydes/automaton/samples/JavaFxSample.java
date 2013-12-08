package com.athaydes.automaton.samples;

import com.athaydes.automaton.FXApp;
import com.athaydes.automaton.FXer;
import com.athaydes.automaton.samples.apps.JavaFxLoginPage;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.Before;
import org.junit.Test;

import static com.athaydes.automaton.assertion.AutomatonMatcher.hasText;
import static com.athaydes.automaton.samples.apps.LoginResult.LOGIN_OK;
import static com.athaydes.automaton.samples.apps.LoginResult.NO_PASSWORD;
import static org.junit.Assert.assertThat;

/**
 * @author Renato
 */
public class JavaFxSample {

    @Before
    public void setup() throws Exception {
        FXApp.startApp( new JavaFxLoginPage() );
        Thread.sleep( 500 );
    }

    @Test
    public void cannotLoginWithoutEnteringPassword() {
        FXer fxer = FXer.getUserWith( FXApp.getScene().getRoot() );

        fxer.clickOn( fxer.getAt( TextField.class ) )
                .type( "Automaton" )
                .clickOn( "#login-button" )
                .waitForFxEvents();

        assertThat( fxer.getAt( "#message-area" ), hasText( NO_PASSWORD.getMessage() ) );
    }

    @Test
    public void canLoginIfCredentialsGivenAreCorrect() {
        FXer fxer = FXer.getUserWith( FXApp.getScene().getRoot() );

        fxer.clickOn( "#combo" ).waitForFxEvents()
                .doubleClickOn( "text:Users" ).waitForFxEvents()
                .clickOn( TextField.class )
                .type( "automaton" )
                .clickOn( PasswordField.class )
                .type( "password" )
                .clickOn( "text:Login" )
                .waitForFxEvents();

        assertThat( fxer.getAt( "#message-area" ), hasText( LOGIN_OK.getMessage() ) );
    }

    public static void main( String[] args ) {
        JavaFxLoginPage app = new JavaFxLoginPage();
        FXApp.startApp( app, args );
    }


}
