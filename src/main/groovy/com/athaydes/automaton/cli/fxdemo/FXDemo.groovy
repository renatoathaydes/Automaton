package com.athaydes.automaton.cli.fxdemo

import com.athaydes.automaton.cli.AutomatonDemo
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane

import static java.util.Collections.reverse

/**
 *
 */
class FXDemo extends BorderPane {

    @FXML
    TextArea selectionStatus
    @FXML
    TextArea scriptText
    @FXML
    TextArea logOutput
    @FXML
    TreeView<String> treeView
    @FXML
    Button runButton

    @FXML
    void initialize() {
        def asTreeItem = { new TreeItem<String>( it as String ) }
        def colors = asTreeItem( 'colors' )
        colors.children.addAll( [ 'blue', 'violet', 'red', 'yellow' ].collect( asTreeItem ) )
        def sports = asTreeItem( 'sports' )
        sports.children.addAll( [ 'basketball', 'soccer', 'football', 'hockey' ].collect( asTreeItem ) )
        def food = asTreeItem( 'food' )
        food.children.addAll( [ 'hot dogs', 'pizza', 'ravioli', 'bananas' ].collect( asTreeItem ) )
        def root = asTreeItem( '' )
        root.children.addAll( colors, sports, food )
        treeView.root = root
        treeView.showRoot = false
        treeView.setOnMouseClicked( new EventHandler<MouseEvent>() {
            @Override
            void handle( MouseEvent mouseEvent ) {
                def selection = treeView.selectionModel.selectedItem
                if ( selection ) {
                    def nodes = [ selection.value ]
                    if ( selection.parent.value ) {
                        nodes += selection.parent.value
                    }
                    reverse( nodes )
                    selectionStatus.text = "You selected ${nodes}"
                }
            }
        } )

        scriptText.text = initialScript()
    }


    void runScript() {
        runButton.disabled = true
        AutomatonDemo.runScript( scriptText.text,
                [ write: { s -> logOutput.appendText( s.toString() ) } ] ) {
            runButton.disabled = false
        }
    }

    void clearOutput() {
        logOutput.clear()
    }

    public void close() {
        Platform.exit()
    }

    String initialScript() {
        '''\
		|doubleClickOn 'text:colors'
		|pause 500
		|clickOn 'text:yellow'
		|
		|assertThat fxer[ 'status-label' ],
		|           hasText( 'You selected [colors, yellow]' )
		|
		|println "Test passed OK"
		|'''.stripMargin()
    }

}
