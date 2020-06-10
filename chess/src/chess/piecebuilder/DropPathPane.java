package chess.piecebuilder;

import java.util.Set;

import chess.util.InputVerification;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class DropPathPane extends BorderPane implements InputVerification {
	private Label label;
	private String pathType;
	private boolean addFinsiher;
	private boolean pathHasBeenDropped;
	public DropPathPane(boolean addFinisher) {
		super();
		this.pathHasBeenDropped = false;
		this.addFinsiher = addFinisher;
		this.label = new Label("Drop Any Path");
		this.pathType = null;
		this.getStyleClass().add("drop-path-pane");
		label.setPadding(new Insets(5));
		this.setCenter(label);
		this.setOnDragDropped(dragEvent -> {
			Dragboard db = dragEvent.getDragboard();
	        boolean success = false;
	        if (db.hasString()) {
	        	Set<TransferMode> transferModes = db.getTransferModes();
	        	if(transferModes.size() == 1 && transferModes.iterator().next() == TransferMode.COPY) {
	    			String builderName = db.getString();
	                PathBuilder builder = PathBuilder.getBuilderByStringNull(builderName);
	                if(builder == null) {
	                	System.out.println("It's null OOPS");
	                	dragEvent.setDropCompleted(false);
	                	return; //don't consume so it goes to MultiCondition
	                }
	                else {
	                	CustomConditionBox ccb = ((CustomConditionBox) getParent());
	                	ObservableList<Node> children = ccb.getChildren();
	                	int myIndex = children.indexOf(DropPathPane.this);
	                	DropPathPane.this.pathHasBeenDropped = true;
	                	ConditionBox.clearPast(children, myIndex, true);
	                	ccb.addBuilder(builderName, addFinisher);
	                	success = true;
	                }
	        	}
	        }
	        dragEvent.setDropCompleted(success);
	        dragEvent.consume();
		});
		this.setOnDragOver(dragEvent -> {
			Dragboard db = dragEvent.getDragboard();
	        if (db.hasString() && !db.getString().equals("mutli-condition")) {
	        	dragEvent.acceptTransferModes(TransferMode.COPY);
	        	dragEvent.consume(); 	//we only consume in here so that it will propagate to MultiCondition
	        							//if it's unsuccessful here.
	        }
		});
	}

	@Override
	public boolean verifyInput() {
		if(!pathHasBeenDropped) {
			if(pathType != null) {
				PieceBuilder.submitError("Custom Condition needs a " +pathType + " Path to be dropped");
			}
			else {
				PieceBuilder.submitError("Custom Condition needs a path to be dropped");
			}
			return false;
		}
		else {
			return true;
		}
	}
	
}
