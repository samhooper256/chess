package chess.piecebuilder;

import javafx.scene.layout.FlowPane;

public class ESFlow extends FlowPane implements ErrorSubmitable{
	private ErrorSubmitable errorSubmitable;
	public ESFlow(ErrorSubmitable es) {
		super();
		this.errorSubmitable = es;
	}
	
	@Override
	public void submitErrorMessage(String message) {
		errorSubmitable.submitErrorMessage(message);
	}

}
