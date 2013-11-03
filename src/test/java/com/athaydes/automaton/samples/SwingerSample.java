package com.athaydes.automaton.samples;

import com.athaydes.automaton.SwingUtil;
import com.athaydes.automaton.Swinger;
import com.athaydes.automaton.SwingerSelector;
import groovy.lang.Closure;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        // Lookup the TextArea using Automaton's SwingUtil class
        JTextArea textArea = ( JTextArea ) SwingUtil.lookup( "text-area", swinger.getComponent() );

        assertThat( textArea.getText(), is( "This is a Swing test" ) );
    }

    @Test
    public void extendingSwinger() {
        Swinger swinger = Swinger.forSwingWindow();

        swinger.setSpecialPrefixes( createCustomSelectors() );

        // click on the Component with name 'text-area', turning all chars lower-case with the custom selector
        swinger.clickOn( "cust:Text-AreA" );

        // clean the text area
        for ( int i = 0; i < 25; i++ ) {
            swinger.type( KeyEvent.VK_BACK_SPACE );
        }

        swinger.type( "Using a custom selector" );

        // attempt to locate any Swing Component with the given text
        Object areaWithTypedText = SwingUtil.text( "Using a custom selector", swinger.getComponent() );

        assertThat( areaWithTypedText, instanceOf( JTextArea.class ) );
    }

    private Map<String, Closure<Component>> createCustomSelectors() {

        // we must use a sorted map as the first entry in the map is used if no prefix is given
        // eg. in this case, click( "cust:field" ) would be the same as click( "field" )
        Map<String, Closure<Component>> customSelectors = new LinkedHashMap<String, Closure<Component>>();
        customSelectors.put( "cust:", new SwingerSelector( this ) {
            @Override
            public Component call( String selector, Component component ) {
                // this custom selector does almost exactly the same thing as Automaton's default selector
                // except that it turns all characters lower-case before looking up by name
                return SwingUtil.lookup( selector.toLowerCase(), component );
            }
        } );

        // a custom selector to find things by ID
        customSelectors.put( "$", new SwingerSelector( this ) {
            List<Component> found = new ArrayList<Component>( 1 );

            @Override
            public Component call( final String selector, Component component ) {
                SwingUtil.navigateBreadthFirst( component, new Closure( this ) {
                    @Override
                    public Object call( Object... args ) {
                        if ( SwingUtil.callMethodIfExists( args[ 0 ], "getId" ).equals( selector ) ) {
                            found.add( ( Component ) args[ 0 ] );
                        }
                        return !found.isEmpty();
                    }
                } );
                return found.isEmpty() ? null : found.get( 0 );
            }
        } );

        // it is always good to keep Automaton's default selectors active
        customSelectors.putAll( Swinger.getDEFAULT_PREFIX_MAP() );
        return customSelectors;
    }

    @Test
    public void exploringJTreeItems() {
        Swinger swinger = Swinger.forSwingWindow();
        swinger.doubleClickOn( "text:colors" ).pause( 250 )
                .clickOn( "text:blue" )
                .doubleClickOn( "text:sports" )
                .doubleClickOn( "text:food" );
    }


}
