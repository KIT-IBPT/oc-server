package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
/**
 * This list contains elements type <code>AbstractTransferElement</code>.
 *
 * @see TransferList
 * @see AbstractTransferElement
 * @author igor@scictrl.com
 */
public class TransferList extends AbstractProtectedList<AbstractTransferElement> {

	/**
	 * This is a default constructor for <code>TransferList</code>.
	 */
	TransferList(DataBushHandler t) {
		super(t);
		type=AbstractTransferElement.class;
	}
	/**
	 * This method execute apply method on owner of this list.
	 *
	 * @see DataBush#apply()
	 * @return a int integer which indicate if method succeded
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public int apply() throws InconsistentDataException, IllegalStateException, DataBushPackedException {
		return owner.apply(owner.ent.magnets);
	}
	/**
	 * This method generate and return array of <code>AbstractTransferElement</code> elements.
	 *
	 * @return an array of {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractTransferElement} objects
	 */
	public AbstractTransferElement[] toTransferArray() {
		Object[] o= dl.toArray();
		AbstractTransferElement[] e= new AbstractTransferElement[o.length];
		for (int i=0; i<o.length;i++) e[i]=(AbstractTransferElement)o[i];
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
		return owner.update(owner.ent.magnets);
	}
}
