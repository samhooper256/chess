package chess.piecebuilder;

import java.lang.reflect.Method;

import chess.util.AFC;
import chess.util.BooleanPath;
import chess.util.Condition;
import chess.util.InputVerification;
import chess.util.IntegerPath;
import chess.util.ObjectPath;
import chess.util.PathBase;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
public abstract class PathBuilder extends HBox implements InputVerification{
	private Label label;
	protected ConditionChoiceBox onChoiceBox;
	
	public PathBuilder() {
		super();
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
	
	public static final String 	OBJECT_BUILDER = "Object",
								BOOLEAN_BUILDER = "Boolean",
								INTEGER_BUILDER = "Integer";
	/**
	 * Returns null if the string is invalid.
	 * @param <T>
	 * @param builderType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends PathBuilder> T getBuilderByStringNull(String builderType) {
		switch(builderType) {
		case INTEGER_BUILDER: return (T) new IntegerPathBuilder();
		case BOOLEAN_BUILDER: return (T) new BooleanPathBuilder();
		case OBJECT_BUILDER: return (T) new ObjectPathBuilder();
		default: return null;
		}
	}
	
	/**
	 * Throws Exception if the string is invalid.
	 * @param <T>
	 * @param builderType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends PathBuilder> T getBuilderByStringEx(String builderType) {
		switch(builderType) {
		case INTEGER_BUILDER: return (T) new IntegerPathBuilder();
		case BOOLEAN_BUILDER: return (T) new BooleanPathBuilder();
		case OBJECT_BUILDER: return (T) new ObjectPathBuilder();
		default: throw new IllegalArgumentException(builderType + " is not a valid builder name");
		}
	}
	
	public static String getStringForBuilder(PathBuilder builder) {
		if(builder instanceof IntegerPathBuilder) {
			return INTEGER_BUILDER;
		}
		else if(builder instanceof BooleanPathBuilder) {
			return BOOLEAN_BUILDER;
		}
		else if(builder instanceof ObjectPathBuilder) {
			return OBJECT_BUILDER;
		}
		else {
			throw new UnsupportedOperationException(builder.getClass() + " is not supported as a builder type");
		}
	}
	
	
	public abstract String getPathTypeName();
	
	public abstract PathBase build();
	
	@Override
	public boolean verifyInput() {
		if(onChoiceBox.getSelectionModel().isEmpty()) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public static PathBuilder reconstruct(PathBase path){
		if(path instanceof IntegerPath) {
			return IntegerPathBuilder.reconstruct((IntegerPath) path);
		}
		else if(path instanceof BooleanPath) {
			return BooleanPathBuilder.reconstruct((BooleanPath) path);
		}
		else if(path instanceof ObjectPath) {
			return ObjectPathBuilder.reconstruct((ObjectPath) path);
		}
		else {
			throw new UnsupportedOperationException("PathBase type " + path.getClass() + " is not supported");
		}
		
	}
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