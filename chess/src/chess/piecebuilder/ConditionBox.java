package chess.piecebuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import chess.util.AFC;
import chess.util.CombinerCondition;
import chess.util.Condition;
import chess.util.ConditionClass;
import chess.util.DoublePathed;
import chess.util.InputVerification;
import chess.util.NOTCondition;
import chess.util.PathBase;
import chess.util.PathedWith;
import chess.util.SinglePathed;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class ConditionBox extends VBox implements InputVerification, MultiConditionPart{
	private Label conditionNameLabel;
	ChoiceBox<String> box1;
	public static StringConverter<? extends Member> memberStringConverter = new StringConverter<>() {

		@Override
		public String toString(Member member) {
			if(member instanceof Field && ((Field) member).isAnnotationPresent(AFC.class)) {
				return ((Field) member).getAnnotation(AFC.class).name();
			}
			else if(member instanceof Method && ((Method) member).isAnnotationPresent(AFC.class)) {
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
	PremadeConditionBox premadeConditionBox;
	private boolean isOnPremade;
	private FlowPane flow;
	private FlowPane settingsFlow;
	private HBox defaultValueHBox, invertedHBox;
	private ChoiceBox<Boolean> defaultValueChoiceBox;
	private CheckBox invertedCheckBox;
	private volatile boolean shouldAddDrop;
	private ChangeListener<Number> box1Listener = new ChangeListener<Number>() {
		@Override
		public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
			String choice = box1.getItems().get((Integer) number2);
        	if(choice.equals("Premade")) {
        		setupForPremade();
        	}
        	else if(choice.equals("Custom")) {
        		setupForCustom(shouldAddDrop);
        	}
		}
	};
	
	public ConditionBox() {
		super();
		this.setFillWidth(true);
		this.getStyleClass().addAll("multi-condition-part", "condition-box");
		flow = new FlowPane();
		shouldAddDrop = true;
		conditionNameLabel = new Label("Condition: ");
		box1 = new ChoiceBox<>();
		box1.getItems().addAll("Premade", "Custom");
		box1.setValue("Select Type");
		box1.getSelectionModel().selectedIndexProperty().addListener(box1Listener);
		
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
			premadeConditionBox = new PremadeConditionBox();
		}
		children.add(premadeConditionBox);
		isOnPremade = true;
	}
	
	private void setupForCustom(boolean addDrop) {
		System.out.println("setup for custom called with addDrop="+addDrop);
		ObservableList<Node> children = flow.getChildren();
		int box1index = children.indexOf(box1);
		clearPast(children, box1index);
		customConditionBox = new CustomConditionBox(this.flow, addDrop);
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
			PieceBuilder.submitError("Condition type (Premade/Custom) has not been selected.");
			return false;
		}
		
		if(isOnPremade) {
			if(premadeConditionBox.getSelectionModel().isEmpty()) {
				PieceBuilder.submitError("Premade condition has not been selected.");
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
	
	public static MultiConditionPart reconstruct(Condition con) {
		System.out.println("reconstructin con:"+con);
		if(con.isPremade()) {
			ConditionBox conditionBox = new ConditionBox();
			conditionBox.box1.getSelectionModel().select("Premade");
			for(Field f : conditionBox.premadeConditionBox.getItems()) {
				try {
					if(f.get(null) == con) {
						conditionBox.premadeConditionBox.getSelectionModel().select(f);
						break;
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
					break;
				}
			}
			conditionBox.defaultValueChoiceBox.getSelectionModel().select(con.getDefault());
			return conditionBox;
		}
		else if(con instanceof NOTCondition) {
			MultiConditionPart part = ConditionBox.reconstruct(((NOTCondition) con).getNottedCondition());
			if(part instanceof ConditionBox) {
				((ConditionBox) part).invertedCheckBox.setSelected(true);
				((ConditionBox) part).defaultValueChoiceBox.getSelectionModel().select(con.getDefault());
			}
			else {
				throw new UnsupportedOperationException("Cannot support : " + part); //Should never happen.
			}
			return part;
		}
		else if(con instanceof CombinerCondition) {
			MultiConditionBox multiCB = new MultiConditionBox(
					con.getCreationMethod(),
				(Node & MultiConditionPart) ConditionBox.reconstruct(((CombinerCondition) con).get1()),
				(Node & MultiConditionPart) ConditionBox.reconstruct(((CombinerCondition) con).get2())
			);
			
			return multiCB;
			
		}
		else {
			ConditionBox conditionBox = new ConditionBox();
			conditionBox.shouldAddDrop = false;
			conditionBox.box1.getSelectionModel().select("Custom");
			conditionBox.shouldAddDrop = true;
			conditionBox.defaultValueChoiceBox.getSelectionModel().select(con.getDefault());
			CustomConditionBox ccb = conditionBox.customConditionBox;
			Method creationMethod = con.getCreationMethod();
			if(con instanceof SinglePathed) {
				PathBase path1 = ((SinglePathed) con).getPath();
				PathBuilder builder1 = PathBuilder.reconstruct(path1);
				BuildFinisher finisher = BuildFinisher.reconstruct(builder1, creationMethod);
				ccb.getChildren().addAll(builder1, finisher);
				ccb.buildFinisher = finisher;
				return conditionBox;
			}
			else if(con instanceof DoublePathed) {
				PathBase path1 = ((DoublePathed) con).getPath1();
				PathBase path2 = ((DoublePathed) con).getPath2();
				PathBuilder builder1 = PathBuilder.reconstruct(path1);
				BuildFinisher finisher = BuildFinisher.reconstruct(builder1, creationMethod);
				PathBuilder builder2 = PathBuilder.reconstruct(path2);
				ccb.buildFinisher = finisher;
				ccb.getChildren().addAll(builder1, finisher, builder2);
				return conditionBox;
			}
			else if(con instanceof PathedWith<?>) {
				PathBase path1 = ((PathedWith<?>) con).getPath();
				PathBuilder builder1 = PathBuilder.reconstruct(path1);
				BuildFinisher finisher = BuildFinisher.reconstruct(builder1, creationMethod);
				Object with = ((PathedWith<?>) con).getWith();
				final Node part2;
				if(with instanceof Class<?>) {
					part2 = new ConditionActionChooser();
					((ConditionActionChooser) part2).getSelectionModel().select((Class<?>) with);
				}
				else if(with instanceof String) {
					part2 = new ConditionPieceChooser();
					((ConditionPieceChooser) part2).getSelectionModel().select((String) with);
				}
				else {
					throw new UnsupportedOperationException("Cannot reconstruct PathWith with generic type: " + with.getClass());
				}
				ccb.buildFinisher = finisher;
				ccb.getChildren().addAll(builder1, finisher, part2);
				return conditionBox;
			}
			else {
				throw new UnsupportedOperationException("Custom cons cannot be reconstructed yet: " + con);
			}
		}
	}
	
	@Override
	public String toString() {
		return "[ConditionBox@"+hashCode()+", children="+getChildren()+"]";
	}
}