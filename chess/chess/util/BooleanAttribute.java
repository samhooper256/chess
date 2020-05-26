package chess.util;

public class BooleanAttribute implements WritableAttributeBase<Boolean>, ReadableAttributeBase<Boolean>{
	
	private Boolean value;
	
	public BooleanAttribute(Boolean item) {
		value = item;
	}
	
	@Override
	public Boolean get() {
		return value;
	}
	
	public boolean getAsPrimitive() {
		return value.booleanValue();
	}
	
	@Override
	public void set(Boolean item) {
		value = item;
	}
	
}
