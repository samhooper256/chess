package chess.piecebuilder;

import chess.util.Condition;
import chess.util.InputVerification;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class ConditionBoxWrap extends VBox implements InputVerification, Buildable<Condition>{
	VBox esvBox;
	private Button deleteConditionButton;
	
	{
		this.getStyleClass().add("condition-box-wrap");
	}
	
	public ConditionBoxWrap() {
		this((MultiConditionPart) (new ConditionBox()));
	}
	
	public ConditionBoxWrap(MultiConditionPart part) {
		if(!(part instanceof Node)) {
			throw new IllegalArgumentException("part does not extend javafx Node: " + part + "(class="+part.getClass()+")");
		}
		esvBox = new VBox();
		this.setFillWidth(true);
		esvBox.setFillWidth(true);
		esvBox.getChildren().add((Node) (part));
		deleteConditionButton = new Button("Delete Condition");
		deleteConditionButton.getStyleClass().add("delete-button");
		deleteConditionButton.setOnMouseClicked(mouseEvent -> {
			((Pane) getParent()).getChildren().remove(this);
		});
		this.getChildren().addAll(esvBox, deleteConditionButton);
		System.out.println(this + " created, esvBox = " + esvBox + ", initial ConditionBox = " + esvBox.getChildren().get(0));
		
	}
	@Override
	public Condition build() {
		return ((Buildable<Condition>) esvBox.getChildren().get(0)).build();
	}

	@Override
	public boolean verifyInput() {
		if(esvBox.getChildren().size() != 1) {
			throw new IllegalArgumentException("esvBox.size() != 1");
		}
		else {
			return ((InputVerification) esvBox.getChildren().get(0)).verifyInput();
		}
	}
	
	static ConditionBoxWrap reconstructCondition(Condition con) {
		//System.out.println("(WRAP)RECONSTRUCTING="+con);
		return new ConditionBoxWrap(ConditionBox.reconstruct(con));
	}
}
