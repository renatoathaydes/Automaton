package com.athaydes.automaton.samples;

import com.athaydes.automaton.FXer;
import com.athaydes.automaton.SwingUtil;
import com.athaydes.automaton.Swinger;
import com.athaydes.automaton.SwingerFxer;
import com.athaydes.automaton.selector.AutomatonSelector;
import com.athaydes.automaton.selector.SimpleFxSelector;
import com.athaydes.automaton.selector.SimpleSwingerSelector;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.athaydes.automaton.assertion.AutomatonMatcher.hasText;
import static com.athaydes.automaton.assertion.AutomatonMatcher.hasValue;
import static com.athaydes.automaton.samples.SwingJavaFXSampleAppTest.getJfxPanel;
import static com.athaydes.automaton.samples.SwingJavaFXSampleAppTest.getjFrame;
import static org.junit.Assert.assertThat;

/**
 * User: Renato
 */
public class SwingJavaFXSampleAppTestInJava {

    static SwingJavaFXSampleAppTest swingJavaFx = new SwingJavaFXSampleAppTest();

    @BeforeClass
    public static void setup() throws Exception {
        SwingJavaFXSampleAppTest.setup();
    }

    @AfterClass
    public static void cleanup() {
        SwingJavaFXSampleAppTest.cleanup();
    }

    @Test
    public void javaCodeCanAlsoDriveSwingJavaFxApps() {
        JFrame frame = getjFrame();
        JFXPanel fxPanel = getJfxPanel();
        String swingTextAreaText = "Hello, I am Swing...";
        String fxInputText = "Hello, JavaFX...";

        SwingerFxer swfx = SwingerFxer.getUserWith( frame, fxPanel.getScene().getRoot() );

        swfx.doubleClickOn( "text:colors" )
                .clickOn( "text-area" )
                .type( swingTextAreaText ).pause( 1000 )
                .clickOn( "#left-color-picker" ).pause( 1000 )
                .moveBy( 60, 40 ).click().pause( 1000 )
                .clickOn( "#fx-input" )
                .type( fxInputText )
                .moveBy( 100, 0 ).pause( 500 );

        assertThat( swfx.getAt( "text-area" ), hasText( swingTextAreaText ) );
        assertThat( swfx.getAt( "#fx-input" ), hasText( fxInputText ) );
        assertThat( swfx.getAt( "#left-color-picker" ), hasValue( swingJavaFx.getTextLeftColor() ) );
    }

    @Test
    public void canOpenNodesInJTrees() {
        JFrame frame = getjFrame();
        JFXPanel fxPanel = getJfxPanel();

        JTree tree = ( JTree ) Swinger.forSwingWindow().getAt( "mboxTree" );
        List<Component> nodes = SwingUtil.collectNodes( tree, "colors", "red" );

        SwingerFxer swingerFxer = SwingerFxer.getUserWith( frame, fxPanel.getScene().getRoot() );

        for ( Component node : nodes ) {
            swingerFxer.doubleClickOn( node ).pause( 2000 );
        }

    }

    @Test
    public void canHaveCustomSelectorsForSwingAndJavaFX() {
        JFrame frame = getjFrame();
        JFXPanel fxPanel = getJfxPanel();

        String swingTextAreaText = "Hello, I am Swing...";
        String fxInputText = "Hello, JavaFX...";

        SwingerFxer swfx = SwingerFxer.getUserWith( frame, fxPanel.getScene().getRoot() );

        swfx.setSwingSelectors( createSwingSelectors() );
        swfx.setJavaFxSelectors( createJavaFxSelectors() );

        swfx.doubleClickOn( "text:colors" )
                .clickOn( "$text-area" )
                .type( swingTextAreaText ).pause( 1000 )
                .clickOn( "$left-color-picker" ).pause( 1000 )
                .moveBy( 60, 40 ).click().pause( 1000 )
                .clickOn( "$fx-input" )
                .type( fxInputText )
                .moveBy( 100, 0 ).pause( 500 );

        assertThat( swfx.getAt( "$text-area" ), hasText( swingTextAreaText ) );
        assertThat( swfx.getAt( "$fx-input" ), hasText( fxInputText ) );
        assertThat( swfx.getAt( "$left-color-picker" ), hasValue( swingJavaFx.getTextLeftColor() ) );

    }

    /**
     * In this example, we create a custom selector which allows users to select JavaFX Nodes
     * by ID and Swing Components by Name using the same prefix $.
     *
     * @return custom selectors
     */
    private Map<String, AutomatonSelector<Component>> createSwingSelectors() {
        Map<String, AutomatonSelector<Component>> customSelectors = new LinkedHashMap<>();
        customSelectors.put( "$", new SimpleSwingerSelector() {
            @Override
            public boolean matches( String selector, Component component ) {
                return Objects.equals( component.getName(), selector );
            }
        } );
        customSelectors.putAll( Swinger.getDEFAULT_SELECTORS() );
        return customSelectors;
    }

    private Map<String, AutomatonSelector<Node>> createJavaFxSelectors() {
        Map<String, AutomatonSelector<Node>> customSelectors = new LinkedHashMap<>();
        customSelectors.put( "$", new SimpleFxSelector() {
            @Override
            public boolean matches( String selector, Node node ) {
                return Objects.equals( node.getId(), selector );
            }
        } );
        customSelectors.putAll( FXer.getDEFAULT_SELECTORS() );
        return customSelectors;
    }

}
