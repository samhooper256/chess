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
	ArrayList<Member> calls;
	Class<?> currentClass;
	
	public ConditionBuilder(Object base){
		this.base = base;
		this.calls = new ArrayList<>();
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
	
	public ConditionBuilder property(String name) {
		Field f;
		try {
			f = currentClass.getField(name);
			calls.add(f);
			currentClass = f.getType();
		} catch (NoSuchFieldException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return this;
	}
	
	public ConditionBuilder call(String name) {
		Method m;
		try {
			m = currentClass.getMethod(name);
			calls.add(m);
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
	
	public BoolPath toBool() {
		if(currentClass == Boolean.class || currentClass == boolean.class) {
			return new BoolPath(base, calls);
		}
		else {
			throw new IllegalArgumentException("Calls/Properties do not lead to a boolean/Boolean");
		}
	}
	
	public IntegerPath toInt() {
		if(currentClass == Integer.class || currentClass == int.class) {
			return new IntegerPath(base, calls);
		}
		else {
			throw new IllegalArgumentException("Calls/Properties do not lead to an int/Integer");
		}
	}
	
	public ObjectPath toObj() {
		if(!currentClass.isPrimitive()) {
			return new ObjectPath(base, calls);
		}
		else {
			throw new IllegalArgumentException("Calls/Properties do not lead to an Object");
		}
	}
	
	
}
