package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DBElementDescriptor;


/**
 * This type represents vertical corrector. It correct particle path in vertical plane.
 *
 * @see HorCorrector
 * @author igor@scictrl.com
 */
public class VerCorrector extends AbstractCorrector {
/**
 * Constructs <code>VerCorrector</code> with specified name and default parameter's values.
 *
 * @param name a {@link java.lang.String} name
 */
public VerCorrector(String name) {
	super(name);
}
/**
 * Constructs the <code>VerCorrector</code> with specified parameters.
 *
 * @param name a {@link java.lang.String} object
 * @param virtual a boolean
 * @param position a double
 * @param relpos a double
 * @param relFrom a {@link java.lang.String} object
 * @param length a double
 * @param ps a {@link java.lang.String} object
 * @param calibrationEntry a {@link java.lang.String} object
 * @param angle a double
 */
public VerCorrector(String name, boolean virtual, double position, double relpos, String relFrom, double length, String ps, String calibrationEntry, double angle) {
	super(name,virtual,position,relpos,relFrom,length,ps,calibrationEntry,angle);
}
/**
 * {@inheritDoc}
 *
 * <p>descriptor.</p>
 * @see AbstractDataBushElement#descriptor
 */
@Override
public DBElementDescriptor descriptor() {
	return DBElementDescriptor.ELDES_VER_CORRECTOR;
}
/**
 * {@inheritDoc}
 *
 * <p>elType.</p>
 * @see AbstractDataBushElement#elType
 */
@Override
public int elType() {
	return DBElementDescriptor.EL_VER_CORRECTOR;
}
}
