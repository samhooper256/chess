package chess.util;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;

import chess.base.Board;
import chess.base.Board.Tile;

public class IntegerPath extends PathBase{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8294183160153854073L;
	
	public static final Method[] creationMethods;
	
	static {
		creationMethods = new Method[6];
		
		try {
			creationMethods[0] = IntegerPath.class.getMethod("greaterThan", IntegerPath.class);
			creationMethods[1] = IntegerPath.class.getMethod("lessThan", IntegerPath.class);
			creationMethods[2] = IntegerPath.class.getMethod("greaterThanOrEqual", IntegerPath.class);
			creationMethods[3] = IntegerPath.class.getMethod("lessThanOrEqual", IntegerPath.class);
			creationMethods[4] = IntegerPath.class.getMethod("isEquals", IntegerPath.class);
			creationMethods[5] = IntegerPath.class.getMethod("notEquals", IntegerPath.class);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public IntegerPath(Object base, ArrayList<MethodAccess> calls) {
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
