package chess.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import chess.base.Board;
import chess.base.LegalAction;
import chess.base.LegalCapture;
import chess.base.LegalMoveAndCapture;
import chess.base.LegalMulti;

public class ActionTree {
	private Node root;
	
	public ActionTree(Collection<TreeNode> primaryNodes) {
		root = new Node(null, primaryNodes);
	}
	
	public Set<LegalAction> getLegals(Board b, int startRow, int startCol){
		//System.out.printf("****** start passed to tree as (%d,%d)%n", startRow,startCol);
		return getLegalsOnNode(root, b, startRow, startCol);
	}
	
	
	public boolean canCheck(Board b, int startRow, int startCol, int destRow, int destCol) {
		return canCheckOnNode(root, b, startRow, startCol, destRow, destCol);
	}
	
	
	private static Set<LegalAction> getLegalsOnNode(TreeNode node, Board b, int startRow, int startCol) {
		if(node == null) {
			return Collections.emptySet();
		}
		else {
			return node.getLegals(b, startRow, startCol);
		}
	}
	
	private boolean canCheckOnNode(TreeNode node, Board b, int startRow, int startCol, int destRow, int destCol) {
		if(node == null) {
			return false;
		}
		else {
			return node.canCheck(b, startRow, startCol, destRow, destCol);
		}
	}
	
	public abstract static class TreeNode{
		protected ArrayList<TreeNode> children;
		
		public abstract Set<LegalAction> getLegals(Board b, int startRow, int startCol);
		
		public abstract boolean canCheck(Board b, int startRow, int startCol, int destRow, int destCol);
		
		public ArrayList<TreeNode> getChildren(){
			return children;
		}
	}
	
	public static class Node extends TreeNode{
		Action action;
		
		public Node(Action a) {
			this.action = a;
			this.children = new ArrayList<>(0);
		}
		
		public Node(Action a, Collection<TreeNode> c) {
			this.action = a;
			this.children = new ArrayList<>(c);
		}
		
		public Node(Action a, TreeNode... c) {
			this.action = a;
			this.children = new ArrayList<>();
			for(int i = 0; i < c.length; i++) {
				this.children.add(c[i]);
			}
		}
		
		@Override
		public Set<LegalAction> getLegals(Board b, int startRow, int startCol){
			Set<LegalAction> legals = new HashSet<>();
			if(action != null) {
				legals.addAll(action.getLegals(b, startRow, startCol));
			}
			
			boolean childrenAllowed = action == null;
			if(action instanceof RelativeJumpAction) {
				childrenAllowed = legals.size() != 0;
			}
			else if(action instanceof RelativeSegmentAction) {
				throw new IllegalArgumentException("unfinished code");
			}
			if(childrenAllowed) {
				for(int i = 0; i < children.size(); i++) {
					legals.addAll(getLegalsOnNode(children.get(i), b, startRow, startCol));
				}
			}
			return legals;
			
		}
		
		@Override
		public boolean canCheck(Board b, int startRow, int startCol, int destRow, int destCol) {
			if(action instanceof MoveAndCaptureAction || action instanceof CaptureAction) {
				Set<? extends LegalAction> legals = action.getLegals(b, startRow, startCol);
				for(LegalAction legal : legals) {
					if(legal.destRow() == destRow && legal.destCol() == destCol) {
						return true;
					}
				}
			}
			else if(action instanceof MultiAction) {
				MultiAction ma = (MultiAction) action;
				/* Make sure it actually has a MNC or Capture Action before calculating all the legals:*/
				if(ma.hasCapture || ma.hasMoveAndCapture) {
					Set<? extends LegalAction> maLegals = ma.getLegals(b, startRow, startCol);
					for(LegalAction legal : maLegals) {
						for(LegalAction legalAction : ((LegalMulti) legal).getActions()) {
							if(legalAction instanceof LegalMoveAndCapture || legalAction instanceof LegalCapture) {
								if(legalAction.destRow() == destRow && legalAction.destCol() == destCol) {
									return true;
								}
							}
						}
					}
				}
			}
			for(int i = 0; i < children.size(); i++) {
				if(children.get(i).canCheck(b, startRow, startCol, destRow, destCol)) {
					return true;
				}
			}
			return false;
		}
		
		public Action getAction() {
			return action;
		}
	}
	
	public static class Choke extends TreeNode{
		private ArrayList<Condition> conditions;
		public Choke(Collection<Condition> cons, Collection<TreeNode> children) {
			this.conditions = new ArrayList<>(cons);
			this.children = new ArrayList<>(children);
		}
		
		@Override
		public Set<LegalAction> getLegals(Board b, int startRow, int startCol) {
			Set<LegalAction> legals = new HashSet<>();
			
			for(int i = 0; i < conditions.size(); i++) {
				if(!conditions.get(i).eval(b, startRow, startCol, -1, -1)) {
					return legals;
				}
			}
			for(int i = 0; i < children.size(); i++) {
				legals.addAll(getLegalsOnNode(children.get(i), b, startRow, startCol));
			}
			
			return legals;
		}

		@Override
		public boolean canCheck(Board b, int startRow, int startCol, int destRow, int destCol) {
			for(int i = 0; i < conditions.size(); i++) {
				if(!conditions.get(i).eval(b, startRow, startCol, -1, -1)) {
					return false;
				}
			}
			for(int i = 0; i < children.size(); i++) {
				if(children.get(i).canCheck(b, startRow, startCol, destRow, destCol)) {
					return true;
				}
			}
			return false;
		}
		
	}
}
