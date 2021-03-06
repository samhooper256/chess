package chess.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import chess.util.ActionTree;
import chess.util.Condition;
import chess.util.Flag;
import chess.util.MoveAndCaptureAction;
import chess.util.MultiAction;
import chess.util.SubMulti;
import javafx.scene.image.Image;

/* *
 * @author Sam Hooper
 */
public class King extends Piece{
	
	public static final Image BLACK_IMAGE;
	public static final Image WHITE_IMAGE;
	
	private static final PieceData data;
	static {
		BLACK_IMAGE = new Image(Piece.class.getResourceAsStream(Main.RESOURCES_PREFIX + "king_black.png"));
		WHITE_IMAGE = new Image(Piece.class.getResourceAsStream(Main.RESOURCES_PREFIX + "king_white.png"));
		
		data = new PieceData("King", WHITE_IMAGE, BLACK_IMAGE);
		/* *
		 * The king's action tree is set up so that he can castle with a rook that is 3 or 4 spaces
		 * away on his left OR right. This behaves correctly for a standard chess game with the normal
		 * starting setup, but it is important to note that he can castle with a rook 3 or 4 spaces
		 * away in EITHER direction.
		 */
		data.setTree(
			new ActionTree(Arrays.asList(
					
				new ActionTree.Node(MoveAndCaptureAction.relative(0, 1, Condition.EOE)),
				new ActionTree.Node(MoveAndCaptureAction.relative(1, 1, Condition.EOE)),
				new ActionTree.Node(MoveAndCaptureAction.relative(1, 0, Condition.EOE)),
				new ActionTree.Node(MoveAndCaptureAction.relative(1, -1, Condition.EOE)),
				new ActionTree.Node(MoveAndCaptureAction.relative(0, -1, Condition.EOE)),
				new ActionTree.Node(MoveAndCaptureAction.relative(-1, -1, Condition.EOE)),
				new ActionTree.Node(MoveAndCaptureAction.relative(-1, 0, Condition.EOE)),
				new ActionTree.Node(MoveAndCaptureAction.relative(-1, 1, Condition.EOE)),
				new ActionTree.Choke(
					new ArrayList<>(Arrays.asList(
						Condition.onSelf().call("hasMoved").toBooleanPath().invert().toCondition()
					)),
					new ArrayList<>(Arrays.asList(
						new ActionTree.Choke(
							new ArrayList<>(Arrays.asList(
								Condition.onStartRelative(0, 1).call("isEmpty").toBooleanPath().toCondition(),
								Condition.onStartRelativeCheckable(0, 1).not()
							)),
							new ArrayList<>(Arrays.asList(
								new ActionTree.Node(MultiAction.relative(0, 2,
									Condition.DIE,
									Condition.onStartRelative(0, 3).call("getPiece").toObjectPath().instanceOf(Rook.class),
									Condition.onStartRelative(0, 3).call("getPiece").call("hasMoved").toBooleanPath().invert().toCondition()
									).addAction(SubMulti.mnc(Flag.ORIGIN, 0, 2))
									.addAction(SubMulti.omnc(Flag.ORIGIN, 0,3,0,1))
								),
								new ActionTree.Node(MultiAction.relative(0, 2,
									Condition.DIE,
									Condition.onStartRelative(0, 3).call("isEmpty").toBooleanPath().toCondition(),
									Condition.onStartRelative(0, 4).call("getPiece").toObjectPath().instanceOf(Rook.class),
									Condition.onStartRelative(0, 4).call("getPiece").call("hasMoved").toBooleanPath().invert().toCondition()
									).addAction(SubMulti.mnc(Flag.ORIGIN, 0, 2))
									.addAction(SubMulti.omnc(Flag.ORIGIN, 0,4,0,1))
								)
							))
						),
						new ActionTree.Choke(
							new ArrayList<>(Arrays.asList(
								Condition.onStartRelative(0, -1).call("isEmpty").toBooleanPath().toCondition(),
								Condition.onStartRelativeCheckable(0, -1).not()
							)),
							new ArrayList<>(Arrays.asList(
								new ActionTree.Node(MultiAction.relative(0, -2,
									Condition.DIE,
									Condition.onStartRelative(0, -3).call("getPiece").toObjectPath().instanceOf(Rook.class),
									Condition.onStartRelative(0, -3).call("getPiece").call("hasMoved").toBooleanPath().invert().toCondition()
									).addAction(SubMulti.mnc(Flag.ORIGIN, 0, -2))
									.addAction(SubMulti.omnc(Flag.ORIGIN, 0,-3,0,-1))
								),
								new ActionTree.Node(MultiAction.relative(0, -2,
									Condition.DIE,
									Condition.onStartRelative(0, -3).call("isEmpty").toBooleanPath().toCondition(),
									Condition.onStartRelative(0, -4).call("getPiece").toObjectPath().instanceOf(Rook.class),
									Condition.onStartRelative(0, -4).call("getPiece").call("hasMoved").toBooleanPath().invert().toCondition()
									).addAction(SubMulti.mnc(Flag.ORIGIN, 0, -2))
									.addAction(SubMulti.omnc(Flag.ORIGIN, 0,-4,0,-1))
								)
							))
						)
					))
				)
			))
		);
		data.setPointValue(0);
	}
	
	public King(boolean color) {
		super(color);
	}

	@Override
	public boolean canCheck(Board b, int startRow, int startCol, int destRow, int destCol) {
		/*This is the ONLY time we don't defer this to the tree.
		 * The reason we have to make this exception is because kings cannot move to areas
		 * that can be checked. So, in order for a king to check if he can check a tile,
		 * he has to know if he can move there - which entails finding out where all the other
		 * pieces can attack, which entails finding out where the OTHER king can attack, when then
		 * calls the OTHER king's canCheck method, which then eventually (by the same process of steps)
		 * calls this method again and thus it repeats forever... 
		 */
		return Math.abs(destRow-startRow) <= 1 && Math.abs(destCol-startCol) <= 1;
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
		return data.getPointValue();
	}

	@Override
	public Set<LegalAction> getLegalActions(Board b, int row, int col) {
		//System.out.printf("Getting legal moves for a King ::%n");
		Set<LegalAction> legals = data.getTree().getLegals(b, row, col);
		//System.out.printf("\tBefore filtering = %s%n", legals);
		legals.removeIf(x -> !b.tryMoveForLegality(row, col, x));
		//System.out.printf("\tAfter filtering  = %s%n", legals);
		return legals;
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
