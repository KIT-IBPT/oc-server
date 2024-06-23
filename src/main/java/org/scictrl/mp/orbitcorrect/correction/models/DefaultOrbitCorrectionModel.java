package org.scictrl.mp.orbitcorrect.correction.models;

import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.correction.Correction;
import org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator;
/**
 * <p>DefaultOrbitCorrectionModel class.</p>
 *
 * @author igor@scictrl.com
 */
public class DefaultOrbitCorrectionModel extends AbstractSVDOrbitCorrectionModel{
	/**
	 * RingKeeper constructor comment.
	 */
	public DefaultOrbitCorrectionModel() {
		super();
		name = "Default OC";
	}
	/** {@inheritDoc} */
	@Override
	public Correction calculateCorrection(OrbitCorrectionOperator engine) throws InconsistentDataException {

		Correction correction= makeCorrection(engine.getCorrectors(getOrientation()), engine.getCurrentOrbit(), engine.getResponseMatrix(getOrientation()),engine.getDataBush(), engine);

		return correction;

	}
}
