package maven;

import com.athaydes.automaton.FXApp;
import com.athaydes.automaton.FXer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.athaydes.automaton.assertion.AutomatonMatcher.hasText;
import static org.junit.Assert.assertThat;

public class MavenAutomatonSampleTest {

	public static class JavaFXExampleApp extends Application {

		@Override
		public void start( Stage stage ) throws Exception {
			System.out.println( "Starting up JavaFXExampleApp" );
			HBox root = new HBox( 10 );
			root.getChildren().addAll(
					new Label( "Hello JavaFX" ),
					new TextField( "Type here" )
			);

			stage.setScene( new Scene( root, 500, 400 ) );

			stage.centerOnScreen();
			stage.show();
		}
	}

	@BeforeClass
	public static void launchApp() throws Exception {
		System.out.println( "Launching Java App" );

		FXApp.startApp( new JavaFXExampleApp() );

		System.out.println( "App has been launched" );

		// let the window open and show before running tests
		Thread.sleep( 2000 );
	}

	@AfterClass
	public static void cleanup() {
		System.out.println( "Cleaning up" );
		FXApp.doInFXThreadBlocking( () -> FXApp.getStage().close() );
	}

	@Test
	public void automatonTest() {
		System.out.println( "Running test" );
		FXer user = FXer.getUserWith();

		user.clickOn( TextField.class )
				.enterText( "" )
				.type( "Hello Automaton" );

		//noinspection unchecked
		assertThat( user.getAt( TextField.class ), hasText( "Hello Automaton" ) );
	}

}
