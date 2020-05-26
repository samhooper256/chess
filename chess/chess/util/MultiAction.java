package chess.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import chess.base.Board;
import chess.base.LegalAction;
import chess.base.LegalMulti;
import chess.base.Piece;

public abstract class MultiAction extends chess.util.Action{
	
	/*
	 * Keys are the various actions that are part of this MultiActon.
	 * The Values (booleans) represent whether the corresponding action
	 * MUST be legal for the whole MultiAction to be legal.
	 * */
	protected ArrayList<Action> actions;
	protected ArrayList<Boolean> states;
	boolean hasMoveAndCapture = false;
	boolean hasCapture = false;
	boolean hasPromotion = false;
	public static RelativeMultiAction relativeDisplay(int relDispRow, int relDispCol, Condition... cons) {
		return new RelativeMultiAction(relDispRow, relDispCol, cons);
	}
	
	/* 
	 * 
	 * THERE CAN BE A MAXIMUM OF ONE PROMOTION. Promotions will always occur first!
	 * THERE CAN BE A MAXIMUM OF ONE MOVE AND CAPTURE. MNCs will always occur last!
	 * 
	 * You many not 'nest' MultiActions; that is, you may not add a MultiAction
	 * to another MultiAction's list of actions.
	 * */
	public MultiAction addAction(Action action) {
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
			states.add(0, true);
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
			states.add(0, true);
			return this;
		}
		
	}
	
	
}

