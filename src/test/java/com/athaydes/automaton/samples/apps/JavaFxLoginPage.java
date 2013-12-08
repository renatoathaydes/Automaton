package com.athaydes.automaton.samples.apps;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import static com.athaydes.automaton.samples.apps.LoginResult.*;

/**
 * @author Renato
 */
public class JavaFxLoginPage extends Application {

    @Override
    public void start( Stage stage ) throws Exception {
        GridPane mainBox = new GridPane();
        mainBox.setId( "login-panel" );
        mainBox.setHgap( 10 );
        mainBox.setVgap( 5 );

        Label userTypeLabel = new Label( "User type:" );

        ComboBox<String> userTypeCombo = new ComboBox<String>();
        userTypeCombo.setId( "combo" );
        userTypeCombo.getItems().setAll( "Admin", "Users", "Managers" );
        userTypeCombo.getSelectionModel().select( 0 );

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
        loginButton.setStyle( "-fx-text-fill: blue;" );
        loginButton.setId( "login-button" );
        loginButton.setOnAction( new EventHandler<ActionEvent>() {
            @Override
            public void handle( ActionEvent actionEvent ) {
                LoginResult result = login( userNameField.getText(),
                        passwordField.getText() );
                messageLabel.setText( result.getMessage() );
                messageLabel.setTextFill(
                        result == LOGIN_OK ? Color.GREEN : Color.RED );
            }
        } );

        GridPane.setConstraints( userTypeLabel, 1, 1 );
        GridPane.setConstraints( userTypeCombo, 2, 1 );
        GridPane.setConstraints( infoLabel, 1, 2 );
        GridPane.setConstraints( userNameLabel, 1, 3 );
        GridPane.setConstraints( userNameField, 2, 3 );
        GridPane.setConstraints( passwordLabel, 1, 4 );
        GridPane.setConstraints( passwordField, 2, 4 );
        GridPane.setConstraints( loginButton, 1, 5, 2, 1 );
        GridPane.setConstraints( messageLabel, 1, 6, 2, 1 );

        mainBox.getChildren().setAll(
                userTypeLabel, userTypeCombo,
                infoLabel,
                userNameLabel, userNameField,
                passwordLabel, passwordField,
                loginButton,
                messageLabel );

        Scene scene = new Scene( mainBox, 400, 180 );
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
