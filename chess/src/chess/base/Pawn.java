package chess.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import chess.util.ActionTree;
import chess.util.CaptureAction;
import chess.util.Condition;
import chess.util.IntegerPath;
import chess.util.MoveAndCaptureAction;
import chess.util.MultiAction;
import chess.util.PromotionAction;
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
								.addAction(MoveAndCaptureAction.relative(-1, 0))
								.addAction(PromotionAction.withOptions(new ArrayList<>(Arrays.asList("Queen","Rook","Bishop","Knight"))))
							),
							new ActionTree.Node(MultiAction.relative(-1, 1, Condition.EOD)
								.addAction(MoveAndCaptureAction.relative(-1, 1))
								.addAction(PromotionAction.withOptions(new ArrayList<>(Arrays.asList("Queen","Rook","Bishop","Knight"))))
							),
							new ActionTree.Node(MultiAction.relative(-1, -1, Condition.EOD)
								.addAction(MoveAndCaptureAction.relative(-1, -1))
								.addAction(PromotionAction.withOptions(new ArrayList<>(Arrays.asList("Queen","Rook","Bishop","Knight"))))
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
							.addAction(MoveAndCaptureAction.relative(-1, -1, Condition.DIE))
							.addAction(CaptureAction.jumpRelative(0, -1, Condition.EOD,
							Condition.onDest().call("getPiece").toObj().referenceEquals(Condition.onBoard().call("lastPlay").call("getPiece").toObj())	
							))
						),
						new ActionTree.Node(MultiAction.relative(-1, 1)
							.addAction(MoveAndCaptureAction.relative(-1, 1, Condition.DIE))
							.addAction(CaptureAction.jumpRelative(0, 1, Condition.EOD,
							Condition.onDest().call("getPiece").toObj().referenceEquals(Condition.onBoard().call("lastPlay").call("getPiece").toObj())	
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
		/*
		if(this.getColor() == b.getBoardOrientation()) { //this pawn is moving "UP"
			return destRow == startRow - 1 && (destCol == startCol - 1 || destCol == startCol + 1);
		}
		else { //this pawn is moving "down"
			return destRow == startRow + 1 && (destCol == startCol - 1 || destCol == startCol + 1);
		}
		*/
	}
	
	
	/*
	@Override
	public ArrayList<int[]> getLegalActions(Board b, int row, int col) {
		ArrayList<int[]> legalMoves = new ArrayList<>();
		int nr, nc;
		boolean ep1 = true, ep2 = true, twoHopPotential = true;
		if(this.getColor() == Piece.WHITE) {
			nr = row - 1; nc = col;
			if(b.inBounds(nr, nc)) {
				Piece p = b.getPieceAt(nr, nc);
				if(p == null && b.tryMoveForLegality(row, col, nr, nc)) {
					legalMoves.add(new int[] {nr, nc});
				}
				else {
					twoHopPotential = false;
				}
			}
			nr = row - 1; nc = col - 1;
			if(b.inBounds(nr, nc)) {
				Piece p = b.getPieceAt(nr, nc);
				if(p != null && p.getColor() != this.getColor() && b.tryMoveForLegality(row, col, nr, nc)) {
					legalMoves.add(new int[] {nr, nc});
					ep1 = false;
				}
			}
			nr = row - 1; nc = col + 1;
			if(b.inBounds(nr, nc)) {
				Piece p = b.getPieceAt(nr, nc);
				if(p != null && p.getColor() != this.getColor() && b.tryMoveForLegality(row, col, nr, nc)) {
					legalMoves.add(new int[] {nr, nc});
					ep2 = false;
				}
			}
			if(twoHopPotential && !this.hasMoved()) {
				nr = row - 2; nc = col;
				if(b.inBounds(nr, nc)) {
					Piece p = b.getPieceAt(nr, nc);
					if(p == null && b.tryMoveForLegality(row, col, nr, nc)) {
						legalMoves.add(new int[] {nr, nc});
					}
				}
			}
			Piece p;
			if(	ep1 && b.inBounds(row, col - 1) && (p = b.getPieceAt(row, col - 1)) instanceof Pawn && 
				p.getColor() != this.getColor() &&
				b.getPieceAt(row - 1, col - 1) == null && b.checkEnPassantLegality(row, col, row - 1, col - 1)){
				legalMoves.add(new int[] {row - 1, col - 1});
			}
			if(	ep2 && b.inBounds(row, col + 1) && (p = b.getPieceAt(row, col + 1)) instanceof Pawn && 
				p.getColor() != this.getColor() &&
				b.getPieceAt(row - 1, col + 1) == null && b.checkEnPassantLegality(row, col, row - 1, col + 1)){
				legalMoves.add(new int[] {row - 1, col + 1});
			}
		}
		else {
			nr = row + 1; nc = col;
			if(b.inBounds(nr, nc)) {
				Piece p = b.getPieceAt(nr, nc);
				if(p == null && b.tryMoveForLegality(row, col, nr, nc)) {
					legalMoves.add(new int[] {nr, nc});
				}
				else {
					twoHopPotential = false;
				}
			}
			nr = row + 1; nc = col - 1;
			if(b.inBounds(nr, nc)) {
				Piece p = b.getPieceAt(nr, nc);
				if(p != null && p.getColor() != this.getColor() && b.tryMoveForLegality(row, col, nr, nc)) {
					legalMoves.add(new int[] {nr, nc});
					ep1 = false;
				}
			}
			nr = row + 1; nc = col + 1;
			if(b.inBounds(nr, nc)) {
				Piece p = b.getPieceAt(nr, nc);
				if(p != null && p.getColor() != this.getColor() && b.tryMoveForLegality(row, col, nr, nc)) {
					legalMoves.add(new int[] {nr, nc});
					ep2 = false;
				}
			}
			if(twoHopPotential && !this.hasMoved()) { 
				nr = row + 2; nc = col;
				if(b.inBounds(nr, nc)) {
					Piece p = b.getPieceAt(nr, nc);
					if(p == null && b.tryMoveForLegality(row, col, nr, nc)) {
						legalMoves.add(new int[] {nr, nc});
					}
				}
			}
			Piece p;
			if(	ep1 && b.inBounds(row, col - 1) && (p = b.getPieceAt(row, col - 1)) instanceof Pawn && 
				p.getColor() != this.getColor() &&
				b.getPieceAt(row + 1, col - 1) == null && b.checkEnPassantLegality(row, col, row + 1, col - 1)){
				legalMoves.add(new int[] {row + 1, col - 1});
			}
			if(	ep2 && b.inBounds(row, col + 1) && (p = b.getPieceAt(row, col + 1)) instanceof Pawn && 
				p.getColor() != this.getColor() &&
				b.getPieceAt(row + 1, col + 1) == null && b.checkEnPassantLegality(row, col, row + 1, col + 1)){
				legalMoves.add(new int[] {row + 1, col + 1});
			}
		}
		return legalMoves;
	}
	*/

	

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

	
}
