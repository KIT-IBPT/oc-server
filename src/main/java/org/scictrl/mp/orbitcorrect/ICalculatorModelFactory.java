package org.scictrl.mp.orbitcorrect;

/**
 * <p>
 * ICalculatorModelFactory interface.
 * </p>
 *
 * @author igor@scictrl.com
 */
public interface ICalculatorModelFactory extends IConfigurable {

	/**
	 * <p>
	 * getBendingCalculatorModel.
	 * </p>
	 *
	 * @param entry a {@link java.lang.String} object
	 * @return a {@link org.scictrl.mp.orbitcorrect.IBendingCalculatorModel} object
	 * @throws org.scictrl.mp.orbitcorrect.EntryNotFound if any.
	 */
	IBendingCalculatorModel getBendingCalculatorModel(java.lang.String entry) throws EntryNotFound;

	/**
	 * <p>
	 * getGenericMagnetCalculatorModel.
	 * </p>
	 *
	 * @param entry a {@link java.lang.String} object
	 * @return a {@link IMagnetCalculatorModel} object
	 * @throws EntryNotFound if any.
	 */
	IMagnetCalculatorModel getGenericMagnetCalculatorModel(java.lang.String entry) throws EntryNotFound;

	/**
	 * <p>
	 * setEnergyToAll.
	 * </p>
	 *
	 * @param energy a double
	 */
	void setEnergyToAll(double energy);
}
