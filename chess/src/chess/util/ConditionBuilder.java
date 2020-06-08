package chess.util;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;

import chess.base.Board;
import chess.base.Board.Tile;
import chess.base.Piece;

public class ConditionBuilder{
	Object base;
	ArrayList<MethodAccess> accesses;
	Class<?> currentClass;
	
	public ConditionBuilder(Object base){
		this.base = base;
		this.accesses = new ArrayList<>();
		if(base instanceof Flag) {
			if(base == Flag.DESTINATION || base == Flag.ORIGIN) {
				currentClass = Tile.class;
			}
			else if(base == Flag.BOARD){
				currentClass = Board.class;
			}
			else if(base == Flag.SELF) {
				currentClass = Piece.class;
			}
			else {
				throw new IllegalArgumentException("bad new bears");
			}
		}
		else if(base instanceof RelativeTile) {
			currentClass = Tile.class;
		}
		else {
			this.currentClass = base.getClass();
		}
	}
	
	public ConditionBuilder call(String name, Object...args) {
		Method m;
		try {
			m = currentClass.getMethod(name);
			accesses.add(new MethodAccess(m, args));
			currentClass = m.getReturnType();
			if(currentClass == null) {
				throw new IllegalArgumentException("no void method calls.");
			}
		} catch (NoSuchMethodException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return this;
	}
	
	public ConditionBuilder call(Method m, Object...args) {
		try {
			accesses.add(new MethodAccess(m, args));
			currentClass = m.getReturnType();
			if(currentClass == null) {
				throw new IllegalArgumentException("no void method calls.");
			}
		}
		catch (SecurityException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return this;
	}

	
	public BooleanPath toBooleanPath() {
		if(currentClass == boolean.class) {
			return new BooleanPath(base, accesses);
		}
		else {
			throw new IllegalArgumentException("Calls/Properties do not lead to a boolean");
		}
	}
	
	public IntegerPath toIntegerPath() {
		if(currentClass == int.class) {
			return new IntegerPath(base, accesses);
		}
		else {
			throw new IllegalArgumentException("Calls/Properties do not lead to an int");
		}
	}
	
	public ObjectPath toObjectPath() {
		if(!currentClass.isPrimitive()) {
			return new ObjectPath(base, accesses);
		}
		else {
			throw new IllegalArgumentException("Calls/Properties do not lead to an Object");
		}
	}
	
	
}
