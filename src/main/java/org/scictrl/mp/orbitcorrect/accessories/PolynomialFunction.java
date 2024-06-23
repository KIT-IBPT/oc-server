package org.scictrl.mp.orbitcorrect.accessories;


/**
 * Class PolynomialFunction represents polynomial function.It gives methods to create polynomial function and to calculate
 * its values.
 *
 * @author igor@scictrl.com
 */
public class PolynomialFunction implements Cloneable {
	/**
	* Coefitients of polynomial function X.
	*/
	public double[] coefX;
	/**
	* Coefitients of polynomial function Y.
	*/
	public double[] coefY;
/**
 * PolynomialFunction default constructor that sets default values to coefitients polynomial functions X and Y.
 */
public PolynomialFunction() {
	super();
	coefX= new double[2];
	coefY= new double[2];

	// default intentity transformation
	coefX[0]=coefY[0]=0.0;
	coefX[1]=coefY[1]=1.0;
}
/**
 * PolynomialFunction constructor that sets two input vectors of doubles to coefitients of polynomial functions X and Y.
 * They must be same size.
 *
 * @param x an array of {@link double} objects
 * @param y an array of {@link double} objects
 * @throws java.lang.IllegalArgumentException if any.
 */
public PolynomialFunction(double[] x, double[] y) throws IllegalArgumentException {
	super();
	if (x.length!=y.length) throw new IllegalArgumentException();
	coefX= x;
	coefY= y;
}
/**
 * {@inheritDoc}
 *
 * Clones PolynomialFunction and returns it as Object.
 */
@Override
public Object clone() {
	PolynomialFunction o=null;
	try {
		o= (PolynomialFunction)super.clone();
	} catch (CloneNotSupportedException e) {e.printStackTrace();}
	o.coefX= coefX;
	o.coefY= coefY;
	return o;
}
/**
 * {@inheritDoc}
 *
 * Returns string of name and coefitients of poynomial functions X and Y.
 */
@Override
public String toString() {
	String s= this.getClass().getName()+"X[";
	for (double element : coefX)
		s+=" "+element;
	s+=" ], Y[";
	for (double element : coefY)
		s+=" "+element;
	s+=" ]";
	return s;
}
/**
 * Returns the value of polynomial function Y at point y.
 *
 * @param y a double argument of polynomial function Y
 * @return a double
 */
public double X(double y) {
	double d= 0;
	for (int i=0; i<coefY.length; i++) d+= coefY[i]*Math.pow(y,i);
	return d;
}
/**
 * Returns the value of polynomial function X at point x.
 *
 * @param x a double argument of polynomial function Y
 * @return a double
 */
public double Y(double x) {
	double d= 0;
	for (int i=0; i<coefX.length; i++) d+= coefX[i]*Math.pow(x,i);
	return d;
}
}
