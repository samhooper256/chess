package chess.util;

import java.io.Serializable;
import java.util.ArrayList;

import chess.base.Board;
import chess.base.Piece;

public abstract class PathBase implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8556832703486034225L;
	protected Object base;
	/*
	 * If calls is empty OR null, base can be used
	 * */
	protected ArrayList<MethodAccess> calls;
	public PathBase(Object base, ArrayList<MethodAccess> calls) {
		this.base = base;
		this.calls = calls;
	}
	
	public Object get(Board b, int startRow, int startCol, int destRow, int destCol) {
		Object end;
		if(base instanceof Flag) {
			if(base == Flag.DESTINATION) {
				end = b.getTileAt(destRow, destCol);
			}
			else if(base == Flag.ORIGIN) {
				end = b.getTileAt(startRow, startCol);
			}
			else if(base == Flag.BOARD) {
				end = b;
			}
			else if(base == Flag.SELF) {
				end = b.getPieceAt(startRow, startCol);
			}
			else {
				throw new IllegalArgumentException("bad news bears");
			}
		}
		else if(base instanceof RelativeTile) {
			RelativeTile helper = (RelativeTile) base;
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			if(helper.relativeTo == Flag.ORIGIN) {
				end = b.getTileAt(startRow + m*helper.calcRow(b, startRow, startCol, destRow, destCol),
						startCol + m*helper.calcCol(b, startRow, startCol, destRow, destCol));
			}
			else if(helper.relativeTo == Flag.DESTINATION) {
				end = b.getTileAt(destRow + m*helper.calcRow(b, startRow, startCol, destRow, destCol),
						destCol + m*helper.calcCol(b, startRow, startCol, destRow, destCol));
			}
			else {
				throw new IllegalArgumentException("bad news bears");
			}
			//System.out.println(end);
		}
		else {
			end = base;
		}
		
		
		if(calls == null) {
			return end;
		}
		Object current = end;
		for(int i = 0; i < calls.size(); i++) {
			current = calls.get(i).retrieve(current, b, startRow, startCol, destRow, destCol);
		}
		return current;
	}
}
