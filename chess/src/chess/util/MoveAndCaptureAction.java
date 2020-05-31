package chess.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import chess.base.Board;
import chess.base.LegalAction;
import chess.base.LegalMoveAndCapture;
import chess.base.Piece;

public abstract class MoveAndCaptureAction extends chess.util.Action{
	protected MoveAndCaptureAction() {}
	
	public static RelativeJumpMoveAndCaptureAction relative(int relRow, int relCol, Condition... cons) {
		return new RelativeJumpMoveAndCaptureAction(relRow, relCol, cons);
	}
	
	public static LineMoveAndCaptureAction line(int deltaRow, int deltaCol, Condition... cons) {
		return new LineMoveAndCaptureAction(deltaRow, deltaCol, cons);
	}
	
	public static RelativeLineMoveAndCaptureAction relLine(int relStartRow, int relStartCol, int dr, int dc,
			Condition... cons) {
		return new RelativeLineMoveAndCaptureAction(relStartRow, relStartCol, dr, dc, cons);
	}
	
	public static RelativeSegmentMoveAndCaptureAction segment(int relStartRow, int relStartCol, int dr, int dc, int length,
			Condition... cons) {
		return new RelativeSegmentMoveAndCaptureAction(relStartRow, relStartCol, dr, dc, length, cons);
	}
	
	public static class LineMoveAndCaptureAction extends MoveAndCaptureAction implements LineAction{
		protected int deltaRow, deltaCol;
		private ArrayList<Condition> stopConditions;
		public LineMoveAndCaptureAction(int dr, int dc, Condition... cons) {
			deltaRow = dr;
			deltaCol = dc;
			stopConditions = new ArrayList<>();
			this.addAllConditions(cons);
		}
		
		/* *
		 * Note that ALL STOPS MUST BE TRUE for us to stop.
		 */
		@Override
		public LineMoveAndCaptureAction stops(Condition... cons) {
			for(int i = 0; i < cons.length; i++) {
				stopConditions.add(cons[i]);
			}
			return this;
		}
		
