package chess.piecebuilder;

import chess.util.Condition;
import javafx.scene.layout.Pane;

public interface MultiConditionPart extends Buildable<Condition>, ErrorSubmitable{
	public Pane getNodeToAddTo();
	public void setNodeToAddTo(Pane node);
}
