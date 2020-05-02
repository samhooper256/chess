package chess;

import java.util.ArrayList;

import javafx.scene.image.Image;

/* *
 * @author Sam Hooper
 */
public class Rook extends Piece {

	public static final Image BLACK_IMAGE;
	public static final Image WHITE_IMAGE;
	
	static {
		BLACK_IMAGE = new Image(Piece.class.getResourceAsStream("/resources/rook_black.png"));
		WHITE_IMAGE = new Image(Piece.class.getResourceAsStream("/resources/rook_white.png"));
	}
	
	public Rook(boolean color) {
		super(color);
	}

	@Override
	public ArrayList<int[]> getLegalMoves(Board b, int row, int col) {
		ArrayList<int[]> legalMoves = new ArrayList<>();
		//search to its left:
		for(int c = col - 1; c >= 0; c--) {
			Piece p = b.getPieceAt(row, c);
			if(p == null) {
				if(b.tryMoveForLegality(row, col, row, c)) {
					legalMoves.add(new int[] {row, c});
				}
			}
			else {
				if(p.getColor() != this.getColor()) {
					if(b.tryMoveForLegality(row, col, row, c)) {
						legalMoves.add(new int[] {row, c});
					}
				}
				break;
			}
		}
		//search to its right:
		for(int c = col + 1; c < b.getBoardSize(); c++) {
			Piece p = b.getPieceAt(row, c);
			if(p == null) {
				if(b.tryMoveForLegality(row, col, row, c)) {
					legalMoves.add(new int[] {row, c});
				}
			}
			else {
				if(p.getColor() != this.getColor()) {
					if(b.tryMoveForLegality(row, col, row, c)) {
						legalMoves.add(new int[] {row, c});
					}
				}
				break;
			}
		}
		//search to its up:
		for(int r = row - 1; r >= 0; r--) {
			Piece p = b.getPieceAt(r, col);
			if(p == null) {
				if(b.tryMoveForLegality(row, col, r, col)) {
					legalMoves.add(new int[] {r, col});
				}
			}
			else {
				if(p.getColor() != this.getColor()) {
					if(b.tryMoveForLegality(row, col, r, col)) {
						legalMoves.add(new int[] {r,col});
					}
				}
				break;
			}
		}
		//search to its down:
		for(int r = row + 1; r < b.getBoardSize(); r++) {
			Piece p = b.getPieceAt(r, col);
			if(p == null) {
				if(b.tryMoveForLegality(row, col, r, col)) {
					legalMoves.add(new int[] {r, col});
				}
			}
			else {
				if(p.getColor() != this.getColor()) {
					if(b.tryMoveForLegality(row, col, r, col)) {
						legalMoves.add(new int[] {r,col});
					}
				}
				break;
			}
		}
		
		return legalMoves;
	}

	@Override
	public boolean canCheck(Board b, int startRow, int startCol, int destRow, int destCol) {
		if(destRow == startRow) {
			if(destCol < startCol) { //enemy is to the LEFT
				for(int c = startCol - 1; c > destCol; c--) {
					Piece p = b.getPieceAt(startRow, c);
					if(p != null) {
						return false;
					}
				}
				return true;
			}
			else { //enemy is to the RIGHT
				for(int c = startCol + 1; c < destCol; c++) {
					Piece p = b.getPieceAt(startRow, c);
					if(p != null) {
						return false;
					}
				}
				return true;
			}
		}
		else if(destCol == startCol) {
			if(destRow < startRow) { //enemy is to the TOP
				for(int r = startRow - 1; r > destRow; r--) {
					Piece p = b.getPieceAt(r, startCol);
					if(p != null) {
						return false;
					}
				}
				return true;
			}
			else { //enemy is to the BOTTOM
				for(int r = startRow + 1; r < destRow; r++) {
					Piece p = b.getPieceAt(r, startCol);
					if(p != null) {
						return false;
					}
				}
				return true;
			}
		}
		else {
			return false;
		}
	}

	@Override
	public Image getImage() {
		return getColor() == Piece.WHITE ? WHITE_IMAGE : BLACK_IMAGE;
	}
	
	public String toString() {
		return getColorString() + " Rook";
	}

}
