package chess.util;

public abstract class CombinerCondition extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5236471405890630535L;
	protected Condition c1, c2;
	
	public CombinerCondition(Condition con1, Condition con2) {
		this.c1 = con1;
		this.c2 = con2;
		//System.out.println("CREATED="+this);
	}
	
	public Condition get1() {
		return c1;
	}
	
	public Condition get2() {
		return c2;
	}
	
	@Override
	public String toString() {
		return "["+getClass().getName()+"@"+hashCode()+":c1="+c1+", c2="+c2+"]";
	}
}
