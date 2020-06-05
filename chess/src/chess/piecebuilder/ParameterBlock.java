package chess.piecebuilder;

import java.lang.reflect.Method;

import chess.util.BoolPath;
import chess.util.IntTextField;
import chess.util.IntegerPath;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;

public class ParameterBlock extends HBox implements Buildable<Object[]>{
	private Method method;
	public ParameterBlock(Method m) {
		this.method = m;
		for(Class<?> clazz : m.getParameterTypes()) {
			if(clazz == int.class || clazz == IntegerPath.class) {
				this.getChildren().add(new IntegerPathBuilder(this));
			}
			else if(clazz == boolean.class || clazz == BoolPath.class) {
				this.getChildren().add(new BoolPathBuilder(this));
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
}
