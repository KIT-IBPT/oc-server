package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DBElementDescriptor;


/**
 * This type represent kicker magnet. This is corrector, which is modeled as it would change direction of particle's velocity discontinously inthe
 * center of it for a specified angle.
 *
 * @see HorCorrector
 * @author igor@scictrl.com
 */
public class Kicker extends AbstractCorrector {
/**
 * Constructs <code>Kicker</code> with specified name and default parameter's values.
 *
 * @param name a {@link java.lang.String} name of abstract corrector
 */
public Kicker(String name) {
	super(name);
}
/**
 * Constructs the <code>Kicker</code> with specified parameters.
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
public Kicker(String name, boolean virtual, double position, double relpos, String relFrom, double length, String ps, String calibrationEntry, double angle) {
	super(name,virtual,position,relpos,relFrom,length,ps,calibrationEntry,angle);
}
/**
 * {@inheritDoc}
 *
 * <p>descriptor.</p>
 * @see AbstractDataBushElement#descriptor()
 */
@Override
public DBElementDescriptor descriptor() {
	return DBElementDescriptor.ELDES_KICKER;
}
/**
 * {@inheritDoc}
 *
 * <p>elType.</p>
 * @see AbstractDataBushElement#elType()
 */
@Override
public int elType() {
	return DBElementDescriptor.EL_KICKER;
}
}
