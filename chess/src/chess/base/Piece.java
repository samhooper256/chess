package chess.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import chess.util.AFC;
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
	
	@AFC(name="has moved")
	public boolean hasMoved() {
		return hasMoved;
	}
	
	@AFC(name="color")
	public boolean getColor() {
		return color;
	}
	
	@AFC(name="is white")
	public boolean isWhite() {
		return color == WHITE;
	}
	
	@AFC(name="is black")
	public boolean isBlack() {
		return color == BLACK;
	}
	
	//PRECONDITON : file and rank are valid, the piece at file and rank is the correct piece
	public Set<LegalAction> getLegalMoves(Board b, char file, char rank){
		int row = b.getBoardSize() - rank + '0';
		int col = file - 'A';
		//System.out.printf("converted " + file + rank + " to (%d,%d)%n", row ,col);
		return getLegalActions(b, row, col);
	}
	
	//PRECONDITON : file and rank are valid, the piece at file and rank is the correct piece
	public abstract Set<LegalAction> getLegalActions(Board b, int row, int col);
	
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
	
	public static boolean isNameOfPiece(String pieceName) {
		return 	pieceName.equals("King") ||
				pieceName.equals("Queen") ||
				pieceName.equals("Rook") ||
				pieceName.equals("Bishop") ||
				pieceName.equals("Knight") ||
				pieceName.equals("Pawn") ||
				CustomPiece.isDefinedPiece(pieceName);
	}
	
	public abstract int getPointValue();
	
	@AFC(name="piece type")
	public abstract PieceType getPieceType();
	
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
	
	public static final Collection<Image> getImagesOfAllPieces() {
		ArrayList<Image> end = new ArrayList<>(12 + CustomPiece.getDefinedPieceCount() * 2);
		end.addAll(Arrays.asList(
				Pawn.WHITE_IMAGE, Pawn.BLACK_IMAGE,
				Knight.WHITE_IMAGE, Knight.BLACK_IMAGE,
				Bishop.WHITE_IMAGE, Bishop.BLACK_IMAGE,
				Rook.WHITE_IMAGE, Rook.BLACK_IMAGE,
				Queen.WHITE_IMAGE, Queen.BLACK_IMAGE,
				King.WHITE_IMAGE, King.BLACK_IMAGE
		));
		end.addAll(CustomPiece.getAllCustomPieceImages());
		return end;
	}
	
	public static final Collection<String> predefinedPieceNames = new ArrayList<>(Arrays.asList("Pawn","Knight","Bishop","Rook","Queen","King"));
	
	public static final Collection<String> getNamesOfAllPieces(){
		ArrayList<String> end = new ArrayList<>(6 + CustomPiece.getDefinedPieceCount());
		end.addAll(predefinedPieceNames);
		end.addAll(CustomPiece.getDefinedPieceNames());
		return end;
	}
	
	public static final int getPieceCount() {
		return 6 + CustomPiece.getDefinedPieceCount();
	}

	public abstract PieceData getPieceData();
	
	public static PieceData getDataFor(String name) {
		switch(name) {
		case "Pawn" : return Pawn.getData();
		case "Knight" : return Knight.getData();
		case "Bishop" : return Bishop.getData();
		case "Rook" : return Rook.getData();
		case "Queen" : return Queen.getData();
		case "King" : return King.getData();
		default : return CustomPiece.getDataFor(name);
		}
	}
}

