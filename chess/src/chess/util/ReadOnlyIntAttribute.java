package chess.util;

public class ReadOnlyIntAttribute implements ReadableAttributeBase<Integer>{
	
	Integer value;
	
	public ReadOnlyIntAttribute(Integer item) {
		value = item;
	}
	
	public Integer get() {
		return value;
	}
	
	public int getAsInt() {
		return value.intValue();
	}
	
}
