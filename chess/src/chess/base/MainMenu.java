package chess.base;

import java.io.File;
import java.util.ArrayList;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainMenu extends StackPane{
	public static final Image LOGO;
	static {
		LOGO = new Image(MainMenu.class.getResourceAsStream(Main.RESOURCES_PREFIX + "logo4.png")); 
	}
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
	
	
	private static final File instructionsFile = new File(Main.RESOURCES_PREFIX + "instructions.html");
	private static MainMenu instance;
	private static ArrayList<Image> pieceImages;
	private Scene scene;
	private GamePanel gamePanel;
	private MenuTransitionHandler mth;
	private ImageView logoImageView;
	
	private MainMenu() {
		scene = new Scene(this, Main.WIDTH, Main.HEIGHT);
		scene.getStylesheets().add(MainMenu.class.getResource(Main.RESOURCES_PREFIX + "mainmenu.css").toExternalForm());
		Pane pane = new Pane();
		this.getChildren().add(pane);
		gamePanel = new GamePanel();
		pieceImages = Piece.getImagesOfAllPieces();
		
		Button newBoardButton = new Button("New Board");
		Button settingsButton = new Button("Settings");
		Button instructionsButton = new Button("Instructions");
		Button quitButton = new Button("Quit");
		
		DoubleBinding buttonWidth = (DoubleBinding) Bindings.max(200, MainMenu.this.widthProperty().divide(6));
		DoubleBinding buttonHeight = MainMenu.this.heightProperty().divide(14);
		mth = new MenuTransitionHandler(pane);
		
		newBoardButton.prefWidthProperty().bind(buttonWidth);
		newBoardButton.prefHeightProperty().bind(buttonHeight);
		newBoardButton.getStyleClass().add("main-menu-button");
		
		settingsButton.getStyleClass().add("main-menu-button");
		settingsButton.prefWidthProperty().bind(buttonWidth);
		settingsButton.prefHeightProperty().bind(buttonHeight);
		
		instructionsButton.getStyleClass().add("main-menu-button");
		instructionsButton.prefWidthProperty().bind(buttonWidth);
		instructionsButton.prefHeightProperty().bind(buttonHeight);
		
		quitButton.getStyleClass().add("main-menu-button");
		quitButton.prefWidthProperty().bind(buttonWidth);
		quitButton.prefHeightProperty().bind(buttonHeight);
		newBoardButton.setOnAction(actionEvent -> {
			Object selection = BoardSelect.getBoardSelection();
			if(selection instanceof Integer) {
				gamePanel.setBoard(((Integer) selection).intValue());
				scene.setRoot(gamePanel);
				pauseBackground();
			}
			else if(selection instanceof BoardPreset) {
				gamePanel.setBoard((BoardPreset) selection);
				scene.setRoot(gamePanel);
				pauseBackground();
			}
			
		});
		
		class InstructionsStage extends Stage{
			public InstructionsStage() {
				super();
				WebView webView = new WebView();
				webView.getEngine().load(instructionsFile.toURI().toString());
				StackPane sp = new StackPane(webView);
				InstructionsStage.this.setScene(new Scene(sp, 800, 600));
			}
		}
		InstructionsStage instructionsStage = new InstructionsStage();
		instructionsButton.setOnAction(actionEvent -> {
			instructionsStage.show();
		});
		settingsButton.setOnAction(actionEvent -> {
			Settings.openOn(MainMenu.this, true, 0.4, 0.4);
		});
		quitButton.setOnAction(actionEvent -> {
			((Stage) quitButton.getScene().getWindow()).close();
		});
		
		logoImageView = new ImageView(LOGO);
		logoImageView.setPreserveRatio(true);
		logoImageView.fitHeightProperty().bind(MainMenu.this.heightProperty().divide(4));
		VBox vBox = new VBox(logoImageView, newBoardButton, instructionsButton, settingsButton, quitButton);
		vBox.getStyleClass().add("main-menu-box");
		this.getChildren().add(vBox);
		mth.start();
	}
	
	public static void pauseBackground() {
		instance.mth.pause();
	}
	
	public static void playBackground() {
		instance.mth.play();
	}
	
	class MenuTransitionHandler{
		private final PseudoBoard[] boards;
		ParallelTransition pt;
		private final int DURATION_MILLIS;
		private final int PBOARD_SIZE = 24;
		public MenuTransitionHandler(Pane whereToAddBoards) {
			super();
			boards = new PseudoBoard[3];
			final double PX_SIZE = Math.max(Main.WIDTH, Main.HEIGHT);
			boards[0] = new PseudoBoard(PX_SIZE, PBOARD_SIZE);
			boards[1] = new PseudoBoard(PX_SIZE, PBOARD_SIZE);
			boards[2] = new PseudoBoard(PX_SIZE, PBOARD_SIZE);
			
			DURATION_MILLIS = (int) (PX_SIZE * 40);
			
			
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
			double tt1break = DURATION_MILLIS * 0.55;
			for(int i = 0; i < PBOARD_SIZE; i++) {
				for(int j = 0; j < PBOARD_SIZE; j++) {
					if(Math.random() < 0.05) {
						boards[0].getTileAt(i, j).setImage(pieceImages.get((int) (Math.random() * pieceImages.size())));
					}
					if(Math.random() < 0.05) {
						boards[1].getTileAt(i, j).setImage(pieceImages.get((int) (Math.random() * pieceImages.size())));
					}
					if(Math.random() < 0.05) {
						boards[2].getTileAt(i, j).setImage(pieceImages.get((int) (Math.random() * pieceImages.size())));
					}
				}
			}
			tt1.currentTimeProperty().addListener(new ChangeListener<Duration>() {

				@Override
				public void changed(ObservableValue<? extends Duration> observable, Duration oldValue,
						Duration newValue) {
					if(oldValue.toMillis() < tt1break && newValue.toMillis() > tt1break) {
						for(int i = 0; i < PBOARD_SIZE; i++) {
							for(int j = 0; j < PBOARD_SIZE; j++) {
								PseudoTile tile = boards[0].getTileAt(i, j);
								PseudoTile tile2 = boards[2].getTileAt(i, j);
								if(!tile2.getChildren().isEmpty()) {
									tile.setImage(tile2.image);
								}
								else {
									tile.clearImage();
								}
							}
						}
					}
					//System.out.println("change called");
				}
			});
			tt1.setOnFinished(actionEvent -> {
				for(int i = 0; i < PBOARD_SIZE; i++) {
					for(int j = 0; j < PBOARD_SIZE; j++) {
						PseudoTile t1 = boards[1].getTileAt(i, j);
						PseudoTile t2 = boards[2].getTileAt(i, j);
						t1.clearImage();
						t2.clearImage();
						if(Math.random() < 0.05) {
							t1.setImage(pieceImages.get((int) (Math.random() * pieceImages.size())));
						}
						if(Math.random() < 0.05) {
							t2.setImage(pieceImages.get((int) (Math.random() * pieceImages.size())));
						}
					}
				}
			});
		}
		
		public void start() {
			pt.playFromStart();
		}
		
		public void pause() {
			pt.pause();
		}
		
		public void play() {
			pt.play();
		}
	}
}

