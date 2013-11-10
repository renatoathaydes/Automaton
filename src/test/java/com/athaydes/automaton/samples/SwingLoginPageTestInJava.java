package com.athaydes.automaton.samples;

import com.athaydes.automaton.Swinger;
import com.athaydes.automaton.samples.apps.SwingLoginPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.athaydes.automaton.assertion.AutomatonMatcher.hasText;
import static org.junit.Assert.assertThat;

/**
 * @author Renato
 */
public class SwingLoginPageTestInJava {

    SwingLoginPage loginPage;

    @Before
    public void before() {
        loginPage = new SwingLoginPage();
    }

    @After
    public void after() {
        loginPage.destroy();
    }

    @Test
    public void testNoUserNameLogin() {
        Swinger swinger = Swinger.getUserWith( loginPage.getJFrame() );

        swinger.clickOn( "combo" )
                .clickOn( "text:Managers" )
                .clickOn( "type:JPasswordField" )
                .type( "password" )
                .clickOn( "ok" ).pause( 250 );

        assertThat( swinger.getAt( "message-area" ), hasText( "Please enter your user name" ) );
    }

}
