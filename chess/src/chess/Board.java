package chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

import javafx.animation.FillTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/* *
 * @author Sam Hooper
 */
public class Board extends StackPane{
	public static final int MAX_BOARD_SIZE = 26;
	public static final int DEFAULT_BOARD_SIZE = 8;
	public static boolean QUEENSIDE = true;
	public static boolean KINGSIDE = false;
	private static BoardPreset defaultBoardPreset;
	
	private final int BOARD_SIZE;
	private Tile[][] board;
	private final boolean enPassantAllowed;
	private int moveNumber;
	private int playNumber;
	private int movesSincePawnOrCapture;
	private LinkedList<GameMove> moveList;
	private int[][] kingLocations; //kingLocations[0] is white king, kingLocations[1] is black king
	private Tile currentlySelectedTile;
	private BoardPreset preset;
	private boolean turn;
	private boolean orientation;
	private boolean castlingAllowed;
	private boolean boardInteracitonAllowed = true;
	private boolean fiftyMoveRule = true;
	
	//UI Components
	private BorderPane boardOverlay;
	private StackPane boardPopupBox, promotionBox;
	private GridPane popupMessageArea;
	private AnchorPane popupMessageOverlay;
	private Text boardPopupMessage, popupMessageXButton, popupGameOverText;
	private Button popupResetButton, popupViewBoardButton;
	private Tile[][] boardDisplay;
	private TilePane promotionOptions;
	private GridPane grid;
	
	private static Color LIGHT_COLOR = Color.rgb(26, 187, 154, 1); //#1abb9a
	private static Color DARK_COLOR = Color.rgb(44, 61, 79,1); //#2c3d4f
	private static Color LIGHT_COLOR_SELECTED = Color.rgb(56, 232, 196,1); //#38e8c4
	private static Color DARK_COLOR_SELECTED = Color.rgb(69, 87, 107, 1); //#45576b
	private static Color LEGAL_MOVE_COLOR = Color.rgb(0, 13, 255, 0.5);
	private static Background light = new Background(new BackgroundFill(LIGHT_COLOR, CornerRadii.EMPTY, Insets.EMPTY));
	private static Background dark = new Background(new BackgroundFill(DARK_COLOR, CornerRadii.EMPTY, Insets.EMPTY));
	private static Background lightSelected = new Background(new BackgroundFill(LIGHT_COLOR_SELECTED, CornerRadii.EMPTY, Insets.EMPTY));
	private static Background darkSelected = new Background(new BackgroundFill(DARK_COLOR_SELECTED, CornerRadii.EMPTY, Insets.EMPTY));
	
	private static String CASTLE_KINGSIDE = "castlekingside", CASTLE_QUEENSIDE = "castlequeenside", EN_PASSANT = "enpassant",
			PROMOTION = "promotion";
	
	static {
		defaultBoardPreset = new BoardPreset(DEFAULT_BOARD_SIZE);
		defaultBoardPreset.setTurn(Piece.WHITE);
		defaultBoardPreset.setCastlingAllowed(true);
		defaultBoardPreset.setEnPassantAllowed(true);
		defaultBoardPreset.setPieces(new String[][] {
			{"-Rook", "-Knight", "-Bishop", "-Queen", "-King", "-Bishop", "-Knight", "-Rook"},
			{"-Pawn", "-Pawn", "-Pawn", "-Pawn", "-Pawn", "-Pawn", "-Pawn", "-Pawn"},
			null, null, null, null,
			{"+Pawn", "+Pawn", "+Pawn", "+Pawn", "+Pawn", "+Pawn", "+Pawn", "+Pawn"},
			{"+Rook", "+Knight", "+Bishop", "+Queen", "+King", "+Bishop", "+Knight", "+Rook"}
		});
	}
	private EventHandler<? super MouseEvent> tileClickAction = event -> {
		if(!boardInteracitonAllowed) {
			System.out.println("click blocked because board interaction is not allowed.");
			return;
		}
		
		Tile source = (Tile) event.getSource();
		if(source.isShowingLegal()) {
			currentlySelectedTile.hideLegalMoves();
			currentlySelectedTile.setSelected(false);
			Board.this.makePlay(currentlySelectedTile.row, currentlySelectedTile.col, source.row, source.col);
		}
		else if(source.currentPiece != null && source.currentPiece.getColor() == turn) {
			//System.out.println(source.row + ", " + source.col + "; currentSelection = " + source.selected);
			source.setSelected(!source.selected);
		}
		else if(currentlySelectedTile != null){
			currentlySelectedTile.hideLegalMoves();
			currentlySelectedTile.setSelected(false);
			currentlySelectedTile = null;
		}
	};
	
	private EventHandler<? super MouseEvent> popupMessageXButtonClickAction = event -> {
		boardOverlay.setCenter(null);
		boardOverlay.setVisible(false);
	};
	
	private class GameMove{
		
		private Piece moved;
		private Piece captured;
		private int startRow, startCol;
		private int destRow, destCol;
		private String[] flags;
		
		private GameMove(Piece moved, Piece captured, int startRow, int startCol, int destRow, int destCol) {
			this.moved = moved;
			this.captured = captured;
			this.startRow = startRow;
			this.startCol = startCol;
			this.destRow = destRow;
			this.destCol = destCol;
			flags = new String[] {};
		}
		
