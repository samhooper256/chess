package chess.base;

import java.util.Arrays;
import java.util.Set;

import chess.util.ActionTree;
import chess.util.Condition;
import chess.util.Flag;
import chess.util.MoveAndCaptureAction;
import chess.util.MultiAction;
import chess.util.OtherMoveAndCaptureAction;
import chess.util.SubMulti;
import javafx.scene.image.Image;

/* *
 * @author Sam Hooper
 */
public class Bishop extends Piece {
	public static final Image BLACK_IMAGE;
	public static final Image WHITE_IMAGE;
	
	private static final int POINT_VALUE = 3;
	
	private static ActionTree tree;
	static {
		BLACK_IMAGE = new Image(Piece.class.getResourceAsStream("/resources/bishop_black.png"));
		WHITE_IMAGE = new Image(Piece.class.getResourceAsStream("/resources/bishop_white.png"));
		
		tree = new ActionTree(Arrays.asList(
			new ActionTree.Node(MoveAndCaptureAction.line(1, 1, Condition.EOE).stops(Condition.POD)),
			new ActionTree.Node(MoveAndCaptureAction.line(1, -1, Condition.EOE).stops(Condition.POD)),
			new ActionTree.Node(MoveAndCaptureAction.line(-1, 1, Condition.EOE).stops(Condition.POD)),
			new ActionTree.Node(MoveAndCaptureAction.line(-1, -1, Condition.EOE).stops(Condition.POD)),
			new ActionTree.Node(
				MultiAction.relative(-2, 0)
				.addAction(SubMulti.omnc(Flag.ORIGIN, 0, -1, -2, -1, Condition.POS), false)
				.addAction(SubMulti.omnc(Flag.ORIGIN, 0, 1, -2, 1, Condition.POS), false)
			)
		));
	}
	
	public Bishop(boolean color) {
		super(color);
	}
	
	@Override
	public Set<LegalAction> getLegalActions(Board b, int row, int col) {
		//System.out.printf("Getting legal moves for a Bishop ::%n");
		Set<LegalAction> legals = tree.getLegals(b, row, col);
		//System.out.printf("\tBefore filtering = %s%n", legals);
		legals.removeIf(x -> !b.tryMoveForLegality(row, col, x));
		//System.out.printf("\tAfter filtering  = %s%n", legals);
		return legals;
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
		return getColorString() + " Bishop";
	}
	
	@Override
	public int getPointValue() {
		return POINT_VALUE;
	}
	
	private static final PieceType pieceType = PieceType.define("Bishop", false);
	@Override
	public PieceType getPieceType() {
		return pieceType;
	}

}
