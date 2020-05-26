package chess.util;

import java.lang.reflect.Member;
import java.util.ArrayList;

import chess.base.Board;

public class BoolPath extends PathBase{
	boolean isInverted = false;
	public BoolPath(Object base, ArrayList<Member> calls) {
		super(base, calls);
	}
	
	public BoolPath(boolean constant) {
		super(Boolean.valueOf(constant), null);
	}

	public Condition toCond() {
		return new SingleBooleanCondition(this, isInverted);
	}
	
	public Condition equals(BoolPath other) {
		if(isInverted) {
			return new BooleanNotEqualsCondition(this, other);
		}
		else {
			return new BooleanEqualsCondition(this, other);
		}
	}
	
	public Condition notEquals(BoolPath other) {
		if(isInverted) {
			return new BooleanEqualsCondition(this, other);
		}
		else {
			return new BooleanNotEqualsCondition(this, other);
		}
	}
	
	public Condition isEnemy() {
		if(isInverted) {
			return new BooleanIsAllyCondition(this);
		}
		else {
			return new BooleanIsEnemyCondition(this);
		}
	}
	
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
}
