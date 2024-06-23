package org.scictrl.mp.orbitcorrect.accessories;

import java.util.Comparator;

import org.scictrl.mp.orbitcorrect.model.optics.SimpleData;
/**
 * DataPositionComparator is implementation of java.util.Comparator and it is used to
 * compare its two arguments for order.It is used to order storage ring elements, as
 * the beam sees them.They are ordered by the position of that elements.
 *
 * @see java.util.Comparator
 * @see SimpleData
 * @see #compare(Object, Object)
 * @author igor@scictrl.com
 */
public class DataPositionComparator implements Comparator<SimpleData> {
	/**
	 * OpticsPositionComparator constructor.
	 */
	public DataPositionComparator() {
		super();
	}
	/**
	 * {@inheritDoc}
	 *
	 * Compares two arguments and returns -1, 0, 1 as the first argument is before the,
	 * at the same position as, or after the second.Arguments are ment as storage ring
	 * elements.
	 */
	@Override
	public int compare(SimpleData arg1, SimpleData arg2) {
		double d= arg1.getPosition()-arg2.getPosition();
		if (d<0) return -1;
		if (d>0) return 1;
		return 0;
	}
}
