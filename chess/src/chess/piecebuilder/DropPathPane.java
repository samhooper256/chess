package chess.piecebuilder;

import java.util.Set;

import chess.util.InputVerification;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;

public class DropPathPane extends BorderPane implements InputVerification {
	private Label label;
	private String pathType;
	private boolean pathHasBeenDropped, addFinisher;
	public DropPathPane(boolean initAddFinisher) {
		super();
		this.addFinisher = initAddFinisher;
		ContextMenu cm = new ContextMenu();
		MenuItem booleanPath = new MenuItem("Boolean Path");
		booleanPath.setOnAction(actionEvent -> {
			drop(PathBuilder.BOOLEAN_BUILDER);
		});
		MenuItem integerPath = new MenuItem("Integer Path");
		integerPath.setOnAction(actionEvent -> {
			drop(PathBuilder.INTEGER_BUILDER);
		});
		MenuItem anythingPath = new MenuItem("Anything Path");
		anythingPath.setOnAction(actionEvent -> {
			drop(PathBuilder.OBJECT_BUILDER);
		});
		cm.getItems().addAll(booleanPath, integerPath, anythingPath);
		
		this.pathHasBeenDropped = false;
		this.label = new Label("Drop Path or Right Click");
		this.pathType = null;
		this.getStyleClass().add("drop-path-pane");
		label.setPadding(new Insets(5));
		this.setCenter(label);
		this.setOnContextMenuRequested(contextMenuEvent -> {
			cm.show(this, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
		});
		this.setOnDragDropped(dragEvent -> {
			Dragboard db = dragEvent.getDragboard();
	        boolean success = false;
	        if (db.hasString()) {
	        	Set<TransferMode> transferModes = db.getTransferModes();
	        	if(transferModes.size() == 1 && transferModes.iterator().next() == TransferMode.COPY) {
	    			drop(db.getString());
                	success = true;
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
	
	private void drop(String builderName) {
		if(pathHasBeenDropped) {
			return;
		}
    	CustomConditionBox ccb = ((CustomConditionBox) getParent());
    	ObservableList<Node> children = ccb.getChildren();
    	int myIndex = children.indexOf(DropPathPane.this);
    	DropPathPane.this.pathHasBeenDropped = true;
    	ConditionBox.clearPast(children, myIndex, true);
    	ccb.addBuilder(builderName, addFinisher);
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
