package chess;

import java.util.ArrayList;

import javafx.scene.image.Image;

/* *
 * @author Sam Hooper
 */
public class Knight extends Piece{
	
	private static int moveA;
	private static int moveB;
	private static int[][] moves;
	public static final Image BLACK_IMAGE;
	public static final Image WHITE_IMAGE;
	
	static {
		BLACK_IMAGE = new Image(Piece.class.getResourceAsStream("/resources/knight_black.png"));
		WHITE_IMAGE = new Image(Piece.class.getResourceAsStream("/resources/knight_white.png"));
		moveA = 1;
		moveB = 2;
		moves = new int[8][2];
		
		updateMoves();
	}
	
	public Knight(boolean color) {
		super(color);
	}
	
	public static int[] getRules() {
		return new int[] {moveA, moveB};
	}
	public static void setRules(int newMoveA, int newMoveB) {
		moveA = newMoveA;
		moveB = newMoveB;
		updateMoves();
	}
	
	private static void updateMoves() {
		moves[0][0] = moveA;	moves[0][1] = moveB;
		moves[1][0] = moveA;	moves[1][1] = -moveB;
		moves[2][0] = -moveA;	moves[2][1] = moveB;
		moves[3][0] = -moveA;	moves[3][1] = -moveB;
		moves[4][0] = moveB; 	moves[4][1] = moveA;
		moves[5][0] = moveB; 	moves[5][1] = -moveA;
		moves[6][0] = -moveB;	moves[6][1] = moveA;
		moves[7][0] = -moveB;	moves[7][1] = -moveA;
	}
	
	@Override
	public ArrayList<int[]> getLegalMoves(Board b, int row, int col) {
		ArrayList<int[]> legalMoves = new ArrayList<int[]>(8);
		for(int[] move : moves) {
			Piece p;
			int nr = row + move[0];
			int nc = col + move[1];
			if(b.inBounds(nr, nc) && ((p = b.getPieceAt(nr, nc)) == null || p.getColor() != this.getColor()) && b.tryMoveForLegality(row, col, nr, nc))
				legalMoves.add(new int[] {nr, nc});
		}
		
		return legalMoves;
	}
	@Override
	public boolean canCheck(Board b, int startRow, int startCol, int destRow, int destCol) {
		int aDiff = Math.abs(startRow - destRow);
		int bDiff = Math.abs(startCol - destCol);
		return aDiff == moveA && bDiff == moveB || aDiff == moveB && bDiff == moveA;
	}
	@Override
	public Image getImage() {
		return getColor() == Piece.WHITE ? WHITE_IMAGE : BLACK_IMAGE;
	}
	
	public String toString() {
		return getColorString() + " Knight";
	}

}
