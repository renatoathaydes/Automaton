package com.athaydes.automaton.samples

import com.athaydes.automaton.Speed
import com.athaydes.automaton.Swinger
import org.junit.After
import org.junit.Before
import org.junit.Test

import javax.swing.*
import java.awt.*

class JListSample {

    JFrame frame

    @Before
    void before() {
        frame = new JFrame( 'JList Test' )
        frame.layout = new FlowLayout()

        String[] listItems = [ 'green', 'red', 'orange', 'dark blue', 'yellow', 'black', 'mauve', 'brown', 'purple' ]
        JList list = new JList( listItems )
        list.layoutOrientation = JList.VERTICAL_WRAP
        list.visibleRowCount = 3

        frame.add( new JScrollPane( list ) )
        frame.pack()

        frame.visible = true
    }

    @After
    void after() {
        frame?.dispose()
    }

    @Test
    void "List items on a multi-column JList can be clicked on using a text selector"() {
        // Use the swinger to click on JList cells in each column
        Swinger swing = Swinger.forSwingWindow()

        swing.clickOn( 'text:black', Speed.VERY_FAST ).pause( 250 )
                .clickOn( 'text:green', Speed.VERY_FAST ).pause( 250 )
                .clickOn( 'text:dark blue', Speed.VERY_FAST ).pause( 250 )
                .clickOn( 'text:brown', Speed.VERY_FAST ).pause( 250 )
                .clickOn( 'text:red', Speed.VERY_FAST ).pause( 250 )
                .clickOn( 'text:purple', Speed.VERY_FAST )
    }

}
