package chess.base;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import chess.util.ActionTree;
import chess.util.MoveAndCaptureAction;
import javafx.beans.binding.Binding;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PieceBuilder extends Stage implements InputVerification{
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
	private static final Image WHITE_DEFAULT_IMAGE, BLACK_DEFAULT_IMAGE;
	Collection<String> currentPieceNames; //TODO Update this whenever PieceBuilder is opened! (and when a new piece is added and PieceBuilder remains open)
	
	static {
		WHITE_DEFAULT_IMAGE = new Image(PieceBuilder.class.getResourceAsStream("/resources/white_default_image.png"));
		BLACK_DEFAULT_IMAGE = new Image(PieceBuilder.class.getResourceAsStream("/resources/black_default_image.png"));
	}
	
	public PieceBuilder() {
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
		whiteImageInternal.setBorder(new Border(new BorderStroke(Color.grayRgb(117), BorderStrokeStyle.DASHED, CornerRadii.EMPTY, new BorderWidths(2))));
		whiteImageInternal.maxWidthProperty().bind(nbw);
		whiteImageInternal.maxHeightProperty().bind(nbw);
		whiteImage = WHITE_DEFAULT_IMAGE;
		whiteImageView = new WrappedImageView(whiteImage);
		whiteImageInternal.getChildren().add(whiteImageView);
		whiteImageOuter.getChildren().add(whiteImageInternal);
		
		blackImageOuter = new StackPane();
		NumberBinding nbb = Bindings.min(blackImageOuter.widthProperty(), blackImageOuter.heightProperty());
		blackImageInternal = new StackPane();
		blackImageInternal.setBorder(new Border(new BorderStroke(Color.grayRgb(117), BorderStrokeStyle.DASHED, CornerRadii.EMPTY, new BorderWidths(2))));
		blackImageInternal.maxWidthProperty().bind(nbb);
		blackImageInternal.maxHeightProperty().bind(nbb);
		blackImage = BLACK_DEFAULT_IMAGE;
		blackImageView = new WrappedImageView(blackImage);
		blackImageInternal.getChildren().add(blackImageView);
		blackImageOuter.getChildren().add(blackImageInternal);
		
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
							whiteImage = new Image(file.toURI().toString());
							whiteImageView.setImage(whiteImage);
							success = true;
						}
						catch(Exception e) {
							//TODO display error messsage about the image???
						}
						
					}
				}
            }
            /* let the source know whether the string was successfully 
             * transferred and used */
            dragEvent.setDropCompleted(success);

            dragEvent.consume();
		});
		blackImageInternal.setOnDragOver(dragEvent -> {
			if (dragEvent.getGestureSource() != blackImageInternal
                    && dragEvent.getDragboard().hasFiles()) {
                /* allow for both copying and moving, whatever user chooses */
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
							blackImage = new Image(file.toURI().toString());
							blackImageView.setImage(blackImage);
							success = true;
						}
						catch(Exception e) {
							//TODO display error messsage about the image???
						}
						
					}
				}
            }
            /* let the source know whether the string was successfully 
             * transferred and used */
            dragEvent.setDropCompleted(success);

            dragEvent.consume();
		});
		
		VBox.setVgrow(whiteImageOuter, Priority.ALWAYS);
		VBox.setVgrow(blackImageOuter, Priority.ALWAYS);
		leftImageVBox.setAlignment(Pos.CENTER);
		leftImageVBox.getChildren().addAll(whiteImageOuter, blackImageOuter);
		
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
		actionTreeBuilder = new ActionTreeBuilder(this);
		gridPane.add(actionTreeBuilder, 1, 0);
		//actionTreeBuilder.setBlank();
		
		///////////////////////////////
		//Make error box
		errorVBox = new VBox();
		errorVBox.setPickOnBounds(false);
		errorVBox.setAlignment(Pos.BOTTOM_RIGHT);
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
		return result;
	}
	
	public void submitErrorMessage(String message) {
		if(!errorsShowing) {
			errorVBox.getChildren().add(hideErrors);
			errorsShowing = true;
		}
		Label label = new Label(message);
		label.styleProperty().bind(errorFontStringExpression);
		errorVBox.getChildren().add(0, label);
	}
	
	public void clearErrors() {
		errorVBox.getChildren().clear();
		errorsShowing = false;
	}
	
	private void reset() {
		whiteImage = WHITE_DEFAULT_IMAGE;
		whiteImageView.setImage(whiteImage);
		blackImage = BLACK_DEFAULT_IMAGE;
		blackImageView.setImage(blackImage);
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
}