		private GameMove(Piece moved, Piece captured, int startRow, int startCol, int destRow, int destCol,
				String... flags) {
			this.moved = moved;
			this.captured = captured;
			this.startRow = startRow;
			this.startCol = startCol;
			this.destRow = destRow;
			this.destCol = destCol;
			this.flags = flags;
		}
		
		public String toString() {
			return String.format("[GameMove = (%d, %d) to (%d, %d), %s takes %s, flags = %s]", startRow, startCol,
					destRow, destCol, moved, captured, Arrays.toString(flags));
		}
	}
	
	public static Board emptyBoard() {
		return new Board(DEFAULT_BOARD_SIZE);
	}
	
	public static Board emptyBoard(int size) {
		return new Board(size);
	}
	
	public static Board defaultBoard() {
		return new Board(defaultBoardPreset);
	}
	
	public static Board fromPreset(BoardPreset preset) {
		return new Board(preset);
	}
	
	private Board(int size) {
		if(size > MAX_BOARD_SIZE) {
			throw new IllegalArgumentException("Desired board size (" + size + ") is greater " + 
			"than MAX_BOARD_SIZE (26)");
		}
		
		BOARD_SIZE = size;
		turn = Piece.WHITE;
		this.preset = null;
		enPassantAllowed = true;
		castlingAllowed = BOARD_SIZE == 8;
		
		finishBoardInit();
		prepareForNextMove();
	}
	
	private Board(BoardPreset preset) {
		if(preset.getBoardSize() > MAX_BOARD_SIZE) {
			throw new IllegalArgumentException("Desired board size (" + preset.getBoardSize() + ") is greater " + 
			"than MAX_BOARD_SIZE (26)");
		}
		
		BOARD_SIZE = preset.getBoardSize();
		turn = preset.getTurn();
		enPassantAllowed = preset.getEnPassantAllowed();
		castlingAllowed = BOARD_SIZE != 8 ? false : preset.getCastlingAllowed();
		this.preset = preset;
		
		finishBoardInit();
		setupPiecesFromPreset(preset);
		prepareForNextMove();
	}
	
	private void finishBoardInit() {
		board = new Tile[BOARD_SIZE][BOARD_SIZE];
		boardDisplay = new Tile[BOARD_SIZE][BOARD_SIZE];
		kingLocations = new int[][] {{-1,-1},{-1,-1}};
		orientation = Piece.WHITE;
		moveList = new LinkedList<>();
		moveNumber = 1;
		playNumber = 1;
		movesSincePawnOrCapture = 0;
		initGUI();
	}

