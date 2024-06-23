package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;


/**
 * <p>VerCorrectorList class.</p>
 *
 * @author igor@scictrl.com
 */
public class VerCorrectorList extends AbstractProtectedMagnetList<VerCorrector> {

	/**
	 * This is a default constructor for <code>VerCorrectorList</code>.
	 */
	VerCorrectorList(DataBushHandler t) {
		super(t);
		type=VerCorrector.class;
	}
	/**
	 * This method execute apply method on owner of this list.
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
	 * This method generate and return array of <code>VerCorrector</code> elements.
	 *
	 * @return an array of {@link org.scictrl.mp.orbitcorrect.model.optics.VerCorrector} objects
	 */
	public VerCorrector[] toCorrectorArray() {
		Object[] o= dl.toArray();
		VerCorrector[] e= new VerCorrector[o.length];
		for (int i=0; i<o.length;i++) e[i]=(VerCorrector)o[i];
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
