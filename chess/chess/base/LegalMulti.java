package chess.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import chess.util.MoveAndCaptureAction;
import chess.util.PromotionAction;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

public class LegalMulti extends LegalAction{
	ArrayList<LegalAction> actions;
	
	public LegalMulti(int dispRow, int dispCol, LegalAction... actions) {
		this.displayRow = dispRow;
		this.displayCol = dispCol;
		ArrayList<LegalAction> a = new ArrayList<>();
		for(int i = 0; i < actions.length; i++) {
			a.add(actions[i]);
		}
		this.actions = a;
	}
	
	public LegalMulti(int dispRow, int dispCol, ArrayList<LegalAction> actions) {
		this.displayRow = dispRow;
		this.displayCol = dispCol;
		this.destRow = dispRow;
		this.destCol = dispCol;
		this.actions = new ArrayList<>(actions);
	}
	
	public ArrayList<LegalAction> getActions(){
		return actions;
	}

	@Override
	public void handle(int startRow, int startCol, Board b) {
		for(int i = 0; i < actions.size(); i++) {
			actions.get(i).handle(startRow, startCol, b);
		}
	}

	@Override
	public Shape getIndicator(int size) {
		Circle indicator = new Circle(size);
		indicator.getStyleClass().add("multi");
		return indicator;
	}
}
