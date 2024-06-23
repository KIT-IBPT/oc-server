package org.scictrl.mp.orbitcorrect;

import org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;


/**
 * <p>DataBushEvent class.</p>
 *
 * @author igor@scictrl.com
 */
public class DataBushEvent extends java.util.EventObject {

	private static final long serialVersionUID = 1L;

	/** Event message. */
	protected String message;

	/** source elements. */
	protected AbstractDataBushElement sourceEl;
	/**
	 * DataBushEvent constructor comment.
	 *
	 * @param source java.lang.Object
	 */
	public DataBushEvent(DataBush source) {
		super(source);
	}
	/**
	 * DataBushEvent constructor comment.
	 *
	 * @param source java.lang.Object
	 * @param message a {@link java.lang.String} object
	 */
	public DataBushEvent(DataBush source, String message) {
		super(source);
		this.message= message;
	}
	/**
	 * ActionReportEvent constructor comment.
	 *
	 * @param source java.lang.Object
	 * @param message a {@link java.lang.String} object
	 */
	public DataBushEvent(AbstractDataBushElement source, String message) {
		this(source.getOwner(),message);
		sourceEl= source;
	}
	/**
	 * <p>Getter for the field <code>message</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * <p>getSourceElement.</p>
	 *
	 * @return DataBushInterface.DataBushElement
	 */
	public AbstractDataBushElement getSourceElement() {
		return sourceEl;
	}
	void setMessage(String st) {
		message= st;
	}
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "DataBushEvent"+((sourceEl!=null) ? ("["+sourceEl.getName()+"]") : "")+": "+message;
	}
}
