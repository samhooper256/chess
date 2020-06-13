package chess.piecebuilder;

import javafx.geometry.Insets;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;

public class ConditionUtilFlowPane extends FlowPane{
	private BooleanPathDrop boolPathDrop;
	private IntegerPathDrop integerPathDrop;
	private ObjectPathDrop objectPathDrop;
	private MultiConditionDrop multiConditionDrop;
	public ConditionUtilFlowPane() {
		super();
		this.setHgap(10);
		boolPathDrop = new BooleanPathDrop();
		integerPathDrop = new IntegerPathDrop();
		objectPathDrop = new ObjectPathDrop();
		multiConditionDrop = new MultiConditionDrop();
		this.getChildren().addAll(boolPathDrop, integerPathDrop, objectPathDrop, multiConditionDrop);
	}
	
	public abstract class ConditionDrop extends BorderPane{
		private Label label;
		public ConditionDrop(String text) {
			label = new Label(text);
			label.setPadding(new Insets(4));
			this.getStyleClass().add("condition-drop");
			this.setCenter(label);
		}
	}
	
	public class BooleanPathDrop extends ConditionDrop{
		public BooleanPathDrop() {
			super("Boolean Path");
			this.getStyleClass().add("boolean-path-drop");
			this.setOnDragDetected(mouseEvent -> {
				Dragboard db = startDragAndDrop(TransferMode.COPY);
				SnapshotParameters parameters = new SnapshotParameters();
				parameters.setFill(Color.TRANSPARENT);
				Image dragViewImage = BooleanPathDrop.this.snapshot(parameters, null);
				db.setDragView(dragViewImage, dragViewImage.getWidth()/2, dragViewImage.getHeight()/2);
				ClipboardContent content = new ClipboardContent();
		        content.putString(PathBuilder.BOOLEAN_BUILDER);
		        db.setContent(content);
		        mouseEvent.consume();
			});
		}
	}
	
	public class IntegerPathDrop extends ConditionDrop{
		public IntegerPathDrop() {
			super("Integer Path");
			this.getStyleClass().add("integer-path-drop");
			this.setOnDragDetected(mouseEvent -> {
				Dragboard db = startDragAndDrop(TransferMode.COPY);
				SnapshotParameters parameters = new SnapshotParameters();
				parameters.setFill(Color.TRANSPARENT);
				Image dragViewImage = IntegerPathDrop.this.snapshot(parameters, null);
				db.setDragView(dragViewImage, dragViewImage.getWidth()/2, dragViewImage.getHeight()/2);
				ClipboardContent content = new ClipboardContent();
		        content.putString(PathBuilder.INTEGER_BUILDER);
		        db.setContent(content);
		        mouseEvent.consume();
			});
		}
	}
	
	public class ObjectPathDrop extends ConditionDrop{
		public ObjectPathDrop() {
			super("Anything Path");
			this.getStyleClass().add("object-path-drop");
			this.setOnDragDetected(mouseEvent -> {
				Dragboard db = startDragAndDrop(TransferMode.COPY);
				SnapshotParameters parameters = new SnapshotParameters();
				parameters.setFill(Color.TRANSPARENT);
				Image dragViewImage = ObjectPathDrop.this.snapshot(parameters, null);
				db.setDragView(dragViewImage, dragViewImage.getWidth()/2, dragViewImage.getHeight()/2);
				ClipboardContent content = new ClipboardContent();
		        content.putString(PathBuilder.OBJECT_BUILDER);
		        db.setContent(content);
		        mouseEvent.consume();
			});
		}
	}
	
	public class MultiConditionDrop extends ConditionDrop{
		public MultiConditionDrop() {
			super("Multi-Condition");
			this.getStyleClass().add("multi-condition-drop");
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
