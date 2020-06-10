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
import chess.util.PromotionAction;
import chess.util.SubMulti;
import chess.util.SummonAction;
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
		BLACK_IMAGE = new Image(Piece.class.getResourceAsStream("/resources/knight_black.png"));
		WHITE_IMAGE = new Image(Piece.class.getResourceAsStream("/resources/knight_white.png"));
		data = new PieceData("Knight", WHITE_IMAGE, BLACK_IMAGE);
		moveA = 1;
		moveB = 2;
		data.setTree(
			new ActionTree(Arrays.asList(
				new ActionTree.Node(MoveAndCaptureAction.relative( moveA,	moveB, Condition.EOE)),
				/*
				new ActionTree.Node(MoveAndCaptureAction.relative( moveA,  -moveB, Condition.EOE)),
				new ActionTree.Node(MoveAndCaptureAction.relative(-moveA,   moveB, Condition.EOE)),
				new ActionTree.Node(MoveAndCaptureAction.relative(-moveA,  -moveB, Condition.EOE)),
				new ActionTree.Node(MoveAndCaptureAction.relative( moveB, 	moveA, Condition.EOE)),
				new ActionTree.Node(MoveAndCaptureAction.relative( moveB,  -moveA, Condition.EOE)),
				new ActionTree.Node(MoveAndCaptureAction.relative(-moveB, 	moveA, Condition.EOE)),
				new ActionTree.Node(MoveAndCaptureAction.relative(-moveB,  -moveA, Condition.EOE)),*/
				new ActionTree.Node(SummonAction.relative(-1, 0, new ArrayList<String>(Arrays.asList("Rook")), 
					Condition.xor(Condition.DIE, Condition.SIE)
				)),
				new ActionTree.Node(CaptureAction.relative(0, 0, Condition.onDestRelative(0,1).call("getRow").toIntegerPath()
						.isEquals(Condition.onBoard().call("getBoardSize").toIntegerPath()))),
				new ActionTree.Node(PromotionAction.withOptions(new ArrayList<>(Arrays.asList("Queen")), 
						Condition.onStartRelative(0, 0).call("getPiece").call("hasMoved").toBooleanPath().isEquals(
						Condition.onStartRelative(-1, 0).call("isEmpty").toBooleanPath()
						))),
				new ActionTree.Node(MoveAndCaptureAction.line(-1, 0, Condition.onDest().call("getPiece").toObjectPath().isNotNull())),
				new ActionTree.Node(CaptureAction.line(-2, 0, Condition.onBoard().toObjectPath().isNotNull())),
				new ActionTree.Node(CaptureAction.line(0, 1, Condition.onStart().call("getPiece").call("getPieceType").toObjectPath()
						.notEquals(Condition.onStartRelative(-1, 0).call("getPiece").call("getPieceType").toObjectPath()))),
				new ActionTree.Node(OtherMoveAndCaptureAction.relative(-1, 0, -2, 0, Condition.onStartRelative(-1, 0).call("getPiece")
						.toObjectPath().isPiece("Pawn"))),
				new ActionTree.Node(SummonAction.segment(-2, -1, 0, 1, 3, false, new ArrayList<>(Arrays.asList("Bishop")),
						Condition.onBoard().call("lastPlay").call("getPlay").toObjectPath().instanceOf(LegalMoveAndCapture.class))),
				new ActionTree.Node(CaptureAction.line(-1, 1, Condition.EOE).stops(Condition.EOD))
				/*
				new ActionTree.Node(MoveAndCaptureAction.relative(-1, 0)),
				new ActionTree.Node(SummonAction.relative(-1, 0, new ArrayList<>(Arrays.asList("Rook","Queen")))),
				new ActionTree.Node(CaptureAction.relative(-1, 0)),
				new ActionTree.Node(MultiAction.relative(-1, 0).addAction(SubMulti.promo(
						new ArrayList<>(Arrays.asList("Ghost")))))*/
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
