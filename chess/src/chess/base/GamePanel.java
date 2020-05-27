package chess.base;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/* *
 * @author Sam Hooper
 */
public class GamePanel extends HBox{
	private Pane leftPanel, rightPanel;
	private StackPane iLeft, iRight;
	private AnchorPane rightAnchor;
	private Button modeButton, resetButton;
	private VBox boardBox;
	private Board board;
	public GamePanel() {
		this.setMinHeight(400);
		this.alignmentProperty().set(Pos.CENTER);
		
	    leftPanel = new Pane();
	    rightPanel = new Pane();
	    rightPanel.setBorder(new Border(new BorderStroke(Color.DARKRED, BorderStrokeStyle.DASHED, CornerRadii.EMPTY, new BorderWidths(1))));
	    iLeft = new StackPane();
	    iRight = new StackPane();
	    iRight.setBorder(new Border(new BorderStroke(Color.DEEPPINK, BorderStrokeStyle.DOTTED, CornerRadii.EMPTY, new BorderWidths(2))));
	    rightPanel.setPrefWidth(0);
	    leftPanel.setPrefWidth(0);
	    rightPanel.getChildren().add(iRight);
	    leftPanel.getChildren().add(iLeft);
	    
	    
	    rightAnchor = new AnchorPane();
	    Button modeButton = new Button("Play Mode");
	    Button resetButton = new Button("Reset Board");
	    resetButton.setOnMouseClicked(x -> board.reset());
	    rightAnchor.getChildren().addAll(modeButton, resetButton);
	    
	    AnchorPane.setLeftAnchor(modeButton, 10d);
	    AnchorPane.setRightAnchor(modeButton, 10d);
	    AnchorPane.setTopAnchor(modeButton, 10d);
	    AnchorPane.setLeftAnchor(resetButton, 10d);
	    AnchorPane.setRightAnchor(resetButton, 10d);
	    AnchorPane.setBottomAnchor(resetButton, 10d);
	    rightAnchor.setBorder(new Border(new BorderStroke(Color.DARKGREEN, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
	    
	    iRight.getChildren().add(rightAnchor);
	    
	    HBox.setHgrow(leftPanel, Priority.ALWAYS);
	    HBox.setHgrow(rightPanel, Priority.ALWAYS);
	    
	    final NumberBinding binding = Bindings.min(widthProperty(), heightProperty());
	    
	    board = Board.defaultBoard();
	    
	    boardBox = new VBox();
	    boardBox.alignmentProperty().set(Pos.CENTER); 
	    boardBox.prefWidthProperty().bind(binding);
        boardBox.prefHeightProperty().bind(binding);
        boardBox.setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);

        VBox.setVgrow(board, Priority.ALWAYS);
        
        boardBox.getChildren().add(board);
        rightAnchor.prefHeightProperty().bind(boardBox.heightProperty());
        rightAnchor.prefWidthProperty().bind(rightPanel.widthProperty());
        getChildren().addAll(leftPanel, boardBox, rightPanel);

        //HBox.setHgrow(this, Priority.ALWAYS);
	}
	
	
}
