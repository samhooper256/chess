package chess.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import chess.util.ActionTree;
import chess.util.CaptureAction;
import chess.util.Condition;
import chess.util.Flag;
import chess.util.IntegerPath;
import chess.util.MoveAndCaptureAction;
import chess.util.MultiAction;
import chess.util.PromotionAction;
import chess.util.SubMulti;
import chess.util.SummonAction;
import javafx.scene.image.Image;

/* *
 * @author Sam Hooper
 */
public class Pawn extends Piece{
	
	public static final Image BLACK_IMAGE;
	public static final Image WHITE_IMAGE;
	
	private static final int POINT_VALUE = 1;
	
	private static ActionTree tree;
	static {
		BLACK_IMAGE = new Image(Piece.class.getResourceAsStream("/resources/pawn_black.png"));
		WHITE_IMAGE = new Image(Piece.class.getResourceAsStream("/resources/pawn_white.png"));
		
		tree = new ActionTree(
			Arrays.asList(
				new ActionTree.Choke(Arrays.asList(IntegerPath.fromStartEnemyDist.greaterThan(new IntegerPath(2))),
					Arrays.asList(
						new ActionTree.Node(MoveAndCaptureAction.relative(-1, 0, Condition.DIE),
							new ActionTree.Node(MoveAndCaptureAction.relative(-2, 0, Condition.DIE,
							Condition.onSelf().call("hasMoved").toBool().invert().toCond()))),
						new ActionTree.Node(MoveAndCaptureAction.relative(-1, 1, Condition.EOD)),
						new ActionTree.Node(MoveAndCaptureAction.relative(-1, -1, Condition.EOD))
					)
				),
				new ActionTree.Choke(Arrays.asList(IntegerPath.fromStartEnemyDist.isEquals(new IntegerPath(2))),
						Arrays.asList(
							new ActionTree.Node(MultiAction.relative(-1, 0, Condition.DIE)
								.addAction(SubMulti.mnc(Flag.DESTINATION,0,0))
								.addAction(SubMulti.promo(new ArrayList<>(Arrays.asList("Queen","Rook","Bishop","Knight"))))
							),
							new ActionTree.Node(MultiAction.relative(-1, 1, Condition.EOD)
								.addAction(SubMulti.mnc(Flag.DESTINATION,0,0))
								.addAction(SubMulti.promo(new ArrayList<>(Arrays.asList("Queen","Rook","Bishop","Knight"))))
							),
							new ActionTree.Node(MultiAction.relative(-1, -1, Condition.EOD)
								.addAction(SubMulti.mnc(Flag.DESTINATION,0,0))
								.addAction(SubMulti.promo(new ArrayList<>(Arrays.asList("Queen","Rook","Bishop","Knight"))))
							)
						)
				),
				new ActionTree.Choke(Arrays.asList(
						Condition.onBoard().call("hasPlay").toBool().toCond(),
						Condition.onBoard().call("lastPlay").call("distance").toInt().isEquals(new IntegerPath(2)),
						Condition.onBoard().call("lastPlay").call("getPiece").toObj().instanceOf(Pawn.class),
						Condition.onBoard().call("lastPlay").call("getPlay").toObj().instanceOf(LegalMoveAndCapture.class)),
					new ArrayList<>(Arrays.asList(
						new ActionTree.Node(MultiAction.relative(-1, -1)
							.addAction(SubMulti.mnc(Flag.DESTINATION, 0, 0, Condition.DIE))
							.addAction(SubMulti.capRel(Flag.ORIGIN, 0, -1, Condition.EOD,
							Condition.onDest().call("getPiece").toObj().isEquals(Condition.onBoard().call("lastPlay").call("getPiece").toObj())	
							))
						),
						new ActionTree.Node(MultiAction.relative(-1, 1)
							.addAction(SubMulti.mnc(Flag.DESTINATION, 0, 0, Condition.DIE))
							.addAction(SubMulti.capRel(Flag.ORIGIN, 0, 1, Condition.EOD,
							Condition.onDest().call("getPiece").toObj().isEquals(Condition.onBoard().call("lastPlay").call("getPiece").toObj())	
							))
						)
					))
				),
				new ActionTree.Node(SummonAction.line(-1, 0, new ArrayList<>(Arrays.asList("Knight")), Condition.DIE).stops(Condition.POD))
				
			)
		);
	}
	
	public Pawn(boolean color) {
		super(color);
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
	
	@Override
	public boolean canCheck(Board b, int startRow, int startCol, int destRow, int destCol) {
		return tree.canCheck(b, startRow, startCol, destRow, destCol);
	}

	

	@Override
	public Image getImage() {
		return getColor() == Piece.WHITE ? WHITE_IMAGE : BLACK_IMAGE;
	}
	
	public String toString() {
		return getColorString() + " Pawn";
	}
	
	@Override
	public int getPointValue() {
		return POINT_VALUE;
	}

	private static final PieceType pieceType = PieceType.define("Pawn", false);
	@Override
	public PieceType getPieceType() {
		return pieceType;
	}
}
