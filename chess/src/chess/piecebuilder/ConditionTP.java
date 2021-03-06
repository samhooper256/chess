package chess.piecebuilder;

import java.util.ArrayList;
import java.util.Collection;

import chess.util.Condition;
import chess.util.InputVerification;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

public class ConditionTP extends TitledPane implements InputVerification, Buildable<Collection<Condition>>{
	private VBox esvBox; //content of this ConditionTP
	private Button addConditionButton;
	private ConditionUtilFlowPane conditionUtilFlowPane;
	public ConditionTP() {
		this("Conditions");
	}
	
	public ConditionTP(String name) {
		super();
		esvBox = new VBox(10);
		esvBox.setPadding(new Insets(10));
		conditionUtilFlowPane = new ConditionUtilFlowPane();
		addConditionButton = new Button("Add condition");
		addConditionButton.setOnMouseClicked(mouseEvent -> {
			ConditionBoxWrap conditionBoxWrap = new ConditionBoxWrap();
			esvBox.getChildren().add(esvBox.getChildren().lastIndexOf(addConditionButton), conditionBoxWrap);
		});
		esvBox.getChildren().addAll(conditionUtilFlowPane, addConditionButton);
		this.setText(name);
		this.setContent(esvBox);
		this.setExpanded(false);
	}
	
	
	@Override
	public Collection<Condition> build(){
		ArrayList<Condition> end = new ArrayList<>(this.getChildren().size());
		for(Node fxNode : esvBox.getChildren()) {
			if(fxNode instanceof Buildable) {
				Object builtObject = ((Buildable<?>) fxNode).build();
				if(builtObject instanceof Condition) {
					end.add((Condition) builtObject);
				}
				else {
					throw new IllegalArgumentException("Unrecognized built object: " + builtObject + 
							" (" + builtObject.getClass() + ")");
				}
			}
		}
		System.out.println("Condition Collection returned: " + end);
		return end;
	}
	
	@Override
	public boolean verifyInput() {
		boolean result = true;
		for(Node fxNode : esvBox.getChildren()) {
			if(fxNode instanceof InputVerification) {
				if(!((InputVerification) fxNode).verifyInput()) {
					result = false;
				}
			}
		}
		
		return result;
	}
	
	public static void reconstruct(Collection<Condition> conditions, ConditionTP whereToAdd) {
		int addIndex = 1;
		for(Condition con : conditions) {
			whereToAdd.esvBox.getChildren().add(addIndex, ConditionBoxWrap.reconstructCondition(con));
			addIndex++;
		}
	}
}
