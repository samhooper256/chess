package chess.util;

import java.io.Serializable;
import java.lang.reflect.Method;

import chess.base.Board;
import chess.base.Piece;

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


public abstract class Condition implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2544194942472379599L;

	public static final Method[] postConstructionModifierMethods;
	static {
		postConstructionModifierMethods = new Method[3];
		try {
			postConstructionModifierMethods[0] = Condition.class.getMethod("and", Condition.class, Condition.class);
			postConstructionModifierMethods[1] = Condition.class.getMethod("or", Condition.class, Condition.class);
			postConstructionModifierMethods[2] = Condition.class.getMethod("xor", Condition.class, Condition.class);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/////////////////////////////////////////////////////////////////////////////
	/* Common Conditions have been made as public static final variables below.*/
	/* ALL OF THESE CONDITIONS COULD BE MADE ON THEIR OWN. They provide no new */
	/* functionality, although they are more efficient than ones created by    */
	/* hand.																   */
	/////////////////////////////////////////////////////////////////////////////
	
	/*EOE = "Enemy or Empty." Standard MoveAndCapture and Capture condition.
	 * Destination tile must be either empty or have a piece of the opposite color.*/
	@AFC(name="Destination is empty or has an enemy")
	public static final Condition EOE = Condition.or(
			Condition.onDest().call("isEmpty").toBooleanPath().toCond(),
			Condition.onDest().call("getPiece").call("getColor").toBooleanPath().isEnemy()
	);
	
	/*POD = "Piece on Destination." Evals to true if there is a piece of any color on the destination.*/
	@AFC(name="Piece on destination")
	public static final Condition POD = Condition.onDest().call("getPiece").toObjectPath().isNotNull();
	
	/* *
	 * POS = "Piece on Start". Evals to true if there is a piece of any color on the start tile.
	 * This condition is only useful for OtherMoveAndCaptures - for all other actions, there is
	 * guaranteed to be a piece on the start.
	 */
	@AFC(name="Piece on start")
	public static final Condition POS = Condition.onSelf().toObjectPath().isNotNull();
	
	/*DIE = "Destination is Empty." Evals to true if there is NOT a piece of any color on the destination.
	 * It is the inverse of POD. It has a very nice acronym :) */
	@AFC(name="Destination is empty")
	public static final Condition DIE = Condition.onDest().call("getPiece").toObjectPath().isNull();
	
	/* *
	 * SIE = "Start is Empty." For the same reason as POS, this is only useful for
	 * OtherMoveAndCaptures.
	 */
	@AFC(name="Start is empty")
	public static final Condition SIE = Condition.onSelf().toObjectPath().isNull();
	
	/*EOD = "Enemy on Destination." Evals to true if there is a piece of the opposite color on the destination.*/
	@AFC(name="Enemy on destination")
	public static final Condition EOD = Condition.onDest().call("getPiece").call("getColor").toBooleanPath().isEnemy();
	
	/*TOD = "Teammate on Destination." Evals to true if there is a piece of the same color on the destination.*/
	@AFC(name="Teammate on destination")
	public static final Condition TOD = Condition.onDest().call("getPiece").call("getColor").toBooleanPath().isAlly();
	
	//////////////////////////////
	/* End of common conditions */
	//////////////////////////////
	
	//this will be returned if evaluating the condition throws an exception.
	boolean defaultValue;
	
	public boolean getDefault() { return defaultValue; }
	
	public void setDefault(boolean newDV) {
		this.defaultValue = newDV;
	}
	/* *
	 * THIS CONDITION MUST ONLY BE USED ON KINGS,
	 * or paradoxical loops (StackOverflow Errors) will occur.
	 */
	public static Condition onStartRelativeCheckable(int relRow, int relCol) {
		return new Condition() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 4101657849303538089L;

			@Override
			boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
				boolean myColor = b.getPieceAt(startRow, startCol).getColor();
				int m = myColor == Piece.WHITE ? 1 : -1;
				return b.getTileAt(startRow + m*relRow, startCol + m*relCol).isCheckableBy(myColor);
			}
		};
	}
	
	abstract boolean eval(Board b, int startRow, int startCol, int destRow, int destCol);
	
	protected boolean evalOrFalse(Board b, int startRow, int startCol, int destRow, int destCol) {
		boolean result;
		try {
			result = this.eval(b, startRow, startCol, destRow, destCol);
		}
		catch(Exception e) {
			return false;
		}
		return result;
	}
	
	public boolean calc(Board b, int startRow, int startCol, int destRow, int destCol) {
		boolean result;
		try {
			result = this.eval(b, startRow, startCol, destRow, destCol);
		}
		catch(Throwable e) {
			return defaultValue;
		}
		return result;
		
	}
	protected boolean evalOrTrue(Board b, int startRow, int startCol, int destRow, int destCol) {
		boolean result;
		try {
			result = this.eval(b, startRow, startCol, destRow, destCol);
		}
		catch(Exception e) {
			return true;
		}
		return result;
	}
	
	@AFC(name="destination tile", returnType=Board.Tile.class)
	public static ConditionBuilder onDest() {
		return new ConditionBuilder(Flag.DESTINATION);
	}
	
	@AFC(name="start tile", returnType=Board.Tile.class)
	public static ConditionBuilder onStart() {
		return new ConditionBuilder(Flag.ORIGIN);
	}
	
	@AFC(name="board", returnType=Board.class)
	public static ConditionBuilder onBoard() {
		return new ConditionBuilder(Flag.BOARD);
	}
	
	@AFC(name="acting piece", returnType=Piece.class)
	public static ConditionBuilder onSelf() {
		return new ConditionBuilder(Flag.SELF);
	}
	public static ConditionBuilder onDestRelative(int row, int col) {
		return new ConditionBuilder(new RelativeTile(
				new IntegerPath(row), new IntegerPath(col), Flag.DESTINATION));
	}
	public static ConditionBuilder onStartRelative(int row, int col) {
		return new ConditionBuilder(
				new RelativeTile(new IntegerPath(row), new IntegerPath(col), Flag.ORIGIN));
	}
	@AFC(name="relative tile to destination", returnType=Board.Tile.class, paramDescriptions={"row","column"})
	public static ConditionBuilder onDestRelative(IntegerPath row, IntegerPath col) {
		return new ConditionBuilder(new RelativeTile(row, col, Flag.DESTINATION));
	}
	@AFC(name="relative tile to start", returnType=Board.Tile.class, paramDescriptions={"row","column"})
	public static ConditionBuilder onStartRelative(IntegerPath row, IntegerPath col) {
		return new ConditionBuilder(new RelativeTile(row, col, Flag.ORIGIN));
	}
	public static ConditionBuilder on(Object o) {
		return new ConditionBuilder(o);
	}
	
	public static Condition not(Condition c1) {
		return new NOTCondition(c1);
	}
	@ConditionCombiner(name="and")
	public static Condition and(Condition c1, Condition c2) {
		return new ANDCondition(c1, c2);
	}
	@ConditionCombiner(name="or")
	public static Condition or(Condition c1, Condition c2) {
		return new ORCondition(c1, c2);
	}
	@ConditionCombiner(name="xor")
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
	public IntegerPath row, col;
	
	public RelativeTile(IntegerPath r, IntegerPath c, Flag rel) {
		this.row = r;
		this.col = c;
		this.relativeTo = rel;
	}
	
	public int calcRow(Board b, int startRow, int startCol, int destRow, int destCol) {
		return row.get(b, startRow, startCol, destRow, destCol);
	}
	
	public int calcCol(Board b, int startRow, int startCol, int destRow, int destCol) {
		return col.get(b, startRow, startCol, destRow, destCol);
	}
	
	@Override
	public String toString() {
		return "[RelativeTile:relativeTo="+relativeTo+", row="+row+", col="+col+"]";
	}
}

class CopyCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7152776211214638386L;

	public Condition getCopy() {
		return copy;
	}

	public void setCopy(Condition copy) {
		this.copy = copy;
	}

	private Condition copy;
	
	public CopyCondition() {}

	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return copy.eval(b, startRow, startCol, destRow, destCol);
	}
	
}

class SingleBooleanCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7370733400023991771L;
	public BooleanPath getPath() {
		return path;
	}

	public void setPath(BooleanPath path) {
		this.path = path;
	}

	public boolean isInverted() {
		return isInverted;
	}

	public void setInverted(boolean isInverted) {
		this.isInverted = isInverted;
	}
	
	public SingleBooleanCondition() {}
	
	BooleanPath path;
	private boolean isInverted;
	public SingleBooleanCondition(BooleanPath path, boolean invert) {
		this.path = path;
		this.isInverted = invert;
	}
	
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return isInverted ^ path.get(b, startRow, startCol, destRow, destCol).booleanValue();
	}
	
	@Override
	public String toString() {
		return "SingleBooleanCondition on boolpath="+path;
	}
}

class BooleanEqualsCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1913366544593682445L;
	BooleanPath path1, path2;
	public BooleanEqualsCondition(BooleanPath path1, BooleanPath path2) {
		this.path1 = path1;
		this.path2 = path2;
	}
	
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path1.get(b, startRow, startCol, destRow, destCol).booleanValue() ==
				path2.get(b, startRow, startCol, destRow, destCol).booleanValue();
	}
}

class BooleanNotEqualsCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2639436480352335867L;
	BooleanPath path1, path2;
	public BooleanNotEqualsCondition(BooleanPath path1, BooleanPath path2) {
		this.path1 = path1;
		this.path2 = path2;
	}
	
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path1.get(b, startRow, startCol, destRow, destCol).booleanValue() !=
				path2.get(b, startRow, startCol, destRow, destCol).booleanValue();
	}
}

class BooleanIsEnemyCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4695878215035150876L;
	BooleanPath path;
	public BooleanIsEnemyCondition(BooleanPath path) {
		this.path = path;
	}
	
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path.get(b, startRow, startCol, destRow, destCol).booleanValue() !=
				b.getPieceAt(startRow, startCol).getColor();
	}
}

class BooleanIsAllyCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3834724608305686972L;
	BooleanPath path;
	public BooleanIsAllyCondition(BooleanPath path) {
		this.path = path;
	}
	
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path.get(b, startRow, startCol, destRow, destCol).booleanValue() ==
				b.getPieceAt(startRow, startCol).getColor();
	}
}

class IntegerGreaterThanCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8834890196662283482L;
	IntegerPath path1, path2;
	public IntegerGreaterThanCondition(IntegerPath path1, IntegerPath path2) {
		this.path1 = path1;
		this.path2 = path2;
	}
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path1.get(b, startRow, startCol, destRow, destCol).intValue() >
				path2.get(b, startRow, startCol, destRow, destCol).intValue();
	}
}

class IntegerLessThanCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2846848638933806135L;
	IntegerPath path1, path2;
	public IntegerLessThanCondition(IntegerPath path1, IntegerPath path2) {
		this.path1 = path1;
		this.path2 = path2;
	}
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path1.get(b, startRow, startCol, destRow, destCol).intValue() < 
				path2.get(b, startRow, startCol, destRow, destCol).intValue();
	}
}

class IntegerGreaterThanOrEqualCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6791049172165444346L;
	IntegerPath path1, path2;
	public IntegerGreaterThanOrEqualCondition(IntegerPath path1, IntegerPath path2) {
		this.path1 = path1;
		this.path2 = path2;
	}
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path1.get(b, startRow, startCol, destRow, destCol).intValue() >= 
				path2.get(b, startRow, startCol, destRow, destCol).intValue();
	}
}

class IntegerLessThanOrEqualCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5092626488745968229L;
	IntegerPath path1, path2;
	public IntegerLessThanOrEqualCondition(IntegerPath path1, IntegerPath path2) {
		this.path1 = path1;
		this.path2 = path2;
	}
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path1.get(b, startRow, startCol, destRow, destCol).intValue() <= 
				path2.get(b, startRow, startCol, destRow, destCol).intValue();
	}
}

class IntegerEqualsCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7832125727388222734L;
	IntegerPath path1, path2;
	public IntegerEqualsCondition(IntegerPath path1, IntegerPath path2) {
		this.path1 = path1;
		this.path2 = path2;
	}
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path1.get(b, startRow, startCol, destRow, destCol).intValue() == 
				path2.get(b, startRow, startCol, destRow, destCol).intValue();
	}
}

class IntegerNotEqualsCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5997723352136973125L;
	IntegerPath path1, path2;
	public IntegerNotEqualsCondition(IntegerPath path1, IntegerPath path2) {
		this.path1 = path1;
		this.path2 = path2;
	}
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path1.get(b, startRow, startCol, destRow, destCol).intValue() != 
				path2.get(b, startRow, startCol, destRow, destCol).intValue();
	}
}

class ObjectEqualsCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = -365220130739916568L;
	ObjectPath path1, path2;
	public ObjectEqualsCondition(ObjectPath path1, ObjectPath path2) {
		this.path1 = path1;
		this.path2 = path2;
	}
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path1.get(b, startRow, startCol, destRow, destCol).equals(
				path2.get(b, startRow, startCol, destRow, destCol));
	}
}

class ObjectNotEqualsConditions extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4458720357778347839L;
	ObjectPath path1, path2;
	public ObjectNotEqualsConditions(ObjectPath path1, ObjectPath path2) {
		this.path1 = path1;
		this.path2 = path2;
	}
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	!path1.get(b, startRow, startCol, destRow, destCol).equals(
				path2.get(b, startRow, startCol, destRow, destCol));
	}
}

class ObjectIsNullCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = 9026549054957151877L;
	ObjectPath path;
	public ObjectIsNullCondition(ObjectPath path) {
		this.path = path;
	}
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path.get(b, startRow, startCol, destRow, destCol) == null;
	}
}

class ObjectIsNotNullCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5096607902070365755L;
	public ObjectPath getPath() {
		return path;
	}
	public void setPath(ObjectPath path) {
		this.path = path;
	}
	ObjectPath path;
	public ObjectIsNotNullCondition() {}
	public ObjectIsNotNullCondition(ObjectPath path) {
		this.path = path;
	}
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	path.get(b, startRow, startCol, destRow, destCol) != null;
	}
}

class ObjectInstanceOfCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1920027248961903582L;
	ObjectPath path;
	Class<?> caster;
	
	public ObjectInstanceOfCondition(ObjectPath path, Class<?> caster) {
		this.path = path;
		this.caster = caster;
	}
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	caster.isInstance(path.get(b, startRow, startCol, destRow, destCol));
	}
}

class ObjectIsPieceCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8092481768362985408L;
	ObjectPath path;
	String pieceName;
	
	public ObjectIsPieceCondition(ObjectPath path, String pieceName) {
		this.path = path;
		this.pieceName = pieceName;
	}
	
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		Object obj = path.get(b, startRow, startCol, destRow, destCol);
		if(obj != null && obj instanceof Piece) {
			return ((Piece) obj).getPieceName().equals(pieceName);
		}
		return false;
	}
}


class ANDCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8781760452433119891L;
	Condition c1, c2;
	
	public ANDCondition(Condition c1, Condition c2) {
		this.c1 = c1;
		this.c2 = c2;
	}
	
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	c1.calc(b, startRow, startCol, destRow, destCol) &&
				c2.calc(b, startRow, startCol, destRow, destCol);
				
	}
}

class ORCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5333062990919125832L;
	Condition c1, c2;
	
	public ORCondition(Condition c1, Condition c2) {
		this.c1 = c1;
		this.c2 = c2;
	}
	
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	c1.calc(b, startRow, startCol, destRow, destCol) ||
				c2.calc(b, startRow, startCol, destRow, destCol);
				
	}
}

class XORCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2320943957306912375L;
	Condition c1, c2;
	
	public XORCondition(Condition c1, Condition c2) {
		this.c1 = c1;
		this.c2 = c2;
	}
	
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	c1.calc(b, startRow, startCol, destRow, destCol) ^
				c2.calc(b, startRow, startCol, destRow, destCol);
				
	}
}

class NOTCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2020623859815942516L;
	Condition c1;
	
	public NOTCondition(Condition c1) {
		this.c1 = c1;
	}
	
	@Override
	boolean eval(Board b, int startRow, int startCol, int destRow, int destCol) {
		return 	!c1.calc(b, startRow, startCol, destRow, destCol);
	}
}

