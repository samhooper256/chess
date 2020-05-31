package chess.base;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;


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
		GamePanel gp = new GamePanel();
		Scene scene = new Scene(gp, 800, 600);
		
		scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());
		primaryStage.setMinHeight(400);
		primaryStage.minWidthProperty().bind(primaryStage.heightProperty().multiply(1.5));
		primaryStage.setTitle("chess++");
		primaryStage.setScene(scene);
		primaryStage.sizeToScene();
		primaryStage.show();
		
	}
}
