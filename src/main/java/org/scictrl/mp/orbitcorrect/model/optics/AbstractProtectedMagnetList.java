package org.scictrl.mp.orbitcorrect.model.optics;



/**
 * Abstract list for all lists containing magnet elements. Contains aditional list with
 * associated PowerSupply elements.
 *
 * @author igor@scictrl.com
 * 
 * @param <T> elements type
 */
public abstract class AbstractProtectedMagnetList<T extends AbstractMagnetElement> extends AbstractProtectedList<T> {
	private PowerSupplyList powerSupplies;
	AbstractProtectedMagnetList(DataBushHandler owner) {
		super(owner);
		powerSupplies= new PowerSupplyList(owner);
	}
	@Override
	void clear() {
		super.clear();
		powerSupplies.clear();
	}
	/**
	 * Returns a list of PowerSupply elements associated with magnet elements from this list.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PowerSupplyList} a list of PowerSupply elements associated with magnet elements from this list
	 */
	public PowerSupplyList getPowerSupplies() {
		return powerSupplies;
	}
}
