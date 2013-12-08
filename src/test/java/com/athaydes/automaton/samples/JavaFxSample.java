package com.athaydes.automaton.samples;

import com.athaydes.automaton.FXApp;
import com.athaydes.automaton.FXer;
import com.athaydes.automaton.samples.apps.JavaFxLoginPage;
import com.athaydes.automaton.selector.AutomatonSelector;
import com.athaydes.automaton.selector.SimpleFxSelector;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.athaydes.automaton.assertion.AutomatonMatcher.hasText;
import static com.athaydes.automaton.samples.apps.LoginResult.LOGIN_OK;
import static com.athaydes.automaton.samples.apps.LoginResult.NO_PASSWORD;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Renato
 */
public class JavaFxSample {

    FXer fxer;

    @Before
    public void setup() throws Exception {
        FXApp.startApp( new JavaFxLoginPage() );
        Thread.sleep( 500 );
        fxer = FXer.getUserWith( FXApp.getScene().getRoot() );
    }

    @Test
    public void cannotLoginWithoutEnteringPassword() {
        fxer.clickOn( fxer.getAt( TextField.class ) )
                .type( "Automaton" )
                .clickOn( "#login-button" )
                .waitForFxEvents();

        assertThat( fxer.getAt( "#message-area" ), hasText( NO_PASSWORD.getMessage() ) );
    }

    @Test
    public void canLoginIfCredentialsGivenAreCorrect() {
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

    @Test
    public void canUseCustomSelector() {
        Map<String, AutomatonSelector<Node>> customSelectors = new HashMap<>();

        customSelectors.put( "$", new SimpleFxSelector() {
            @Override
            public boolean followPopups() {
                return false;
            }

            @Override
            public boolean matches( String selector, Node node ) {
                return node.getStyle().contains( selector );
            }
        } );
        customSelectors.putAll( FXer.getDEFAULT_SELECTORS() );

        fxer.setSelectors( customSelectors );

        Node node = fxer.getAt( "$blue" );
        assertThat( node.getStyle().contains( "blue" ), is( true ) );
    }

    @Test
    public void startingCustomNodes() {
        final Stage stage = FXApp.initialize();
        FXApp.doInFXThreadBlocking( new Runnable() {
            public void run() {
                stage.getScene().setRoot( new VBox() );
            }
        } );
    }

    public static void main( String[] args ) {
        JavaFxLoginPage app = new JavaFxLoginPage();
        FXApp.startApp( app, args );
    }


}
