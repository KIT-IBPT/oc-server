package org.scictrl.mp.orbitcorrect.correction;

/**
 * Common OC model for methods involving SVD response matrix decomposition.
 *
 * @author igor@scictrl.com
 */
public interface ISVDOrbitCorrectionModel extends IOrbitCorrectionModel {

	/**
	 * <p>setMinimalEigenvalue.</p>
	 *
	 * @param newMinimalEigenvalue double
	 */
	void setMinimalEigenvalue(double newMinimalEigenvalue);

	/**
	 * <p>getEigenvalues.</p>
	 *
	 * @return an array of {@link double} objects
	 */
	double[] getEigenvalues();

	/**
	 * <p>getEigenvectorsUsed.</p>
	 *
	 * @return a int
	 */
	int getEigenvectorsUsed();

	/**
	 * <p>getMinimalEigenvalue.</p>
	 *
	 * @return a double
	 */
	double getMinimalEigenvalue();

}
