package chess.base;

import java.util.Arrays;

public class BoardPlay{
	
	private Piece piece;
	private int startRow, startCol;
	private LegalAction play;
	
	public static BoardPlay of(int startRow, int startCol, Piece piece, LegalAction play) {
		return new BoardPlay(startRow, startCol, piece, play);
	}
	
	private BoardPlay(int sr, int sc, Piece pi, LegalAction p) {
		this.startRow = sr;
		this.startCol = sc;
		this.piece = pi;
		this.play = p;
	}
	
	public int getStartRow() {return startRow;}
	public int getStartCol() {return startCol;}
	public LegalAction getPlay() {return play;}
	public Piece getPiece() {return piece;}
	
	public int distance() {
		return Math.abs(startRow - play.destRow()) + Math.abs(startCol - play.destCol());
	}
}
