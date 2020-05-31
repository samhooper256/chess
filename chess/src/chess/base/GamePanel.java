package chess.base;

import java.util.ArrayList;
import java.util.Arrays;

import chess.util.ActionTree;
import chess.util.CaptureAction;
import chess.util.Condition;
import chess.util.MoveAndCaptureAction;
import chess.util.SummonAction;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;


/* *
 * @author Sam Hooper
 */
public class GamePanel extends StackPane{
	private HBox hBox;
	private Pane leftPanel, rightPanel;
	private StackPane iLeft, iRight;
	private AnchorPane rightAnchor, leftAnchor;
	private VBox leftVBox;
	private Button modeButton, resetButton;
	private VBox boardBox;
	private Board board;
	private Settings settingsMenu;
	private TilePane piecePicker;
	
	public GamePanel() {
		hBox = new HBox();
		hBox.setMinHeight(400);
		hBox.alignmentProperty().set(Pos.CENTER);
		
		//Make board
		final NumberBinding binding = Bindings.min(widthProperty(), heightProperty());
	    
		BoardPreset pre = new BoardPreset(9);
		pre.setPieces(new String[][]{
				{null,null,null,null,null,null,null,"-Pawn","-King"},
				{null,null,null,null,null,null,null,"-Pawn","-Pawn"},
				{null,null,null,null,null,null,null,null,"-Queen"},
				{null,null,null,"+Ghost",null,null,null,null,null},
				{null,null,null,null,null,null,null,null,null},
				{null,null,null,null,null,null,null,null,null},
				{"+Queen",null,null,null,null,null,null,null,null},
				{"+Pawn","+Pawn",null,null,null,null,null,null,null},
				{"+King","+Pawn",null,null,null,null,null,null,null}});
		
		CustomPiece.PieceData ghostData = new CustomPiece.PieceData("Ghost");
		ghostData.whiteImage = new Image(Piece.class.getResourceAsStream("/resources/ghost_white.png"));
		ghostData.blackImage = new Image(Piece.class.getResourceAsStream("/resources/ghost_black.png"));
		ghostData.pointValue = 5;
		ghostData.tree = new ActionTree(Arrays.asList(
			/*
			new ActionTree.Node(MoveAndCaptureAction.segment(1, 1, 1, 1, 3),
				new ActionTree.Node(MoveAndCaptureAction.relLine(3, 4, 0, 1)),
				new ActionTree.Node(MoveAndCaptureAction.relLine(4, 3, 1, 0))
			)*/
				/*
			new ActionTree.Node(MoveAndCaptureAction.segment(0, 1, 0, 1, 2).stops(Condition.POD),
				new ActionTree.Node(MoveAndCaptureAction.relLine(0, 2, 1, 0)),
				new ActionTree.Node(MoveAndCaptureAction.relLine(0, 2, -1, 0))
			),
			new ActionTree.Node(MoveAndCaptureAction.segment(0, -1, 0, -1, 2).stops(Condition.POD),
				new ActionTree.Node(MoveAndCaptureAction.relLine(0, -2, 1, 0)),
				new ActionTree.Node(MoveAndCaptureAction.relLine(0, -2, -1, 0))
			),
			new ActionTree.Node(MoveAndCaptureAction.segment(1, 0, 1, 0, 2).stops(Condition.POD),
				new ActionTree.Node(MoveAndCaptureAction.relLine(2, 0, 0, 1)),
				new ActionTree.Node(MoveAndCaptureAction.relLine(2, 0, 0, -1))
			),
			new ActionTree.Node(MoveAndCaptureAction.segment(-1, 0, -1, 0, 2).stops(Condition.POD),
				new ActionTree.Node(MoveAndCaptureAction.relLine(-2, 0, 0, 1)),
				new ActionTree.Node(MoveAndCaptureAction.relLine(-2, 0, 0, -1))
			)*/
			new ActionTree.Node(CaptureAction.radius(3, false, false)),
			new ActionTree.Node(MoveAndCaptureAction.radius(2, true, false)),
			new ActionTree.Node(SummonAction.radius(1, true, false, new ArrayList<String>(Arrays.asList("Bishop")), Condition.DIE))
		));
		
		CustomPiece.defineNewPiece(ghostData);
	    
		//TODO UNCOMMENT AND DELETE STUFF
		board = Board.fromPreset(this, pre);
		//board = Board.fromPreset(this, pre);
	    
	    boardBox = new VBox();
	    boardBox.alignmentProperty().set(Pos.CENTER); 
	    boardBox.prefWidthProperty().bind(binding);
        boardBox.prefHeightProperty().bind(binding);
        boardBox.setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);

