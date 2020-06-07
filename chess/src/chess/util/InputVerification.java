package chess.util;

/**
 * This interface can be used to signify that a class has some user input before that needs to be verified.
 * @author Sam
 *
 */
public interface InputVerification{
	/**
	 * Returns whether or not this object currently contains valid user input.
	 * @return true if input is valid, false otherwise.
	 */
	public boolean verifyInput();
}
