package chess.util;

import java.util.ArrayList;

public abstract class MultiAction extends chess.util.Action{
	
	protected ArrayList<Action> actions;
	protected ArrayList<Boolean> states;
	boolean hasMoveAndCapture = false;
	boolean hasCapture = false;
	boolean hasPromotion = false;
	public static RelativeMultiAction relative(int relDispRow, int relDispCol, Condition... cons) {
		return new RelativeMultiAction(relDispRow, relDispCol, cons);
	}
	
	/* *
	 * Equivalent to addAction(action, true)
	 */
	public MultiAction addAction(Action action) {
		return addAction(action, true);
	}
	
	/* 
	 * 
	 * THERE CAN BE A MAXIMUM OF ONE PROMOTION. Promotions will always occur first!
	 * THERE CAN BE A MAXIMUM OF ONE MOVE AND CAPTURE. MNCs will always occur last!
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
	
	public MultiAction addAction(Action action, boolean state) {
		if(action instanceof MultiAction) {
			throw new IllegalArgumentException("You may not add a MultiAction to another MultiAction's list of actions");
		}
		if(action instanceof PromotionAction) {
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
			if(action instanceof CaptureAction) {
				if(!(action instanceof CaptureAction.RelativeJumpCaptureAction)) {
					throw new IllegalArgumentException("Only RelativeJumpCaptureAction actions are allowed.");
				}
				hasCapture = true;
			}
			else if(action instanceof MoveAndCaptureAction) {
				if(hasMoveAndCapture) {
					throw new IllegalArgumentException("A MultiAction cannot have more than one MoveAndCaptureAction");
				}
				else {
					hasMoveAndCapture = true;
				}
			}
			actions.add(action);
			states.add(0, state);
			return this;
		}
	}
	
	
}

