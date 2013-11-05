package com.athaydes.automaton.samples;

import com.athaydes.automaton.SwingUtil;
import com.athaydes.automaton.SwingerFxer;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static com.athaydes.automaton.SwingUtil.lookup;
import static junit.framework.Assert.assertEquals;

/**
 * User: Renato
 */
public class SwingJavaFXSampleAppTestInJava {

    static SwingJavaFXSampleAppTest swingJavaFx = new SwingJavaFXSampleAppTest();

    @BeforeClass
    public static void setup() throws Exception {
        swingJavaFx.setup();
    }

    @AfterClass
    public static void cleanup() {
        swingJavaFx.cleanup();
    }

    @Test
    public void javaCodeCanAlsoDriveSwingJavaFxApps() {
        JFrame frame = swingJavaFx.getjFrame();
        JFXPanel fxPanel = swingJavaFx.getJfxPanel();
        String swingTextAreaText = "Hello, I am Swing...";
        String fxInputText = "Hello, JavaFX...";

        SwingerFxer.userWith( frame, fxPanel.getScene().getRoot() )
                .doubleClickOn( "text:colors" )
                .clickOn( "text-area" )
                .type( swingTextAreaText ).pause( 1000 )
                .clickOn( "#left-color-picker" ).pause( 1000 )
                .moveBy( 60, 40 ).click().pause( 1000 )
                .clickOn( "#fx-input" )
                .type( fxInputText )
                .moveBy( 100, 0 ).pause( 500 );

        JTextArea jTextArea = ( JTextArea ) lookup( "text-area", frame );
        TextField textField = ( TextField ) fxPanel.getScene().lookup( "#fx-input" );
        ColorPicker leftPicker = ( ColorPicker ) fxPanel.getScene().lookup( "#left-color-picker" );

        assertEquals( swingTextAreaText, jTextArea.getText() );
        assertEquals( fxInputText, textField.getText() );
        assertEquals( leftPicker.getValue(), swingJavaFx.getTextLeftColor() );
    }

    @Test
    public void canOpenNodesInJTrees() {
        JFrame frame = swingJavaFx.getjFrame();
        JFXPanel fxPanel = swingJavaFx.getJfxPanel();

        JTree tree = ( JTree ) SwingUtil.lookup( "mboxTree", frame );
        List<Component> nodes = SwingUtil.collectNodes( tree, Arrays.asList( "colors", "red" ) );

        SwingerFxer swingerFxer = SwingerFxer.userWith( frame, fxPanel.getScene().getRoot() );

        for ( Component node : nodes ) {
            swingerFxer.doubleClickOn( node ).pause( 2000 );
        }

    }

}
