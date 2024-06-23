package org.scictrl.mp.orbitcorrect;

/**
 * This type represent exception that is thrown usually by method, which takes string (key) as argument
 * and do something with this string, if string is suitable. If not, this exception is thrown.
 *
 * @author igor@scictrl.com
 */
public class EntryNotFound extends Exception {
/**
	 *
	 */
	private static final long serialVersionUID = 1L;
/**
 * Default <code>EntryNotFound</code> constructor.
 */
public EntryNotFound() {
	super();
}
/**
 * Constructs an <code>EntryNotFound</code> exception with the specified detail message.
 *
 * @param   s   the detail message.
 */
public EntryNotFound(String s) {
	super(s);
}
/**
 * {@inheritDoc}
 *
 * Returns a String that represents the value of this object.
 */
@Override
public String toString() {
	// Insert code to print the receiver here.
	// This implementation forwards the message to super. You may replace or supplement this.
	return "entry not found: "+super.getMessage();
}
}
