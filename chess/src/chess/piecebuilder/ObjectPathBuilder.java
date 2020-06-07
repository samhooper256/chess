package chess.piecebuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import chess.util.ConditionBuilder;
import chess.util.InputVerification;
import chess.util.ObjectPath;
import chess.util.PathBase;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class ObjectPathBuilder extends PathBuilder{

	public ObjectPathBuilder() {
		super();
		this.setStyle("-fx-border-width: 1px; -fx-border-color: rgba(255, 235, 59, 1.0);");
	}

	@Override
	public String getPathTypeName() {
		return "Anything";
	}

	@Override
	public boolean verifyInput() {
		boolean result = super.verifyInput();
		if(!result) {
			PieceBuilder.submitError("Anything Path has no selection for the \"on\" box");
			return false;
		}
		ObservableList<Node> children = this.getChildren();
		Node last = children.get(children.size() - 1);
		if(last instanceof ConditionChoiceBox) {
			ConditionOption selected = ((ConditionChoiceBox) last).getValue();
			if(selected == null) {
				Node previousNode = children.get(children.size() - 2);
				if(previousNode instanceof ConditionChoiceBox) {
					ConditionOption previousCO = ((ConditionChoiceBox) previousNode).getValue();
					if(previousCO instanceof MethodConditionOption) {
						if(((MethodConditionOption) previousCO).method.getReturnType().isPrimitive()) {
							PieceBuilder.submitError("Anything path does not lead to valid property");
							result = false;
						}
					}
					else {
						throw new UnsupportedOperationException(previousCO.getClass() + " is not a supported ConditionOption type for ObjectPathBuilders");
					}
				}
				else {
					System.out.println("\n\n**" + previousNode.getClass() + "**\n\n");
					PieceBuilder.submitError("Anything Path does not have a selection");
					result = false;
				}
			}
			else if(selected instanceof MethodConditionOption) {
				if(((MethodConditionOption) selected).method.getReturnType().isPrimitive()) {
					PieceBuilder.submitError("Anything path does not lead to valid property");
					result = false;
				}
			}
			else {
				throw new UnsupportedOperationException(selected.getClass() + " is not a supported ConditionOption type for ObjectPathBuilders");
			}
		}
		else {
			throw new UnsupportedOperationException(last.getClass() + " is not supported by ObjectBuilder");
		}
		for(Node fxNode : children) {
			if(fxNode instanceof InputVerification) {
				boolean verify = ((InputVerification) fxNode).verifyInput();
				result &= verify;
			}
		}
		return result;
	}
	@Override
	public ObjectPath build() {
		try {
			Method onMethod = null;
			ConditionOption onChoiceBoxValue = onChoiceBox.getValue();
			if(onChoiceBoxValue instanceof MethodConditionOption) {
				onMethod = ((MethodConditionOption) onChoiceBoxValue).method;
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
			while_loop:
			while(index < children.size()) {
				Node child = children.get(index);
				if(child instanceof ConditionChoiceBox) {
					if(((ConditionChoiceBox) child).getSelectionModel().isEmpty()) {
						break while_loop;
					}
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
			return conditionBuilder.toObjectPath();
			
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new IllegalArgumentException("Unknown Error ( see stack trace )");
	}

}
