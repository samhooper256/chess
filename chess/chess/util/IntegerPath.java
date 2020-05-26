package chess.util;

import java.lang.reflect.Member;
import java.util.ArrayList;

import chess.base.Board;
import chess.base.Board.Tile;

public class IntegerPath extends PathBase{
	
	public static final IntegerPath fromStartEnemyDist = new IntegerPath(null, null) {
		@Override
		public Integer get(Board b, int startRow, int startCol, int destRow, int destCol) {
			Tile t = b.getTileAt(startRow, startCol);
			return (Integer) t.enemyLineDistance(t.getPiece().getColor());
		}
	};
	
	public static final IntegerPath fromDestEnemyDist = new IntegerPath(null, null) {
		@Override
		public Integer get(Board b, int startRow, int startCol, int destRow, int destCol) {
			Tile t = b.getTileAt(destRow, destCol);
			return (Integer) t.enemyLineDistance(b.getPieceAt(startRow, startCol).getColor());
		}
	};

	public IntegerPath(Object base, ArrayList<Member> calls) {
		super(base, calls);
	}
	
	public IntegerPath(int constant) {
		super(Integer.valueOf(constant), null);
	}
	
	public Condition greaterThan(IntegerPath other) {
		return new IntegerGreaterThanCondition(this, other);
	}
	public Condition lessThan(IntegerPath other) {
		return new IntegerLessThanCondition(this, other);
	}
	public Condition greaterThanOrEqual(IntegerPath other) {
		return new IntegerGreaterThanOrEqualCondition(this, other);
	}
	public Condition lessThanOrEqual(IntegerPath other) {
		return new IntegerLessThanOrEqualCondition(this, other);
	}
	public Condition isEquals(IntegerPath other) {
		return new IntegerEqualsCondition(this, other);
	}
	public Condition notEquals(IntegerPath other) {
		return new IntegerNotEqualsCondition(this, other);
	}
	
	@Override
	public Integer get(Board b, int startRow, int startCol, int destRow, int destCol) {
		return (Integer) super.get(b, startRow, startCol, destRow, destCol);
	}
}
