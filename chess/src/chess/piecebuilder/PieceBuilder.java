package chess.piecebuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import chess.base.CustomPiece;
import chess.base.GamePanel;
import chess.base.Main;
import chess.base.Piece;
import chess.base.PieceData;
import chess.base.WrappedImageView;
import chess.util.InputVerification;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PieceBuilder extends Stage implements InputVerification{
	public static final Image WHITE_DEFAULT_IMAGE, BLACK_DEFAULT_IMAGE, ERROR_LOADING_IMAGE;
	public static final String WHITE_DEFAULT_URI, BLACK_DEFAULT_URI;
	public static final int IMAGE_SIZE = 240;
	public static final Image RELATIVE, LINE, RELATIVE_LINE, RELATIVE_SEGMENT, RADIUS, ON_START;
	
	static {
		WHITE_DEFAULT_IMAGE = new Image(PieceBuilder.class.getResourceAsStream(WHITE_DEFAULT_URI = (Main.RESOURCES_PREFIX + "white_default_image.png")),
				IMAGE_SIZE, IMAGE_SIZE, false, true);
		BLACK_DEFAULT_IMAGE = new Image(PieceBuilder.class.getResourceAsStream(BLACK_DEFAULT_URI = (Main.RESOURCES_PREFIX + "black_default_image.png")),
				IMAGE_SIZE, IMAGE_SIZE, false, true);
		ERROR_LOADING_IMAGE = new Image(PieceBuilder.class.getResourceAsStream(Main.RESOURCES_PREFIX + "errorloading.png"),
				IMAGE_SIZE, IMAGE_SIZE, false, true);
		RELATIVE = new Image(PieceBuilder.class.getResourceAsStream(Main.RESOURCES_PREFIX + "relative_icon.png"), 16, 16, false, true);
		LINE = new Image(PieceBuilder.class.getResourceAsStream(Main.RESOURCES_PREFIX + "line_icon.png"), 16, 16, false, true);
		RELATIVE_LINE = new Image(PieceBuilder.class.getResourceAsStream(Main.RESOURCES_PREFIX + "relative_line_icon.png"), 16, 16, false, true);
		RELATIVE_SEGMENT = new Image(PieceBuilder.class.getResourceAsStream(Main.RESOURCES_PREFIX + "relative_segment_icon.png"), 16, 16, false, true);
		RADIUS = new Image(PieceBuilder.class.getResourceAsStream(Main.RESOURCES_PREFIX + "radius_icon.png"), 16, 16, false, true);
		ON_START = new Image(PieceBuilder.class.getResourceAsStream(Main.RESOURCES_PREFIX + "on_start_icon.png"), 16, 16, false, true);
		try {
			new File("userpieces").createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static PieceBuilder instance;
	
	/**
	 * Creates a single PieceBuilder instance and returns it.
	 * If a PieceBuilder instance has already been created, this method throws an
	 * UnsupportedOperationException as only one instance of this class should exist
	 * at any time.
	 */
	public static PieceBuilder make() {
		if(instance == null) {
			return instance = new PieceBuilder();
		}
		else {
			throw new UnsupportedOperationException("A PieceBuilder instance already exists");
		}
	}
	
	/**
	 * Returns the existing PieceBuilder instance, or creates one and returns it
	 * if it does not exist. Does not throw any exceptions.
	 */
	public PieceBuilder makeOrGet() {
		if(instance == null) {
			return make();
		}
		else {
			return instance;
		}
	}
	
	/**
	 * Returns the PieceBuilder instance, throwing a NullPointerException if it has
	 * not been created yet.
	 */
	public PieceBuilder getInstance() {
		if(instance == null) {
			throw new NullPointerException("The instance does not exist.");
		}
		else {
			return instance;
		}
	}
	
	public static void setGamePanel(GamePanel gp) {
		associatedGamePanel = gp;
	}
	
	public static void submitError(String message) {
		instance.submitErrorMessage(message);
	}
	
	public static void clearErrors() {
		instance.clearErrors0();
	}
	
	public static Collection<String> currentPieceNames(){
		return instance.currentPieceNames0();
	}
	
	private Scene scene;
	private StackPane outerStackPane, whiteImageOuter, blackImageOuter, whiteImageInternal, blackImageInternal;
	private VBox outermostVBox, leftVBox;
	GridPane leftImageVBox;
	private GridPane gridPane;
	private TextField nameTextField;
	private Label nameLabel;
	private HBox nameHBox;
	private Button createPieceButton, hideErrors;
	private ImageView whiteImageView, blackImageView;
	private Image whiteImage, blackImage;
	private ActionTreeBuilder actionTreeBuilder;
	private VBox errorVBox;
	private boolean errorsShowing;
	private DoubleProperty errorFontSize;
	private StringExpression errorFontStringExpression;
	private static Collection<String> currentPieceNames;
	private static Collection<String> currentCustomPieceNames;
	private FileChooser fileChooser;
	private AnchorPane whiteXAnchor, blackXAnchor;
	private Label whiteX, blackX;
	private String whiteImageURIString, blackImageURIString;
	private PieceData currentData;
	private SuccessPopup successPopup;
	private static GamePanel associatedGamePanel;
	
	private PieceBuilder() {
		super();
		associatedGamePanel = null;
		currentData = null;
		whiteImageURIString = null;
		blackImageURIString = null;
		successPopup = new SuccessPopup();
		outermostVBox = new VBox();
		outermostVBox.setFillWidth(true);
		outerStackPane = new StackPane();
		VBox.setVgrow(outerStackPane, Priority.ALWAYS);
		currentPieceNames = new ArrayList<>();
		currentCustomPieceNames = new ArrayList<>();
		updatePieceNames();
		setupMenuBar();
		outermostVBox.getChildren().addAll(menuBar, outerStackPane);
		scene = new Scene(outermostVBox, 600, 400);
		scene.getStylesheets().addAll(PieceBuilder.class.getResource(Main.RESOURCES_PREFIX + "style.css").toExternalForm(),
				PieceBuilder.class.getResource(Main.RESOURCES_PREFIX + "piecebuilderstyle.css").toExternalForm());
		
		gridPane = new GridPane();
		RowConstraints row1 = new RowConstraints();
		row1.setPercentHeight(100);
		gridPane.getRowConstraints().add(row1);
		
		ColumnConstraints col1 = new ColumnConstraints();
		col1.setPercentWidth(25);
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setPercentWidth(75);
		gridPane.getColumnConstraints().addAll(col1,col2);
		gridPane.setGridLinesVisible(true);
		
		outerStackPane.getChildren().add(gridPane);
		
		//Make left part
		leftVBox = new VBox(10);
		leftVBox.setFillWidth(true);
		gridPane.add(leftVBox, 0, 0);
		
		nameTextField = new TextField();
		nameTextField.prefWidthProperty().bind(leftVBox.widthProperty().divide(2));
		nameLabel = new Label("Name: ");
		nameHBox = new HBox(10, nameLabel, nameTextField);
		nameHBox.setAlignment(Pos.CENTER);
		
		leftImageVBox = new GridPane();
		leftImageVBox.setVgap(10);
		final RowConstraints rc1 = new RowConstraints();
		rc1.setPercentHeight(50);
		
		final RowConstraints rc2 = new RowConstraints();
		rc2.setPercentHeight(50);
		
		leftImageVBox.getRowConstraints().addAll(rc1,rc2);
		
		final ColumnConstraints cc1 = new ColumnConstraints();
		cc1.setPercentWidth(100);
		leftImageVBox.getColumnConstraints().add(cc1);
		
		
		whiteImageOuter = new StackPane();
		whiteImageOuter.maxWidthProperty().bind(leftImageVBox.widthProperty());
		
		whiteImageInternal = new StackPane();
		whiteImageInternal.getStyleClass().add("insert-image-box");
		whiteXAnchor = new AnchorPane(); 
		whiteX = new Label("X");
		whiteX.getStyleClass().add("image-x");
		AnchorPane.setRightAnchor(whiteX, 6d);
		AnchorPane.setTopAnchor(whiteX, 6d);
		whiteXAnchor.getChildren().add(whiteX);
		whiteXAnchor.setPickOnBounds(false);
		whiteXAnchor.setVisible(false);
		whiteImage = WHITE_DEFAULT_IMAGE;
		whiteImageView = new WrappedImageView(whiteImage, 0, 0);
		whiteImageView.setPreserveRatio(true);
		whiteImageInternal.getChildren().addAll(whiteImageView, whiteXAnchor);
		whiteImageInternal.maxWidthProperty().bind(whiteImageInternal.heightProperty());
		whiteImageOuter.getChildren().add(whiteImageInternal);
		whiteImageInternal.maxHeightProperty().bind(whiteImageOuter.widthProperty());
		
		blackImageOuter = new StackPane();
		blackImageInternal = new StackPane();
		blackImageInternal.getStyleClass().add("insert-image-box");
		
		blackXAnchor = new AnchorPane(); 
		blackX = new Label("X");
		blackX.getStyleClass().add("image-x");
		AnchorPane.setRightAnchor(blackX, 6d);
		AnchorPane.setTopAnchor(blackX, 6d);
		blackXAnchor.getChildren().add(blackX);
		blackXAnchor.setPickOnBounds(false);
		blackXAnchor.setVisible(false);
		blackImage = BLACK_DEFAULT_IMAGE;
		blackImageView = new WrappedImageView(blackImage, 0, 0);
		blackImageView.setPreserveRatio(true);
		blackImageInternal.getChildren().addAll(blackImageView, blackXAnchor);
		blackImageInternal.maxWidthProperty().bind(blackImageInternal.heightProperty());
		blackImageOuter.getChildren().add(blackImageInternal);
		blackImageInternal.maxHeightProperty().bind(blackImageOuter.widthProperty());
		
		whiteX.setOnMouseClicked(mouseEvent -> {
			clearWhiteImage();
			mouseEvent.consume();
			gridPane.requestLayout();
		});
		whiteImageInternal.setOnMouseClicked(mouseEvent -> {
			fileChooser.setTitle("Select White Piece Image");
			File selectedFile = fileChooser.showOpenDialog(PieceBuilder.this);
			if(selectedFile != null) {
				setCustomWhiteImage(selectedFile);
			}
		});
		whiteImageInternal.setOnDragOver(dragEvent -> {
			if (dragEvent.getGestureSource() != whiteImageInternal
                    && dragEvent.getDragboard().hasFiles()) {
                /* allow for both copying and moving, whatever user chooses */
				dragEvent.acceptTransferModes(TransferMode.COPY);
            }
			dragEvent.consume();
		});
		whiteImageInternal.setOnDragDropped(dragEvent -> {
			Dragboard db = dragEvent.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
				if(files.size() == 1) {
					File file = files.get(0);
					if(isValidImage(file)) {
						try {
							setCustomWhiteImage(file);
							success = true;
						}
						catch(Exception e) {
							//TODO display error messsage about the image???
						}
						
					}
				}
            }
            dragEvent.setDropCompleted(success);

            dragEvent.consume();
		});
		blackX.setOnMouseClicked(mouseEvent -> {
			clearBlackImage();
			mouseEvent.consume();
			gridPane.requestLayout();
		});
		blackImageInternal.setOnMouseClicked(mouseEvent -> {
			fileChooser.setTitle("Select Black Piece Image");
			File selectedFile = fileChooser.showOpenDialog(PieceBuilder.this);
			if(selectedFile != null) {
				setCustomBlackImage(selectedFile);
			}
		});
		blackImageInternal.setOnDragOver(dragEvent -> {
			if (dragEvent.getGestureSource() != blackImageInternal
                    && dragEvent.getDragboard().hasFiles()) {
				dragEvent.acceptTransferModes(TransferMode.COPY);
            }
			dragEvent.consume();
		});
		blackImageInternal.setOnDragDropped(dragEvent -> {
			Dragboard db = dragEvent.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
				if(files.size() == 1) {
					File file = files.get(0);
					if(isValidImage(file)) {
						try {
							setCustomBlackImage(file);
							success = true;
						}
						catch(Exception e) {
							//TODO display error messsage about the image???
						}
					}
				}
            }
            dragEvent.setDropCompleted(success);

            dragEvent.consume();
		});
		
		leftImageVBox.setAlignment(Pos.CENTER);
		leftImageVBox.add(whiteImageOuter, 0, 0);
		leftImageVBox.add(blackImageOuter, 0, 1);
		
		fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("All Images", "*.*"),
            new FileChooser.ExtensionFilter("JPG", "*.jpg"),
            new FileChooser.ExtensionFilter("PNG", "*.png"),
            new FileChooser.ExtensionFilter("BMP", "*.bmp"),
            new FileChooser.ExtensionFilter("GIF", "*.gif")
        );
		
		createPieceButton = new Button("Create Piece");
		createPieceButton.setMaxWidth(Double.MAX_VALUE);
		//createPieceButton.setWrapText(true);
		createPieceButton.setId("create-piece-button");
		createPieceButton.setOnMouseClicked(mouseEvent -> attemptFinish());
		
		VBox.setVgrow(leftImageVBox, Priority.ALWAYS);
		
		leftVBox.setPadding(new Insets(10));
		leftVBox.getChildren().addAll(nameHBox, leftImageVBox, createPieceButton);
		///////////////////////////////
		//Make right part
		actionTreeBuilder = new ActionTreeBuilder();
		gridPane.add(actionTreeBuilder, 1, 0);
		//actionTreeBuilder.setBlank();
		
		///////////////////////////////
		//Make error box
		errorVBox = new VBox();
		errorVBox.setPickOnBounds(false);
		errorVBox.setAlignment(Pos.BOTTOM_RIGHT);
		errorVBox.setMouseTransparent(true);
		hideErrors = new Button("Hide errors");
		hideErrors.prefHeightProperty().bind(outerStackPane.heightProperty().divide(12));
		hideErrors.prefWidthProperty().bind(outerStackPane.widthProperty().divide(6));
		hideErrors.setOnMouseClicked(value -> {
			clearErrors();
		});
		outerStackPane.getChildren().add(errorVBox);
		errorFontSize = new SimpleDoubleProperty(12);
		errorFontSize.bind((outermostVBox.widthProperty().add(outermostVBox.heightProperty())).divide(75));
		errorFontStringExpression = Bindings.concat("-fx-font-size: ", errorFontSize.asString(), "; -fx-text-fill: #ff0000;");
		errorsShowing = false;
		//////////////////////////////
		this.setTitle("chess++ Piece Builder");
		this.setScene(scene);
		this.sizeToScene();
		//this.setMinHeight(400);
		//this.minWidthProperty().bind(this.heightProperty().multiply(1.5));
		this.initModality(Modality.APPLICATION_MODAL);
		this.setOnCloseRequest(windowEvent -> {
			attemptClose();
			windowEvent.consume();
		});
	}
	
	private MenuBar menuBar;
	private Menu optionsMenu;
	private MenuItem newPieceMenuItem, editPieceMenuItem, deletePieceMenuItem;
	private PiecePopup editPiecePopup, deletePiecePopup;
	private ConfirmDeletionPopup deleteConfirmation;
	
	private void setupMenuBar() {
		newPieceMenuItem = new MenuItem("Create New Piece");
		newPieceMenuItem.setOnAction(actionEvent -> {
			PieceBuilder.reset(false); //TODO Make a "You have unsaved changes" popup here
		});
		editPiecePopup = new PiecePopup("Select a piece to edit", "Edit", actionEvent -> {
			editPiecePopup.hide();
			PieceBuilder.open(Piece.getDataFor(editPiecePopup.getCurrentlySelectedOption().getPieceName()));
		});
		editPieceMenuItem = new MenuItem("Edit Existing Piece");
		editPieceMenuItem.setOnAction(actionEvent -> {
			editPiecePopup.reloadOptions();
			editPiecePopup.show();
		});
		deletePiecePopup = new PiecePopup("Select a piece to delete", "Delete", actionEvent -> {
			deletePiecePopup.hide();
			PieceBuilder.attemptDelete(deletePiecePopup.getCurrentlySelectedOption().getPieceName());
		});
		deletePieceMenuItem = new MenuItem("Delete Existing Piece");
		deletePieceMenuItem.setOnAction(actionEvent -> {
			deletePiecePopup.reloadOptions();
			deletePiecePopup.show();
		});
		deleteConfirmation = new ConfirmDeletionPopup();
		
		optionsMenu = new Menu("Options");
		optionsMenu.getItems().addAll(newPieceMenuItem, editPieceMenuItem, deletePieceMenuItem);
		
		menuBar = new MenuBar(optionsMenu);
	}
	
	private static void updatePieceNames() {
		currentCustomPieceNames = CustomPiece.getDefinedPieceNames();
		currentPieceNames.clear();
		currentPieceNames.addAll(Piece.predefinedPieceNames);
		currentPieceNames.addAll(currentCustomPieceNames);
		System.out.println("PieceNames updated, now\n\ttotal="+currentPieceNames+"\n\t"
				+ "custom="+currentCustomPieceNames);
	}

	private class PiecePopup extends Stage{
		private TilePane tilePane;
		private Button cancelButton, selectButton;
		private PieceOption currentlySelectedOption;
		private final EventHandler<? super MouseEvent> pieceOptionClickAction = mouseEvent -> {
			PieceOption source = (PieceOption) mouseEvent.getSource();
			if(PiecePopup.this.currentlySelectedOption == source) {
				source.setSelected(false);
				PiecePopup.this.currentlySelectedOption = null;
				PiecePopup.this.selectButton.setDisable(true);
				mouseEvent.consume();
				return;
			}
			if(currentlySelectedOption != null) {
				currentlySelectedOption.setSelected(false);
			}
			PiecePopup.this.currentlySelectedOption = source;
			PiecePopup.this.currentlySelectedOption.setSelected(true);
			selectButton.setDisable(false);
			
			mouseEvent.consume();
			
		};
		
		public PiecePopup(String title, String selectText, EventHandler<ActionEvent> selectEvent) {
			super();
			StackPane content = new StackPane();
			this.initModality(Modality.APPLICATION_MODAL);
			this.initOwner(PieceBuilder.this);
			this.initStyle(StageStyle.UNDECORATED);
			this.setScene(new Scene(content));
			tilePane = new TilePane();
			tilePane.setHgap(5);
			tilePane.setVgap(5);
			for(String s : currentCustomPieceNames) {
				tilePane.getChildren().add(new PieceOption(s));
			}
			VBox vBox = new VBox(10);
			vBox.setPadding(new Insets(10));
			vBox.setFillWidth(true);
			
			Label titleLabel = new Label(title);
			ScrollPane scrollPane = new ScrollPane(tilePane);
			
			HBox buttonsBox = new HBox(20);
			buttonsBox.setFillHeight(true);
			cancelButton = new Button("Cancel");
			cancelButton.setOnAction(actionEvent -> {
				PiecePopup.this.hide();
			});
			selectButton = new Button(selectText);
			selectButton.setOnAction(selectEvent);
			selectButton.setDisable(true);
			buttonsBox.getChildren().addAll(cancelButton, selectButton);
			buttonsBox.setAlignment(Pos.CENTER);
			vBox.getChildren().addAll(titleLabel, scrollPane, buttonsBox);
			content.getChildren().add(vBox);
			content.setStyle("-fx-background-color: white;");
			this.sizeToScene();
		}
		
		public PieceOption getCurrentlySelectedOption() {
			return currentlySelectedOption;
		}
		
		public void reloadOptions() {
			tilePane.getChildren().clear();
			for(String s : currentCustomPieceNames) {
				tilePane.getChildren().add(new PieceOption(s));
			}
		}
		
		private class PieceOption extends StackPane {
			private final String pieceName;
			private boolean isSelected;
			public PieceOption(String pieceName) {
				super();
				isSelected = false;
				this.pieceName = pieceName;
				this.getChildren().add(new Label(pieceName));
				this.setOnMouseClicked(pieceOptionClickAction);
			}
			
			public String getPieceName() {
				return pieceName;
			}
			
			public void setSelected(boolean newSelected) {
				isSelected = newSelected;
				if(isSelected) {
					PieceOption.this.setStyle("-fx-background-color: lightblue;");
				}
				else {
					PieceOption.this.setStyle("-fx-background-color: inherit;");
				}
			}
		}
	}
	
	private boolean isValidImage(File f) {
		if(!f.exists()) return false;
		String path = f.getPath();
		int dotIndex = path.lastIndexOf('.');
		String extension = path.substring(dotIndex + 1);
		if(extension.equalsIgnoreCase("png") ||
			extension.equalsIgnoreCase("jpg") ||
			extension.equalsIgnoreCase("jpeg") ||
			extension.equalsIgnoreCase("bmp") ||
			extension.equalsIgnoreCase("gif")) {
			return true;
		}
		else {
			return false;
		}
	}
	private void setCustomWhiteImage(File imageFile) {
		whiteImage = new Image(whiteImageURIString = imageFile.toURI().toString(), 240, 240, false, true);
		whiteImageView.setImage(whiteImage);
		whiteXAnchor.setVisible(true);
	}
	private void setCustomBlackImage(File imageFile) {
		blackImage = new Image(blackImageURIString = imageFile.toURI().toString(), 240, 240, false, true);
		blackImageView.setImage(blackImage);
		blackXAnchor.setVisible(true);
	}
	
	private void clearWhiteImage() {
		if(whiteImage != WHITE_DEFAULT_IMAGE) {
			whiteImage = WHITE_DEFAULT_IMAGE;
			whiteImageView.setImage(whiteImage);
			whiteXAnchor.setVisible(false);
		}
	}
	
	private void clearBlackImage() {
		if(blackImage != BLACK_DEFAULT_IMAGE) {
			blackImage = BLACK_DEFAULT_IMAGE;
			blackImageView.setImage(blackImage);
			blackXAnchor.setVisible(false);
		}
	}
	
	private void attemptFinish() {
		System.out.println("Attempting finish, currentData = " + currentData);
		if(currentData == null) {
			attemptCreate();
		}
		else {
			attemptSave();
		}
	}
	
	private void attemptSave() {
		clearErrors();
		if(!verifyInput()) {
			return;
		}
		currentData.setWhiteImageURIString(whiteImageURIString);
		currentData.setBlackImageURIString(blackImageURIString);
		currentData.setTree(actionTreeBuilder.build());
		currentData.setPointValue(5);
		
		File file = new File(Main.PIECES_FOLDER, currentData.getName() + ".dat");
		boolean error = false;
		try {
			FileWriter temp = new FileWriter(file, false);
			temp.flush();
			temp.close();
			FileOutputStream fos = new FileOutputStream(file); 
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(currentData);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			error = true;
			e.printStackTrace();
		}
		if(!error) {
			CustomPiece.updatePieceData(currentData);
			successPopup.setMessage("\"" + currentData.getName() + "\" saved successfully.");
			successPopup.show();
		}
		
		
	}
	
	private void attemptCreate() {
		clearErrors();
		if(!verifyInput()) {
			return;
		}
		if(whiteImage == WHITE_DEFAULT_IMAGE) {
			//TODO warning message about using the default image
		}
		if(blackImage == BLACK_DEFAULT_IMAGE) {
			//TODO warning message about using the default image
		}
		PieceData pieceData = new PieceData(nameTextField.getText().strip());
		pieceData.setWhiteImageURIString(whiteImageURIString);
		pieceData.setBlackImageURIString(blackImageURIString);
		pieceData.setTree(actionTreeBuilder.build());
		pieceData.setPointValue(5);
		
		File file = new File(Main.PIECES_FOLDER, pieceData.getName() + ".dat");
		boolean error = false;
		try {
			file.createNewFile();
			FileWriter temp = new FileWriter(file, false);
			temp.flush();
			temp.close();
			FileOutputStream fos = new FileOutputStream(file); 
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(pieceData);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			error = true;
			e.printStackTrace();
		}
		if(!error) {
			CustomPiece.defineNewPiece(pieceData);
			successPopup.setMessage("Piece \"" + pieceData.getName() + "\" created successfully.");
			successPopup.show();
			PieceBuilder.reset();
			currentData = pieceData;
		}
	}
	
	/**
	 * BLOCKS while it waits for the user to confirm deletion.
	 * Does nothing if the user cancels the deletion.
	 * @param pieceName
	 */
	public static void attemptDelete(String pieceName) {
		if(confirmDelete(pieceName)){
			System.out.println(">>> Delete confirmed");
			CustomPiece.deletePiece(pieceName);
			File file = new File("userpieces/" + pieceName + ".dat");
			file.delete();
			PieceBuilder.reset(true);
		}
		else {
			System.out.println(">>> Delete cancelled");
		}
	}
	
	private static boolean confirmDelete(String pieceName) {
		instance.deleteConfirmation.setPieceName(pieceName);
		instance.deleteConfirmation.showAndWait();
		return instance.deleteConfirmation.getLastResult();
	}
	
	private class ConfirmDeletionPopup extends Stage{
		private Label sureLabel;
		private volatile boolean result;
		public ConfirmDeletionPopup() {
			super();
			this.initModality(Modality.APPLICATION_MODAL);
			this.initOwner(PieceBuilder.this);
			StackPane content = new StackPane();
			this.setScene(new Scene(content));
			VBox vBox = new VBox(30);
			vBox.setPadding(new Insets(20));
			sureLabel = new Label("Are you sure you want to delete?");
			sureLabel.setWrapText(true);
			Button cancelButton = new Button("Cancel");
			cancelButton.setOnAction(actionEvent -> {
				result = false;
				this.close();
			});
			Button confirmButton = new Button("Confirm");
			confirmButton.setOnAction(actionEvent -> {
				result = true;
				this.close();
			});
			HBox buttonsBox = new HBox(20);
			buttonsBox.getChildren().addAll(cancelButton, confirmButton);
			buttonsBox.setAlignment(Pos.CENTER_RIGHT);
			vBox.getChildren().addAll(sureLabel, buttonsBox);
			content.getChildren().add(vBox);
			this.setOnCloseRequest(windowEvent -> {
				result = false;
				this.close();
			});
			this.sizeToScene();
		}
		
		public boolean getLastResult() {
			return result;
		}
		
		public void setPieceName(String pieceName) {
			sureLabel.setText("Are you sure you want to delete the \"" + pieceName + "\" piece?"
					+ " This will reset the Piece Builder");
		}
	}
	
	private class SuccessPopup extends Stage{
		private Label messageLabel;
		public SuccessPopup() {
			this("");
		}
		public SuccessPopup(String message) {
			super();
			this.initModality(Modality.APPLICATION_MODAL);
			this.initOwner(PieceBuilder.this);
			StackPane content = new StackPane();
			this.setScene(new Scene(content));
			VBox vBox = new VBox(30);
			vBox.setPadding(new Insets(20));
			vBox.setFillWidth(true);
			messageLabel = new Label(message);
			messageLabel.setWrapText(true);
			Button closeButton = new Button("Close");
			closeButton.setOnAction(actionEvent -> {
				this.close();
			});
			HBox hBox = new HBox(20, closeButton);
			hBox.setAlignment(Pos.CENTER_RIGHT);
			vBox.getChildren().addAll(messageLabel, hBox);
			content.getChildren().add(vBox);
			this.sizeToScene();
		}
		
		public void setMessage(String newMessage) {
			messageLabel.setText(newMessage);
		}
	}
	@Override
	public boolean verifyInput() {
		System.out.println("PieceBuilder verify called");
		try {
			boolean result = true;
			String name = nameTextField.getText().strip();
			if(name == null || name.isEmpty() || name.isBlank()) {
				submitErrorMessage("name field is blank");
				result = false;
			}
			if(currentData == null) {
				if(Piece.isNameOfPiece(name)) {
					submitErrorMessage("A piece with this name already exists.");
					result = false;
				}
			}
			if(name.indexOf('+') >= 0 || name.indexOf('-') >= 0) {
				submitErrorMessage("Piece names cannot contain a plus (+) or minus (-)");
				result = false;
			}
			result &= actionTreeBuilder.verifyInput();
			if(result) {
				System.out.println("****INPUT SUCCESSFULLY VERIFIED****");
			}
			else {
				System.out.println("Input Validation returned false.");
			}
			return result;
		}
		catch(Exception exception) {
			System.out.println("Exception occured while trying to validate input:");
			exception.printStackTrace(System.err);
			submitErrorMessage("Unkown Error occured.");
		}
		return false;
	}

	private void submitErrorMessage(String message) {
		if(!errorsShowing) {
			errorVBox.getChildren().add(hideErrors);
			errorsShowing = true;
			errorVBox.setMouseTransparent(false);
		}
		Label label = new Label(message);
		label.setMouseTransparent(true);
		label.styleProperty().bind(errorFontStringExpression);
		errorVBox.getChildren().add(0, label);
	}
	
	private void clearErrors0() {
		errorVBox.getChildren().clear();
		errorsShowing = false;
		errorVBox.setMouseTransparent(true);
	}
	
	/** Does not check if instance is null*/
	private static void reset(boolean updatePieces) {
		if(updatePieces) {
			updatePieceNames();
		}
		clearErrors();
		instance.whiteImage = WHITE_DEFAULT_IMAGE;
		instance.whiteImageURIString = null;
		instance.whiteImageView.setImage(instance.whiteImage);
		instance.whiteXAnchor.setVisible(false);
		instance.blackImage = BLACK_DEFAULT_IMAGE;
		instance.blackImageURIString = null;
		instance.blackImageView.setImage(instance.blackImage);
		instance.blackXAnchor.setVisible(false);
		instance.nameTextField.setText("");
		instance.createPieceButton.setText("Create Piece");
		instance.actionTreeBuilder.reset();
		instance.nameTextField.setEditable(true);
		instance.currentData = null;
	}
	
	/** Does not check if instance is null*/
	private static void reset() {
		reset(true);
	}
	public static void open() {
		if(associatedGamePanel != null) {
			associatedGamePanel.getBoard().deselect();
		}
		reset();
		instance.show();
	}
	
	public static void open(Piece p) {
		if(p == null) {
			throw new NullPointerException();
		}
		open(p.getPieceData());
	}
	
	public static void open(String pieceName) {
		if(pieceName == null) {
			throw new NullPointerException();
		}
		open(Piece.getDataFor(pieceName));
	}
	
	public static void open(PieceData data) {
		if(data == null) {
			throw new NullPointerException();
		}
		if(associatedGamePanel != null) {
			associatedGamePanel.getBoard().deselect();
		}
		reset();
		instance.nameTextField.setText(data.getName());
		instance.nameTextField.setEditable(false);
		instance.whiteImage = data.getImage(Piece.WHITE);
		instance.whiteImageURIString = data.getWhiteImageURIString();
		if(instance.whiteImageURIString != null) {
			instance.whiteImageView.setImage(instance.whiteImage);
			instance.whiteXAnchor.setVisible(true);
		}
		instance.blackImage = data.getImage(Piece.BLACK);
		instance.blackImageURIString = data.getBlackImageURIString();
		if(instance.blackImageURIString != null) {
			instance.blackImageView.setImage(instance.blackImage);
			instance.blackXAnchor.setVisible(true);
		}
		instance.show();
		instance.actionTreeBuilder.loadTree(data.getTree());
		instance.createPieceButton.setText("Save Edits");
		instance.currentData = data;
		
	}
	
	public void attemptClose() {
		close0();
	}
	
	public void close0() {
		close();
		if(associatedGamePanel != null) {
			associatedGamePanel.getBoard().movePreparerForFXThread.prepare();
		}
		
	}
	
	private Collection<String> currentPieceNames0(){
		return currentPieceNames;
	}
}
