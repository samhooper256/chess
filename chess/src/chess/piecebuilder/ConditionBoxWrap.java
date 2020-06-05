package chess.piecebuilder;

import chess.util.Condition;
import chess.util.InputVerification;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class ConditionBoxWrap extends VBox implements InputVerification, ErrorSubmitable, Buildable<Condition>{
	ESVBox esvBox;
	private Pane nodeToAddTo;
	private Button deleteConditionButton;
	public ConditionBoxWrap(Pane ntad) {
		if(!(ntad instanceof ErrorSubmitable)) {
			throw new IllegalArgumentException("nodeToAddTo is not ErrorSubmitable");
		}
		this.nodeToAddTo = ntad;
		esvBox = new ESVBox(this);
		this.setFillWidth(true);
		esvBox.setFillWidth(true);
		ConditionBox initialCB = new ConditionBox(this, this.esvBox);
		esvBox.getChildren().add((Node) initialCB);
		
		this.setStyle("-fx-border-color: pink;");
		deleteConditionButton = new Button("Delete Condition");
		deleteConditionButton.setStyle("-fx-background-color: transparent; -fx-border-width: 1px; -fx-border-color: #b00000;"
				+ "-fx-border-radius: 6; -fx-text-fill: #b00000;"); //TODO Put this in CSS (and add hover effect)
		deleteConditionButton.setOnMouseClicked(mouseEvent -> {
			nodeToAddTo.getChildren().remove(this);
		});
		this.getChildren().addAll(esvBox, deleteConditionButton);
		System.out.println(this + " created, esvBox = " + esvBox + ", initial ConditionBox = " + initialCB);
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
	@Override
	public void submitErrorMessage(String message) {
		((ErrorSubmitable) nodeToAddTo).submitErrorMessage(message);
	}

}
