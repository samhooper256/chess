package chess.piecebuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;

import chess.base.CustomPiece;
import chess.base.Piece;
import chess.base.PieceData;
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
import javafx.scene.layout.BorderPane;
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
	public static final Image WHITE_DEFAULT_IMAGE, BLACK_DEFAULT_IMAGE;
	public static final String WHITE_DEFAULT_URI, BLACK_DEFAULT_URI;
	//TODO Maybe make an "error loading" image to use when the file path is wrong?
	
	static {
		WHITE_DEFAULT_IMAGE = new Image(PieceBuilder.class.getResourceAsStream(WHITE_DEFAULT_URI = "/resources/white_default_image.png"), 240, 240, false, true);
		BLACK_DEFAULT_IMAGE = new Image(PieceBuilder.class.getResourceAsStream(BLACK_DEFAULT_URI = "/resources/black_default_image.png"), 240, 240, false, true);
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
	private static Collection<String> currentPieceNames; //TODO Update this whenever PieceBuilder is opened! (and when a new piece is added and PieceBuilder remains open)
	private FileChooser fileChooser;
	private AnchorPane whiteXAnchor, blackXAnchor;
	private Label whiteX, blackX;
	private String whiteImageURIString, blackImageURIString;
	
	private PieceBuilder() {
		super();
		whiteImageURIString = null;
		blackImageURIString = null;
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
		
		leftImageVBox = new GridPane();
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
		whiteImageInternal.setBorder(new Border(new BorderStroke(Color.grayRgb(117), BorderStrokeStyle.DASHED, CornerRadii.EMPTY, new BorderWidths(2)))); //TODO put in css
		whiteXAnchor = new AnchorPane(); 
		whiteX = new Label("X"); //TODO IN css, make this color red (and make font larger?) (do the same for blackX)
		AnchorPane.setRightAnchor(whiteX, 10d);
		AnchorPane.setTopAnchor(whiteX, 10d);
		whiteXAnchor.getChildren().add(whiteX);
		whiteXAnchor.setPickOnBounds(false);
		whiteXAnchor.setVisible(false);
		whiteImage = WHITE_DEFAULT_IMAGE;
		whiteImageView = new WrappedImageView(whiteImage, 0, 0);
		whiteImageView.setPreserveRatio(true);
		whiteImageInternal.getChildren().addAll(whiteImageView, whiteXAnchor);
		whiteImageInternal.maxWidthProperty().bind(whiteImageInternal.heightProperty());
		whiteImageOuter.getChildren().add(whiteImageInternal);
		//whiteImageOuter.setStyle("-fx-background-color: pink;");
		
		blackImageOuter = new StackPane();
		blackImageInternal = new StackPane();
		blackImageInternal.setBorder(new Border(new BorderStroke(Color.grayRgb(117), BorderStrokeStyle.DASHED, CornerRadii.EMPTY, new BorderWidths(2)))); //TODO put in css
		
		blackXAnchor = new AnchorPane(); 
		blackX = new Label("X"); //TODO IN css, make this color red (and make font larger?) (do the same for blackX)
		AnchorPane.setRightAnchor(blackX, 10d);
		AnchorPane.setTopAnchor(blackX, 10d);
		blackXAnchor.getChildren().add(blackX);
		blackXAnchor.setPickOnBounds(false);
		blackXAnchor.setVisible(false);
		blackImage = BLACK_DEFAULT_IMAGE;
		blackImageView = new WrappedImageView(blackImage, 0, 0);
		blackImageView.setPreserveRatio(true);
		blackImageInternal.getChildren().addAll(blackImageView, blackXAnchor);
		blackImageInternal.maxWidthProperty().bind(blackImageInternal.heightProperty());
		blackImageOuter.getChildren().add(blackImageInternal);
		//blackImageOuter.setStyle("-fx-background-color: pink;");
		
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
		//this.setMinHeight(400);
		//this.minWidthProperty().bind(this.heightProperty().multiply(1.5));
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
		
		File file = new File("userpieces/" + pieceData.getName() + ".dat");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			if(CustomPiece.isDefinedPieceName(name)) {
				submitErrorMessage("A piece with this name already exists.");
			}
			if(name.indexOf('+') >= 0 || name.indexOf('-') >= 0) {
				submitErrorMessage("Piece names cannot contain a plus (+) or minus (-)"); //TODO actual error message
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
	
	/** Does not check if instance is null*/
	private static void reset() {
		instance.whiteImage = WHITE_DEFAULT_IMAGE;
		instance.whiteImageView.setImage(instance.whiteImage);
		instance.whiteXAnchor.setVisible(false);
		instance.blackImage = BLACK_DEFAULT_IMAGE;
		instance.blackImageView.setImage(instance.blackImage);
		instance.blackXAnchor.setVisible(false);
		instance.nameTextField.setText("");
		instance.actionTreeBuilder.reset();
	}
	public static void open() {
		currentPieceNames = Piece.getNamesOfAllPieces();
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
		currentPieceNames = Piece.getNamesOfAllPieces();
		reset();
		instance.nameTextField.setText(data.getName());
		instance.nameTextField.setEditable(false);
		instance.whiteImage = data.getImage(Piece.WHITE);
		instance.whiteImageView.setImage(instance.whiteImage);
		instance.whiteXAnchor.setVisible(true);
		instance.blackImage = data.getImage(Piece.BLACK);
		instance.blackImageView.setImage(instance.blackImage);
		instance.blackXAnchor.setVisible(true);
		instance.show();
		instance.actionTreeBuilder.loadTree(data.getTree());
		
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
