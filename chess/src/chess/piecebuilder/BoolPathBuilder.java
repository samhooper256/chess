package chess.piecebuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import chess.util.AFC;
import chess.util.BoolPath;
import chess.util.Condition;
import chess.util.ConditionBuilder;
import chess.util.PathBase;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

public class BoolPathBuilder extends PathBuilder{
	public BoolPathBuilder(Pane ntad) {
		super(ntad);
		this.setStyle("-fx-border-width: 1px; -fx-border-color: rgba(228, 56, 255, 1.0);");
		onChoiceBox.getItems().addAll(new BooleanLiteralConditionOption(onChoiceBox, true),
				new BooleanLiteralConditionOption(onChoiceBox, false));
	}

	@Override
	public String getPathTypeName() {
		return "Boolean";
	}

	@Override
	public BoolPath build() {
		try {
			
		Method onMethod = null;
		ConditionOption onChoiceBoxValue = onChoiceBox.getValue();
		if(onChoiceBoxValue instanceof MethodConditionOption) {
			onMethod = ((MethodConditionOption) onChoiceBoxValue).method;
		}
		else if(onChoiceBoxValue instanceof BooleanLiteralConditionOption){
			return ((BooleanLiteralConditionOption) onChoiceBoxValue).getBooleanPath();
		}
		else {
			throw new IllegalArgumentException(onChoiceBoxValue.getClass() + " is not a support ConditionOption type");
		}
		
		//TODO support onstartrelative and ondestrelative
		
		ObservableList<Node> children = this.getChildren();
		int index = children.indexOf(onChoiceBox);
		Object[] onParams;
		if(children.get(index + 1) instanceof ParameterBlock) {
			System.out.println("next to on WAS paramblock");
			onParams = ((ParameterBlock) children.get(index + 1)).build();
			index++;
		}
		else {
			System.out.println("next to on was NOT paramblock");
			onParams = new Object[0];
		}
		System.out.println("onParams="+Arrays.deepToString(onParams));
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
		return conditionBuilder.toBool();
		
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new IllegalArgumentException("Unknown Error ( see stack trace )");
	}
}
