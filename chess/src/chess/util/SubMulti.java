package chess.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import chess.base.Board;
import chess.base.LegalAction;
import chess.base.LegalCapture;
import chess.base.LegalMoveAndCapture;
import chess.base.LegalOtherMoveAndCapture;
import chess.base.LegalPromotion;
import chess.base.Piece;

public abstract class SubMulti extends Action{
	
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
	
	@AFC(name="Promotion", paramDescriptions={"promotion options"})
	public static Promo promo(ArrayList<String> options, Condition... cons) {
		return new Promo(options, cons);
	}
	public static class MNC extends SubMulti{
		private int relRow, relCol;
		private Flag relativeTo;
		private MNC(Flag rt, int rr, int rc, Condition... cons) {
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
				col = startCol + m*relCol;
			}
			
			if(b.inBounds(row, col) && checkConditions(b, startRow, startCol, row, col)) {
				return Collections.singleton(new LegalMoveAndCapture(row, col));
			}
			else {
				return Collections.emptySet();
			}
		}
	}
	
	public static class CapRel extends SubMulti{
		private int relRow, relCol;
		private Flag relativeTo;
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
	}
	
	public static class Promo extends SubMulti{
		private ArrayList<String> options;
		public Promo(ArrayList<String> options, Condition... cons) {
			this.options = options;
			this.addAllConditions(cons);
		}
		
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol, int destRow, int destCol) {
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			
			if(checkConditions(b, startRow, startCol, startRow, startCol)) {
				return Collections.singleton(new LegalPromotion(startRow, startCol, options));
			}
			else {
				return Collections.emptySet();
			}
		}
	}
	
	public static class OMNC extends SubMulti{
		private int otherRelStartRow, otherRelStartCol, otherRelDestRow, otherRelDestCol;
		private Flag relativeTo;
		public OMNC(Flag relativeTo, int orsr, int orsc, int ordr, int ordc, Condition... cons) {
			if(relativeTo != Flag.ORIGIN && relativeTo != Flag.DESTINATION) {
				throw new IllegalArgumentException("Flag must be Flag.ORIGIN or Flag.DESTINATION. Flag was: " + relativeTo);
			}
			this.otherRelStartRow = orsr;
			this.otherRelStartCol = orsc;
			this.otherRelDestRow = ordr;
			this.otherRelDestCol = ordc;
			this.relativeTo = relativeTo;
		}
		
		/* *
		 * NOTE: Like in OhterMoveAndCaptureAction, the startRow/Col passed to checkConditions is the
		 * startRow/Col of the "other" piece that's being moved, not the "acting" piece in whose ActionTree this exists.
		 * 
		 * It also REQUIRES a piece on the start.
		 */
		
		
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol, int destRow, int destCol) {
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
			
			if(b.getPieceAt(sr, sc) != null && checkConditions(b, sr, sc, dr, dc)) {
				return Collections.singleton(new LegalOtherMoveAndCapture(sr, sc, dr, dc));
			}
			else {
				return Collections.emptySet();
			}
		}
	}
	@Override
	public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
		throw new UnsupportedOperationException("getLegals must be called with a startRow/Col AND destRow/Col");
	}
	protected abstract Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol, int finalDestRow,
			int finalDestCol);
}
