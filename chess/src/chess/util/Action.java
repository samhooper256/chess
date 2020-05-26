package chess.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import chess.base.Board;
import chess.base.LegalAction;

public abstract class Action {
	protected ArrayList<Condition> conditions;
	
	public Action() {
		conditions = new ArrayList<>();
	}
	
	public void addCondition(Condition c) {
		conditions.add(c);
	}
	
	public void addAllConditions(Condition... cons) {
		for(int i = 0; i < cons.length; i++) {
			conditions.add(cons[i]);
		}
	}
	
	public boolean checkConditions(Board b, int startRow, int startCol, int destRow, int destCol) {
		for(int i = 0; i < conditions.size(); i++) {
			if(conditions.get(i).eval(b, startRow, startCol, destRow, destCol) == false) {
				return false;
			}
		}
		return true;
	}
	
	public abstract Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol);
}
