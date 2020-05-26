package chess.util;

import java.lang.reflect.Member;
import java.util.ArrayList;

import chess.base.Board;

public class ObjectPath extends PathBase{

	public ObjectPath(Object base, ArrayList<Member> calls) {
		super(base, calls);
	}
	
	public ObjectPath(Object val) {
		super(val, null);
	}
	
	public Condition referenceEquals(ObjectPath other) {
		return new ObjectReferenceEqualsCondition(this, other);
	}
	
	public Condition notReferenceEquals(ObjectPath other) {
		return new ObjectNotReferenceEqualsCondition(this, other);
	}
	
	public Condition isNull() {
		return new ObjectIsNullCondition(this);
	}
	
	public Condition isNotNull() {
		return new ObjectIsNotNullCondition(this);
	}
	
	public Condition instanceOf(Class<?> caster) {
		return new ObjectInstanceOfCondition(this, caster);
	}

	@Override
	public Object get(Board b, int startRow, int startCol, int destRow, int destCol) {
		return (Object) super.get(b, startRow, startCol, destRow, destCol);
	}
}
