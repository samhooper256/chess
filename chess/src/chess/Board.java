package chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Board extends StackPane{
	public static final int MAX_BOARD_SIZE = 26;
	public static final int DEFAULT_BOARD_SIZE = 8;
	public static boolean QUEENSIDE = true;
	public static boolean KINGSIDE = false;
	private final int BOARD_SIZE;
	private final Tile[][] board;
	private final boolean enPassantAllowed;
	private int moveNumber;
	private int playNumber;
	private CheckStatus checkStatus;
	private LinkedList<GameMove> moveList;
	private int[][] kingLocations; //kingLocations[0] is white king, kingLocations[1] is black king
	private Tile currentlySelectedTile;
	private boolean turn;
	private boolean orientation;
	private boolean castlingAllowed;
	private boolean boardInteracitonAllowed = true;
	
	//UI Components
	private final Tile[][] boardDisplay;
	private final GridPane grid = new GridPane();
	
	private static Color LIGHT_COLOR = Color.rgb(26, 187, 154, 1);
	private static Color DARK_COLOR = Color.rgb(44, 61, 79,1);
	private static Color LIGHT_COLOR_SELECTED = Color.rgb(56, 232, 196,1);
	private static Color DARK_COLOR_SELECTED = Color.rgb(69, 87, 107, 1);
	private static Color LEGAL_MOVE_COLOR = Color.rgb(0, 13, 255, 0.5);
	private static Background light = new Background(new BackgroundFill(LIGHT_COLOR, CornerRadii.EMPTY, Insets.EMPTY));
	private static Background dark = new Background(new BackgroundFill(DARK_COLOR, CornerRadii.EMPTY, Insets.EMPTY));
	private static Background lightSelected = new Background(new BackgroundFill(LIGHT_COLOR_SELECTED, CornerRadii.EMPTY, Insets.EMPTY));
	private static Background darkSelected = new Background(new BackgroundFill(DARK_COLOR_SELECTED, CornerRadii.EMPTY, Insets.EMPTY));
	
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
			System.out.println(source.row + ", " + source.col + "; currentSelection = " + source.selected);
			source.setSelected(!source.selected);
		}
		else if(currentlySelectedTile != null){
			currentlySelectedTile.hideLegalMoves();
			currentlySelectedTile.setSelected(false);
			currentlySelectedTile = null;
		}
	};
	
	public enum CheckStatus{
		WHITE, BLACK, NONE;
	}
	
	private enum SpecialMoveFlag{
		EN_PASSANT, CASTLE_QUEENSIDE, CASTLE_KINGSIDE;
	}
	
	private class GameMove{
		
		private Piece moved;
		private Piece captured;
		private int startRow, startCol;
		private int destRow, destCol;
		private SpecialMoveFlag[] flags;
		
		private GameMove(Piece moved, Piece captured, int startRow, int startCol, int destRow, int destCol) {
			this.moved = moved;
			this.captured = captured;
			this.startRow = startRow;
			this.startCol = startCol;
			this.destRow = destRow;
			this.destCol = destCol;
			flags = new SpecialMoveFlag[] {};
		}
		
		private GameMove(Piece moved, Piece captured, int startRow, int startCol, int destRow, int destCol,
				SpecialMoveFlag... flags) {
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
	
	public Board() {
		BOARD_SIZE = DEFAULT_BOARD_SIZE;
		board = new Tile[BOARD_SIZE][BOARD_SIZE];
		boardDisplay = new Tile[BOARD_SIZE][BOARD_SIZE];
		kingLocations = new int[][] {{-1,-1},{-1,-1}};
		turn = Piece.WHITE;
		orientation = Piece.WHITE;
		moveList = new LinkedList<>();
		enPassantAllowed = true;
		moveNumber = 0;
		playNumber = 0;
		castlingAllowed = true;
		initGUI();
		
		defaultSetup(); //TODO: Change to defaultSetup();
		
		calculateAllLegalMoves();
		
		//TODO delete!!!
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				Scanner sc = new Scanner(System.in);
				while(true) {
					if(!sc.nextLine().contentEquals("stop!")) {
						System.out.println("flipping");
						Board.this.flip();
					}
				}
			}
			
		}).start();
		
	}
	
	public Board(int size) {
		if(size > MAX_BOARD_SIZE)
			throw new IllegalArgumentException("Desired board size (" + size + ") is greater " + 
			"than MAX_BOARD_SIZE (26)");
		BOARD_SIZE = size;
		board = new Tile[BOARD_SIZE][BOARD_SIZE];
		boardDisplay = new Tile[BOARD_SIZE][BOARD_SIZE];
		kingLocations = new int[][] {{-1,-1},{-1,-1}};
		turn = Piece.WHITE;
		orientation = Piece.WHITE;
		moveList = new LinkedList<>();
		enPassantAllowed = true;
		moveNumber = 0;
		playNumber = 0;
		castlingAllowed = BOARD_SIZE == 8;
		
		initGUI();
		
		calculateAllLegalMoves();
	}

	private void initGUI() {
		grid.setMinSize(200, 200);
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
		
		this.getChildren().add(grid);
	}
	
	private void defaultSetup() {
		if(BOARD_SIZE != 8) return; //returns silently - this method should never be called when the size != 8.
		
		board[0][0].setPiece(new Rook(Piece.BLACK));
		board[0][1].setPiece(new Knight(Piece.BLACK));
		board[0][2].setPiece(new Bishop(Piece.BLACK));
		board[0][3].setPiece(new Queen(Piece.BLACK));
		board[0][4].setPiece(new King(Piece.BLACK));
		board[0][5].setPiece(new Bishop(Piece.BLACK));
		board[0][6].setPiece(new Knight(Piece.BLACK));
		board[0][7].setPiece(new Rook(Piece.BLACK));
		
		board[1][0].setPiece(new Pawn(Piece.BLACK));
		board[1][1].setPiece(new Pawn(Piece.BLACK));
		board[1][2].setPiece(new Pawn(Piece.BLACK));
		board[1][3].setPiece(new Pawn(Piece.BLACK));
		board[1][4].setPiece(new Pawn(Piece.BLACK));
		board[1][5].setPiece(new Pawn(Piece.BLACK));
		board[1][6].setPiece(new Pawn(Piece.BLACK));
		board[1][7].setPiece(new Pawn(Piece.BLACK));
		
		board[7][0].setPiece(new Rook(Piece.WHITE));
		board[7][1].setPiece(new Knight(Piece.WHITE));
		board[7][2].setPiece(new Bishop(Piece.WHITE));
		board[7][3].setPiece(new Queen(Piece.WHITE));
		board[7][4].setPiece(new King(Piece.WHITE));
		board[7][5].setPiece(new Bishop(Piece.WHITE));
		board[7][6].setPiece(new Knight(Piece.WHITE));
		board[7][7].setPiece(new Rook(Piece.WHITE));
		
		board[6][0].setPiece(new Pawn(Piece.WHITE));
		board[6][1].setPiece(new Pawn(Piece.WHITE));
		board[6][2].setPiece(new Pawn(Piece.WHITE));
		board[6][3].setPiece(new Pawn(Piece.WHITE));
		board[6][4].setPiece(new Pawn(Piece.WHITE));
		board[6][5].setPiece(new Pawn(Piece.WHITE));
		board[6][6].setPiece(new Pawn(Piece.WHITE));
		board[6][7].setPiece(new Pawn(Piece.WHITE));
		

		kingLocations[0] = new int[] {7, 4};
		kingLocations[1] = new int[] {0, 4};
		
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
	
	public CheckStatus getCheckStatus() {
		return checkStatus;
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
					Math.min(7 - destCol, destCol) <= 1 ? SpecialMoveFlag.CASTLE_KINGSIDE : SpecialMoveFlag.CASTLE_QUEENSIDE);
			
			System.out.println("DIFF = " + Math.abs(destCol - startCol));
			
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
					startCol, destRow, destCol, SpecialMoveFlag.EN_PASSANT);
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
		
		
		moveList.add(theMove);
		
		System.out.println(theMove); //TODO delete
		
		p.setHasMoved(true);
		
		
		
		if(turn == Piece.WHITE) {
			turn = Piece.BLACK;
		}
		else {
			turn = Piece.WHITE;
			moveNumber++;
		}
		
		playNumber++;
			
		grid.requestLayout();
		
		calculateAllLegalMoves();
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
		
		System.out.printf("trying move for legality (King) : (%d, %d) -> (%d, %d), piece = %s", startRow,
				startCol,destRow,destCol,p);
		
		boolean result = true;
		boolean attackingColor = p.getColor() == Piece.WHITE ? Piece.BLACK : Piece.WHITE;
		outer:
		for(int i = 0; i < BOARD_SIZE; i++) {
			for(int j = 0; j < BOARD_SIZE; j++) {
				if(i == destRow && j == destCol) continue;
				Piece o = getPieceAt(i, j);
				if(	
					o != null && !(o instanceof King) && o.getColor() == attackingColor &&
					o.canCheck(Board.this, i, j, destRow, destCol)) {
					result = false;
					break outer;
				}
			}
		}
		
		board[startRow][startCol].currentPiece = p;
		board[destRow][destCol].currentPiece = onTile;
		
		System.out.println("..." + result);
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
	private void calculateAllLegalMoves() {
		boardInteracitonAllowed = false;
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				boolean anyLegalMovesWhite = false;
				boolean anyLegalMovesBlack = false;
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
					}
				}
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
							System.out.println("Black wins by checkmate."); //TODO
						}
						else {
							System.out.println("Draw by stalemate."); //TODO
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
							System.out.println("White wins by checkmate."); //TODO
						}
						else {
							System.out.println("Draw by stalemate."); //TODO
						}
					}
				}
				boardInteracitonAllowed = true;
			}
		});
		t.setPriority(6);
		t.start();
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
            this.setBackground((row+col) % 2 == 0 ? light : dark);
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
					System.out.println("legalMoves : " + legalMoves);
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
