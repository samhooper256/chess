package chess.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import chess.base.Board;
import chess.base.LegalAction;
import chess.base.LegalCapture;
import chess.base.LegalMoveAndCapture;
import chess.base.LegalOtherMoveAndCapture;
import chess.base.LegalPromotion;
import chess.base.LegalSummon;
import chess.base.Piece;

public abstract class SubMulti extends Action{
	
	public static final Method[] subMultiCreationMethods;
	
	static {
		subMultiCreationMethods = new Method[7];
		
		try {
			subMultiCreationMethods[0] = SubMulti.class.getMethod("mnc", Flag.class, int.class, int.class, Condition[].class);
			subMultiCreationMethods[1] = SubMulti.class.getMethod("omnc", Flag.class, int.class, int.class,
					int.class, int.class, Condition[].class);
			subMultiCreationMethods[2] = SubMulti.class.getMethod("capRel", Flag.class, int.class, int.class, Condition[].class);
			subMultiCreationMethods[3] = SubMulti.class.getMethod("capRad", Flag.class, int.class, boolean.class,
					boolean.class, Condition[].class);
			subMultiCreationMethods[4] = SubMulti.class.getMethod("promo", ArrayList.class, Condition[].class);
			subMultiCreationMethods[5] = SubMulti.class.getMethod("summonRel", Flag.class, int.class, int.class,
					ArrayList.class, Condition[].class);
			subMultiCreationMethods[6] = SubMulti.class.getMethod("summonRad", Flag.class, int.class, boolean.class,
					boolean.class, ArrayList.class, Condition[].class);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1996745756513613004L;
	@AFC(name="Move And Capture", paramDescriptions={"relative to", "relative row", "relative column"})
	public static MNC mnc(Flag relativeTo, int relRow, int relCol, Condition... cons) {
		return new MNC(relativeTo, relRow, relCol, cons);
	}
	
	@AFC(name="Other Move And Capture", paramDescriptions={"relative to", "relative start row", "relative start column",
			"relative destination row", "relative destination column"})
	public static OMNC omnc(Flag relativeTo, int relsr, int relsc, int reldr, int reldc, Condition... cons) {
		return new OMNC(relativeTo, relsr, relsc, reldr, reldc, cons);
	}
	
	@AFC(name="Relative Capture", paramDescriptions={"relative to", "relative row", "relative column"})
	public static CapRel capRel(Flag relativeTo, int relRow, int relCol, Condition... cons) {
		return new CapRel(relativeTo, relRow, relCol, cons);
	}
	
	@AFC(name="Radius Capture", paramDescriptions= {"relative to", "radius", "fill", "include self"})
	public static CapRad capRad(Flag relativeTo, int radius, boolean fill, boolean includeSelf, Condition... cons) {
		return new CapRad(relativeTo, radius, fill, includeSelf, cons);
	}
	
	@AFC(name="Promotion", paramDescriptions={"promotion options"})
	public static Promo promo(ArrayList<String> options, Condition... cons) {
		return new Promo(options, cons);
	}
	
	@AFC(name="Relative Summon", paramDescriptions={"relative to", "relative row", "relative column", "summon options"})
	public static SummonRel summonRel(Flag rt, int rr, int rc, ArrayList<String> optns, Condition... cons) {
		return new SummonRel(rt, rr, rc, optns, cons);
	}
	
	@AFC(name="Radius Summon", paramDescriptions={"relative to", "radius", "fill", "include self", "summon options"})
	public static SummonRad summonRad(Flag relativeTo, int radius, boolean fill, boolean includeSelf, ArrayList<String> optns, Condition... cons) {
		System.out.printf("Args passed to summonRad: %s, %d, %s, %s, %s, %s", relativeTo, radius, fill, includeSelf, optns, Arrays.deepToString(cons));
		return new SummonRad(relativeTo, radius, fill, includeSelf, optns, cons);
	}
	
	public static class MNC extends SubMulti implements RelativeJumpAction{
		/**
		 * 
		 */
		private static final long serialVersionUID = -4576845262225150818L;
		public final Flag relativeTo;
		public final int relRow, relCol;
		
		private MNC(Flag rt, int rr, int rc, Condition... cons) {
			if(rt != Flag.ORIGIN && rt != Flag.DESTINATION) {
				throw new IllegalArgumentException("Flag must be Flag.ORIGIN or Flag.DESTINATION. Flag was: " + rt);
			}
			this.relativeTo = rt;
			this.relRow = rr;
			this.relCol = rc;
			this.addAllConditions(cons);
		}
		
		public Method getMethod() {
			return subMultiCreationMethods[0];
		}
		
		public static Method getCreationMethod() {
			return subMultiCreationMethods[0];
		}
		
		public static Class<? extends LegalAction> correspondingLegal(){
			return LegalMoveAndCapture.class;
		}
		
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol, int destRow, int destCol) {
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int row, col;
			if(relativeTo == Flag.ORIGIN) {
				row = startRow + m*relRow;
				col = startCol + m*relCol;
			}
			else {
				row = destRow + m*relRow;
				col = startCol + m*relCol;
			}
			
			if(b.inBounds(row, col) && checkConditions(b, startRow, startCol, row, col)) {
				return Collections.singleton(new LegalMoveAndCapture(row, col));
			}
			else {
				return Collections.emptySet();
			}
		}
		@Override
		public Object[] getReconstructionParameters() {
			return new Object[] {relativeTo, relRow, relCol};
		}
	}
	
	public static class OMNC extends SubMulti implements RelativeJumpAction{
		/**
		 * 
		 */
		private static final long serialVersionUID = -755367866536044169L;
		public final Flag relativeTo;
		public final int otherRelStartRow, otherRelStartCol, otherRelDestRow, otherRelDestCol;
		private OMNC(Flag rt, int orsr, int orsc, int ordr, int ordc, Condition... cons) {
			if(rt != Flag.ORIGIN && rt != Flag.DESTINATION) {
				throw new IllegalArgumentException("Flag must be Flag.ORIGIN or Flag.DESTINATION. Flag was: " + rt);
			}
			this.otherRelStartRow = orsr;
			this.otherRelStartCol = orsc;
			this.otherRelDestRow = ordr;
			this.otherRelDestCol = ordc;
			this.relativeTo = rt;
			this.addAllConditions(cons);
		}
		
		/* *
		 * NOTE: Like in OhterMoveAndCaptureAction, the startRow/Col passed to checkConditions is the
		 * startRow/Col of the "other" piece that's being moved, not the "acting" piece in whose ActionTree this exists.
		 * 
		 * It also REQUIRES a piece on the start.
		 */
		
		
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol, int destRow, int destCol) {
			if(relativeTo != Flag.ORIGIN && relativeTo != Flag.DESTINATION) {
				throw new IllegalArgumentException("Flag must be Flag.ORIGIN or Flag.DESTINATION. Flag was: " + relativeTo);
			}
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int sr, sc, dr, dc;
			if(relativeTo == Flag.ORIGIN) {
				sr = startRow + m*otherRelStartRow;
				sc = startCol + m*otherRelStartCol;
				dr = startRow + m*otherRelDestRow;
				dc = startCol + m*otherRelDestCol;
			}
			else {
				sr = destRow + m*otherRelStartRow;
				sc = destCol + m*otherRelStartCol;
				dr = destRow + m*otherRelDestRow;
				dc = destCol + m*otherRelDestCol;
			}
			
			if(b.inBounds(sr, sc) && b.inBounds(dr, dc) && b.getPieceAt(sr, sc) != null && checkConditions(b, sr, sc, dr, dc)) {
				return Collections.singleton(new LegalOtherMoveAndCapture(sr, sc, dr, dc));
			}
			else {
				return Collections.emptySet();
			}
		}
		
		public Method getMethod() {
			return subMultiCreationMethods[1];
		}
		
		public static Method getCreationMethod() {
			return subMultiCreationMethods[1];
		}
		
		public static Class<? extends LegalAction> correspondingLegal(){
			return LegalOtherMoveAndCapture.class;
		}
		
		@Override
		public Object[] getReconstructionParameters() {
			return new Object[] {relativeTo, otherRelStartRow, otherRelStartCol, otherRelDestRow, otherRelDestCol};
		}
	}

