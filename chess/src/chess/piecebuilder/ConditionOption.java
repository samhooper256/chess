package chess.piecebuilder;

import javafx.scene.control.MenuItem;

public abstract class ConditionOption extends MenuItem{
	protected final ConditionChoiceBox choiceBox;
	
	protected ConditionOption(ConditionChoiceBox choiceBox) {
		super();
		this.choiceBox = choiceBox;
	}
	
	public abstract void updatePane();
	
	
}
