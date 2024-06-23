package org.scictrl.mp.orbitcorrect.correction.models;

import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.correction.Correction;
import org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
import org.scictrl.mp.orbitcorrect.utilities.Orbit;
import org.scictrl.mp.orbitcorrect.utilities.ResponseMatrix;
/**
 * <p>ResponseMatrixBumpModel class.</p>
 *
 * @author igor@scictrl.com
 */
public class ResponseMatrixBumpModel extends AbstractSVDBasedModel {
	private double bumpHeight = 0.0;
	private BPMonitor bumpedElement = null;
	private boolean useExternalResponseMatrix = false;
	private ResponseMatrix externalResponseMatrix = null;
	private int bumpedCount = 1;
	/**
	 * RingKeeper constructor comment.
	 */
	public ResponseMatrixBumpModel() {
		super();
		name= "Response Matrix Bump";
	}

	/**
	 * <p>Getter for the field <code>bumpedCount</code>.</p>
	 *
	 * @return int
	 */
	public int getBumpedCount() {
		return bumpedCount;
	}
	/**
	 * <p>Getter for the field <code>bumpedElement</code>.</p>
	 *
	 * @return BPMonitor
	 */
	public org.scictrl.mp.orbitcorrect.model.optics.BPMonitor getBumpedElement() {
		return bumpedElement;
	}
	/**
	 * <p>Getter for the field <code>bumpHeight</code>.</p>
	 *
	 * @return double
	 */
	public double getBumpHeight() {
		return bumpHeight;
	}
	/**
	 * <p>Getter for the field <code>externalResponseMatrix</code>.</p>
	 *
	 * @return ResponseMatrix
	 */
	public ResponseMatrix getExternalResponseMatrix() {
		return externalResponseMatrix;
	}
	/**
	 * <p>isUseExternalResponseMatrix.</p>
	 *
	 * @return boolean
	 */
	public boolean isUseExternalResponseMatrix() {
		return useExternalResponseMatrix;
	}

	/** {@inheritDoc} */
	@Override
	public Correction calculateCorrection(OrbitCorrectionOperator engine)
			throws InconsistentDataException {

		ResponseMatrix rm= (useExternalResponseMatrix) ? externalResponseMatrix : engine.getResponseMatrix(getOrientation());

		return makeCorrection(engine.getCorrectors(getOrientation()), engine.getCurrentOrbit(), rm, engine.getDataBush(),engine);
	}

	/** {@inheritDoc} */
	@Override
	protected Correction makeCorrection(
			ElementList<AbstractCorrector> correctors, Orbit orbit,
			ResponseMatrix responseMatrix, DataBush db,
			OrbitCorrectionOperator op) throws InconsistentDataException {


		testMonitors(orbit.getBPMs());
		testCorrectors(correctors);

		if (!orbit.getBPMs().contains(bumpedElement)) throw new InconsistentDataException("Selected BPMs should contain bumped element!");
		if (!db.hasClosedOrbitSolution()) throw new InconsistentDataException("DataBush has no closed orbit solution!");

		int ms = orbit.getBPMs().size();
		int cs = correctors.size();
		int i=0;

		if (useExternalResponseMatrix) {
			String[] bpms= new String[ms];
			String[] cors= new String[cs];
			for (i=0; i<ms; i++) bpms[i]= orbit.getBPMs().get(i).getName();
			for (i=0; i<cs; i++) cors[i]= correctors.get(i).getName();

			try {
				responseMatrix= responseMatrix.submatrix(bpms,cors);
			} catch (IllegalArgumentException e) {
				throw new InconsistentDataException(e.getMessage());
			}
		}


		double[] B = new double[ms];

		if (bumpedCount>ms) bumpedCount=ms;

		i=orbit.getBPMs().indexOf(bumpedElement);
		if (i+bumpedCount<ms) {
			for (int j=i; j<i+bumpedCount; j++) B[j]= bumpHeight;
		} else {
			for (int j=i; j<ms; j++) B[j]= bumpHeight;
			for (int j=0; j<i+bumpedCount-ms; j++) B[j]= bumpHeight;
		}

		double[] c= new double[correctors.size()];
		makeSVDInversion(B,responseMatrix,c,db);

		return new Correction(correctors, null, c, getOrientation(), op.getCorrectionScale(),eigenvectorsUsed,eigenvalues);
	}
	/**
	 * <p>Setter for the field <code>bumpedCount</code>.</p>
	 *
	 * @param newBumpedCount int
	 */
	public void setBumpedCount(int newBumpedCount) {
		if (bumpedCount != newBumpedCount) {
			bumpedCount = newBumpedCount;
		}
	}
	/**
	 * <p>Setter for the field <code>bumpedElement</code>.</p>
	 *
	 * @param newBumpedElement BPMonitor
	 */
	public void setBumpedElement(org.scictrl.mp.orbitcorrect.model.optics.BPMonitor newBumpedElement) {
		if (bumpedElement != newBumpedElement) {
			bumpedElement = newBumpedElement;
		}
	}
	/**
	 * <p>Setter for the field <code>bumpHeight</code>.</p>
	 *
	 * @param newBumpHeight double
	 */
	public void setBumpHeight(double newBumpHeight) {
		if (bumpHeight!=newBumpHeight) {
			bumpHeight = newBumpHeight;
		}
	}
	/**
	 * <p>Setter for the field <code>externalResponseMatrix</code>.</p>
	 *
	 * @param newExternalResponseMatrix ResponseMatrix
	 */
	public void setExternalResponseMatrix(ResponseMatrix newExternalResponseMatrix) {
		externalResponseMatrix = newExternalResponseMatrix;
	}
	/**
	 * <p>Setter for the field <code>useExternalResponseMatrix</code>.</p>
	 *
	 * @param newUseExternalResponseMatrix boolean
	 */
	public void setUseExternalResponseMatrix(boolean newUseExternalResponseMatrix) {
		if (useExternalResponseMatrix != newUseExternalResponseMatrix) {
			useExternalResponseMatrix = newUseExternalResponseMatrix;
		}
	}

}
