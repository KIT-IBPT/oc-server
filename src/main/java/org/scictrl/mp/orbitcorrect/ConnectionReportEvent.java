package org.scictrl.mp.orbitcorrect;

import org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;


/**
 * <p>ConnectionReportEvent class.</p>
 *
 * @author igor@scictrl.com
 */
public class ConnectionReportEvent extends ActionReportEvent {
/**
	 *
	 */
	private static final long serialVersionUID = 1L;
/**
 * ActionReportEvent constructor comment.
 *
 * @param db a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} object
 * @param message a {@link java.lang.String} object
 * @param code a int
 */
public ConnectionReportEvent(DataBush db,String message, int code) {
	super(db,message,code);
}
/**
 * ActionReportEvent constructor comment.
 *
 * @param source java.lang.Object
 * @param message a {@link java.lang.String} object
 * @param code a int
 */
public ConnectionReportEvent(AbstractDataBushElement source, String message, int code) {
	super(source.getOwner(),message,code);
}
}
