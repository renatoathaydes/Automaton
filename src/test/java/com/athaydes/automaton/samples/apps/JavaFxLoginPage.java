package com.athaydes.automaton.samples.apps;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import static com.athaydes.automaton.samples.apps.JavaFxLoginPage.LoginResult.*;

/**
 * @author Renato
 */
public class JavaFxLoginPage extends Application {


    static enum LoginResult {
        LOGIN_OK( "You are logged in!" ),
        BAD_CREDENTIALS( "Bad credentials" ),
        NO_USER_NAME( "Please enter your user name" ),
        NO_PASSWORD( "Please enter your password" );

        final String message;

        LoginResult( String message ) {
            this.message = message;
        }
    }


    @Override
    public void start( Stage stage ) throws Exception {
        GridPane mainBox = new GridPane();
        mainBox.setId( "login-panel" );
        mainBox.setHgap( 10 );
        mainBox.setVgap( 5 );

        Label infoLabel = new Label( "Enter your credentials:" );

        Label userNameLabel = new Label( "UserName:" );
        final TextField userNameField = new TextField();
        userNameField.setId( "user-name" );

        Label passwordLabel = new Label( "Password:" );
        final PasswordField passwordField = new PasswordField();
        passwordField.setId( "user-password" );

        final Label messageLabel = new Label();
        messageLabel.setId( "message-area" );

        Button loginButton = new Button( "Login" );
        loginButton.setId( "login-button" );
        loginButton.setOnAction( new EventHandler<ActionEvent>() {
            @Override
            public void handle( ActionEvent actionEvent ) {
                LoginResult result = login( userNameField.getText(),
                        passwordField.getText() );
                messageLabel.setText( result.message );
                messageLabel.setTextFill(
                        result == LOGIN_OK ? Color.GREEN : Color.RED );
            }
        } );

        GridPane.setConstraints( infoLabel, 1, 1 );
        GridPane.setConstraints( userNameLabel, 1, 2 );
        GridPane.setConstraints( userNameField, 2, 2 );
        GridPane.setConstraints( passwordLabel, 1, 3 );
        GridPane.setConstraints( passwordField, 2, 3 );
        GridPane.setConstraints( loginButton, 1, 4, 2, 1 );
        GridPane.setConstraints( messageLabel, 1, 5, 2, 1 );

        mainBox.getChildren().setAll(
                infoLabel,
                userNameLabel, userNameField,
                passwordLabel, passwordField,
                loginButton,
                messageLabel );

        Scene scene = new Scene( mainBox, 400, 150 );
        stage.setScene( scene );
        stage.show();
    }

    private static LoginResult login( String username, String password ) {
        if ( username.trim().isEmpty() )
            return NO_USER_NAME;
        if ( password.trim().isEmpty() )
            return NO_PASSWORD;

        boolean credentialsOk = ( "automaton".equals( username ) &&
                "password".equals( password ) );

        if ( credentialsOk )
            return LOGIN_OK;
        else
            return BAD_CREDENTIALS;
    }

}
