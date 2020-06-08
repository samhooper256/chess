package chess.piecebuilder;

import chess.util.BooleanPath;

public class BooleanLiteralConditionOption extends ConditionOption{
	private final boolean value;
	protected BooleanLiteralConditionOption(ConditionChoiceBox choiceBox, boolean value) {
		super(choiceBox);
		this.value = value;
	}
	
	public boolean getBooleanValue() {
		return value;
	}
	
	public BooleanPath getBooleanPath() {
		if(value) {
			return BooleanPath.trueConstantBoolPath;
		}
		else {
			return BooleanPath.falseConstantBoolPath;
		}
	}
	
	@Override
	public String toString() {
		return String.valueOf(value);
	}
	
	@Override
	public void updatePaneImpl() {
		//TODO - do I even do anything here?
	}

}
