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

	public IntegerPath(Object base, ArrayList<MemberAccess> calls) {
		super(base, calls);
	}
	
	public IntegerPath(int constant) {
		super(Integer.valueOf(constant), null);
	}
	
	@AFC(name="is greater than")
	public Condition greaterThan(IntegerPath other) {
		return new IntegerGreaterThanCondition(this, other);
	}
	@AFC(name="is less than")
	public Condition lessThan(IntegerPath other) {
		return new IntegerLessThanCondition(this, other);
	}
	@AFC(name="is greater than or equal to")
	public Condition greaterThanOrEqual(IntegerPath other) {
		return new IntegerGreaterThanOrEqualCondition(this, other);
	}
	@AFC(name="is less than or equal to")
	public Condition lessThanOrEqual(IntegerPath other) {
		return new IntegerLessThanOrEqualCondition(this, other);
	}
	@AFC(name="equals")
	public Condition isEquals(IntegerPath other) {
		return new IntegerEqualsCondition(this, other);
	}
	@AFC(name="does not equal")
	public Condition notEquals(IntegerPath other) {
		return new IntegerNotEqualsCondition(this, other);
	}
	
	@Override
	public Integer get(Board b, int startRow, int startCol, int destRow, int destCol) {
		return (Integer) super.get(b, startRow, startCol, destRow, destCol);
	}
	
	@Override
	public String toString() {
		return "[IntegerPath:calls="+calls+", base="+base+"]";
	}
}
