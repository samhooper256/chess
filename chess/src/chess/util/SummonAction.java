package chess.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import chess.base.Board;
import chess.base.LegalAction;
import chess.base.LegalMoveAndCapture;
import chess.base.LegalSummon;
import chess.base.Piece;


public abstract class SummonAction extends Action{
	protected ArrayList<String> options;
	
	public static SummonAction relativeWithOptions(int dr, int dc, ArrayList<String> options, Condition... cons) {
		if(options.size() == 0) {
			throw new IllegalArgumentException("A promotion action must have at least one option (options.size() == 0)");
		}
		return new RelativeSummonAction(dr, dc, options, cons);
	}
	
	private static class RelativeSummonAction extends SummonAction implements RelativeJumpAction{
		private int relRow;
		private int relCol;
		private ArrayList<String> options;
		
		private RelativeSummonAction(int dr, int dc, ArrayList<String> options, Condition... cons) {
			this.relRow = dr;
			this.relCol = dc;
			this.options = options;
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
}
