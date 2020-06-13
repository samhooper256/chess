package chess.util;

import java.lang.reflect.Method;
import java.util.ArrayList;

import chess.base.Board;

public class BooleanPath extends PathBase{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5129853734669038191L;
	boolean isInverted = false;
	public BooleanPath(Object base, ArrayList<MethodAccess> calls) {
		super(base, calls);
	}
	
	public static final BooleanPath trueConstantBoolPath;
	public static final BooleanPath falseConstantBoolPath;
	public static final Method[] creationMethods;
	static {
		creationMethods = new Method[5];

		try {
			creationMethods[0] = BooleanPath.class.getMethod("toCondition");
			creationMethods[1] = BooleanPath.class.getMethod("isEquals", BooleanPath.class);
			creationMethods[2] = BooleanPath.class.getMethod("notEquals", BooleanPath.class);
			creationMethods[3] = BooleanPath.class.getMethod("isEnemy");
			creationMethods[4] = BooleanPath.class.getMethod("isAlly");
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//creationMethods[1] = BooleanPath.class.getMethod("toCondition");
		trueConstantBoolPath = new BooleanPath(true);
		falseConstantBoolPath = new BooleanPath(false);
	}
	public BooleanPath(boolean constant) {
		super(Boolean.valueOf(constant), null);
	}
	
	
	
	@AFC(name="to condition")
	public Condition toCondition() {
		return new SingleBooleanCondition(this, isInverted);
	}
	
	@AFC(name="equals")
	public Condition isEquals(BooleanPath other) {
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
