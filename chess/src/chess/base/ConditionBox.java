package chess.base;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import chess.util.AFC;
import chess.util.Condition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.FlowPane;
import javafx.util.StringConverter;

public class ConditionBox extends FlowPane implements InputVerification{
	private Label conditionNameLabel;
	private ChoiceBox<String> box1;
	private StringConverter<? extends Member> stringConverter;
	private ChoiceBox<Field> premadeChoiceBox;
	private boolean isOnPremade;
	private PieceBuilder pieceBuilder;
	public ConditionBox(PieceBuilder builder) {
		this.pieceBuilder = builder;
		conditionNameLabel = new Label("Condition: ");
		box1 = new ChoiceBox<>();
		box1.getItems().addAll("Premade", "Custom");
		box1.setValue("Select Type");
		box1.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
				String choice = box1.getItems().get((Integer) number2);
	        	if(choice.equals("Premade")) {
	        		setupForPremade();
	        	}
	        	else if(choice.equals("Custom")) {
	        		setupForCustom();
	        	}
			}
		});
		stringConverter = new StringConverter<>() {

			@Override
			public String toString(Member member) {
				if(member instanceof Field) {
					return ((Field) member).getAnnotation(AFC.class).name();
				}
				else if(member instanceof Method) {
					return ((Method) member).getAnnotation(AFC.class).name();
				}
				else {
					return "???";
				}
			}

			@Override
			public Member fromString(String string) {
				return null;
			}
		};
		this.getChildren().addAll(conditionNameLabel, box1);
		this.setHgap(2);
		this.setVgap(4);
	}
	
	private void setupForPremade() {
		ObservableList<Node> children = this.getChildren();
		int box1index = children.indexOf(box1);
		while(children.size() > box1index + 1) {
			children.remove(children.size() - 1);
		}
		if(premadeChoiceBox == null) {
			premadeChoiceBox = new ChoiceBox<>();
			premadeChoiceBox.setConverter((StringConverter<Field>) stringConverter);
			
			for(Field f : Condition.class.getFields()) {
				if(Modifier.isStatic(f.getModifiers()) && f.isAnnotationPresent(AFC.class)){
					premadeChoiceBox.getItems().add(f);
				}
			}
		}
		this.getChildren().add(premadeChoiceBox);
		isOnPremade = true;
	}
	
	private void setupForCustom() {
		
		isOnPremade = false;
	}
	
	public Condition build() {
		if(isOnPremade) {
			try {
				Condition cond = (Condition) premadeChoiceBox.getValue().get(null);
				//System.out.println("Condition returned: " + cond);
				return cond;
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			throw new IllegalArgumentException("Unkown Error");
		}
		else {
			throw new IllegalArgumentException("Custom conditions cannot be built yet.");
		}
	}
	
	class ConditionChoiceBox extends ChoiceBox<ConditionOption>{
		public ConditionChoiceBox(String... options) {
			super();
			for(int i = 0; i < options.length; i++) {
				this.getItems().add(new ConditionOption(options[i], this));
			}
			ConditionChoiceBox.this.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
		      @Override
		      public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
		        ConditionOption choice = ConditionChoiceBox.this.getItems().get((Integer) number2);
		        choice.updatePane();
		      }
		    });
		}
	}
	class ConditionOption extends MenuItem{
		private final String name;
		private final ConditionChoiceBox choiceBox;
		public ConditionOption(String name, ConditionChoiceBox choiceBox) {
			super(name);
			this.name = name;
			this.choiceBox = choiceBox;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		private void updatePane() {
			int myIndex = ConditionBox.this.getChildren().indexOf(choiceBox);
			ObservableList<Node> paneChildren = ConditionBox.this.getChildren();
			while(paneChildren.size() > myIndex + 1) {
				paneChildren.remove(paneChildren.size() - 1);
			}
			paneChildren.add(new ConditionChoiceBox("AAAAAAAAAAAAAAAAAAAAAAAAAAA","BBBBBBBBBBBB"));
		}
	}
	@Override
	public boolean verifyInput() {
		if(box1.getSelectionModel().isEmpty()) {
			pieceBuilder.submitErrorMessage("Condition type (Premade/Custom) has not been selected.");
			return false;
		}
		
		if(isOnPremade) {
			if(premadeChoiceBox.getSelectionModel().isEmpty()) {
				pieceBuilder.submitErrorMessage("Premade condition has not been selected.");
				return false;
			}
			else {
				return true;
			}
		}
		else {
			boolean result = true;
			//TODO Verify input for custom conditions
			return result;
		}
		
	}
}