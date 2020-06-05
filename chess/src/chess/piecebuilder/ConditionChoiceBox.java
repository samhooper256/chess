package chess.piecebuilder;

import java.lang.reflect.Method;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.Pane;

public class ConditionChoiceBox extends ChoiceBox<ConditionOption>{
	public Pane nodeToAddTo;
	public ConditionChoiceBox(Pane ntad) {
		super();
		this.nodeToAddTo = ntad;
		ConditionChoiceBox.this.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
	      @Override
	      public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
	        ConditionOption choice = ConditionChoiceBox.this.getItems().get((Integer) number2);
	        choice.updatePane();
	      }
	    });
	}
	
	/* *
	 * PRECONDITION: m has an AFC annotation.
	 */
	public void addMethod(Method m) {
		this.getItems().add(new MethodConditionOption(this, m));
	}
	
	/* *
	 * PRECONDITION: m has an AFC annotation.
	 */
	public void addMethod(String name, Method m) {
		this.getItems().add(new MethodConditionOption(this, name, m));
	}
}
