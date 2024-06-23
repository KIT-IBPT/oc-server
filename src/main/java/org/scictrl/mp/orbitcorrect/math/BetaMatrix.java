package org.scictrl.mp.orbitcorrect.math;

import org.scictrl.mp.orbitcorrect.DBMath;
/**
 * <p>BetaMatrix class.</p>
 *
 * @author igor@scictrl.com
 */
public final class BetaMatrix extends TransferMatrix {
	private double betaX=0.0;
	private double betaZ = 0.0;
	private double alphaX = 0.0;
	private double alphaZ = 0.0;
/**
 * DriftMatrix constructor comment.
 *
 * @param betaX a double
 * @param betaZ a double
 * @param alphaX a double
 * @param alphaZ a double
 */
public BetaMatrix(double betaX, double betaZ, double alphaX, double alphaZ) {
	super(0.0);
	set(betaX,betaZ,alphaX,alphaZ);
}
/** {@inheritDoc} */
@Override
public boolean canJoin(DoubleMatrix matrix) {
	return false;
}
/**
 * <p>Getter for the field <code>alphaX</code>.</p>
 *
 * @return double
 */
public double getAlphaX() {
	return alphaX;
}
/**
 * <p>Getter for the field <code>alphaZ</code>.</p>
 *
 * @return double
 */
public double getAlphaZ() {
	return alphaZ;
}
/**
 * <p>Getter for the field <code>betaX</code>.</p>
 *
 * @return double
 */
public double getBetaX() {
	return betaX;
}
/**
 * <p>Getter for the field <code>betaZ</code>.</p>
 *
 * @return double
 */
public double getBetaZ() {
	return betaZ;
}
/**
 * <p>getGamaX.</p>
 *
 * @return double
 */
public double getGamaX() {
	return (1.0+alphaX*alphaX)/betaX;
}
/**
 * <p>getGamaZ.</p>
 *
 * @return double
 */
public double getGamaZ() {
	return (1.0+alphaZ*alphaZ)/betaZ;
}
/**
 * {@inheritDoc}
 *
 * Multiplies <code>matrix</code> from right with this matrix and
 * stores result in this matrix. Return Matrix is stih matrix.
 */
@Override
public DoubleMatrix join(DoubleMatrix matrix) {
	if (!canJoin(matrix)) throw new IllegalDimensionException("Can not join, matrix dimension does not match!");
	return this;
}
/**
 * DriftMatrix constructor comment.
 *
 * @param betaX a double
 * @param betaZ a double
 * @param alphaX a double
 * @param alphaZ a double
 */
public void set(double betaX, double betaZ, double alphaX, double alphaZ) {
	this.betaX=betaX;
	this.alphaX=alphaX;
	this.betaZ= betaZ;
	this.alphaZ=alphaZ;
	DBMath.makeBetaMatrix(betaX,betaZ,alphaX,alphaZ,this.matrix);
}
/**
 * <p>Setter for the field <code>alphaX</code>.</p>
 *
 * @param newAlphaX double
 */
public void setAlphaX(double newAlphaX) {
	alphaX = newAlphaX;
	DBMath.makeBetaMatrix(betaX,betaZ,alphaX,alphaZ,this.matrix);
}
/**
 * <p>Setter for the field <code>alphaZ</code>.</p>
 *
 * @param newAlphaZ double
 */
public void setAlphaZ(double newAlphaZ) {
	alphaZ = newAlphaZ;
	DBMath.makeBetaMatrix(betaX,betaZ,alphaX,alphaZ,this.matrix);
}
/**
 * <p>Setter for the field <code>betaX</code>.</p>
 *
 * @param newBetaX double
 */
public void setBetaX(double newBetaX) {
	betaX = newBetaX;
	DBMath.makeBetaMatrix(betaX,betaZ,alphaX,alphaZ,this.matrix);
}
/**
 * <p>Setter for the field <code>betaZ</code>.</p>
 *
 * @param newBetaZ double
 */
public void setBetaZ(double newBetaZ) {
	betaZ = newBetaZ;
	DBMath.makeBetaMatrix(betaX,betaZ,alphaX,alphaZ,this.matrix);
}
}
