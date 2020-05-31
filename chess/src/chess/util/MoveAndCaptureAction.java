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

public abstract class MoveAndCaptureAction extends chess.util.Action{
	private MoveAndCaptureAction() {}
	
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
			boolean requiresOnBoardStart, Condition... cons) {
		return new RelativeSegmentMoveAndCaptureAction(relStartRow, relStartCol, dr, dc, length, requiresOnBoardStart, cons);
	}
	
	public static RadiusMoveAndCaptureAction radius(int radius, boolean fill, boolean includeSelf, Condition... cons) {
		return new RadiusMoveAndCaptureAction(radius, fill, includeSelf, cons);
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

	public static class LineMoveAndCaptureAction extends MoveAndCaptureAction implements LineAction{
		protected int deltaRow, deltaCol;
		private ArrayList<Condition> stopConditions;
		
		public LineMoveAndCaptureAction(int dr, int dc, Condition... cons) {
			deltaRow = dr;
			deltaCol = dc;
			stopConditions = new ArrayList<>();
			this.addAllConditions(cons);
		}
		
		@Override
		public LineMoveAndCaptureAction stops(Condition... cons) {
			return (LineMoveAndCaptureAction) LineAction.super.stops(cons);
		}
		
		/*
		 * Important: Stop conditions are checked AFTER the conditions for adding a legal move. Thus,
		 * the first destination that meets the stop conditions WILL BE ADDED.
		 * */
		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			Set<LegalMoveAndCapture> legals = new HashSet<>();
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int row = startRow + m*deltaRow;
			int col = startCol + m*deltaCol;
			while(b.inBounds(row, col)) {
				if(checkConditions(b, startRow, startCol, row, col)) {
					legals.add(new LegalMoveAndCapture(row,col));
				}
				if(checkStops(b, startRow, startCol, row, col)){
					break;
				}
				row += m*deltaRow;
				col += m*deltaCol;
			}
			return legals;
		}

		@Override
		public Collection<Condition> getStopConditions() {
			return stopConditions;
		}
	}
	
	public static class RelativeLineMoveAndCaptureAction extends LineMoveAndCaptureAction{
		private int relStartRow, relStartCol;
		private boolean requiresOnBoardStart;
		
		public RelativeLineMoveAndCaptureAction(int relsr, int relsc, int dr, int dc, Condition... cons) {
			super(dr, dc, cons);
			relStartRow = relsr;
			relStartCol = relsc;
			requiresOnBoardStart = false;
		}
		
		public RelativeLineMoveAndCaptureAction(int relsr, int relsc, int dr, int dc, boolean onBoardStart, Condition... cons) {
			super(dr, dc, cons);
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
			Set<LegalMoveAndCapture> legals = new HashSet<>();
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int row = startRow + m*relStartRow;
			int col = startCol + m*relStartCol;
			if(requiresOnBoardStart) {
				while(b.inBounds(row, col)) {
					if(checkConditions(b, startRow, startCol, row, col)) {
						legals.add(new LegalMoveAndCapture(row,col));
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
							legals.add(new LegalMoveAndCapture(row,col));
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
			//System.out.printf("(%d,%d) rlMNC returning legals: %s%n", startRow, startCol, legals);
			return legals;
		}
	}
	
	public static class RelativeSegmentMoveAndCaptureAction extends MoveAndCaptureAction
			implements RelativeSegmentAction{
		private int relStartRow, relStartCol, deltaRow, deltaCol, length;
		private boolean requiresOnBoardStart;
		private ArrayList<Condition> stopConditions;
		
		public RelativeSegmentMoveAndCaptureAction(int relStartRow, int relStartCol, int deltaRow, int deltaCol, int length,
				boolean requiresOnBoardStart, Condition... cons) {
			if(length <= 0) {
				throw new IllegalArgumentException("Length cannot be <= 0 (it is " + length + ")");
			}
			this.relStartRow = relStartRow;
			this.relStartCol = relStartCol;
			this.deltaRow = deltaRow;
			this.deltaCol = deltaCol;
			this.length = length;
			this.requiresOnBoardStart = requiresOnBoardStart;
			stopConditions = new ArrayList<>();
			this.addAllConditions(cons);
		}
		
		public RelativeSegmentMoveAndCaptureAction stops(Condition... cons) {
			return (RelativeSegmentMoveAndCaptureAction) RelativeSegmentAction.super.stops(cons);
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

		@Override
		public Collection<Condition> getStopConditions() {
			return stopConditions;
		}

		@Override
		public void setRequiresOnBoardStart(boolean newRequiresOnBoardStart) {
			requiresOnBoardStart = newRequiresOnBoardStart;
			
		}

		@Override
		public boolean getRequiresOnBoardStart() {
			return requiresOnBoardStart;
		}
	}
	
	public static class RadiusMoveAndCaptureAction extends MoveAndCaptureAction implements RadiusAction{
		private int radius;
		private boolean includeSelf;
		private boolean fill;
		
		public RadiusMoveAndCaptureAction(int radius, boolean fill, boolean includeSelf, Condition... cons) {
			if(radius <= 0) {
				throw new IllegalArgumentException("radius cannot be <= 0 (it is " + radius + ")");
			}
			this.radius = radius;
			this.fill = fill;
			this.includeSelf = this.fill ? includeSelf : false; //if fill is not true, there is no way it can include itself.
			this.addAllConditions(cons);
		}
		
		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			Set<LegalMoveAndCapture> legals = new HashSet<>();
			if(fill) {
				for(int i = 0; i < 2 * radius + 1; i++) {
					int r = startRow - radius + i;
					for(int c = startCol - radius, j = 0; j < 2 * radius + 1; j++, c++) {
						if(b.inBounds(r, c)) {
							if(r == startRow && c == startCol && !includeSelf) {
								continue;
							}
							if(checkConditions(b, startRow, startCol, r, c)) {
								legals.add(new LegalMoveAndCapture(r,c));
							}
						}
					}
				}
			}
			else {
				int baseC = startCol - radius;
				int baseR = startRow - radius;
				for(int i = 0, r = startRow - radius; i < 2 * radius + 1; i++) {
					int c = baseC + i;
					if(b.inBounds(r,c)) {
						if(checkConditions(b, startRow, startCol, r, c)) {
							legals.add(new LegalMoveAndCapture(r,c));
						}
					}
					
				}
				for(int i = 0, r = startRow + radius; i < 2 * radius + 1; i++) {
					int c = baseC + i;
					if(b.inBounds(r,c)) {
						if(checkConditions(b, startRow, startCol, r, c)) {
							legals.add(new LegalMoveAndCapture(r,c));
						}
					}
				}
				for(int i = 1, c = startCol - radius; i < 2 * radius; i++) {
					int r = baseR + i;
					if(b.inBounds(r, c)) {
						if(checkConditions(b, startRow, startCol, r, c)) {
							legals.add(new LegalMoveAndCapture(r,c));
						}
					}
				}
				for(int i = 1, c = startCol + radius; i < 2 * radius; i++) {
					int r = baseR + i;
					if(b.inBounds(r, c)) {
						if(checkConditions(b, startRow, startCol, r, c)) {
							legals.add(new LegalMoveAndCapture(r,c));
						}
					}
				}
			}
			
			return legals;
		}
		
	}
}