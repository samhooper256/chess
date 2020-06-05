package chess.piecebuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import chess.util.AFC;
import chess.util.Condition;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

public class PremadeConditionBox extends ChoiceBox<Field> {
	private Pane nodeToAddTo; //Will always be a conditionBox
	public PremadeConditionBox(Pane ntad) {
		this.nodeToAddTo = ntad;
		this.setConverter((StringConverter<Field>) ConditionBox.memberStringConverter);
		for(Field f : Condition.class.getFields()) {
			if(Modifier.isStatic(f.getModifiers()) && f.isAnnotationPresent(AFC.class)){
				PremadeConditionBox.this.getItems().add(f);
			}
		}
	}
}
