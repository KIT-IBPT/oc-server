package org.scictrl.mp.orbitcorrect;

import org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;


/**
 * <p>ActionReportEvent class.</p>
 *
 * @author igor@scictrl.com
 */
public class ActionReportEvent extends DataBushEvent {

	private static final long serialVersionUID = 5781700973557138980L;

	/** Error code. */
	private int code;

	/** Accumulation of exceptions. */
	private DataBushPackedException exceptions;

	/**
	 * ActionReportEvent constructor comment.
	 *
	 * @param db a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} object
	 * @param message a {@link java.lang.String} object
	 * @param code a int
	 */
	public ActionReportEvent(DataBush db,String message, int code) {
		super(db,message);
		this.code= code;
	}
	/**
	 * ActionReportEvent constructor comment.
	 *
	 * @param db a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} object
	 * @param message a {@link java.lang.String} object
	 * @param code a int
	 * @param ex a {@link org.scictrl.mp.orbitcorrect.DataBushPackedException} object
	 */
	public ActionReportEvent(DataBush db,String message, int code, DataBushPackedException ex) {
		super(db,message);
		this.code= code;
		exceptions= ex;
	}
	/**
	 * ActionReportEvent constructor comment.
	 *
	 * @param source java.lang.Object
	 * @param message a {@link java.lang.String} object
	 * @param code a int
	 */
	public ActionReportEvent(AbstractDataBushElement source, String message, int code) {
		super(source,message);
		this.code= code;
	}
	/**
	 * ActionReportEvent constructor comment.
	 *
	 * @param source java.lang.Object
	 * @param message a {@link java.lang.String} object
	 * @param code a int
	 * @param ex a {@link org.scictrl.mp.orbitcorrect.DataBushPackedException} object
	 */
	public ActionReportEvent(AbstractDataBushElement source, String message, int code, DataBushPackedException ex) {
		super(source,message);
		this.code= code;
		exceptions= ex;
	}
	/**
	 * <p>Getter for the field <code>exceptions</code>.</p>
	 *
	 * @return PackedDBException
	 */
	public DataBushPackedException getExceptions() {
		return exceptions;
	}
	/**
	 * <p>getReportCode.</p>
	 *
	 * @return java.lang.String
	 */
	public int getReportCode() {
		return code;
	}
	/**
	 *
	 * @return java.lang.String
	 */
	void setReportCode(int value) {
		code= value;
	}
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ActionReportEvent"+((sourceEl!=null) ? ("["+sourceEl.getName()+"]") : "")+": "+message+" code: "+code+((exceptions==null) ? "" : " "+exceptions);
	}
}
