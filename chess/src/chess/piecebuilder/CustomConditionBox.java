package chess.piecebuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import chess.util.Condition;
import chess.util.InputVerification;
import chess.util.PathBase;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
public class CustomConditionBox extends VBox implements InputVerification, Buildable<Condition>{
	private BuildFinisher buildFinisher;
	private Pane nodeToAddTo;
	private boolean waitingForDrop;
	public CustomConditionBox(Pane ntad, boolean addDrop) {
		super();
		this.prefWidthProperty().bind(ntad.widthProperty());
		this.nodeToAddTo = ntad;
		this.setStyle("-fx-border-width: 1px; -fx-border-color: #b00000;");
		if(addDrop) {
			System.out.println("ccb +drop");
			this.getChildren().add(new DropPathPane(true));
		}
		else {
			System.out.println("ccb no drop");
		}
		waitingForDrop = true;
	}
	
	void addBuilder(String pathType, boolean suggestedAddFinisher) {
		System.out.printf(">>>addBuilder(%s, %s)", pathType, suggestedAddFinisher);
		System.out.println("children before: " + getChildren());
		waitingForDrop = false;
		PathBuilder builder = PathBuilder.getBuilderByStringEx(pathType);
		this.getChildren().add(builder);
		if(suggestedAddFinisher) {
			if(buildFinisher == null) {
				buildFinisher = BuildFinisher.getFinisherFor(builder);
				this.getChildren().add(buildFinisher);
				buildFinisher.postAdd();
				
			}
			else {
				throw new IllegalArgumentException("Cannot have multiple finishers!");
			}
		}
		System.out.println("children after: " + getChildren());
	}
	
	public void addFinisherEx(BuildFinisher fin, boolean doPostAdd) {
		if(buildFinisher == null) {
			buildFinisher = fin;
			this.getChildren().add(buildFinisher);
			if(doPostAdd) {
				buildFinisher.postAdd();
			}
		}
		else {
			throw new IllegalArgumentException("CustomConditionBox already has finisher");
		}
	}
	
	@Override
	public boolean verifyInput() {
		boolean result = true;
		//System.out.println("my children = " + getChildren());
		for(Node fxNode : getChildren()) {
			//System.out.println("verifying " + fxNode);
			if(fxNode instanceof InputVerification && !((InputVerification) fxNode).verifyInput()) {
				//System.out.println("\treturning because invalid");
				result = false;
			}
		}
		return result;
	}

	@Override
	public Condition build() {
		PathBuilder firstBuilder = buildFinisher.getPrecedingBuilder();
		PathBase abstractBase = firstBuilder.build();
		Method finishMethod = buildFinisher.getValue();
		Object[] actualArgs = new Object[finishMethod.getParameterCount()];
		int index = this.getChildren().indexOf(buildFinisher) + 1;
		for(int i = 0; i < actualArgs.length; i++) {
			Node child = this.getChildren().get(index + i);
			if(child instanceof PathBuilder) {
				actualArgs[i] = ((PathBuilder) child).build(); 
			}
			else if (child instanceof ChoiceBox<?>) {
				actualArgs[i] = ((ChoiceBox<?>) child).getValue();
			}
			else {
				throw new IllegalArgumentException(child + " is not a valid parameter-getter");
			}
		}
		try {
			return (Condition) finishMethod.invoke(abstractBase, actualArgs);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new IllegalArgumentException("Unknown Error");
	}
}


/*
//FlowPane implementation - might be better? //TODO Delete this if it's left unused
public class CustomConditionBox extends FlowPane implements InputVerification, Buildable<Condition>{
	private DropPathPane dropPathPane;
	private BuildFinisher buildFinisher;
	private Pane nodeToAddTo;
	private boolean waitingForDrop;
	public CustomConditionBox(Pane ntad) {
		super();
		this.prefWrapLengthProperty().bind(ntad.widthProperty());
		this.nodeToAddTo = ntad;
		this.setStyle("-fx-border-width: 1px; -fx-border-color: #b00000;");
		dropPathPane = new DropPathPane(true, this);
		this.getChildren().add(dropPathPane);
		waitingForDrop = true;
	}
	
	void addDropPathPane(String pathType, boolean addFinisher) {
		this.getChildren().add(new DropPathPane(addFinisher, this, pathType));
	}
	
	void notifyBuilderAdded(PathBuilder builder, boolean suggestedAddFinisher) {
		waitingForDrop = false;
		if(suggestedAddFinisher) {
			BuildFinisher finisher = BuildFinisher.on(builder);
			this.getChildren().add(BuildFinisher.on(builder));
			finisher.postAdd();
		}
	}
	
	@Override
	public boolean verifyInput() {
		// todo verify inputs!
		return true;
	}

	@Override
	public Condition build() {
		// todo Auto-generated method stub
		return null;
	}
}
*/