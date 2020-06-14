package chess.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import chess.base.Board;
import chess.base.LegalAction;
import chess.base.LegalCapture;
import chess.base.LegalMulti;
import chess.base.Piece;
import chess.piecebuilder.Pair;

public abstract class MultiAction extends chess.util.Action{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -717815859325937989L;
	protected ArrayList<SubMulti> actions;
	protected ArrayList<Boolean> states;
	boolean hasMoveAndCapture = false;
	boolean hasCapture = false; //This variable is used by ActionTree's canCheck(...) to short-circuit. KEEP IT UPDATED.
	boolean hasPromotion = false;
	
	public Pair<List<SubMulti>, List<Boolean>> getSubMultiData(){
		return new Pair<>(actions, states);
	}
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
	
	public static Class<? extends LegalAction> correspondingLegal(){
		return LegalMulti.class;
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
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 7365243869927755382L;
		private static Method CREATION_METHOD;
		static {
			try {
				CREATION_METHOD = MultiAction.class.getMethod("relative", int.class, int.class, Condition[].class);
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		public final int relRow, relCol;
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
		
		@Override
		public Method getMethod() {
			return CREATION_METHOD;
		}

		public static Method getCreationMethod() {
			return CREATION_METHOD;
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
					Set<? extends LegalAction> actLegals = act.getLegals(b, startRow, startCol, finalDestRow, finalDestCol);
					if(actLegals.isEmpty()) {
						if(states.get(i).booleanValue()) {
							return Collections.emptySet();
						}
					}
					else {
						legals.addAll(actLegals);
					}
				}
				if(legals.isEmpty()) {
					return Collections.emptySet();
				}
				else {
					return Collections.singleton(new LegalMulti(finalDestRow, finalDestCol, legals));
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

		@Override
		public Object[] getReconstructionParameters() {
			return new Object[] {relRow, relCol};
		}
	}
	
}

