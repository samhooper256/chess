package chess.util;

import java.util.Set;

import chess.base.Board;
import chess.base.LegalAction;

public interface RelativeSegmentAction {
	public boolean reachedEndOfSegment(Board b, int startRow, int startCol, Set<? extends LegalAction> legals);
}
