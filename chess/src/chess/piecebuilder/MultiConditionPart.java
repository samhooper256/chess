package chess.piecebuilder;

import chess.util.Condition;
import chess.util.InputVerification;
import javafx.scene.layout.Pane;

public interface MultiConditionPart extends Buildable<Condition>, ErrorSubmitable, InputVerification{
	public Pane getNodeToAddTo();
	public void setNodeToAddTo(Pane node);
	public ConditionBoxWrap getWrap();
}
