package chess.util;

import javafx.scene.control.TextField;

public class IntTextField extends TextField implements InputVerification{
	private int radix;
	
	public IntTextField() {
		super();
		radix = 10;
	}
	
	private static boolean isInteger(String s, int theRadix) {
	    if(s.isEmpty()) return false;
	    for(int i = 0; i < s.length(); i++) {
	        if(i == 0 && s.charAt(i) == '-') {
	            if(s.length() == 1) return false;
	            else continue;
	        }
	        if(Character.digit(s.charAt(i),theRadix) < 0) return false;
	    }
	    return true;
	}
	
	@Override
	public boolean verifyInput() {
		return isInteger(getText().strip(), radix);
	}
	
	public int getInt() {
		return Integer.parseInt(getText().strip());
	}
}
