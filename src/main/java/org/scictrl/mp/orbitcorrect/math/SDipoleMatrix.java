package org.scictrl.mp.orbitcorrect.math;

import org.scictrl.mp.orbitcorrect.DBMath;
/**
 * <p>SDipoleMatrix class.</p>
 *
 * @author igor@scictrl.com
 */
public final class SDipoleMatrix extends DipoleMatrix {
/**
 * SDipoleMatrix constructor comment.
 *
 * @param newLength double
 * @param newK double
 * @param newRadius double
 */
public SDipoleMatrix(double newLength, double newK, double newRadius) {
	super(newLength, newK, newRadius);
	makeMatrix();
}
/** {@inheritDoc} */
@Override
public boolean canJoin(DoubleMatrix matrix) {
	if (matrix instanceof SDipoleMatrix)
		if ((((SDipoleMatrix)matrix).getK()==k)&&(((SDipoleMatrix)matrix).getRadius()==radius)) return true;
	return false;
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
	length+=((SDipoleMatrix)matrix).getLength();
	DBMath.makeDipole(radius,k,length,this.matrix);
	return this;
}
/** {@inheritDoc} */
@Override
protected double[][] makeMatrix() {
	return DBMath.makeDipole(radius,k,length,this.matrix);
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
		return new SDipoleMatrix(length+((DriftMatrix)matrix).getLength(),k,radius);
	}
	return super.multiplay(matrix);
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
