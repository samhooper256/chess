package chess.util;

public class IntAttribute implements WritableAttributeBase<Integer>, ReadableAttributeBase<Integer>{
	
	private Integer value;
	
	public IntAttribute(Integer item) {
		value = item;
	}
	
	@Override
	public Integer get() {
		return value;
	}
	
	@Override
	public void set(Integer item) {
		value = item;
	}
	
	public int getAsPrimitive() {
		return value.intValue();
	}
	
}
