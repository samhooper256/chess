package chess.piecebuilder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Set;

import chess.util.AFC;
import chess.util.Condition;
import chess.util.ConditionCombiner;
import chess.util.InputVerification;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class MultiConditionBox extends VBox implements InputVerification, MultiConditionPart{
	private MultiConditionPart mcp1, mcp2;
	private Pane nodeToAddTo;
	private ChoiceBox<Method> choiceBox;
	private static final StringConverter<Method> methodStringConverter = new StringConverter<>() {

		@Override
		public String toString(Method method) {
			return method.getAnnotation(ConditionCombiner.class).name();
		}

		@Override
		public Method fromString(String string) {
			return null;
		}
	};
	public MultiConditionBox(MultiConditionPart first){
		super(4);
		this.setFillWidth(true);
		this.mcp1 = first;
		this.mcp2 = new ConditionBox(this);
		this.nodeToAddTo = first.getNodeToAddTo();
		first.setNodeToAddTo(this);
		ObservableList<Node> children = nodeToAddTo.getChildren();
		int index = children.indexOf(mcp1);
		children.remove(mcp1);
		//System.out.println("index = " + index);
		//System.out.println(nodeToAddTo + "\n" + nodeToAddTo.getChildren());
		choiceBox = new ChoiceBox<>();
		choiceBox.getItems().addAll(Condition.postConstructionModifierMethods);
		choiceBox.setConverter(methodStringConverter);
		choiceBox.setValue(Condition.postConstructionModifierMethods[0]);
		this.getChildren().addAll((Node) mcp1, choiceBox, (Node) mcp2);
		if(index >= 0) {
			children.add(index, this);
		}
		else {
			throw new IllegalArgumentException("It's not there???");
		}
		//TODO COde from this line to *** is the exact same as in CustomConditionBox - fix?
		this.setOnDragOver(dragEvent -> {
			Dragboard db = dragEvent.getDragboard();
	        if (db.hasString() && db.getString().equals("multi-condition")) {
	        	dragEvent.acceptTransferModes(TransferMode.COPY);
	        }
	        dragEvent.consume();
		});
		this.setOnDragDropped(dragEvent -> {
			//System.out.println("MultiConditionBox dropped (consumes): " + dragEvent);
			Dragboard db = dragEvent.getDragboard();
	        boolean success = false;
	        if (db.hasString() && db.getString().equals("multi-condition")) {
	        	Set<TransferMode> transferModes = db.getTransferModes();
	        	if(transferModes.size() == 1 && transferModes.iterator().next() == TransferMode.COPY) {
	    			new MultiConditionBox(this); //constructor handles rewiring nodeToAddTo
	        	}
	        }
	        dragEvent.setDropCompleted(success);
	        dragEvent.consume();
		});
		//***
		
		this.setStyle("-fx-border-width: 1px; -fx-border-color: rgba(38, 38, 255, 1.0);"); //TODO put in CSS (this and elsewhere)
	}
	@Override
	public boolean verifyInput() {
		// TODO verify inputs!
		return true;
	}
	
	@Override
	public Pane getNodeToAddTo() {
		return nodeToAddTo;
	}
	@Override
	public void setNodeToAddTo(Pane node) {
		this.nodeToAddTo = node;
	}
	@Override
	public Condition build() {
		try {
			return (Condition) choiceBox.getValue().invoke(null, mcp1.build(), mcp2.build());
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new IllegalArgumentException("Unkown error");
	}
	@Override
	public void submitErrorMessage(String message) {
		((ErrorSubmitable) nodeToAddTo).submitErrorMessage(message);
	}
}
