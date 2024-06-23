package org.scictrl.mp.orbitcorrect;

/**
 * <p>IMagnetCalculatorModel interface.</p>
 *
 * @author igor@scictrl.com
 */
public interface IMagnetCalculatorModel {

	/**
	 * <p>getName.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	java.lang.String getName();

	/**
	 * <p>calcCurrent.</p>
	 *
	 * @return double
	 * @param fieldStrength double
	 */
	double calcCurrent(double fieldStrength);
	/**
	 * <p>calcFieldStrength.</p>
	 *
	 * @return double
	 * @param current double
	 */
	double calcFieldStrength(double current);
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
