package chess.piecebuilder;

import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

//Does not implement InputVerification because input will always be valid
public class BooleanInputHBox extends HBox{
	private static final int SPACING = 5;
	private CheckBox checkBox;
	public BooleanInputHBox(String parameterName) {
		super(SPACING);
		this.setAlignment(Pos.CENTER_LEFT);
		this.getChildren().add(new Label(String.format("%s: ", parameterName)));
		checkBox = new CheckBox();
		this.getChildren().add(checkBox);
	}
	
	public void setValue(boolean newValue) {
		checkBox.setSelected(newValue);
	}
	public boolean getBoolean() {
		return checkBox.isSelected();
	}
}
