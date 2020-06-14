package chess.base;

import chess.piecebuilder.IntInputHBox;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

class BoardSelect extends Stage{
	private volatile Object result;
	
	private static BoardSelect instance;
	
	public static synchronized void make() {
		if(instance == null) {
			instance = new BoardSelect();
		}
	}
	
	Label errorMessage;
	
	private ChoiceBox<BoardPreset> presetChoiceBox;
	private BoardSelect() {
		super();
		this.initStyle(StageStyle.UNDECORATED);
		this.initModality(Modality.APPLICATION_MODAL);
		IntInputHBox intBox = new IntInputHBox("Enter a board size:");
		Label orLabel = new Label("or");
		Label presetLabel = new Label("Select Preset:");
		Button selectButton = new Button("Select");
		Button cancelButton = new Button("Cancel");
		HBox buttonBox = new HBox(20, cancelButton, selectButton);
		errorMessage = new Label();
		presetChoiceBox = new ChoiceBox<>();
		presetChoiceBox.getItems().addAll(BoardPreset.getPresets());
		HBox bottomHBox = new HBox(IntInputHBox.SPACING, presetLabel, presetChoiceBox);
		cancelButton.setOnAction(actionEvent -> {
			result = null;
			BoardSelect.this.close();
		});
		selectButton.setOnAction(actionEvent -> {
			if(!presetChoiceBox.getSelectionModel().isEmpty()) {
				result = presetChoiceBox.getValue();
				BoardSelect.this.close();
			}
			else if(intBox.verifyInput()) {
				int num = intBox.getInt();
				if(num < Board.MIN_BOARD_SIZE || num > Board.MAX_BOARD_SIZE) {
					errorMessage.setText("Size must be between " + Board.MIN_BOARD_SIZE + " and " +
							Board.MAX_BOARD_SIZE);
				}
				else {
					result = Integer.valueOf(intBox.getInt());
					BoardSelect.this.close();
				}
			}
			else {
				errorMessage.setText("Invalid number");
			}
			
		});
		
		VBox vBox = new VBox(intBox, orLabel, bottomHBox, buttonBox, errorMessage);
		Scene scene = new Scene(vBox);
		scene.getStylesheets().add(BoardSelect.class.getResource(Main.RESOURCES_PREFIX + "popupstyle.css").toExternalForm());
		this.setScene(scene);
		this.sizeToScene();
	}
	
	public Object getResult() {
		return result;
	}
	
	private void updatePresetChoiceBox() {
		presetChoiceBox.getItems().clear();
		presetChoiceBox.getItems().addAll(BoardPreset.getPresets());
	}
	
	/**
	 * Returns either an Integer representing the new empty board size or a board preset.
	 * BLOCKS until it is done
	 */
	public static Object getBoardSelection() {
		instance.result = null;
		instance.updatePresetChoiceBox();
		instance.errorMessage.setText("");
		instance.showAndWait();
		return instance.result;
	}
}
