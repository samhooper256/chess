package chess.piecebuilder;

import javafx.scene.control.MenuItem;

public abstract class ConditionOption extends MenuItem {
	protected static volatile boolean updatesAllowed = true;
	public static void setUpdatesAllowed(boolean newUpdatesAllowed) {
		updatesAllowed = newUpdatesAllowed;
	}
	protected final ConditionChoiceBox choiceBox;
	
	protected ConditionOption(ConditionChoiceBox choiceBox) {
		super();
		this.choiceBox = choiceBox;
	}
	
	public void updatePane() {
		if(updatesAllowed) {
			updatePaneImpl();
		}
	}
	
	protected abstract void updatePaneImpl();
}
