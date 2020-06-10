package chess.util;

import java.io.Serializable;
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

public class ActionTree implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 39304872760544913L;
	private Node root;
	
	public ActionTree(Collection<TreeNode> primaryNodes) {
		root = new Node(null, primaryNodes, true);
	}
	
	public ActionTree() {
		root = new Node(null, true);
	}
	
	public void addPrimaryNode(TreeNode primaryNode) {
		root.addChild(primaryNode);
	}
	
	public Collection<TreeNode> getPrimaryNodes() {
		return Collections.unmodifiableCollection(root.getChildren());
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
	
	public static boolean supportsChildren(Action a) {
		return a == null || supportsChildren(a.getClass());
	}
	
	public static boolean supportsChildren(Class<? extends Action> clazz) {
		return 	RelativeJumpAction.class.isAssignableFrom(clazz) ||
				RelativeSegmentAction.class.isAssignableFrom(clazz);
		
	}
	
	public abstract static class TreeNode implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -3433443613140889365L;
		protected ArrayList<TreeNode> children;
		
		public abstract Set<LegalAction> getLegals(Board b, int startRow, int startCol);
		
		public abstract boolean canCheck(Board b, int startRow, int startCol, int destRow, int destCol);
		
		public ArrayList<TreeNode> getChildren(){
			return children;
		}
		
		public void addChild(TreeNode child) {
			this.getChildren().add(child);
		}
		
		public void addAllChildren(Collection<TreeNode> newChildren) {
			this.getChildren().ensureCapacity(this.getChildren().size() + newChildren.size());
			for(TreeNode child : newChildren) {
				addChild(child);
			}
		}
	}
	
	@Override
	public String toString() {
		return "[ActionTree@"+hashCode()+":root="+root.toString()+"]";
	}
	
	public static class Node extends TreeNode{
		/**
		 * 
		 */
		private static final long serialVersionUID = 5169350659726304648L;
		Action action;
		private Node(Action a, boolean ignoreNullCheck) {
			if(!ignoreNullCheck && a == null) {
				throw new NullPointerException();
			}
			this.action = a;
			this.children = new ArrayList<>(0);
		}
		
		private Node(Action a, Collection<TreeNode> c, boolean ignoreNullCheck) {
			if(!ignoreNullCheck && a == null) {
				throw new NullPointerException();
			}
			this.action = a;
			this.children = new ArrayList<>(c);
		}
		
		public Node(Action a) {
			if(a == null) {
				throw new NullPointerException();
			}
			this.action = a;
			this.children = new ArrayList<>(0);
		}
		
		public Node(Action a, Collection<TreeNode> c) {
			if(a == null) {
				throw new NullPointerException();
			}
			this.action = a;
			this.children = new ArrayList<>(c);
		}
		
		public Node(Action a, TreeNode... c) {
			if(a == null) {
				throw new NullPointerException();
			}
			this.action = a;
			this.children = new ArrayList<>();
			for(int i = 0; i < c.length; i++) {
				this.children.add(c[i]);
			}
		}
		
		@Override
		public void addChild(TreeNode child) {
			if(!supportsChildren(action)) {
				throw new IllegalArgumentException("This node cannot have children because its action is of type: "
						+ action.getClass());
			}
			super.addChild(child);
		}
		
		@Override
		public Set<LegalAction> getLegals(Board b, int startRow, int startCol){
			Set<LegalAction> legals = new HashSet<>();
			if(action != null) {
				legals.addAll(action.getLegals(b, startRow, startCol));
			}
			
			boolean childrenAllowed = false;
			if(action == null) {
				childrenAllowed = true;
			}
			else {
				if(action instanceof RelativeJumpAction) {
					childrenAllowed = legals.size() != 0;
				}
				else if(action instanceof RelativeSegmentAction) {
					childrenAllowed = ((RelativeSegmentAction) action).reachedEndOfSegment(b, startRow, startCol, legals);
					//System.out.println("children = " +this.getChildren());
					//System.out.println("children.getlegals = " + this.getChildren().get(0).getLegals(b, startRow, startCol));
					//System.out.println("childrenallowed = " + childrenAllowed);
				}
			}
			if(childrenAllowed) {
				for(int i = 0; i < children.size(); i++) {
					legals.addAll(getLegalsOnNode(children.get(i), b, startRow, startCol));
					//System.out.println(legals);
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
		
		@Override
		public String toString() {
			return String.format("[Node@%x:action=%s, children=%s]", hashCode(), action, children); 
		}
	}
	
	public static class Choke extends TreeNode{
		/**
		 * 
		 */
		private static final long serialVersionUID = -7266910654873937379L;
		private ArrayList<Condition> conditions;
		public Choke(Collection<Condition> cons, Collection<TreeNode> children) {
			this.conditions = new ArrayList<>(cons);
			this.children = new ArrayList<>(children);
		}
		
		public ArrayList<Condition> getChokeConditions(){
			return conditions;
		}
		
		@Override
		public Set<LegalAction> getLegals(Board b, int startRow, int startCol) {
			Set<LegalAction> legals = new HashSet<>();
			
			for(int i = 0; i < conditions.size(); i++) {
				if(!conditions.get(i).calc(b, startRow, startCol, -1, -1)) {
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
				if(!conditions.get(i).calc(b, startRow, startCol, -1, -1)) {
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
		
		@Override
		public String toString() {
			return "[Choke@"+hashCode()+":conditions="+conditions+", children="+children+"]";
		}
		
	}
}
