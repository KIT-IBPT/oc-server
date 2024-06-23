package org.scictrl.mp.orbitcorrect.math;

import org.scictrl.mp.orbitcorrect.DBMath;
/**
 * <p>QuadrupoleMatrix class.</p>
 *
 * @author igor@scictrl.com
 */
public final class QuadrupoleMatrix extends TransferMatrix {
	private double k = 0.0;
/**
 * DriftMatrix constructor comment.
 *
 * @param newLength a double
 * @param newK a double
 */
public QuadrupoleMatrix(double newLength, double newK) {
	super(newLength);
	set(newLength, newK);
}
/** {@inheritDoc} */
@Override
public boolean canJoin(DoubleMatrix matrix) {
	if (matrix instanceof QuadrupoleMatrix)
		if (((QuadrupoleMatrix)matrix).getK()==k) return true;
	return false;
}
/**
 * <p>Getter for the field <code>k</code>.</p>
 *
 * @return double
 */
public double getK() {
	return k;
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
	length+=((QuadrupoleMatrix)matrix).getLength();
	DBMath.makeQuadrupole(k,length,this.matrix);
	return this;
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
			return new QuadrupoleMatrix(length+((DriftMatrix)matrix).getLength(),k);
		}
		return super.multiplay(matrix);
	}
/**
 * <p>set.</p>
 *
 * @param newK double
 * @param newLength a double
 */
public void set(double newLength, double newK) {
	k = newK;
	length= newLength;
	DBMath.makeQuadrupole(k,length,this.matrix);
}
/**
 * <p>Setter for the field <code>k</code>.</p>
 *
 * @param newK double
 */
public void setK(double newK) {
	k = newK;
	DBMath.makeQuadrupole(k,length,this.matrix);
}
/**
 * <p>setLength.</p>
 *
 * @param length double
 */
public void setLength(double length) {
	this.length= length;
	DBMath.makeQuadrupole(k,length,this.matrix);
}
}
