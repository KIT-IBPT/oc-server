package org.scictrl.mp.orbitcorrect.math;


/**
 * <p>DispersionVector class.</p>
 *
 * @author igor@scictrl.com
 */
public class DispersionVector extends DoubleMatrix {
/**
 * DispersionVector constructor comment.
 *
 * @param d a double
 * @param dp a double
 */
public DispersionVector(double d, double dp) {
	super(5,1);
	matrix[4][0]=1.0;
	set(d,dp);
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
 * <p>getDispersion.</p>
 *
 * @return double
 */
public double getDispersion() {
	return matrix[0][0];
}
/**
 * <p>getDispersionPrime.</p>
 *
 * @return double
 */
public double getDispersionPrime() {
	return matrix[1][0];
}
/**
 * <p>set.</p>
 *
 * @param newDispersion double
 * @param newDispersionPrime a double
 */
public void set(double newDispersion, double newDispersionPrime) {
	matrix[0][0] = newDispersion;
	matrix[1][0]= newDispersionPrime;
}
/**
 * <p>setDispersion.</p>
 *
 * @param newDispersion double
 */
public void setDispersion(double newDispersion) {
	matrix[0][0] = newDispersion;
}
/**
 * <p>setDispersionPrime.</p>
 *
 * @param newDispersionPrime double
 */
public void setDispersionPrime(double newDispersionPrime) {
	matrix[1][0] = newDispersionPrime;
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
