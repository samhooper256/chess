package chess.piecebuilder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import chess.util.AFC;
import chess.util.Flag;
import chess.util.InputVerification;
import chess.util.SubMulti;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SubActionsTP extends TitledPane implements InputVerification, Buildable<Pair<List<SubMulti>, List<Boolean>>>{
	private static final List<Method> subMultiCreationMethods;
	static {
		subMultiCreationMethods = new ArrayList<>();
		Method[] allSubMultiMethods = SubMulti.class.getMethods();
		for(Method m : allSubMultiMethods) {
			if(m.isAnnotationPresent(AFC.class)) {
				subMultiCreationMethods.add(m);
			}
		}
	}
	
	private VBox vBox; //content of this SubActionTP
	private MenuButton addActionButton;
	private CheckBox necessaryCheckBox;
	
	public SubActionsTP() {
		super();
		vBox = new VBox(10);
		vBox.setPadding(new Insets(10,0,10,10));
		addActionButton = new MenuButton("Add action");
		necessaryCheckBox = new CheckBox("necessary"); //TODO add tooltip
		setupAddActionButton();
		vBox.getChildren().add(addActionButton);
		this.setText("Sub-Actions");
		this.setContent(vBox);
		this.setExpanded(false);
	}
	
	private void setupAddActionButton() {
		ObservableList<MenuItem> items = addActionButton.getItems();
		for(Method m : subMultiCreationMethods) {
			MenuItem mi = new MenuItem();
			mi.setText(m.getAnnotation(AFC.class).name());
			mi.setOnAction(actionEvent -> {
				addSubMultiAction(m);
			});
			items.add(mi);
		}
	}
	
	private void addSubMultiAction(Method creationMethod) {
		SubActionsTP.this.vBox.getChildren().add(0, new SubActionTP(creationMethod));
	}
	
	
	
	@Override
	public boolean verifyInput() {
		boolean result = true;
		for(Node fxNode : vBox.getChildren()) {
			if(fxNode instanceof InputVerification) {
				if(!((InputVerification) fxNode).verifyInput()) {
					result = false;
				}
			}
		}
		return result;
	}
	
	@Override
	public Pair<List<SubMulti>, List<Boolean>> build(){
		List<SubMulti> end = new ArrayList<>();
		for(Node fxNode : vBox.getChildren()) {
			if(fxNode instanceof SubActionTP) {
				end.add(((SubActionTP) fxNode).build());
			}
		}
		List<Boolean> states = new ArrayList<>(end.size());
		return new Pair<>(end, null); //TODO ADD STATES
	}
	
	private class SubActionTP extends TitledPane implements InputVerification, Buildable<SubMulti>{
		private Method creationMethod;
		private AFC afc;
		private VBox vBox;
		private ChoiceBox<Flag> flagChoice;
		private SubActionTP(Method cm) {
			super();
			this.creationMethod = cm;
			this.afc = cm.getAnnotation(AFC.class);
			this.setText(afc.name());
			vBox = new VBox();
			this.setContent(vBox);
			int paramIndex = 0;
			if(cm.getParameterTypes()[0] == Flag.class) {
				flagChoice = new ChoiceBox<>();
				flagChoice.getItems().addAll(Flag.ORIGIN, Flag.DESTINATION);
				HBox flagChoiceHBox = new HBox(4);
				flagChoiceHBox.getChildren().addAll(new Label("relative to: "), flagChoice);
				flagChoiceHBox.setAlignment(Pos.CENTER_LEFT);
				vBox.getChildren().add(flagChoiceHBox);
				paramIndex++;
			}
			else {
				flagChoice = null;
			}
		}
		@Override
		public SubMulti build() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public boolean verifyInput() {
			// TODO Auto-generated method stub
			return false;
		}
		
		
		
	}
}