		protected boolean checkStops(Board b, int startRow, int startCol, int destRow, int destCol) {
			if(stopConditions.size() == 0) {
				return false;
			}
			
			for(int i = 0; i < stopConditions.size(); i++) {
				if(!stopConditions.get(i).calc(b, startRow, startCol, destRow, destCol)) {
					return false;
				}
			}
			return true;
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
	
	public static class RelativeLineMoveAndCaptureAction extends LineMoveAndCaptureAction{
		private int relStartRow, relStartCol;
		private boolean requiresOnBoardStart;
		
		public RelativeLineMoveAndCaptureAction(int relsr, int relsc, int dr, int dc, Condition... cons) {
			super(dr,dc);
			relStartRow = relsr;
			relStartCol = relsc;
			requiresOnBoardStart = false;
		}
		
		public RelativeLineMoveAndCaptureAction(int relsr, int relsc, int dr, int dc, boolean onBoardStart, Condition... cons) {
			super(dr,dc);
			relStartRow = relsr;
			relStartCol = relsc;
			requiresOnBoardStart = onBoardStart;
		}
		
		@Override
		public RelativeLineMoveAndCaptureAction stops(Condition... cons) {
			return (RelativeLineMoveAndCaptureAction) super.stops(cons);
		}
		
		/*
		 * Important: Stop conditions are checked AFTER the conditions for adding a legal move. Thus,
		 * the first destination that meets the stop conditions WILL BE ADDED.
		 * */
		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			Set<LegalMoveAndCapture> moveSet = new HashSet<>();
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int row = startRow + m*relStartRow;
			int col = startCol + m*relStartCol;
			if(requiresOnBoardStart) {
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
			}
			else {
				int mDist;
				while((mDist = manhattanDist(row,col,b)) == 0 ||  
						mDist > manhattanDist(row + m*deltaRow, col + m*deltaCol, b)) {
					if(mDist == 0) {
						//we are in bounds (guaranteed by manhattanDist)
						if(checkConditions(b, startRow, startCol, row, col)) {
							moveSet.add(new LegalMoveAndCapture(row,col));
						}
						if(checkStops(b, startRow, startCol, row, col)){
							System.out.println("Broke bc stops");
							break;
						}
					}
					row += m*deltaRow;
					col += m*deltaCol;
				}
			}
			//System.out.printf("(%d,%d) rlMNC returning legals: %s%n", startRow, startCol, moveSet);
			return moveSet;
		}
		
		private int manhattanDist(int row, int col, Board b) {
			if(b.inBounds(row, col)) {
				return 0;
			}
			
			final int size = b.getBoardSizeAsInt();
			
			int rowDist, colDist;
			
			if(row < 0) {
				rowDist = -row;
			}
			else if(row >= size) {
				rowDist = row - size + 1;
			}
			else {
				rowDist = 0;
			}

			if(col < 0) {
				colDist = -col;
			}
			else if(col >= size){
				colDist = col - size + 1;
			}
			else {
				colDist = 0;
			}
			
			//System.out.printf("mDist returning %d+%d for (%d,%d)%n", rowDist,colDist,row,col);
			return rowDist + colDist;
			
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
	
	//TODO RelativeSegment actions!!!
	public static class RelativeSegmentMoveAndCaptureAction extends MoveAndCaptureAction implements RelativeSegmentAction{
		private int relStartRow, relStartCol, deltaRow, deltaCol, length;
		private boolean requiresOnBoardStart;
		private ArrayList<Condition> stopConditions;
		public RelativeSegmentMoveAndCaptureAction(int relStartRow, int relStartCol, int deltaRow, int deltaCol, int length,
				Condition... cons) {
			if(length <= 0) {
				throw new IllegalArgumentException("Length cannot be <= 0 (it is " + length + ")");
			}
			this.relStartRow = relStartRow;
			this.relStartCol = relStartCol;
			this.deltaRow = deltaRow;
			this.deltaCol = deltaCol;
			this.length = length;
			requiresOnBoardStart = false;
			stopConditions = new ArrayList<>();
			this.addAllConditions(cons);
		}
		
		public RelativeSegmentMoveAndCaptureAction stops(Condition... cons) {
			for(int i = 0; i < cons.length; i++) {
				stopConditions.add(cons[i]);
			}
			return this;
		}

		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			Set<LegalMoveAndCapture> legals = new HashSet<>();
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int row = startRow + m*relStartRow, col = startCol + m*relStartCol;
			int i = 0;
			if(!b.inBounds(row, col)) {
				if(requiresOnBoardStart) {
					return legals;
				}
				else {
					for(; i < length && !b.inBounds(row, col); i++) {
						row += m*deltaRow;
						col += m*deltaCol;
					}
				}
			}
			for(; i < length && b.inBounds(row, col); i++) {
				if(checkConditions(b, startRow, startCol, row, col)) {
					legals.add(new LegalMoveAndCapture(row,col));
				}
				if(checkStops(b, startRow, startCol, row, col)){
					System.out.println("Broke bc stops");
					break;
				}
				row += m*deltaRow;
				col += m*deltaCol;
			}
			
			return legals;
		}
		
		protected boolean checkStops(Board b, int startRow, int startCol, int destRow, int destCol) {
			if(stopConditions.size() == 0) {
				return false;
			}
			
			for(int i = 0; i < stopConditions.size(); i++) {
				if(!stopConditions.get(i).calc(b, startRow, startCol, destRow, destCol)) {
					return false;
				}
			}
			return true;
		}
		

		@Override
		public boolean reachedEndOfSegment(Board b, int startRow, int startCol, Set<? extends LegalAction> legals) {
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int destRow = startRow + m*relStartRow + m*deltaRow*(length-1), destCol = startCol + m*relStartCol + m*deltaCol*(length-1);
			//System.out.printf("\tdesired dest = (%d,%d)%n", destRow, destCol);
			for(LegalAction act : legals) {
				if(act.destRow() == destRow && act.destCol() == destCol) {
					//System.out.println("REOS = TRUE");
					return true;
				}
			}
			//.out.println("REOS = FALSE");
			return false;
		}
	}
}