        VBox.setVgrow(board, Priority.ALWAYS);
        
        boardBox.getChildren().add(board);
        
        
        
        
        /////////////////////////
	    leftPanel = new Pane();
	    rightPanel = new Pane();
	    iLeft = new StackPane();
	    iRight = new StackPane();
	    rightPanel.setPrefWidth(0);
	    leftPanel.setPrefWidth(0);
	    rightPanel.getChildren().add(iRight);
	    leftPanel.getChildren().add(iLeft);
	    
	    //Make right half:
	    rightAnchor = new AnchorPane();
	    modeButton = new Button("Play Mode");
	    resetButton = new Button("Reset Board");
	    resetButton.setOnMouseClicked(x -> board.reset());
	    //modeButton.setFocusTraversable(false);
	    rightAnchor.getChildren().addAll(modeButton, resetButton);
	    
	    AnchorPane.setLeftAnchor(modeButton, 10d);
	    AnchorPane.setRightAnchor(modeButton, 10d);
	    AnchorPane.setTopAnchor(modeButton, 10d);
	    AnchorPane.setLeftAnchor(resetButton, 10d);
	    AnchorPane.setRightAnchor(resetButton, 10d);
	    AnchorPane.setBottomAnchor(resetButton, 10d);
	    rightAnchor.setBorder(new Border(new BorderStroke(Color.DARKGREEN, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
	    
	    piecePicker = new Pane();
	    piecePicker.setBorder(new Border(new BorderStroke(Color.DEEPPINK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
	    AnchorPane.setTopAnchor(piecePicker, 50d);
	    AnchorPane.setLeftAnchor(piecePicker, 10d);
	    AnchorPane.setRightAnchor(piecePicker, 10d);
	    rightAnchor.getChildren().add(piecePicker);
	    
	    iRight.getChildren().add(rightAnchor);
	    /////////////////////////
	    //Make left half:
	    leftVBox = new VBox();
	    leftVBox.setAlignment(Pos.CENTER_RIGHT);
	    StackPane turnWrap = new StackPane();
	    StackPane boardWrap = new StackPane();
	    turnWrap.setAlignment(Pos.CENTER_RIGHT);
	    boardWrap.setAlignment(Pos.CENTER_RIGHT);
	    turnWrap.maxHeightProperty().bind(boardBox.heightProperty().divide(16));
	    boardWrap.maxHeightProperty().bind(boardBox.heightProperty().divide(16));
	    ImageView flipTurnImage = new WrappedImageView(new Image(GamePanel.class.getResourceAsStream("/resources/flip_turn.png")));
	    flipTurnImage.setPreserveRatio(true);
	    ImageView flipBoardImage = new WrappedImageView(new Image(GamePanel.class.getResourceAsStream("/resources/flip_board.png")));
	    flipBoardImage.setPreserveRatio(true);
	    flipTurnImage.setPickOnBounds(true);
	    flipBoardImage.setPickOnBounds(true);
	    
	    turnWrap.getChildren().add(flipTurnImage);
	    boardWrap.getChildren().add(flipBoardImage);
	    
	    leftVBox.getChildren().addAll(turnWrap, boardWrap);
	    
	    iLeft.getChildren().add(leftVBox);
	    
	    flipBoardImage.setOnMouseClicked(x -> board.flip());
        flipTurnImage.setOnMouseClicked(x -> board.flipTurn());
        
        ColorAdjust ca = new ColorAdjust();
        ca.setBrightness(0.2);
        flipBoardImage.setOnMouseEntered(x -> flipBoardImage.setEffect(ca));
        flipBoardImage.setOnMouseExited(x -> flipBoardImage.setEffect(null));
        flipTurnImage.setOnMouseEntered(x -> flipTurnImage.setEffect(ca));
        flipTurnImage.setOnMouseExited(x -> flipTurnImage.setEffect(null));
        Tooltip.install(flipTurnImage, new Tooltip("Flip Turn"));
        Tooltip.install(flipBoardImage, new Tooltip("Flip Board"));
        
        ImageView settingsWheel = new ImageView(new Image(GamePanel.class.getResourceAsStream("/resources/settings_wheel.png")));
        settingsWheel.setPreserveRatio(true);
        settingsWheel.fitHeightProperty().bind(boardBox.heightProperty().divide(16));
        settingsWheel.setPickOnBounds(true);
        leftAnchor = new AnchorPane();
        leftAnchor.getChildren().add(settingsWheel);
        leftAnchor.setPickOnBounds(false);
        AnchorPane.setLeftAnchor(settingsWheel, 10d);
        AnchorPane.setTopAnchor(settingsWheel, 10d);
        settingsWheel.setOnMouseEntered(x -> settingsWheel.setEffect(ca));
        settingsWheel.setOnMouseExited(x -> settingsWheel.setEffect(null));
        
        iLeft.getChildren().add(leftAnchor);
        
        settingsMenu = new Settings();
        
        settingsWheel.setOnMouseClicked(x -> settingsMenu.open());
	    ///////////////////
	    
	    
	    HBox.setHgrow(leftPanel, Priority.ALWAYS);
	    HBox.setHgrow(rightPanel, Priority.ALWAYS);
	    
	    
        
        rightAnchor.prefHeightProperty().bind(boardBox.heightProperty());
        rightAnchor.prefWidthProperty().bind(rightPanel.widthProperty());
        
        leftVBox.prefHeightProperty().bind(boardBox.heightProperty());
        leftVBox.prefWidthProperty().bind(leftPanel.widthProperty());
        
        
        
        
        hBox.getChildren().addAll(leftPanel, boardBox, rightPanel);
        this.getChildren().addAll(hBox, settingsMenu);
        //HBox.setHgrow(this, Priority.ALWAYS);
	}
	
	private enum Mode{
		FREEPLAY, PLAY;
	}
	
	public void changeMode(Mode mode) {
		if(mode == Mode.FREEPLAY) {
			
		}
		else if(mode == Mode.PLAY) {
			
		}
		else {
			throw new IllegalArgumentException(mode + " is not supported");
		}
	}
	
	private void setToFreeplay() {
		
	}
	
	public Board getBoard() { return board; }
	
	public Settings settings() { return settingsMenu;}
	
	public class Settings extends StackPane{
		ColorAdjust darken;
		private GridPane gridPane;
		private ScrollPane scroll;
		private VBox vBox;
		private HBox lowerBox;
		private CheckBox autoFlipCheckBox;
		private boolean autoFlip;
		public Settings() {
			super();
			this.setVisible(false);
			gridPane = new GridPane();
			gridPane.setPickOnBounds(false);
			gridPane.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
			gridPane.setMinSize(300, 400);
			gridPane.maxWidthProperty().bind(GamePanel.this.widthProperty().divide(1.5));
			gridPane.maxHeightProperty().bind(GamePanel.this.heightProperty().divide(1.5));
			gridPane.setId("settings-grid-pane");
			RowConstraints row1 = new RowConstraints();
			row1.setPercentHeight(80);
			RowConstraints row2 = new RowConstraints();
			row2.setPercentHeight(20);
			gridPane.getRowConstraints().addAll(row1, row2);
			ColumnConstraints col1 = new ColumnConstraints();
			col1.setPercentWidth(100);
			gridPane.getColumnConstraints().add(col1);
			gridPane.setGridLinesVisible(true);
			
			vBox = new VBox();
			scroll = new ScrollPane(vBox);
			gridPane.add(scroll, 0, 0);
			
			autoFlipCheckBox = new CheckBox("Auto-flip");
			Tooltip.install(autoFlipCheckBox, new Tooltip("When enabled, the board will automatically be rotated so that the pieces of the player"
					+ " whose turn it is appear at the bottom."));
			autoFlipCheckBox.setSelected(true);
			autoFlip = true;
			
			vBox.getChildren().addAll(autoFlipCheckBox);
			
			Button cancelButton = new Button("Cancel");
			cancelButton.setOnMouseClicked(x -> this.closeWithoutApplying());
			Button applyButton = new Button("Apply");
			applyButton.setOnMouseClicked(x -> this.close());
			lowerBox = new HBox(20);
			lowerBox.setAlignment(Pos.CENTER_RIGHT);
			lowerBox.setPadding(new Insets(20));
			lowerBox.getChildren().addAll(cancelButton, applyButton);
			gridPane.add(lowerBox, 0, 1);
			
			darken = new ColorAdjust();
			darken.setBrightness(-0.25);
			
			this.getChildren().add(gridPane);
		}
		
		private void applySettings() {
			autoFlip = autoFlipCheckBox.isSelected();
		}
		
		private void keepSettings() {
			autoFlipCheckBox.setSelected(autoFlip);
		}
		
		public void open() {
			GamePanel.this.hBox.setEffect(darken);
			this.setPickOnBounds(true);
			this.setVisible(true);
		}
		
		public void close() {
			this.applySettings();
			close0();
		}
		
		public void closeWithoutApplying() {
			this.keepSettings();
			close0();
		}
		
		private void close0(){
			this.setVisible(false);
			this.setPickOnBounds(false);
			GamePanel.this.hBox.setEffect(null);
		}
		
		public boolean getAutoFlip() {
			return autoFlip;
		}
	}
	
}
