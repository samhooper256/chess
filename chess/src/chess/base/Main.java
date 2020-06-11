package chess.base;

import chess.util.Condition;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

//TODO DELETE UNUSED IMPORTS (chess.util.*)
/* *
 * @author Sam Hooper
 */
public class Main extends Application{
	public static void main(String[] args) {
		Application.launch(args);
	}
	
	public static final double WIDTH, HEIGHT;
	private static Scene scene;
	static {
		Rectangle2D screenSize = Screen.getPrimary().getBounds();
		WIDTH = screenSize.getWidth();
		HEIGHT = screenSize.getHeight();
	}
	@Override
	public void start(Stage primaryStage) throws Exception {
		scene = MainMenu.make();
		scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());
		primaryStage.setMinHeight(400);
		primaryStage.minWidthProperty().bind(primaryStage.heightProperty().multiply(1.5));
		primaryStage.setTitle("chess++");
		primaryStage.setScene(scene);
		primaryStage.setMaximized(true);
		primaryStage.show();
	}
	
	public static Scene getScene() {
		return scene;
	}
	
}
