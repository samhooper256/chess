package chess.piecebuilder;

import java.util.ArrayList;

import chess.util.InputVerification;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;

class PieceOptionsInputHBox extends HBox implements InputVerification{
	private static final int SPACING = 5;
	private TilePane tilePane;
	public PieceOptionsInputHBox(String parameterName) {
		super(SPACING);
		this.setAlignment(Pos.CENTER_LEFT);
		tilePane = new TilePane();
		tilePane.setSnapToPixel(true);
		tilePane.setTileAlignment(Pos.CENTER_LEFT);
		tilePane.setVgap(5);
		tilePane.setHgap(5);
		ObservableList<Node> tilePaneChildren = tilePane.getChildren();
		for(String s : PieceBuilder.currentPieceNames()) {
			if(!s.equals("King")) {
				tilePaneChildren.add(new CheckBox(s));
			}
		}
		this.getChildren().addAll(new Label(String.format("%s: ", parameterName)), tilePane);
	}
	
	public ArrayList<String> getArrayList() {
		ArrayList<String> end = new ArrayList<>();
		for(Node fxNode : tilePane.getChildren()) {
			CheckBox cb = (CheckBox) fxNode;
			if(cb.isSelected()) {
				end.add(cb.getText());
			}
		}
		return end;
	}

	@Override
	public boolean verifyInput() {
		for(Node fxNode : tilePane.getChildren()) {
			CheckBox cb = (CheckBox) fxNode;
			if(cb.isSelected()) {
				return true;
			}
		}
		PieceBuilder.submitError(((Label) this.getChildren().get(0)).getText() + " needs at least one piece");
		return false;
	}

	public void selectAll(ArrayList<String> pieceNames) {
		OUTER:
		for(String name : pieceNames) {
			for(Node fxNode : tilePane.getChildren()) {
				if(fxNode instanceof CheckBox && ((CheckBox) fxNode).getText().equals(name)) {
					((CheckBox) fxNode).setSelected(true);
					continue OUTER;
				}
			}
		}
	}
}
