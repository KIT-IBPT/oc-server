package org.scictrl.mp.orbitcorrect.accessories;

import java.util.Comparator;

import org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement;
/**
 * Compares two optical elements and orders them by the position.
 *
 * @author igor@scictrl.com
 */
public class OpticsPositionComparator implements Comparator<Object> {
/**
 * OpticsPositionComparator constructor.
 */
public OpticsPositionComparator() {
	super();
}
/**
 * {@inheritDoc}
 *
 * Compares two optical elements and returns -1, 0, 1 as the first element is before the,
 * at the same position as, or after the second element.Elements are ment as storage ring
 * elements.
 */
@Override
public int compare(Object arg1, Object arg2) {
	double d= ((AbstractOpticalElement)arg1).getPosition()-((AbstractOpticalElement)arg2).getPosition();
	if (d<0) return -1;
	if (d>0) return 1;
	return 0;
}
}
