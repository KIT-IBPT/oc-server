package org.scictrl.mp.orbitcorrect.math;

import org.scictrl.mp.orbitcorrect.DBMath;
/**
 * <p>DoubleVector class.</p>
 *
 * @author igor@scictrl.com
 */
public class DoubleVector extends DoubleMatrix {
/**
 * DoubleVector constructor comment.
 *
 * @param dimension int
 */
public DoubleVector(int dimension) {
	super(dimension,1);
}
/**
 * {@inheritDoc}
 *
 * Test if can be joined with this matrix.
 */
@Override
public boolean canJoin(DoubleMatrix matrix) {
	return false;
}
/**
 * Returns value from matrix at (row,column) position.
 *
 * @return double value
 * @param r int row
 */
public double get(int r) {
	if (r<0 || r>rows) throw new IllegalDimensionException("No such row");
	return matrix[r][0];
}
/**
 * Multiplies <code>matrix</code> from right with this matrix and
 * returns result as new matrix
 *
 * @return DoubleMatrix result=<code>matrix</code> times this matrix
 * @param matrix DoubleMatrix
 */
public DoubleVector multiplayVector(DoubleMatrix matrix) {
	if (!canMulitplay(matrix)) throw new IllegalDimensionException("Can not mulitply, matrix dimension does not match!");
	DoubleVector m= new DoubleVector(matrix.rows());
	DBMath.multiply(matrix.matrix,this.matrix,m.matrix);
	return m;
}
/**
 * Returns value from matrix at (row,column) position.
 *
 * @param r int row
 * @param value a double
 */
public void set(int r, double value) {
	matrix[r][0]=value;
}
}
