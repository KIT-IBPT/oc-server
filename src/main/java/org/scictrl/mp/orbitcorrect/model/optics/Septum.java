package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.DBElementDescriptor;


/**
 * This type represents septum. It is not controlled by <code>DataBush</code> and is just place holder
 *
 * @author igor@scictrl.com
 */
public class Septum extends AbstractMagnetElement {
/**
 * Constructs <code>Septum</code> with specified name and default parameter's values.
 *
 * @param name a {@link java.lang.String} name
 */
public Septum(String name) {
	super(name);
}
/**
 * Constructs the <code>Septum</code> with specified parameters.
 *
 * @param name a {@link java.lang.String} object
 * @param virtual a boolean
 * @param position a double
 * @param relpos a double
 * @param relFrom a {@link java.lang.String} object
 * @param length a double
 * @param ps a {@link java.lang.String} object
 */
public Septum(String name, boolean virtual, double position, double relpos, String relFrom, double length, String ps) {
	super(name,virtual,position,relpos,relFrom,length,ps);
}
/**
 * {@inheritDoc}
 *
 * <p>descriptor.</p>
 * @see AbstractDataBushElement#descriptor
 */
@Override
public DBElementDescriptor descriptor() {
	return DBElementDescriptor.ELDES_SEPTUM;
}
/**
 * {@inheritDoc}
 *
 * <p>elType.</p>
 * @see AbstractDataBushElement#elType
 */
@Override
public int elType() {
	return DBElementDescriptor.EL_SEPTUM;
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
	return super.toString()+" >"+DBConst.EOL;
}
}
