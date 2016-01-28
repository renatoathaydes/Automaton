import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

public class DemoUI {

    private static void initAndShowGUI() {
        final JFrame frame = new JFrame( "Swing and JavaFX" );

        final List<Runnable> cleanFieldsCallbacks = new ArrayList<>();

        Container container = frame.getContentPane();
        final JFXPanel fxPanel = new JFXPanel();
        JPanel swingNorth = createSwingNorthPanel( cleanFieldsCallbacks );

        container.add( swingNorth, BorderLayout.NORTH );
        container.add( fxPanel, BorderLayout.CENTER );
        container.add( createSwingSouthPanel(), BorderLayout.SOUTH );

        frame.setSize( 500, 300 );
        frame.setLocationRelativeTo( null );
        frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
        frame.setVisible( true );

        Platform.runLater( new Runnable() {
            @Override
            public void run() {
                Scene scene = createScene( cleanFieldsCallbacks );
                fxPanel.setScene( scene );
            }
        } );
    }

    private static JComponent createSwingSouthPanel() {
        JPanel southPanel = new JPanel( new FlowLayout( FlowLayout.CENTER, 10, 5 ) );
        southPanel.add( new JLabel( "<html><em>Developed by " +
                "<a href='https://github.com/renatoathaydes'>Renato Athaydes</a></em>, 2016" ) );
        return southPanel;
    }

    private static JPanel createSwingNorthPanel( List<Runnable> cleanFieldsCallbacks ) {
        JPanel swingNorth = new JPanel( new FlowLayout( FlowLayout.LEFT, 5, 10 ) );
        swingNorth.add( new JLabel( "Please enter something:" ) );
        final JTextField swingText = new JTextField();
        swingText.setName( "swing-enter-something-field" );
        swingText.setPreferredSize( new Dimension( 200, 30 ) );

        cleanFieldsCallbacks.add( new Runnable() {
            @Override
            public void run() {
                if ( SwingUtilities.isEventDispatchThread() ) {
                    swingText.setText( "" );
                } else {
                    SwingUtilities.invokeLater( this );
                }
            }
        } );

        swingNorth.add( swingText );
        return swingNorth;
    }

    private static Scene createScene( final List<Runnable> cleanFieldsCallbacks ) {
        Group root = new Group();
        Scene scene = new Scene( root, Color.ALICEBLUE );

        Text text = new Text();
        text.setFill( new LinearGradient( 0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop( 0, Color.RED ), new Stop( 1, Color.DARKRED ) ) );
        text.setX( 50 );
        text.setY( 30 );
        text.setFont( new Font( "Arial Black", 25 ) );
        text.setText( "A JavaFX Demo inside Swing!" );


        HBox fxForm = new HBox( 5 );
        fxForm.setLayoutX( 15 );
        fxForm.setLayoutY( 50 );
        fxForm.getChildren().add( new Label( "Enter something here also:" ) );

        final TextField fxField = new TextField();
        fxField.setId( "fx-enter-something-field" );
        fxForm.getChildren().add( fxField );

        cleanFieldsCallbacks.add( new Runnable() {
            @Override
            public void run() {
                if ( Platform.isFxApplicationThread() ) {
                    fxField.setText( "" );
                } else {
                    Platform.runLater( this );
                }
            }
        } );

        Button cleanFieldsButton = new Button( "Clean text fields", new Rectangle( 12, 12, Color.RED ) );
        cleanFieldsButton.setId( "clean-fields-button" );
        cleanFieldsButton.setLayoutX( 15 );
        cleanFieldsButton.setLayoutY( 100 );
        cleanFieldsButton.setOnMouseClicked( new EventHandler<MouseEvent>() {
            @Override
            public void handle( MouseEvent event ) {
                for (Runnable clean : cleanFieldsCallbacks) clean.run();
            }
        } );

        root.getChildren().addAll( text, fxForm, cleanFieldsButton );

        return scene;
    }

    public static void main( String[] args ) {
        SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run() {
                initAndShowGUI();
            }
        } );
    }
}