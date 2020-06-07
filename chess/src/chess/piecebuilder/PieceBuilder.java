package chess.piecebuilder;

import java.io.File;
import java.util.Collection;
import java.util.List;

import chess.base.CustomPiece;
import chess.base.Piece;
import chess.base.WrappedImageView;
import chess.util.InputVerification;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PieceBuilder extends Stage implements InputVerification{
	private static final Image WHITE_DEFAULT_IMAGE, BLACK_DEFAULT_IMAGE;
	
	static {
		WHITE_DEFAULT_IMAGE = new Image(PieceBuilder.class.getResourceAsStream("/resources/white_default_image.png"), 240, 240, false, true);
		BLACK_DEFAULT_IMAGE = new Image(PieceBuilder.class.getResourceAsStream("/resources/black_default_image.png"), 240, 240, false, true);
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
	
	public static void submitError(String message) {
		if(instance == null) {
			throw new NullPointerException("The instance does not exist.");
		}
		else {
			instance.submitErrorMessage(message);
		}
	}
	
	public static void clearErrors() {
		if(instance == null) {
			throw new NullPointerException("The instance does not exist.");
		}
		else {
			instance.clearErrors0();
		}
	}
	
	public static Collection<String> currentPieceNames(){
		if(instance == null) {
			throw new NullPointerException("The instance does not exist.");
		}
		else {
			return instance.currentPieceNames0();
		}
	}
	
	private Scene scene;
	private StackPane outerStackPane, whiteImageOuter, blackImageOuter, whiteImageInternal, blackImageInternal;
	private VBox outermostVBox, leftVBox, leftImageVBox;
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
	private Collection<String> currentPieceNames; //TODO Update this whenever PieceBuilder is opened! (and when a new piece is added and PieceBuilder remains open)
	private FileChooser fileChooser;
	private AnchorPane whiteXAnchor, blackXAnchor;
	private Label whiteX, blackX;
	
	private PieceBuilder() {
		super();
		outermostVBox = new VBox();
		outermostVBox.setFillWidth(true);
		outerStackPane = new StackPane();
		VBox.setVgrow(outerStackPane, Priority.ALWAYS);
		outermostVBox.getChildren().addAll(new MenuBar(new Menu("oof")), outerStackPane);
		scene = new Scene(outermostVBox, 600, 400);
		scene.getStylesheets().add(PieceBuilder.class.getResource("piecebuilderstyle.css").toExternalForm());
		
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
		
		leftImageVBox = new VBox(10);
		
		
		whiteImageOuter = new StackPane();
		whiteImageOuter.maxWidthProperty().bind(leftImageVBox.widthProperty());
		NumberBinding nbw = Bindings.min(whiteImageOuter.widthProperty(), whiteImageOuter.heightProperty());
		whiteImageInternal = new StackPane();
		whiteImageInternal.setBorder(new Border(new BorderStroke(Color.grayRgb(117), BorderStrokeStyle.DASHED, CornerRadii.EMPTY, new BorderWidths(2)))); //TODO put in css
		whiteImageInternal.maxWidthProperty().bind(nbw);
		whiteImageInternal.maxHeightProperty().bind(nbw);
		whiteXAnchor = new AnchorPane(); 
		whiteX = new Label("X"); //TODO IN css, make this color red (and make font larger?) (do the same for blackX)
		AnchorPane.setRightAnchor(whiteX, 10d);
		AnchorPane.setTopAnchor(whiteX, 10d);
		whiteXAnchor.getChildren().add(whiteX);
		whiteXAnchor.setPickOnBounds(false);
		whiteXAnchor.setVisible(false);
		whiteImage = WHITE_DEFAULT_IMAGE;
		whiteImageView = new WrappedImageView(whiteImage, 0, 0);
		whiteImageInternal.getChildren().addAll(whiteImageView, whiteXAnchor);
		whiteImageOuter.getChildren().add(whiteImageInternal);
		
		blackImageOuter = new StackPane();
		NumberBinding nbb = Bindings.min(blackImageOuter.widthProperty(), blackImageOuter.heightProperty());
		blackImageInternal = new StackPane();
		blackImageInternal.setBorder(new Border(new BorderStroke(Color.grayRgb(117), BorderStrokeStyle.DASHED, CornerRadii.EMPTY, new BorderWidths(2)))); //TODO put in css
		blackImageInternal.maxWidthProperty().bind(nbb);
		blackImageInternal.maxHeightProperty().bind(nbb);
		blackXAnchor = new AnchorPane(); 
		blackX = new Label("X"); //TODO IN css, make this color red (and make font larger?) (do the same for blackX)
		AnchorPane.setRightAnchor(blackX, 10d);
		AnchorPane.setTopAnchor(blackX, 10d);
		blackXAnchor.getChildren().add(blackX);
		blackXAnchor.setPickOnBounds(false);
		blackXAnchor.setVisible(false);
		blackImage = BLACK_DEFAULT_IMAGE;
		blackImageView = new WrappedImageView(blackImage, 0, 0);
		blackImageInternal.getChildren().addAll(blackImageView, blackXAnchor);
		blackImageOuter.getChildren().add(blackImageInternal);
		
		whiteX.setOnMouseClicked(mouseEvent -> {
			clearWhiteImage();
			mouseEvent.consume();
		});
		whiteImageInternal.setOnMouseClicked(mouseEvent -> {
			fileChooser.setTitle("Select White Piece Image");
			File selectedFile = fileChooser.showOpenDialog(PieceBuilder.this);
			if(selectedFile != null) {
				Image newWhiteImage = new Image(selectedFile.toURI().toString(), 240, 240, false, true);
				setCustomWhiteImage(newWhiteImage);
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
							Image newWhiteImage = new Image(file.toURI().toString(), 240, 240, false, true);
							setCustomWhiteImage(newWhiteImage);
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
		});
		blackImageInternal.setOnMouseClicked(mouseEvent -> {
			fileChooser.setTitle("Select Black Piece Image");
			File selectedFile = fileChooser.showOpenDialog(PieceBuilder.this);
			if(selectedFile != null) {
				Image newBlackImage = new Image(selectedFile.toURI().toString(), 240, 240, false, true);
				setCustomBlackImage(newBlackImage);
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
							blackImage = new Image(file.toURI().toString(), 240, 240, false, true);
							blackImageView.setImage(blackImage);
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
		
		VBox.setVgrow(whiteImageOuter, Priority.ALWAYS);
		VBox.setVgrow(blackImageOuter, Priority.ALWAYS);
		leftImageVBox.setAlignment(Pos.CENTER);
		leftImageVBox.getChildren().addAll(whiteImageOuter, blackImageOuter);
		
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
		createPieceButton.prefHeightProperty().bind(leftVBox.heightProperty().divide(8));
		createPieceButton.setWrapText(true);
		createPieceButton.setId("create-piece-button");
		createPieceButton.setOnMouseClicked(mouseEvent -> attemptCreate());
		
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
		this.setMinHeight(400);
		this.minWidthProperty().bind(this.heightProperty().multiply(1.5));
		this.initModality(Modality.APPLICATION_MODAL);
		this.setOnCloseRequest(windowEvent -> {
			attemptClose();
			windowEvent.consume();
		});
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
	private void setCustomWhiteImage(Image newWhiteImage) {
		whiteImage = newWhiteImage;
		whiteImageView.setImage(whiteImage);
		whiteXAnchor.setVisible(true);
	}
	private void setCustomBlackImage(Image newBlackImage) {
		blackImage = newBlackImage;
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
		CustomPiece.PieceData pieceData = new CustomPiece.PieceData(nameTextField.getText().strip());
		pieceData.whiteImage = whiteImage;
		pieceData.blackImage = blackImage;
		pieceData.tree = actionTreeBuilder.build();
		pieceData.pointValue = 5;
		CustomPiece.defineNewPiece(pieceData);
		this.hide(); //TODO popup window saying it was a success?
		
	}
	@Override
	public boolean verifyInput() {
		try {
			boolean result = true;
			String name = nameTextField.getText().strip();
			if(name == null || name.isEmpty() || name.isBlank()) {
				submitErrorMessage("name field is blank");
				result &= false;
			}
			if(name.indexOf('+') >= 0 || name.indexOf('-') >= 0) {
				submitErrorMessage("Bad name (contains plus or minus"); //TODO actual error message
				result &= false;
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
		}
		Label label = new Label(message);
		label.styleProperty().bind(errorFontStringExpression);
		errorVBox.getChildren().add(0, label);
	}
	
	private void clearErrors0() {
		errorVBox.getChildren().clear();
		errorsShowing = false;
	}
	
	private void reset() {
		whiteImage = WHITE_DEFAULT_IMAGE;
		whiteImageView.setImage(whiteImage);
		whiteXAnchor.setVisible(false);
		blackImage = BLACK_DEFAULT_IMAGE;
		blackImageView.setImage(blackImage);
		blackXAnchor.setVisible(false);
		nameTextField.setText("");
		actionTreeBuilder.reset();
	}
	public void open() {
		currentPieceNames = Piece.getNamesOfAllPieces();
		this.reset();
		this.show();
	}
	
	public void open(Piece p) {
		this.show();
	}
	
	public void attemptClose() {
		close0();
	}
	
	public void close0() {
		close();
	}
	
	private Collection<String> currentPieceNames0(){
		return currentPieceNames;
	}
}
