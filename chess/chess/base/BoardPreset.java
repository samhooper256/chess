package chess.base;

import java.util.Arrays;

import chess.util.ReadOnlyIntAttribute;

/* *
 * Mutable class representing a board preset.
 * The only immutable property is the board size.
 * 
 * @author 	Sam Hooper
 */
public class BoardPreset {
	private final ReadOnlyIntAttribute size;
	private boolean turn, fiftyMoveRule;
	private String[][] pieceNames;
	
	public BoardPreset() {
		size = new ReadOnlyIntAttribute(8);
		turn = Piece.WHITE;
		fiftyMoveRule = true;
		pieceNames = new String[size.getAsInt()][size.getAsInt()];
	}
	
	public BoardPreset(int size) {
		this.size = new ReadOnlyIntAttribute(size);
		turn = Piece.WHITE;
		fiftyMoveRule = true;
		pieceNames = new String[this.size.getAsInt()][this.size.getAsInt()];
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
	
	public int getBoardSizeAsInt() {
		return size.getAsInt();
	}
	
	public void setFiftyMoveRule(boolean fiftyMoveRule) {
		this.fiftyMoveRule = fiftyMoveRule;
	}
	
	public boolean getFiftyMoveRule() {
		return this.fiftyMoveRule;
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
		if(pieceNamesInit.length != size.getAsInt()) {
			throw new IllegalArgumentException("Invalid number of rows (" + pieceNamesInit.length + "), should be " + size);
		}
		for(int i = 0; i < size.getAsInt(); i++) {
			if(pieceNamesInit[i] == null) {
				pieceNames[i] = new String[size.getAsInt()];
			}
			else {
				pieceNames[i] = Arrays.copyOf(pieceNamesInit[i], size.getAsInt());
			}
		}
	}
	
	public String[][] getPieces() {
		String[][] end = new String[size.getAsInt()][size.getAsInt()];
		for(int i = 0; i < size.getAsInt(); i++) {
			end[i] = Arrays.copyOf(pieceNames[i], size.getAsInt());
		}
		return end;
	}
	
	
}
