/**
 *
 */
package org.scictrl.mp.orbitcorrect.model.optics;

import java.util.Iterator;


/**
 * This class
 *
 * @author igor@scictrl.com
 * 
 * @param <T> elements type
 */
public class ValueList<T extends AbstractDataBushElement> implements Iterable<T> {

	private final ElementList<T> elements;
	private final double[] values;

	/**
	 * <p>Constructor for ValueList.</p>
	 *
	 * @param el a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @param values an array of {@link double} objects
	 */
	public ValueList(ElementList<T> el, double[] values) {
		if (values.length!=el.size()) {
			throw new IllegalArgumentException("Number of elements and values is not same!");
		}
		this.elements=el;
		this.values= values;
	}

	/**
	 * <p>size.</p>
	 *
	 * @return a int
	 */
	public int size() {
		return values.length;
	}

	/**
	 * <p>Getter for the field <code>values</code>.</p>
	 *
	 * @return an array of {@link double} objects
	 */
	public double[] getValues() {
		return values;
	}

	/**
	 * Returns new set, whcih has values multiplied by a scale factor.
	 *
	 * @return ne wwset with inverted values
	 * @param scaleFactor a double
	 */
	public ValueList<T> getScaledValues(double scaleFactor) {
		double[] scaledValues = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			scaledValues[i] = scaleFactor*values[i];
		}
		return new ValueList<>(elements,scaledValues);
	}

	/**
	 * Returns new set, whcih has values with negative sign. This is usefull for undoing
	 * set of value changes to correctors.
	 *
	 * @return ne wwset with inverted values
	 */
	public ValueList<T> invertValues() {
		double[] scaledValues = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			scaledValues[i] = -values[i];
		}
		return new ValueList<>(elements,scaledValues);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>iterator.</p>
	 */
	@Override
	public Iterator<T> iterator() {
		return elements.iterator();
	}

	/**
	 * Applys a <code>ValueList</code> of corrections to some <code>ValueList</code>
	 *
	 * @param corrections the <code>ValueList</code> of corrections to apply
	 * @return updated <code>ValueList</code>
	 */
	public ValueList<T> addValues(double[] corrections) {
		double[] val = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			val[i] = values[i]+corrections[i];
		}
		return new ValueList<>(elements,val);
	}

}
