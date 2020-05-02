package chess;

import java.util.Arrays;

/* *
 * Mutable class representing a board preset.
 * The only immutable property is the board size.
 * 
 * @author 	Sam Hooper
 */
public class BoardPreset {
	private final int size;
	private boolean turn, castlingAllowed, enPassantAllowed;
	private String[][] pieceNames;
	
	public BoardPreset() {
		size = 8;
		turn = Piece.WHITE;
		pieceNames = new String[size][size];
	}
	
	public BoardPreset(int size) {
		this.size = size;
		turn = Piece.WHITE;
		pieceNames = new String[size][size];
	}
	
	public void setTurn(boolean color) {
		this.turn = color;
	}
	
	public boolean getTurn() {
		return turn;
	}
	
	public void setPieceNameAt(int row, int col, String name) {
		pieceNames[row][col] = name;
	}
	
	public String getPieceNameAt(int row, int col) {
		return pieceNames[row][col];
	}
	
	public int getBoardSize() {
		return size;
	}
	
	public boolean getCastlingAllowed() {
		return castlingAllowed;
	}
	
	public void setCastlingAllowed(boolean castlingAllowed) {
		this.castlingAllowed = castlingAllowed;
	}
	
	public boolean getEnPassantAllowed() {
		return enPassantAllowed;
	}
	
	public void setEnPassantAllowed(boolean enPassantAllowed) {
		this.enPassantAllowed = enPassantAllowed;
	}
	
	/* *
	 * Expects {@code size} String arrays each with with {@code size} elements.
	 * Copies the given array into its internal array.
	 * 
	 * Each String[] represents a row in the board, going from the top to the bottom.
	 * 
	 * Null can be passed to indicate a row with no pieces.
	 * 
	 * NOTE: This method does not ensure that the given piece names are valid.
	 */
	public void setPieces(String[]... pieceNamesInit) {
		if(pieceNamesInit.length != size) {
			throw new IllegalArgumentException("Invalid number of rows (" + pieceNamesInit.length + "), should be " + size);
		}
		for(int i = 0; i < size; i++) {
			if(pieceNamesInit[i] == null) {
				pieceNames[i] = new String[size];
			}
			else {
				pieceNames[i] = Arrays.copyOf(pieceNamesInit[i], size);
			}
		}
	}
	
	public String[][] getPieces() {
		String[][] end = new String[size][size];
		for(int i = 0; i < size; i++) {
			end[i] = Arrays.copyOf(pieceNames[i], size);
		}
		return end;
	}
	
	
}
