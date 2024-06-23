package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;


/**
 * <p>CorrectorList class.</p>
 *
 * @author igor@scictrl.com
 */
public class CorrectorList extends AbstractProtectedMagnetList<AbstractCorrector> {

	/**
	 * This is a default constructor for <code>CorrectorList</code>.
	 */
	CorrectorList(DataBushHandler t) {
		super(t);
		type= AbstractCorrector.class;
	}
	/**
	 * <p>apply.</p>
	 *
	 * @return a int
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public int apply() throws InconsistentDataException, IllegalStateException, DataBushPackedException {
		return owner.apply(this);
	}
	/**
	 * <p>toCorrectorArray.</p>
	 *
	 * @return an array of {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector} objects
	 */
	public AbstractCorrector[] toCorrectorArray() {
		Object[] o= dl.toArray();
		AbstractCorrector[] e= new AbstractCorrector[o.length];
		for (int i=0; i<o.length;i++) e[i]=(AbstractCorrector)o[i];
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
