package chess.util;

import java.util.Objects;
import java.util.ArrayList;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import chess.base.Board;
import chess.base.Pawn;
import chess.base.Piece;
import chess.base.Board.Tile;

/*

Condition.relativeTileToStart(dr, dc).piece().{isSameColor(), isOppositeColor(), hasMoved(), is({"Rook", "Knight", etc.})}
Condition.relativeTileToDestination(dr, dc) //See above

Condition.destinationTile().{isLight(), isDark(), isOccupied(), piece().isSameColor (etc.)}
Condition.startTile().{isLight(), isDark(), etc.}



Examples:

MoveAndCaptureAction a = MoveAndCaptureAction.jumpRelative(1,2);
a.addCondition(Condition.or(Condition.destinationTile().isEmpty(), Condition.destinationTile().piece().isOppositeColor()));

//en passant:
MultiAction a = new MultiAction(1, 1);
a.addAction(MoveAndCaptureAction.jumpRelative(1,1));
a.addAction(CaptureAction.jumpRelative(1,0));

a.addAction(MoveAndCaptureAction.jumpRelative(1,1));
a.addAction(CaptureAction.jumpRelative(1,0));
a.addCondition(Condition.andAll(
	Condition.onDest().call("isOccupied").toBool().invert().toCond(),
	Condition.onStartRelative(1,0).call("getPiece").toObj().instanceOf(Pawn.class),
	Condition.onStartRelative(1,0).call("getPiece").call("getColor").toBool().isEnemy();
	Condition.onBoard().call("getMoves").call("last").call("action").toObj().referenceEquals(<ref to move>);
	
));

*/

enum Flag{
	DESTINATION, ORIGIN, BOARD, SELF
}
public abstract class Condition{
	
	/*EOE = "Enemy or Empty." Standard MoveAndCapture and Capture condition.
	 * Destination tile must be either empty or have a piece of the opposite color.*/
	public static final Condition EOE = new Condition() {
		public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
			Piece p = b.getPieceAt(destRow, destCol);
			return p == null || p.getColor() != b.getPieceAt(startRow, startCol).getColor();
		}
	};
	
	/*POD = "Piece on Destination." Evals to true if there is a piece of any color on the destination.*/
	public static final Condition POD = new Condition() {
		public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
			Piece p = b.getPieceAt(destRow, destCol);
			return p != null;
		}
	};
	
	/*DIE = "Destination is Empty." Evals to true if there is NOT a piece of any color on the destination.
	 * It is the inverse of POD. It has a very nice acronym :) */
	public static final Condition DIE = new Condition() {
		public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
			Piece p = b.getPieceAt(destRow, destCol);
			return p == null;
		}
	};
	
	/*EOD = "Enemy on Destination." Evals to true if there is a piece of the opposite color on the destination.*/
	public static final Condition EOD = new Condition() {
		public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
			Piece p = b.getPieceAt(destRow, destCol);
			return p != null && p.getColor() != b.getPieceAt(startRow, startCol).getColor();
		}
	};
	
	/*TOD = "Teammate on Destination." Evals to true if there is a piece of the same color on the destination.*/
	public static final Condition TOD = new Condition() {
		public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
			Piece p = b.getPieceAt(destRow, destCol);
			return p != null && p.getColor() == b.getPieceAt(startRow, startCol).getColor();
		}
	};
	
	
	public static Condition onStartRelativeCheckable(int relRow, int relCol) {
		return new Condition() {
			@Override
			public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
				boolean myColor = b.getPieceAt(startRow, startCol).getColor();
				int m = myColor == Piece.WHITE ? 1 : -1;
				return b.getTileAt(startRow + m*relRow, startCol + m*relCol).isCheckableBy(myColor);
			}
		};
	}
	
	public abstract boolean eval(Board b, int startRow, int startCol, int destRow, int destCol);
	
	public static ConditionBuilder onDest() {
		return new ConditionBuilder(Flag.DESTINATION);
	}
	public static ConditionBuilder onStart() {
		return new ConditionBuilder(Flag.ORIGIN);
	}
	public static ConditionBuilder onBoard() {
		return new ConditionBuilder(Flag.BOARD);
	}
	public static ConditionBuilder onSelf() {
		return new ConditionBuilder(Flag.SELF);
	}
	public static ConditionBuilder onDestRelative(int row, int col) {
		return new ConditionBuilder(new RelativeTile(row, col, Flag.DESTINATION));
	}
	public static ConditionBuilder onStartRelative(int row, int col) {
		return new ConditionBuilder(new RelativeTile(row, col, Flag.ORIGIN));
	}
	public static ConditionBuilder on(Object o) {
		return new ConditionBuilder(o);
	}
	
	public static Condition not(Condition c1) {
		return new NOTCondition(c1);
	}
	
	public static Condition and(Condition c1, Condition c2) {
		return new ANDCondition(c1, c2);
	}
	
	public static Condition or(Condition c1, Condition c2) {
		return new ORCondition(c1, c2);
	}
	
	public static Condition xor(Condition c1, Condition c2) {
		return new XORCondition(c1, c2);
	}
	
	public Condition and(Condition c2) {
		return Condition.and(this, c2);
	}
	
	public Condition or(Condition c2) {
		return Condition.or(this, c2);
	}
	
	public Condition xor(Condition c2) {
		return Condition.xor(this, c2);
	}
	
	public Condition not() {
		return Condition.not(this);
	}
}

