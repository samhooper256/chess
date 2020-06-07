package chess.piecebuilder;

import java.lang.reflect.Method;

import chess.util.BooleanPath;
import chess.util.InputVerification;
import chess.util.IntTextField;
import chess.util.IntegerPath;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;

public class ParameterBlock extends HBox implements Buildable<Object[]>, InputVerification{
	private Method method;
	public ParameterBlock(Method m) {
		this.method = m;
		for(Class<?> clazz : m.getParameterTypes()) {
			if(clazz == int.class || clazz == IntegerPath.class) {
				this.getChildren().add(new IntegerPathBuilder());
			}
			else if(clazz == boolean.class || clazz == BooleanPath.class) {
				this.getChildren().add(new BooleanPathBuilder());
			}
			else {
				throw new IllegalArgumentException("this parameter type is not supported");
			}
		}
	}
	@Override
	public Object[] build() {
		Object[] params = new Object[method.getParameterCount()];
		ObservableList<Node> children = this.getChildren();
		if(params.length != children.size()) {
			throw new IllegalArgumentException("Invalid number of nodes in this paramblock???");
		}
		for(int i = 0; i < params.length; i++) {
			params[i] = ((PathBuilder) children.get(i)).build();
		}
		return params;
	}
	
	@Override
	public boolean verifyInput() {
		boolean result = true;
		for(Node fxNode : getChildren()) {
			if(fxNode instanceof InputVerification) {
				result &= ((InputVerification) fxNode).verifyInput();
			}
		}
		return result;
	}
}
