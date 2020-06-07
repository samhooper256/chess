package chess.piecebuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import chess.util.ConditionBuilder;
import chess.util.InputVerification;
import chess.util.IntTextField;
import chess.util.IntegerPath;
import chess.util.PathBase;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class IntegerPathBuilder extends PathBuilder{
	public IntegerPathBuilder() {
		super();
		this.setStyle("-fx-border-width: 1px; -fx-border-color: rgba(255, 149, 0, 1.0);");
		onChoiceBox.getItems().addAll(new IntegerLiteralConditionOption(onChoiceBox));
	}

	@Override
	public String getPathTypeName() {
		return "Integer";
	}
	
	@Override
	public boolean verifyInput() {
		boolean result = super.verifyInput();
		if(!result) {
			PieceBuilder.submitError("Integer Path has no selection for the \"on\" box");
			return false;
		}
		ObservableList<Node> children = this.getChildren();
		Node last = children.get(children.size() - 1);
		if(last instanceof ConditionChoiceBox) {
			ConditionOption selected = ((ConditionChoiceBox) last).getValue();
			if(selected == null) {
				PieceBuilder.submitError("Integer Path does have a selection");
				result = false;
			}
			else if(selected instanceof MethodConditionOption) {
				if(!(((MethodConditionOption) selected).method.getReturnType() == int.class)) {
					PieceBuilder.submitError("Integer Path does not lead to a integer property");
					result = false;
				}
			}
			else if(selected instanceof IntegerLiteralConditionOption) {
				boolean inputValid = ((IntegerLiteralConditionOption) selected).verifyInput();
				if(!inputValid) {
					PieceBuilder.submitError("Integer Path number input box is invalid.");
				}
			}
			else {
				throw new UnsupportedOperationException(selected.getClass() + " not supported");
			}
		}
		else if(last instanceof IntTextField) {
			boolean verify = ((IntTextField) last).verifyInput();
			if(!verify) {
				PieceBuilder.submitError("Integer Path number input box is invalid.");
				result = false;
			}
		}
		//System.out.println("intbuilder children = " + children);
		for(Node fxNode : children) {
			if(fxNode instanceof InputVerification) {
				boolean verify = ((InputVerification) fxNode).verifyInput();
				result &= verify;
			}
		}
		return result;
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
			return conditionBuilder.toIntegerPath();
			
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new IllegalArgumentException("Unknown Error ( see stack trace )");
	}
}

