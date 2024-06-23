package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;


/**
 * <p>CalMagnetList class.</p>
 *
 * @author igor@scictrl.com
 */
public class CalMagnetList extends AbstractProtectedMagnetList<AbstractCalibratedMagnet> {

	/**
	 * This is a default constructor for <code>CalMagnetList</code>.
	 */
	CalMagnetList(DataBushHandler t) {
		super(t);
		type= AbstractCalibratedMagnet.class;
	}
	/**
	 * This method execute apply methon on owner of this list.
	 *
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if different bendings in list demands different current.
	 * @see DataBushHandler#apply
	 * @return a int integer which indicate if method succeded
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public int apply() throws InconsistentDataException, IllegalStateException, DataBushPackedException {
		return owner.apply(this);
	}
	/**
	 * This method generate and return array of <code>AbstractCalibratedMagnet</code> elements.
	 *
	 * @return an array of {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractCalibratedMagnet} objects
	 */
	public AbstractCalibratedMagnet[] toCalMagnetArray() {
		Object[] o= dl.toArray();
		AbstractCalibratedMagnet[] e= new AbstractCalibratedMagnet[o.length];
		for (int i=0; i<o.length;i++) e[i]=(AbstractCalibratedMagnet)o[i];
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
