package chess.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import chess.base.Board;
import chess.base.LegalAction;
import chess.base.LegalSummon;
import chess.base.Piece;


public abstract class SummonAction extends Action{
	protected ArrayList<String> options;
	
	public static RelativeSummonAction relative(int dr, int dc, ArrayList<String> options, Condition... cons) {
		if(options.size() == 0) {
			throw new IllegalArgumentException("A promotion action must have at least one option (options.size() == 0)");
		}
		return new RelativeSummonAction(dr, dc, options, cons);
	}
	
	public static LineSummonAction line(int dr, int dc, ArrayList<String> options, Condition...cons) {
		if(options.size() == 0) {
			throw new IllegalArgumentException("A promotion action must have at least one option (options.size() == 0)");
		}
		return new LineSummonAction(dr, dc, options, cons);
	}
	
	protected SummonAction(ArrayList<String> ops) {
		this.options = ops;
	}
	
	public static class RelativeSummonAction extends SummonAction implements RelativeJumpAction{
		private int relRow;
		private int relCol;
		
		private RelativeSummonAction(int dr, int dc, ArrayList<String> options, Condition... cons) {
			super(options);
			this.relRow = dr;
			this.relCol = dc;
			this.addAllConditions(cons);
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
	}
	
	public static class LineSummonAction extends SummonAction implements LineAction {
		private int deltaRow, deltaCol;
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
			for(int i = 0; i < cons.length; i++) {
				stopConditions.add(cons[i]);
			}
			return this;
		}
		
		/* *
		 * Returns true if we should stop, false otherwise.
		 */
		private boolean checkStops(Board b, int startRow, int startCol, int destRow, int destCol) {
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
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			Set<LegalSummon> summonSet = new HashSet<>();
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int row = startRow + m*deltaRow;
			int col = startCol + m*deltaCol;
			while(b.inBounds(row, col)) {
				if(checkConditions(b, startRow, startCol, row, col)) {
					summonSet.add(new LegalSummon(row,col,options));
				}
				if(checkStops(b, startRow, startCol, row, col)) {
					break;
				}
				row += m*deltaRow;
				col += m*deltaCol;
			}
			return summonSet;
		}
		
	}
}
