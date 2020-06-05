package chess.base;

import chess.util.AFC;

public class BoardPlay{
	
	private Piece piece, onDest;
	private int startRow, startCol;
	private LegalAction play;
	
	public static BoardPlay of(int startRow, int startCol, Piece piece, Piece onDest, LegalAction play) {
		return new BoardPlay(startRow, startCol, piece, onDest, play);
	}
	
	private BoardPlay(int sr, int sc, Piece pi, Piece onDest, LegalAction p) {
		this.startRow = sr;
		this.startCol = sc;
		this.piece = pi;
		this.onDest = onDest;
		this.play = p;
	}
	
	@AFC(name="start row")
	public int getStartRow() {return startRow;}
	@AFC(name="start column")
	public int getStartCol() {return startCol;}
	public LegalAction getPlay() {return play;}
	@AFC(name="acting piece")
	public Piece getPiece() {return piece;}
	@AFC(name="piece on destination")
	public Piece getOnDest() {return onDest;}
	
	@AFC(name="distance")
	public int distance() {
		return Math.abs(startRow - play.destRow()) + Math.abs(startCol - play.destCol());
	}
}
