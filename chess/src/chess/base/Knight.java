package chess.base;

import java.util.Arrays;
import java.util.Set;

import chess.util.ActionTree;
import chess.util.Condition;
import chess.util.MoveAndCaptureAction;
import javafx.scene.image.Image;

/* *
 * @author Sam Hooper
 */
public class Knight extends Piece{
	
	private static final int moveA;
	private static final int moveB;
	public static final Image BLACK_IMAGE;
	public static final Image WHITE_IMAGE;

	private static PieceData data;
	static {
		BLACK_IMAGE = new Image(Piece.class.getResourceAsStream(Main.RESOURCES_PREFIX + "knight_black.png"));
		WHITE_IMAGE = new Image(Piece.class.getResourceAsStream(Main.RESOURCES_PREFIX + "knight_white.png"));
		data = new PieceData("Knight", WHITE_IMAGE, BLACK_IMAGE);
		moveA = 1;
		moveB = 2;
		data.setTree(
			new ActionTree(Arrays.asList(
				new ActionTree.Node(MoveAndCaptureAction.relative( moveA,	moveB, Condition.EOE)),
				new ActionTree.Node(MoveAndCaptureAction.relative( moveA,  -moveB, Condition.EOE)),
				new ActionTree.Node(MoveAndCaptureAction.relative(-moveA,   moveB, Condition.EOE)),
				new ActionTree.Node(MoveAndCaptureAction.relative(-moveA,  -moveB, Condition.EOE)),
				new ActionTree.Node(MoveAndCaptureAction.relative( moveB, 	moveA, Condition.EOE)),
				new ActionTree.Node(MoveAndCaptureAction.relative( moveB,  -moveA, Condition.EOE)),
				new ActionTree.Node(MoveAndCaptureAction.relative(-moveB, 	moveA, Condition.EOE)),
				new ActionTree.Node(MoveAndCaptureAction.relative(-moveB,  -moveA, Condition.EOE))
			))
		);
		data.setPointValue(3);
	}
	
	

	@Override
	public Set<LegalAction> getLegalActions(Board b, int row, int col) {
		//System.out.printf("Getting legal moves for a Knight ::%n");
		Set<LegalAction> legals = data.getTree().getLegals(b, row, col);
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
		return data.getTree().canCheck(b, startRow, startCol, destRow, destCol);
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
		return data.getPointValue();
	
	}
	@Override
	public PieceType getPieceType() {
		return data.getPieceType();
	}
	
	@Override
	public PieceData getPieceData() {
		return data;
	}
	
	public static PieceData getData() {
		return data;
	}

}