	public static class CapRel extends SubMulti implements RelativeJumpAction{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1423861083679743582L;
		public final Flag relativeTo;
		public final int relRow, relCol;
		private CapRel(Flag rt, int rr, int rc, Condition... cons) {
			if(rt != Flag.ORIGIN && rt != Flag.DESTINATION) {
				throw new IllegalArgumentException("Flag must be Flag.ORIGIN or Flag.DESTINATION. Flag was: " + rt);
			}
			this.relativeTo = rt;
			this.relRow = rr;
			this.relCol = rc;
			this.addAllConditions(cons);
		}
		
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol, int destRow, int destCol) {
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int row, col;
			if(relativeTo == Flag.ORIGIN) {
				row = startRow + m*relRow;
				col = startCol + m*relCol;
			}
			else {
				row = destRow + m*relRow;
				col = destCol + m*relCol;
			}
			
			if(b.inBounds(row, col) && checkConditions(b, startRow, startCol, row, col)) {
				return Collections.singleton(new LegalCapture(row, col));
			}
			else {
				return Collections.emptySet();
			}
		}
		
		public Method getMethod() {
			return subMultiCreationMethods[2];
		}
		
		public static Method getCreationMethod() {
			return subMultiCreationMethods[2];
		}
		
		public static Class<? extends LegalAction> correspondingLegal(){
			return LegalCapture.class;
		}
		
