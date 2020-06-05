package chess.piecebuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

import chess.util.AFC;
import chess.util.Condition;
import chess.util.ConditionClass;
import chess.util.InputVerification;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class ConditionBox extends VBox implements InputVerification, MultiConditionPart{
	private Label conditionNameLabel;
	private ChoiceBox<String> box1;
	public static StringConverter<? extends Member> memberStringConverter = new StringConverter<>() {

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
	public static StringConverter<Class<?>> classStringConverter = new StringConverter<>() {

		@Override
		public String toString(Class<?> clazz) {
			if(clazz.isAnnotationPresent(ConditionClass.class)) {
				ConditionClass annotation = clazz.getAnnotation(ConditionClass.class);
				return annotation.name();
			}
			else {
				return clazz.getSimpleName();
			}
		}

		@Override
		public Class<?> fromString(String string) {
			return null;
		}
	};
	
	private CustomConditionBox customConditionBox;
	private PremadeConditionBox premadeConditionBox;
	private boolean isOnPremade;
	private ESFlow flow;
	private FlowPane settingsFlow;
	private Pane nodeToAddTo;
	private HBox defaultValueHBox, invertedHBox;
	private ChoiceBox<Boolean> defaultValueChoiceBox;
	private CheckBox invertedCheckBox;
	private ConditionBoxWrap conditionBoxWrap;
	public ConditionBox(ConditionBoxWrap wrap, Pane ntad) {
		if(!(ntad instanceof ErrorSubmitable)) {
			throw new IllegalArgumentException("nodeToAddTo is not ErrorSubmitable");
		}
		this.conditionBoxWrap = wrap;
		this.nodeToAddTo = ntad;
		this.setFillWidth(true);
		flow = new ESFlow(this);
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
		
		flow.getChildren().addAll(conditionNameLabel, box1);
		flow.setHgap(2);
		flow.setVgap(4);
		this.setOnDragOver(dragEvent -> {
			//System.out.println("Condition box drag over");
			Dragboard db = dragEvent.getDragboard();
	        if (db.hasString() && db.getString().equals("multi-condition")) {
	        	dragEvent.acceptTransferModes(TransferMode.COPY);
	        }
	        dragEvent.consume();
		});
		this.setOnDragDropped(dragEvent -> {
			System.out.println("ConditionBox dropped (consumes) ");
			Dragboard db = dragEvent.getDragboard();
	        boolean success = false;
	        if (db.hasString() && db.getString().equals("multi-condition")) {
	        	System.out.println(">has correct string");
	        	Set<TransferMode> transferModes = db.getTransferModes();
	        	if(transferModes.size() == 1 && transferModes.iterator().next() == TransferMode.COPY) {
	        		System.out.println(">has valid transfer modes");
	    			new MultiConditionBox(this); //constructor handles rewiring nodeToAddTo
	    			success = true;
	        	}
	        }
	        System.out.println(">success="+success);
	        dragEvent.setDropCompleted(success);
	        dragEvent.consume();
		});
		this.setStyle("-fx-border-width: 1px; -fx-border-color: rgba(200,200,200,1.0); -fx-border-style: dashed;");
		
		defaultValueChoiceBox = new ChoiceBox<>();
		defaultValueChoiceBox.getItems().addAll(true,false);
		defaultValueChoiceBox.setValue(false);
		defaultValueHBox = new HBox(new Label("Default value:"), defaultValueChoiceBox); //TODO add tooltip
		defaultValueHBox.setSpacing(4);
		defaultValueHBox.setAlignment(Pos.CENTER_LEFT);
		
		invertedCheckBox = new CheckBox();
		invertedHBox = new HBox(new Label("Inverted:"), invertedCheckBox);
		invertedHBox.setSpacing(4);
		invertedHBox.setAlignment(Pos.CENTER_LEFT);
		
		settingsFlow = new FlowPane();
		settingsFlow.setHgap(10);
		settingsFlow.setVgap(4);
		settingsFlow.getChildren().addAll(defaultValueHBox, invertedHBox);
		this.getChildren().addAll(flow, settingsFlow);
	}
	
	private void setupForPremade() {
		ObservableList<Node> children = flow.getChildren();
		int box1index = children.indexOf(box1);
		clearPast(children, box1index);
		if(premadeConditionBox == null) {
			premadeConditionBox = new PremadeConditionBox(this.flow);
		}
		children.add(premadeConditionBox);
		isOnPremade = true;
	}
	
	private void setupForCustom() {
		
		ObservableList<Node> children = flow.getChildren();
		int box1index = children.indexOf(box1);
		clearPast(children, box1index);
		if(customConditionBox == null) {
			customConditionBox = new CustomConditionBox(this.flow);
		}
		children.add(customConditionBox);
		isOnPremade = false;
	}
	
	public static void clearPast(List<?> list, int index) {
		clearPast(list, index, false);
	}
	public static void clearPast(List<?> list, int index, boolean inclusive) {
		if(index < 0) {
			throw new IllegalArgumentException("index = " + index);
		}
		int realIndex = inclusive ? index : index + 1;
		while(list.size() > realIndex) {
			list.remove(list.size() - 1);
		}
	}
	@Override
	public Condition build() {
		Condition cond = null;
		if(isOnPremade) {
			try {
				cond = (Condition) premadeConditionBox.getValue().get(null);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			cond = customConditionBox.build();
		}
		if(cond == null) {
			throw new NullPointerException();
		}
		cond.setDefault(defaultValueChoiceBox.getValue().booleanValue());
		if(invertedCheckBox.isSelected()) {
			cond = cond.not();
		}
		return cond;
	}
	
	@Override
	public boolean verifyInput() {
		if(box1.getSelectionModel().isEmpty()) {
			submitErrorMessage("Condition type (Premade/Custom) has not been selected.");
			return false;
		}
		
		if(isOnPremade) {
			if(premadeConditionBox.getSelectionModel().isEmpty()) {
				submitErrorMessage("Premade condition has not been selected.");
				return false;
			}
			else {
				return true;
			}
		}
		else {
			boolean result = customConditionBox.verifyInput();
			return result;
		}
		
	}

	@Override
	public Pane getNodeToAddTo() {
		return nodeToAddTo;
	}

	@Override
	public void setNodeToAddTo(Pane node) {
		this.nodeToAddTo = node;
	}

	@Override
	public void submitErrorMessage(String message) {
		((ErrorSubmitable) nodeToAddTo).submitErrorMessage(message);
	}

	@Override
	public ConditionBoxWrap getWrap() {
		return conditionBoxWrap;
	}
	@Override
	public String toString() {
		return "[ConditionBox@"+hashCode()+", children="+getChildren()+"]";
	}
}