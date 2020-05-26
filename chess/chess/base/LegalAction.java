package chess.base;

import javafx.scene.shape.Shape;

public abstract class LegalAction {
	/*
	 * NOTE: The displayRow and displayCol values should indicated the square that should be displayed
	 * as the legal move for the player to click on. It does not have to be related to the actual
	 * move itself, although it should whenever possible.
	 * */
	protected int displayRow, displayCol;
	protected int destRow, destCol;
	
	public int destRow() {
		return destRow;
	}
	
	public int destCol() {
		return destCol;
	}
	
	public int row() {
		return displayRow;
	}
	
	public int col() {
		return displayCol;
	}
	
	/* *
	 * This method SHOULD BLOCK until the effect of this action is visible to the user.
	 */
	public abstract void handle(int startRow, int startColm, Board b);
	
	public abstract Shape getIndicator(int size);
	
	@Override
	public boolean equals(Object o) {
		if(o != null && o instanceof LegalAction) {
			LegalAction a = (LegalAction) o;
			return 	a.getClass() == this.getClass() && destRow == a.destRow
					&& destCol == a.destCol;
		}
		else {
			return false;
		}
	}
	
	@Override 
	public int hashCode() {
		return destRow * destCol ^ (destRow + destCol);
	}
	
	@Override
	public String toString() {
		return String.format("%s = [dest=(%d,%d), disp=(%d,%d)]", this.getClass().getName(), destRow,destCol,
				displayRow,displayCol);
	}
}
