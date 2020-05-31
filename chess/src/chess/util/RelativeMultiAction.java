package chess.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import chess.base.Board;
import chess.base.LegalAction;
import chess.base.LegalMulti;
import chess.base.Piece;

public class RelativeMultiAction extends MultiAction {
	
	int relRow, relCol;
	RelativeMultiAction(int relRow, int relCol, Condition... cons) {
		this.actions = new ArrayList<>();
		this.states = new ArrayList<>();
		this.relRow = relRow;
		this.relCol = relCol;
		this.addAllConditions(cons);
	}

	@Override
	public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
		int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
		int finalDestRow = startRow + m*relRow, finalDestCol = startCol + m*relCol;
		//System.out.printf("multi got passed a start of (%d,%d)", startRow, startCol);
		if(b.inBounds(finalDestRow, finalDestCol) && checkConditions(b, startRow, startCol, finalDestRow, finalDestCol)) {
			MoveAndCaptureAction mnc = null;
			ArrayList<LegalAction> legals = new ArrayList<>();
			for(int i = 0; i < actions.size(); i++) {
				Action act = actions.get(i);
				if(act instanceof MoveAndCaptureAction) {
					mnc = (MoveAndCaptureAction) act;
				}
				else {
					Set<? extends LegalAction> actLegals = act.getLegals(b, startRow, startCol);
					if(actLegals.isEmpty()) {
						if(states.get(i)) {
							return Collections.emptySet();
						}
					}
					else {
						legals.add(actLegals.iterator().next());
					}
				}
			}
			if(mnc != null) {
				Set<LegalMulti> end = new HashSet<>();
				for(LegalAction leg : mnc.getLegals(b, startRow, startCol)) {
					legals.add(leg);
					//LegalMulti's constructor copies the "legals" ArrayList, so it's okay to reuse it.
					end.add(new LegalMulti(finalDestRow, finalDestCol, legals));
					legals.remove(legals.size() - 1);
				}
				return end;
			}
			else {
				if(legals.isEmpty()) {
					return Collections.emptySet();
				}
				else {
					Set<? extends LegalAction> s = Collections.singleton(new LegalMulti(finalDestRow, finalDestCol, legals));
					return s;
				}
			}
		}
		else {
			return Collections.emptySet();
		}
		
	}
	
	@Override
	public RelativeMultiAction addAction(Action a) {
		return (RelativeMultiAction) super.addAction(a);
	}
}
