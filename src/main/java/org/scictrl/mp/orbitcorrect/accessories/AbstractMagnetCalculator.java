package org.scictrl.mp.orbitcorrect.accessories;

import org.scictrl.mp.orbitcorrect.DBMath;
import org.scictrl.mp.orbitcorrect.IMagnetCalculatorModel;

/**
 * <p>AbstractMagnetCalculator implements IMagnetCalculatorModel and extends its functionality.</p>
 *
 * @see IMagnetCalculatorModel
 * @author igor@scictrl.com
 */
public abstract class AbstractMagnetCalculator implements Cloneable {
	
	/**
	 * Energy of electrons in GeV.
	 */
	protected double energy;
	
	/**
	 * Electron charge.
	 */
	protected double e;
	
	private String name;
	
	/**
	 * AbstractMagnetCalculator constructor that sets given parameter to field.
	 *
	 * @param name java.lang.String
	 */
	public AbstractMagnetCalculator(java.lang.String name) {
		this.name= name;
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * Clones AbstractMagnetCalculator and its values.
	 */
	@Override
	public Object clone() {
		AbstractMagnetCalculator o= null;
		try{
			o= (AbstractMagnetCalculator) super.clone();
		} catch (CloneNotSupportedException e) {e.printStackTrace();}
		o.e= e;
		o.energy= energy;
		o.name= name;
		return o;
	}
	
	/**
	 * Returns electrons energy.
	 *
	 * @return electrons energy
	 */
	public double getEnergy() {
		return energy;
	}
	
	/**
	 * Returns name.
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets energy for electrons.
	 *
	 * @param energy a double
	 */
	public void setEnergy(double energy) {
		this.energy= energy;
		e= energy==0.0 ? 0.0 : Math.sqrt(energy*energy-DBMath.MC2*DBMath.MC2);
	}
}
