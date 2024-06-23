package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
/**
 * This list contains elements type <code>PowerSupply</code>.
 *
 * @see PowerSupplyList
 * @see PowerSupply
 * @author igor@scictrl.com
 */
public class PowerSupplyList extends AbstractProtectedList<PowerSupply> {

	/**
	 * This is a default constructor for <code>PowerSupplyList</code>.
	 */
	PowerSupplyList(DataBushHandler t) {
		super(t);
		type=PowerSupply.class;
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
		return owner.apply(this);
	}
	/**
	 * This method generate and return array of <code>PowerSupply</code> elements.
	 *
	 * @return an array of {@link org.scictrl.mp.orbitcorrect.model.optics.PowerSupply} objects
	 */
	public PowerSupply[] toPowerSupplyArray() {
		Object[] o= dl.toArray();
		PowerSupply[] e= new PowerSupply[o.length];
		for (int i=0; i<o.length;i++) e[i]=(PowerSupply)o[i];
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
