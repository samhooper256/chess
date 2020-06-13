package chess.piecebuilder;

import java.lang.reflect.Method;

import chess.util.BooleanPath;
import chess.util.InputVerification;
import chess.util.IntegerPath;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class ParameterBlock extends VBox implements Buildable<Object[]>, InputVerification{
	private Method method;
	public ParameterBlock(Method m) {
		this(m, true);
	}
	private ParameterBlock(Method m, boolean addEmptyBuilders) {
		super(2);
		this.setAlignment(Pos.CENTER_LEFT);
		this.method = m;
		if(addEmptyBuilders) {
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
	
	public static ParameterBlock reconstruct(Method m, Object... args) {
		ParameterBlock pBlock = new ParameterBlock(m, false);
		for(Object arg : args) {
			if(arg instanceof IntegerPath) {
				pBlock.getChildren().add(IntegerPathBuilder.reconstruct((IntegerPath) arg));
			}
			else if(arg instanceof BooleanPath) {
				pBlock.getChildren().add(BooleanPathBuilder.reconstruct((BooleanPath) arg));
			}
			else if(arg.getClass() == Integer.class || arg.getClass() == int.class) {
				pBlock.getChildren().add(IntegerPathBuilder.reconstruct((int) arg));
			}
			else if(arg.getClass() == Boolean.class || arg.getClass() == boolean.class) {
				pBlock.getChildren().add(BooleanPathBuilder.reconstruct((boolean) arg));
			}
		}
		return pBlock;
	}
}
