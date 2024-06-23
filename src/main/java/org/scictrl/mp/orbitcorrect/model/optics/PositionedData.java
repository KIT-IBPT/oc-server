package org.scictrl.mp.orbitcorrect.model.optics;



/**
 * This type extends <code>SimpleData</code> and represent any of machine functions (actually alpha or beta), on <code>position</code>.
 *
 * @author igor@scictrl.com
 */
public class PositionedData extends SimpleData {
	/**
	 * <code>PositionedData</code> constructor.
	 *
	 * @param name a {@link java.lang.String} object
	 */
	public PositionedData(String name) {
		super(name);
	}
	/**
	 * <code>PositionedData</code> constructor.
	 *
	 * @param name a {@link java.lang.String} object
	 * @param source AbstractOpticalElement
	 */
	public PositionedData(String name, AbstractOpticalElement source) {
		super(name,source);
	}
	/**
	 * <code>PositionedData</code> constructor.
	 *
	 * @param name a {@link java.lang.String} object
	 * @param source AbstractOpticalElement
	 * @param x double
	 * @param z double
	 */
	public PositionedData(String name, AbstractOpticalElement source, double x, double z) {
		super(name,source,x,z);
	}
	/**
	 * This method returns machine function, parallel to orbit plane and perpendicular to beam direction.
	 *
	 * @return x double
	 */
	public double x() {
		return x;
	}
	/**
	 * This method returns machine function, perpendicular to orbit plane and to beam direction.
	 *
	 * @return z double
	 */
	public double z() {
		return z;
	}

	/**
	 * <p>set.</p>
	 *
	 * @param d an array of {@link java.lang.Double} objects
	 */
	public void set(Double[] d) {
		set(d[0],d[1]);
	}
}
