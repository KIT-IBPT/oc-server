package org.scictrl.mp.orbitcorrect.correction.models;

import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.correction.Correction;
import org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
import org.scictrl.mp.orbitcorrect.utilities.Orbit;
import org.scictrl.mp.orbitcorrect.utilities.ResponseMatrix;
/**
 * <p>CorrectorStrengthReductionModel class.</p>
 *
 * @author igor@scictrl.com
 */
public class CorrectorStrengthReductionModel extends AbstractSVDBasedModel {
	/**
	 * CorrectorStrengthReductionModel constructor comment.
	 */
	public CorrectorStrengthReductionModel() {
		super();
		name= "Corrector Strength Reduction";
		setReductionScale(1.0);
		setReductionEnabled(true);
	}
	/** {@inheritDoc} */
	@Override
	public Correction calculateCorrection(OrbitCorrectionOperator engine)
			throws InconsistentDataException {

		Orbit o= engine.getCurrentOrbit();
		Correction correction= makeCorrection(engine.getCorrectors(getOrientation()),o, engine.getResponseMatrix(getOrientation()),engine.getDataBush(),engine);

		return correction;
	}
	/** {@inheritDoc} */
	@Override
	protected Correction makeCorrection(
			ElementList<AbstractCorrector> correctors, Orbit orbit,
			ResponseMatrix responseMatrix, DataBush db,
			OrbitCorrectionOperator op) throws InconsistentDataException {

	// test lists
		testCorrectors(correctors);
		testMonitors(orbit.getBPMs());

		if (!db.hasClosedOrbitSolution()) throw new org.scictrl.mp.orbitcorrect.InconsistentDataException("DataBush has no closed orbit solution!");

		int cs= correctors.size();

	// prepare correction

		double[] correction = new double[cs];
		for (int ii=0; ii< cs;ii++) {
			correction[ii]= 0.0;
		}

		addReduction(orbit.getBPMs(),correctors,responseMatrix,correction);

		return new Correction(correctors, null, correction, getOrientation(), op.getCorrectionScale(), reductionEigenvectorsUsed, reductionEigenvalues);
	}
}
