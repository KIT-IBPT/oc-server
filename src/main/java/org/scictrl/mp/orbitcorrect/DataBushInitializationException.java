package org.scictrl.mp.orbitcorrect;

import org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement;


/**
 * This exception occurs at initialization.
 *
 * @author igor@scictrl.com
 */
public class DataBushInitializationException extends DataBushException {
/**
	 *
	 */
	private static final long serialVersionUID = 1L;
/**
 * <code>DataBushInitializationException</code> constructor with specified parameter.
 *
 * @param s java.lang.String
 */
public DataBushInitializationException(String s) {
	super(s);
}
/**
 * <code>DataBushInitializationException</code> constructor with specified parameters.
 *
 * @param s java.lang.String
 * @param source AbstractDataBushElement
 */
public DataBushInitializationException(AbstractDataBushElement source, String s) {
	super(source,s);
}
/**
 * <code>DataBushInitializationException</code> constructor with specified parameter.
 *
 * @param e DataBushInterface.InconsistentDataException
 */
public DataBushInitializationException(InconsistentDataException e) {
	this(e.getSource(),e.getMessage());
}
/**
 * {@inheritDoc}
 *
 * Returns the fully-qualified name of the entity represented by this <code>DataBushException</code>
 * object, as a <code>String</code>.
 */
@Override
protected String getClassName() {
	return this.getClass().getName();
}
}
