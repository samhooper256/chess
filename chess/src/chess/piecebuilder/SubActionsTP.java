package chess.piecebuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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

public class SubActionsTP extends TitledPane implements InputVerification, Buildable<List<Pair<SubMulti, Boolean>>>{
	
	private VBox vBox; //content of this SubActiosnTP
	private MenuButton addActionButton;
	public SubActionsTP() {
		super();
		vBox = new VBox(10);
		vBox.setPadding(new Insets(10,0,10,10));
		addActionButton = new MenuButton("Add action");
		setupAddActionButton();
		vBox.getChildren().add(addActionButton);
		this.setText("Sub-Actions");
		this.setContent(vBox);
		this.setExpanded(false);
	}
	
	private void setupAddActionButton() {
		ObservableList<MenuItem> items = addActionButton.getItems();
		for(Method m : SubMulti.subMultiCreationMethods) {
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
	
	public static void reconstruct(Pair<List<SubMulti>, List<Boolean>> subData, SubActionsTP whereToAdd) {
		int addIndex = 0;
		List<SubMulti> actions = subData.get1();
		List<Boolean> states = subData.get2();
		if(actions.size() != states.size()) {
			throw new IllegalArgumentException("actions.size() != states.size() for MultiAction");
		}
		Iterator<SubMulti> actionsIterator = actions.iterator();
		Iterator<Boolean> statesIterator = states.iterator();
		while(actionsIterator.hasNext()) {
			SubMulti nextAction = actionsIterator.next();
			Boolean nextState = statesIterator.next();
			whereToAdd.vBox.getChildren().add(addIndex, reconstruct(nextAction, nextState));
			addIndex++;
		}
	}
	
	private static SubActionTP reconstruct(SubMulti action, Boolean state) {
		SubActionTP subTP = null;
		try {
			subTP = new SubActionTP((Method) action.getClass().getMethod("getCreationMethod").invoke(null));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(subTP == null) {
			System.err.println("Failed reconstructing subActionTP;"
					+ " Could not find static method \"getCreationMethod()\" in class " + action.getClass());
			return null;
		}
		
		Object[] reconstructionParams = action.getReconstructionParameters();
		//System.out.println("recon params = " + Arrays.toString(reconstructionParams));
		ObservableList<Node> tpChildren = subTP.subVBox.getChildren();
		for(int rpIndex = 0, tpIndex = 0; rpIndex < reconstructionParams.length; rpIndex++) {
			//System.out.println("\tentered loop, rpIndex = " + rpIndex + ", tpIndex = " + tpIndex);
			if(reconstructionParams[rpIndex].getClass() == int.class || reconstructionParams[rpIndex].getClass() == Integer.class) {
				//System.out.println("twas an int");
				while(!(tpChildren.get(tpIndex) instanceof IntInputHBox)) {
					tpIndex++;
				}
				if(tpIndex >= tpChildren.size()) {
					throw new IllegalArgumentException("Could not find an IntInputHBox for "
							+ "reconstruction param at index " + rpIndex);
				}
				((IntInputHBox) tpChildren.get(tpIndex)).setValue((int) reconstructionParams[rpIndex]);
				tpIndex++;
			}
			else if(reconstructionParams[rpIndex].getClass() == boolean.class || reconstructionParams[rpIndex].getClass() == Boolean.class) {
				while(!(tpChildren.get(tpIndex) instanceof BooleanInputHBox)) {
					tpIndex++;
				}
				if(tpIndex >= tpChildren.size()) {
					throw new IllegalArgumentException("Could not find a BooleanInputHBox for "
							+ "reconstruction param at index " + rpIndex);
				}
				((BooleanInputHBox) tpChildren.get(tpIndex)).setValue((boolean) reconstructionParams[rpIndex]);
				tpIndex++;
			}
			else if(reconstructionParams[rpIndex].getClass() == ArrayList.class) {
				while(!(tpChildren.get(tpIndex) instanceof PieceOptionsInputHBox)) {
					tpIndex++;
				}
				if(tpIndex >= tpChildren.size()) {
					throw new IllegalArgumentException("Could not find a PieceOptionsInputHBox for "
							+ "reconstruction param at index " + rpIndex);
				}
				((PieceOptionsInputHBox) tpChildren.get(tpIndex)).selectAll((ArrayList<String>) reconstructionParams[rpIndex]);
			}
			else if(reconstructionParams[rpIndex].getClass() == Flag.class) {
				while(!(tpChildren.get(tpIndex) instanceof FlagInputHBox)) {
					tpIndex++;
				}
				if(tpIndex >= tpChildren.size()) {
					throw new IllegalArgumentException("Could not find a PieceOptionsInputHBox for "
							+ "reconstruction param at index " + rpIndex);
				}
				((FlagInputHBox) tpChildren.get(tpIndex)).setValue((Flag) reconstructionParams[rpIndex]);
			}
			else {
				System.err.println("unrecognized reconstruction param type: " + reconstructionParams[rpIndex].getClass());
			}
		}
		
		ConditionTP.reconstruct(action.getConditions(), subTP.conditionTP);
		
		subTP.necessaryHBox.setValue(state.booleanValue());
		
		return subTP;
	}
	
	private static class SubActionTP extends TitledPane implements InputVerification, Buildable<Pair<SubMulti, Boolean>>{
		private Method creationMethod;
		private AFC afc;
		private VBox subVBox; //content of this SubActionTP
		private BooleanInputHBox necessaryHBox;
		
		private ConditionTP conditionTP; //All submultis are guaranteed not to have child actions, stop conditions, or sub-actions.
		private Button deleteActionButton;
		private Pane deleteActionButtonWrap;
		/**
		 * 
		 * @param cm the creation method for this SubMulti type. It should be located within the {@link SubMulti}
		 * class.
		 */
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
			String[] paramNames = afc.paramDescriptions();
			for(; paramIndex < params.length; paramIndex++) {
				Parameter p = params[paramIndex];
				if(p.getType() == Condition[].class || p.getType() == Condition.class) {
					continue;
				}
				else {
					if(p.getType() == Flag.class) {
						FlagInputHBox hBox = new FlagInputHBox(4, "relative to:", Flag.ORIGIN, Flag.DESTINATION);
						subVBox.getChildren().add(hBox);
					}
					else if(p.getType() == int.class) {
						IntInputHBox hBox = new IntInputHBox(paramNames[paramIndex]);
						subVBox.getChildren().add(hBox);
					}
					else if(p.getType() == boolean.class) {
						BooleanInputHBox hBox = new BooleanInputHBox(paramNames[paramIndex]);
						subVBox.getChildren().add(hBox);
					}
					else if(p.getType() == ArrayList.class && ((ParameterizedType) p.getParameterizedType()).getActualTypeArguments()[0] == String.class) {
						PieceOptionsInputHBox hBox = new PieceOptionsInputHBox(paramNames[paramIndex]);
						subVBox.getChildren().add(hBox);
					}
					else {
						System.err.println("unsupported type: " + p.getType());
					}
				}
			}
			subVBox.getChildren().add(conditionTP = new ConditionTP());
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
				else if(child instanceof FlagInputHBox) {
					actualParams[apIndex++] = ((FlagInputHBox) child).getFlag();
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
}
