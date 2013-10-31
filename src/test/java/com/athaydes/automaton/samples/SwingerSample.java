package com.athaydes.automaton.samples;

import com.athaydes.automaton.SwingUtil;
import com.athaydes.automaton.Swinger;
import groovy.lang.Closure;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * User: Renato
 */
public class SwingerSample {

    static SwingJavaFXSampleAppTest swingJavaFx = new SwingJavaFXSampleAppTest();

    @BeforeClass
    public static void startup() {
        swingJavaFx.setup();
    }

    @AfterClass
    public static void cleanup() throws Exception {

        // notice here we find the Component to click by its text, not by its name
        Swinger.forSwingWindow()
                .clickOn( "text:File" )
                .clickOn( "text:Exit" );
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

        swinger.clickOn( "text-area" );

        // clean the text area
        for ( int i = 0; i < 25; i++ ) {
            swinger.type( KeyEvent.VK_BACK_SPACE );
        }

        swinger.type( "Using a custom selector" );

        // Lookup the TextArea using Automaton's SwingUtil class
        JTextArea textArea = ( JTextArea ) SwingUtil.lookup( "text-area", swinger.getComponent() );

        assertThat( textArea.getText(), is( "Using a custom selector" ) );
    }

    private Map<String, Closure> createCustomSelectors() {

        // we must use a sorted map as the first entry is used if no prefix is given
        Map<String, Closure> customSelectors = new LinkedHashMap<String, Closure>();
        customSelectors.put( "cust:", new Closure( this ) {
            @Override
            public Object call( Object... args ) {
                // this custom selector does exactly the same thing as Automaton's default selector
                String selector = ( String ) args[ 0 ];
                Component component = ( Component ) args[ 1 ];
                return SwingUtil.lookup( selector, component );
            }
        } );

        // it is always good to keep Automaton's default selectors active
        customSelectors.putAll( Swinger.getDEFAULT_PREFIX_MAP() );
        return customSelectors;
    }

}
