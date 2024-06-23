package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;

/**
 * <p>BendingList class.</p>
 *
 * @author igor@scictrl.com
 */
public class BendingList extends AbstractProtectedMagnetList<AbstractBending> {

		/**
	 * This is a default constructor for <code>BendingList</code>.
	 */
	BendingList(DataBushHandler t) {
		super(t);
		type=AbstractBending.class;
	}
	/**
	 * This method execute apply method on owner of this list.
	 *
	 * @return integer which indicate if method succeded
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if different bendings in list demands different current.
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public int applyField() throws InconsistentDataException, IllegalStateException, DataBushPackedException {
		return owner.apply(this);
	}
	/**
	 * This method generate and return array of <code>AbstractBending</code> elements.
	 *
	 * @return an array of {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractBending} objects
	 */
	public AbstractBending[] toBendingArray() {
		Object[] o= dl.toArray();
		AbstractBending[] e= new AbstractBending[o.length];
		for (int i=0; i<o.length;i++) e[i]=(AbstractBending)o[i];
		return e;
	}
	/**
	 * This method reads current from <code>PowerSupply</code> and transforms it to magnetic properties and sets it to list's elements.
	 *
	 * @return a int
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public int update() throws IllegalStateException, DataBushPackedException {
		return owner.update(this);
	}
}
