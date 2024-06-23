/**
 *
 */
package org.scictrl.mp.orbitcorrect;

/**
 * <p>IOrientationMarker interface.</p>
 *
 * @author igor@kriznar.com
 */
public interface IOrientationMarker {

	/**
	 * <p>getOrientation.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 */
	public Orientation getOrientation();

	/**
	 * <p>isHorizontal.</p>
	 *
	 * @return a boolean
	 */
	public boolean isHorizontal();
	/**
	 * <p>isVertical.</p>
	 *
	 * @return a boolean
	 */
	public boolean isVertical();

}
