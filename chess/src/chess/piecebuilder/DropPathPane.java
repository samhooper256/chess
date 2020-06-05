package chess.piecebuilder;

import java.util.Set;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class DropPathPane extends BorderPane{
	private Label label;
	private String pathType;
	private boolean addFinsiher;
	public Pane nodeToAddTo;
	public DropPathPane(boolean addFinisher, Pane ntad) {
		super();
		this.addFinsiher = addFinisher;
		this.label = new Label("Drop Any Path");
		this.nodeToAddTo = ntad;
		label.setPadding(new Insets(5));
		this.setCenter(label);
		this.setOnDragDropped(dragEvent -> {
			Dragboard db = dragEvent.getDragboard();
	        boolean success = false;
	        if (db.hasString()) {
	        	Set<TransferMode> transferModes = db.getTransferModes();
	        	if(transferModes.size() == 1 && transferModes.iterator().next() == TransferMode.COPY) {
	    			String builderName = db.getString();
	                PathBuilder builder = null;
	                if(builderName.equals("bool")) {
	                	builder = new BoolPathBuilder(nodeToAddTo);
	                }
	                else if(builderName.equals("integer")) {
	                	builder = new IntegerPathBuilder(nodeToAddTo);
	                }
	                else if(builderName.equals("object")) {
	                	builder = new ObjectPathBuilder(nodeToAddTo);
	                }
	                else {
	                	dragEvent.setDropCompleted(false);
	                	return; //don't consume so it goes to MultiCondition
	                }
	                
	                if(builder != null) {
	                	ObservableList<Node> children = nodeToAddTo.getChildren();
	                	int myIndex = children.indexOf(DropPathPane.this);
	                	ConditionBox.clearPast(children, myIndex, true);
	                	children.add(builder);
	                	if(nodeToAddTo instanceof CustomConditionBox) {
	                		((CustomConditionBox) nodeToAddTo).notifyBuilderAdded(builder, addFinisher);
	                	}
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
		this.setStyle("-fx-border-width: 1px; -fx-border-color: #55ff55;");
	}
	
	public DropPathPane(boolean addFinisher, Pane ntad, String pathType) {
		super();
		this.addFinsiher = addFinisher;
		this.pathType = pathType;
		this.nodeToAddTo = ntad;
		String labelText = "???";
		if(pathType.equals("bool")) {
			labelText = "Boolean";
		}
		else if(pathType.equals("integer")) {
			labelText = "Integer";
		}
		else if(pathType.equals("object")) {
			labelText = "Object";
		}
		else {
			throw new IllegalArgumentException("Invalid pathType (" + pathType + ")");
		}
		this.label = new Label("Drop " + labelText + " Path");
		label.setPadding(new Insets(5));
		this.setCenter(label);
		this.setOnDragDropped(dragEvent -> {
			//System.out.println("TYPE II DROPPED");
			Dragboard db = dragEvent.getDragboard();
	        boolean success = false;
	        if (db.hasString() && !db.getString().equals("multi-condition")) {
	        	Set<TransferMode> transferModes = db.getTransferModes();
	        	if(transferModes.size() == 1 && transferModes.iterator().next() == TransferMode.COPY) {
	    			String builderName = db.getString();
	                PathBuilder builder = null;
	                if(builderName.equals("bool")) {
	                	builder = new BoolPathBuilder(nodeToAddTo);
	                }
	                else if(builderName.equals("integer")) {
	                	builder = new IntegerPathBuilder(nodeToAddTo);
	                }
	                else if(builderName.equals("object")) {
	                	builder = new ObjectPathBuilder(nodeToAddTo);
	                }
	                else {
	                	throw new IllegalArgumentException("bad builder name: " + builderName);
	                }
	                
	                if(builder != null) {
	                	ObservableList<Node> children = nodeToAddTo.getChildren();
	                	int myIndex = children.indexOf(DropPathPane.this);
	                	ConditionBox.clearPast(children, myIndex, true);
	                	children.add(builder);
	                	if(nodeToAddTo instanceof CustomConditionBox) {
	                		((CustomConditionBox) nodeToAddTo).notifyBuilderAdded(builder, addFinisher);
	                	}
	                	success = true;
	                }
	        	}
	        }
	        else {
	        	dragEvent.setDropCompleted(false);
	        	return; //don't consume so it goes to MutliCondition
	        }
	        dragEvent.setDropCompleted(success);
	        dragEvent.consume();
		});
		this.setOnDragOver(dragEvent -> {
			Dragboard db = dragEvent.getDragboard();
	        if (db.hasString() && db.getString().equals(pathType)) {
	        	dragEvent.acceptTransferModes(TransferMode.COPY);
	        	dragEvent.consume();
	        }
		});
		this.setStyle("-fx-border-width: 1px; -fx-border-color: #00ff00;");
	}
	
}
