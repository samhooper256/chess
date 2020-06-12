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
        
        settingsWheel.setOnMouseClicked(x -> Settings.openOn(GamePanel.this, true, 0.4, 0.4));
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
        this.getChildren().add(hBox);
        //HBox.setHgrow(this, Priority.ALWAYS);
        
        mode = Mode.PLAY;
        
        board = null;
              
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
		setBoard(Board.emptyBoard(GamePanel.this, size));
	}
	
	public void setBoard(BoardPreset preset) {
		setBoard(Board.fromPreset(GamePanel.this, preset));
	}
	
	private void setBoard(Board newBoard) {
		board = newBoard;
		VBox.setVgrow(board, Priority.ALWAYS);
		boardBox.getChildren().clear();
		boardBox.getChildren().add(0, board);
		
		
	}
	
	public void selectNewBoard() {
		Object result = BoardSelect.getBoardSelection();
		if(result instanceof Integer) {
			GamePanel.this.setBoard(((Integer) result).intValue());
		}
		else if(result instanceof BoardPreset) {
			GamePanel.this.setBoard((BoardPreset) result);
		}
	}
	
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

	public void saveBoardAsPreset() {
		PresetCreation.createOn(board);
	}
}
