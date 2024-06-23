package org.scictrl.mp.orbitcorrect.correction;

import org.scictrl.mp.orbitcorrect.IConfigurable;
import org.scictrl.mp.orbitcorrect.IOrientationMarker;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.Orientation;
/**
 * General orbit correction model that calculates correction.
 *
 * @author igor@scictrl.com
 */
public interface IOrbitCorrectionModel extends IOrientationMarker, IConfigurable {
	/**
	 * <p>calculateCorrection.</p>
	 *
	 * @return double[]
	 * @param engine a {@link org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator} object
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 */
	Correction calculateCorrection(OrbitCorrectionOperator engine) throws InconsistentDataException;
	/**
	 * <p>getName.</p>
	 *
	 * @return java.lang.String
	 */
	String getName();

	/**
	 * Initializes model for selected orientation. Called by application after instantiated.
	 *
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 */
	void initialize(Orientation ori);

}
