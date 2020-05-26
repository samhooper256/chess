package chess.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import chess.base.Board;
import chess.base.LegalAction;
import chess.base.LegalCapture;
import chess.base.LegalMoveAndCapture;
import chess.base.Piece;
import chess.util.MoveAndCaptureAction.LineMoveAndCaptureAction;

public abstract class CaptureAction extends chess.util.Action{
	private CaptureAction() {}

	public static RelativeJumpCaptureAction jumpRelative(int relRow, int relCol, Condition... cons) {
		return new RelativeJumpCaptureAction(relRow, relCol, cons);
	}
	
	public static LineCaptureAction line(int deltaRow, int deltaCol, Condition... cons) {
		return new LineCaptureAction(deltaRow, deltaCol, cons);
	}
	
	public static class LineCaptureAction extends CaptureAction implements LineAction{
		private int deltaRow, deltaCol; //deltaRow, deltaCol
		private ArrayList<Condition> stopConditions;
		public LineCaptureAction(int dr, int dc, Condition... cons) {
			deltaRow = dr;
			deltaCol = dc;
			stopConditions = new ArrayList<>();
			this.addAllConditions(cons);
		}
		
		public LineCaptureAction stops(Condition... cons) {
			for(int i = 0; i < cons.length; i++) {
				stopConditions.add(cons[i]);
			}
			return this;
		}
		
		private boolean checkStops(Board b, int startRow, int startCol, int destRow, int destCol) {
			for(int i = 0; i < stopConditions.size(); i++) {
				if(stopConditions.get(i).eval(b, startRow, startCol, destRow, destCol)) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			Set<LegalCapture> moveSet = new HashSet<>();
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int row = startRow + m*deltaRow;
			int col = startCol + m*deltaCol;
			while(b.inBounds(row, col)) {
				if(checkConditions(b, startRow, startCol, row, col)) {
					moveSet.add(new LegalCapture(row,col));
				}
				if(checkStops(b, startRow, startCol, row, col)){
					break;
				}
				row += m*deltaRow;
				col += m*deltaCol;
			}
			return moveSet;
		}
	}
	
	public static class RelativeJumpCaptureAction extends CaptureAction implements RelativeJumpAction{
		private int relRow, relCol;
		public RelativeJumpCaptureAction(int relRow, int relCol, Condition... cons) {
			this.relRow = relRow;
			this.relCol = relCol;
			this.addAllConditions(cons);
		}
		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int destRow = startRow + m*relRow, destCol = startCol + m*relCol;
			if(b.inBounds(destRow, destCol) && checkConditions(b, startRow, startCol, destRow, destCol)) {
				return Collections.singleton(new LegalCapture(destRow, destCol));
			}
			else {
				Set<? extends LegalAction> s = Collections.emptySet();
				return s;
			}
		}
	}
}
