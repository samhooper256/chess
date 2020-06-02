package chess.base;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chess.util.Action;
import chess.util.ActionTree;
import chess.util.Condition;
import chess.util.User;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

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
		
		ObservableList<MenuItem> items = addActionButton.getItems();
		for(Class<? extends Action> actionType : actionTypes) {
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
			/*
			if(subActionTypes.size() == 1) {
				mi = new MenuItem(actionName);
				Class<? extends Action> type = subActionTypes.get(0);
				mi.setOnAction(actionEvent -> addActionToMainTree(type));
			}
			else {*/
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
				subMi.setOnAction(actionEvent -> addActionToMainTree(subActionType));
				subItems.add(subMi);
			}
			/*}*/	
			items.add(mi);
		}
		
		baseContent.getChildren().add(actionTreeTitledPane);
		bcScrollPane = new ScrollPane(baseContent);
		baseContent.minWidthProperty().bind(bcScrollPane.widthProperty());
		this.getChildren().addAll(bcScrollPane);
	}
	
	private void addActionToMainTree(Class<? extends Action> clazz) {
		ObservableList<Node> children = actionTreeVBox.getChildren();
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
		VBox vBox = new VBox();
		ActionTP newActionTP = new ActionTP(title, vBox, creationMethod);
		
		
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
		private Method creationMethod;
		private VBox vBox;
		private String[] paramNames;
		private Parameter[] params;
		public ActionTP(String name, VBox content, Method creationMethod) {
			super(name, content);
			this.vBox = content;
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
			
			ActionTree.Node atNode = new ActionTree.Node(action);
			return atNode;
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
