package chess.base;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PresetCreation extends Stage{
	private static PresetCreation instance;
	public static synchronized void make() {
		if(instance == null) {
			instance = new PresetCreation();
		}
	}
	
	private static Board currentBoard;
	
	private PresetCreation() {
		super();
		this.initModality(Modality.APPLICATION_MODAL);
		this.initStyle(StageStyle.UNDECORATED);
		
		Label nameLabel = new Label("Enter preset name:");
		TextField nameInputField = new TextField();
		HBox topHBox = new HBox(5, nameLabel, nameInputField);
		topHBox.setAlignment(Pos.CENTER);
		Label startingTurnLabel = new Label("Starting turn:");
		RadioButton whiteRadio = new RadioButton("White");
		RadioButton blackRadio = new RadioButton("Black");
		ToggleGroup tg = new ToggleGroup();
		whiteRadio.setToggleGroup(tg);
		blackRadio.setToggleGroup(tg);
		tg.selectToggle(whiteRadio);
		
		HBox bottomHBox = new HBox(5, startingTurnLabel, whiteRadio, blackRadio);
		bottomHBox.setAlignment(Pos.CENTER);
		Button createButton = new Button("Create");
		Label errorMessage = new Label("");
		createButton.setOnAction(actionEvent -> {
			String potentialName = nameInputField.getText();
			if(potentialName == null || potentialName.isBlank()) {
				errorMessage.setText("Name must not be empty or blank");
			}
			else if(potentialName.length() > 32) {
				errorMessage.setText("Name must be between 1 and 32 characters.");
				return;
			}
			else {
				BoardPreset.saveBoardPreset(potentialName, whiteRadio.isSelected(), currentBoard);
				close();
			}
		});
		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction(actionEvent -> {
			close();
		});
		
		HBox buttonBox = new HBox(20, cancelButton, createButton);
		
		VBox vBox = new VBox(topHBox, bottomHBox, buttonBox, errorMessage);
		vBox.setAlignment(Pos.CENTER);
		Scene scene = new Scene(vBox);
		scene.getStylesheets().add(PresetCreation.class.getResource("popupstyle.css").toExternalForm());
		this.setScene(scene);
	}
	
	public static void createOn(Board b) {
		currentBoard = b;
		instance.show();
	}
}
