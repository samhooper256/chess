package chess.piecebuilder;

import chess.util.InputVerification;
import chess.util.IntTextField;
import chess.util.IntegerPath;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class IntegerLiteralConditionOption extends ConditionOption implements InputVerification{
	private IntTextField followingIntTextField;
	protected IntegerLiteralConditionOption(ConditionChoiceBox choiceBox) {
		super(choiceBox);
	}
	
	@Override
	public String toString() {
		return "number";
	}
	
	@Override
	public void updatePane() {
		Pane pb = super.choiceBox.nodeToAddTo;
		ObservableList<Node> children = pb.getChildren();
		int myIndex = children.indexOf(super.choiceBox);
		ConditionBox.clearPast(children, myIndex);
		children.add(followingIntTextField = new IntTextField());
	}
	
	/* *
	 * TextField should be verified before this is called.
	 */
	public int getIntValue() {
		if(followingIntTextField == null) {
			throw new NullPointerException();
		}
		else {
			return followingIntTextField.getInt();
		}
	}
	
	public IntegerPath getIntegerPath() {
		return new IntegerPath(getIntValue());
	}

	@Override
	public boolean verifyInput() {
		//System.out.println("verifying input of IntegerLiteralConditionOption...");
		if(followingIntTextField == null) {
			//System.out.println("Following inttextfield was null");
			return false;
		}
		boolean result = followingIntTextField.verifyInput();
		//System.out.println("result = " + result);
		return result;
	}
}
