package chess.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import chess.base.Board;
import chess.base.LegalAction;
import chess.base.LegalPromotion;
import chess.util.SummonAction.LineSummonAction;
import chess.util.SummonAction.RadiusSummonAction;
import chess.util.SummonAction.RelativeSummonAction;

public abstract class PromotionAction extends Action{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2954238791691219273L;
	protected ArrayList<String> options;
	
	@User(params={"promotion options"})
	public static PromotionAction withOptions(ArrayList<String> options, Condition... cons) {
		if(options.size() == 0) {
			throw new IllegalArgumentException("A promotion action must have at least one option (options.size() == 0)");
		}
		return new ConcretePromotionAction(options, cons);
	}
	
	private static List<Class<? extends Action>> immediateSubtypes = 
			Collections.unmodifiableList(Arrays.asList(
					ConcretePromotionAction.class
			));
	public static List<Class<? extends Action>> getImmediateSubtypes(){
		return immediateSubtypes;
	}
	
	public static String getActionName() {
		return "Promotion";
	}
	
	public static String getVariant() {
		return "On Start";
	}
	
	public static class ConcretePromotionAction extends PromotionAction{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -3377596962210324410L;

		private ConcretePromotionAction(ArrayList<String> options, Condition... cons) {
			this.options = options;
			this.addAllConditions(cons);
		}
		
		public static Method getCreationMethod() throws NoSuchMethodException, SecurityException {
			return PromotionAction.class.getMethod("withOptions", ArrayList.class, Condition[].class);
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
	}
}