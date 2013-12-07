package com.athaydes.automaton.samples;

import com.athaydes.automaton.SwingUtil;
import com.athaydes.automaton.Swinger;
import com.athaydes.automaton.selector.AutomatonSelector;
import com.athaydes.automaton.selector.SimpleSwingerSelector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.athaydes.automaton.SwingUtil.collectNodes;
import static com.athaydes.automaton.selector.StringSwingerSelectors.matchingAll;
import static com.athaydes.automaton.selector.StringSwingerSelectors.matchingAny;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * User: Renato
 */
public class SwingerSample {

    @BeforeClass
    public static void startup() {
        SwingJavaFXSampleAppTest.setup();
    }

    @AfterClass
    public static void cleanup() throws Exception {

        // notice here we find the Component to click by its text, not by its name
        Swinger.forSwingWindow()
                .clickOn( "text:File" ).pause( 250 )
                .clickOn( "text:Exit" );

        SwingJavaFXSampleAppTest.cleanup();
    }

    @Test
    public void testingAnApplicationWithoutHavingTheFrame() {
        Swinger swinger = Swinger.forSwingWindow();

        // click on the Component with name 'text-area'
        swinger.clickOn( "text-area" );

        // clean the text area
        for ( int i = 0; i < 25; i++ ) {
            swinger.type( KeyEvent.VK_BACK_SPACE );
        }

        swinger.type( "This is a Swing test" );

        // Lookup the TextArea by class
        JTextArea textArea = swinger.getAt( JTextArea.class );

        assertThat( textArea.getText(), is( "This is a Swing test" ) );
    }

    @Test
    public void extendingSwinger() {
        Swinger swinger = Swinger.forSwingWindow();

        swinger.setSelectors( createCustomSelectors() );

        // click on the Component with name 'text-area', turning all chars lower-case with the custom selector
        swinger.clickOn( "cust:Text-AreA" );

        // clean the text area
        for ( int i = 0; i < 25; i++ ) {
            swinger.type( KeyEvent.VK_BACK_SPACE );
        }

        swinger.type( "Using a custom selector" );

        // attempt to locate any Swing Component with the given text
        Component areaWithTypedText = swinger.getAt( "text:Using a custom selector" );

        assertThat( areaWithTypedText, instanceOf( JTextArea.class ) );
    }

    private Map<String, AutomatonSelector<Component>> createCustomSelectors() {

        // we must use a sorted map as the first entry in the map is used if no prefix is given
        // eg. in this case, click( "cust:field" ) would be the same as click( "field" )
        Map<String, AutomatonSelector<Component>> customSelectors = new LinkedHashMap<>();
        customSelectors.put( "cust:", new SimpleSwingerSelector() {
            @Override
            public boolean matches( String selector, Component component ) {
                // this custom selector does almost exactly the same thing as Automaton's default selector
                // except that it disregards case before looking up by name
                String compName = component.getName() == null ? "" : component.getName();
                return selector.compareToIgnoreCase( compName ) == 0;
            }
        } );

        // a custom selector to find things by ID
        customSelectors.put( "$", new SimpleSwingerSelector() {
            @Override
            public boolean matches( String selector, Component component ) {
                return SwingUtil.callMethodIfExists( component, "getId" ).equals( selector );
            }
        } );

        // it is always good to keep Automaton's default selectors active
        customSelectors.putAll( Swinger.getDEFAULT_SELECTORS() );
        return customSelectors;
    }

    @Test
    public void exploringJTreeItems() {
        Swinger swinger = Swinger.forSwingWindow();

        swinger.doubleClickOn( "text:colors" ).pause( 250 )
                .clickOn( "text:blue" )
                .doubleClickOn( "text:sports" )
                .doubleClickOn( "text:food" );

        // safer way explore trees by path (limits search space to the given tree)
        JTree tree = swinger.getAt( JTree.class );

        swinger.clickOn( collectNodes( tree, "colors", "blue" ) )
                .doubleClickOn( collectNodes( tree, "sports" ) )
                .doubleClickOn( collectNodes( tree, "food" ) );
    }

    @Test
    public void usingComplexSelectors() {
        Swinger swinger = Swinger.forSwingWindow();

        // will throw an Exception if nothing is found
        swinger.getAt( matchingAll( "type:JTree", "name:mboxTree" ) );

        List<Component> nodes = swinger.getAll( matchingAny( "text:colors", "text:sports" ) );
        assertThat( nodes.size(), is( 2 ) );


        swinger.clickOn( matchingAll( "type:JComboBox", "name:combo" ) );
    }

}
