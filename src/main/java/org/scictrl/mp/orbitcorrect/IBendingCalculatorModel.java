package org.scictrl.mp.orbitcorrect;

/**
 * <p>IBendingCalculatorModel interface.</p>
 *
 * @author igor@scictrl.com
 */
public interface IBendingCalculatorModel {

	/**
	 * <p>calcCurrentFromEnergy.</p>
	 *
	 * @param energy a double
	 * @return a double
	 */
	double calcCurrentFromEnergy(double energy);

	/**
	 * <p>calcCurrentFromQuadrupoleStrength.</p>
	 *
	 * @param gradStrength a double
	 * @return a double
	 */
	double calcCurrentFromQuadrupoleStrength(double gradStrength);

	/**
	 * <p>calcEnergy.</p>
	 *
	 * @param current a double
	 * @return a double
	 */
	double calcEnergy(double current);

	/**
	 * <p>calcQuadrupoleStrength.</p>
	 *
	 * @param current a double
	 * @return a double
	 */
	double calcQuadrupoleStrength(double current);

	/**
	 * <p>getName.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	java.lang.String getName();

	/**
	 * <p>getEnergy.</p>
	 *
	 * @return double
	 */
	double getEnergy();

	/**
	 * <p>setEnergy.</p>
	 *
	 * @param energy double
	 */
	void setEnergy(double energy);

}


