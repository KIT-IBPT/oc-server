/**
 *
 */
package org.scictrl.mp.orbitcorrect.mvc;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds references to data entities and value collections.
 * Preferred way to exchange data from controller to view is trough the model.
 * View must listen to model and process visually the data.
 *
 * @author igor@scictrl.com
 */
public abstract class ApplicationModel {

	/**
	 * Define constants in form
	 * public static final PROPERTY_NAME = "propertyName";
	 * where "propertyName" corresponds to property field name on this class.
	 * Of course getter ant getter are expected and setter must call {@link #handlePropertySet(String, Object)}
	 * method.
	 */

	/**
	 * Define fields corresponding to defines propery names.
	 */

	/*
	 * Internal property change support
	 */
	private PropertyChangeSupport pcSupport= new PropertyChangeSupport(this);
	private Set<String> set=new HashSet<>();

	/**
	 * <p>Constructor for ApplicationModel.</p>
	 */
	public ApplicationModel(){}


	/**
	 * <p>addPropertyChangeListener.</p>
	 *
	 * @param n a {@link java.lang.String} object
	 * @param l a {@link java.beans.PropertyChangeListener} object
	 */
	public void addPropertyChangeListener(String n, PropertyChangeListener l) {
		if (n==null) {
			pcSupport.addPropertyChangeListener(l);
		}
		pcSupport.addPropertyChangeListener(n, l);
	}

	/**
	 * <p>addPropertyChangeListener.</p>
	 *
	 * @param l a {@link java.beans.PropertyChangeListener} object
	 */
	public void addPropertyChangeListener(PropertyChangeListener l) {
		pcSupport.addPropertyChangeListener(l);
	}

	/**
	 * <p>handlePropertySet.</p>
	 *
	 * @param property a {@link java.lang.String} object
	 * @param newValue a {@link java.lang.Object} object
	 */
	protected void handlePropertySet(String property, Object newValue) {
		try {
/*			System.out.println(getClass().getCanonicalName());
			Field[] ff= getClass().getFields();
			System.out.println(Arrays.toString(ff));
			ff= getClass().getDeclaredFields();
			System.out.println(Arrays.toString(ff));
*/
			Field f= getClass().getDeclaredField(property);
			f.setAccessible(true);
			boolean defined= set.contains(property);
			if (defined) {
				Object oldValue= f.get(this);
				if (oldValue==newValue) {
					return;
				}
				f.set(this, newValue);
				pcSupport.firePropertyChange(property, oldValue, newValue);
			} else {
				f.set(this, newValue);
				set.add(property);
				pcSupport.firePropertyChange(property, null, newValue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>fireChange.</p>
	 *
	 * @param property a {@link java.lang.String} object
	 */
	public void fireChange(String property) {
		try {
			Field f= getClass().getDeclaredField(property);
			f.setAccessible(true);
			boolean defined= set.contains(property);
			if (defined) {
				Object oldValue= f.get(this);
				pcSupport.firePropertyChange(property, null, oldValue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
