package org.scictrl.mp.orbitcorrect;


/**
 * Indicates Horizontal or Vertical orientation of data.
 *
 * @author igor@scictrl.com
 */
public enum Orientation {

	/** Horizontal orientation. */
	HORIZONTAL,
	/** Vertical orientation. */
	VERTICAL;

	/** Constant <code>_HORIZONTAL=0</code> */
	public static final int _HORIZONTAL = 0;

	/** Constant <code>_VERTICAL=1</code> */
	public static final int _VERTICAL = 1;

	/** Constant <code>_H=_HORIZONTAL</code> */
	public static final int _H= _HORIZONTAL;

	/** Constant <code>_V=_VERTICAL</code> */
	public static final int _V= _VERTICAL;

	/** Constant <code>H</code> */
	public static final Orientation H= Orientation.HORIZONTAL;

	/** Constant <code>V</code> */
	public static final Orientation V= Orientation.VERTICAL;

	/**
	 * Same as ordinal.
	 *
	 * @return ordinal
	 * @deprecated use ordinal()
	 */
	@Deprecated
	public int value() {
		return ordinal();
	}

	/**
	 * <p>isHorizontal.</p>
	 *
	 * @return a boolean
	 */
	public boolean isHorizontal() {
		return this==HORIZONTAL;
	}

	/**
	 * <p>isVertical.</p>
	 *
	 * @return a boolean
	 */
	public boolean isVertical() {
		return this==VERTICAL;
	}

	/**
	 * <p>Short name, H or V.</p>
	 *
	 * @return a short name, H or V
	 */
	public String getShortName() {
		return isHorizontal() ? "H" : "V";
	}

}
