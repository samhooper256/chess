package chess.util;

import java.lang.reflect.Member;
import java.util.ArrayList;

import chess.base.Board;

public class BooleanPath extends PathBase{
	boolean isInverted = false;
	public BooleanPath(Object base, ArrayList<MemberAccess> calls) {
		super(base, calls);
	}
	
	public static final BooleanPath trueConstantBoolPath;
	public static final BooleanPath falseConstantBoolPath;
	static {
		trueConstantBoolPath = new BooleanPath(true);
		falseConstantBoolPath = new BooleanPath(false);
	}
	public BooleanPath(boolean constant) {
		super(Boolean.valueOf(constant), null);
	}
	
	@AFC(name="to condition")
	public Condition toCond() {
		return new SingleBooleanCondition(this, isInverted);
	}
	
	@AFC(name="equals")
	public Condition equals(BooleanPath other) {
		if(isInverted) {
			return new BooleanNotEqualsCondition(this, other);
		}
		else {
			return new BooleanEqualsCondition(this, other);
		}
	}
	
	@AFC(name="does not equal")
	public Condition notEquals(BooleanPath other) {
		if(isInverted) {
			return new BooleanEqualsCondition(this, other);
		}
		else {
			return new BooleanNotEqualsCondition(this, other);
		}
	}
	
	@AFC(name="is enemy")
	public Condition isEnemy() {
		if(isInverted) {
			return new BooleanIsAllyCondition(this);
		}
		else {
			return new BooleanIsEnemyCondition(this);
		}
	}
	
	@AFC(name="is teammate")
	public Condition isAlly() {
		if(isInverted) {
			return new BooleanIsEnemyCondition(this);
		}
		else {
			return new BooleanIsAllyCondition(this);
		}
	}
	
	@Override
	public Boolean get(Board b, int startRow, int startCol, int destRow, int destCol) {
		return (Boolean) super.get(b, startRow, startCol, destRow, destCol);
	}
	
	public BooleanPath invert() {
		isInverted = !isInverted;
		return this;
	}
	@Override
	public String toString() {
		return "[BoolPath: calls=" + calls + ", base=" +base + "]";
	}
}