class RelativeTile{
	public Flag relativeTo;
	public int row, col;
	
	public RelativeTile(int r, int c, Flag rel) {
		this.row = r;
		this.col = c;
		this.relativeTo = rel;
	}
}

abstract class PathBase{
	protected Object base;
	/*
	 * If calls is empty OR null, base can be used
	 * */
	protected ArrayList<Member> calls;
	public PathBase(Object base, ArrayList<Member> calls) {
		this.base = base;
		this.calls = calls;
	}
	
	public Object get(Board b, int startRow, int startCol, int destRow, int destCol) {
		Object end;
		if(base instanceof Flag) {
			if(base == Flag.DESTINATION) {
				end = b.getTileAt(destRow, destCol);
			}
			else if(base == Flag.ORIGIN) {
				end = b.getTileAt(startRow, startCol);
			}
			else if(base == Flag.BOARD) {
				end = b;
			}
			else if(base == Flag.SELF) {
				end = b.getPieceAt(startRow, startCol);
			}
			else {
				throw new IllegalArgumentException("bad news bears");
			}
		}
		else if(base instanceof RelativeTile) {
			RelativeTile helper = (RelativeTile) base;
			if(helper.relativeTo == Flag.ORIGIN) {
				end = b.getTileAt(startRow + helper.row, startCol + helper.col);
			}
			else if(helper.relativeTo == Flag.DESTINATION) {
				end = b.getTileAt(destRow + helper.row, destCol + helper.col);
			}
			else {
				throw new IllegalArgumentException("bad news bears");
			}
		}
		else {
			end = base;
		}
		
		if(calls == null) {
			return end;
		}
		Object current = end;
		for(int i = 0; i < calls.size(); i++) {
			try {
				Member m = calls.get(i);
				if(m instanceof Method) {
					current = ((Method) calls.get(i)).invoke(current);
				}
				else { //instance of Field
					current = ((Field) calls.get(i)).get(current);
				}
			} catch (IllegalAccessException e) {
				// Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// Auto-generated catch block
				e.printStackTrace();
			}
		}
		return current;
	}
}

class SingleBooleanCondition extends Condition{
	BoolPath path;
	private boolean isInverted;
	public SingleBooleanCondition(BoolPath path, boolean invert) {
		this.path = path;
		this.isInverted = invert;
	}
	
	@Override
	public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return isInverted ^ path.get(b, startRow, startCol, destRow, destCol).booleanValue();
	}
}

class BooleanEqualsCondition extends Condition{
	BoolPath path1, path2;
	public BooleanEqualsCondition(BoolPath path1, BoolPath path2) {
		this.path1 = path1;
		this.path2 = path2;
	}
	
	@Override
	public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path1.get(b, startRow, startCol, destRow, destCol).booleanValue() ==
				path2.get(b, startRow, startCol, destRow, destCol).booleanValue();
	}
}

class BooleanNotEqualsCondition extends Condition{
	BoolPath path1, path2;
	public BooleanNotEqualsCondition(BoolPath path1, BoolPath path2) {
		this.path1 = path1;
		this.path2 = path2;
	}
	
	@Override
	public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path1.get(b, startRow, startCol, destRow, destCol).booleanValue() !=
				path2.get(b, startRow, startCol, destRow, destCol).booleanValue();
	}
}

class BooleanIsEnemyCondition extends Condition{
	BoolPath path;
	public BooleanIsEnemyCondition(BoolPath path) {
		this.path = path;
	}
	
	@Override
	public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path.get(b, startRow, startCol, destRow, destCol).booleanValue() !=
				b.getPieceAt(startRow, startCol).getColor();
	}
}

class BooleanIsAllyCondition extends Condition{
	BoolPath path;
	public BooleanIsAllyCondition(BoolPath path) {
		this.path = path;
	}
	
	@Override
	public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path.get(b, startRow, startCol, destRow, destCol).booleanValue() ==
				b.getPieceAt(startRow, startCol).getColor();
	}
}

class IntegerGreaterThanCondition extends Condition{
	IntegerPath path1, path2;
	public IntegerGreaterThanCondition(IntegerPath path1, IntegerPath path2) {
		this.path1 = path1;
		this.path2 = path2;
	}
	@Override
	public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path1.get(b, startRow, startCol, destRow, destCol).intValue() >
				path2.get(b, startRow, startCol, destRow, destCol).intValue();
	}
}

