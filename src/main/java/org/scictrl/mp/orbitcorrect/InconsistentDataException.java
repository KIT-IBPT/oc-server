package org.scictrl.mp.orbitcorrect;

import org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement;


/**
 * This exception occurs, if data is not consistent.
 *
 * @author igor@scictrl.com
 */
public class InconsistentDataException extends DataBushException {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * <code>InconsistentDataException</code> constructor with specified parameter.
	 *
	 * @param s java.lang.String
	 */
	public InconsistentDataException(String s) {
		super(s);
	}
	/**
	 * <code>InconsistentDataException</code> constructor with specified parameters.
	 *
	 * @param s java.lang.String
	 * @param source AbstractDataBushElement
	 */
	public InconsistentDataException(AbstractDataBushElement source, String s) {
		super(source, s);
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
