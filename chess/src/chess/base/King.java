package chess.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import chess.util.ActionTree;
import chess.util.CaptureAction;
import chess.util.Condition;
import chess.util.MoveAndCaptureAction;
import chess.util.MultiAction;
import chess.util.OtherMoveAndCaptureAction;
import javafx.scene.image.Image;

/* *
 * @author Sam Hooper
 */
public class King extends Piece{
	
	public static final Image BLACK_IMAGE;
	public static final Image WHITE_IMAGE;
	
	private static final int POINT_VALUE = 0;
	
	
	private static ActionTree tree;
	static {
		BLACK_IMAGE = new Image(Piece.class.getResourceAsStream("/resources/king_black.png"));
		WHITE_IMAGE = new Image(Piece.class.getResourceAsStream("/resources/king_white.png"));
		
		tree = new ActionTree(Arrays.asList(
				
			new ActionTree.Node(MoveAndCaptureAction.jumpRelative(0, 1, Condition.EOE)),
			new ActionTree.Node(MoveAndCaptureAction.jumpRelative(1, 1, Condition.EOE)),
			new ActionTree.Node(MoveAndCaptureAction.jumpRelative(1, 0, Condition.EOE)),
			new ActionTree.Node(MoveAndCaptureAction.jumpRelative(1, -1, Condition.EOE)),
			new ActionTree.Node(MoveAndCaptureAction.jumpRelative(0, -1, Condition.EOE)),
			new ActionTree.Node(MoveAndCaptureAction.jumpRelative(-1, -1, Condition.EOE)),
			new ActionTree.Node(MoveAndCaptureAction.jumpRelative(-1, 0, Condition.EOE)),
			new ActionTree.Node(MoveAndCaptureAction.jumpRelative(-1, 1, Condition.EOE)),
			new ActionTree.Node(MultiAction.relativeDisplay(0, 2, Condition.DIE, Condition.onStartRelative(0, 1).call("getPiece").toObj().isNull(),
					Condition.onSelf().call("hasMoved").toBool().invert().toCond(),
					Condition.onStartRelativeCheckable(0, 1).not()
					).addAction(MoveAndCaptureAction.jumpRelative(0, 2))
					.addAction(OtherMoveAndCaptureAction.relative(0,3,0,1)))
			
		));
	}
	
	public King(boolean color) {
		super(color);
	}

	@Override
	public boolean canCheck(Board b, int startRow, int startCol, int destRow, int destCol) {
		return tree.canCheck(b, startRow, startCol, destRow, destCol);
	}

	@Override
	public Image getImage() {
		return getColor() == Piece.WHITE ? WHITE_IMAGE : BLACK_IMAGE;
	}
	
	public String toString() {
		return getColorString() + " King";
	}
	
	@Override
	public int getPointValue() {
		return POINT_VALUE;
	}

	@Override
	public Set<LegalAction> getLegalActions(Board b, int row, int col) {
		//System.out.printf("Getting legal moves for a King ::%n");
		Set<LegalAction> legals = tree.getLegals(b, row, col);
		//System.out.printf("\tBefore filtering = %s%n", legals);
		legals.removeIf(x -> !b.tryMoveForLegality(row, col, x));
		//System.out.printf("\tAfter filtering  = %s%n", legals);
		return legals;
	}
}