class PseudoBoard extends StackPane{
	private GridPane gridPane;
	private PseudoTile[][] tiles;
	private final int size;
	public PseudoBoard(final double pxSize, final int SIZE) {
		this.setMinWidth(pxSize);
		this.setMinHeight(pxSize);
		this.setMaxSize(pxSize, pxSize);
		this.setPrefSize(pxSize, pxSize);
		//this.setStyle("-fx-border-color:red; -fx-border-width:3");
		this.size = SIZE;
		tiles = new PseudoTile[SIZE][SIZE];
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
				PseudoTile pTile = tiles[i][j] = new PseudoTile(i, j);
				gridPane.add(pTile, i, j);
			}
		}
		
		this.getChildren().add(gridPane);
	}
	
	public int getSize() {
		return size;
	}
	
	/**
	 * DOES NOT PERFORM BOUNDS CHECKING
	 * @param row
	 * @param col
	 * @return
	 */
	public PseudoTile getTileAt(int row, int col) {
		return tiles[row][col];
	}
}

class PseudoTile extends StackPane{
	volatile Image image;
	public PseudoTile(int row, int col) {
		super();
		this.setStyle("-fx-background-color: " + ((row+col) % 2 == 0 ? Board.LIGHT_COLOR : Board.DARK_COLOR) + ";");
	}
	
	public void setImage(Image im) {
		ImageView imv = new WrappedImageView(image = im);
		if(this.getChildren().isEmpty()) {
			this.getChildren().add(0, imv);
		}
		else {
			this.getChildren().set(0, imv);
		}
		
	}
	
	public void clearImage() {
		image = null;
		this.getChildren().clear();
	}
}