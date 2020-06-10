package chess.util;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import chess.base.Board;
import chess.base.LegalAction;

public abstract class Action implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7670901325804547489L;
	private static List<Class<? extends Action>> immediateSubtypes = 
			Collections.unmodifiableList(Arrays.asList(
				MoveAndCaptureAction.class,
				OtherMoveAndCaptureAction.class,
				CaptureAction.class,
				PromotionAction.class,
				SummonAction.class,
				MultiAction.class
			));
	
	public static List<Class<? extends Action>> getImmediateSubtypes() {
		return immediateSubtypes;
	}
	
	protected ArrayList<Condition> conditions;
	
	public Action() {
		conditions = new ArrayList<>();
	}
	
	public void addCondition(Condition c) {
		conditions.add(c);
	}
	
	public Collection<Condition> getConditions(){
		return Collections.unmodifiableCollection(conditions);
	}
	
	public void addAllConditions(Condition... cons) {
		for(int i = 0; i < cons.length; i++) {
			conditions.add(cons[i]);
		}
	}
	
	public void addAllConditions(Collection<Condition> cons) {
		conditions.addAll(cons);
	}
	
	public boolean checkConditions(Board b, int startRow, int startCol, int destRow, int destCol) {
		for(int i = 0; i < conditions.size(); i++) {
			if(conditions.get(i).calc(b, startRow, startCol, destRow, destCol) == false) {
				return false;
			}
		}
		return true;
	}
	
	protected static final int manhattanDist(int row, int col, Board b) {
		if(b.inBounds(row, col)) {
			return 0;
		}
		
		final int size = b.getBoardSize();
		
		int rowDist, colDist;
		
		if(row < 0) {
			rowDist = -row;
		}
		else if(row >= size) {
			rowDist = row - size + 1;
		}
		else {
			rowDist = 0;
		}

		if(col < 0) {
			colDist = -col;
		}
		else if(col >= size){
			colDist = col - size + 1;
		}
		else {
			colDist = 0;
		}
		
		//System.out.printf("mDist returning %d+%d for (%d,%d)%n", rowDist,colDist,row,col);
		return rowDist + colDist;
		
	}
	
	
	public abstract Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol);
	
	public abstract Object[] getReconstructionParameters();
	
	public static String getActionName() {
		return "???";
	}
	
	public static String getVariant() {
		return "";
	}
	
	@Override
	public String toString() {
		return String.format("[%s@%d:conditions=%s]", getClass().getName(), hashCode(), getConditions());
	}
}
