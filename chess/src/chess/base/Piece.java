package chess.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import javafx.scene.image.Image;

/* *
 * @author Sam Hooper
 */
public abstract class Piece {
	
	protected boolean color;
	protected boolean hasMoved;
	
	public static final boolean WHITE = true;
	public static final boolean BLACK = false;
	
	public static Piece forName(String name) {
		if(name == null || name.isEmpty()) {
			return null;
		}
		
		if(name.length() == 1) {
			throw new IllegalArgumentException("Piece doesn't exist: \"" + name + "\"");
		}
		
		boolean color;
		if(name.charAt(0) == '+') {
			color = Piece.WHITE;
		}
		else if(name.charAt(0) == '-'){
			color = Piece.BLACK;
		}
		else {
			throw new IllegalArgumentException("Piece doesn't exist.");
		}
		return Piece.forName(name.substring(1), color);
	}
	
	public static Piece forName(String name, boolean color) {
		if(name == null || name.isEmpty()) {
			return null;
		}
		
		Piece end;
		switch(name) {
		case "Pawn" : end = new Pawn(color); break;
		case "Knight" : end = new Knight(color); break;
		case "Bishop" : end = new Bishop(color); break;
		case "Rook" : end = new Rook(color); break;
		case "Queen" : end = new Queen(color); break;
		case "King" : end = new King(color); break;
		default : end = CustomPiece.forName(name, color); break;
		}
		return end;
	}
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
	public Set<LegalAction> getLegalMoves(Board b, char file, char rank){
		int row = b.getBoardSizeAsInt() - rank + '0';
		int col = file - 'A';
		//System.out.printf("converted " + file + rank + " to (%d,%d)%n", row ,col);
		return getLegalActions(b, row, col);
	}
	
	//PRECONDITON : file and rank are valid, the piece at file and rank is the correct piece
	public abstract Set<LegalAction> getLegalActions(Board b, int row, int col);
	
	//PRECONDITON : file and rank are valid, the piece at file and rank is the correct piece
	public boolean canCheck(Board b, char startFile, char startRank, char destFile, char destRank) {
		int startRow = b.getBoardSizeAsInt() - startRank + '0';
		int startCol = startFile - 'A';
		int destRow = b.getBoardSizeAsInt() - destRank + '0';
		int destCol = destFile - 'A';
		return canCheck(b, startRow, startCol, destRow, destCol);
	}
	//PRECONDITON : file and rank are valid, the piece at file and rank is the correct piece
	public abstract boolean canCheck(Board b, int startRow, int startCol, int destRow, int destCol);
	
	public abstract Image getImage();
	
	public String getPieceName(){
		return this.getClass().getSimpleName();
	}
	
	public String getFullName() {
		return (color == Piece.WHITE ? '+' : '-') + getPieceName();
	}
	
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
	
	public abstract int getPointValue();
	
	public static final Collection<Piece> getInstancesOfAllPieces(){
		ArrayList<Piece> end = new ArrayList<>(12 + CustomPiece.getDefinedPieceCount() * 2);
		end.addAll(Arrays.asList(
				new Pawn(Piece.WHITE), new Pawn(Piece.BLACK),
				new Knight(Piece.WHITE), new Knight(Piece.BLACK),
				new Bishop(Piece.WHITE), new Bishop(Piece.BLACK),
				new Rook(Piece.WHITE), new Rook(Piece.BLACK),
				new Queen(Piece.WHITE), new Queen(Piece.BLACK),
				new King(Piece.WHITE), new King(Piece.BLACK)
		));
		end.addAll(CustomPiece.getInstancesOfDefinedPieces());
		return end;
	}
	
	public static final Collection<String> getNamesOfAllPieces(){
		ArrayList<String> end = new ArrayList<>(6 + CustomPiece.getDefinedPieceCount());
		end.addAll(Arrays.asList("Pawn","Knight","Bishop","Rook","Queen","King"));
		end.addAll(CustomPiece.getDefinedPieceNames());
		return end;
	}
}

