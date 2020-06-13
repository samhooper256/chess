package chess.piecebuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import chess.util.Condition;
import chess.util.ConditionCombiner;
import chess.util.InputVerification;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class MultiConditionBox extends VBox implements MultiConditionPart{
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
	
	{
		this.setOnDragOver(dragEvent -> {
			//System.out.println("MultiConditionBox drag over");
			Dragboard db = dragEvent.getDragboard();
	        if (db.hasString() && db.getString().equals("multi-condition")) {
	        	dragEvent.acceptTransferModes(TransferMode.COPY);
	        }
	        dragEvent.consume();
		});
		this.setOnDragDropped(dragEvent -> {
			//System.out.println("MultiConditionBox dropped (consumes)");
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
	}
	
	{
		this.getStyleClass().addAll("multi-condition-part", "multi-condition-box");
	}
	
	public <T extends Node & MultiConditionPart> MultiConditionBox(T first){
		super(4);
		System.out.println("Entered MCP constructor, mcp passed was: " + first);
		this.setFillWidth(true);
		MultiConditionPart mcp1, mcp2;
		mcp1 = first;
		mcp2 = new ConditionBox();
		Pane myParent = (Pane) ((Node) mcp1).getParent();
		int mcp1index = myParent.getChildren().indexOf(mcp1);
		//System.out.println("\mcp1's ntad is" + this + " (this)");
		myParent.getChildren().remove(mcp1);
		//System.out.println("\tmy ntad's children (after removing mcp1):" + nodeToAddTo.getChildren());
		choiceBox = new ChoiceBox<>();
		choiceBox.getItems().addAll(Condition.postConstructionModifierMethods);
		choiceBox.setConverter(methodStringConverter);
		choiceBox.setValue(Condition.postConstructionModifierMethods[0]);
		this.getChildren().addAll((Node) mcp1, choiceBox, (Node) mcp2);
		//System.out.println("\tmy children (after add 3 things):" + getChildren());
		myParent.getChildren().add(mcp1index, this);
		//System.out.println("\tfinally, my ntad's children (after ntad.getChildren().add(mcp1index, this)): " + nodeToAddTo.getChildren());
		System.out.println("created: " +this );
	}
	public <T extends Node & MultiConditionPart> MultiConditionBox(Method modifierMethod, T first, T second) {
		this.setFillWidth(true);
		choiceBox = new ChoiceBox<>();
		choiceBox.getItems().addAll(Condition.postConstructionModifierMethods);
		choiceBox.setConverter(methodStringConverter);
		choiceBox.setValue(modifierMethod);
		this.getChildren().addAll((Node) first, choiceBox, (Node) second);
	}
	@Override
	public boolean verifyInput() {
		return 	((InputVerification) getChildren().get(0)).verifyInput() &
				((InputVerification) getChildren().get(2)).verifyInput(); //single & on purpose
	}
	
	@Override
	public Condition build() {
		try {
			return (Condition) choiceBox.getValue().invoke(null, ((Buildable<Condition>) getChildren().get(0)).build(),
					((Buildable<Condition>) getChildren().get(2)).build());
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		throw new IllegalArgumentException("Unkown error");
	}
	@Override
	public String toString() {
		return "[MutliConditionBox@"+hashCode()+", children="+getChildren()+"]";
	}
}
