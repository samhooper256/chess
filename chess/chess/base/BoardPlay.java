package chess.base;

import java.util.Arrays;

public class BoardPlay{
	
	public Piece moved;
	public Piece captured;
	public int startRow, startCol;
	public int destRow, destCol;
	public String[] flags;
	
	public BoardPlay(Piece moved, Piece captured, int startRow, int startCol, int destRow, int destCol) {
		this.moved = moved;
		this.captured = captured;
		this.startRow = startRow;
		this.startCol = startCol;
		this.destRow = destRow;
		this.destCol = destCol;
		flags = new String[] {};
	}
	
	public BoardPlay(Piece moved, Piece captured, int startRow, int startCol, int destRow, int destCol,
			String... flags) {
		this.moved = moved;
		this.captured = captured;
		this.startRow = startRow;
		this.startCol = startCol;
		this.destRow = destRow;
		this.destCol = destCol;
		this.flags = flags;
	}
	
	public String toString() {
		return String.format("[GameMove = (%d, %d) to (%d, %d), %s takes %s, flags = %s]", startRow, startCol,
				destRow, destCol, moved, captured, Arrays.toString(flags));
	}
}
