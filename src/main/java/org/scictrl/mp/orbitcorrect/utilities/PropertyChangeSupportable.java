package org.scictrl.mp.orbitcorrect.utilities;

import java.beans.PropertyChangeSupport;

/**
 * Help handling property change listeners.
 *
 * @author igor@scictrl.com
 */
public class PropertyChangeSupportable {

	/** Property change listener support. */
	protected PropertyChangeSupport propertyListeners = new java.beans.PropertyChangeSupport(this);

	/**
	 * BeamAnalizatorBean constructor comment.
	 */
	public PropertyChangeSupportable() {
		super();
	}
	/**
	 * <p>addPropertyChangeListener.</p>
	 *
	 * @param listener a {@link java.beans.PropertyChangeListener} object
	 */
	public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
		propertyListeners.addPropertyChangeListener(listener);
	}
	/**
	 * <p>addPropertyChangeListener.</p>
	 *
	 * @param propertyName a {@link java.lang.String} object
	 * @param listener a {@link java.beans.PropertyChangeListener} object
	 */
	public void addPropertyChangeListener(String propertyName, java.beans.PropertyChangeListener listener) {
		propertyListeners.addPropertyChangeListener(propertyName,listener);
	}
	/**
	 * <p>firePropertyChange.</p>
	 *
	 * @param event java.beans.PropertyChangeEvent
	 */
	protected void firePropertyChange(java.beans.PropertyChangeEvent event) {
		propertyListeners.firePropertyChange(event);
	}
	/**
	 * <p>firePropertyChange.</p>
	 *
	 * @param propertyName a {@link java.lang.String} object
	 */
	protected void firePropertyChange(String propertyName) {
		propertyListeners.firePropertyChange(propertyName, 0, 1);
	}
	/**
	 * <p>removePropertyChangeListener.</p>
	 *
	 * @param listener a {@link java.beans.PropertyChangeListener} object
	 */
	public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
		propertyListeners.removePropertyChangeListener(listener);
	}
	/**
	 * <p>removePropertyChangeListener.</p>
	 *
	 * @param propertyName a {@link java.lang.String} object
	 * @param listener a {@link java.beans.PropertyChangeListener} object
	 */
	public void removePropertyChangeListener(String propertyName, java.beans.PropertyChangeListener listener) {
		propertyListeners.removePropertyChangeListener(propertyName,listener);
	}
}
