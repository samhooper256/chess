package chess.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ActionOptionsDisplay extends StackPane{
	public VBox vBox;
	private Label text;
	private ScrollPane sp;
	private AnchorPane anchor;
	private ToggleGroup tg;
	private LegalAction selectedAction;
	private Accordion accordion;
	private Button confirm;
	
	private Object toNotify;
	
	public ActionOptionsDisplay() {
		super();
		vBox = new VBox(10);
		vBox.setPadding(new Insets(0,20,0,20));
		vBox.prefWidthProperty().bind(this.widthProperty());
		vBox.prefHeightProperty().bind(this.heightProperty());
		vBox.setAlignment(Pos.CENTER);
		text = new Label();
		text.setText("");
		
		tg = new ToggleGroup();
		
		accordion = new Accordion();
		accordion.prefWidthProperty().bind(vBox.widthProperty());
		sp = new ScrollPane(accordion);
		sp.setFitToWidth(true);
		
		confirm = new Button("Confirm");
		confirm.setDisable(true);
		confirm.setOnMouseClicked(x -> {
			loop:
			for(Iterator<TitledPane> itr = accordion.getPanes().iterator(); itr.hasNext();) {
				ActionTitledPane tp = (ActionTitledPane) itr.next();
				if(tp.isSelected()) {
					this.selectedAction = tp.getAction();
					break loop;
				}
			}
			ActionOptionsDisplay.this.setVisible(false);
			synchronized(toNotify) {
				toNotify.notifyAll();
			}
		});
		vBox.getChildren().addAll(text, sp, confirm);
		this.getChildren().add(vBox);
		
		anchor = new AnchorPane();
		anchor.setPickOnBounds(false);
		this.getChildren().add(anchor);
		
	}
	
	
	public boolean hasAction() {
		return selectedAction != null;
	}
	
	public LegalAction takeAction() {
		LegalAction action = selectedAction;
		selectedAction = null;
		return action;
	}
	
	public void setMessage(String message) {
		text.setText(message);
	}

	/* *
	 * MUST BE CALLED FROM FX THREAD.
	 */
	public void choose(Object notifyWhenDone, Collection<LegalAction> options) {
		toNotify = notifyWhenDone;
		confirm.setDisable(true);
		accordion.getPanes().clear();
		for(Iterator<LegalAction> itr = options.iterator(); itr.hasNext();) {
			LegalAction act = itr.next();
			accordion.getPanes().add(new ActionTitledPane(act));
		}
		this.setVisible(true);		
	}
	
	private EventHandler<? super MouseEvent> atpradioClicked = x -> {
		ActionOptionsDisplay.this.confirm.setDisable(false);
	};
	
	private class ActionTitledPane extends TitledPane{
		private LegalAction action;
		private RadioButton radioButton;
		
		public ActionTitledPane(LegalAction a) {
			super();
			action = a;
			Node content;
			if(action instanceof LegalMulti) {
				content = new Accordion();
				ArrayList<LegalAction> subActions = ((LegalMulti) action).getActions();
				for(int i = 0; i < subActions.size(); i++) {
					LegalAction subAction = subActions.get(i);
					Label myText = new Label(subAction.getDescription());
					myText.setWrapText(true);
					((Accordion) content).getPanes().add(new TitledPane(subAction.getName(),
							myText));
				}
			}
			else {
				content = new Label(action.getDescription());
				((Label) content).setWrapText(true);
			}
			this.setContent(content);
			BorderPane bPane = new BorderPane();
			bPane.setLeft(new Label(action.getName()));
			Label label = new Label(" ");
			bPane.setCenter(label);
			radioButton = new RadioButton();
			radioButton.setToggleGroup(ActionOptionsDisplay.this.tg);
			radioButton.setOnMouseClicked(atpradioClicked);
			bPane.setRight(radioButton);
			bPane.maxWidthProperty().bind(vBox.widthProperty());
			this.setGraphic(bPane);
		}
		
		public LegalAction getAction() {
			return action;
		}
		
		public boolean isSelected() {
			return radioButton.isSelected();
		}
	}
}
