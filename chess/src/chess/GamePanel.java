package chess;

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
	    
	    StackPane board = Board.defaultBoard();
	    
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
