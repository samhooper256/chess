package chess.base;

import java.util.ArrayList;

import chess.piecebuilder.IntInputHBox;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class Settings extends StackPane{
	
	private static Settings instance;
	private static StackPane currentlyOpenedOn;
	
	ColorAdjust darken;
	private GridPane gridPane;
	private ScrollPane scroll;
	private VBox mainVBox;
	private PresetManager presetManager;
	private HBox lowerBox;
	private Label errorMessage;
	/** Auto-flip Setting */
	private CheckBox autoFlipCheckBox;
	/** Draw by insufficient material Setting*/
	private CheckBox insufficientMaterialCheckBox;
	/** N-move rule Setting*/
	private HBox moveRuleHBox;
	private CheckBox moveRuleCheckBox;
	private TextField moveRuleTextField;
	private Label moveRuleLabel;
	private StackPane settingsStackPane;
	
	private Button managePresetsButton, changeBoardButton, saveAsPresetButton;
	
	/////////////////////
	private static volatile int moveRule;
	private static volatile boolean autoFlip, insufficientMaterial, moveRuleEnabled;
	
	public static synchronized void make() {
		if(instance == null) {
			instance = new Settings();
		}
	}
	
	private Settings() {
		super();
		this.setVisible(false);
		settingsStackPane = new StackPane();
		settingsStackPane.getStyleClass().add("settings-menu");
		gridPane = new GridPane();
		gridPane.setPickOnBounds(false);
		gridPane.setMinSize(300, 400);
		gridPane.getStyleClass().add("settings-grid-pane");
		RowConstraints row1 = new RowConstraints();
		row1.setPercentHeight(80);
		RowConstraints row2 = new RowConstraints();
		row2.setPercentHeight(20);
		gridPane.getRowConstraints().addAll(row1, row2);
		ColumnConstraints col1 = new ColumnConstraints();
		col1.setPercentWidth(100);
		gridPane.getColumnConstraints().add(col1);
		
		mainVBox = new VBox();
		mainVBox.getStyleClass().add("settings-upper-box");
		scroll = new ScrollPane(mainVBox);
		scroll.getStyleClass().add("settings-scroll-pane");
		gridPane.add(scroll, 0, 0);
		
		/* Create auto-flip setting*/
		autoFlipCheckBox = new CheckBox("Auto-flip");
		Tooltip.install(autoFlipCheckBox, new Tooltip("When enabled, the board will automatically be rotated so that the pieces of the player"
				+ " whose turn it is appears at the bottom."));
		autoFlipCheckBox.setSelected(true);
		autoFlip = true;
		
		/* Create draw by insufficient material setting*/
		insufficientMaterialCheckBox = new CheckBox("Draw by insufficient matierial");
		insufficientMaterialCheckBox.setSelected(true);
		Tooltip.install(insufficientMaterialCheckBox, new Tooltip("Determines whether the insufficient material rule is enabled."));
		insufficientMaterial = true;
		
		/* Create N-move-rule setting*/
		moveRuleCheckBox = new CheckBox();
		moveRuleCheckBox.setSelected(true);
		moveRuleTextField = new TextField("50");
		moveRuleTextField.setPrefWidth(50);
		moveRuleLabel = new Label("move rule");
		moveRuleHBox = new HBox(5, moveRuleCheckBox, moveRuleTextField, moveRuleLabel);
		moveRuleHBox.setAlignment(Pos.CENTER_LEFT);
		moveRule = 50;
		moveRuleEnabled = true;
		
		presetManager = new PresetManager();
		/*
		Button managePresetsBackButton = new Button("Back to Settings");
		managePresetsBackButton.setOnAction(actionEvent -> {
			scroll.setContent(mainVBox);
		});
		presetManager.getChildren().add(managePresetsBackButton);
		*/
		managePresetsButton = new Button("Manage Presets");
		managePresetsButton.setOnAction(actionEvent -> {
			presetManager.updatePresets();
			scroll.setContent(presetManager);
		});
		
		changeBoardButton = new Button("Change board");
		changeBoardButton.setOnAction(actionEvent -> {
			if(currentlyOpenedOn instanceof GamePanel) { //instanceof returns false for null
				((GamePanel) currentlyOpenedOn).selectNewBoard();
			}
		});
		saveAsPresetButton = new Button("Save current board as preset");
		saveAsPresetButton.setOnAction(actionEvent -> {
			if(currentlyOpenedOn instanceof GamePanel) { //instanceof returns false for null
				((GamePanel) currentlyOpenedOn).saveBoardAsPreset();
			}
		});
		mainVBox.getChildren().addAll(autoFlipCheckBox, insufficientMaterialCheckBox, moveRuleHBox, 
				managePresetsButton, changeBoardButton, saveAsPresetButton);
		
		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction(actionEvent -> this.closeWithoutApplying());
		Button applyButton = new Button("Apply");
		applyButton.setOnAction(actionEvent -> this.attemptClose());
		Button backToMainButton = new Button("Back to Main Menu");
		backToMainButton.setOnAction(actionEvent -> {
			this.closeWithoutApplying();
			MainMenu.playBackground();
			MainMenu mainMenu = MainMenu.get();
			if(Main.scene.getRoot() != mainMenu) {
				Main.scene.setRoot(mainMenu);
			}
			
		});
		errorMessage = new Label("");
		errorMessage.setWrapText(true);
		errorMessage.setTextFill(Color.RED);
		lowerBox = new HBox();
		lowerBox.getStyleClass().add("settings-lower-box");
		lowerBox.getChildren().addAll(errorMessage, backToMainButton, cancelButton, applyButton);
		gridPane.add(lowerBox, 0, 1);
		
		darken = new ColorAdjust();
		darken.setBrightness(-0.25);
		
		settingsStackPane.getChildren().add(gridPane);
		this.getChildren().add(settingsStackPane);
	}
	
	class PresetManager extends VBox{
		private Label presetHeaderLabel;
		public PresetManager() {
			super();
			presetHeaderLabel = new Label("Presets");
			presetGrid = new GridPane();
			presetGrid.getStyleClass().add("preset-grid-pane");
			presetGrid.setHgap(10);
			presetGrid.setVgap(10);
			this.getStyleClass().add("settings-upper-box");
			updatePresets();
			this.getChildren().addAll(presetHeaderLabel, presetGrid);
		}
		
		private GridPane presetGrid;
		
		public void updatePresets() {
			presetGrid.getChildren().clear();
			ArrayList<BoardPreset> presets = BoardPreset.getPresets();
			presetHeaderLabel.setText("Presets (" + (presets.size() < 100 ? presets.size() : "99+") +")");
			for(int i = 0; i < presets.size(); i++) {
				BoardPreset preset = presets.get(i);
				presetGrid.add(new Label(preset.getName()), 0, i);
				presetGrid.add(new PresetHBox(preset), 1, i);
				
			}
		}
		
		class PresetHBox extends HBox {
			private static final int SPACING = 10;
			private BoardPreset preset;
			public PresetHBox(BoardPreset pre) {
				super(SPACING);
				this.preset = pre;
				this.setAlignment(Pos.CENTER_LEFT);
				Button renameButton = new Button("Rename");
				Button renameConfirmButton = new Button("Confirm");
				Button deleteButton = new Button("Delete");
				renameButton.setOnAction(actionEvent -> {
					ObservableList<Node> children = PresetHBox.this.getChildren();
					children.clear();
					children.add(renameConfirmButton);
					int rowIndex = GridPane.getRowIndex(PresetHBox.this);
					removeNodeFromGridPane(presetGrid, 0, rowIndex);
					presetGrid.add(new TextField(), 0, rowIndex);
				});
				renameConfirmButton.setOnAction(actionEvent -> {
					ObservableList<Node> children = PresetHBox.this.getChildren();
					int rowIndex = GridPane.getRowIndex(PresetHBox.this);
					TextField tf = (TextField) getNodeFromGridPane(presetGrid, 0, rowIndex);
					String newPresetName = tf.getText();
					preset.setName(newPresetName);
					if(newPresetName == null || newPresetName.isBlank()) {
						children.add(new Label("Must not be empty."));
					}
					else {
						children.clear();
						children.addAll(renameButton, deleteButton);
						presetGrid.getChildren().remove(tf);
						presetGrid.add(new Label(newPresetName), 0, rowIndex);
					}
				});
				deleteButton.setOnAction(actionEvent ->{
					int rowIndex = GridPane.getRowIndex(PresetHBox.this);
					ObservableList<Node> gridChildren = presetGrid.getChildren();
					final int presetGridIndex = gridChildren.indexOf(PresetHBox.this);
					removeNodeFromGridPane(presetGrid, 0, rowIndex);
					removeNodeFromGridPane(presetGrid, 1, rowIndex);
					
					System.out.println("PresetHBox.this = " + PresetHBox.this);
					System.out.println("gridChildren = " + gridChildren);
					
					System.out.println("presetGridIndex = " + presetGridIndex);
					for(int i = presetGridIndex - 1; i < gridChildren.size(); i++) {
						Node child = gridChildren.get(i);
						int childRow = GridPane.getRowIndex(child);
						if(childRow > rowIndex) {
							GridPane.setConstraints(child, GridPane.getColumnIndex(child), childRow - 1);
						}
					}
					
					BoardPreset.deletePreset(preset);
				});
				this.getChildren().addAll(renameButton, deleteButton);
			}
		}
		
		private void removeNodeFromGridPane(GridPane gridPane, int col, int row) {
			ObservableList<Node> children = gridPane.getChildren();
			for (Node node : children) {
		        if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
		            children.remove(node);
		            return;
		        }
		    }
		}
		private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
		    for (Node node : gridPane.getChildren()) {
		        if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
		            return node;
		        }
		    }
		    return null;
		}
	}
	
	private boolean applySettings() {
		if(moveRuleCheckBox.isSelected()) {
			if(!IntInputHBox.isInteger(moveRuleTextField.getText().strip(), 10)){
				errorMessage.setText("You must enter a positive integer for move rule if it is enabled.");
				return false;
			}
		}
		
		if(moveRuleCheckBox.isSelected()) {
			moveRule = Integer.parseInt(moveRuleTextField.getText().strip()); //make sure to call strip
			moveRuleTextField.setText(String.valueOf(moveRule));
		}
		moveRuleEnabled = moveRuleCheckBox.isSelected();
		autoFlip = autoFlipCheckBox.isSelected();
		insufficientMaterial = insufficientMaterialCheckBox.isSelected();
		return true;
	}
	
	
	
	private void keepSettings() {
		autoFlipCheckBox.setSelected(autoFlip);
		insufficientMaterialCheckBox.setSelected(insufficientMaterial);
		moveRuleCheckBox.setSelected(moveRuleEnabled);
		moveRuleTextField.setText(String.valueOf(moveRule));
	}
	
	public static void openOn(StackPane stackPane) {
		openOn(stackPane, true, 0.5, 0.5);
	}
	
	public static void openOn(StackPane stackPane, boolean modal, double widthPercent, double heightPercent) {
		if(instance == null) {
			instance = new Settings();
		}
		instance.setPickOnBounds(modal);
		instance.setVisible(true);
		currentlyOpenedOn = stackPane;
		instance.settingsStackPane.maxWidthProperty().bind(instance.widthProperty().multiply(widthPercent));
		instance.settingsStackPane.maxHeightProperty().bind(instance.heightProperty().multiply(heightPercent));
		if(stackPane instanceof GamePanel) {
			instance.changeBoardButton.setDisable(false);
			instance.saveAsPresetButton.setDisable(false);
		}
		else {
			instance.changeBoardButton.setDisable(true);
			instance.saveAsPresetButton.setDisable(true);
		}
		instance.scroll.setContent(instance.mainVBox);
		stackPane.getChildren().add(instance);
	}
	
	private void attemptClose() {
		boolean apply = this.applySettings();
		if(apply) {
			close0();
		}
	}
	
	public void closeWithoutApplying() {
		this.keepSettings();
		close0();
	}
	
	private void close0(){
		errorMessage.setText("");
		currentlyOpenedOn.getChildren().remove(this);
		currentlyOpenedOn = null;
		this.setVisible(false);
		this.setPickOnBounds(false);
	}
	
	public static boolean getAutoFlip() {
		return autoFlip;
	}
	
	public static boolean getInsufficientMaterial() {
		return insufficientMaterial;
	}
	
	public static boolean moveRuleEnabled() {
		return moveRuleEnabled;
	}
	
	public static int getMoveRule() {
		return moveRule;
	}
}
