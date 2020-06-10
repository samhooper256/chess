package chess.piecebuilder;

import chess.util.Flag;
import chess.util.InputVerification;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class FlagInputHBox extends HBox implements InputVerification{
	private final Flag[] supportedFlags;
	private ChoiceBox<Flag> flagChoice;
	private final String title;
	/**
	 * 
	 * @param spacing
	 * @param title
	 * @param supportedFlags must contain at least one Flag or an IllegalArgumentException will be thrown.
	 */
	public FlagInputHBox(int spacing, String title, Flag... supportedFlags) {
		super(spacing);
		if(supportedFlags.length == 0) {
			throw new IllegalArgumentException("supportedFlags.length == 0");
		}
		this.title = title;
		this.supportedFlags = supportedFlags;
		flagChoice = new ChoiceBox<>();
		flagChoice.getItems().addAll(supportedFlags);
		this.getChildren().addAll(new Label(title), flagChoice);
		this.setAlignment(Pos.CENTER_LEFT);
	}
	
	/**
	 * 
	 * @param newValue
	 * @throws IllegalArgumentException if newValue is not supported by this FlagInputHBox
	 */
	public void setValue(Flag newValue) {
		for(Flag f : supportedFlags) {
			if(f == newValue) {
				flagChoice.getSelectionModel().select(newValue);
				return;
			}
		}
		throw new IllegalArgumentException("newValue Flag is not supported");
	}
	public Flag getFlag() {
		return flagChoice.getValue();
	}

	@Override
	public boolean verifyInput() {
		if(flagChoice.getSelectionModel().isEmpty()) {
			PieceBuilder.submitError(title + " box has no selection.");
			return false;
		}
		else {
			return true;
		}
	}
}
