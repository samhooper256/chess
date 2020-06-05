package chess.util;

import java.lang.reflect.Member;
import java.util.ArrayList;

import chess.base.Board;

public class BoolPath extends PathBase{
	boolean isInverted = false;
	public BoolPath(Object base, ArrayList<MemberAccess> calls) {
		super(base, calls);
	}
	
	public static final BoolPath trueConstantBoolPath;
	public static final BoolPath falseConstantBoolPath;
	static {
		trueConstantBoolPath = new BoolPath(true);
		falseConstantBoolPath = new BoolPath(false);
	}
	public BoolPath(boolean constant) {
		super(Boolean.valueOf(constant), null);
	}
	
	@AFC(name="to condition")
	public Condition toCond() {
		return new SingleBooleanCondition(this, isInverted);
	}
	
	@AFC(name="equals")
	public Condition equals(BoolPath other) {
		if(isInverted) {
			return new BooleanNotEqualsCondition(this, other);
		}
		else {
			return new BooleanEqualsCondition(this, other);
		}
	}
	
	@AFC(name="does not equal")
	public Condition notEquals(BoolPath other) {
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
	
	public BoolPath invert() {
		isInverted = !isInverted;
		return this;
	}
	@Override
	public String toString() {
		return "[BoolPath: calls=" + calls + ", base=" +base + "]";
	}
}
