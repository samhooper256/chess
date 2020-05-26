package chess.base;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/* *
 * @author Sam Hooper
 */
public class GamePanel extends HBox{
	public GamePanel() {
		final VBox vBox = new VBox();

	    vBox.alignmentProperty().set(Pos.CENTER);
	    this.alignmentProperty().set(Pos.CENTER);
	    
	    Board board = Board.defaultBoard(); //TODO uncomment
	    
	    /*
	    BoardPreset pre = new BoardPreset();
	    pre.setPieces(new String[] {"-King", null, null, null, null, null, null, null},
	    		new String[] {null, null, null, null, null, null, null, "-Bishop"},
	    		new String[] {null, null, null, null, "-Knight", "+Knight", null, null},
	    		new String[] {null, null, null, null, "-Queen", "+Queen", null, null},
	    		new String[] {null, null, null, "+Bishop", "+Knight", null, "+Rook", null},
	    		null,
	    		new String[] {"+King", null, "-Rook", null, null, null, null, null},
	    		null);
	    Board board = Board.fromPreset(pre);
	    */
	    final NumberBinding binding = Bindings.min(widthProperty(), heightProperty());
	    
	    vBox.prefWidthProperty().bind(binding);
        vBox.prefHeightProperty().bind(binding);
        vBox.setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);

        vBox.setFillWidth(true);
        VBox.setVgrow(board, Priority.ALWAYS);
        
        vBox.getChildren().add(board);

        getChildren().add(vBox);

        HBox.setHgrow(this, Priority.ALWAYS);
	}
}
