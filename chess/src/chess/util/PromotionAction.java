package chess.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import chess.base.Board;
import chess.base.LegalAction;
import chess.base.LegalPromotion;

public abstract class PromotionAction extends Action{
	
	protected ArrayList<String> options;
	
	public static PromotionAction withOptions(ArrayList<String> options, Condition... cons) {
		if(options.size() == 0) {
			throw new IllegalArgumentException("A promotion action must have at least one option (options.size() == 0)");
		}
		return new ConcretePromotionAction(options, cons);
	}
	
	private static class ConcretePromotionAction extends PromotionAction{
		
		private ConcretePromotionAction(ArrayList<String> options, Condition... cons) {
			this.options = options;
			this.addAllConditions(cons);
		}

		@Override
		public Set<? extends LegalAction> getLegals(Board b, int startRow, int startCol) {
			if(checkConditions(b, startRow, startCol, startRow, startCol)) {
				return Collections.singleton(new LegalPromotion(startRow, startCol, options));
			}
			else {
				return Collections.emptySet();
			}
		}
		
		/*
		@Override
		public LegalPromotion next(Board b, int startRow, int startCol) {
			if(!this.checkConditions(b, startRow, startCol, startRow, startCol)) {
				return null;
			}
			return new LegalPromotion(startRow, startCol, options);
		}
		*/
		
	}
}
