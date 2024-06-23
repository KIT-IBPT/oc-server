package org.scictrl.mp.orbitcorrect.correction;

import org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator.State;


/**
 * <p>IOrbitCorrectionListener interface.</p>
 *
 * @author igor@scictrl.com
 */
public interface IOrbitCorrectionListener {
	/**
	 * <p>stateChange.</p>
	 *
	 * @param state a {@link org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator.State} object
	 * @param message a {@link java.lang.String} object
	 */
	public void stateChange(State state, String message);
	/**
	 * <p>progressReport.</p>
	 *
	 * @param progress a double
	 * @param message a {@link java.lang.String} object
	 */
	public void progressReport(double progress, String message);
	/**
	 * <p>newCorrection.</p>
	 *
	 * @param correction a {@link org.scictrl.mp.orbitcorrect.correction.Correction} object
	 */
	public void newCorrection(Correction correction);
}
