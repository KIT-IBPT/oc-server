package org.scictrl.mp.orbitcorrect.accessories;

import org.scictrl.mp.orbitcorrect.IBendingCalculatorModel;
/**
 * <p>DefaultBendingCalculator class.</p>
 *
 * @author igor@scictrl.com
 */
public class DefaultBendingCalculator extends AbstractMagnetCalculator implements IBendingCalculatorModel {

	private PolynomialFunction trans_E;
	private PolynomialFunction trans_k;

	/**
	 * DefaultBendingCalculator constructor.
	 *
	 * @param name a {@link java.lang.String} object
	 * @param transE a {@link org.scictrl.mp.orbitcorrect.accessories.PolynomialFunction} object
	 * @param transk a {@link org.scictrl.mp.orbitcorrect.accessories.PolynomialFunction} object
	 */
	public DefaultBendingCalculator(String name, PolynomialFunction transE,PolynomialFunction transk) {
		super(name);
		trans_E= transE;
		trans_k= transk;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Calculates current in bending magnets from energy of the beam.
	 */
	@Override
	public double calcCurrentFromEnergy(double energy) {
		return trans_E.X(energy);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Calculates current in quadrupoles from quadrupole field strength.
	 */
	@Override
	public double calcCurrentFromQuadrupoleStrength(double gradStrength) {
		return trans_k.X(gradStrength*e);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Calculates energy from current in bendings.
	 */
	@Override
	public double calcEnergy(double current) {
		return trans_E.Y(current);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Calculats quadrupolfield strength from current.
	 */
	@Override
	public double calcQuadrupoleStrength(double current) {
		return e==0.0 ? 0.0 : trans_k.Y(current)/e;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Clones DefaultBendingCalculator.
	 */
	@Override
	public Object clone() {
		DefaultBendingCalculator o= (DefaultBendingCalculator)super.clone();
		o.trans_E= (PolynomialFunction)trans_E.clone();
		o.trans_k= (PolynomialFunction)trans_k.clone();
		return o;
	}

	/**
	 * Sets plynomial function trans_E for calculatin energy.
	 */
	void setTrans_E(PolynomialFunction f) {
		trans_E=f;
	}

	/**
	 * Sets polynomial function trans_k for calculating quadrupole field strength.
	 */
	void setTrans_k(PolynomialFunction f) {
		trans_k=f;
	}
}
