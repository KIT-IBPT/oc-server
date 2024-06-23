package org.scictrl.mp.orbitcorrect.model.optics;



/**
 * This type extends <code>SimpleData</code> and represent machine function dispersion on <code>position</code>.
 *
 * @author igor@scictrl.com
 */
public class DispersionData extends SimpleData {
/**
 * <code>DispersionData</code> constructor.
 *
 * @param name a {@link java.lang.String} object
 */
public DispersionData(String name) {
	super(name);
}
/**
 * <code>DispersionData</code> constructor.
 *
 * @param name a {@link java.lang.String} object
 * @param source AbstractOpticalElement
 */
public DispersionData(String name, AbstractOpticalElement source) {
	super(name,source);
}
/**
 * <code>DispersionData</code> constructor.
 *
 * @param name a {@link java.lang.String} object
 * @param source AbstractOpticalElement
 * @param x double
 * @param z double
 */
public DispersionData(String name, AbstractOpticalElement source, double x, double z) {
	super(name,source,x,z);
}
/**
 * This method returns machine function dispersion.
 *
 * @return x double
 */
public double d() {
	return x;
}
/**
 * This method returns machine function dispersion prime.
 *
 * @return z double
 */
public double dp() {
	return z;
}
}
