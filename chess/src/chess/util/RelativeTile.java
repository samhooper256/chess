package chess.util;

import chess.base.Board;

public class RelativeTile{
	public Flag relativeTo;
	public IntegerPath row, col;
	
	public RelativeTile(IntegerPath r, IntegerPath c, Flag rel) {
		this.row = r;
		this.col = c;
		this.relativeTo = rel;
	}
	
	public int calcRow(Board b, int startRow, int startCol, int destRow, int destCol) {
		return row.get(b, startRow, startCol, destRow, destCol);
	}
	
	public int calcCol(Board b, int startRow, int startCol, int destRow, int destCol) {
		return col.get(b, startRow, startCol, destRow, destCol);
	}
	
	@Override
	public String toString() {
		return "[RelativeTile:relativeTo="+relativeTo+", row="+row+", col="+col+"]";
	}
}
