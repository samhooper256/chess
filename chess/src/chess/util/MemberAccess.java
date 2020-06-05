package chess.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;

import chess.base.Board;

public class MemberAccess {
	private Member member;
	private Object[] arguments;
	public MemberAccess(Method mem, Object... args) {
		this.member = mem;
		this.arguments = args;
	}
	
	public MemberAccess(Field mem) {
		this.member = mem;
	}
	
	public Object retrieve(Object obj, Board b, int startRow, int startCol, int destRow, int destCol) {
		try {
			if(member instanceof Field) {
				return ((Field) member).get(obj);
			}
			else if(member instanceof Method) {
				if(arguments.length == 0) {
					return ((Method) member).invoke(obj);
				}
				else {
					Method method = (Method) member;
					Object[] actualArgs = new Object[arguments.length];
					Class<?>[] paramTypes = method.getParameterTypes();
					for(int i = 0; i < actualArgs.length; i++) {
						if(PathBase.class.isAssignableFrom(paramTypes[i])) {
							actualArgs[i] = arguments[i];
						}
						else {
							if(arguments[i] instanceof PathBase) {
								actualArgs[i] = ((PathBase) arguments[i]).get(b, startRow, startCol, destRow, destCol);
							}
							else {
								actualArgs[i] = arguments[i];
							}
						}
					}
					return method.invoke(obj, actualArgs);
				}
			}
			else {
				throw new IllegalArgumentException("bad Member (not a Field or Method)");
			}
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;	
	}
	
	public boolean hasParameters() {
		return arguments != null && arguments.length > 0;
	}
	
	public boolean isMethod() {
		return member instanceof Method;
	}
	
	public boolean isField() {
		return member instanceof Field;
	}
	
	@Override
	public String toString() {
		return "[MemberAccess:member="+member+", arguments="+Arrays.deepToString(arguments)+"]";
	}
	
}
