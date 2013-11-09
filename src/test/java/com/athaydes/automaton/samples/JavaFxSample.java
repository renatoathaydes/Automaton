package com.athaydes.automaton.samples;

import com.athaydes.automaton.FXApp;
import com.athaydes.automaton.FXer;
import com.athaydes.automaton.samples.apps.JavaFxLoginPage;
import javafx.scene.control.TextField;
import org.junit.Before;
import org.junit.Test;

import static com.athaydes.automaton.assertion.AutomatonMatcher.hasText;
import static org.junit.Assert.assertThat;

/**
 * @author Renato
 */
public class JavaFxSample {

    @Before
    public void setup() {
        FXApp.startApp( new JavaFxLoginPage() );
    }

    @Test
    public void testLogin() {
        FXer fxer = FXer.getUserWith( FXApp.getScene().getRoot() );

        fxer.clickOn( fxer.getAt( TextField.class ) )
                .type( "Automaton" )
                .clickOn( "#login-button" );

        assertThat( fxer.getAt( "#message-area" ), hasText( "Please enter your password" ) );
    }

    public static void main( String[] args ) {
        JavaFxLoginPage app = new JavaFxLoginPage();
        FXApp.startApp( app, args );
    }


}
