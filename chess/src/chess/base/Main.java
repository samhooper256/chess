package chess.base;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import chess.util.*;



//TODO DELETE UNUSED IMPORTS (chess.util.*)
/* *
 * @author Sam Hooper
 */
public class Main extends Application{
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		Scene scene = new Scene(new GamePanel(), 800, 600);
		scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());
		primaryStage.setMinHeight(400);
		primaryStage.minWidthProperty().bind(primaryStage.heightProperty());
		primaryStage.setTitle("Chess!");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
