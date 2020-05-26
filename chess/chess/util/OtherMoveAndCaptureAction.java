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
	
	public static class RelativeOtherMoveAndCaptureAction extends OtherMoveAndCaptureAction{
		int relStartRow, relStartCol, relDestRow, relDestCol;
		public RelativeOtherMoveAndCaptureAction(int otherRelStartRow,
			int otherRelStartCol, int otherRelDestRow, int otherRelDestCol, Condition... cons) {
			this.relStartRow = otherRelStartRow;
			this.relStartCol = otherRelStartCol;
			this.relDestRow = otherRelDestRow;
			this.relDestCol = otherRelDestCol;
			this.addAllConditions(cons);
		}
		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int sr = startRow + m*relStartRow, sc = startCol + m*relStartCol,
				dr = startRow + m*relDestRow, dc = startCol + m*relDestCol;
//			System.out.printf("^^^ getLegals on OMNC<%d,%d>: (%d,%d) -> (%d,%d)%n^^^ relatives = (%d,%d), (%d,%d)%n",
//					startRow,startCol,sr,sc,dr,dc,relStartRow,relStartCol,relDestRow,relDestCol);
			if(b.inBounds(sr, sc) && b.inBounds(dr, dc) && checkConditions(b, sr, sc, dr, dc)) {
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
}
