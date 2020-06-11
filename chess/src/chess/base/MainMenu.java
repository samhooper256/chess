package chess.base;

import chess.piecebuilder.IntInputHBox;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class MainMenu extends StackPane{
	public static synchronized Scene make() {
		if(instance != null) {
			throw new UnsupportedOperationException("Cannot have more than one instance of MainMenu");
		}
		instance = new MainMenu();
		return instance.scene;
	}
	public static MainMenu get() {
		if(instance == null) {
			throw new NullPointerException("Instance has not yet been created (instance == null)");
		}
		return instance;
	}
	
	/**
	 * Returns either an Integer representing the new empty board size or a board preset.
	 */
	public static Object getBoardSelection() {
		boardSelect.showAndWait();
		return boardSelect.getResult();
	}
	
	private static MainMenu instance;
	private Scene scene;
	private GamePanel gamePanel;
	private static BoardSelect boardSelect;
	
	static {
		boardSelect = new BoardSelect();
	}
	private MainMenu() {
		scene = new Scene(this, Main.WIDTH, Main.HEIGHT);
		scene.getStylesheets().add(MainMenu.class.getResource("mainmenu.css").toExternalForm());
		Pane pane = new Pane();
		this.getChildren().add(pane);
		gamePanel = new GamePanel();
		
		MenuTransitionHandler mth = new MenuTransitionHandler(pane);
		Button newBoardButton = new Button("New Board");
		newBoardButton.getStyleClass().add("main-menu-button");
		Button settingsButton = new Button("Settings");
		settingsButton.getStyleClass().add("main-menu-button");
		Button quitButton = new Button("Quit");
		quitButton.getStyleClass().add("main-menu-button");
		newBoardButton.setOnAction(actionEvent -> {
			Object selection = getBoardSelection();
			if(selection != null) {
				if(selection instanceof Integer) {
					gamePanel.setBoard(((Integer) selection).intValue());
				}
				else {
					throw new UnsupportedOperationException();
				}
			}
			scene.setRoot(gamePanel);
		});
		VBox vBox = new VBox(newBoardButton, settingsButton, quitButton);
		vBox.setAlignment(Pos.CENTER);
		this.getChildren().add(vBox);
		mth.start();
	}
	
	class MenuTransitionHandler{
		private final PseudoBoard[] boards;
		ParallelTransition pt;
		private final int DURATION_MILLIS;
		public MenuTransitionHandler(Pane whereToAddBoards) {
			super();
			boards = new PseudoBoard[3];
			final double PX_SIZE = Math.max(Main.WIDTH, Main.HEIGHT);
			boards[0] = new PseudoBoard(PX_SIZE, 24);
			boards[1] = new PseudoBoard(PX_SIZE, 24);
			boards[2] = new PseudoBoard(PX_SIZE, 24);
			
			DURATION_MILLIS = (int) (PX_SIZE * 30);
			
			
			TranslateTransition tt1 = new TranslateTransition(Duration.millis(DURATION_MILLIS), boards[0]);
			tt1.setFromY(0);
			tt1.setToY(2 * PX_SIZE);
			tt1.setInterpolator(Interpolator.LINEAR);
			TranslateTransition tt2 = new TranslateTransition(Duration.millis(DURATION_MILLIS), boards[1]);
			tt2.setFromY(-PX_SIZE);
			tt2.setToY(PX_SIZE);
			tt2.setInterpolator(Interpolator.LINEAR);
			TranslateTransition tt3 = new TranslateTransition(Duration.millis(DURATION_MILLIS), boards[2]);
			tt3.setFromY(-2*PX_SIZE);
			tt3.setToY(0);
			tt3.setInterpolator(Interpolator.LINEAR);
			pt = new ParallelTransition(tt1,tt2,tt3);
			pt.setCycleCount(Animation.INDEFINITE);
			whereToAddBoards.getChildren().addAll(boards);
		}
		
		public void start() {
			pt.playFromStart();
		}
	}
}

class PseudoBoard extends StackPane{
	private GridPane gridPane;
	
	public PseudoBoard(final double pxSize, final int SIZE) {
		this.setMinWidth(pxSize);
		this.setMinHeight(pxSize);
		
		gridPane = new GridPane();
		for(int i = 0; i < SIZE; i++) {
			RowConstraints rc = new RowConstraints();
			rc.setPercentHeight(100.0/SIZE);
			gridPane.getRowConstraints().add(rc);
			ColumnConstraints cc = new ColumnConstraints();
			cc.setPercentWidth(100.0/SIZE);
			gridPane.getColumnConstraints().add(cc);
		}
		
		for(int i = 0; i < SIZE; i++) {
			for(int j = 0; j < SIZE; j++) {
				PseudoTile pTile = new PseudoTile(i, j);
				gridPane.add(pTile, i, j);
			}
		}
		
		this.getChildren().add(gridPane);
	}
}

class PseudoTile extends StackPane{
	public PseudoTile(int row, int col) {
		super();
		this.setStyle("-fx-background-color: " + ((row+col) % 2 == 0 ? Board.LIGHT_COLOR : Board.DARK_COLOR) + ";");
	}
}

class BoardSelect extends Stage{
	Object result;
	public BoardSelect() {
		super();
		this.initModality(Modality.APPLICATION_MODAL);
		IntInputHBox intBox = new IntInputHBox("Enter a board size:");
		Label orLabel = new Label("or");
		Label presetLabel = new Label("Select Preset:");
		HBox bottomHBox = new HBox(presetLabel);
		Button selectButton = new Button("Select");
		Label errorMessage = new Label();
		selectButton.setOnAction(actionEvent -> {
			//TODO presets - choose the preset if it's available, else choose the number
			if(intBox.verifyInput()) {
				result = Integer.valueOf(intBox.getInt());
				BoardSelect.this.close();
			}
			else {
				errorMessage.setText("Invalid number");
			}
			
		});
		VBox vBox = new VBox(intBox, orLabel, bottomHBox, selectButton, errorMessage);
		vBox.setAlignment(Pos.CENTER);
		Scene scene = new Scene(vBox);
		this.setScene(scene);
		this.sizeToScene();
	}
	
	public Object getResult() {
		return result;
	}
}