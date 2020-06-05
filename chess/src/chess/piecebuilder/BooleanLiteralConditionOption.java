package chess.piecebuilder;

import chess.util.BoolPath;

public class BooleanLiteralConditionOption extends ConditionOption{
	boolean value;
	protected BooleanLiteralConditionOption(ConditionChoiceBox choiceBox, boolean value) {
		super(choiceBox);
		this.value = value;
	}
	
	public boolean getBooleanValue() {
		return value;
	}
	
	public BoolPath getBooleanPath() {
		if(value) {
			return BoolPath.trueConstantBoolPath;
		}
		else {
			return BoolPath.falseConstantBoolPath;
		}
	}
	
	@Override
	public String toString() {
		return String.valueOf(value);
	}
	
	@Override
	public void updatePane() {
		//TODO - do I even do anything here?
	}

}
