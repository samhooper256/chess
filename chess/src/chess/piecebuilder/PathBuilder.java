package chess.piecebuilder;

import java.lang.reflect.Method;

import chess.util.AFC;
import chess.util.Condition;
import chess.util.PathBase;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
public abstract class PathBuilder extends HBox{
	private Label label;
	protected ConditionChoiceBox onChoiceBox;
	Pane nodeToAddTo;
	public PathBuilder(Pane ntad) {
		super();
		this.nodeToAddTo = ntad;
		this.setSpacing(4);
		this.setAlignment(Pos.CENTER_LEFT);
		label = new Label(getPathTypeName() + " Path on: ");
		this.onChoiceBox = new ConditionChoiceBox(PathBuilder.this);
		for(Method m : Condition.class.getMethods()) {
			if(m.isAnnotationPresent(AFC.class)) {
				onChoiceBox.addMethod(m);
			}
		}
		this.getChildren().addAll(label, onChoiceBox);
	}
	
	public abstract String getPathTypeName();
	
	public abstract PathBase build();
}
/*
//FlowPane implementation - might be better?
public abstract class PathBuilder extends FlowPane{
	private Label label;
	protected ConditionChoiceBox onChoiceBox;
	Pane nodeToAddTo;
	public PathBuilder(Pane ntad) {
		super();
		this.nodeToAddTo = ntad;
		this.setHgap(4);
		label = new Label(getPathTypeName() + " Path on: ");
		this.onChoiceBox = new ConditionChoiceBox(PathBuilder.this);
		for(Method m : Condition.class.getMethods()) {
			if(m.isAnnotationPresent(AFC.class)) {
				onChoiceBox.addMethod(m);
			}
		}
		this.getChildren().addAll(label, onChoiceBox);
	}
	
	public abstract String getPathTypeName();
	
	public abstract PathBase build();
}
*/