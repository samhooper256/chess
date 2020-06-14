package chess.base;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

/* *
 * @author Sam Hooper
 */
public class Main extends Application{
	public static void main(String[] args) {
		Application.launch(args);
	}
	
	public static final double WIDTH, HEIGHT;
	public static Scene scene;
	static {
		Rectangle2D screenSize = Screen.getPrimary().getBounds();
		WIDTH = screenSize.getWidth();
		HEIGHT = screenSize.getHeight();
	}
	@Override
	public void start(Stage primaryStage) throws Exception {
		BoardSelect.make();
		PresetCreation.make();
		Settings.make();
		scene = MainMenu.make();
		scene.getStylesheets().add(Main.class.getResource("/resources/style.css").toExternalForm());
		primaryStage.setMinHeight(400);
		primaryStage.minWidthProperty().bind(primaryStage.heightProperty().multiply(1.5));
		primaryStage.setTitle("chess++");
		primaryStage.setScene(scene);
		primaryStage.setMaximized(true);
		primaryStage.setOnCloseRequest(windowEvent -> {
			BoardPreset.savePresets();
			primaryStage.close();
		});
		primaryStage.show();
	}
	
	public static Scene getScene() {
		return scene;
	}
	
}
