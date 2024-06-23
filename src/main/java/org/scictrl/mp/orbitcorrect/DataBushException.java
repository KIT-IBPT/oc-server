package org.scictrl.mp.orbitcorrect;

import org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement;


/**
 * <code>DataBushException</code> is general exception for dealing with DataBush. It has three
 * subclasses for more specific exceptions.
 *
 * @author igor@scictrl.com
 */
public class DataBushException extends Exception {

	private static final long serialVersionUID = 1L;

	/** Source element. */
	public AbstractDataBushElement source=null;

	/** Exception code. */
	public int code=DBConst.RC_REMOTE_EXCEPTION;

	/**
	 * <code>DataBushException</code> constructor with specified parameter.
	 */
	public DataBushException() {
		super();
	}

	/**
	 * <code>DataBushException</code> constructor with specified parameter.
	 *
	 * @param message a {@link java.lang.String} object
	 */
	public DataBushException(String message) {
		super(message);
	}
	/**
	 * <code>DataBushException</code> constructor with specified parameter.
	 *
	 * @param message message
	 * @param cause the cause
	 */
	public DataBushException(String message, Throwable cause) {
		super(message,cause);
	}
	/**
	 * <code>DataBushException</code> constructor with specified parameters.
	 *
	 * @param exception message
	 * @param code error code
	 */
	public DataBushException(String exception, int code) {
		super(exception);
		this.code= code;
	}
	/**
	 * <code>DataBushException</code> constructor with specified parameters.
	 *
	 * @param source AbstractDataBushElement
	 * @param message the message
	 */
	public DataBushException(AbstractDataBushElement source, String message) {
		super(message);
		this.source=source;
	}
	/**
	 * <code>DataBushException</code> constructor with specified parameters.
	 *
	 * @param source AbstractDataBushElement
	 * @param message the message
	 * @param cause a {@link java.lang.Throwable} object
	 * @param code a int
	 */
	public DataBushException(AbstractDataBushElement source, String message, int code, Throwable cause) {
		super(message,cause);
		this.code= code;
		this.source=source;
	}
	/**
	 * <code>DataBushException</code> constructor with specified parameters.
	 *
	 * @param source AbstractDataBushElement
	 * @param message the message
	 * @param code error code
	 */
	public DataBushException(AbstractDataBushElement source, String message, int code) {
		this(source,message);
		this.code= code;
	}
	/**
	 * This method create string that describe exception.
	 *
	 * @return java.lang.String
	 */
	public String formatMessage() {
		StringBuffer st= new StringBuffer();
		if (source!=null) st.append("["+source.getName()+"#"+source.descriptor().getElClass()+"]");

		if (getMessage()!=null) {
			st.append(": ");
			st.append(getMessage());
		}

		if (getCause()!=null) {
			st.append(": ");
			st.append(getCause());
		}

		return st.toString();
	}
	/**
	 * Returns the fully-qualified name of the entity represented by this <code>DataBushException</code>
	 * object, as a <code>String</code>.
	 *
	 * @return java.lang.String
	 */
	protected String getClassName() {
		return this.getClass().getSimpleName();
	}
	/**
	 * <p>Getter for the field <code>code</code>.</p>
	 *
	 * @return int
	 */
	public int getCode() {
		return code;
	}
	/**
	 * Returns source of <code>DataBushException</code>, which is <code>AbstractDataBushElement</code>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement} object
	 */
	public AbstractDataBushElement getSource() {
		return source;
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method create string that describe this class object. This string is  different, than string, returned by
	 * <code>formatMessage</code> output. Actually it is extended with name, returned by <code>getClassName</code>.
	 * @see org.scictrl.mp.orbitcorrect.DataBushException#formatMessage
	 * @see org.scictrl.mp.orbitcorrect.DataBushException#getClassName
	 */
	@Override
	public String toString() {
		return getClassName()+": "+formatMessage();
	}
}
