package chess.base;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import chess.util.Action;
import chess.util.ActionTree;
import chess.util.Condition;
import chess.util.MoveAndCaptureAction;
import chess.util.MultiAction;
import chess.util.RelativeJumpAction;
import chess.util.User;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class ActionTreeBuilder extends StackPane implements InputVerification{
	private static List<Class<? extends Action>> actionTypes = Action.getImmediateSubtypes();
	
	private TitledPane actionTreeTitledPane;
	private VBox baseContent, actionTreeVBox;
	private MenuButton addActionButton;
	private ScrollPane bcScrollPane;
	private PieceBuilder pieceBuilder;
	
	public ActionTreeBuilder(PieceBuilder pieceBuilder) {
		super();
		this.pieceBuilder = pieceBuilder;
		baseContent = new VBox();
		baseContent.setFillWidth(true);
		actionTreeVBox = new VBox(10);
		actionTreeTitledPane = new TitledPane("Action Tree", actionTreeVBox);
		addActionButton = new MenuButton("Add action");
		actionTreeVBox.getChildren().addAll(addActionButton);
		
		setupAddActionButton(addActionButton, actionTreeVBox.getChildren());
		
		baseContent.getChildren().add(actionTreeTitledPane);
		bcScrollPane = new ScrollPane(baseContent);
		baseContent.minWidthProperty().bind(bcScrollPane.widthProperty());
		this.getChildren().addAll(bcScrollPane);
	}
	
	private void setupAddActionButton(MenuButton button, ObservableList<Node> whereToAddActionTPs) {
		setupAddActionButton(button, whereToAddActionTPs, true, true);
	}
	private void setupAddActionButton(MenuButton button, ObservableList<Node> whereToAddActionTPs, boolean multisAllowed, boolean childrenPossible) {
		ObservableList<MenuItem> items = button.getItems();
		for(Class<? extends Action> actionType : actionTypes) {
			if(!multisAllowed && actionType == MultiAction.class) {
				continue;
			}
			String actionName = null;
			List<Class<? extends Action>> subActionTypes = null;
			try {
				actionName = (String) actionType.getMethod("getActionName").invoke(null);
				subActionTypes = (List<Class<? extends Action>>) actionType.getMethod("getImmediateSubtypes").invoke(null);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MenuItem mi = null;
			mi = new Menu(actionName);
			ObservableList<MenuItem> subItems = ((Menu) mi).getItems();
			for(Class<? extends Action> subActionType : subActionTypes) {
				String actionVariant = null;
				try {
					actionVariant = (String) subActionType.getMethod("getVariant").invoke(null);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				MenuItem subMi = new MenuItem(actionVariant);
				subMi.setOnAction(actionEvent -> addActionTo(whereToAddActionTPs, subActionType, childrenPossible));
				subItems.add(subMi);
			}
			items.add(mi);
		}
	}
	
	private void addActionTo(ObservableList<Node> children, Class<? extends Action> clazz, boolean childrenPossible) {
		String title = "???";
		try {
			title = 
				(String) clazz.getMethod("getVariant").invoke(null) + " " +
				(String) clazz.getMethod("getActionName").invoke(null);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Method creationMethod = null;
		try {
			creationMethod = (Method) clazz.getMethod("getCreationMethod").invoke(null);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		VBox vBox = new VBox(4);
		ActionTP newActionTP;
		if(MultiAction.class.isAssignableFrom(clazz)) {
			newActionTP = new MultiActionTP(title, vBox, creationMethod);
		}
		else {
			newActionTP = new ActionTP(title, vBox, creationMethod, childrenPossible);
		}
		children.add(children.size() - 1, newActionTP);
	}
	
	@Override
	public boolean verifyInput() {
		boolean result = true;
		for(Node fxNode : actionTreeVBox.getChildren()) {
			if(fxNode instanceof InputVerification) {
				if(!((InputVerification) fxNode).verifyInput()) {
					result = false; //don't return right away so user sees all error messages at once.
				}
			}
		}
		return result;
	}
	public ActionTree build() {
		ActionTree tree = new ActionTree();
		for(Node fxNode : actionTreeVBox.getChildren()) {
			if(fxNode instanceof ActionTP) {
				ActionTP actionTP = (ActionTP) fxNode;
				tree.addPrimaryNode(actionTP.build());
			}
		}
		return tree;
	}
	
	public void reset() {
		actionTreeVBox.getChildren().clear();
		actionTreeVBox.getChildren().add(addActionButton);
	}
	
	
	private static boolean isInteger(String s, int radix) {
	    if(s.isEmpty()) return false;
	    for(int i = 0; i < s.length(); i++) {
	        if(i == 0 && s.charAt(i) == '-') {
	            if(s.length() == 1) return false;
	            else continue;
	        }
	        if(Character.digit(s.charAt(i),radix) < 0) return false;
	    }
	    return true;
	}
	
	
	private class ActionTP extends TitledPane implements InputVerification{
		protected Method creationMethod;
		protected VBox vBox;
		protected String[] paramNames;
		protected Parameter[] params;
		protected ChildTP childPane;
		protected ConditionTP conditionPane;
		protected Button deleteActionButton;
		public ActionTP(String name, VBox content, Method creationMethod, boolean childrenPossible) {
			super();
			this.setText(name);
			this.vBox = content;
			this.setContent(vBox);
			this.creationMethod = creationMethod;
			Annotation userAnnotation = creationMethod.getAnnotation(User.class);
			paramNames = null;
			params = creationMethod.getParameters();
			try {
				paramNames = (String[]) userAnnotation.annotationType().getMethod("params").invoke(userAnnotation);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for(int i = 0; i < params.length; i++) {
				Parameter p = params[i];
				if(p.getType() == Condition[].class || p.getType() == Condition.class) {
					continue;
				}
				else {
					if(p.getType() == int.class) {
						IntInputHBox hBox = new IntInputHBox(paramNames[i]);
						vBox.getChildren().add(hBox);
					}
					else if(p.getType() == boolean.class) {
						BooleanInputHBox hBox = new BooleanInputHBox(paramNames[i]);
						vBox.getChildren().add(hBox);
					}
					else if(p.getType() == ArrayList.class && ((ParameterizedType) p.getParameterizedType()).getActualTypeArguments()[0] == String.class) {
						StringArrayListInputHBox hBox= new StringArrayListInputHBox(paramNames[i]);
						vBox.getChildren().add(hBox);
					}
					else {
						System.err.println("unsupported type");
					}
					
				}
			}
			conditionPane = new ConditionTP();
			vBox.getChildren().add(conditionPane);
			if(childrenPossible && ActionTree.supportsChildren((Class<? extends Action>) creationMethod.getReturnType())) {
				childPane = new ChildTP();
				vBox.getChildren().add(childPane);
			}
			else {
				childPane = null;
			}
			
			deleteActionButton = new Button("Delete Action");
			deleteActionButton.setStyle("-fx-background-color: transparent; -fx-border-width: 1px; -fx-border-color: #b00000;"
					+ "-fx-border-radius: 6; -fx-text-fill: #b00000;"); //TODO Put this in CSS (and add hover effect)
			deleteActionButton.setOnMouseClicked(mouseEvent -> {
				((Pane) ActionTP.this.getParent()).getChildren().remove(ActionTP.this);
			});
			
			Pane deleteActionButtonWrap = new HBox(deleteActionButton);
			deleteActionButtonWrap.setPadding(new Insets(10,0,0,0));

			vBox.getChildren().add(deleteActionButtonWrap);
		}
		
		@Override 
		public boolean verifyInput() {
			boolean result = true;
			for(Node fxNode : vBox.getChildren()) {
				if(fxNode instanceof InputVerification) {
					if(!((InputVerification) fxNode).verifyInput()) {
						result = false; //don't return right away so that the user gets all the error messages at once.
					}
				}
			}
			return result;
		}
		
		public Method getCreationMethod() {
			return creationMethod;
		}
		
		public ActionTree.Node build() {
			Action action = buildAction();
			
			//System.out.println("action = " + action);
			ActionTree.Node atNode = new ActionTree.Node(action);
			if(childPane != null) {
				atNode.addAllChildren(childPane.build());
			}
			
			return atNode;
		}
		
		public Action buildAction() {
			Object[] actualParams = new Object[creationMethod.getParameterCount()];
			int apIndex = 0;
			for(Node n : vBox.getChildren()) {
				if(n instanceof IntInputHBox) {
					IntInputHBox intInput = (IntInputHBox) n;
					actualParams[apIndex] = intInput.getInt();
					apIndex++;
				}
				else if(n instanceof BooleanInputHBox) {
					BooleanInputHBox booleanInput = (BooleanInputHBox) n;
					actualParams[apIndex] = booleanInput.getBoolean();
					apIndex++;
				}
				else if(n instanceof StringArrayListInputHBox) {
					StringArrayListInputHBox listInput = (StringArrayListInputHBox) n;
					actualParams[apIndex] = listInput.getArrayList();
					apIndex++;
				}
			}
			//TODO : CONDITIONS
			if(apIndex == actualParams.length - 1) {
				actualParams[apIndex] = new Condition[0];
			}
			else {
				throw new IllegalArgumentException("Oh no: " + Arrays.deepToString(actualParams) + " ; apIndex = " + apIndex);
			}
			Action action = null;
			try {
				action = (Action) creationMethod.invoke(null, actualParams);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			action.addAllConditions(conditionPane.build());
			return action;
		}
	}
	
	private class MultiActionTP extends ActionTP{
		protected SubActionTP subTP;
		public MultiActionTP(String name, VBox content, Method creationMethod) {
			super(name, content, creationMethod, true);
			subTP = new SubActionTP();
			ObservableList<Node> children = super.vBox.getChildren();
			int ctpindex = children.indexOf(conditionPane);
			if(ctpindex >= 0) {
				children.add(ctpindex, subTP);
			}
			else {
				children.add(children.size() - 1, subTP);
			}
		}
		
		@Override 
		public ActionTree.Node build(){
			ActionTree.Node atNode = super.build();
			MultiAction action = (MultiAction) atNode.getAction();
			Pair<List<Action>, List<Boolean>> subs = subTP.build();
			action.addAllActions(subs.get1());
			return atNode;
		}
	}
	
	private class SubActionTP extends TitledPane implements InputVerification{
		private VBox vBox;
		private MenuButton addActionButton;
		public SubActionTP() {
			super();
			vBox = new VBox(10);
			addActionButton = new MenuButton("Add action");
			setupAddActionButton(addActionButton, vBox.getChildren(), false, false);
			vBox.getChildren().add(addActionButton);
			this.setText("Sub-Actions");
			this.setContent(vBox);
			this.setExpanded(false);
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
		
		public Pair<List<Action>, List<Boolean>> build(){
			List<Action> end = new ArrayList<>();
			for(Node fxNode : vBox.getChildren()) {
				if(fxNode instanceof ActionTP) {
					end.add(((ActionTP) fxNode).buildAction());
				}
			}
			List<Boolean> states = new ArrayList<>(end.size());
			return new Pair<>(end, null); //TODO ADD STATES
		}
	}
	
	private class ConditionTP extends TitledPane implements InputVerification{
		private VBox vBox;
		private Button addConditionButton;
		public ConditionTP() {
			super();
			vBox = new VBox(10);
			addConditionButton = new Button("Add condition");
			addConditionButton.setOnMouseClicked(mouseEvent -> {
				ConditionBox conditionBox = new ConditionBox(pieceBuilder);
				vBox.getChildren().add(vBox.getChildren().size() - 1, conditionBox);
			});
			vBox.getChildren().add(addConditionButton);
			this.setText("Conditions");
			this.setContent(vBox);
			this.setExpanded(false);
		}
		
		public Collection<Condition> build(){
			ArrayList<Condition> end = new ArrayList<>(this.getChildren().size());
			for(Node fxNode : vBox.getChildren()) {
				if(fxNode instanceof ConditionBox) {
					end.add(((ConditionBox) fxNode).build());
				}
			}
			System.out.println("Condition Collection returned: " + end);
			return end;
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
		
	}
	
	private class ChildTP extends TitledPane implements InputVerification{
		private VBox vBox;
		private MenuButton addActionButton;
		public ChildTP() {
			super();
			this.setText("Children");
			vBox = new VBox(10);
			addActionButton = new MenuButton("Add action");
			setupAddActionButton(addActionButton, vBox.getChildren());
			vBox.getChildren().add(addActionButton);
			this.setContent(vBox);
			this.setExpanded(false);
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
		
		public List<ActionTree.TreeNode> build(){
			List<ActionTree.TreeNode> end = new ArrayList<>();
			for(Node fxNode : vBox.getChildren()) {
				if(fxNode instanceof ActionTP) {
					end.add(((ActionTP) fxNode).build());
				}
			}
			return end;
		}
	}
	
	private class IntInputHBox extends HBox implements InputVerification{
		private static final int SPACING = 5;
		private TextField textField;
		public IntInputHBox(String parameterName) {
			super(SPACING);
			this.setAlignment(Pos.CENTER_LEFT);
			this.getChildren().add(new Label(String.format("%s (integer): ", parameterName)));
			textField = new TextField();
			this.getChildren().add(textField);
		}
		
		public boolean verifyInput() {
			System.out.println("Verifying int input");
			boolean result = isInteger(textField.getText().strip(), 10);
			if(!result) {
				pieceBuilder.submitErrorMessage(((Label) this.getChildren().get(0)).getText() + " is invalid");
			}
			return result;
		}
		
		//isValid should be called right before this to avoid an exception
		public int getInt() {
			return Integer.parseInt(textField.getText().strip());
		}
	}
	
	//Does not implement InputVerification because input will always be valid
	private class BooleanInputHBox extends HBox{
		private static final int SPACING = 5;
		private CheckBox checkBox;
		public BooleanInputHBox(String parameterName) {
			super(SPACING);
			this.setAlignment(Pos.CENTER_LEFT);
			this.getChildren().add(new Label(String.format("%s: ", parameterName)));
			checkBox = new CheckBox();
			this.getChildren().add(checkBox);
		}
		
		public boolean getBoolean() {
			return checkBox.isSelected();
		}
	}
	
	private class StringArrayListInputHBox extends HBox implements InputVerification{
		private static final int SPACING = 5;
		private TilePane tilePane;
		public StringArrayListInputHBox(String parameterName) {
			super(SPACING);
			this.setAlignment(Pos.CENTER_LEFT);
			tilePane = new TilePane();
			tilePane.setSnapToPixel(true);
			tilePane.setTileAlignment(Pos.CENTER_LEFT);
			tilePane.setVgap(5);
			tilePane.setHgap(5);
			ObservableList<Node> tilePaneChildren = tilePane.getChildren();
			for(String s : pieceBuilder.currentPieceNames) {
				if(!s.equals("King")) {
					tilePaneChildren.add(new CheckBox(s));
				}
			}
			this.getChildren().addAll(new Label(String.format("%s: ", parameterName)), tilePane);
		}
		
		public ArrayList<String> getArrayList() {
			ArrayList<String> end = new ArrayList<>();
			for(Node fxNode : tilePane.getChildren()) {
				CheckBox cb = (CheckBox) fxNode;
				if(cb.isSelected()) {
					end.add(cb.getText());
				}
			}
			return end;
		}

		@Override
		public boolean verifyInput() {
			for(Node fxNode : tilePane.getChildren()) {
				CheckBox cb = (CheckBox) fxNode;
				if(cb.isSelected()) {
					return true;
				}
			}
			pieceBuilder.submitErrorMessage(((Label) this.getChildren().get(0)).getText() + " needs at least one piece");
			return false;
		}
	}
}
