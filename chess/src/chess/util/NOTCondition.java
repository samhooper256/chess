package chess.util;

import java.lang.reflect.Method;

import chess.base.Board;

public class NOTCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2020623859815942516L;
	Condition c1;
	
	public NOTCondition(Condition c1) {
		this.c1 = c1;
	}
	
	public Condition getNottedCondition() {
		return c1;
	}
	
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	!c1.calc(b, startRow, startCol, destRow, destCol);
	}
	
	
	@Override
	public Method getCreationMethod() {
		throw new UnsupportedOperationException();
	}
}
