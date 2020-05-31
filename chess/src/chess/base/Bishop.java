package chess.base;

import java.util.Arrays;
import java.util.Set;

import chess.util.ActionTree;
import chess.util.Condition;
import chess.util.MoveAndCaptureAction;
import chess.util.MultiAction;
import chess.util.OtherMoveAndCaptureAction;
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
				.addAction(OtherMoveAndCaptureAction.relative(0, -1, -2, -1, Condition.POS), false)
				.addAction(OtherMoveAndCaptureAction.relative(0, 1, -2, 1, Condition.POS), false)
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
	
	/*
	@Override
	public ArrayList<int[]> getLegalActions(Board b, int row, int col) {
		ArrayList<int[]> legalMoves = new ArrayList<>();
		
		for(int r = row + 1, c = col + 1; b.inBounds(r, c); r++, c++) { //searches toward BOTTOM RIGHT
			Piece p = b.getPieceAt(r,c);
			if(p == null) {
				if(b.tryMoveForLegality(row, col, r, c)) {
					legalMoves.add(new int[] {r,c});
				}
			}
			else {
				if(p.getColor() != this.getColor()) {
					if(b.tryMoveForLegality(row, col, r, c)) {
						legalMoves.add(new int[] {r,c});
					}
				}
				break;
			}
		}
		
		for(int r = row + 1, c = col - 1; b.inBounds(r, c); r++, c--) { //searches toward BOTTOM LEFT
			Piece p = b.getPieceAt(r,c);
			if(p == null) {
				if(b.tryMoveForLegality(row, col, r, c)) {
					legalMoves.add(new int[] {r,c});
				}
			}
			else {
				if(p.getColor() != this.getColor()) {
					if(b.tryMoveForLegality(row, col, r, c)) {
						legalMoves.add(new int[] {r,c});
					}
				}
				break;
			}
		}
		
		for(int r = row - 1, c = col + 1; b.inBounds(r, c); r--, c++) { //searches toward TOP RIGHT
			Piece p = b.getPieceAt(r,c);
			if(p == null) {
				if(b.tryMoveForLegality(row, col, r, c)) {
					legalMoves.add(new int[] {r,c});
				}
			}
			else {
				if(p.getColor() != this.getColor()) {
					if(b.tryMoveForLegality(row, col, r, c)) {
						legalMoves.add(new int[] {r,c});
					}
				}
				break;
			}
		}
		
		for(int r = row - 1, c = col - 1; b.inBounds(r, c); r--, c--) { //searches toward TOP LEFT
			Piece p = b.getPieceAt(r,c);
			if(p == null) {
				if(b.tryMoveForLegality(row, col, r, c)) {
					legalMoves.add(new int[] {r,c});
				}
			}
			else {
				if(p.getColor() != this.getColor()) {
					if(b.tryMoveForLegality(row, col, r, c)) {
						legalMoves.add(new int[] {r,c});
					}
				}
				break;
			}
		}
		
		return legalMoves;
	}
	
	*/
	@Override
	public boolean canCheck(Board b, int startRow, int startCol, int destRow, int destCol) {
		return tree.canCheck(b, startRow, startCol, destRow, destCol);
		/*
		int colDist = destCol - startCol;
		int rowDist = destRow - startRow;
		
		if(Math.abs(colDist) != Math.abs(rowDist)) return false;
		
		int abs = Math.abs(colDist);
		
		if(colDist > 0) {
			if(rowDist > 0) { //enemy is toward BOTTOM RIGHT
				for(int i = 1; i < abs; i++) {
					if(b.getPieceAt(startRow + i, startCol + i) != null) {
						return false;
					}
				}
				return true;
			}
			else { //enemy is toward TOP RIGHT
				for(int i = 1; i < abs; i++) {
					if(b.getPieceAt(startRow - i, startCol + i) != null) {
						return false;
					}
				}
				return true;
			}
		}
		else {
			if(rowDist > 0) { //enemy is toward BOTTOM LEFT
				for(int i = 1; i < abs; i++) {
					if(b.getPieceAt(startRow + i, startCol - i) != null) {
						return false;
					}
				}
				return true;
			}
			else { //enemy is toward TOP LEFT
				for(int i = 1; i < abs; i++) {
					if(b.getPieceAt(startRow - i, startCol - i) != null) {
						return false;
					}
				}
				return true;
			}
		}
		*/
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

	

}