class IntegerLessThanCondition extends Condition{
	IntegerPath path1, path2;
	public IntegerLessThanCondition(IntegerPath path1, IntegerPath path2) {
		this.path1 = path1;
		this.path2 = path2;
	}
	@Override
	public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path1.get(b, startRow, startCol, destRow, destCol).intValue() < 
				path2.get(b, startRow, startCol, destRow, destCol).intValue();
	}
}

class IntegerGreaterThanOrEqualCondition extends Condition{
	IntegerPath path1, path2;
	public IntegerGreaterThanOrEqualCondition(IntegerPath path1, IntegerPath path2) {
		this.path1 = path1;
		this.path2 = path2;
	}
	@Override
	public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path1.get(b, startRow, startCol, destRow, destCol).intValue() >= 
				path2.get(b, startRow, startCol, destRow, destCol).intValue();
	}
}

class IntegerLessThanOrEqualCondition extends Condition{
	IntegerPath path1, path2;
	public IntegerLessThanOrEqualCondition(IntegerPath path1, IntegerPath path2) {
		this.path1 = path1;
		this.path2 = path2;
	}
	@Override
	public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path1.get(b, startRow, startCol, destRow, destCol).intValue() <= 
				path2.get(b, startRow, startCol, destRow, destCol).intValue();
	}
}

class IntegerEqualsCondition extends Condition{
	IntegerPath path1, path2;
	public IntegerEqualsCondition(IntegerPath path1, IntegerPath path2) {
		this.path1 = path1;
		this.path2 = path2;
	}
	@Override
	public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path1.get(b, startRow, startCol, destRow, destCol).intValue() == 
				path2.get(b, startRow, startCol, destRow, destCol).intValue();
	}
}

class IntegerNotEqualsCondition extends Condition{
	IntegerPath path1, path2;
	public IntegerNotEqualsCondition(IntegerPath path1, IntegerPath path2) {
		this.path1 = path1;
		this.path2 = path2;
	}
	@Override
	public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path1.get(b, startRow, startCol, destRow, destCol).intValue() != 
				path2.get(b, startRow, startCol, destRow, destCol).intValue();
	}
}

class ObjectReferenceEqualsCondition extends Condition{
	ObjectPath path1, path2;
	public ObjectReferenceEqualsCondition(ObjectPath path1, ObjectPath path2) {
		this.path1 = path1;
		this.path2 = path2;
	}
	@Override
	public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path1.get(b, startRow, startCol, destRow, destCol) ==
				path2.get(b, startRow, startCol, destRow, destCol);
	}
}

class ObjectNotReferenceEqualsCondition extends Condition{
	ObjectPath path1, path2;
	public ObjectNotReferenceEqualsCondition(ObjectPath path1, ObjectPath path2) {
		this.path1 = path1;
		this.path2 = path2;
	}
	@Override
	public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path1.get(b, startRow, startCol, destRow, destCol) !=
				path2.get(b, startRow, startCol, destRow, destCol);
	}
}

class ObjectIsNullCondition extends Condition{
	ObjectPath path;
	public ObjectIsNullCondition(ObjectPath path) {
		this.path = path;
	}
	@Override
	public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path.get(b, startRow, startCol, destRow, destCol) == null;
	}
}

class ObjectIsNotNullCondition extends Condition{
	ObjectPath path;
	public ObjectIsNotNullCondition(ObjectPath path) {
		this.path = path;
	}
	@Override
	public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path.get(b, startRow, startCol, destRow, destCol) != null;
	}
}

class ObjectInstanceOfCondition extends Condition{
	ObjectPath path;
	Class<?> caster;
	
	public ObjectInstanceOfCondition(ObjectPath path, Class<?> caster) {
		this.path = path;
		this.caster = caster;
	}
	@Override
	public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	caster.isInstance(path.get(b, startRow, startCol, destRow, destCol));
	}
}


class ANDCondition extends Condition{
	Condition c1, c2;
	
	public ANDCondition(Condition c1, Condition c2) {
		this.c1 = c1;
		this.c2 = c2;
	}
	
	@Override
	public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	c1.eval(b, startRow, startCol, destRow, destCol) &&
				c2.eval(b, startRow, startCol, destRow, destCol);
				
	}
}

class ORCondition extends Condition{
	Condition c1, c2;
	
	public ORCondition(Condition c1, Condition c2) {
		this.c1 = c1;
		this.c2 = c2;
	}
	
	@Override
	public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	c1.eval(b, startRow, startCol, destRow, destCol) ||
				c2.eval(b, startRow, startCol, destRow, destCol);
				
	}
}

class XORCondition extends Condition{
	Condition c1, c2;
	
	public XORCondition(Condition c1, Condition c2) {
		this.c1 = c1;
		this.c2 = c2;
	}
	
	@Override
	public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	c1.eval(b, startRow, startCol, destRow, destCol) ^
				c2.eval(b, startRow, startCol, destRow, destCol);
				
	}
}

class NOTCondition extends Condition{
	Condition c1;
	
	public NOTCondition(Condition c1) {
		this.c1 = c1;
	}
	
	@Override
	public boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	!c1.eval(b, startRow, startCol, destRow, destCol);
	}
}
