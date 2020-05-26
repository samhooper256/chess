package chess.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import chess.util.ActionTree;
import chess.util.BooleanAttribute;
import chess.util.CaptureAction;
import chess.util.Condition;
import chess.util.MoveAndCaptureAction;
import chess.util.MultiAction;
import chess.util.PromotionAction;
import javafx.scene.image.Image;

/* *
 * @author Sam Hooper
 */
public class Knight extends Piece{
	
	private static int moveA;
	private static int moveB;
	public static final Image BLACK_IMAGE;
	public static final Image WHITE_IMAGE;
	
	private static final int POINT_VALUE = 3;
	
	private static ActionTree tree;
	static {
		BLACK_IMAGE = new Image(Piece.class.getResourceAsStream("/resources/knight_black.png"));
		WHITE_IMAGE = new Image(Piece.class.getResourceAsStream("/resources/knight_white.png"));
		moveA = 1;
		moveB = 2;
		
		tree = new ActionTree(Arrays.asList(
			new ActionTree.Node(MoveAndCaptureAction.jumpRelative( moveA,	moveB, Condition.EOE)),
			new ActionTree.Node(MoveAndCaptureAction.jumpRelative( moveA,  -moveB, Condition.EOE)),
			new ActionTree.Node(MoveAndCaptureAction.jumpRelative(-moveA,   moveB, Condition.EOE)),
			new ActionTree.Node(MoveAndCaptureAction.jumpRelative(-moveA,  -moveB, Condition.EOE)),
			new ActionTree.Node(MoveAndCaptureAction.jumpRelative( moveB, 	moveA, Condition.EOE)),
			new ActionTree.Node(MoveAndCaptureAction.jumpRelative( moveB,  -moveA, Condition.EOE)),
			new ActionTree.Node(MoveAndCaptureAction.jumpRelative(-moveB, 	moveA, Condition.EOE)),
			new ActionTree.Node(MoveAndCaptureAction.jumpRelative(-moveB,  -moveA, Condition.EOE))
		));
	}
	
	

	@Override
	public Set<LegalAction> getLegalActions(Board b, int row, int col) {
		//System.out.printf("Getting legal moves for a Knight ::%n");
		Set<LegalAction> legals = tree.getLegals(b, row, col);
		//System.out.printf("\tBefore filtering = %s%n", legals);
		legals.removeIf(x -> !b.tryMoveForLegality(row, col, x));
		//System.out.printf("\tAfter filtering  = %s%n", legals);
		return legals;
	}
	
	public Knight(boolean color) {
		super(color);
	}
	
	@Override
	public boolean canCheck(Board b, int startRow, int startCol, int destRow, int destCol) {
		return tree.canCheck(b, startRow, startCol, destRow, destCol);
		/*
		int aDiff = Math.abs(startRow - destRow);
		int bDiff = Math.abs(startCol - destCol);
		return aDiff == moveA && bDiff == moveB || aDiff == moveB && bDiff == moveA;
		*/
	}
	@Override
	public Image getImage() {
		return getColor() == Piece.WHITE ? WHITE_IMAGE : BLACK_IMAGE;
	}
	
	public String toString() {
		return getColorString() + " Knight";
	}
	
	@Override
	public int getPointValue() {
		return POINT_VALUE;
	}
	
	

}