package org.scictrl.mp.orbitcorrect;


/**
 * <code>ControlSystemException</code> is Exception that is thrown when dealing with control system.
 *
 * @author igor@scictrl.com
 */
public class ControlSystemException extends Exception {

	private static final long serialVersionUID = 1L;

	/** Constant <code>TEST_FAILED_MESSAGE="Test failed for: "</code> */
	public static final String TEST_FAILED_MESSAGE = "Test failed for: ";

	/** Underlying exception. */
	private java.lang.Throwable intercepted;

	/** Exception source object. */
	private java.lang.Object source = null;

	/** Next exception in sequence. */
	private ControlSystemException next;

	/**
	 * <code>ControlSystemException</code> default constructor.
	 */
	public ControlSystemException() {
		super();
	}
	/**
	 * ControlSystemException constructor comment.
	 *
	 * @param source a {@link java.lang.Object} object
	 * @param intercepted a {@link java.lang.Throwable} object
	 */
	public ControlSystemException(Object source, Throwable intercepted) {
		this(null,source);
		this.intercepted=intercepted;
	}
	/**
	 * Constructs an <code>ControlSystemException</code> with the specified detail message.
	 *
	 * @param   s   the detail message.
	 */
	public ControlSystemException(String s) {
		super(s);
	}
	/**
	 * Constructs an <code>ControlSystemException</code> with the specified detail message.
	 *
	 * @param   s   the detail message.
	 * @param source a {@link java.lang.Object} object
	 */
	public ControlSystemException(String s, Object source) {
		super(s);
		this.source=source;
	}
	/**
	 * ControlSystemException constructor comment.
	 *
	 * @param s java.lang.String
	 * @param source a {@link java.lang.Object} object
	 * @param intercepted a {@link java.lang.Throwable} object
	 */
	public ControlSystemException(String s, Object source, Throwable intercepted) {
		this(s,source);
		this.intercepted=intercepted;
	}
	/**
	 * ControlSystemException constructor comment.
	 *
	 * @param s java.lang.String
	 * @param source a {@link java.lang.Object} object
	 * @param intercepted a {@link java.lang.Throwable} object
	 * @param previous a {@link org.scictrl.mp.orbitcorrect.ControlSystemException} object
	 */
	public ControlSystemException(String s, Object source, Throwable intercepted, ControlSystemException previous) {
		this(s,source);
		this.intercepted=intercepted;
		previous.next=this;
	}
	/**
	 * ControlSystemException constructor comment.
	 *
	 * @param s java.lang.String
	 * @param intercepted a {@link java.lang.Throwable} object
	 */
	public ControlSystemException(String s, Throwable intercepted) {
		super(s);
		this.intercepted=intercepted;
	}
	/**
	 * ControlSystemException constructor comment.
	 *
	 * @param intercepted a {@link java.lang.Throwable} object
	 */
	public ControlSystemException(Throwable intercepted) {
		super();
		this.intercepted=intercepted;
	}
	/**
	 * <p>Getter for the field <code>intercepted</code>.</p>
	 *
	 * @return java.lang.Throwable
	 */
	public java.lang.Throwable getIntercepted() {
		return intercepted;
	}
	/**
	 * <p>Getter for the field <code>next</code>.</p>
	 *
	 * @return ControlSystemException
	 */
	public ControlSystemException getNext() {
		return next;
	}
	/**
	 * <p>Getter for the field <code>source</code>.</p>
	 *
	 * @return java.lang.Object
	 */
	public java.lang.Object getSource() {
		return source;
	}
	/**
	 * <p>hasNext.</p>
	 *
	 * @return boolean
	 */
	public boolean hasNext() {
		return next!=null;
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
		return super.toString()+(intercepted!=null ? " -> "+intercepted.toString() : "" );
	}
}
