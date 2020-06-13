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
import chess.base.LegalSummon;
import chess.base.Piece;

public abstract class SummonAction extends Action{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7028404081965570002L;
	public final ArrayList<String> options;
	
	@User(params={"relative row", "relative column", "summon options"})
	public static RelativeSummonAction relative(int relRow, int relCol, ArrayList<String> ops, Condition... cons) {
		if(ops.size() == 0) {
			throw new IllegalArgumentException("A promotion action must have at least one option (options.size() == 0)");
		}
		return new RelativeSummonAction(relRow, relCol, ops, cons);
	}
	
	@User(params={"delta row", "delta column", "summon options"})
	public static LineSummonAction line(int dr, int dc, ArrayList<String> ops, Condition...cons) {
		if(ops.size() == 0) {
			throw new IllegalArgumentException("A promotion action must have at least one option (options.size() == 0)");
		}
		return new LineSummonAction(dr, dc, ops, cons);
	}
	
	@User(params={"relative start row", "relative start column", "delta row", "delta column", "requires start to be on board", "summon options"})
	public static RelativeLineSummonAction relLine(int relStartRow, int relStartCol, int dr, int dc,
			boolean requiresOnBoardStart, ArrayList<String> ops, Condition... cons) {
		if(ops.size() == 0) {
			throw new IllegalArgumentException("A promotion action must have at least one option (options.size() == 0)");
		}
		return new RelativeLineSummonAction(relStartRow, relStartCol, dr, dc, requiresOnBoardStart, ops, cons);
	}
	
	@User(params={"relative start row", "relative start column", "delta row", "delta column", "segment length", "requires start to be on board", "summon options"})
	public static RelativeSegmentSummonAction segment(int relStartRow, int relStartCol, int dr, int dc, int length,
			boolean requiresOnBoardStart, ArrayList<String> ops, Condition... cons) {
		if(ops.size() == 0) {
			throw new IllegalArgumentException("A promotion action must have at least one option (options.size() == 0)");
		}
		return new RelativeSegmentSummonAction(relStartRow, relStartCol, dr, dc, length, requiresOnBoardStart, ops, cons);
	}
	
	@User(params={"radius", "fill", "include self", "summon options"})
	public static RadiusSummonAction radius(int radius, boolean fill, boolean includeSelf, ArrayList<String> ops, Condition... cons) {
		if(ops.size() == 0) {
			throw new IllegalArgumentException("A promotion action must have at least one option (options.size() == 0)");
		}
		return new RadiusSummonAction(radius, fill, includeSelf, ops, cons);
	}
	
	private static List<Class<? extends Action>> immediateSubtypes = 
			Collections.unmodifiableList(Arrays.asList(
					RelativeSummonAction.class,
					LineSummonAction.class,
					RelativeLineSummonAction.class,
					RelativeSegmentSummonAction.class,
					RadiusSummonAction.class
					
			));
	public static List<Class<? extends Action>> getImmediateSubtypes(){
		return immediateSubtypes;
	}
	
	public static String getActionName() {
		return "Summon";
	}
	
	protected SummonAction(ArrayList<String> ops) {
		this.options = ops;
	}
	
	public static class RelativeSummonAction extends SummonAction implements RelativeJumpAction{
		/**
		 * 
		 */
		private static final long serialVersionUID = 525503007331799059L;
		private static Method CREATION_METHOD;
		static {
			try {
				CREATION_METHOD = SummonAction.class.getMethod("relative", int.class, int.class, ArrayList.class, Condition[].class);
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		public final int relRow;
		public final int relCol;
		
		private RelativeSummonAction(int relRow, int relCol, ArrayList<String> options, Condition... cons) {
			super(options);
			this.relRow = relRow;
			this.relCol = relCol;
			this.addAllConditions(cons);
		}
		
		public static String getVariant() {
			return "Relative";
		}
		
		@Override
		public Method getMethod() {
			return CREATION_METHOD;
		}
		
		public static Method getCreationMethod() {
			return CREATION_METHOD;
		}

		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int destRow = startRow + m*relRow, destCol = startCol + m*relCol;
			
			if(b.inBounds(destRow, destCol) && checkConditions(b, startRow, startCol, destRow, destCol)) {
				return Collections.singleton(new LegalSummon(destRow, destCol, options));
			}
			else {
				Set<? extends LegalAction> s = Collections.emptySet();
				return s;
			}
		}
		
		@Override
		public Object[] getReconstructionParameters() {
			return new Object[] {relRow, relCol, options};
		}
	}
	
