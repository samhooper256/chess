package chess.piecebuilder;

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
import chess.util.ActionTree.Choke;
import chess.util.Condition;
import chess.util.InputVerification;
import chess.util.MoveAndCaptureAction;
import chess.util.MultiAction;
import chess.util.RelativeJumpAction;
import chess.util.StoppableAction;
import chess.util.SubMulti;
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

public class ActionTreeBuilder extends StackPane implements InputVerification, ErrorSubmitable, Buildable<ActionTree>{
	private static List<Class<? extends Action>> actionTypes = Action.getImmediateSubtypes();
	
	private TitledPane actionTreeTitledPane;
	private VBox baseContent, actionTreeVBox; //actionTreeVBox is the content of the actionTreeTitledPane
	private MenuButton addActionButton;
	private ScrollPane bcScrollPane;
	private PieceBuilder pieceBuilder;
	private Button addBottleneckButton;
	
	public ActionTreeBuilder(PieceBuilder pieceBuilder) {
		super();
		this.pieceBuilder = pieceBuilder;
		baseContent = new VBox();
		baseContent.setFillWidth(true);
		actionTreeVBox = new VBox(10);
		actionTreeTitledPane = new TitledPane("Action Tree", actionTreeVBox);
		actionTreeVBox.setPadding(new Insets(10,0,10,10));
		
		addActionButton = new MenuButton("Add Action");
		setupAddActionButton(addActionButton, actionTreeVBox.getChildren());
		addBottleneckButton = new Button("Add Bottleneck");
		addBottleneckButton.setOnAction(actionEvent -> ActionTreeBuilder.this.addBottleneck(actionTreeVBox.getChildren(), addActionButton));
		actionTreeVBox.getChildren().addAll(addActionButton, addBottleneckButton);
		
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
				subMi.setOnAction(actionEvent -> addActionTo(whereToAddActionTPs, subActionType, button, childrenPossible));
				subItems.add(subMi);
			}
			items.add(mi);
		}
	}
	
	private void addActionTo(ObservableList<Node> children, Class<? extends Action> clazz, Node addBefore, boolean childrenPossible) {
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
		children.add(children.lastIndexOf(addBefore), newActionTP);
	}
	
	private void addBottleneck(ObservableList<Node> children, Node addBefore) {
		children.add(children.lastIndexOf(addBefore), new ChokeTP());
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
	
	@Override
	public ActionTree build() {
		ActionTree tree = new ActionTree();
		for(Node fxNode : actionTreeVBox.getChildren()) {
			if(fxNode instanceof Buildable) {
				Object builtObject = ((Buildable<?>) fxNode).build();
				if(builtObject instanceof ActionTree.TreeNode) {
					tree.addPrimaryNode((ActionTree.TreeNode) builtObject);
				}
				else {
					throw new UnsupportedOperationException("Unrecognized Buildable: " + fxNode);
				}
			}
		}
		return tree;
	}
	
	public void reset() {
		actionTreeVBox.getChildren().clear();
		actionTreeVBox.getChildren().add(addActionButton);
		actionTreeVBox.getChildren().add(addBottleneckButton);
	}

	private class ActionTP extends TitledPane implements InputVerification, Buildable<ActionTree.Node>{
		protected Method creationMethod;
		protected VBox vBox; //content of this TitledPane
		protected String[] paramNames;
		protected Parameter[] params;
		protected ChildTP childPane;
		protected ConditionTP conditionPane, stopConditionPane;
		protected Button deleteActionButton;
		public ActionTP(String name, VBox content, Method creationMethod, boolean childrenPossible) {
			super();
			this.setText(name);
			this.vBox = content;
			this.setContent(vBox);
			vBox.setPadding(new Insets(10,0,10,10));
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
						IntInputHBox hBox = new IntInputHBox(paramNames[i], pieceBuilder);
						vBox.getChildren().add(hBox);
					}
					else if(p.getType() == boolean.class) {
						BooleanInputHBox hBox = new BooleanInputHBox(paramNames[i]);
						vBox.getChildren().add(hBox);
					}
					else if(p.getType() == ArrayList.class && ((ParameterizedType) p.getParameterizedType()).getActualTypeArguments()[0] == String.class) {
						PieceOptionsInputHBox hBox= new PieceOptionsInputHBox(paramNames[i], pieceBuilder);
						vBox.getChildren().add(hBox);
					}
					else {
						System.err.println("unsupported type");
					}
					
				}
			}
			conditionPane = new ConditionTP(pieceBuilder);
			vBox.getChildren().add(conditionPane);
			if(StoppableAction.class.isAssignableFrom(creationMethod.getReturnType())) {
				stopConditionPane = new ConditionTP("Stop Conditions", pieceBuilder);
				vBox.getChildren().add(stopConditionPane);
			}
			else {
				stopConditionPane = null;
			}
			
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
		
		@Override
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
				else if(n instanceof PieceOptionsInputHBox) {
					PieceOptionsInputHBox listInput = (PieceOptionsInputHBox) n;
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
			if(stopConditionPane != null && action instanceof StoppableAction) {
				((StoppableAction) action).stops(stopConditionPane.build());
			}
			return action;
		}
	}
	
	private class ChokeTP extends TitledPane implements InputVerification, Buildable<ActionTree.Choke>{
		private VBox vBox; //the content of this TitledPane
		private Button deleteChokeButton;
		private ConditionTP conditionTP;
		private ChildTP childTP;
		public ChokeTP() {
			vBox = new VBox(4);
			this.setContent(vBox);
			this.setText("Bottleneck");
			conditionTP = new ConditionTP(pieceBuilder);
			childTP = new ChildTP();
			deleteChokeButton = new Button("Delete Bottleneck");
			deleteChokeButton.setStyle("-fx-background-color: transparent; -fx-border-width: 1px; -fx-border-color: #b00000;"
					+ "-fx-border-radius: 6; -fx-text-fill: #b00000;"); //TODO Put this in CSS (and add hover effect)
			deleteChokeButton.setOnMouseClicked(mouseEvent -> {
				((Pane) ChokeTP.this.getParent()).getChildren().remove(ChokeTP.this);
			});
			
			Pane deleteChokeButtonWrap = new HBox(deleteChokeButton);
			deleteChokeButtonWrap.setPadding(new Insets(10,0,0,0));
			
			vBox.getChildren().addAll(conditionTP, childTP, deleteChokeButtonWrap);
		}
		@Override
		public ActionTree.Choke build() {
			return new ActionTree.Choke(conditionTP.build(), childTP.build());
		}

		@Override
		public boolean verifyInput() {
			//TODO - does this need more work?
			return conditionTP.verifyInput() & childTP.verifyInput(); //single & on purpose
		}
		
	}
	
	private class MultiActionTP extends ActionTP{
		protected SubActionsTP subTP;
		
		public MultiActionTP(String name, VBox content, Method creationMethod) {
			super(name, content, creationMethod, true);
			subTP = new SubActionsTP(pieceBuilder);
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
			List<Pair<SubMulti, Boolean>> subs = subTP.build();
			for(Pair<SubMulti, Boolean> p : subs) {
				action.addAction(p.get1(), p.get2().booleanValue());
			}
			return atNode;
		}
	}
	
	private class ChildTP extends TitledPane implements InputVerification, Buildable<List<ActionTree.TreeNode>>{
		private VBox vBox; //content of this ChildTP
		private MenuButton addActionButton;
		private Button addBottleneckButton;
		public ChildTP() {
			super();
			this.setText("Children");
			vBox = new VBox(10);
			vBox.setPadding(new Insets(10,0,10,10));
			addActionButton = new MenuButton("Add action");
			setupAddActionButton(addActionButton, vBox.getChildren());
			addBottleneckButton = new Button("Add Bottleneck");
			addBottleneckButton.setOnAction(actionEvent -> addBottleneck(vBox.getChildren(), addActionButton));
			vBox.getChildren().addAll(addActionButton, addBottleneckButton);
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
		
		@Override
		public List<ActionTree.TreeNode> build(){
			List<ActionTree.TreeNode> end = new ArrayList<>();
			for(Node fxNode : vBox.getChildren()) {
				if(fxNode instanceof Buildable) {
					Object builtObject = ((Buildable<?>) fxNode).build();
					if(builtObject instanceof ActionTree.TreeNode) {
						end.add((ActionTree.TreeNode) builtObject);
					}
					else {
						throw new UnsupportedOperationException("Unrecognized Buildable: " + fxNode);
					}
				}
			}
			return end;
		}

	}
	
	@Override
	public void submitErrorMessage(String message) {
		pieceBuilder.submitErrorMessage(message);
	}
}
