package chess.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import chess.base.Board;
import chess.base.LegalAction;
import chess.base.LegalCapture;
import chess.base.LegalCapture;
import chess.base.Piece;

public abstract class CaptureAction extends chess.util.Action{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1468552990586649613L;
	
	@User(params={"relative row", "relative column"})
	public static RelativeCaptureAction relative(int relRow, int relCol, Condition... cons) {
		return new RelativeCaptureAction(relRow, relCol, cons);
	}
	
	@User(params={"delta row", "delta column"})
	public static LineCaptureAction line(int deltaRow, int deltaCol, Condition... cons) {
		return new LineCaptureAction(deltaRow, deltaCol, cons);
	}
	
	@User(params={"relative start row", "relative start column", "delta row", "delta column", "requires start to be on board"})
	public static RelativeLineCaptureAction relLine(int relStartRow, int relStartCol, int dr, int dc, boolean requiresOnBoardStart,
			Condition... cons) {
		return new RelativeLineCaptureAction(relStartRow, relStartCol, dr, dc, requiresOnBoardStart, cons);
	}
	
	@User(params={"relative start row", "relative start column", "delta row", "delta column", "length", "segment length", "requires start to be on board"})
	public static RelativeSegmentCaptureAction segment(int relStartRow, int relStartCol, int dr, int dc, int length,
			boolean requiresOnBoardStart, Condition... cons) {
		return new RelativeSegmentCaptureAction(relStartRow, relStartCol, dr, dc, length, requiresOnBoardStart, cons);
	}
	
	@User(params={"radius", "fill", "include self"})
	public static RadiusCaptureAction radius(int radius, boolean fill, boolean includeSelf, Condition... cons) {
		return new RadiusCaptureAction(radius, fill, includeSelf, cons);
	}
	
	public static String getActionName() {
		return "Capture";
	}
	
	private static List<Class<? extends Action>> immediateSubtypes = 
			Collections.unmodifiableList(Arrays.asList(
					RelativeCaptureAction.class,
					LineCaptureAction.class,
					RelativeLineCaptureAction.class,
					RelativeSegmentCaptureAction.class,
					RadiusCaptureAction.class
					
			));
	public static List<Class<? extends Action>> getImmediateSubtypes(){
		return immediateSubtypes;
	}
	
	public static class RelativeCaptureAction extends CaptureAction implements RelativeJumpAction{
		/**
		 * 
		 */
		private static final long serialVersionUID = 8353535313029856877L;
		public final int relRow, relCol;
		public RelativeCaptureAction(int relRow, int relCol, Condition... cons) {
			this.relRow = relRow;
			this.relCol = relCol;
			this.addAllConditions(cons);
		}
		
		public static String getVariant() {
			return "Relative";
		}
		
		public static Method getCreationMethod() throws NoSuchMethodException, SecurityException {
			return CaptureAction.class.getMethod("relative", int.class, int.class, Condition[].class);
		}
		
		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int destRow = startRow + m*relRow, destCol = startCol + m*relCol;
			//System.out.printf("getting legals from rel M&C<%d,%d> : (%d,%d) -> (%d,%d)%n", relRow, relCol, startRow, startCol,
			//		destRow,destCol);
			if(b.inBounds(destRow, destCol) && checkConditions(b, startRow, startCol, destRow, destCol)) {
				return Collections.singleton(new LegalCapture(destRow, destCol));
			}
			else {
				//System.out.println("\trelative M&C failed conditions check");
				Set<? extends LegalAction> s = Collections.emptySet();
				return s;
			}
		}
		
