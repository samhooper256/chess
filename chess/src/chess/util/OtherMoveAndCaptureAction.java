package chess.util;

import java.util.Collections;
import java.util.Set;

import chess.base.Board;
import chess.base.LegalAction;
import chess.base.LegalOtherMoveAndCapture;
import chess.base.Piece;


public abstract class OtherMoveAndCaptureAction extends chess.util.Action{
	public static RelativeOtherMoveAndCaptureAction relative(int otherRelStartRow,
			int otherRelStartCol, int otherRelDestRow, int otherRelDestCol, Condition... cons) {
		return new RelativeOtherMoveAndCaptureAction(otherRelStartRow, otherRelStartCol,
				otherRelDestRow, otherRelDestCol, cons);
	}
	
	public static class RelativeOtherMoveAndCaptureAction extends OtherMoveAndCaptureAction implements RelativeJumpAction{
		int relStartRow, relStartCol, relDestRow, relDestCol;
		public RelativeOtherMoveAndCaptureAction(int otherRelStartRow,
			int otherRelStartCol, int otherRelDestRow, int otherRelDestCol, Condition... cons) {
			this.relStartRow = otherRelStartRow;
			this.relStartCol = otherRelStartCol;
			this.relDestRow = otherRelDestRow;
			this.relDestCol = otherRelDestCol;
			this.addAllConditions(cons);
		}
		
		/* *
		 * IMPORTANT:
		 * OtherMoveAndCapture using the startRow/Col of the piece it's moving as the startRow/Col
		 * passed to checkConditions. So, for example, in the castling move, the startRow/Col
		 * passed to checkConditions are the coordinates of the Rook, not the King. Likewise,
		 * the destRow/Col are the destination of the piece to be moved (aka the tile the Rook
		 * lands on)
		 * 
		 * ALSO IMPORTANT: OtherMoveAndCapture REQUIRES there to be a piece on the starting tile
		 * of the OtherMoveAndCapture.
		 */
		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			Piece actingPiece = b.getPieceAt(startRow, startCol);
			int m = actingPiece.getColor() == Piece.WHITE ? 1 : -1;
			int sr = startRow + m*relStartRow, sc = startCol + m*relStartCol,
				dr = startRow + m*relDestRow, dc = startCol + m*relDestCol;
			
			if(b.inBounds(sr, sc) && b.inBounds(dr, dc) && b.getPieceAt(sr, sc) != null && checkConditions(b, sr, sc, dr, dc)) {
				Set<? extends LegalAction> s = Collections.singleton(new LegalOtherMoveAndCapture(sr,sc,dr,dc));
//				System.out.println("\treturning " + s);
				return s;
			}
			else {
				Set<? extends LegalAction> s = Collections.emptySet();
				return s;
			}
		}
	}
	
	/* *
	 * Note that there are no line- or radius-based OtherMoveAndCaptureActions.
	 */
}
