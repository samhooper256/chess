package chess.util;

public class Attribute<E> implements WritableAttributeBase<E>, ReadableAttributeBase<E>{
	
	private E value;
	
	public Attribute(E item) {
		value = item;
	}
	
	@Override
	public E get() {
		return value;
	}
	
	@Override
	public void set(E item) {
		value = item;
	}
	
}
