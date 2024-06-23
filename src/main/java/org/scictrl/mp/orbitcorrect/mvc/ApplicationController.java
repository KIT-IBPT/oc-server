package org.scictrl.mp.orbitcorrect.mvc;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;

/**
 * Abstract class on which application should be built.
 * It is the controller part of MVC.
 * It has references to the ApplicationView and DataModel and ApplicationEngine, which has common
 * application writing support for DataBush based applications.
 *
 * @author igor@scictrl.com
 * 
 * @param <M> {@link ApplicationModel} implementation type
 * @param <W> {@link ApplicationView} implementation type
 */
public abstract class ApplicationController <M extends ApplicationModel, W extends ApplicationView<?,?,?>> {

	/** Logger instance.*/
	protected final Logger logger = LogManager.getLogger(this.getClass());

	private ApplicationEngine engine;

	private String name;

	private M model;
	private PropertyChangeSupport support;

	private W view;


	/**
	 * Constructs new frame. New DBInitializer is created if needed. This constructor also
	 * calls <code>initializeDataBush()</code> method.
	 *
	 * @param name a {@link java.lang.String} object
	 */
	public ApplicationController(String name) {
		super();
		this.name=name;
		support= new PropertyChangeSupport(this);
	}
	/**
	 * Called whenever the part throws an exception.
	 *
	 * @param exception java.lang.Throwable
	 */
	protected void handleException(Throwable exception) {

		/* Uncomment the following lines to print uncaught exceptions to stdout */
		logger.warn("UNCAUGHT EXCEPTION - "+exception, exception);
		 //exception.printStackTrace();
	}

	/**
	 * <p>addPropertyChangeListener.</p>
	 *
	 * @param n a {@link java.lang.String} object
	 * @param l a {@link java.beans.PropertyChangeListener} object
	 */
	public void addPropertyChangeListener(String n, PropertyChangeListener l) {
		if (n==null) {
			support.addPropertyChangeListener(l);
		}
		support.addPropertyChangeListener(n, l);
	}

	/**
	 * <p>addPropertyChangeListener.</p>
	 *
	 * @param l a {@link java.beans.PropertyChangeListener} object
	 */
	public void addPropertyChangeListener(PropertyChangeListener l) {
		support.addPropertyChangeListener(l);
	}

	/**
	 * <p>Getter for the field <code>engine</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.mvc.ApplicationEngine} object
	 */
	public synchronized ApplicationEngine getEngine() {
		if (engine == null) {
			synchronized (this) {
				if (engine == null) {
					engine=createNewEngine();
					engine.initialize();
				}
			}
		}

		return engine;
	}

	/**
	 * <p>getDataBush.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} object
	 */
	public DataBush getDataBush() {
		return getEngine().getServer().getDataBush();
	}

	/**
	 * <p>createNewEngine.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.mvc.ApplicationEngine} object
	 */
	protected ApplicationEngine createNewEngine() {
		return new ApplicationEngine(getName());
	}

	/**
	 * <p>createNewModel.</p>
	 *
	 * @return a M object
	 */
	protected abstract M createNewModel();

	/**
	 * <p>Getter for the field <code>model</code>.</p>
	 *
	 * @return a M object
	 */
	public synchronized M getModel() {
		if (model == null) {
			synchronized (this) {
				if (model == null) {
					model=createNewModel();
				}
			}
		}

		return model;
	}

	/**
	 * <p>createNewView.</p>
	 *
	 * @return a W object
	 */
	protected abstract W createNewView();

	/**
	 * <p>Getter for the field <code>view</code>.</p>
	 *
	 * @return a W object
	 */
	public synchronized W getView() {
		if (view == null) {
			synchronized (this) {
				if (view == null) {
					view=createNewView();
					view.initialize(this);
				}
			}
		}

		return view;
	}

	/**
	 * <p>Getter for the field <code>name</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>shutdown.</p>
	 */
	public synchronized void shutdown() {

		getView().setVisible(false);

		getEngine().shutdown();

		//System.exit(0);
	}

}
