package chess.base;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

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
	
	public static final String RESOURCES_PREFIX = "/src/resources/"; //"/resources/";
	public static final File USER_FOLDER = new File(System.getProperty("user.dir"), "chess++ user configs");
	public static final File PIECES_FOLDER;
	public static final String PRESETS_FILENAME = "presets.dat";
	public static double WIDTH, HEIGHT;
	public static Scene scene;
	
	static {
		if(!USER_FOLDER.exists()) {
			USER_FOLDER.mkdir();
		}
		
		PIECES_FOLDER = new File(USER_FOLDER, "pieces");
		if(!PIECES_FOLDER.exists()) {
			PIECES_FOLDER.mkdir();
		}
	}
	@Override
	public void start(Stage primaryStage) throws Exception {
	
		Rectangle2D screenSize = Screen.getPrimary().getBounds();
		WIDTH = screenSize.getWidth();
		HEIGHT = screenSize.getHeight();
		BoardSelect.make();
		PresetCreation.make();
		Settings.make();
		scene = MainMenu.make();
		scene.getStylesheets().add(Main.class.getResource(RESOURCES_PREFIX + "style.css").toExternalForm());
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
