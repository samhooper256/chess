package chess;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/* *
 * @author Sam Hooper
 */
public class Main extends Application{
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		Scene scene = new Scene(new GamePanel(), 800, 600);
		scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());
		primaryStage.setMinHeight(400);
		primaryStage.minWidthProperty().bind(primaryStage.heightProperty());
		primaryStage.setTitle("Chess!");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