		@Override
		public Object[] getReconstructionParameters() {
			return new Object[] {relativeTo, relRow, relCol};
		}
	}
	
	public static class CapRad extends SubMulti implements RadiusAction{
		/**
		 * 
		 */
		private static final long serialVersionUID = 6194146969384968159L;
		public final Flag relativeTo;
		public final int radius;
		public final boolean fill, includeSelf;
		public CapRad(Flag rt, int rad, boolean fil, boolean is, Condition... cons) {
			if(rt != Flag.ORIGIN && rt != Flag.DESTINATION) {
				throw new IllegalArgumentException("Flag must be Flag.ORIGIN or Flag.DESTINATION. Flag was: " + rt);
			}
			this.relativeTo = rt;
			this.radius = rad;
			this.fill = fil;
			this.includeSelf = is;
			this.addAllConditions(cons);
		}
		@Override
		protected Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol, int destRow,
				int destCol) {
			Set<LegalCapture> legals = new HashSet<>();
			int finalRow, finalCol;
			if(relativeTo == Flag.ORIGIN) {
				finalRow = startRow;
				finalCol = startCol;
			}
			else {
				finalRow = destRow;
				finalCol = destCol;
			}
			if(fill) {
				for(int i = 0; i < 2 * radius + 1; i++) {
					int r = finalRow - radius + i;
					for(int c = finalCol - radius, j = 0; j < 2 * radius + 1; j++, c++) {
						if(b.inBounds(r, c)) {
							if(r == finalRow && c == finalCol && !includeSelf) {
								continue;
							}
							if(checkConditions(b, finalRow, finalCol, r, c)) {
								legals.add(new LegalCapture(r,c));
							}
						}
					}
				}
			}
			else {
				int baseC = finalCol - radius;
				int baseR = finalRow - radius;
				for(int i = 0, r = finalRow - radius; i < 2 * radius + 1; i++) {
					int c = baseC + i;
					if(b.inBounds(r,c)) {
						if(checkConditions(b, finalRow, finalCol, r, c)) {
							legals.add(new LegalCapture(r,c));
						}
					}
					
				}
				for(int i = 0, r = finalRow + radius; i < 2 * radius + 1; i++) {
					int c = baseC + i;
					if(b.inBounds(r,c)) {
						if(checkConditions(b, finalRow, finalCol, r, c)) {
							legals.add(new LegalCapture(r,c));
						}
					}
				}
				for(int i = 1, c = finalCol - radius; i < 2 * radius; i++) {
					int r = baseR + i;
					if(b.inBounds(r, c)) {
						if(checkConditions(b, finalRow, finalCol, r, c)) {
							legals.add(new LegalCapture(r,c));
						}
					}
				}
				for(int i = 1, c = finalCol + radius; i < 2 * radius; i++) {
					int r = baseR + i;
					if(b.inBounds(r, c)) {
						if(checkConditions(b, finalRow, finalCol, r, c)) {
							legals.add(new LegalCapture(r,c));
						}
					}
				}
			}
			
			return legals;
		}
		
		public Method getMethod() {
			return subMultiCreationMethods[3];
		}
		
		public static Method getCreationMethod() {
			return subMultiCreationMethods[3];
		}
		
		public static Class<? extends LegalAction> correspondingLegal(){
			return LegalCapture.class;
		}
		
		@Override
		public Object[] getReconstructionParameters() {
			return new Object[] {relativeTo, radius, fill, includeSelf};
		}
	}
	
	public static class Promo extends SubMulti{
		/**
		 * 
		 */
		private static final long serialVersionUID = 6958577812004211549L;
		public final ArrayList<String> options;
		public Promo(ArrayList<String> options, Condition... cons) {
			this.options = options;
			this.addAllConditions(cons);
		}
		
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol, int destRow, int destCol) {
			
			if(checkConditions(b, startRow, startCol, startRow, startCol)) {
				return Collections.singleton(new LegalPromotion(startRow, startCol, options));
			}
			else {
				return Collections.emptySet();
			}
		}
		
		public Method getMethod() {
			return subMultiCreationMethods[4];
		}
		
		public static Method getCreationMethod() {
			return subMultiCreationMethods[4];
		}
		
		public static Class<? extends LegalAction> correspondingLegal(){
			return LegalPromotion.class;
		}
		
		@Override
		public Object[] getReconstructionParameters() {
			return new Object[] {options};
		}
	}
	
	public static class SummonRel extends SubMulti implements RelativeJumpAction{
		/**
		 * 
		 */
		private static final long serialVersionUID = -8645706655336055337L;
		public final Flag relativeTo;
		public final int relativeRow, relativeCol;
		public final ArrayList<String> options;
		private SummonRel(Flag rt, int rr, int rc, ArrayList<String> ops, Condition... cons) {
			if(rt != Flag.ORIGIN && rt != Flag.DESTINATION) {
				throw new IllegalArgumentException("Flag must be Flag.ORIGIN or Flag.DESTINATION. Flag was: " + rt);
			}
			this.relativeTo = rt;
			this.relativeRow = rr;
			this.relativeCol = rc;
			this.options = ops;
			this.addAllConditions(cons);
		}
		@Override
		protected Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol, int destRow, int destCol) {
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int row, col;
			if(relativeTo == Flag.ORIGIN) {
				row = startRow + m*relativeRow;
				col = startCol + m*relativeCol;
			}
			else {
				row = destRow + m*relativeRow;
				col = destCol + m*relativeCol;
			}
			
			if(b.inBounds(row, col) && checkConditions(b, startRow, startCol, row, col)) {
				return Collections.singleton(new LegalSummon(row, col, options));
			}
			else {
				return Collections.emptySet();
			}
		}
		
		public Method getMethod() {
			return subMultiCreationMethods[5];
		}
		
		public static Method getCreationMethod() {
			return subMultiCreationMethods[5];
		}
		
		public static Class<? extends LegalAction> correspondingLegal(){
			return LegalSummon.class;
		}
		
		@Override
		public Object[] getReconstructionParameters() {
			return new Object[] {relativeTo, relativeRow, relativeCol, options};
		}
	}
	
	public static class SummonRad extends SubMulti implements RadiusAction{
		/**
		 * 
		 */
		private static final long serialVersionUID = 2775004933263978789L;
		public final Flag relativeTo;
		public final int radius;
		public final boolean fill, includeSelf;
		public final ArrayList<String> options;
		public SummonRad(Flag rt, int rad, boolean fil, boolean is, ArrayList<String> ops, Condition... cons) {
			if(rt != Flag.ORIGIN && rt != Flag.DESTINATION) {
				throw new IllegalArgumentException("Flag must be Flag.ORIGIN or Flag.DESTINATION. Flag was: " + rt);
			}
			this.relativeTo = rt;
			this.radius = rad;
			this.fill = fil;
			this.includeSelf = is;
			this.options = ops;
			this.addAllConditions(cons);
		}
		@Override
		protected Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol, int destRow,
				int destCol) {
			Set<LegalSummon> legals = new HashSet<>();
			int finalRow, finalCol;
			if(relativeTo == Flag.ORIGIN) {
				finalRow = startRow;
				finalCol = startCol;
			}
			else {
				finalRow = destRow;
				finalCol = destCol;
			}
			if(fill) {
				for(int i = 0; i < 2 * radius + 1; i++) {
					int r = finalRow - radius + i;
					for(int c = finalCol - radius, j = 0; j < 2 * radius + 1; j++, c++) {
						if(b.inBounds(r, c)) {
							if(r == finalRow && c == finalCol && !includeSelf) {
								continue;
							}
							if(checkConditions(b, finalRow, finalCol, r, c)) {
								legals.add(new LegalSummon(r,c, options));
							}
						}
					}
				}
			}
			else {
				int baseC = finalCol - radius;
				int baseR = finalRow - radius;
				for(int i = 0, r = finalRow - radius; i < 2 * radius + 1; i++) {
					int c = baseC + i;
					if(b.inBounds(r,c)) {
						if(checkConditions(b, finalRow, finalCol, r, c)) {
							legals.add(new LegalSummon(r,c, options));
						}
					}
					
				}
				for(int i = 0, r = finalRow + radius; i < 2 * radius + 1; i++) {
					int c = baseC + i;
					if(b.inBounds(r,c)) {
						if(checkConditions(b, finalRow, finalCol, r, c)) {
							legals.add(new LegalSummon(r,c, options));
						}
					}
				}
				for(int i = 1, c = finalCol - radius; i < 2 * radius; i++) {
					int r = baseR + i;
					if(b.inBounds(r, c)) {
						if(checkConditions(b, finalRow, finalCol, r, c)) {
							legals.add(new LegalSummon(r,c, options));
						}
					}
				}
				for(int i = 1, c = finalCol + radius; i < 2 * radius; i++) {
					int r = baseR + i;
					if(b.inBounds(r, c)) {
						if(checkConditions(b, finalRow, finalCol, r, c)) {
							legals.add(new LegalSummon(r,c, options));
						}
					}
				}
			}
			
			return legals;
		}
		
		public Method getMethod() {
			return subMultiCreationMethods[6];
		}
		
		public static Method getCreationMethod() {
			return subMultiCreationMethods[6];
		}
		
		public static Class<? extends LegalAction> correspondingLegal(){
			return LegalSummon.class;
		}
		
		@Override
		public Object[] getReconstructionParameters() {
			return new Object[] {relativeTo, radius, fill, includeSelf, options};
		}
	}
	
	
	@Override
	public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
		throw new UnsupportedOperationException("getLegals must be called with a startRow/Col AND destRow/Col");
	}
	protected abstract Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol, int destRow,
			int destCol);
}
