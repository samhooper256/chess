package chess;

import java.util.ArrayList;

import javafx.scene.image.Image;

public class Pawn extends Piece{

	private static final Image BLACK_IMAGE;
	private static final Image WHITE_IMAGE;
	
	static {
		BLACK_IMAGE = new Image(Piece.class.getResourceAsStream("/resources/pawn_black.png"));
		WHITE_IMAGE = new Image(Piece.class.getResourceAsStream("/resources/pawn_white.png"));
	}
	
	public Pawn(boolean color) {
		super(color);
	}
	
	@Override
	public ArrayList<int[]> getLegalMoves(Board b, int row, int col) {
		ArrayList<int[]> legalMoves = new ArrayList<>();
		int nr, nc;
		boolean ep1 = true, ep2 = true;
		if(this.getColor() == Piece.WHITE) {
			nr = row - 1; nc = col;
			if(b.inBounds(nr, nc)) {
				Piece p = b.getPieceAt(nr, nc);
				if(p == null && b.tryMoveForLegality(row, col, nr, nc)) {
					legalMoves.add(new int[] {nr, nc});
				}
			}
			nr = row - 1; nc = col - 1;
			if(b.inBounds(nr, nc)) {
				Piece p = b.getPieceAt(nr, nc);
				if(p != null && p.getColor() != this.getColor() && b.tryMoveForLegality(row, col, nr, nc)) {
					legalMoves.add(new int[] {nr, nc});
					ep1 = false;
				}
			}
			nr = row - 1; nc = col + 1;
			if(b.inBounds(nr, nc)) {
				Piece p = b.getPieceAt(nr, nc);
				if(p != null && p.getColor() != this.getColor() && b.tryMoveForLegality(row, col, nr, nc)) {
					legalMoves.add(new int[] {nr, nc});
					ep2 = false;
				}
			}
			if(!this.hasMoved()) {
				nr = row - 2; nc = col;
				if(b.inBounds(nr, nc)) {
					Piece p = b.getPieceAt(nr, nc);
					if(p == null && b.tryMoveForLegality(row, col, nr, nc)) {
						legalMoves.add(new int[] {nr, nc});
					}
				}
			}
			if(b.enPassantsAllowed()) {
				Piece p;
				if(	ep1 && b.inBounds(row, col - 1) && (p = b.getPieceAt(row, col - 1)) instanceof Pawn && 
					p.getColor() != this.getColor() &&
					b.getPieceAt(row - 1, col - 1) == null && b.checkEnPassantLegality(row, col, row - 1, col - 1)){
					legalMoves.add(new int[] {row - 1, col - 1});
				}
				if(	ep2 && b.inBounds(row, col + 1) && (p = b.getPieceAt(row, col + 1)) instanceof Pawn && 
					p.getColor() != this.getColor() &&
					b.getPieceAt(row - 1, col + 1) == null && b.checkEnPassantLegality(row, col, row - 1, col + 1)){
					legalMoves.add(new int[] {row - 1, col + 1});
				}
			}
		}
		else {
			nr = row + 1; nc = col;
			if(b.inBounds(nr, nc)) {
				Piece p = b.getPieceAt(nr, nc);
				if(p == null && b.tryMoveForLegality(row, col, nr, nc)) {
					legalMoves.add(new int[] {nr, nc});
				}
			}
			nr = row + 1; nc = col - 1;
			if(b.inBounds(nr, nc)) {
				Piece p = b.getPieceAt(nr, nc);
				if(p != null && p.getColor() != this.getColor() && b.tryMoveForLegality(row, col, nr, nc)) {
					legalMoves.add(new int[] {nr, nc});
					ep1 = false;
				}
			}
			nr = row + 1; nc = col + 1;
			if(b.inBounds(nr, nc)) {
				Piece p = b.getPieceAt(nr, nc);
				if(p != null && p.getColor() != this.getColor() && b.tryMoveForLegality(row, col, nr, nc)) {
					legalMoves.add(new int[] {nr, nc});
					ep2 = false;
				}
			}
			if(!this.hasMoved()) { 
				nr = row + 2; nc = col;
				if(b.inBounds(nr, nc)) {
					Piece p = b.getPieceAt(nr, nc);
					if(p == null && b.tryMoveForLegality(row, col, nr, nc)) {
						legalMoves.add(new int[] {nr, nc});
					}
				}
			}
			if(b.enPassantsAllowed()) {
				Piece p;
				if(	ep1 && b.inBounds(row, col - 1) && (p = b.getPieceAt(row, col - 1)) instanceof Pawn && 
					p.getColor() != this.getColor() &&
					b.getPieceAt(row + 1, col - 1) == null && b.checkEnPassantLegality(row, col, row + 1, col - 1)){
					legalMoves.add(new int[] {row + 1, col - 1});
				}
				if(	ep2 && b.inBounds(row, col + 1) && (p = b.getPieceAt(row, col + 1)) instanceof Pawn && 
					p.getColor() != this.getColor() &&
					b.getPieceAt(row + 1, col + 1) == null && b.checkEnPassantLegality(row, col, row + 1, col + 1)){
					legalMoves.add(new int[] {row + 1, col + 1});
				}
			}
		}
		return legalMoves;
	}

	@Override
	public boolean canCheck(Board b, int startRow, int startCol, int destRow, int destCol) {
		if(this.getColor() == b.getBoardOrientation()) { //this pawn is moving "UP"
			return destRow == startRow - 1 && (destCol == startCol - 1 || destCol == startCol + 1);
		}
		else { //this pawn is moving "down"
			return destRow == startRow + 1 && (destCol == startCol - 1 || destCol == startCol + 1);
		}
	}

	@Override
	public Image getImage() {
		return getColor() == Piece.WHITE ? WHITE_IMAGE : BLACK_IMAGE;
	}
	
	public String toString() {
		return getColorString() + " Pawn";
	}

}
