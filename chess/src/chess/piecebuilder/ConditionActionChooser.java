package chess.piecebuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import chess.base.LegalAction;
import chess.base.LegalCapture;
import chess.base.LegalMoveAndCapture;
import chess.base.LegalMulti;
import chess.base.LegalOtherMoveAndCapture;
import chess.base.LegalPromotion;
import chess.base.LegalSummon;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;

public class ConditionActionChooser extends ChoiceBox<Class<?>> {
	public static Collection<Class<?>> conditionSupportedClassTypes;
	
	static {
		conditionSupportedClassTypes = new ArrayList<>();
		conditionSupportedClassTypes.addAll(Arrays.asList(
			LegalAction.class,
			LegalMoveAndCapture.class,
			LegalOtherMoveAndCapture.class,
			LegalCapture.class,
			LegalPromotion.class,
			LegalSummon.class,
			LegalMulti.class
		));
	}
	
	public ConditionActionChooser() {
		super();
		this.setConverter(ConditionBox.classStringConverter);
		ObservableList<Class<?>> items = this.getItems();
		for(Class<?> clazz : conditionSupportedClassTypes) {
			items.add(clazz);
		}
	}
}
