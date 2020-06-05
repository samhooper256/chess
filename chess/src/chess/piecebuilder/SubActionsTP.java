package chess.piecebuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chess.util.AFC;
import chess.util.Condition;
import chess.util.Flag;
import chess.util.InputVerification;
import chess.util.SubMulti;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class SubActionsTP extends TitledPane implements InputVerification, ErrorSubmitable, Buildable<List<Pair<SubMulti, Boolean>>>{
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
	
	private VBox vBox; //content of this SubActiosnTP
	private MenuButton addActionButton;
	private CheckBox necessaryCheckBox;
	private PieceBuilder pieceBuilder;
	public SubActionsTP(PieceBuilder pb) {
		super();
		this.pieceBuilder = pb;
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
		SubActionsTP.this.vBox.getChildren().add(SubActionsTP.this.vBox.getChildren().size() - 1, new SubActionTP(creationMethod));
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
	public List<Pair<SubMulti, Boolean>> build(){
		List<Pair<SubMulti, Boolean>> end = new ArrayList<>();
		for(Node fxNode : vBox.getChildren()) {
			if(fxNode instanceof SubActionTP) {
				end.add(((SubActionTP) fxNode).build());
			}
		}
		return end;
	}
	
	private class SubActionTP extends TitledPane implements InputVerification, Buildable<Pair<SubMulti, Boolean>>{
		private Method creationMethod;
		private AFC afc;
		private VBox subVBox;
		private BooleanInputHBox necessaryHBox;
		private ChoiceBox<Flag> flagChoice;
		private ConditionTP conditionTP; //All submultis are guaranteed not to have child actions, stop conditions, or sub-actions.
		private Button deleteActionButton;
		private Pane deleteActionButtonWrap;
		private HBox flagChoiceHBox;
		private SubActionTP(Method cm) {
			super();
			this.creationMethod = cm;
			this.afc = cm.getAnnotation(AFC.class);
			this.setText(afc.name());
			subVBox = new VBox(4);
			this.setContent(subVBox);
			necessaryHBox = new BooleanInputHBox("is necessary");
			subVBox.getChildren().add(necessaryHBox);
			Parameter[] params = cm.getParameters();
			int paramIndex = 0;
			if(params[0].getType() == Flag.class) {
				flagChoice = new ChoiceBox<>();
				flagChoice.getItems().addAll(Flag.ORIGIN, Flag.DESTINATION);
				flagChoiceHBox = new HBox(4);
				flagChoiceHBox.getChildren().addAll(new Label("relative to: "), flagChoice);
				flagChoiceHBox.setAlignment(Pos.CENTER_LEFT);
				subVBox.getChildren().add(flagChoiceHBox);
				paramIndex++;
			}
			else {
				flagChoice = null;
			}
			String[] paramNames = afc.paramDescriptions();
			for(; paramIndex < params.length; paramIndex++) {
				Parameter p = params[paramIndex];
				if(p.getType() == Condition[].class || p.getType() == Condition.class) {
					continue;
				}
				else {
					if(p.getType() == int.class) {
						IntInputHBox hBox = new IntInputHBox(paramNames[paramIndex], pieceBuilder);
						subVBox.getChildren().add(hBox);
					}
					else if(p.getType() == boolean.class) {
						BooleanInputHBox hBox = new BooleanInputHBox(paramNames[paramIndex]);
						subVBox.getChildren().add(hBox);
					}
					else if(p.getType() == ArrayList.class && ((ParameterizedType) p.getParameterizedType()).getActualTypeArguments()[0] == String.class) {
						PieceOptionsInputHBox hBox= new PieceOptionsInputHBox(paramNames[paramIndex], pieceBuilder);
						subVBox.getChildren().add(hBox);
					}
					else {
						System.err.println("unsupported type");
					}
				}
			}
			subVBox.getChildren().add(conditionTP = new ConditionTP(pieceBuilder));
			deleteActionButton = new Button("Delete Sub-Action");
			deleteActionButton.setStyle("-fx-background-color: transparent; -fx-border-width: 1px; -fx-border-color: #b00000;"
					+ "-fx-border-radius: 6; -fx-text-fill: #b00000;"); //TODO Put this in CSS (and add hover effect)
			deleteActionButton.setOnMouseClicked(mouseEvent -> {
				((Pane) SubActionTP.this.getParent()).getChildren().remove(SubActionTP.this);
			});
			
			deleteActionButtonWrap = new HBox(deleteActionButton);
			deleteActionButtonWrap.setPadding(new Insets(10,0,0,0));
			subVBox.getChildren().add(deleteActionButtonWrap);
			
		}
		@Override
		public Pair<SubMulti, Boolean> build() {
			boolean state = necessaryHBox.getBoolean();
			Object[] actualParams = new Object[creationMethod.getParameterCount()];
			int apIndex = 0;
			for(Node child : subVBox.getChildren()) {
				if(child == necessaryHBox || child == deleteActionButtonWrap) {
					continue;
				}
				else if(child == flagChoiceHBox) {
					actualParams[apIndex++] = flagChoice.getValue();
				}
				else if(child instanceof IntInputHBox) {
					actualParams[apIndex++] = ((IntInputHBox) child).getInt();
				}
				else if(child instanceof BooleanInputHBox) {
					actualParams[apIndex++] = ((BooleanInputHBox) child).getBoolean();
				}
				else if(child instanceof PieceOptionsInputHBox) {
					actualParams[apIndex++] = ((PieceOptionsInputHBox) child).getArrayList();
				}
				else if(child instanceof ConditionTP) {
					actualParams[apIndex++] = new Condition[0];
				}
				else {
					throw new IllegalArgumentException("Unrecognized child node in this SubActionTP's content: " + child +
							" at subVBox.getChildren.indexOf = " + subVBox.getChildren().indexOf(child));
				}
			}
			if(apIndex != actualParams.length) {
				throw new IllegalArgumentException("Invalid number of parameters in this SubActionTP's content, should be: "
						+ actualParams.length + ", was " + apIndex);
			}
			
			SubMulti subMulti = null;
			System.out.println("ACTUAL PARAMETERS found to be: " + Arrays.deepToString(actualParams));
			try {
				subMulti = (SubMulti) creationMethod.invoke(null, actualParams);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				//System.err.println("*****Error produced by invoking creatonMethod.invoke(null, actualParams)*****\n\n");
				e.printStackTrace();
				//System.err.println("\n\n*************\n");
			}
			if(subMulti == null) {
				throw new NullPointerException("Unable to create the SubMulti by invoking creationMethod.invoke(null, actualParams)\n" +
						"creation method=" + creationMethod +"\nactualParams="+Arrays.deepToString(actualParams));
			}
			
			subMulti.addAllConditions(conditionTP.build());
			return new Pair<>(subMulti, Boolean.valueOf(state));
		}
		@Override
		public boolean verifyInput() {
			boolean result = true;
			if(flagChoice != null && flagChoice.getSelectionModel().isEmpty()) {
				submitErrorMessage("relative to (in a Multi's Sub-Action): has no selection");
				result = false;
			}
			for(Node fxNode : subVBox.getChildren()) {
				if(fxNode instanceof InputVerification) {
					if(!((InputVerification) fxNode).verifyInput()) {
						result = false;
					}
				}
			}
			return result;
		}
		
		
		
	}

	@Override
	public void submitErrorMessage(String message) {
		pieceBuilder.submitErrorMessage(message);
	}
}
