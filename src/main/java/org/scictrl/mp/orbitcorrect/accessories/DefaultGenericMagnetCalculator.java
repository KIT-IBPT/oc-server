package org.scictrl.mp.orbitcorrect.accessories;

import org.scictrl.mp.orbitcorrect.IMagnetCalculatorModel;
/**
 * DefaultGenericMagnetCalculator is used to calculate currents from field strengths and vice versa.
 *
 * @see AbstractMagnetCalculator
 * @see org.scictrl.mp.orbitcorrect.IMagnetCalculatorModel
 * @author igor@scictrl.com
 */
public class DefaultGenericMagnetCalculator extends AbstractMagnetCalculator implements IMagnetCalculatorModel {
	private PolynomialFunction trans;
	/**
	 * <p>Constructor for DefaultGenericMagnetCalculator.</p>
	 *
	 * @param trans PolynomialFunction
	 * @param name a {@link java.lang.String} object
	 */
	public DefaultGenericMagnetCalculator(String name, PolynomialFunction trans) {
		super(name);
		this.trans= trans;
	}
	/**
	 * {@inheritDoc}
	 *
	 * Calculates current for gven magnet from given field strength.
	 */
	@Override
	public double calcCurrent(double fieldStrength) {
		return trans.X(fieldStrength*e);
	}
	/**
	 * {@inheritDoc}
	 *
	 * Calculates field strength for given magnet at given current.
	 */
	@Override
	public double calcFieldStrength(double current) {
		return trans.Y(current)/e;
	}
	/**
	 * {@inheritDoc}
	 *
	 * Clones DefaultGenericMagnetCalculator.
	 */
	@Override
	public Object clone() {
		DefaultGenericMagnetCalculator o= (DefaultGenericMagnetCalculator)super.clone();
		o.trans= (PolynomialFunction)trans.clone();
		return o;
	}
}