	public static class LineSummonAction extends SummonAction implements LineAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6964271500790227250L;
		private static Method CREATION_METHOD;
		static {
			try {
				CREATION_METHOD = SummonAction.class.getMethod("line", int.class, int.class, ArrayList.class, Condition[].class);
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		public final int deltaRow, deltaCol;
		private ArrayList<Condition> stopConditions;
		
		public LineSummonAction(int dr, int dc, ArrayList<String> options, Condition... cons) {
			super(options);
			this.deltaRow = dr;
			this.deltaCol = dc;
			stopConditions = new ArrayList<>();
			this.addAllConditions(cons);
		}
		
		@Override
		public LineSummonAction stops(Condition... cons) {
			return (LineSummonAction) LineAction.super.stops(cons);
		}
		
		public static String getVariant() {
			return "Line";
		}
		
		@Override
		public Method getMethod() {
			return CREATION_METHOD;
		}
		
		public static Method getCreationMethod() {
			return CREATION_METHOD;
		}
		
		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			Set<LegalSummon> legals = new HashSet<>();
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int row = startRow + m*deltaRow;
			int col = startCol + m*deltaCol;
			while(b.inBounds(row, col)) {
				if(checkConditions(b, startRow, startCol, row, col)) {
					legals.add(new LegalSummon(row, col, options));
				}
				if(checkStops(b, startRow, startCol, row, col)) {
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
			return new Object[] {deltaRow, deltaCol, options};
		}
	}
	
	public static class RelativeLineSummonAction extends LineSummonAction{
		/**
		 * 
		 */
		private static final long serialVersionUID = 3665438309855629292L;
		private static Method CREATION_METHOD;
		static {
			try {
				CREATION_METHOD = SummonAction.class.getMethod("relLine", int.class, int.class, int.class, int.class, boolean.class, ArrayList.class, Condition[].class);
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		public final int relStartRow, relStartCol;
		public final boolean requiresOnBoardStart;
		
		public RelativeLineSummonAction(int relsr, int relsc, int dr, int dc, boolean onBoardStart,
				ArrayList<String> options, Condition... cons) {
			super(dr, dc, options, cons);
			relStartRow = relsr;
			relStartCol = relsc;
			requiresOnBoardStart = onBoardStart;
		}
		
		@Override
		public RelativeLineSummonAction stops(Condition... cons) {
			return (RelativeLineSummonAction) super.stops(cons);
		}
		
		public static String getVariant() {
			return "Relative Line";
		}
		
		@Override
		public Method getMethod() {
			return CREATION_METHOD;
		}
		
		public static Method getCreationMethod() {
			return CREATION_METHOD;
		}
		
		/*
		 * Important: Stop conditions are checked AFTER the conditions for adding a legal move. Thus,
		 * the first destination that meets the stop conditions WILL BE ADDED.
		 * */
		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			Set<LegalSummon> legals = new HashSet<>();
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int row = startRow + m*relStartRow;
			int col = startCol + m*relStartCol;
			if(requiresOnBoardStart) {
				while(b.inBounds(row, col)) {
					if(checkConditions(b, startRow, startCol, row, col)) {
						legals.add(new LegalSummon(row, col, options));
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
							legals.add(new LegalSummon(row, col, options));
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
			return new Object[] {relStartRow, relStartCol, deltaRow, deltaCol, requiresOnBoardStart, options};
		}
	}
	
	public static class RelativeSegmentSummonAction extends SummonAction
			implements RelativeSegmentAction{
		/**
		 * 
		 */
		private static final long serialVersionUID = 4828990037209511659L;
		private static Method CREATION_METHOD;
		static {
			try {
				CREATION_METHOD = SummonAction.class.getMethod("segment", int.class, int.class, int.class, int.class, int.class, boolean.class, ArrayList.class, Condition[].class);
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		public final int relStartRow, relStartCol, deltaRow, deltaCol, length;
		public final boolean requiresOnBoardStart;
		private ArrayList<Condition> stopConditions;
		
		public RelativeSegmentSummonAction(int relStartRow, int relStartCol, int deltaRow, int deltaCol, int length,
				boolean requiresOnBoardStart, ArrayList<String> options, Condition... cons) {
			super(options);
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
		
		public RelativeSegmentSummonAction stops(Condition... cons) {
			return (RelativeSegmentSummonAction) RelativeSegmentAction.super.stops(cons);
		}
		
		public static String getVariant() {
			return "Relative Segment";
		}
		
		@Override
		public Method getMethod() {
			return CREATION_METHOD;
		}
		
		public static Method getCreationMethod() {
			return CREATION_METHOD;
		}
		
		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			Set<LegalSummon> legals = new HashSet<>();
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
					legals.add(new LegalSummon(row, col, options));
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
		
		public Collection<Condition> getStopConditions() {
			return stopConditions;
		}

		@Override
		public boolean getRequiresOnBoardStart() {
			return requiresOnBoardStart;
		}

		@Override
		public Object[] getReconstructionParameters() {
			return new Object[] {relStartRow, relStartCol, deltaRow, deltaCol, length, requiresOnBoardStart, options};
		}
		
		
	}
	
	public static class RadiusSummonAction extends SummonAction implements RadiusAction{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1707015099806534041L;
		private static Method CREATION_METHOD;
		static {
			try {
				CREATION_METHOD = SummonAction.class.getMethod("radius", int.class, boolean.class, boolean.class, ArrayList.class, Condition[].class);
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		public final int radius;
		public final boolean fill;
		public final boolean includeSelf;
		
		public RadiusSummonAction(int radius, boolean fill, boolean includeSelf, ArrayList<String> options,
				Condition... cons) {
			super(options);
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
		
		@Override
		public Method getMethod() {
			return CREATION_METHOD;
		}
		
		public static Method getCreationMethod() {
			return CREATION_METHOD;
		}
		
		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			Set<LegalSummon> legals = new HashSet<>();
			if(fill) {
				for(int i = 0; i < 2 * radius + 1; i++) {
					int r = startRow - radius + i;
					for(int c = startCol - radius, j = 0; j < 2 * radius + 1; j++, c++) {
						if(b.inBounds(r, c)) {
							if(r == startRow && c == startCol && !includeSelf) {
								continue;
							}
							if(checkConditions(b, startRow, startCol, r, c)) {
								legals.add(new LegalSummon(r, c, options));
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
					if(b.inBounds(r, c)) {
						if(checkConditions(b, startRow, startCol, r, c)) {
							legals.add(new LegalSummon(r, c, options));
						}
					}
					
				}
				for(int i = 0, r = startRow + radius; i < 2 * radius + 1; i++) {
					int c = baseC + i;
					if(b.inBounds(r, c)) {
						if(checkConditions(b, startRow, startCol, r, c)) {
							legals.add(new LegalSummon(r, c, options));
						}
					}
				}
				for(int i = 1, c = startCol - radius; i < 2 * radius; i++) {
					int r = baseR + i;
					if(b.inBounds(r, c)) {
						if(checkConditions(b, startRow, startCol, r, c)) {
							legals.add(new LegalSummon(r, c, options));
						}
					}
				}
				for(int i = 1, c = startCol + radius; i < 2 * radius; i++) {
					int r = baseR + i;
					if(b.inBounds(r, c)) {
						if(checkConditions(b, startRow, startCol, r, c)) {
							legals.add(new LegalSummon(r, c, options));
						}
					}
				}
			}
			
			return legals;
		}
		
		@Override
		public Object[] getReconstructionParameters() {
			return new Object[] {radius, fill, includeSelf, options};
		}
		
	}
}
