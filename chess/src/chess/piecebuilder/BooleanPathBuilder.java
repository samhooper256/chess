package chess.piecebuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import chess.util.AFC;
import chess.util.BooleanPath;
import chess.util.Condition;
import chess.util.ConditionBuilder;
import chess.util.InputVerification;
import chess.util.MethodAccess;
import chess.util.PathBase;
import chess.util.RelativeTile;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

public class BooleanPathBuilder extends PathBuilder{
	public BooleanPathBuilder() {
		super();
		this.getStyleClass().add("boolean-path-builder");
		onChoiceBox.getItems().addAll(new BooleanLiteralConditionOption(onChoiceBox, true),
				new BooleanLiteralConditionOption(onChoiceBox, false));
	}

	@Override
	public String getPathTypeName() {
		return "Boolean";
	}

	@Override
	public BooleanPath build() {
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
		return conditionBuilder.toBooleanPath();
		
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new IllegalArgumentException("Unknown Error ( see stack trace )");
	}

	@Override
	public boolean verifyInput() {
		boolean result = super.verifyInput();
		if(!result) {
			PieceBuilder.submitError("Boolean Path has no selection for the \"on\" box");
			return false;
		}
		ObservableList<Node> children = this.getChildren(); //BoolPathBuilders are Panes
		Node last = children.get(children.size() - 1);
		if(last instanceof ConditionChoiceBox) {
			ConditionOption selected = ((ConditionChoiceBox) last).getValue();
			if(selected == null) {
				PieceBuilder.submitError("Boolean Path does have a selection");
				result = false;
			}
			else if(selected instanceof MethodConditionOption) {
				if(!(((MethodConditionOption) selected).method.getReturnType() == boolean.class)) {
					PieceBuilder.submitError("Boolean Path does not lead to a boolean property");
					result = false;
				}
			}
			else if(selected instanceof BooleanLiteralConditionOption) {
				//we're good, nothing to do here
			}
			else {
				throw new UnsupportedOperationException(selected.getClass() + " not supported");
			}
		}
		for(Node fxNode : children) {
			if(fxNode instanceof InputVerification) {
				result &= ((InputVerification) fxNode).verifyInput();
			}
		}
		return result;
	}
	
	public static BooleanPathBuilder reconstruct(boolean bool) {
		BooleanPathBuilder builder = new BooleanPathBuilder();
		for(ConditionOption co : builder.onChoiceBox.getItems()) {
			if(co instanceof BooleanLiteralConditionOption) {
				if(((BooleanLiteralConditionOption) co).getBooleanValue() == (bool)) {
					builder.onChoiceBox.getSelectionModel().select(co);
					break;
				}
			}
		}
		return builder;
	}
	
	public static BooleanPathBuilder reconstruct(BooleanPath path) {
		//TODO Very similar code to IntegerPath#reconstruct
		System.out.println("*****RECREATING:"+path);
		BooleanPathBuilder builder = new BooleanPathBuilder();
		ObservableList<Node> children = builder.getChildren();
		Object base = path.getBase();
		ConditionOption conditionOpt = null;
		if(base.getClass() == Boolean.class || base.getClass() == boolean.class) {
			for(ConditionOption co : builder.onChoiceBox.getItems()) {
				if(co instanceof BooleanLiteralConditionOption) {
					if(((BooleanLiteralConditionOption) co).getBooleanValue() == ((boolean) base)) {
						builder.onChoiceBox.getSelectionModel().select(conditionOpt = co);
						break;
					}
				}
			}
			return builder;
		}
		else {
			Method m = Condition.getOnMethodFromBase(base);
			if(m == null) {
				throw new IllegalArgumentException("Invalid BooleanPath base: " + base);
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
