/**
 *
 */
package org.scictrl.mp.orbitcorrect.mvc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * View for the application. It provides lazy initiallization pattern for two groups of classes:
 * Swing actions and Swing components.
 *
 * IT must listen to data model.
 *
 * @author igor@scictrl.com
 * 
 * @param <C> {@link ApplicationController} implementation type
 * @param <AC> {@link ApplicationView.Actions} implementation type
 * @param <CO> {@link ApplicationView.Components} implementation type
 */
public abstract class ApplicationView <C extends ApplicationController<?,?>, AC extends ApplicationView<?,?,?>.Actions, CO extends ApplicationView<?,?,?>.Components> implements PropertyChangeListener {

	/**
	 * Actions should be used in components. They should call methods in controller, which provides
	 * logic.
	 * @author igor@kriznar.com
	 *
	 */
	public abstract class Actions {
		
		/**
		 * Constructor.
		 */
		protected Actions() {
		}

		/**
		 * Declare action fields here.
		 */

		/**
		 * Declare lazy action getters here.
		 */

		/**
		 * Returns action which is stored in this class instance
		 * @param name property name that has getter that returns the action
		 * @return action
		 */
		public Action getAction(String name) {
			try {
				PropertyDescriptor pd= new PropertyDescriptor(name,this.getClass());
				return (Action) pd.getReadMethod().invoke(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	/**
	 * The Swing components.
	 *
	 * @author igor@kriznar.com
	 *
	 */
	public abstract class Components {
		
		/**
		 * Constructor.
		 */
		protected Components() {
		}

		/**
		 * Declare component fields here.
		 */

		/**
		 * Declare lazy component getters here.
		 */


		/**
		 * Returns component which is stored in this class instance
		 * @param name property name that has getter that returns the component
		 * @return component
		 */
		public JComponent getComponent(String name) {
			try {
				PropertyDescriptor pd= new PropertyDescriptor(name,this.getClass());
				return (JComponent) pd.getReadMethod().invoke(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	private boolean initialized =false;

	/**
	 * Actions handler.
	 */
	public AC actions;

	/**
	 * Components hodler.
	 */
	public CO components;

	/**
	 * Controller
	 */
	public C controller;


	/**
	 * <p>Constructor for ApplicationView.</p>
	 */
	public ApplicationView() {

	}

	/**
	 * <p>initialize.</p>
	 *
	 * @param controller a {@link org.scictrl.mp.orbitcorrect.mvc.ApplicationController} object
	 */
	@SuppressWarnings("unchecked")
	public synchronized void initialize (ApplicationController<?,?> controller) {

		if (initialized ) {
			return;
		}

		actions= newActions();
		components= newComponents();

		this.controller= (C) controller;

		controller.addPropertyChangeListener(this);
		controller.getEngine().addPropertyChangeListener(this);
		controller.getModel().addPropertyChangeListener(this);

		initialized=true;
	}

	/**
	 * <p>newActions.</p>
	 *
	 * @return a AC object
	 */
	protected abstract AC newActions();
	/**
	 * <p>newComponents.</p>
	 *
	 * @return a CO object
	 */
	protected abstract CO newComponents();


	/** {@inheritDoc} */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {

		/*
		 * This cevent should be handled in GUI components. For security reasons it is
		 * better to be executed trugh Swing GUI thread.
		 */

		if (SwingUtilities.isEventDispatchThread()) {
			handlePropertyChange(evt);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					handlePropertyChange(evt);
				}
			});
		}

	}

	/**
	 * <p>setVisible.</p>
	 *
	 * @param visible a boolean
	 */
	public void setVisible(final boolean visible) {
		if (SwingUtilities.isEventDispatchThread()) {
			handleSetVisible(visible);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					handleSetVisible(visible);
				}
			});
		}
	}

	/**
	 * Handle model and controller updates by updating GUI.
	 *
	 * @param evt a {@link java.beans.PropertyChangeEvent} object
	 */
	protected abstract void handlePropertyChange(PropertyChangeEvent evt);

	/**
	 * <p>handleSetVisible.</p>
	 *
	 * @param visible a boolean
	 */
	protected abstract void handleSetVisible(boolean visible);
}
