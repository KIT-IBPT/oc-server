package org.scictrl.mp.orbitcorrect.math;


/**
 * <p>Abstract DipoleMatrix class.</p>
 *
 * @author igor@scictrl.com
 */
public abstract class DipoleMatrix extends TransferMatrix {
	/** Quadrupole coefficient */
	protected double k = 0.0;
	/** Bending radius */
	protected double radius = 0.0;

/**
 * DriftMatrix constructor comment.
 *
 * @param newLength a double
 * @param newK a double
 * @param newRadius a double
 */
public DipoleMatrix(double newLength, double newK, double newRadius) {
	super(newLength);
	k = newK;
	radius= newRadius;
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
 * <p>Getter for the field <code>radius</code>.</p>
 *
 * @return double
 */
public double getRadius() {
	return radius;
}
/**
 * <p>makeMatrix.</p>
 *
 * @return an array of {@link double} objects
 */
protected abstract double[][] makeMatrix();
/**
 * <p>set.</p>
 *
 * @param newK double
 * @param newLength a double
 * @param newRadius a double
 */
public void set(double newLength, double newK, double newRadius) {
	k = newK;
	length= newLength;
	radius= newRadius;
	makeMatrix();
}
/**
 * <p>Setter for the field <code>k</code>.</p>
 *
 * @param newK double
 */
public void setK(double newK) {
	k = newK;
	makeMatrix();
}
/**
 * <p>setLength.</p>
 *
 * @param length double
 */
public void setLength(double length) {
	this.length= length;
	makeMatrix();
}
/**
 * <p>Setter for the field <code>radius</code>.</p>
 *
 * @param newRadius double
 */
public void setRadius(double newRadius) {
	radius = newRadius;
	makeMatrix();
}
/** {@inheritDoc} */
@Override
public BetaMatrix transport(BetaMatrix beta) {
	return transport(beta,matrix);
}
}
