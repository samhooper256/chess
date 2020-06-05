package chess.piecebuilder;

import javafx.geometry.Insets;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class ConditionUtilFlowPane extends FlowPane{
	private BoolPathDrop boolPathDrop;
	private IntegerPathDrop integerPathDrop;
	private ObjectPathDrop objectPathDrop;
	private MultiConditionDrop multiConditionDrop;
	public ConditionUtilFlowPane() {
		super();
		this.setHgap(10);
		boolPathDrop = new BoolPathDrop();
		integerPathDrop = new IntegerPathDrop();
		objectPathDrop = new ObjectPathDrop();
		multiConditionDrop = new MultiConditionDrop();
		this.getChildren().addAll(boolPathDrop, integerPathDrop, objectPathDrop, multiConditionDrop);
	}
	
	public class ConditionDrop extends BorderPane{
		private Label label;
		public ConditionDrop(String text) {
			label = new Label(text);
			label.setPadding(new Insets(4));
			this.setCenter(label);
		}
	}
	
	public class BoolPathDrop extends ConditionDrop{
		public BoolPathDrop() {
			super("Boolean Path");
			this.setStyle("-fx-background-color: rgba(228, 56, 255, 1.0);"); //TODO put in css
			this.setOnDragDetected(mouseEvent -> {
				Dragboard db = startDragAndDrop(TransferMode.COPY);
				SnapshotParameters parameters = new SnapshotParameters();
				parameters.setFill(Color.TRANSPARENT);
				Image dragViewImage = BoolPathDrop.this.snapshot(parameters, null);
				db.setDragView(dragViewImage, dragViewImage.getWidth()/2, dragViewImage.getHeight()/2);
				ClipboardContent content = new ClipboardContent();
		        content.putString("bool");
		        db.setContent(content);
		        mouseEvent.consume();
			});
		}
	}
	
	public class IntegerPathDrop extends ConditionDrop{
		public IntegerPathDrop() {
			super("Integer Path");
			this.setStyle("-fx-background-color: rgba(255, 149, 0, 1.0);"); //TODO put in css
			this.setOnDragDetected(mouseEvent -> {
				Dragboard db = startDragAndDrop(TransferMode.COPY);
				SnapshotParameters parameters = new SnapshotParameters();
				parameters.setFill(Color.TRANSPARENT);
				Image dragViewImage = IntegerPathDrop.this.snapshot(parameters, null);
				db.setDragView(dragViewImage, dragViewImage.getWidth()/2, dragViewImage.getHeight()/2);
				ClipboardContent content = new ClipboardContent();
		        content.putString("integer");
		        db.setContent(content);
		        mouseEvent.consume();
			});
		}
	}
	
	public class ObjectPathDrop extends ConditionDrop{
		public ObjectPathDrop() {
			super("Anything Path");
			this.setStyle("-fx-background-color: rgba(255, 235, 59, 1.0);"); //TODO put in css
			this.setOnDragDetected(mouseEvent -> {
				Dragboard db = startDragAndDrop(TransferMode.COPY);
				SnapshotParameters parameters = new SnapshotParameters();
				parameters.setFill(Color.TRANSPARENT);
				Image dragViewImage = ObjectPathDrop.this.snapshot(parameters, null);
				db.setDragView(dragViewImage, dragViewImage.getWidth()/2, dragViewImage.getHeight()/2);
				ClipboardContent content = new ClipboardContent();
		        content.putString("object");
		        db.setContent(content);
		        mouseEvent.consume();
			});
		}
	}
	
	public class MultiConditionDrop extends ConditionDrop{
		public MultiConditionDrop() {
			super("Multi-Condition");
			this.setStyle("-fx-background-color: rgba(38, 38, 255, 1.0);"); //TODO put in css
			this.setOnDragDetected(mouseEvent -> {
				Dragboard db = startDragAndDrop(TransferMode.COPY);
				SnapshotParameters parameters = new SnapshotParameters();
				parameters.setFill(Color.TRANSPARENT);
				Image dragViewImage = MultiConditionDrop.this.snapshot(parameters, null);
				db.setDragView(dragViewImage, dragViewImage.getWidth()/2, dragViewImage.getHeight()/2);
				ClipboardContent content = new ClipboardContent();
		        content.putString("multi-condition");
		        db.setContent(content);
		        mouseEvent.consume();
			});
		}
	}
}
