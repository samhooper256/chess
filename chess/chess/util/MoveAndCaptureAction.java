package chess.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import chess.base.Board;
import chess.base.LegalAction;
import chess.base.LegalMoveAndCapture;
import chess.base.Piece;

public abstract class MoveAndCaptureAction extends chess.util.Action{
	protected MoveAndCaptureAction() {}
	
	public static RelativeJumpMoveAndCaptureAction jumpRelative(int relRow, int relCol, Condition... cons) {
		return new RelativeJumpMoveAndCaptureAction(relRow, relCol, cons);
	}
	
	public static LineMoveAndCaptureAction line(int deltaRow, int deltaCol, Condition... cons) {
		return new LineMoveAndCaptureAction(deltaRow, deltaCol, cons);
	}
	
	public static RelativeSegmentMoveAndCaptureAction segment(int relStartRow, int relStartCol, int relDestRow, int relDestCol,
			Condition... cons) {
		throw new IllegalArgumentException("don't call this yet");
		//return new RelativeSegmentMoveAndCaptureAction(relStartRow, relStartCol, relDestRow, relDestCol);
	}
	
	public static class LineMoveAndCaptureAction extends MoveAndCaptureAction implements LineAction{
		private int deltaRow, deltaCol; //deltaRow, deltaCol
		private ArrayList<Condition> stopConditions;
		public LineMoveAndCaptureAction(int dr, int dc, Condition... cons) {
			deltaRow = dr;
			deltaCol = dc;
			stopConditions = new ArrayList<>();
			this.addAllConditions(cons);
		}
		
		public LineMoveAndCaptureAction stops(Condition... cons) {
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
		
		/*
		 * Important: Stop conditions are checked AFTER the conditions for adding a legal move. Thus,
		 * the first destination that meets the stop conditions WILL BE ADDED.
		 * */
		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			Set<LegalMoveAndCapture> moveSet = new HashSet<>();
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int row = startRow + m*deltaRow;
			int col = startCol + m*deltaCol;
			while(b.inBounds(row, col)) {
				if(checkConditions(b, startRow, startCol, row, col)) {
					moveSet.add(new LegalMoveAndCapture(row,col));
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
	
	public static class RelativeJumpMoveAndCaptureAction extends MoveAndCaptureAction implements RelativeJumpAction{
		private int relRow, relCol;
		public RelativeJumpMoveAndCaptureAction(int relRow, int relCol, Condition... cons) {
			this.relRow = relRow;
			this.relCol = relCol;
			this.addAllConditions(cons);
		}
		
		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int destRow = startRow + m*relRow, destCol = startCol + m*relCol;
			//System.out.printf("getting legals from rel M&C<%d,%d> : (%d,%d) -> (%d,%d)%n", relRow, relCol, startRow, startCol,
			//		destRow,destCol);
			if(b.inBounds(destRow, destCol) && checkConditions(b, startRow, startCol, destRow, destCol)) {
				return Collections.singleton(new LegalMoveAndCapture(destRow, destCol));
			}
			else {
				//System.out.println("\trelative M&C failed conditions check");
				Set<? extends LegalAction> s = Collections.emptySet();
				return s;
			}
		}
		
	}
	
	public static class RelativeSegmentMoveAndCaptureAction extends MoveAndCaptureAction implements RelativeSegmentAction{
		public RelativeSegmentMoveAndCaptureAction(int sr, int sc, int dr, int dc) {
			
		}

		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			// TODO Auto-generated method stub
			return null;
		}
	}
}




