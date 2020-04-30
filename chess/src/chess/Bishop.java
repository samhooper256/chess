package chess;

import java.util.ArrayList;

import javafx.scene.image.Image;

public class Bishop extends Piece {
	private static final Image BLACK_IMAGE;
	private static final Image WHITE_IMAGE;
	
	static {
		BLACK_IMAGE = new Image(Piece.class.getResourceAsStream("/resources/bishop_black.png"));
		WHITE_IMAGE = new Image(Piece.class.getResourceAsStream("/resources/bishop_white.png"));
	}
	
	public Bishop(boolean color) {
		super(color);
	}
	
	@Override
	public ArrayList<int[]> getLegalMoves(Board b, int row, int col) {
		ArrayList<int[]> legalMoves = new ArrayList<>();
		
		for(int r = row + 1, c = col + 1; b.inBounds(r, c); r++, c++) { //searches toward BOTTOM RIGHT
			Piece p = b.getPieceAt(r,c);
			if(p == null) {
				if(b.tryMoveForLegality(row, col, r, c)) {
					legalMoves.add(new int[] {r,c});
				}
			}
			else {
				if(p.getColor() != this.getColor()) {
					if(b.tryMoveForLegality(row, col, r, c)) {
						legalMoves.add(new int[] {r,c});
					}
				}
				break;
			}
		}
		
		for(int r = row + 1, c = col - 1; b.inBounds(r, c); r++, c--) { //searches toward BOTTOM LEFT
			Piece p = b.getPieceAt(r,c);
			if(p == null) {
				if(b.tryMoveForLegality(row, col, r, c)) {
					legalMoves.add(new int[] {r,c});
				}
			}
			else {
				if(p.getColor() != this.getColor()) {
					if(b.tryMoveForLegality(row, col, r, c)) {
						legalMoves.add(new int[] {r,c});
					}
				}
				break;
			}
		}
		
		for(int r = row - 1, c = col + 1; b.inBounds(r, c); r--, c++) { //searches toward TOP RIGHT
			Piece p = b.getPieceAt(r,c);
			if(p == null) {
				if(b.tryMoveForLegality(row, col, r, c)) {
					legalMoves.add(new int[] {r,c});
				}
			}
			else {
				if(p.getColor() != this.getColor()) {
					if(b.tryMoveForLegality(row, col, r, c)) {
						legalMoves.add(new int[] {r,c});
					}
				}
				break;
			}
		}
		
		for(int r = row - 1, c = col - 1; b.inBounds(r, c); r--, c--) { //searches toward TOP LEFT
			Piece p = b.getPieceAt(r,c);
			if(p == null) {
				if(b.tryMoveForLegality(row, col, r, c)) {
					legalMoves.add(new int[] {r,c});
				}
			}
			else {
				if(p.getColor() != this.getColor()) {
					if(b.tryMoveForLegality(row, col, r, c)) {
						legalMoves.add(new int[] {r,c});
					}
				}
				break;
			}
		}
		
		return legalMoves;
	}

	@Override
	public boolean canCheck(Board b, int startRow, int startCol, int destRow, int destCol) {
		int colDist = destCol - startCol;
		int rowDist = destRow - startRow;
		
		if(Math.abs(colDist) != Math.abs(rowDist)) return false;
		
		int abs = Math.abs(colDist);
		
		if(colDist > 0) {
			if(rowDist > 0) { //enemy is toward BOTTOM RIGHT
				for(int i = 1; i < abs; i++) {
					if(b.getPieceAt(startRow + i, startCol + i) != null) {
						return false;
					}
				}
				return true;
			}
			else { //enemy is toward TOP RIGHT
				for(int i = 1; i < abs; i++) {
					if(b.getPieceAt(startRow - i, startCol + i) != null) {
						return false;
					}
				}
				return true;
			}
		}
		else {
			if(rowDist > 0) { //enemy is toward BOTTOM LEFT
				for(int i = 1; i < abs; i++) {
					if(b.getPieceAt(startRow + i, startCol - i) != null) {
						return false;
					}
				}
				return true;
			}
			else { //enemy is toward TOP LEFT
				for(int i = 1; i < abs; i++) {
					if(b.getPieceAt(startRow - i, startCol - i) != null) {
						return false;
					}
				}
				return true;
			}
		}
	}

	@Override
	public Image getImage() {
		return getColor() == Piece.WHITE ? WHITE_IMAGE : BLACK_IMAGE;
	}
	
	public String toString() {
		return getColorString() + " Bishop";
	}

}
