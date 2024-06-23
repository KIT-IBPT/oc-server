/**
 *
 */
package org.scictrl.mp.orbitcorrect.server;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scictrl.csshell.epics.server.application.EmbeddedApplicationServer;
import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.DBInitializer;
import org.scictrl.mp.orbitcorrect.DataBushEvent;
import org.scictrl.mp.orbitcorrect.DataBushException;
import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.IDataBushListener;
import org.scictrl.mp.orbitcorrect.Orientation;
import org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionModel;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractMagnetElement;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
import org.scictrl.mp.orbitcorrect.model.optics.MagnetList;
import org.scictrl.mp.orbitcorrect.model.optics.PowerSupply;
import org.scictrl.mp.orbitcorrect.mvc.ApplicationEngine;
import org.scictrl.remotestate.RemoteStateNotifier;

import si.ijs.anka.config.BootstrapLoader;

/**
 * <p>DataBushServerLocal class.</p>
 *
 * @author igor@kriznar.com
 */
public class DataBushServerLocal implements IDataBushServer {

	/** Process watchdog. */
	protected RemoteStateNotifier watchdogRunning;

	private static final String PROPERTY_DATABUSH_INIT_FILE = "databush.init";
	private static final String DATABUSHSERVER = "DataBushServer";
	//private static final String DEFAULT_INIT_FILE = "IDataBushServer.properties";
	private static final String DATABUSH = "DataBush";
	//private static final String WATCHDOG_ENABLED = "DataBush.watchdog_enabled";
	private DataBush dataBush;
	//private IOrbitCorrectionModel horizontalOrbitCorrection;
	//private IOrbitCorrectionModel verticalOrbitCorrection;

	private DBInitializer dbInitializer;
	private boolean simulation=false;
	private ServerDataModel dataModel;
	private EmbeddedApplicationServer epicsServer;

	private Logger log= LogManager.getLogger(DataBushServerLocal.class);
	//private AutomaticOrbitCorrectionModel automaticOrbitCorrectionModel;
	private ApplicationEngine engine;

	/**
	 * <p>Constructor for DataBushServerLocal.</p>
	 */
	public DataBushServerLocal() {
		dataModel= new ServerDataModel();
	}

	/**
	 * <p>Constructor for DataBushServerLocal.</p>
	 *
	 * @param engine a {@link org.scictrl.mp.orbitcorrect.mvc.ApplicationEngine} object
	 */
	public DataBushServerLocal(ApplicationEngine engine) {
		this();
		this.engine=engine;
	}

	/**
	 * <p>Getter for the field <code>engine</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.mvc.ApplicationEngine} object
	 */
	public synchronized ApplicationEngine getEngine() {
		if (engine == null) {
			engine = new ApplicationEngine(DATABUSHSERVER);
		}
		return engine;
	}

	/**
	 * <p>startEpicsServer.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @return a {@link org.scictrl.csshell.epics.server.application.EmbeddedApplicationServer} object
	 * @throws java.lang.IllegalAccessException if any.
	 */
	public EmbeddedApplicationServer startEpicsServer(String name) throws IllegalAccessException {
		if(epicsServer!=null) {
			throw new IllegalAccessException("Server is allready started");
		}
		epicsServer= EmbeddedApplicationServer.newInstance(name);
		try {
			epicsServer.startServer();
		} catch (Exception e) {
			log.error("EPICS server could not start: "+e.toString(), e);
			e.printStackTrace();
		}
		log.info("EPICS server '"+name+"' is active!");
		return epicsServer;
	}

	/**
	 * <p>Getter for the field <code>epicsServer</code>.</p>
	 *
	 * @return a {@link org.scictrl.csshell.epics.server.application.EmbeddedApplicationServer} object
	 */
	public EmbeddedApplicationServer getEpicsServer() {
		return epicsServer;
	}

	/** {@inheritDoc} */
	@Override
	public ServerDataModel getDataModel() {
		return dataModel;
	}

	/* (non-Javadoc)
	 * @see org.scictrl.mp.orbitcorrect.utilities.server.DataBushServer#getDataBush()
	 */
	/** {@inheritDoc} */
	@Override
	public DataBush getDataBush() {
		return dataBush;
	}


	/**
	 * <p>initDB.</p>
	 *
	 * @throws java.net.MalformedURLException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushException if any.
	 * @throws java.io.IOException if any.
	 */
	public void initDB() throws MalformedURLException, DataBushException, IOException {

		log.info("starting DataBush initialization...");

		getDBInitializer().initDB();
		if (simulation) {
			Iterator<BPMonitor> it= getDataBush().getBPMonitors().iterator();
			while(it.hasNext()) it.next().setVirtual(true);
		}
		getDataBush().setUpdateMode(DBConst.UPDATE_MANUAL);

		try {
			getDataBush().connect();
		} catch (DataBushPackedException e) {

			log.error("DataBush connect failed, packed connection!");

			for (DataBushException ex : e.packed) {
				log.error(ex.toString(), ex);
			}

			e.throwIt();
			//connectToConnectableDevices(e);
		}

		getDataBush().update();
		getDataBush().setProperySetFiresEvent(false);
		getDataBush().setUpdateMode(DBConst.UPDATE_ON_EVENT);
		log.info("DataBush initialization done!");

	}


