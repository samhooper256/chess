package chess.util;

import java.util.Collection;

import chess.base.Board;

public interface StoppableAction {
	public Collection<Condition> getStopConditions();
	
	/* *
	 * Returns true if we should stop, false otherwise.
	 */
	public default boolean checkStops(Board b, int startRow, int startCol, int destRow, int destCol) {
		Collection<Condition> stopConditions = getStopConditions();
		if(stopConditions.size() == 0) {
			return false;
		}
	
		for(Condition c : stopConditions) {
			if(c.calc(b, startRow, startCol, destRow, destCol)) {
				return true;
			}
		}
		
		return false;
	}
	
	/* *
	 * Note that ALL STOPS MUST BE TRUE for us to stop.
	 */
	public default StoppableAction stops(Condition... cons) {
		Collection<Condition> stopConditions = getStopConditions();
		for(int i = 0; i < cons.length; i++) {
			stopConditions.add(cons[i]);
		}
		return this;
	}
}
