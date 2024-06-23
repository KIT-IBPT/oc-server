package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.DBElementDescriptor;


/**
 * This type does not represent the actual element. It is inducted only to one mark position,
 * where we can read machine functions, calculate length from one to another position an so on.
 * It can be placed inside other element. Markers are also often used to marking points of simmetry.
 *
 * @author igor@scictrl.com
 */
public class Marker extends AbstractOpticalElement {
/**
 * Marker constructor, which request only name.
 *
 * @param name a {@link java.lang.String} object
 */
public Marker(String name) {
	super(name);
}
/**
 * Marker constructor, that request specified parameters:
 *
 * @param name a {@link java.lang.String} object
 * @param virtual a boolean
 * @param position a double
 * @param relpos a double
 * @param relFrom a {@link java.lang.String} object
 */
public Marker(String name, boolean virtual, double position, double relpos, String relFrom) {
	super(name, virtual,position,relpos,relFrom);
}
/**
 * {@inheritDoc}
 *
 * <p>descriptor.</p>
 * @see AbstractDataBushElement#descriptor()
 */
@Override
public DBElementDescriptor descriptor() {
	return DBElementDescriptor.ELDES_MARKER;
}
/**
 * {@inheritDoc}
 *
 * <p>elType.</p>
 * @see AbstractDataBushElement#elType()
 */
@Override
public int elType() {
	return DBElementDescriptor.EL_MARKER;
}
/**
 * {@inheritDoc}
 *
 * <p>setWith.</p>
 * @see AbstractDataBushElement#setWith(Object[])
 */
@Override
public void setWith(Object[] par) throws IllegalStateException {
	super.setWith(par);
}
/**
 * {@inheritDoc}
 *
 * Returns a String that represents the value of this object.
 */
@Override
public String toString() {
	return super.toString()+" >"+DBConst.EOL;
}
}
