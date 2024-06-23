package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DBElementDescriptor;


/**
 * This type represents horizontal corrector. Correctors helds dipole field and are used to
 * change direction of particle velocity as correction of bending magnets and other errors.
 *
 * @author igor@scictrl.com
 */
public class HorCorrector extends AbstractCorrector {
/**
 * Constructs <code>HorCorrector</code> with specified name and default parameter's values.
 *
 * @param name a {@link java.lang.String} name of abstract corrector
 */
public HorCorrector(String name) {
	super(name);
}
/**
 * Constructs the <code>HorCorrector</code> with specified parameters.
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
public HorCorrector(String name, boolean virtual, double position, double relpos, String relFrom, double length, String ps, String calibrationEntry, double angle) {
	super(name,virtual,position,relpos,relFrom,length,ps, calibrationEntry,angle);
}
/**
 * {@inheritDoc}
 *
 * <p>descriptor.</p>
 * @see AbstractDataBushElement#descriptor()
 */
@Override
public DBElementDescriptor descriptor() {
	return DBElementDescriptor.ELDES_HOR_CORRECTOR;
}
/**
 * {@inheritDoc}
 *
 * <p>elType.</p>
 * @see AbstractDataBushElement#elType()
 */
@Override
public int elType() {
	return DBElementDescriptor.EL_HOR_CORRECTOR;
}
}
