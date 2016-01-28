package sample

import com.athaydes.automaton.FXApp
import com.athaydes.automaton.FXer
import com.athaydes.automaton.assertion.AutomatonMatcher.hasText
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.stage.Stage
import org.junit.AfterClass
import org.junit.Assert.assertThat
import org.junit.BeforeClass
import org.junit.Test


class JavaFXExampleApp : Application() {

    @Throws(Exception::class)
    override fun start(stage: Stage) {
        println("Starting up JavaFXExampleApp")
        val root = HBox(10.0)
        root.children.addAll(
                Label("Hello JavaFX"),
                TextField("Type here"))

        stage.scene = Scene(root, 500.0, 400.0)

        stage.centerOnScreen()
        stage.show()
    }
}

class KotlinAutomatonTest {

    public companion object {

        @JvmStatic
        @BeforeClass
        public fun launchApp() {
            println("Launching Kotlin App")
            FXApp.startApp(JavaFXExampleApp())
            println("App has been launched")

            Thread.sleep(2000)
        }

        @JvmStatic
        @AfterClass
        public fun cleanup() {
            println("Cleaning up")
            FXApp.doInFXThreadBlocking { FXApp.getStage().close() }
        }
    }


    @Test
    public fun automatonTest() {
        println("Running test")

        val user: FXer = FXer.getUserWith()

        user.clickOn(TextField::class.java)
                .enterText("")
                .type("Hello Automaton")

        assertThat(user.getAt(TextField::class.java), hasText("Hello Automaton"))
    }

}
