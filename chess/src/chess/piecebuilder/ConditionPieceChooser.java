package chess.piecebuilder;

import java.util.Collection;

import chess.base.Piece;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;

public class ConditionPieceChooser extends ChoiceBox<String>{
	private static Collection<String> pieceNames;
	
	static {
		pieceNames = Piece.getNamesOfAllPieces();
	}
	
	public ConditionPieceChooser() {
		updatePieceNames();
		ObservableList<String> items = this.getItems();
		for(String pieceName : pieceNames) {
			items.add(pieceName);
		}
	}
	
	public void updatePieceNames() {
		pieceNames = Piece.getNamesOfAllPieces();
	}

}
