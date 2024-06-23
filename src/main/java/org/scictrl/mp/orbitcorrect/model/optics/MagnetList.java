package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;


/**
 * <p>MagnetList class.</p>
 *
 * @author igor@scictrl.com
 */
public class MagnetList extends AbstractProtectedMagnetList<AbstractMagnetElement> {

	/**
	 * This is a default constructor for <code>MagnetList</code>.
	 */
	MagnetList(DataBushHandler t) {
		super(t);
		type=AbstractMagnetElement.class;
	}
	/**
	 * This method execute apply methon on owner of this list.
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
	 * This method generate and return array of <code>AbstractMagnetElement</code> elements.
	 *
	 * @return an array of {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractMagnetElement} objects
	 */
	public AbstractMagnetElement[] toMagnetArray() {
		Object[] o= dl.toArray();
		AbstractMagnetElement[] e= new AbstractMagnetElement[o.length];
		for (int i=0; i<o.length;i++) e[i]=(AbstractMagnetElement)o[i];
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
