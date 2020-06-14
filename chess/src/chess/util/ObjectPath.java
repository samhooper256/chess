package chess.util;

import java.lang.reflect.Method;
import java.util.ArrayList;

import chess.base.Board;

public class ObjectPath extends PathBase{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1584609769677799028L;

	public ObjectPath(Object base, ArrayList<MethodAccess> calls) {
		super(base, calls);
	}
	
	public ObjectPath(Object val) {
		super(val, null);
	}
	
public static final Method[] creationMethods;
	
	static {
		creationMethods = new Method[6];
		
		try {
			creationMethods[0] = ObjectPath.class.getMethod("isEquals", ObjectPath.class);
			creationMethods[1] = ObjectPath.class.getMethod("notEquals", ObjectPath.class);
			creationMethods[2] = ObjectPath.class.getMethod("isNotNull");
			creationMethods[3] = ObjectPath.class.getMethod("isNull");
			creationMethods[4] = ObjectPath.class.getMethod("instanceOf", Class.class);
			creationMethods[5] = ObjectPath.class.getMethod("isPiece", String.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	@AFC(name="equals")
	public Condition isEquals(ObjectPath other) {
		return new ObjectEqualsCondition(this, other);
	}
	
	@AFC(name="does not equal")
	public Condition notEquals(ObjectPath other) {
		return new ObjectNotEqualsConditions(this, other);
	}
	
	@AFC(name="exists")
	public Condition isNotNull() {
		return new ObjectIsNotNullCondition(this);
	}
	
	@AFC(name="does not exist")
	public Condition isNull() {
		return new ObjectIsNullCondition(this);
	}
	
	@AFC(name="is the action type")
	public Condition instanceOf(Class<?> caster) {
		return new ObjectInstanceOfCondition(this, caster);
	}
	
	@AFC(name="is the piece")
	public Condition isPiece(String pieceName) {
		return new ObjectIsPieceCondition(this, pieceName);
	}

	@Override
	public Object get(Board b, int startRow, int startCol, int destRow, int destCol) {
		return (Object) super.get(b, startRow, startCol, destRow, destCol);
	}
}
