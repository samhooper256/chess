package chess.piecebuilder;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;

public class ReferenceContextMenu extends ContextMenu{
	
	private Node parent;
	
	public ReferenceContextMenu(Node parent) {
		super();
		this.parent = parent;
	}
	
	public Node getParent() {
		return parent;
	}
}
