package org.scictrl.mp.orbitcorrect.correction.models;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.Orientation;
import org.scictrl.mp.orbitcorrect.accessories.Utilities;
import org.scictrl.mp.orbitcorrect.correction.Correction;
import org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
import org.scictrl.mp.orbitcorrect.utilities.Orbit;
import org.scictrl.mp.orbitcorrect.utilities.ResponseMatrix;
/**
 * <p>Abstract AbstractSVDOrbitCorrectionModel class.</p>
 *
 * @author igor@scictrl.com
 */
public abstract class AbstractSVDOrbitCorrectionModel extends AbstractSVDBasedModel{
	private double offset = 0.0;
	private boolean modelCalculatesOffset = false;
	private double beamAverage = 0.0;
	private boolean makeShift = false;

	/**
	 * RingKeeper constructor comment.
	 */
	public AbstractSVDOrbitCorrectionModel() {
		super();
	}

	/**
	 * <p>Getter for the field <code>beamAverage</code>.</p>
	 *
	 * @return double
	 */
	public double getBeamAverage() {
		return beamAverage;
	}
	/**
	 * <p>Getter for the field <code>offset</code>.</p>
	 *
	 * @return double
	 */
	public double getOffset() {
		return offset;
	}
	/**
	 * <p>isMakeShift.</p>
	 *
	 * @return boolean
	 */
	public boolean isMakeShift() {
		return makeShift;
	}
	/**
	 * <p>isModelCalculatesOffset.</p>
	 *
	 * @return boolean
	 */
	public boolean isModelCalculatesOffset() {
		return modelCalculatesOffset;
	}
	/** {@inheritDoc} */
	@Override
	protected Correction makeCorrection(ElementList<AbstractCorrector> correctors, Orbit orbit, ResponseMatrix responseMatrix, DataBush db, OrbitCorrectionOperator oc) throws InconsistentDataException {

		//testMonitors(orbit.getBPMs());
		//testCorrectors(correctors);

		if (!db.hasClosedOrbitSolution()) {
			throw new InconsistentDataException("DataBush has no closed orbit solution!");
		}

		if (!responseMatrix.isValid()) {
			throw new InconsistentDataException("ResponseMatrix is not valid!");
		}


		int ms = orbit.getBPMs().size();
		//int cs = correctors.size();
		double[] B = new double[ms];

	    /*
	     * Until better idea, we don't print anywhere.
	     */
		PrintWriter pw= new PrintWriter(new Writer() {

			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
			}

			@Override
			public void flush() throws IOException {
			}

			@Override
			public void close() throws IOException {
			}
		});

		beamAverage= 0.0;
		if (makeShift) {
			for (int i=0;i<ms;B[i++]=-offset);
		} else {
			beamAverage=orbit.getAvg(getOrientation());
			if (debug) {
				pw.println("AVERAGE "+beamAverage);
				pw.println("OFFSET "+offset);
				pw.println("HOR "+(beamAverage*2 - offset));
				pw.println("VER "+offset);
			}


			boolean hack= Boolean.getBoolean("oc.hack.hor");

			double off=0.0;
			if (modelCalculatesOffset) {
				//pw.println("AVERAGE "+a);
			} else {
				if (getOrientation()==Orientation.HORIZONTAL) {
					if (hack) {
						off= beamAverage*2 - offset;
						if (debug) pw.println("OFFSET WITH HACK "+off);
					} else {
						off= offset;
						if (debug) pw.println("OFFSET NO HACK "+off);
					}
				} else {
					off= offset;
					if (debug) pw.println("OFFSET "+off);
				}
			}

			double[] orb= orbit.getPositions(getOrientation());
			for (int j = 0; j < B.length; j++) {
				B[j] = off - orb[j];
			}

			if (debug) {
				Utilities.printMatrix(B,pw);
				pw.flush();
			}
		}

		double[] correction= new double[correctors.size()];

		makeSVDInversion(B,responseMatrix,correction,db);

		if (reductionEnabled) {
			addReduction(orbit.getBPMs(),correctors,responseMatrix, correction);
		}

		return new Correction(correctors, null, correction, getOrientation(), oc.getCorrectionScale(), eigenvectorsUsed, eigenvalues);

	}
	/**
	 * <p>Setter for the field <code>makeShift</code>.</p>
	 *
	 * @param newMakeShift boolean
	 */
	public void setMakeShift(boolean newMakeShift) {
		if (makeShift!=newMakeShift) {
			makeShift = newMakeShift;
		}
	}
	/**
	 * <p>Setter for the field <code>modelCalculatesOffset</code>.</p>
	 *
	 * @param newModelCalculatesOffset a boolean
	 */
	public void setModelCalculatesOffset(boolean newModelCalculatesOffset) {
		if (modelCalculatesOffset != newModelCalculatesOffset) {
			modelCalculatesOffset = newModelCalculatesOffset;
		}
	}
	/**
	 * <p>Setter for the field <code>offset</code>.</p>
	 *
	 * @param newShiftConstant double
	 */
	public void setOffset(double newShiftConstant) {
		if (offset != newShiftConstant) {
			offset = newShiftConstant;
		}
	}
}