		@Override
		public Object[] getReconstructionParameters() {
			return new Object[] {relRow, relCol};
		}
	}

	public static class LineCaptureAction extends CaptureAction implements LineAction{
		/**
		 * 
		 */
		private static final long serialVersionUID = 6584141374081901533L;
		public final int deltaRow, deltaCol;
		private ArrayList<Condition> stopConditions;
		
		public LineCaptureAction(int dr, int dc, Condition... cons) {
			deltaRow = dr;
			deltaCol = dc;
			stopConditions = new ArrayList<>();
			this.addAllConditions(cons);
		}
		
		@Override
		public LineCaptureAction stops(Condition... cons) {
			return (LineCaptureAction) LineAction.super.stops(cons);
		}
		
		public static Method getCreationMethod() throws NoSuchMethodException, SecurityException {
			return CaptureAction.class.getMethod("line", int.class, int.class, Condition[].class);
		}
		
		public static String getVariant() {
			return "Line";
		}
		
		/*
		 * Important: Stop conditions are checked AFTER the conditions for adding a legal move. Thus,
		 * the first destination that meets the stop conditions WILL BE ADDED.
		 * */
		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			Set<LegalCapture> legals = new HashSet<>();
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int row = startRow + m*deltaRow;
			int col = startCol + m*deltaCol;
			while(b.inBounds(row, col)) {
				if(checkConditions(b, startRow, startCol, row, col)) {
					legals.add(new LegalCapture(row,col));
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
		@Override
		public Object[] getReconstructionParameters() {
			return new Object[] {deltaRow, deltaCol};
		}
	}
	
	public static class RelativeLineCaptureAction extends LineCaptureAction{
		/**
		 * 
		 */
		private static final long serialVersionUID = -7904039124287966499L;
		public final int relStartRow, relStartCol;
		public final boolean requiresOnBoardStart;
		
		public RelativeLineCaptureAction(int relsr, int relsc, int dr, int dc, boolean onBoardStart, Condition... cons) {
			super(dr, dc, cons);
			relStartRow = relsr;
			relStartCol = relsc;
			requiresOnBoardStart = onBoardStart;
		}
		
		public static Method getCreationMethod() throws NoSuchMethodException, SecurityException {
			return CaptureAction.class.getMethod("relLine", int.class, int.class, int.class, int.class, boolean.class, Condition[].class);
		}
		
		@Override
		public RelativeLineCaptureAction stops(Condition... cons) {
			return (RelativeLineCaptureAction) super.stops(cons);
		}
		
		public static String getVariant() {
			return "Relative Line";
		}
		
		/*
		 * Important: Stop conditions are checked AFTER the conditions for adding a legal move. Thus,
		 * the first destination that meets the stop conditions WILL BE ADDED.
		 * */
		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			Set<LegalCapture> legals = new HashSet<>();
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int row = startRow + m*relStartRow;
			int col = startCol + m*relStartCol;
			if(requiresOnBoardStart) {
				while(b.inBounds(row, col)) {
					if(checkConditions(b, startRow, startCol, row, col)) {
						legals.add(new LegalCapture(row,col));
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
							legals.add(new LegalCapture(row,col));
						}
						if(checkStops(b, startRow, startCol, row, col)){
							//System.out.println("Broke bc stops");
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
		
		@Override
		public Object[] getReconstructionParameters() {
			return new Object[] {relStartRow, relStartCol, deltaRow, deltaCol, requiresOnBoardStart};
		}
	}
	
	public static class RelativeSegmentCaptureAction extends CaptureAction
			implements RelativeSegmentAction{
		/**
		 * 
		 */
		private static final long serialVersionUID = 7121333223961890443L;
		public final int relStartRow, relStartCol, deltaRow, deltaCol, length;
		public final boolean requiresOnBoardStart;
		private ArrayList<Condition> stopConditions;
		
		public RelativeSegmentCaptureAction(int relStartRow, int relStartCol, int deltaRow, int deltaCol, int length,
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
		
		public static Method getCreationMethod() throws NoSuchMethodException, SecurityException {
			return CaptureAction.class.getMethod("segment", int.class, int.class, int.class, int.class, int.class, boolean.class, Condition[].class);
		}
		
		public RelativeSegmentCaptureAction stops(Condition... cons) {
			return (RelativeSegmentCaptureAction) RelativeSegmentAction.super.stops(cons);
		}
		
		public static String getVariant() {
			return "Relative Segment";
		}

		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			Set<LegalCapture> legals = new HashSet<>();
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
					legals.add(new LegalCapture(row,col));
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
		public boolean getRequiresOnBoardStart() {
			return requiresOnBoardStart;
		}
		
		@Override
		public Object[] getReconstructionParameters() {
			return new Object[] {relStartRow, relStartCol, deltaRow, deltaCol, length, requiresOnBoardStart};
		}
	}
	
	public static class RadiusCaptureAction extends CaptureAction implements RadiusAction{
		/**
		 * 
		 */
		private static final long serialVersionUID = -4480635918843039304L;
		public final int radius;
		public final boolean includeSelf;
		public final boolean fill;
		
		public RadiusCaptureAction(int radius, boolean fill, boolean includeSelf, Condition... cons) {
			if(radius <= 0) {
				throw new IllegalArgumentException("radius cannot be <= 0 (it is " + radius + ")");
			}
			this.radius = radius;
			this.fill = fill;
			this.includeSelf = this.fill ? includeSelf : false; //if fill is not true, there is no way it can include itself.
			this.addAllConditions(cons);
		}
		
		public static String getVariant() {
			return "Radius";
		}
		
		public static Method getCreationMethod() throws NoSuchMethodException, SecurityException {
			return CaptureAction.class.getMethod("radius", int.class, boolean.class, boolean.class, Condition[].class);
		}
		
		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			Set<LegalCapture> legals = new HashSet<>();
			if(fill) {
				for(int i = 0; i < 2 * radius + 1; i++) {
					int r = startRow - radius + i;
					for(int c = startCol - radius, j = 0; j < 2 * radius + 1; j++, c++) {
						if(b.inBounds(r, c)) {
							if(r == startRow && c == startCol && !includeSelf) {
								continue;
							}
							if(checkConditions(b, startRow, startCol, r, c)) {
								legals.add(new LegalCapture(r,c));
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
							legals.add(new LegalCapture(r,c));
						}
					}
					
				}
				for(int i = 0, r = startRow + radius; i < 2 * radius + 1; i++) {
					int c = baseC + i;
					if(b.inBounds(r,c)) {
						if(checkConditions(b, startRow, startCol, r, c)) {
							legals.add(new LegalCapture(r,c));
						}
					}
				}
				for(int i = 1, c = startCol - radius; i < 2 * radius; i++) {
					int r = baseR + i;
					if(b.inBounds(r, c)) {
						if(checkConditions(b, startRow, startCol, r, c)) {
							legals.add(new LegalCapture(r,c));
						}
					}
				}
				for(int i = 1, c = startCol + radius; i < 2 * radius; i++) {
					int r = baseR + i;
					if(b.inBounds(r, c)) {
						if(checkConditions(b, startRow, startCol, r, c)) {
							legals.add(new LegalCapture(r,c));
						}
					}
				}
			}
			
			return legals;
		}

		@Override
		public Object[] getReconstructionParameters() {
			return new Object[] {radius, fill, includeSelf};
		}
		
	}
}