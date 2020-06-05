package chess.util;

import java.lang.reflect.Member;
import java.util.ArrayList;

import chess.base.Board;

public class ObjectPath extends PathBase{

	public ObjectPath(Object base, ArrayList<MemberAccess> calls) {
		super(base, calls);
	}
	
	public ObjectPath(Object val) {
		super(val, null);
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
	
	@AFC(name="does not exit")
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
