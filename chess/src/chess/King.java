package chess;

import java.util.ArrayList;

import javafx.scene.image.Image;

public class King extends Piece{
	
	private static final Image BLACK_IMAGE;
	private static final Image WHITE_IMAGE;
	private static final int[][] moves;
	static {
		BLACK_IMAGE = new Image(Piece.class.getResourceAsStream("/resources/king_black.png"));
		WHITE_IMAGE = new Image(Piece.class.getResourceAsStream("/resources/king_white.png"));
		moves = new int[][] {
			{0,1},
			{-1,1},
			{-1,0},
			{-1,-1},
			{0,-1},
			{1,-1},
			{1,0},
			{1,1},
		};
	}
	
	public King(boolean color) {
		super(color);
	}
	
	@Override
	public ArrayList<int[]> getLegalMoves(Board b, int row, int col) {
		ArrayList<int[]> legalMoves = new ArrayList<>();
		for(int[] move : moves) {
			int nr = row + move[0], nc = col + move[1];
			Piece p;
			if(b.inBounds(nr, nc) && ((p = b.getPieceAt(nr, nc)) == null || p.getColor() != this.getColor()) &&
					b.tryMoveForLegality(row, col, nr, nc)){
				legalMoves.add(new int[] {nr, nc});
			}
		}
		
		if(b.getBoardSize() == Board.DEFAULT_BOARD_SIZE) { //castling is only enabled for size 8 (default) boards.
			if(!hasMoved()) {
				if(b.castlingAllowed(getColor(), Board.QUEENSIDE)) {
					legalMoves.add(new int[] {row, col == 4 ? 2 : 5});
				}
				if(b.castlingAllowed(getColor(), Board.KINGSIDE)) {
					legalMoves.add(new int[] {row, col == 4 ? 6 : 2});
				}
			}
		}
				
		return legalMoves;
	}

	@Override
	public boolean canCheck(Board b, int startRow, int startCol, int destRow, int destCol) {
		return false;
	}

	@Override
	public Image getImage() {
		return getColor() == Piece.WHITE ? WHITE_IMAGE : BLACK_IMAGE;
	}
	
	public String toString() {
		return getColorString() + " King";
	}
	
	

	

}