	private void initGUI() {
		grid = new GridPane();
		grid.setMinSize(200, 200);
		
		boardOverlay = new BorderPane();
		boardOverlay.setVisible(false);
		boardOverlay.setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		boardPopupMessage = new Text();
		boardPopupMessage.setFont(Font.font("Candara", FontWeight.NORMAL, FontPosture.REGULAR, 18));
		boardPopupBox = new StackPane();
		boardPopupBox.setMaxSize(300, 200);
		boardPopupBox.setPrefSize(300, 200);
		popupMessageArea = new GridPane();
		//popupMessageArea.setGridLinesVisible(true);
		RowConstraints row1 = new RowConstraints();
		row1.setValignment(VPos.CENTER);
		row1.setPercentHeight(25);
		RowConstraints row2 = new RowConstraints();
		row2.setValignment(VPos.CENTER);
		row2.setPercentHeight(25);
		RowConstraints row3 = new RowConstraints();
		row3.setValignment(VPos.CENTER);
		row3.setPercentHeight(50);
		popupMessageArea.getRowConstraints().addAll(row1, row2, row3);
		ColumnConstraints col1 = new ColumnConstraints();
		col1.setHalignment(HPos.CENTER);
		col1.setPercentWidth(50);
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setHalignment(HPos.CENTER);
		col2.setPercentWidth(50);
		popupMessageArea.getColumnConstraints().addAll(col1, col2);
		
		boardPopupBox.getChildren().add(popupMessageArea);
		//messageBox.setBackground(whiteBackground);
		popupMessageArea.setStyle("-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.8), 10, 0.5, 0.0, 0.0);"
				+ "-fx-background-radius: 5 5 5 5;"
				+ "-fx-border-radius: 5 5 5 5;"
				+ "-fx-background-color: #FFFFFF;");
		popupGameOverText = new Text("Game Over");
		popupGameOverText.setFont(Font.font("Century Gothic", FontWeight.SEMI_BOLD, FontPosture.REGULAR, 24));
		popupResetButton = new Button("Reset Board");
		popupResetButton.setBackground(light);
		//popupResetButton.getStyleClass().add("button1"); //TODO CSS
		
		popupResetButton.setStyle(
				"-fx-background-radius: 5 5 5 5;"
				+ "-fx-border-radius: 5 5 5 5;"
				+ "-fx-background-color: " + toRGBCode(LIGHT_COLOR) + ";");
		
		popupResetButton.setPrefSize(100, 50);
		popupResetButton.setOnMouseEntered(event -> {
			popupResetButton.setStyle("-fx-background-color: " + toRGBCode(LIGHT_COLOR_SELECTED) + ";");
		});
		popupResetButton.setOnMouseExited(event -> {
			popupResetButton.setStyle("-fx-background-color: " + toRGBCode(LIGHT_COLOR) + ";");
		});
		popupResetButton.setOnMouseClicked(event -> {
			setupPiecesFromPreset(preset);
			this.turn = preset.getTurn();
			moveList.clear();
			moveNumber = 1;
			playNumber = 1;
			movesSincePawnOrCapture = 0;
			boardOverlay.setCenter(null);
			boardOverlay.setVisible(false);
			prepareForNextMove();
		});
		popupViewBoardButton = new Button("View Board");
		popupViewBoardButton.setBackground(light);
		popupViewBoardButton.setStyle(
				"-fx-background-radius: 5 5 5 5;"
				+ "-fx-border-radius: 5 5 5 5;"
				+ "-fx-background-color: " + toRGBCode(LIGHT_COLOR) + ";");
		popupViewBoardButton.setPrefSize(100, 50);
		popupViewBoardButton.setOnMouseEntered(event -> {
			popupViewBoardButton.setStyle("-fx-background-color: " + toRGBCode(LIGHT_COLOR_SELECTED) + ";");
		});
		popupViewBoardButton.setOnMouseExited(event -> {
			popupViewBoardButton.setStyle("-fx-background-color: " + toRGBCode(LIGHT_COLOR) + ";");
		});
		popupViewBoardButton.setOnMouseClicked(event -> {
			boardOverlay.setCenter(null);
			boardOverlay.setVisible(false);
		});
		popupMessageArea.add(popupGameOverText, 0, 0, 2, 1);
		popupMessageArea.add(boardPopupMessage, 0, 1, 2, 1);
		popupMessageArea.add(popupResetButton, 0, 2, 1, 1);
		popupMessageArea.add(popupViewBoardButton, 1, 2, 1, 1);
		
		popupMessageOverlay = new AnchorPane();
		popupMessageOverlay.setPickOnBounds(false);
		popupMessageXButton = new Text("X");
		popupMessageXButton.setFill(DARK_COLOR);
		popupMessageXButton.setOnMouseClicked(popupMessageXButtonClickAction);
		FillTransition ft = new FillTransition(Duration.millis(100), popupMessageXButton, DARK_COLOR, DARK_COLOR_SELECTED);
		EventHandler<? super MouseEvent> popupMessageXButtonHoverEnter = event -> {
			ft.setRate(1.0);
			ft.play();
		};
		EventHandler<? super MouseEvent> popupMessageXButtonHoverExit = event -> {
			ft.setRate(-1.0);
			ft.play();
		};
		popupMessageXButton.setOnMouseEntered(popupMessageXButtonHoverEnter);
		popupMessageXButton.setOnMouseExited(popupMessageXButtonHoverExit);
		
		popupMessageXButton.setFont(Font.font("Gill Sans MT", FontWeight.BOLD, FontPosture.REGULAR, 20));
		AnchorPane.setRightAnchor(popupMessageXButton, 5.0);
		AnchorPane.setTopAnchor(popupMessageXButton, 5.0);
		popupMessageOverlay.getChildren().add(popupMessageXButton);
		
		boardPopupBox.setAlignment(Pos.CENTER);
		boardPopupBox.getChildren().add(popupMessageOverlay);
		
		for (int i = 0; i < BOARD_SIZE; i++) {
			final ColumnConstraints columnConstraints = new ColumnConstraints(Control.USE_PREF_SIZE, Control.USE_COMPUTED_SIZE, Double.MAX_VALUE);
            columnConstraints.setPercentWidth(100.0/BOARD_SIZE);
            grid.getColumnConstraints().add(columnConstraints);
            
            final RowConstraints rowConstraints = new RowConstraints(Control.USE_PREF_SIZE, Control.USE_COMPUTED_SIZE, Double.MAX_VALUE);
            rowConstraints.setPercentHeight(100.0/BOARD_SIZE);
            grid.getRowConstraints().add(rowConstraints);
        }
		
		
		
		for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
            	Tile child = new Tile(i, j);
	            board[i][j] = child;
                GridPane.setRowIndex(child, i);
                GridPane.setColumnIndex(child, j);
                grid.getChildren().add(child);
            }
        }
		
		for(int i = 0; i < BOARD_SIZE; i++) {
			for(int j = 0; j < BOARD_SIZE; j++) {
				boardDisplay[i][j] = board[i][j];
			}
		}
		
		this.getChildren().add(0, grid);
		this.getChildren().add(1, boardOverlay);
		
		
		promotionOptions = new TilePane();
		promotionBox = new StackPane();
		promotionBox.setStyle("-fx-background-color: #FFFF1F;");
		
		
		promotionBox.getChildren().add(promotionOptions);
		
		
		
	}
	
	private GridPane promotionGridPane;
	private ScrollPane promotionScrollPane;
	private StackPane promotionStackPane;
	
	private void promotion(Piece thePiece, int startRow, int startCol, int destRow, int destCol, Piece... options) {
		class PromotionIcon extends ImageView{
			
			Piece piece;
			
			public PromotionIcon(Piece p) {
				super(p.getImage());
				this.piece = p;
				this.setOnMouseClicked(event -> {
					promotionGridPane.getChildren().clear();
					setPieceAt(startRow, startCol, null);
					setPieceAt(destRow, destCol, piece);
					GameMove theMove = new GameMove(thePiece, getPieceAt(destRow,destCol), startRow, startCol,
							destRow, destCol, PROMOTION + p.getFullName());
					wrapUpMove(theMove);
					boardOverlay.setVisible(false);
				});
			}
		}
		promotionOptions.getChildren().clear();
		//promotionOptions.getChildren().add(new WrappedImageView(Knight.WHITE_IMAGE));
		boardOverlay.setCenter(null);
		promotionGridPane = new GridPane();
		promotionGridPane.setGridLinesVisible(true);
		for(int i = 0; i < options.length; i++) {
			promotionGridPane.add(new PromotionIcon(options[i]), i % 4, i / 4);
		}
		promotionScrollPane = new ScrollPane(promotionGridPane);
		promotionScrollPane.setFitToWidth(true);
		promotionStackPane = new StackPane();
		promotionStackPane.maxHeightProperty().bind(grid.heightProperty().divide(2));
		promotionStackPane.maxWidthProperty().bind(grid.widthProperty().divide(2));
		promotionStackPane.setStyle("-fx-background-color : #1FFFFE");
		promotionStackPane.getChildren().add(promotionScrollPane);
		boardOverlay.setCenter(promotionStackPane);
		boardOverlay.setVisible(true);
		boardOverlay.requestLayout();
	}
	
	
	
	private int[] getDisplayTileCoordinates(int row, int col) {
		if(this.orientation == Piece.WHITE) {
			return new int[] {row, col};
		}
		else {
			return new int[] {BOARD_SIZE - row - 1, BOARD_SIZE - col - 1};
		}
	}
	
	public static String toRGBCode( Color color )
    {
        return String.format( "#%02X%02X%02X",
            (int)( color.getRed() * 255 ),
            (int)( color.getGreen() * 255 ),
            (int)( color.getBlue() * 255 ) );
    }
	
	private void setupPiecesFromPreset(BoardPreset preset) {
		clearPieces();
		for(int i = 0; i < BOARD_SIZE; i++) {
			for(int j = 0; j < BOARD_SIZE; j++) {
				board[i][j].setPiece(Piece.forName(preset.getPieceNameAt(i, j)));
				if(board[i][j].currentPiece instanceof King) {
					if(board[i][j].currentPiece.getColor() == Piece.WHITE) {
						if(kingLocations[0][0] == -1) {
							kingLocations[0][0] = i;
							kingLocations[0][1] = j;
						}
						else {
							throw new IllegalArgumentException("Preset has too many white kings!");
						}
					}
					else {
						if(kingLocations[1][0] == -1) {
							kingLocations[1][0] = i;
							kingLocations[1][1] = j;
						}
						else {
							throw new IllegalArgumentException("Preset has too many black kings!");
						}
					}
				}
			}
		}
		
		if(kingLocations[0][0] == -1) {
			throw new IllegalArgumentException("Preset doesn't have a white king!");
		}
		if(kingLocations[1][0] == -1) {
			throw new IllegalArgumentException("Preset doesn't have a black king!");
		}
	}
	
	private void clearPieces() {
		for(int i = 0; i < BOARD_SIZE; i++) {
			for(int j = 0; j < BOARD_SIZE; j++) {
				board[i][j].setPiece(null);
			}
		}
		kingLocations[0][0] = kingLocations[0][1] = kingLocations[1][0] = kingLocations[1][1] = -1;
	}
	
	//TODO: REMOVE THIS LATER
	private void customSetup() {
		board[0][4].setPiece(new King(Piece.BLACK));
		board[7][4].setPiece(new King(Piece.WHITE));
		kingLocations[0] = new int[] {7, 4};
		kingLocations[1] = new int[] {0, 4};
		
		board[6][3].setPiece(new Queen(Piece.WHITE));
		board[4][1].setPiece(new Bishop(Piece.BLACK));
	}
	
	/* *
	 * Precondition:	1) startRow/startCol represents a Pawn
	 * 					2) enPassantAllowed is true
	 */
	boolean checkEnPassantLegality(int startRow, int startCol, int destRow, int destCol) {
		GameMove mostRecentMove = moveList.get(moveList.size() - 1);
		if(!(mostRecentMove.moved instanceof Pawn) || Math.abs(mostRecentMove.destRow - mostRecentMove.startRow) <= 1) {
			return false;
		}
		return 	mostRecentMove.destCol == destCol &&
				mostRecentMove.destRow == startRow;
	}
	
	public boolean castlingAllowed() {
		return castlingAllowed;
	}
	public boolean enPassantsAllowed() {
		return enPassantAllowed;
	}
	
	public int getBoardSize() {
		return BOARD_SIZE;
	}
	
	public boolean getBoardOrientation() {
		return orientation;
	}
	
	public Piece getPieceAt(String chessNotation) {
		int[] location = convertChessNotation(chessNotation);
		return board[location[0]][location[1]].currentPiece;
	}
	
	public Piece getPieceAt(int row, int col) {
		return board[row][col].currentPiece;
	}
	
	public ArrayList<int[]> getLegalMoves(String chessNotation) {
		int[] location = convertChessNotation(chessNotation);
		return board[location[0]][location[1]].getLegalMoves();
	}
	
	public boolean inBounds(int row, int col) {
		return row >= 0 && col >= 0 && row < BOARD_SIZE && col < BOARD_SIZE;
	}
	
	//PRECONDITION: rows & cols are valid, startRow/startCol represents a NON-KING piece.
	public boolean checkLegal(int startRow, int startCol, int destRow, int destCol) {
		return tryMoveForLegality(startRow, startCol, destRow, destCol);
	}
	
	//PRECONDITION: THE MOVE IS LEGAL
	public void makePlay(int startRow, int startCol, int destRow, int destCol) {
		Piece p = getPieceAt(startRow, startCol);
		
		p.setHasMoved(true);
		
		if(p instanceof Pawn && ((p.color == Piece.WHITE && destRow == 0) || (p.color == Piece.BLACK && destRow == BOARD_SIZE - 1))) {
			//TODO make this work for any promotion
			promotion(p, startRow, startCol, destRow, destCol, Piece.forName("Queen", p.color), Piece.forName("Rook", p.color),
					Piece.forName("Bishop", p.color), Piece.forName("Knight", p.color));
			return;
		}
		
		//keep track of kings!
		if(p instanceof King) {
			if(p.getColor() == Piece.WHITE) {
				kingLocations[0][0] = destRow;
				kingLocations[0][1] = destCol;
			}
			else {
				kingLocations[1][0] = destRow;
				kingLocations[1][1] = destCol;
			}
		}
		
		GameMove theMove = null;
		if(p instanceof King && Math.abs(startCol - destCol) > 1) { //this move is a castle.
			//At this point we can assume that: 1) the rooks and king are in place for castling
			//2) all squares between them are empty
			//3) the board size is 8x8.
			
			theMove = new GameMove(p, getPieceAt(destRow,destCol), startRow,
					startCol, destRow, destCol, 
					Math.min(7 - destCol, destCol) <= 1 ? CASTLE_KINGSIDE : CASTLE_QUEENSIDE);
			
			if(destCol > startCol) {
				setPieceAt(destRow,destCol, p);
				Piece rook = board[destRow][7].setPiece(null);
				setPieceAt(destRow, destCol - 1, rook);
			}
			else {
				setPieceAt(destRow,destCol, p);
				Piece rook = board[destRow][0].setPiece(null);
				setPieceAt(destRow, destCol + 1, rook);
			}
			setPieceAt(startRow, startCol, null);
			
		}
		else if(p instanceof Pawn && destCol != startCol && getPieceAt(destRow, destCol) == null) {
			//this move is an en passant
			theMove = new GameMove(p, getPieceAt(startRow,destCol), startRow,
					startCol, destRow, destCol, EN_PASSANT);
			setPieceAt(startRow, startCol, null);
			setPieceAt(startRow, destCol, null);
			setPieceAt(destRow, destCol, p);
			
		}
		else {
			theMove = new GameMove(p, getPieceAt(destRow,destCol), startRow,
					startCol, destRow, destCol);
			
			setPieceAt(startRow, startCol, null);
			setPieceAt(destRow, destCol, p);
		}
		
		wrapUpMove(theMove);
		
	}
	
	private void wrapUpMove(GameMove theMove) {
		moveList.add(theMove);
		System.out.println("Wrapping up move, incoming turn = " + turn);
		if(turn == Piece.WHITE) {
			turn = Piece.BLACK;
		}
		else {
			turn = Piece.WHITE;
			moveNumber++;
			if(fiftyMoveRule) {
				if(!(theMove.moved instanceof Pawn || theMove.captured != null ||
					moveList.get(moveList.size() - 2).moved instanceof Pawn ||
					moveList.get(moveList.size() - 2).captured != null)) {
					movesSincePawnOrCapture++;
				}
				else {
					movesSincePawnOrCapture = 0;
				}
			}
		}
		
		playNumber++;
			
		grid.requestLayout();
		
		prepareForNextMove();
	}
	
	/* *
	 * Precondition: It is assumed in this method that the king of the specified pieceColor
	 *  has not moved during this game AND that the board size is DEFAULT_BOARD_SIZE (8).
	 *  
	 *  This method should only be called from the King class.
	 */
	boolean isLegalCastle(final boolean pieceColor, final boolean side) {
		
		if(pieceColor == Piece.WHITE) {
			if(this.orientation == Piece.WHITE) {
				if(side == QUEENSIDE) {
					return 	board[7][0].currentPiece instanceof Rook &&
							!board[7][0].currentPiece.hasMoved() &&
							board[7][1].currentPiece == null &&
							board[7][2].currentPiece == null &&
							board[7][3].currentPiece == null;
				}
				else {
					return 	board[7][7].currentPiece instanceof Rook &&
							!board[7][7].currentPiece.hasMoved() &&
							board[7][6].currentPiece == null &&
							board[7][5].currentPiece == null;
				}
			}
			else {
				if(side == QUEENSIDE) {
					return 	board[0][7].currentPiece instanceof Rook &&
							!board[0][7].currentPiece.hasMoved() &&
							board[0][6].currentPiece == null &&
							board[0][5].currentPiece == null &&
							board[0][4].currentPiece == null;
				}
				else {
					return 	board[0][0].currentPiece instanceof Rook &&
							!board[0][0].currentPiece.hasMoved() &&
							board[0][1].currentPiece == null &&
							board[0][2].currentPiece == null;
				}
			}
		}
		else { //piece is black
			if(this.orientation == Piece.WHITE) {
				if(side == QUEENSIDE) {
					return 	board[0][0].currentPiece instanceof Rook &&
							!board[0][0].currentPiece.hasMoved() &&
							board[0][1].currentPiece == null &&
							board[0][2].currentPiece == null &&
							board[0][3].currentPiece == null;
				}
				else {
					return 	board[0][7].currentPiece instanceof Rook &&
							!board[0][7].currentPiece.hasMoved() &&
							board[0][6].currentPiece == null &&
							board[0][5].currentPiece == null;
				}
			}
			else {
				if(side == QUEENSIDE) {
					return 	board[7][7].currentPiece instanceof Rook &&
							!board[7][7].currentPiece.hasMoved() &&
							board[7][6].currentPiece == null &&
							board[7][5].currentPiece == null &&
							board[7][4].currentPiece == null;
				}
				else {
					return 	board[7][0].currentPiece instanceof Rook &&
							!board[7][0].currentPiece.hasMoved() &&
							board[7][1].currentPiece == null &&
							board[7][2].currentPiece == null;
				}
			}
		}
	}
	
	private Piece setPieceAt(int row, int col, Piece p) {
		return board[row][col].setPiece(p);
	}
	
	boolean tryMoveForLegality(int startRow, int startCol, int destRow, int destCol) {
		Piece p = getPieceAt(startRow, startCol);
		if(p == null) {
			throw new IllegalArgumentException("cannot move a nonexistent piece. startRow/startCol represents an empty square.");
		}
		else if(p instanceof King) {
			return tryMoveForLegalityKing(startRow, startCol, destRow, destCol);
		}
		else {
			return tryMoveForLegalityNonKing(startRow, startCol, destRow, destCol);
		}
	}
	//PRECONDITION: rows & cols are valid, startRow/startCol represents a NON-KING piece.
	private boolean tryMoveForLegalityNonKing(int startRow, int startCol, int destRow, int destCol) {
		Piece p = getPieceAt(startRow, startCol);
		Piece onTile = getPieceAt(destRow, destCol);
		board[startRow][startCol].currentPiece = null;
		board[destRow][destCol].currentPiece = p;
		
//		System.out.printf("trying move for legality (NonKing) : (%d, %d) -> (%d, %d), piece = %s", startRow,
//				startCol,destRow,destCol,p);
		
		int[] kingSpot = p.getColor() == Piece.WHITE ? kingLocations[0] : kingLocations[1];
		boolean result = true;
		boolean attackingColor = p.getColor() == Piece.WHITE ? Piece.BLACK : Piece.WHITE;
		outer:
		for(int i = 0; i < BOARD_SIZE; i++) {
			for(int j = 0; j < BOARD_SIZE; j++) {
				if(i == destRow && j == destCol) continue;
				Piece o = getPieceAt(i, j);
				if(	
					o != null && !(o instanceof King) && o.getColor() == attackingColor &&
					o.canCheck(Board.this, i, j, kingSpot[0], kingSpot[1])) {
					result = false;
					break outer;
				}
			}
		}
		
		board[startRow][startCol].currentPiece = p;
		board[destRow][destCol].currentPiece = onTile;
		
//		System.out.println("..." + result);
		return result;
	}
	/* *
	 * Precondition: All rows and columns are valid, startRow/startCol represents the location of a king.
	 */
	private boolean tryMoveForLegalityKing(int startRow, int startCol, int destRow, int destCol) {
		
		Piece p = getPieceAt(startRow, startCol);
		Piece onTile = getPieceAt(destRow, destCol);
		board[startRow][startCol].currentPiece = null;
		board[destRow][destCol].currentPiece = p;
		
//		System.out.printf("trying move for legality (King) : (%d, %d) -> (%d, %d), piece = %s", startRow,
//				startCol,destRow,destCol,p);
		
		boolean result = true;
		boolean attackingColor = p.getColor() == Piece.WHITE ? Piece.BLACK : Piece.WHITE;
		outer:
		for(int i = 0; i < BOARD_SIZE; i++) {
			for(int j = 0; j < BOARD_SIZE; j++) {
				if(i == destRow && j == destCol) continue;
				Piece o = getPieceAt(i, j);
				if(	
					o != null && o.getColor() == attackingColor &&
					o.canCheck(Board.this, i, j, destRow, destCol)) {
					result = false;
					break outer;
				}
			}
		}
		
		board[startRow][startCol].currentPiece = p;
		board[destRow][destCol].currentPiece = onTile;
		
		//System.out.println("..." + result);
		return result;
	}
	
	private void undoMove() {
		//TODO
	}
	
	private void flip() {
		Platform.runLater(new Runnable() {
			public void run() {
				Board.this.orientation = !Board.this.orientation;
				
				if(orientation == Piece.BLACK) {
					for(int i = 0; i < BOARD_SIZE; i++) {
						for(int j = 0; j < BOARD_SIZE; j++) {
							boardDisplay[i][j] = board[BOARD_SIZE - i - 1][BOARD_SIZE - j - 1];
						}
					}
				}
				else {
					for(int i = 0; i < BOARD_SIZE; i++) {
						for(int j = 0; j < BOARD_SIZE; j++) {
							boardDisplay[i][j] = board[i][j];
						}
					}
				}
				
				
				grid.getChildren().clear();
				for (int i = 0; i < BOARD_SIZE; i++) {
		            for (int j = 0; j < BOARD_SIZE; j++) {
		            	Tile child = boardDisplay[i][j];
		                GridPane.setRowIndex(child, i);
		                GridPane.setColumnIndex(child, j);
		                grid.getChildren().add(child);
		            }
		        }
			}
		});
		
	}
	
	/* *
	 * 
	 */
	private void prepareForNextMove() {
		boardInteracitonAllowed = false;
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				if(fiftyMoveRule && movesSincePawnOrCapture >= 50) {
					endGame("fifty");
					boardInteracitonAllowed = true;
					return;
				}
				boolean anyLegalMovesWhite = false;
				boolean anyLegalMovesBlack = false;
				ArrayList<Piece> whitePieces = new ArrayList<>(BOARD_SIZE/2);
				ArrayList<Piece> blackPieces = new ArrayList<>(BOARD_SIZE/2);
				for(int i = 0; i < BOARD_SIZE; i++) {
					for(int j = 0; j < BOARD_SIZE; j++) {
						if(board[i][j].calculateLegalMoves()) {
							if(board[i][j].currentPiece.getColor() == Piece.WHITE) {
								anyLegalMovesWhite = true;
							}
							else {
								anyLegalMovesBlack = true;
							}
						}
						Piece p;
						if((p = board[i][j].currentPiece) != null) {
							if(p.getColor() == Piece.WHITE) {
								whitePieces.add(p);
							}
							else {
								blackPieces.add(p);
							}
						}
					}
				}
				//Now check for insufficient material:
				MATERIAL_CHECK:
				{
					int wk = 0, bk = 0, wb = 0, bb = 0;
					boolean wbc = false, bbc = false;
					for(int i = 0; i < whitePieces.size(); i++) {
						Piece p = whitePieces.get(i);
						if(p instanceof King) {
							continue;
						}
						else if(p instanceof Knight) {
							wk++;
							if(wk > 2) {
								break MATERIAL_CHECK;
							}
						}
						else if(p instanceof Bishop) {
							wb++;
							if(wb > 1) {
								break MATERIAL_CHECK;
							}
							else {
								wbc = p.getColor();
							}
						}
						else {
							break MATERIAL_CHECK;
						}
					}
					for(int i = 0; i < blackPieces.size(); i++) {
						Piece p = blackPieces.get(i);
						if(p instanceof King) {
							continue;
						}
						else if(p instanceof Knight) {
							bk++;
							if(bk > 2) {
								break MATERIAL_CHECK;
							}
						}
						else if(p instanceof Bishop) {
							bb++;
							if(bb > 1) {
								break MATERIAL_CHECK;
							}
							else {
								bbc = p.getColor();
							}
						}
						else {
							break MATERIAL_CHECK;
						}
					}
					/* *
					 * ONLY the following conditions will cause an automatic draw:
					 * 
					 * King vs king with no other pieces.
					 * King and bishop vs king.
					 * King and knight vs king.
					 * King and bishop vs king and bishop of the same coloured square.
					 * 
					 * Although there are other conditions where neither player can FORCE mate,
					 * those positions are not an automatic draw because a mate could still be
					 * achieved if one player "helps" the other.
					 */
					if(	wk == 0 && bk == 0 && wb == 0 && bb == 0 ||
						wk == 0 && bk == 0 && (wb == 1 ^ bb == 1) ||
						(wk == 0 ^ bk == 0) && wb == 0 && bb == 0 ||
						wk == 0 && bk == 0 && wb == 1 && bb == 1 && wbc == bbc) {
						endGame("material");
					}
					else {
						break MATERIAL_CHECK;
					}	
				}
				
				//Now check for checkmate/stalemate:
				if(Board.this.turn == Piece.WHITE) {
					if(!anyLegalMovesWhite) {
						boolean isCheckmate = false;
						OUTER:
						for(int i = 0; i < BOARD_SIZE; i++) {
							for(int j = 0; j < BOARD_SIZE; j++) {
								Piece p = board[i][j].currentPiece;
								if(p != null && p.getColor() == Piece.BLACK && p.canCheck(Board.this, i, j, kingLocations[0][0], kingLocations[0][1])){
									isCheckmate = true;
									break OUTER;
								}
							}
						}
						if(isCheckmate) {
							endGame("black");
						}
						else {
							endGame("stalemate");
						}
					}
				}
				else {
					if(!anyLegalMovesBlack) {
						boolean isCheckmate = false;
						OUTER:
						for(int i = 0; i < BOARD_SIZE; i++) {
							for(int j = 0; j < BOARD_SIZE; j++) {
								Piece p = board[i][j].currentPiece;
								if(p != null && p.getColor() == Piece.WHITE && p.canCheck(Board.this, i, j, kingLocations[1][0], kingLocations[1][1])){
									isCheckmate = true;
									break OUTER;
								}
							}
						}
						if(isCheckmate) {
							endGame("white");
						}
						else {
							endGame("stalemate");
						}
					}
				}
				boardInteracitonAllowed = true;
			}
		});
		t.setPriority(6);
		t.start();
	}
	
	private void endGame(String result) {
		Platform.runLater(new Runnable() {
			public void run() {
				if("white".equals(result)) {
					boardPopupMessage.setText("White wins by checkmate");
					boardOverlay.setCenter(boardPopupBox);
					boardOverlay.setVisible(true);
				}
				else if("black".equals(result)) {
					boardPopupMessage.setText("Black wins by checkmate");
					boardOverlay.setCenter(boardPopupBox);
					boardOverlay.setVisible(true);
				}
				else if("stalemate".equals(result)) {
					boardPopupMessage.setText("Draw by stalemate");
					boardOverlay.setCenter(boardPopupBox);
					boardOverlay.setVisible(true);
				}
				else if("fifty".equals(result)) {
					boardPopupMessage.setText("Draw by 50 move-rule");
					boardOverlay.setCenter(boardPopupBox);
					boardOverlay.setVisible(true);
				}
				else if("material".equals(result)) {
					boardPopupMessage.setText("Draw by insufficient material");
					boardOverlay.setCenter(boardPopupBox);
					boardOverlay.setVisible(true);
				}
			}
		});
		
	}
	
	private int[] convertChessNotation(String chessNotation) {
		int row, col;
		
		if(chessNotation.length() == 3) {
			chessNotation = chessNotation.toUpperCase();
			col = chessNotation.charAt(0) - 'A';
			row = 10 * (chessNotation.charAt(1) - '0') + (chessNotation.charAt(2) - '1');
		}
		else if(chessNotation.length() == 2) {
			chessNotation = chessNotation.toUpperCase();
			col = chessNotation.charAt(0) - 'A';
			row = chessNotation.charAt(1) - '1';
		}
		else {
			throw new IllegalArgumentException("Tile " + chessNotation + " is not valid for board size " + BOARD_SIZE);
		}
		if(row >= BOARD_SIZE || col >= BOARD_SIZE || row < 0 || col < 0)
			throw new IllegalArgumentException("Tile " + chessNotation + " is not valid for board size " + BOARD_SIZE);
		
		return new int[]{row, col};
	}
	
	private class Tile extends StackPane{
		
		private Piece currentPiece;
		private char boardFile;
		private char boardRank;
		private int row;
		private int col;
		private Background color;
		private boolean selected;
		private boolean isShowingLegal;
		private ArrayList<int[]> legalMoves;
		private Circle legalMoveIndicator;
		private WrappedImageView currentImageView;
		
		public Tile(int row, int col) {
			super();
			this.row = row;
			this.col = col;
			boardFile = (char) ('A' + col);
			boardRank = (char) ('1' + (BOARD_SIZE - row - 1));
			selected = false;
			isShowingLegal = false;
			currentPiece = null;
			legalMoveIndicator = new Circle(8);
			legalMoveIndicator.setFill(LEGAL_MOVE_COLOR);
			currentImageView = null;
            this.setBackground(color = (row+col) % 2 == 0 ? light : dark);
            this.setOnMouseClicked(tileClickAction);
		}
		private ArrayList<int[]> getLegalMoves() {
			return legalMoves;
		}
		
		/* *
		 * returns true iff one or more legal moves are found, false otherwise.
		 */
		private boolean calculateLegalMoves() {
			this.legalMoves = currentPiece == null ? null : currentPiece.getLegalMoves(Board.this, row, col);
			return this.legalMoves != null && this.legalMoves.size() > 0;
		}
		
		private Piece setPiece(Piece p) {
			if(p == null && currentPiece == null) return null;
			Piece temp = currentPiece;
			currentPiece = p;
			
			if(currentPiece == null) {
				this.getChildren().remove(currentImageView);
				currentImageView = null;
				this.legalMoves = null;
			}
			else {
				this.getChildren().remove(currentImageView);
				this.getChildren().add(0, currentImageView = new WrappedImageView(p.getImage()));
			}
			this.requestLayout();
			return temp;
		}
		
		
		private void setSelected(boolean status) {
			if(status == selected) return;
			selected = status;
			if(selected) {
				if(currentlySelectedTile != null)
					currentlySelectedTile.setSelected(false);
				this.setBackground((row+col) % 2 == 0 ? lightSelected : darkSelected);
				currentlySelectedTile = this;
				if(currentPiece != null) {
					//System.out.println("legalMoves : " + legalMoves);
					for(int[] spot : legalMoves) {
						board[spot[0]][spot[1]].setShowingLegal(true);
					}
				}
			}
			else {
				this.setBackground((row+col) % 2 == 0 ? light : dark);
				this.hideLegalMoves();
			}
			this.requestLayout();
		}
		
		private void hideLegalMoves() {
			if(legalMoves != null) {
				for(int[] spot : legalMoves)
					board[spot[0]][spot[1]].setShowingLegal(false);
			}
		}
		
		private void setShowingLegal(boolean status) {
			if(status == isShowingLegal) return;
			
			isShowingLegal = status;
			if(isShowingLegal == true) {
				this.getChildren().add(legalMoveIndicator);
			}
			else {
				this.getChildren().remove(legalMoveIndicator);
			}
		}
		
		private boolean isShowingLegal() {
			return isShowingLegal;
		}
	}
}
