package chess.piecebuilder;

import javafx.scene.layout.VBox;

public class ESVBox extends VBox implements ErrorSubmitable{
	private ErrorSubmitable errorSubmitable;
	public ESVBox(ErrorSubmitable es) {
		super();
		this.errorSubmitable = es;
	}
	
	public ESVBox(ErrorSubmitable es, int spacing) {
		super(spacing);
		this.errorSubmitable = es;
	}
	
	@Override
	public void submitErrorMessage(String message) {
		errorSubmitable.submitErrorMessage(message);
	}

}
