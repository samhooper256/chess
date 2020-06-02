package chess.base;

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
	
	public int getStartRow() {return startRow;}
	public int getStartCol() {return startCol;}
	public LegalAction getPlay() {return play;}
	public Piece getPiece() {return piece;}
	public Piece getOnDest() {return onDest;}
	
	public int distance() {
		return Math.abs(startRow - play.destRow()) + Math.abs(startCol - play.destCol());
	}
}
