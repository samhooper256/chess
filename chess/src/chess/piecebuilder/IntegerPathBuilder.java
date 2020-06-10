package chess.piecebuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import chess.util.Condition;
import chess.util.ConditionBuilder;
import chess.util.InputVerification;
import chess.util.IntTextField;
import chess.util.IntegerPath;
import chess.util.MethodAccess;
import chess.util.PathBase;
import chess.util.RelativeTile;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class IntegerPathBuilder extends PathBuilder{
	public IntegerPathBuilder() {
		super();
		this.getStyleClass().add("integer-path-builder");
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
	
	public static IntegerPathBuilder reconstruct(int base) {
		IntegerPathBuilder builder = new IntegerPathBuilder();
		ConditionOption conditionOpt = null;
		for(ConditionOption co : builder.onChoiceBox.getItems()) {
			if(co instanceof IntegerLiteralConditionOption) {
				builder.onChoiceBox.getSelectionModel().select(conditionOpt = co);
				break;
			}
		}
		conditionOpt.updatePaneImpl();
		((IntegerLiteralConditionOption) conditionOpt).followingIntTextField.setText(String.valueOf(base));
		return builder;
	}
	
	public static IntegerPathBuilder reconstruct(IntegerPath path) {
		System.out.println("*****RECREATING:"+path);
		IntegerPathBuilder builder = new IntegerPathBuilder();
		ObservableList<Node> children = builder.getChildren();
		Object base = path.getBase();
		ConditionOption conditionOpt = null;
		if(base.getClass() == Integer.class || base.getClass() == int.class) {
			for(ConditionOption co : builder.onChoiceBox.getItems()) {
				if(co instanceof IntegerLiteralConditionOption) {
					builder.onChoiceBox.getSelectionModel().select(conditionOpt = co);
					break;
				}
			}
			conditionOpt.updatePaneImpl();
			((IntegerLiteralConditionOption) conditionOpt).followingIntTextField.setText(String.valueOf(base));
			return builder;
		}
		else {
			Method m = Condition.getOnMethodFromBase(base);
			if(m == null) {
				throw new IllegalArgumentException("Invalid IntegerPath base: " + base);
			}
			for(ConditionOption co : builder.onChoiceBox.getItems()) {
				if(co instanceof MethodConditionOption && ((MethodConditionOption) co).method.equals(m)) {
					builder.onChoiceBox.getSelectionModel().select(conditionOpt = co);
					break;
				}
			}
			if(base instanceof RelativeTile) {
				children.add(ParameterBlock.reconstruct(m, ((RelativeTile) base).row, ((RelativeTile) base).col));
			}
		}
		
		if(conditionOpt == null) {
			throw new NullPointerException("ConditionOption could not be located");
		}
		List<MethodAccess> accesses = path.getCallsOrEmpty();
		Iterator<MethodAccess> itr = accesses.iterator();
		OUTER:
		while(itr.hasNext()) {
			System.out.println("entered while; hasNext() = " + itr.hasNext());
			MethodAccess ma = itr.next();
			Object[] args = ma.getArguments();
			Method maMethod;
			try {
				maMethod = ma.getMethod();
			} catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break OUTER;
			}
			if(args.length > 0) {
				children.add(ParameterBlock.reconstruct(maMethod, args));
			}
			if(conditionOpt instanceof MethodConditionOption) {
				ConditionChoiceBox next = ((MethodConditionOption) conditionOpt).getNextCB();
				
				boolean selectedSuccessfully = false;
				INNER:
				for(ConditionOption co : next.getItems()) {
					if(co instanceof MethodConditionOption) {
						if(((MethodConditionOption) co).method.equals(maMethod)) {
							next.getSelectionModel().select(conditionOpt = co);
							selectedSuccessfully = true;
							break INNER;
						}
					}
					else {
						throw new UnsupportedOperationException("Unsupported ConditionOption type: " + next);
					}
				}
				if(!selectedSuccessfully) {
					throw new IllegalArgumentException("Could not locate method: " + maMethod);
				}
				children.add(next);
			}
			else {
				System.out.println(">>> found a non-MethodCO: "+ conditionOpt);
				break OUTER;
			}
		}
		
		System.out.println("*****DONE, RETURNING:"+builder);
		return builder;
	}
}

