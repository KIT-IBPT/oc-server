package org.scictrl.mp.orbitcorrect;


/**
 * <p>ICurrentApplyModel interface.</p>
 *
 * @author igor@scictrl.com
 */
public interface ICurrentApplyModel extends IConfigurable {

	/**
	 * <p>addCurrentValue.</p>
	 *
	 * @param current a double
	 * @param magnet a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractCalibratedMagnet} object
	 */
	void addCurrentValue(double current, org.scictrl.mp.orbitcorrect.model.optics.AbstractCalibratedMagnet magnet);

	/**
	 * <p>getCurrent.</p>
	 *
	 * @return a double
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 * @throws java.lang.IllegalStateException if any.
	 */
	double getCurrent() throws org.scictrl.mp.orbitcorrect.InconsistentDataException, IllegalStateException;

	/**
	 * <p>getValueCount.</p>
	 *
	 * @return a int
	 */
	int getValueCount();

	/**
	 * <p>reset.</p>
	 */
	void reset();
}
