package chess.base;

import java.util.Arrays;
import java.util.Set;

import chess.util.ActionTree;
import chess.util.Condition;
import chess.util.MoveAndCaptureAction;
import chess.util.OtherMoveAndCaptureAction;
import javafx.scene.image.Image;

/* *
 * @author Sam Hooper
 */
public class Queen extends Piece {
	public static final Image BLACK_IMAGE;
	public static final Image WHITE_IMAGE;
	
	private static final PieceData data;
	
	static {
		BLACK_IMAGE = new Image(Piece.class.getResourceAsStream(Main.RESOURCES_PREFIX + "queen_black.png"));
		WHITE_IMAGE = new Image(Piece.class.getResourceAsStream(Main.RESOURCES_PREFIX + "queen_white.png"));
		
		data = new PieceData("Queen", WHITE_IMAGE, BLACK_IMAGE);
		data.setTree(
			new ActionTree(Arrays.asList(
				new ActionTree.Node(MoveAndCaptureAction.line(0, 1, Condition.EOE).stops(Condition.POD)),
				new ActionTree.Node(MoveAndCaptureAction.line(1, 1, Condition.EOE).stops(Condition.POD)),
				new ActionTree.Node(MoveAndCaptureAction.line(1, 0, Condition.EOE).stops(Condition.POD)),
				new ActionTree.Node(MoveAndCaptureAction.line(1, -1, Condition.EOE).stops(Condition.POD)),
				new ActionTree.Node(MoveAndCaptureAction.line(0, -1, Condition.EOE).stops(Condition.POD)),
				new ActionTree.Node(MoveAndCaptureAction.line(-1, -1, Condition.EOE).stops(Condition.POD)),
				new ActionTree.Node(MoveAndCaptureAction.line(-1, 0, Condition.EOE).stops(Condition.POD)),
				new ActionTree.Node(MoveAndCaptureAction.line(-1, 1, Condition.EOE).stops(Condition.POD)),
				new ActionTree.Node(OtherMoveAndCaptureAction.relative(0, -1, 0, -2)),
				new ActionTree.Node(OtherMoveAndCaptureAction.relative(0, 1, 0, 2))
			))
		);
		data.setPointValue(8);
	}
	
	public Queen(boolean color) {
		super(color);
	}
	
	@Override
	public Set<LegalAction> getLegalActions(Board b, int row, int col) {
		//System.out.printf("Getting legal moves for a Queen ::%n");
		Set<LegalAction> legals = data.getTree().getLegals(b, row, col);
		//System.out.printf("\tBefore filtering = %s%n", legals);
		legals.removeIf(x -> !b.tryMoveForLegality(row, col, x));
		//System.out.printf("\tAfter filtering  = %s%n", legals);
		return legals;
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
		return getColorString() + " Queen";
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