	@SuppressWarnings("unused")
	private void connectToConnectableDevices(DataBushPackedException pe) throws org.scictrl.mp.orbitcorrect.DataBushInitializationException {
		System.out.println("DataBush connection failed to some devices!");

	// all elements in databush
		ElementList<AbstractDataBushElement> allDevices = new ElementList<>();

		Iterator<AbstractOpticalElement>  oI = getDataBush().getOptics().iterator();
		Iterator<PowerSupply> psI = getDataBush().getPowerSupplies().iterator();

		while(oI.hasNext()) {
			allDevices.add(oI.next());
		}

		while(psI.hasNext()) {
			allDevices.add(psI.next());
		}

		if (getDataBush().getRFGenerator() != null) allDevices.add(getDataBush().getRFGenerator());
		if (getDataBush().getDataBushInfo()!= null) allDevices.add(getDataBush().getDataBushInfo());


		String massage = "OC can not connect to following devices:\n";

	//elements which failed, also removed from complete list
		ElementList<AbstractDataBushElement> connectionFailedDevices = new ElementList<>();

		int l = 0;
		for (int i = 0; i < pe.packed.length; i++) {
			AbstractDataBushElement connectionFailedDevice = pe.packed[i].getSource();
			allDevices.remove(connectionFailedDevice);
			connectionFailedDevices.add(connectionFailedDevice);
			massage = massage + (i==0 ? "" : ", ") + connectionFailedDevice.getName();
			l += connectionFailedDevice.getName().length();
			if (l >= 100) {
				massage += "\n";
				l = 0;
			}


			if(connectionFailedDevice instanceof org.scictrl.mp.orbitcorrect.model.optics.PowerSupply) {
//				System.out.println("PowerSupplay...Adding depending magnets to ConnectionFailedDevices > ");

				MagnetList dependingMagnets = ((PowerSupply)connectionFailedDevice).getDependingMagnets();
				Iterator<AbstractMagnetElement> dmI = dependingMagnets.iterator();
				while(dmI.hasNext()) {
					AbstractDataBushElement dependingMagnet  = dmI.next();
					allDevices.remove(dependingMagnet);
					connectionFailedDevices.add(dependingMagnet);
						massage = massage + ", " + connectionFailedDevice.getName();
						l += connectionFailedDevice.getName().length();
						if (l >= 100) {
							massage += "\n";
							l = 0;
						}
				}
			}
		}

			massage =
				massage
					+ "\n"
					+ "Press Ok to continue without this devices or"
					+ "\n"
					+ "cancel, resolve problems and restart application.";

		/*if (javax.swing.JOptionPane.OK_OPTION!=javax.swing.JOptionPane.showConfirmDialog(this,massage,"Connection Failed!",javax.swing.JOptionPane.OK_CANCEL_OPTION,javax.swing.JOptionPane.ERROR_MESSAGE)) {
			this.dispose();
		}*/

		getDataBush().clear();


		for (AbstractDataBushElement el : allDevices) {
			if (el instanceof org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement) {
				((org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement)el).setRelFrom(null);
			}
		}


		System.out.println("REInitializing DataBush!");
		getDataBush().init(allDevices);

		try {
			System.out.println("REConnecting!");
			getDataBush().connect();
		} catch (DataBushPackedException e) {
			connectToConnectableDevices(e);
		}
	}

	/**
	 * <p>getDBInitializer.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.DBInitializer} object
	 * @throws java.io.IOException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushException if any.
	 */
	public DBInitializer getDBInitializer() throws IOException, DataBushException {
		if (dbInitializer == null) {

			String fname= getEngine().getConfiguration().getString(PROPERTY_DATABUSH_INIT_FILE, DBInitializer.DBINIT_DATA_FILE);

			File f= BootstrapLoader.getInstance().getApplicationConfigFile(DATABUSH, fname);

			dbInitializer = DBInitializer.newInstance(f);
		}

		return dbInitializer;
	}

