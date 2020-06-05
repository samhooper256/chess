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
import chess.base.LegalMulti;
import chess.base.Piece;
import chess.util.SummonAction.LineSummonAction;
import chess.util.SummonAction.RadiusSummonAction;
import chess.util.SummonAction.RelativeSummonAction;

public abstract class MultiAction extends chess.util.Action{
	
	protected ArrayList<SubMulti> actions;
	protected ArrayList<Boolean> states;
	boolean hasMoveAndCapture = false;
	boolean hasCapture = false; //This variable is used by ActionTree's canCheck(...) to short-circuit. KEEP IT UPDATED.
	boolean hasPromotion = false;
	
	@User(params={"relative display row", "relative display column"})
	public static RelativeMultiAction relative(int relDispRow, int relDispCol, Condition... cons) {
		return new RelativeMultiAction(relDispRow, relDispCol, cons);
	}
	
	/* *
	 * Equivalent to addAction(action, true)
	 */
	public MultiAction addAction(SubMulti action) {
		return addAction(action, true);
	}
	
	public MultiAction addAllActions(Collection<SubMulti> actions) {
		for(SubMulti a : actions) {
			addAction(a, true);
		}
		return this;
	}
	
	private static List<Class<? extends Action>> immediateSubtypes = 
			Collections.unmodifiableList(Arrays.asList(
					RelativeMultiAction.class
					
			));
	public static List<Class<? extends Action>> getImmediateSubtypes(){
		return immediateSubtypes;
	}
	
	public static String getActionName() {
		return "Multi";
	}
	
	/* 
	 * 
	 * THERE CAN BE A MAXIMUM OF ONE PROMOTION. Promotions will always occur first!
	 * 
	 * You many not 'nest' MultiActions; that is, you may not add a MultiAction
	 * to another MultiAction's list of actions.
	 * 
	 * the "state" parameter determines whether or not this action MUST
	 * be legal for the entire MultiAction to be legal (true means it does
	 * need to be legal, false means it doesn't).
	 * 
	 * If no actions are legal, the MultiAction will not be legal, regardless of
	 * states.
	 * */
	
	public MultiAction addAction(SubMulti action, boolean state) {
		if(action instanceof SubMulti.Promo) {
			if(hasPromotion) {
				throw new IllegalArgumentException("A MultiAction cannot have more than one PromotionAction");
			}
			else {
				hasPromotion = true;
			}
			actions.add(0, action);
			states.add(0, state);
			return this;
		}
		else {
			if(action instanceof SubMulti.CapRel) {
				hasCapture = true;
			}
			else if(action instanceof SubMulti.MNC) {
				if(hasMoveAndCapture) {
					throw new IllegalArgumentException("Only one SubMulti.MNC is allowed.");
				}
				else {
					hasMoveAndCapture = true;
				}
			}
			actions.add(action);
			states.add(state);
			return this;
		}
		
	}
	
	public static class RelativeMultiAction extends MultiAction implements RelativeJumpAction{
		
		int relRow, relCol;
		RelativeMultiAction(int relRow, int relCol, Condition... cons) {
			this.actions = new ArrayList<>();
			this.states = new ArrayList<>();
			this.relRow = relRow;
			this.relCol = relCol;
			this.addAllConditions(cons);
		}
		
		public static String getVariant() {
			return "Relative";
		}
		
		public static Method getCreationMethod() throws NoSuchMethodException, SecurityException {
			return MultiAction.class.getMethod("relative", int.class, int.class, Condition[].class);
		}
		
		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			int m = b.getPieceAt(startRow, startCol).getColor() == Piece.WHITE ? 1 : -1;
			int finalDestRow = startRow + m*relRow, finalDestCol = startCol + m*relCol;
			//System.out.printf("multi got passed a start of (%d,%d)", startRow, startCol);
			if(b.inBounds(finalDestRow, finalDestCol) && checkConditions(b, startRow, startCol, finalDestRow, finalDestCol)) {
				SubMulti.MNC mnc = null;
				ArrayList<LegalAction> legals = new ArrayList<>();
				for(int i = 0; i < actions.size(); i++) {
					SubMulti act = actions.get(i);
					if(act instanceof SubMulti.MNC) {
						mnc = (SubMulti.MNC) act;
					}
					else {
						Set<? extends LegalAction> actLegals = act.getLegals(b, startRow, startCol, finalDestRow, finalDestCol);
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
					for(LegalAction leg : mnc.getLegals(b, startRow, startCol, finalDestRow, finalDestCol)) {
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
		public RelativeMultiAction addAction(SubMulti a) {
			return (RelativeMultiAction) super.addAction(a);
		}
	}
	
}

