package chess.piecebuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import chess.util.ConditionBuilder;
import chess.util.IntegerPath;
import chess.util.PathBase;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class IntegerPathBuilder extends PathBuilder{
	public IntegerPathBuilder(Pane ntad) {
		super(ntad);
		this.setStyle("-fx-border-width: 1px; -fx-border-color: rgba(255, 149, 0, 1.0);");
		onChoiceBox.getItems().addAll(new IntegerLiteralConditionOption(onChoiceBox));
	}

	@Override
	public String getPathTypeName() {
		return "Integer";
	}

	@Override
	public IntegerPath build() {
		try {
			Method onMethod = null;
			ConditionOption onChoiceBoxValue = onChoiceBox.getValue();
			if(onChoiceBoxValue instanceof MethodConditionOption) {
				onMethod = ((MethodConditionOption) onChoiceBoxValue).method;
			}
			else if(onChoiceBoxValue instanceof IntegerLiteralConditionOption){
				return ((IntegerLiteralConditionOption) onChoiceBoxValue).getIntegerPath();
			}
			else {
				throw new IllegalArgumentException(onChoiceBoxValue.getClass() + " is not a support ConditionOption type");
			}
			
			ObservableList<Node> children = this.getChildren();
			int index = children.indexOf(onChoiceBox);
			Object[] onParams;
			if(children.get(index + 1) instanceof ParameterBlock) {
				onParams = ((ParameterBlock) children.get(index + 1)).build();
				index++;
			}
			else {
				onParams = new Object[0];
			}
			ConditionBuilder conditionBuilder = (ConditionBuilder) onMethod.invoke(null, onParams);
			index++;
			while(index < children.size()) {
				Node child = children.get(index);
				if(child instanceof ConditionChoiceBox) {
					ConditionOption conditionOption = ((ConditionChoiceBox) child).getValue();
					if(conditionOption instanceof MethodConditionOption) {
						Object[] args;
						if(index + 1 < children.size() && children.get(index + 1) instanceof ParameterBlock) {
							args = ((ParameterBlock) children.get(index + 1)).build();
							index++;
						}
						else {
							args = new Object[0];
						}
						conditionBuilder.call(((MethodConditionOption) conditionOption).method, args);
					}
					else {
						throw new IllegalArgumentException(conditionOption.getClass() + "not supported");
					}
				}
				else {
					throw new IllegalArgumentException("Not a ConditionChoiceBox");
				}
				index++;
			}
			return conditionBuilder.toInt();
			
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new IllegalArgumentException("Unknown Error ( see stack trace )");
	}
}

