package org.scictrl.mp.orbitcorrect.math;

import org.scictrl.mp.orbitcorrect.DBMath;
/**
 * <p>DriftMatrix class.</p>
 *
 * @author igor@scictrl.com
 */
public final class DriftMatrix extends TransferMatrix {
/**
 * DriftMatrix constructor comment.
 *
 * @param length double
 */
public DriftMatrix(double length) {
	super(length);
	org.scictrl.mp.orbitcorrect.DBMath.makeDrift(length,matrix);
}
/** {@inheritDoc} */
@Override
public boolean canJoin(DoubleMatrix matrix) {
	return matrix instanceof DriftMatrix;
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
	length+=((DriftMatrix)matrix).getLength();
	DBMath.makeDrift(length,this.matrix);
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
	if (matrix instanceof DriftMatrix) {
		return new DriftMatrix(length+((DriftMatrix)matrix).getLength());
	}
	return super.multiplay(matrix);
}
/**
 * <p>setLength.</p>
 *
 * @param length double
 */
public void setLength(double length) {
	this.length= length;
	DBMath.makeDrift(length,this.matrix);
}
/** {@inheritDoc} */
@Override
public BetaMatrix transport(BetaMatrix beta) {
	return transport(beta,length);
}
/**
 * <p>transport.</p>
 *
 * @return BetaMatrix
 * @param beta BetaMatrix
 * @param length a double
 */
public BetaMatrix transport(BetaMatrix beta, double length) {
	double gx= beta.getGamaX();
	double gz= beta.getGamaZ();
	beta.set(
		beta.getBetaX()-2.0*length*beta.getAlphaX()+length*length*gx,
		beta.getBetaZ()-2.0*length*beta.getAlphaZ()+length*length*gz,
		beta.getAlphaX()-length*gx,
		beta.getAlphaZ()-length*gz
		);
	return beta;
}
}
