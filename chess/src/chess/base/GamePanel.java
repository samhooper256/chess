package chess.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import chess.piecebuilder.PieceBuilder;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
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
	private Button modeButton, resetButton, clearBoardButton, pieceBuilderButton;
	private VBox boardBox;
	private Board board;
	private Settings settingsMenu;
	private TilePane piecePicker;
	private ScrollPane piecePickerWrap;
	private ImageView cancelPieceIcon;
	private Pane cancelPieceIconWrap;
	Label turnLabel;
	private PieceBuilder pieceBuilder;
	private static final String PLAY_TEXT = "Play Mode", FREEPLAY_TEXT = "Freeplay Mode";
	public static final String WHITE_TO_MOVE_TEXT = "White to Move", BLACK_TO_MOVE_TEXT = "Black to Move";
	private Mode mode;
	
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
		
	    boardBox = new VBox();
	    boardBox.alignmentProperty().set(Pos.CENTER); 
	    boardBox.prefWidthProperty().bind(binding);
        boardBox.prefHeightProperty().bind(binding);
        boardBox.setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
        
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
	    modeButton = new Button(FREEPLAY_TEXT);
	    modeButton.getStyleClass().add("mode-button");
	    modeButton.setOnMouseClicked(mouseEvent -> {
	    	if(modeButton.getText().equals(FREEPLAY_TEXT)) {
	    		GamePanel.this.setToFreeplay();
	    	}
	    	else if(modeButton.getText().equals(PLAY_TEXT)) {
	    		GamePanel.this.setToPlay();
	    	}
	    });
	    resetButton = new Button("Reset Board");
	    resetButton.getStyleClass().add("reset-board-button");
	    resetButton.setOnMouseClicked(x -> board.reset());
	    rightAnchor.getChildren().addAll(modeButton, resetButton);
	    
	    AnchorPane.setLeftAnchor(modeButton, 10d);
	    AnchorPane.setRightAnchor(modeButton, 10d);
	    AnchorPane.setTopAnchor(modeButton, 10d);
	    AnchorPane.setLeftAnchor(resetButton, 10d);
	    AnchorPane.setRightAnchor(resetButton, 10d);
	    AnchorPane.setBottomAnchor(resetButton, 10d);
	    
	    piecePicker = new TilePane();
	    
	    piecePickerWrap = new ScrollPane(piecePicker);
	    piecePickerWrap.setFitToWidth(true);
	    piecePickerWrap.setVisible(false);
	    
	    AnchorPane.setTopAnchor(piecePickerWrap, 50d);
	    AnchorPane.setLeftAnchor(piecePickerWrap, 10d);
	    AnchorPane.setRightAnchor(piecePickerWrap, 10d);
	    AnchorPane.setBottomAnchor(piecePickerWrap, 50d);
	    
	    
	    turnLabel = new Label(WHITE_TO_MOVE_TEXT);
	    turnLabel.getStyleClass().add("turn-label");
	    turnLabel.setAlignment(Pos.CENTER);
	    AnchorPane.setLeftAnchor(turnLabel, 10d);
	    AnchorPane.setRightAnchor(turnLabel, 10d);
	    AnchorPane.setTopAnchor(turnLabel, 50d);
	    AnchorPane.setBottomAnchor(turnLabel, 50d);
	    
	    
	    
	    rightAnchor.getChildren().addAll(piecePickerWrap, turnLabel);
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
	    
	    AnchorPane leftTopAnchor = new AnchorPane();
	    AnchorPane.setRightAnchor(leftVBox, 20d);
	    leftTopAnchor.getChildren().add(leftVBox);
	    iLeft.getChildren().add(leftVBox);
	    
	    flipBoardImage.setOnMouseClicked(x -> board.flip());
        flipTurnImage.setOnMouseClicked(x -> board.flipTurn());
        
        ColorAdjust ca = new ColorAdjust(); //this cannot be done from css... :(
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
        
        cancelPieceIcon = new ImageView(new Image(GamePanel.class.getResourceAsStream("/resources/cancel.png")));
        cancelPieceIcon.setPreserveRatio(true);
        cancelPieceIcon.fitHeightProperty().bind(boardBox.heightProperty().divide(12));
        cancelPieceIcon.setPickOnBounds(true);
        cancelPieceIcon.setVisible(false);
        cancelPieceIconWrap = new Pane();
        cancelPieceIconWrap.getChildren().add(cancelPieceIcon);
        leftAnchor.getChildren().add(cancelPieceIconWrap);
        AnchorPane.setRightAnchor(cancelPieceIconWrap, 10d);
        AnchorPane.setTopAnchor(cancelPieceIconWrap, 10d);
        
        ColorAdjust cancelPieceButtonCA = new ColorAdjust();
        cancelPieceButtonCA.setBrightness(0.15);
        cancelPieceIcon.setOnDragEntered(dragEvent -> {
        	cancelPieceIcon.setEffect(cancelPieceButtonCA);
        });
        cancelPieceIcon.setOnDragExited(dragEvent -> {
        	cancelPieceIcon.setEffect(null);
        });
        
        cancelPieceIcon.setOnDragOver(dragEvent -> {
        	Dragboard db = dragEvent.getDragboard();
            if (db.hasString()) {
            	dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            
            dragEvent.consume();
        });
        cancelPieceIcon.setOnDragDropped(dragEvent -> {
    		Dragboard db = dragEvent.getDragboard();
            boolean success = false;
            if (db.hasString()) {
            	Set<TransferMode> transferModes = db.getTransferModes();
            	if(transferModes.size() == 1) {
            		TransferMode tmode = transferModes.iterator().next();
            		if(tmode == TransferMode.MOVE) {
            			String text = db.getString();
            			int commaIndex = text.indexOf(',');
            			if(commaIndex > 0) {
            				int row = Integer.parseInt(text.substring(0, commaIndex));
            				int col = Integer.parseInt(text.substring(commaIndex + 1));
            				board.getTileAt(row, col).setPiece(null);
            				
            	        	success = true;
            			}
            		}
            		else if(tmode == TransferMode.COPY) {
            			success = true;
            		}
            			
            	}
            		
            }
            if(success) {
            	finishDrag();
            }
            
            dragEvent.setDropCompleted(success);
            dragEvent.consume();
        });
        
        clearBoardButton = new Button("Clear Board");
        clearBoardButton.getStyleClass().add("clear-board-button");
        AnchorPane.setRightAnchor(clearBoardButton, 10d);
        AnchorPane.setLeftAnchor(clearBoardButton, 10d);
        AnchorPane.setBottomAnchor(clearBoardButton, 10d);
        clearBoardButton.prefHeightProperty().bind(this.heightProperty().divide(8));
        leftAnchor.getChildren().add(clearBoardButton);
        clearBoardButton.setOnAction(mouseEvent -> {
        	board.clearBoard();
        });
        clearBoardButton.setVisible(false);
        
        pieceBuilderButton = new Button("Piece Builder");
        pieceBuilderButton.getStyleClass().add("piece-builder-button");
        AnchorPane.setBottomAnchor(pieceBuilderButton, 10d);
        AnchorPane.setLeftAnchor(pieceBuilderButton, 10d);
        AnchorPane.setRightAnchor(pieceBuilderButton, 10d);
        pieceBuilderButton.setOnAction(mouseEvent -> {
        	PieceBuilder.open();
        });
        leftAnchor.getChildren().add(pieceBuilderButton);
        
        iLeft.getChildren().add(leftAnchor);
        
        settingsMenu = new Settings();
        
        settingsWheel.setOnMouseClicked(x -> settingsMenu.open());
	    ///////////////////
	    
	    
	    HBox.setHgrow(leftPanel, Priority.ALWAYS);
	    HBox.setHgrow(rightPanel, Priority.ALWAYS);
	    
	    
        
        rightAnchor.prefHeightProperty().bind(boardBox.heightProperty());
        rightAnchor.prefWidthProperty().bind(rightPanel.widthProperty());
        
        leftAnchor.prefHeightProperty().bind(boardBox.heightProperty());
        leftAnchor.prefWidthProperty().bind(rightPanel.widthProperty());
        
        leftVBox.prefHeightProperty().bind(boardBox.heightProperty());
        leftVBox.prefWidthProperty().bind(leftPanel.widthProperty());
        
        
        
        
        hBox.getChildren().addAll(leftPanel, boardBox, rightPanel);
        this.getChildren().addAll(hBox, settingsMenu);
        //HBox.setHgrow(this, Priority.ALWAYS);
        
        mode = Mode.PLAY;
        
        //TODO UNCOMMENT AND DELETE STUFF
        //board = Board.fromPreset(this, pre);
        board = Board.defaultBoard(this);
        VBox.setVgrow(board, Priority.ALWAYS);
      
        boardBox.getChildren().add(board);
              
        pieceBuilder = PieceBuilder.make();
		PieceBuilder.setGamePanel(this);
	}
	
	public enum Mode{
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
		piecePicker.getChildren().clear();
		Collection<Piece> pieceInstances = Piece.getInstancesOfAllPieces();
	    Collection<PickIcon> pickIcons = new ArrayList<>(pieceInstances.size());
	    for(Piece p : pieceInstances) {
	    	pickIcons.add(new PickIcon(p));
	    }
	    piecePicker.getChildren().addAll(pickIcons);
	    
		turnLabel.setVisible(false);
		pieceBuilderButton.setVisible(false);
	    piecePickerWrap.setVisible(true);
	    clearBoardButton.setVisible(true);
		modeButton.setText(PLAY_TEXT);
		board.deselect();
		mode = Mode.FREEPLAY;
	}
	
	private void setToPlay() {
	    piecePickerWrap.setVisible(false);
	    clearBoardButton.setVisible(false);
		turnLabel.setVisible(true);
		pieceBuilderButton.setVisible(true);
		modeButton.setText(FREEPLAY_TEXT);
		board.updateKingLocations();
		board.movePreparerForFXThread.prepare();
		mode = Mode.PLAY;
	}
	
	void setCancelVisibility(boolean vis) {
		cancelPieceIcon.setVisible(vis);
	}
	
	public Mode getMode() { return mode; }
	
	public Board getBoard() { return board; }
	
	public void setBoard(int size) {
		Board newBoard = Board.emptyBoard(this, size);
		board = newBoard;
		VBox.setVgrow(board, Priority.ALWAYS);
		boardBox.getChildren().set(0, board);
	}
	
	public Settings settings() { return settingsMenu;}
	
	void setTurnText(String text) {
		turnLabel.setText(text);
	}
	
	public void finishDrag() {
		setCancelVisibility(false);
	}
	
	public void startDrag(MouseEvent m, Piece p) {
		setCancelVisibility(true);
	}
	class PickIcon extends StackPane{
		Piece myPiece;
		private WrappedImageView im;
		private EventHandler<? super MouseEvent> onDragDetected = dragEvent -> {
			PickIcon source = (PickIcon) dragEvent.getSource();
			Dragboard db = startDragAndDrop(TransferMode.COPY);
			SnapshotParameters parameters = new SnapshotParameters();
			parameters.setFill(Color.TRANSPARENT);
			Image dragViewImage = source.im.snapshot(parameters, null);
			db.setDragView(dragViewImage, dragViewImage.getWidth()/2, dragViewImage.getHeight()/2);
			ClipboardContent content = new ClipboardContent();
	        content.putString(myPiece.getFullName());
	        db.setContent(content);
	        startDrag(dragEvent, myPiece);
	        dragEvent.consume();
		};
		public PickIcon(Piece p) {
			myPiece = p;
			this.prefWidthProperty().bind(GamePanel.this.heightProperty().divide(8));
			this.prefHeightProperty().bind(this.prefWidthProperty());
			im = new WrappedImageView(p.getImage());
			im.setPreserveRatio(true);
			this.getChildren().add(im);
			this.setOnDragDetected(onDragDetected);
			this.setOnDragDone(dragEvent -> {
	        	GamePanel.this.finishDrag();
	        });
		};
	}
	
	private static boolean isInteger(String s, int radix) {
	    if(s.isEmpty()) return false;
	    for(int i = 0; i < s.length(); i++) {
	        if(i == 0 && s.charAt(i) == '-') {
	            if(s.length() == 1) return false;
	            else continue;
	        }
	        if(Character.digit(s.charAt(i),radix) < 0) return false;
	    }
	    return true;
	}
	
	
	public class Settings extends StackPane{
		ColorAdjust darken;
		private GridPane gridPane;
		private ScrollPane scroll;
		private VBox vBox;
		private HBox lowerBox;
		private Label errorMessage;
		/** Auto-flip Setting */
		private CheckBox autoFlipCheckBox;
		/** Draw by insufficient material Setting*/
		private CheckBox insufficientMaterialCheckBox;
		/** N-move rule Setting*/
		private HBox moveRuleHBox;
		private CheckBox moveRuleCheckBox;
		private TextField moveRuleTextField;
		private Label moveRuleLabel;
		/////////////////////
		private int moveRule;
		private boolean autoFlip, insufficientMaterial, moveRuleEnabled;
		
		public Settings() {
			super();
			this.setVisible(false);
			gridPane = new GridPane();
			gridPane.setPickOnBounds(false);
			gridPane.setMinSize(300, 400);
			gridPane.maxWidthProperty().bind(GamePanel.this.widthProperty().divide(1.5));
			gridPane.maxHeightProperty().bind(GamePanel.this.heightProperty().divide(1.5));
			gridPane.getStyleClass().add("settings-grid-pane");
			RowConstraints row1 = new RowConstraints();
			row1.setPercentHeight(80);
			RowConstraints row2 = new RowConstraints();
			row2.setPercentHeight(20);
			gridPane.getRowConstraints().addAll(row1, row2);
			ColumnConstraints col1 = new ColumnConstraints();
			col1.setPercentWidth(100);
			gridPane.getColumnConstraints().add(col1);
			gridPane.setGridLinesVisible(true);
			
			vBox = new VBox(5);
			scroll = new ScrollPane(vBox);
			gridPane.add(scroll, 0, 0);
			
			/* Create auto-flip setting*/
			autoFlipCheckBox = new CheckBox("Auto-flip");
			Tooltip.install(autoFlipCheckBox, new Tooltip("When enabled, the board will automatically be rotated so that the pieces of the player"
					+ " whose turn it is appears at the bottom."));
			autoFlipCheckBox.setSelected(true);
			autoFlip = true;
			
			/* Create draw by insufficient material setting*/
			insufficientMaterialCheckBox = new CheckBox("Draw by insufficient matierial");
			insufficientMaterialCheckBox.setSelected(true);
			Tooltip.install(insufficientMaterialCheckBox, new Tooltip("Determines whether the insufficient material rule is enabled."));
			insufficientMaterial = true;
			
			/* Create N-move-rule setting*/
			moveRuleCheckBox = new CheckBox();
			moveRuleCheckBox.setSelected(true);
			moveRuleTextField = new TextField("50");
			moveRuleTextField.prefWidthProperty().bind(Settings.this.widthProperty().divide(32));
			moveRuleLabel = new Label("move rule");
			moveRuleHBox = new HBox(5, moveRuleCheckBox, moveRuleTextField, moveRuleLabel);
			moveRuleHBox.setAlignment(Pos.CENTER_LEFT);
			moveRule = 50;
			moveRuleEnabled = true;
			
			vBox.getChildren().addAll(autoFlipCheckBox, insufficientMaterialCheckBox, moveRuleHBox);
			
			Button cancelButton = new Button("Cancel");
			cancelButton.setOnMouseClicked(x -> this.closeWithoutApplying());
			Button applyButton = new Button("Apply");
			applyButton.setOnMouseClicked(x -> this.attemptClose());
			errorMessage = new Label("");
			errorMessage.setWrapText(true);
			errorMessage.setTextFill(Color.RED);
			lowerBox = new HBox(20);
			lowerBox.setAlignment(Pos.CENTER_RIGHT);
			lowerBox.setPadding(new Insets(20));
			Button butt = new Button("Change board");
			butt.setOnAction(actionEvent -> {
				GamePanel.this.setBoard(5 + (int) (Math.random() * 19)); 
			});
			lowerBox.getChildren().addAll(butt, /*errorMessage,*/ cancelButton, applyButton); //TODO put "error message" somewhere
			gridPane.add(lowerBox, 0, 1);
			
			darken = new ColorAdjust();
			darken.setBrightness(-0.25);
			
			this.getChildren().add(gridPane);
		}
		
		private boolean applySettings() {
			if(moveRuleCheckBox.isSelected()) {
				if(!isInteger(moveRuleTextField.getText().strip(), 10)){
					errorMessage.setText("You must enter a positive integer for move rule if it is enabled.");
					return false;
				}
			}
			
			if(moveRuleCheckBox.isSelected()) {
				moveRule = Integer.parseInt(moveRuleTextField.getText().strip()); //make sure to call strip
				moveRuleTextField.setText(String.valueOf(moveRule));
			}
			moveRuleEnabled = moveRuleCheckBox.isSelected();
			autoFlip = autoFlipCheckBox.isSelected();
			insufficientMaterial = insufficientMaterialCheckBox.isSelected();
			return true;
		}
		
		
		
		private void keepSettings() {
			autoFlipCheckBox.setSelected(autoFlip);
			insufficientMaterialCheckBox.setSelected(insufficientMaterial);
			moveRuleCheckBox.setSelected(moveRuleEnabled);
			moveRuleTextField.setText(String.valueOf(moveRule));
		}
		
		public void open() {
			GamePanel.this.hBox.setEffect(darken);
			this.setPickOnBounds(true);
			this.setVisible(true);
		}
		
		public void attemptClose() {
			boolean apply = this.applySettings();
			if(apply) {
				close0();
			}
		}
		
		public void closeWithoutApplying() {
			this.keepSettings();
			close0();
		}
		
		private void close0(){
			errorMessage.setText("");
			this.setVisible(false);
			this.setPickOnBounds(false);
			GamePanel.this.hBox.setEffect(null);
		}
		
		public boolean getAutoFlip() {
			return autoFlip;
		}
		
		public boolean getInsufficientMaterial() {
			return insufficientMaterial;
		}
		
		public boolean moveRuleEnabled() {
			return moveRuleEnabled;
		}
		
		public int getMoveRule() {
			return moveRule;
		}
	}
	
}
