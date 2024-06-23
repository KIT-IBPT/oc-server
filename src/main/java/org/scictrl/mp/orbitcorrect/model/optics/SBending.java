package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.DBElementDescriptor;


/**
 * This type represent sector dipole magnet. Face of this magnet is always rectangular to particle beam.
 *
 * @author igor@scictrl.com
 */
public class SBending extends AbstractBending {
/**
 * Constructs <code>SBending</code> with specified name and default parameter's values.
 *
 * @param name a {@link java.lang.String} name of abstract bending
 */
public SBending(String name) {
	super(name);
}
/**
 * Constructs the <code>SBending</code> with specified parameters.
 *
 * @param name a {@link java.lang.String} object
 * @param virtual a boolean
 * @param position a double
 * @param relpos a double
 * @param relFrom a {@link java.lang.String} object
 * @param length a double
 * @param ps a {@link java.lang.String} object
 * @param calibrationEntry a {@link java.lang.String} object
 * @param quadrupoleStrength a double
 * @param radius a double
 * @param dipoleField a double
 * @param energy a double
 */
public SBending(String name, boolean virtual, double position, double relpos, String relFrom, double length, String ps, String calibrationEntry, double quadrupoleStrength, double radius, double dipoleField, double energy) {
	super(name, virtual, position, relpos, relFrom,length, ps, calibrationEntry, quadrupoleStrength, radius, dipoleField, energy);
}
/**
 * {@inheritDoc}
 *
 * <p>descriptor.</p>
 * @see AbstractDataBushElement#descriptor
 */
@Override
public DBElementDescriptor descriptor() {
	return DBElementDescriptor.ELDES_SBENDING;
}
/**
 * {@inheritDoc}
 *
 * <p>elType.</p>
 * @see AbstractDataBushElement#elType
 */
@Override
public int elType() {
	return DBElementDescriptor.EL_SBENDING;
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
