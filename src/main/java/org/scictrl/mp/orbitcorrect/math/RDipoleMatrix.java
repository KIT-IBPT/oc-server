package org.scictrl.mp.orbitcorrect.math;

import org.scictrl.mp.orbitcorrect.DBMath;
/**
 * <p>RDipoleMatrix class.</p>
 *
 * @author igor@scictrl.com
 */
public final class RDipoleMatrix extends DipoleMatrix {
	private double lAngle;
	private double rAngle;
/**
 * SDipoleMatrix constructor comment.
 *
 * @param newLength double
 * @param newK double
 * @param newRadius double
 * @param newLAngle a double
 * @param newRAngle a double
 */
public RDipoleMatrix(double newLength, double newK, double newRadius, double newLAngle, double newRAngle) {
	super(newLength, newK, newRadius);
	lAngle= newLAngle;
	rAngle= newRAngle;
	makeMatrix();
}
/** {@inheritDoc} */
@Override
public boolean canJoin(DoubleMatrix matrix) {
	if (matrix instanceof RDipoleMatrix)
		if ((((RDipoleMatrix)matrix).getK()==k)&&(((RDipoleMatrix)matrix).getRadius()==radius)&&(((RDipoleMatrix)matrix).rAngle==lAngle)) return true;
	return false;
}
/**
 * <p>getEdgeAngle.</p>
 *
 * @return double
 */
public double getEdgeAngle() {
	return length/2.0/radius;
}
/**
 * <p>Getter for the field <code>lAngle</code>.</p>
 *
 * @return double
 */
public double getLAngle() {
	return lAngle;
}
/**
 * <p>Getter for the field <code>rAngle</code>.</p>
 *
 * @return double
 */
public double getRAngle() {
	return rAngle;
}
/**
 * {@inheritDoc}
 *
 * Multiplies <code>matrix</code> from right with this matrix and
 * stores result in this matrix. Return Matrix is stih matrix.
 */
@Override
public DoubleMatrix join(DoubleMatrix matrix) {
	if (!canJoin(matrix)) throw new IllegalDimensionException("Can not join, matrix does not match!");
	length+=((RDipoleMatrix)matrix).getLength();
	rAngle=((RDipoleMatrix)matrix).rAngle;
	makeMatrix();
	return this;
}
/** {@inheritDoc} */
@Override
protected double[][] makeMatrix() {
	return makeMatrix(lAngle,length,rAngle,matrix);
}
/**
 * <p>makeMatrix.</p>
 *
 * @param lAngle a double
 * @param length a double
 * @param rAngle a double
 * @param matrix an array of {@link double} objects
 * @return an array of {@link double} objects
 */
protected double[][] makeMatrix(double lAngle, double length, double rAngle, double[][] matrix) {
	DBMath.makeDipole(radius,k,length,matrix);
	double a1= Math.tan(rAngle)/radius;
	double a2= Math.tan(lAngle)/radius;

	matrix[0][0]+=a1*matrix[0][1];
	matrix[1][0]+=a2*matrix[0][0]+a1*matrix[1][1];
	matrix[1][1]+=a2*matrix[0][1];

	matrix[2][2]-=a1*matrix[2][3];
	matrix[3][2]-=(a2*matrix[2][2]+a1*matrix[3][3]);
	matrix[3][3]-=a2*matrix[2][3];

	return matrix;
}
	/**
	 * {@inheritDoc}
	 *
	 * Multiplies <code>matrix</code> from right with this matrix and
	 * returns result as new matrix
	 */
	@Override
	public TransferMatrix multiplay(TransferMatrix matrix) {
		if (!canMulitplay(matrix)) throw new IllegalDimensionException("Can not mulitply, matrix dimension does not match!");
		if (canJoin(matrix)) {
			return new RDipoleMatrix(length+((RDipoleMatrix)matrix).getLength(),k,radius,lAngle,((RDipoleMatrix)matrix).rAngle);
		}
		return super.multiplay(matrix);
	}
/**
 * <p>set.</p>
 *
 * @param newK double
 * @param newLength a double
 * @param newRadius a double
 * @param lAngle a double
 * @param rAngle a double
 */
public void set(double newLength, double newK, double newRadius, double lAngle, double rAngle) {
	this.rAngle= rAngle;
	this.lAngle= lAngle;
	super.set(newLength,newK,newRadius);
}
/**
 * <p>Setter for the field <code>lAngle</code>.</p>
 *
 * @param newLAngle double
 */
public void setLAngle(double newLAngle) {
	lAngle = newLAngle;
	makeMatrix();
}
/**
 * <p>Setter for the field <code>rAngle</code>.</p>
 *
 * @param newRAngle double
 */
public void setRAngle(double newRAngle) {
	rAngle = newRAngle;
	makeMatrix();
}
/**
 * {@inheritDoc}
 *
 * Returns a String that represents the value of this object.
 */
@Override
public String toString() {
	// Insert code to print the receiver here.
	// This implementation forwards the message to super. You may replace or supplement this.
	return super.toString();
}
}
