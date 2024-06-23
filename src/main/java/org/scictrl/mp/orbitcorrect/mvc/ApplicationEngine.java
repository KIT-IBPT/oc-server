/**
 *
 */
package org.scictrl.mp.orbitcorrect.mvc;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scictrl.csshell.AbstractConnector;
import org.scictrl.csshell.RemoteException;
import org.scictrl.csshell.dummy.DummyConnector;
import org.scictrl.mp.orbitcorrect.server.DataBushServerLocal;
import org.scictrl.mp.orbitcorrect.server.IDataBushServer;

import si.ijs.anka.config.BootstrapLoader;

/**
 * <p>ApplicationEngine class.</p>
 *
 * @author igor@kriznar.com
 */
public class ApplicationEngine {

	/** Constant <code>PARAM_CONNECTOR="Connector"</code> */
	public static final String PARAM_CONNECTOR="Connector";
	/** Constant <code>EPICS_CONNECTOR_NAME="EPICS"</code> */
	public static final String EPICS_CONNECTOR_NAME="EPICS";
	/** Constant <code>EPICS_CONNECTOR_INSTANCE="org.scictrl.csshell.epics.EPICSConnecto"{trunked}</code> */
	public static final String EPICS_CONNECTOR_INSTANCE="org.scictrl.csshell.epics.EPICSConnector";


	/** Constant <code>PARAM_DATABUSH_LOCATOR="DataBush.locator"</code> */
	public static final String PARAM_DATABUSH_LOCATOR="DataBush.locator";
	/** Constant <code>LOCAL_DATABUSH_LOCATOR="LOCAL"</code> */
	public static final String LOCAL_DATABUSH_LOCATOR="LOCAL";
	/** Constant <code>STATE="state"</code> */
	public static final String STATE="state";

	/**
	 * Engine state.
	 */
	public enum State {
		/** Initial state.*/
		INITIAL,
		/** Last operation failed.*/
		FAILED,
		/** Initialized.*/
		INITIALIZED,
		/** Operation is active.*/
		ACTIVE,
		/** Engine closed.*/
		CLOSED;

		/**
		 * State is initial.
		 * @return <code>true</code> if initial
		 */
		public boolean isInitial() {
			return this==INITIAL;
		}

		/**
		 * State is failed.
		 * @return <code>true</code> if state failed
		 */
		public boolean isFailed() {
			return this==FAILED;
		}

		/**
		 * State is initialized.
		 * @return <code>true</code> if state initial
		 */
		public boolean isInitialized() {
			return this==INITIALIZED;
		}

		/**
		 * State is active.
		 * @return <code>true</code> if state active
		 */
		public boolean isActive() {
			return this==ACTIVE;
		}

		/**
		 * State is closed
		 * @return <code>true</code> if state closed
		 */
		public boolean isClosed() {
			return this==CLOSED;
		}
	}


	private String name;
	private PropertiesConfiguration configuration;
	private PropertiesConfiguration configurationPersistance;
	private Logger log;
	private IDataBushServer server;
	private PropertyChangeSupport support;
	private State state=State.INITIAL;
	private String stateReport;
	private AbstractConnector<?> connector;

	/**
	 * <p>Constructor for ApplicationEngine.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 */
	public ApplicationEngine(String name) {
		this.name=name;
		//BasicConfigurator.configure();
		log= LogManager.getLogger(this.getClass());
		log.info("Application '"+name+"' started.");
		support= new PropertyChangeSupport(this);
	}

	/**
	 * <p>getLogger.</p>
	 *
	 * @return a {@link org.apache.logging.log4j.Logger} object
	 */
	public Logger getLogger() {
		return LogManager.getRootLogger();
	}


	/**
	 * Returns true if it uses private locally started DataBush infrastructure and server.
	 *
	 * @return true if it uses private locally started DataBush
	 */
	public boolean isLocal() {
		String loc= getProperty(PARAM_DATABUSH_LOCATOR,LOCAL_DATABUSH_LOCATOR);
		return LOCAL_DATABUSH_LOCATOR.equals(loc.toUpperCase());
	}

	/**
	 * <p>getProperty.</p>
	 *
	 * @param key a {@link java.lang.String} object
	 * @param defaultValue a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public String getProperty(String key, String defaultValue) {
		return getConfiguration().getString(key, defaultValue);
	}

	/**
	 * <p>setLocal.</p>
	 *
	 * @param b a boolean
	 */
	public void setLocal(boolean b) {
		if (state!=State.INITIAL) {
			throw new IllegalStateException("Settable only in INITIAL state!");
		}
		configuration.setProperty(PARAM_DATABUSH_LOCATOR, LOCAL_DATABUSH_LOCATOR);
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
	 * <p>Getter for the field <code>state</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.mvc.ApplicationEngine.State} object
	 */
	public State getState() {
		return state;
	}

