package chess;

import java.util.ArrayList;

import javafx.scene.image.Image;

public abstract class Piece {
	
	protected boolean color;
	protected boolean hasMoved;
	
	public static final boolean WHITE = true;
	public static final boolean BLACK = false;
	
	public Piece(boolean color) {
		this.color = color;
		hasMoved = false;
	}
	
	public void setHasMoved(boolean hasMoved) {
		this.hasMoved = hasMoved;
	}
	public boolean hasMoved() {
		return hasMoved;
	}
	
	public boolean getColor() {
		return color;
	}
	
	public boolean isWhite() {
		return color == WHITE;
	}
	
	public boolean isBlack() {
		return color == BLACK;
	}
	
	//PRECONDITON : file and rank are valid, the piece at file and rank is the correct piece
	public ArrayList<int[]> getLegalMoves(Board b, char file, char rank){
		int row = b.getBoardSize() - rank + '0';
		int col = file - 'A';
		//System.out.printf("converted " + file + rank + " to (%d,%d)%n", row ,col);
		return getLegalMoves(b, row, col);
	}
	
	//PRECONDITON : file and rank are valid, the piece at file and rank is the correct piece
	public abstract ArrayList<int[]> getLegalMoves(Board b, int row, int col);
	
	//PRECONDITON : file and rank are valid, the piece at file and rank is the correct piece
	public boolean canCheck(Board b, char startFile, char startRank, char destFile, char destRank) {
		int startRow = b.getBoardSize() - startRank + '0';
		int startCol = startFile - 'A';
		int destRow = b.getBoardSize() - destRank + '0';
		int destCol = destFile - 'A';
		return canCheck(b, startRow, startCol, destRow, destCol);
	}
	//PRECONDITON : file and rank are valid, the piece at file and rank is the correct piece
	public abstract boolean canCheck(Board b, int startRow, int startCol, int destRow, int destCol);
	
	public abstract Image getImage();
	
	protected String getColorString() {
		if(this.color == WHITE) {
			return "White";
		}
		else {
			return "Black";
		}
	}
	
	public String toString() {
		return getColorString() + " Piece";
	}
}

