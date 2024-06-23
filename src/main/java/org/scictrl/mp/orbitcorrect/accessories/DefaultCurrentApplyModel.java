package org.scictrl.mp.orbitcorrect.accessories;

import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.scictrl.mp.orbitcorrect.ICurrentApplyModel;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCalibratedMagnet;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
/**
 * This is default implementation of ICurrentApplyModel. It calculates AVG
 * value of added currents.
 *
 * @author igor@scictrl.com
 */
public final class DefaultCurrentApplyModel implements ICurrentApplyModel {
	/**
	 * Current precision in DataBush.
	*/
	private final double precision= 1;
	/**
	 * ElementList of magnets that contribute to AVG value of current.
	*/
	public final ElementList<AbstractCalibratedMagnet> magnets= new ElementList<>();
	/**
	* ArrayList of current values.
	*/
	private double avg=0.0;
	private double rms=0.0;
	private int count=0;

	/**
	 * DefaultCurrentApplyModel constructor.
	 */
	public DefaultCurrentApplyModel() {
		//System.out.println("[DefaultCurrentApplyModel] created");
	}
	/**
	 * {@inheritDoc}
	 *
	 * Adds magnet and its current to arrays.This currents are used to calculate AVG value of currents on given magnets.
	 */
	@Override
	public void addCurrentValue(double current, AbstractCalibratedMagnet magnet) {
		magnets.add(magnet);
		avg+=current;
		rms+=current*current;
		count++;
	}

	/**
	 * {@inheritDoc}
	 *
	 * getCurrent returns average value of added currents. If RMS of currents
	 * differes from AVS more than DataBush precision, InconsistentDataException
	 * exception is thrown. Fields magnets and values are cleard.
	 */
	@Override
	public double getCurrent() throws InconsistentDataException, IllegalStateException {
		if (count==0) throw new IllegalStateException("Add some currents first!");
		double a= avg;
		if (count==1) {
			reset();
			return a;
		}
		avg=avg/count;
		a=avg;
		rms= rms/count;
		double std= Math.sqrt(Math.abs(rms-avg*avg));


		if (std/(avg==0 ? 1.0 : avg )*100.0>precision) {
			Iterator<AbstractCalibratedMagnet> eit= magnets.iterator();
			String s= "Applying currents differ more than precision for elements ";
			while (eit.hasNext()) s+= eit.next().getName()+" ";
			throw new InconsistentDataException(s);
		}
		reset();
		return a;
	}
	/**
	 * {@inheritDoc}
	 *
	 * Returnes the number of currents that contribute to AVG.
	 */
	@Override
	public int getValueCount() {
		return magnets.size();
	}
	/**
	 * {@inheritDoc}
	 *
	 * Cleares magnets and values.After reseting one could calculate new AVG.
	 */
	@Override
	public void reset() {
		magnets.clear();
		avg=0.0;
		rms=0.0;
		count=0;
	}

	/** {@inheritDoc} */
	@Override
	public void configure(Configuration conf) {
	}
}