	/**
	 * <p>Getter for the field <code>stateReport</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getStateReport() {
		return stateReport;
	}

	/**
	 * <p>Setter for the field <code>state</code>.</p>
	 *
	 * @param s a {@link org.scictrl.mp.orbitcorrect.mvc.ApplicationEngine.State} object
	 * @param report a {@link java.lang.String} object
	 */
	public void setState(State s, String report) {
		if (this.state==s) {
			return;
		}
		State old= this.state;
		this.state=s;
		this.stateReport=report;
		support.firePropertyChange(STATE, old, s);
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
	 * <p>initialize.</p>
	 */
	public void initialize() {
		if (isLocal()) {
			if (getState().isInitial()) {
				try {
					server= new DataBushServerLocal(this);
					server.initialize();
					setState(State.INITIALIZED,"All OK.");
				} catch (Exception e) {
					getLogger().error("Failed to create DataBush server connection", e);
					setState(State.FAILED, e.toString());
					return;
				}
			}
		}
	}

	/**
	 * <p>activate.</p>
	 */
	public void activate() {
		setState(State.ACTIVE,"Activated.");
	}

	/**
	 * <p>fail.</p>
	 *
	 * @param t a {@link java.lang.Throwable} object
	 */
	public void fail(Throwable t) {
		setState(State.FAILED,t.toString());
	}

	/**
	 * <p>resolveApplicationConfigFile.</p>
	 *
	 * @param confFileName a {@link java.lang.String} object
	 * @return a {@link java.io.File} object
	 */
	public File resolveApplicationConfigFile(String confFileName) {
		File f= BootstrapLoader.getInstance().getApplicationConfigFile(name, confFileName);
		return f;
	}

	/**
	 * <p>Getter for the field <code>configuration</code>.</p>
	 *
	 * @return a {@link org.apache.commons.configuration.Configuration} object
	 */
	public synchronized Configuration getConfiguration() {
		if (configuration==null) {
			configuration= new PropertiesConfiguration();
			Reader r;
			try {
				File f= BootstrapLoader.getInstance().getApplicationConfigFile(name, name+".properties");
				r = new BufferedReader(new FileReader(f));
				configuration.load(r);
				configuration.setAutoSave(false);
				configuration.setFile(f);
				r.close();
				getLogger().info("Application configuration loaded from: "+f.toString());
			} catch (Exception e) {
				log.warn("Failed to load configuration: "+e.toString());
				try {
					File f= BootstrapLoader.getInstance().getApplicationConfigFile("DataBush", "DataBush.properties");
					r = new BufferedReader(new FileReader(f));
					configuration.load(r);
					configuration.setAutoSave(false);
					configuration.setFile(f);
					r.close();
					getLogger().info("Application configuration loaded from fall-back: "+f.toString());
				} catch (Exception ex) {
					log.warn("Failed to load fall-back configuration: "+ex.toString());
				}
			}
		}
		return configuration;
	}

	/**
	 * <p>getPersistanceStore.</p>
	 *
	 * @return a {@link org.apache.commons.configuration.Configuration} object
	 */
	public synchronized Configuration getPersistanceStore() {
		if (configurationPersistance==null) {
			try {
				configurationPersistance= new PropertiesConfiguration(BootstrapLoader.getInstance().getApplicationConfigFile(name, name+"Persistance.properties"));
			} catch (ConfigurationException e) {
				log.error("Failed to load configuration: "+e.toString(), e);
				configurationPersistance= new PropertiesConfiguration();			}
		}
		return configurationPersistance;
	}


	/**
	 * <p>Getter for the field <code>server</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.server.IDataBushServer} object
	 */
	public synchronized IDataBushServer getServer() {
		if (server==null) {
			throw new NullPointerException("Not yet initialized!");
		}
		return server;
	}

	/**
	 * <p>getProperty.</p>
	 *
	 * @param key a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public String getProperty(String key) {
		return getConfiguration().getString(key);
	}

	/**
	 * <p>shutdown.</p>
	 */
	public synchronized void shutdown() {

		setState(State.CLOSED, "Application closed.");
	}

	/**
	 * <p>Getter for the field <code>connector</code>.</p>
	 *
	 * @return a {@link org.scictrl.csshell.AbstractConnector} object
	 * @deprecated use EControlSystemEngine instead
	 */
	@Deprecated
	public AbstractConnector<?> getConnector() {
		if (connector==null) {
			synchronized (this) {
				if (connector==null) {
					String c= getProperty(PARAM_CONNECTOR, EPICS_CONNECTOR_INSTANCE);
					if (EPICS_CONNECTOR_NAME.equalsIgnoreCase(c)) {
						c= EPICS_CONNECTOR_INSTANCE;
					}

					try {
						Class<?> cl= Class.forName(c);

						if (!AbstractConnector.class.isAssignableFrom(cl)) {
							log.error("Configured class '"+c+"' is not a connetor!");
							connector= DummyConnector.getInstance(null);
							return connector;
						}

						connector = (AbstractConnector<?>) cl.getMethod("getInstance", Properties.class).invoke(null, ConfigurationConverter.getProperties(getConfiguration()));

					} catch (Exception e) {
						log.error("Failed to instantiate connector '"+c+"':"+e, e);
						try {
							connector= DummyConnector.getInstance(null);
						} catch (RemoteException e1) {
							// never happens
							e1.printStackTrace();
						}
					}
				}
			}
		}
		return connector;
	}

}