	/** {@inheritDoc} */
	@Override
	public void shutdown() {
		if (epicsServer!=null) {
			epicsServer.shutdown();
		}
		dataBush.abort();
		try {
			dataBush.disconnect();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataBushPackedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dataBush.clear();
	}

	/**
	 * <p>addOrbitCorrectionModelClass.</p>
	 *
	 * @throws java.lang.ClassNotFoundException if fails
	 * @throws java.lang.IllegalAccessException if fails
	 * @throws java.lang.InstantiationException if fails
	 * @throws java.lang.SecurityException if fails
	 * @throws java.lang.NoSuchMethodException if fails
	 * @throws java.lang.reflect.InvocationTargetException if fails
	 * @throws java.lang.IllegalArgumentException if fails
	 * @param model a {@link java.lang.String} object
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 */
	public void addOrbitCorrectionModelClass(String model, Orientation ori) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<?> cl= Class.forName(model);
		if (!IOrbitCorrectionModel.class.isAssignableFrom(cl)) throw new ClassCastException("Not IOrbitCorrectionModel class!");
		IOrbitCorrectionModel m=null;
		m= (IOrbitCorrectionModel)cl.getDeclaredConstructor().newInstance();
		m.initialize(ori);
		List<IOrbitCorrectionModel> l= getDataModel().getAvailableCorrectionModels(ori);
		for (IOrbitCorrectionModel ocm : l) {
			if (ocm.getName().equals(m.getName())) {
				return;
			}
		}
		getDataModel().addAvailableCorrectionModel(m);
	}

	/** {@inheritDoc} */
	@Override
	public void initialize() throws ConfigurationException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, DataBushException {

		//String init= System.getProperty(DBSERVER_INIT_FILE, DEFAULT_INIT_FILE);

		//configuration= BootstrapLoader.getInstance().getApplicationConfigProperties(DBSERVER, init);

		// TODO: load from configuration

/*		addOrbitCorrectionModelClass("Default OC",DefaultOrbitCorrectionModel.class.getName());
		addOrbitCorrectionModelClass("RM OC",EmpiricOrbitCorrectionModel.class.getName());
		addOrbitCorrectionModelClass("Freq OC",FrequencyOrbitCorrectionModel.class.getName());
		addOrbitCorrectionModelClass("C Reduction",CorrectorStrengthReductionModel.class.getName());
		addOrbitCorrectionModelClass("3C Bump",Bump3CModel.class.getName());
		addOrbitCorrectionModelClass("4C Bump",Bump4CModel.class.getName());
		addOrbitCorrectionModelClass("RM Bump",ResponseMatrixBumpModel.class.getName());
*/
		dataBush = getDBInitializer().getDataBush();

		if (log.isDebugEnabled()) {
			dataBush.addDataBushListener(new IDataBushListener() {

				@Override
				public void statusChanged(DataBushEvent e) {
					log.debug("STATUS "+e.toString());
				}

				@Override
				public void rfChanged(DataBushEvent e) {
					log.debug("RF "+e.toString());
				}

				@Override
				public void machineFunctionsChanged(DataBushEvent e) {
					log.debug("OPTICS "+e.toString());
				}

				@Override
				public void inconsistentData(DataBushEvent e) {
					log.debug("DATA ERROR "+e.toString());
				}

				@Override
				public void fieldChanged(DataBushEvent e) {
					//log.debug("FIELD "+e.toString());
				}

				@Override
				public void beamChanged(DataBushEvent e) {
					//log.debug("BEAM "+e.toString());
				}
			});
		}

/*		dataBush.addDataBushListener(new org.scictrl.mp.orbitcorrect.DataBushAdapter() {
			@Override
			public void fieldChanged( org.scictrl.mp.orbitcorrect.DataBushEvent e) {
				//horizontalOrbitCorrection.invalidateCalculations(true,"All calculations invalidated: field changed");
				//verticalOrbitCorrection.invalidateCalculations(true,"All calculations invalidated: field changed");
			}
			public void machineFunctionsChanged( DataBushEvent e) {
				getHorizontalOrbitCorrection().setLabelMessage("All calculations invalidated: beam position changed");
				getVerticalOrbitCorrection().setLabelMessage("All calculations invalidated: beam position changed");
				getHorizontalOrbitCorrection().invalidateCalculations(true);
				getVerticalOrbitCorrection().invalidateCalculations(true);
			}
		});
*/
		//initialize();

		/*boolean watchdog= getConfiguration().getBoolean(WATCHDOG_ENABLED, false);
		if (watchdog) {
			watchdogRunning= new RemoteStateNotifier("A:GL:Machine:01:OC:Running", 1000);
			watchdogRunning.activateAutoSwitch(false);
		}*/

		initDB();

		if (getDataBush().isStatusOperational()) {

			@SuppressWarnings("unchecked")
			ElementList<BPMonitor> bpms= (ElementList<BPMonitor>) getDataBush().getBPMonitors().toElementList();
			@SuppressWarnings("unchecked")
			ElementList<AbstractCorrector> corH= (ElementList<AbstractCorrector>) getDataBush().getHorCorrectors().toElementList();
			@SuppressWarnings("unchecked")
			ElementList<AbstractCorrector> corV= (ElementList<AbstractCorrector>) getDataBush().getVerCorrectors().toElementList();

			getDataModel().setAvailableBpmsH(bpms);
			getDataModel().setAvailableBpmsV(bpms);
			getDataModel().setAvailableCorrectorsH(corH);
			getDataModel().setAvailableCorrectorsV(corV);
		}
	}

	/**
	 * <p>calculateCorrection.</p>
	 *
	 * @param orientation a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 */
	public void calculateCorrection(Orientation orientation) {
		// TODO Auto-generated method stub

	}



}
