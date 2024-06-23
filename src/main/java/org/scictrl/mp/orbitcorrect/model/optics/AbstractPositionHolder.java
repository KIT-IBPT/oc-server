package org.scictrl.mp.orbitcorrect.model.optics;

/**
 * This is an abstract class that has only constructor method and (public) fields. His only task is to hold data.
 */
abstract class AbstractPositionHolder {

	public double position=0;
	public int index=-1;

	/**
	 * <code>AbstractPositionHolder</code> constructor. It initialize position to specified value.
	 *
	 * @param pos double
	 */
	public AbstractPositionHolder(double pos) {
		super();
		position= pos;
	}
}
