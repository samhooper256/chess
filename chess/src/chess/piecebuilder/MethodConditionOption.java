package chess.piecebuilder;

import java.lang.reflect.Method;

import chess.util.AFC;
import chess.util.IntTextField;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class MethodConditionOption extends ConditionOption{
	Method method;
	private AFC afc;
	private String customName;
	public MethodConditionOption(ConditionChoiceBox choiceBox, Method m) {
		super(choiceBox);
		this.method = m;
		this.afc = method.getAnnotation(AFC.class);
		Pane pb = super.choiceBox.nodeToAddTo;
		if(	pb instanceof BooleanPathBuilder && 
			(afc.returnType() == boolean.class || afc.returnType() == Void.class && m.getReturnType() == boolean.class)) {
			//TODO Green text for booleans
		}
		if(	pb instanceof BooleanPathBuilder && 
			(afc.returnType() == int.class || afc.returnType() == Void.class && m.getReturnType() == int.class)) {
			//TODO Green text for ints
		}
		customName = null;
	}
	
	public MethodConditionOption(ConditionChoiceBox choiceBox, String name, Method m) {
		super(choiceBox);
		this.method = m;
		this.afc = method.getAnnotation(AFC.class);
		if(afc.returnType() != Void.class && m.getReturnType() == boolean.class) {
			//TODO Green text for booleans
		}
		customName = name;
	}
	
	@Override 
	public String toString() {
		return customName == null ? afc.name() : customName;
	}
	@Override
	public void updatePaneImpl() {
		Class<?> returnType = getMethodReturnType();
		Pane pb = super.choiceBox.nodeToAddTo;
		ObservableList<Node> children = pb.getChildren();
		int myIndex = children.indexOf(super.choiceBox);
		ConditionBox.clearPast(children, myIndex);
		System.out.println("method.getParameterCount() == " + method.getParameterCount());
		if(method.getParameterCount() > 0) {
			System.out.println("MAKING PBLOCK");
			ParameterBlock pBlock = new ParameterBlock(method);
			children.add(pBlock);
		}
		if(pb instanceof BooleanPathBuilder && returnType == boolean.class) {
			//TODO Do something?
		}
		else if(pb instanceof IntegerPathBuilder && returnType == int.class) {
			//TODO Maybe support Integer.class as well?
			//TODO Do something?
		}
		else {
			ConditionChoiceBox newCB = getNextCB(returnType);
			if(newCB.getItems().isEmpty()) {
				if(!(pb instanceof ObjectPathBuilder)) {
					children.add(new Label("(no matches found)"));//TODO maybe set text color to red?
				}
			}
			else {
				children.add(newCB);
			}
		}
	}

	public ConditionChoiceBox getNextCB(Class<?> returnType) {
		Pane pb = super.choiceBox.nodeToAddTo;
		ConditionChoiceBox newCB = new ConditionChoiceBox(super.choiceBox.nodeToAddTo);
		for(Method m : returnType.getMethods()) {
			if(m.isAnnotationPresent(AFC.class)) {
				if(pb instanceof PathBuilder) {
					Class<?> nextReturnType = m.getReturnType();
					if(pb instanceof BooleanPathBuilder) {
						if(nextReturnType == boolean.class || !nextReturnType.isPrimitive()) {
							newCB.addMethod(m);
						}
					}
					else if(pb instanceof IntegerPathBuilder) {
						if(nextReturnType == int.class || !nextReturnType.isPrimitive()) {
							newCB.addMethod(m);
						}
					}
					else if(pb instanceof ObjectPathBuilder) {
						if(!nextReturnType.isPrimitive()) { 
							//TODO Maybe the "anything path" should allow for anything? even primitives?
							newCB.addMethod(m);
						}
					}
					else {
						throw new UnsupportedOperationException(pb.getClass() + " not supported");
					}
				}
				else {
					newCB.addMethod(m);
				}
			}
		}
		return newCB;
	}
	
	public ConditionChoiceBox getNextCB() {
		return getNextCB(getMethodReturnType());
	}
	
	public Class<?> getMethodReturnType(){
		return afc.returnType() == Void.class ? method.getReturnType() : afc.returnType();
	}
	
}
