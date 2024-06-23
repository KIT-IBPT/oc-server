package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;


/**
 * <p>HorCorrectorList class.</p>
 *
 * @author igor@scictrl.com
 */
public class HorCorrectorList extends AbstractProtectedMagnetList<HorCorrector> {

	/**
	 * This is a default constructor for <code>HorCorrectorList</code>.
	 */
	HorCorrectorList(DataBushHandler t) {
		super(t);
		type=HorCorrector.class;
	}
	/**
	 * This method execute apply method on owner of this list.
	 *
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if different bendings in list demands different current.
	 * @return a int integer which indicate if method succeded
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public int apply() throws InconsistentDataException, IllegalStateException, DataBushPackedException {
		return owner.apply(this);
	}
	/**
	 * This method generate and return array of <code>HorCorrector</code> elements.
	 *
	 * @return an array of {@link org.scictrl.mp.orbitcorrect.model.optics.HorCorrector} objects
	 */
	public HorCorrector[] toCorrectorArray() {
		Object[] o= dl.toArray();
		HorCorrector[] e= new HorCorrector[o.length];
		for (int i=0; i<o.length;i++) e[i]=(HorCorrector)o[i];
		return e;
	}
	/**
	 * <p>update.</p>
	 *
	 * @return a int
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public int update() throws IllegalStateException, DataBushPackedException {
		return owner.update(this);
	}
}
