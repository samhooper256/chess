package chess.piecebuilder;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PieceBuilderDriver extends Application{

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		/*
		primaryStage = new PieceBuilder();
		primaryStage.show();
		*/
		ActionTreeBuilder atb = new ActionTreeBuilder(null);
		Scene scene = new Scene(atb, 600, 400);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

}
