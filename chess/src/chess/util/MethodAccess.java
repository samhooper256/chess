package chess.util;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;

import chess.base.Board;

public class MethodAccess implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1356992796269951865L;
	public Method getMethod() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		return Class.forName(className).getMethod(methodName, parameterTypes);
	}

	public Object[] getArguments() {
		return arguments;
	}

	private String className;
	private String methodName;
	private Class<?>[] parameterTypes;
	private final Object[] arguments;
	public MethodAccess(Method method, Object... args) {
		this.className = method.getDeclaringClass().getName();
		this.methodName = method.getName();
		this.parameterTypes = method.getParameterTypes();
		this.arguments = args;
	}
	
	public Object retrieve(Object obj, Board b, int startRow, int startCol, int destRow, int destCol) {
		try {
			Method method = getMethod();
			if(arguments.length == 0) {
				return method.invoke(obj);
			}
			else {
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
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException |
				NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			//DON'T Print stack trace - Exception is okay to be thrown, it will get caught by Condition.calc() and the 
			//Condition's defaultValue will be returned
		}
		return null;	
	}
	
	@Override
	public String toString() {
		return "[MethodAccess:className="+className+", methodName="+methodName+", parameterTypes="+Arrays.deepToString(parameterTypes)+
				", arguments="+Arrays.deepToString(arguments)+"]";
	}
	
}
