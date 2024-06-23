package org.scictrl.mp.orbitcorrect;

/**
 * Basic interface for elements.
 *
 * @author igor@scictrl.com
 */
public interface ISimpleElement extends Cloneable {
	/**
	 * Returns the clone of this element.
	 *
	 * @return a {@link java.lang.Object} the clone of this element.
	 */
	Object clone();
	/**
	 * Returns the unique name of this element. Each element used in DataBush must have unique name.
	 * Two element instances with same name are threated as same instance.
	 *
	 * @return a {@link java.lang.String} the unique name of this element.
	 */
	String getName();
	/**
	 * {@inheritDoc}
	 *
	 * Returns the string representation of this element.
	 */
	@Override
	String toString();
}
