package chess.piecebuilder;

import chess.util.InputVerification;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class IntInputHBox extends HBox implements InputVerification{
	private static final int SPACING = 5;
	private TextField textField;
	private ErrorSubmitable submitErrorsTo;
	public IntInputHBox(String parameterName, ErrorSubmitable es) {
		super(SPACING);
		this.submitErrorsTo = es;
		this.setAlignment(Pos.CENTER_LEFT);
		this.getChildren().add(new Label(String.format("%s (integer): ", parameterName)));
		textField = new TextField();
		this.getChildren().add(textField);
	}
	
	public boolean verifyInput() {
		//System.out.println("Verifying int input");
		boolean result = isInteger(textField.getText().strip(), 10);
		if(!result) {
			submitErrorsTo.submitErrorMessage(((Label) this.getChildren().get(0)).getText() + " is invalid");
		}
		return result;
	}
	
	//isValid should be called right before this to avoid an exception
	public int getInt() {
		return Integer.parseInt(textField.getText().strip());
	}
	
	private static boolean isInteger(String s, int radix) {
	    if(s.isEmpty()) return false;
	    for(int i = 0; i < s.length(); i++) {
	        if(i == 0 && s.charAt(i) == '-') {
	            if(s.length() == 1) return false;
	            else continue;
	        }
	        if(Character.digit(s.charAt(i),radix) < 0) return false;
	    }
	    return true;
	}
}
