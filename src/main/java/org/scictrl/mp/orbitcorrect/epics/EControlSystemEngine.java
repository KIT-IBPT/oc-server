package org.scictrl.mp.orbitcorrect.epics;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scictrl.csshell.Connection;
import org.scictrl.csshell.DataType;
import org.scictrl.csshell.RemoteException;
import org.scictrl.csshell.epics.EPICSConnection;
import org.scictrl.csshell.epics.EPICSConnector;
import org.scictrl.csshell.epics.EPICSUtilities;
import org.scictrl.csshell.epics.server.Server;
import org.scictrl.csshell.epics.server.processor.MemoryValueProcessor;
import org.scictrl.mp.orbitcorrect.ControlSystemException;
import org.scictrl.mp.orbitcorrect.model.IDataConnector;
import org.scictrl.mp.orbitcorrect.model.IControlSystemEngine;
import org.scictrl.mp.orbitcorrect.model.IWriteConnector;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.DataBushInfo;
import org.scictrl.mp.orbitcorrect.model.optics.IBindedElement;
import org.scictrl.mp.orbitcorrect.model.optics.PowerSupply;
import org.scictrl.mp.orbitcorrect.model.optics.RFGenerator;
import org.scictrl.remotestate.RemoteStateNotifier;

import gov.aps.jca.CAException;

/**
 * <p>EControlSystemEngine class.</p>
 *
 * @author igor@scictrl.com
 */
public class EControlSystemEngine implements IControlSystemEngine {

	private static final String DB_CSENGINE_WATCHDOG_AUTOMODE_PV = "db.csengine.watchdog.automodePV";
	private static final String DB_CSENGINE_WATCHDOG_ENABLED = "db.csengine.watchdog.enabled";


	/** Constant <code>BEAM_CURRENT_NAME="A:SR:BeamInfo:01:Current"</code> */
	public static String BEAM_CURRENT_NAME="A:SR:BeamInfo:01:Current";
	/** Constant <code>RF_STEPPING_NAME="A:TI:SignGen:SR-01:Status:Stepping"</code> */
	public static String RF_STEPPING_NAME="A:TI:SignGen:SR-01:Status:Stepping";


	private EPICSConnector connector;
	private boolean watchdogEnabled=false;
	private RemoteStateNotifier watchdogAuto;
	private RemoteStateNotifier watchdogCorrect;
	// "A:GL:Machine:01:OC:AutoMode"
	private String watchdogAutoPV;
	// "A:GL:Machine:01:OC:Correcting"
	private String watchdogCorrectPV;


	private RemoteStateNotifier watchdogRunning;


	private String watchdogRunningPV;

	private Logger log= LogManager.getLogger(getClass());
	
	private final boolean debug=false;
	private final boolean test=true;


	private Server server;

	/**
	 * <p>Constructor for EControlSystemEngine.</p>
	 */
	public EControlSystemEngine() {
		super();
		//watchdogEnabled= Boolean.getBoolean("DataBush.watchdog_enabled");
	}
	
	/**
	 * <p>getSimulator.</p>
	 *
	 * @return a {@link org.scictrl.csshell.epics.server.Server} object
	 * @throws org.apache.commons.configuration.ConfigurationException if any.
	 * @throws org.scictrl.csshell.RemoteException if any.
	 * @throws gov.aps.jca.CAException if any.
	 */
	public Server getSimulator() throws ConfigurationException, RemoteException, CAException {
		if (server==null) {
			server= new Server(true);
			server.activate();
		}
		return server;
	}

	/**
	 * <p>Getter for the field <code>connector</code>.</p>
	 *
	 * @return a {@link org.scictrl.csshell.epics.EPICSConnector} object
	 * @throws org.scictrl.csshell.RemoteException if any.
	 */
	public EPICSConnector getConnector() throws org.scictrl.csshell.RemoteException {
		if (connector == null) {
			connector = EPICSConnector.newInstance(System.getProperties());
		}
		return connector;

	}

	/**
	 * <p>Getter for the field <code>watchdogAuto</code>.</p>
	 *
	 * @return a {@link org.scictrl.remotestate.RemoteStateNotifier} object
	 */
	public RemoteStateNotifier getWatchdogAuto() {
		if (watchdogAuto==null) {
			watchdogAuto= new RemoteStateNotifier(watchdogAutoPV, 1000);
			if (watchdogAutoPV!=null) {
				try {
					watchdogAuto.activate(false);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
		return watchdogAuto;
	}

	/**
	 * <p>Getter for the field <code>watchdogRunning</code>.</p>
	 *
	 * @return a {@link org.scictrl.remotestate.RemoteStateNotifier} object
	 */
	public RemoteStateNotifier getWatchdogRunning() {
		if (watchdogRunning==null) {
			watchdogRunning= new RemoteStateNotifier(watchdogRunningPV, 1000);
			if (watchdogRunningPV!=null) {
				try {
					watchdogRunning.activateWithAutoSwitch(true, 0);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
		return watchdogRunning;
	}

	/**
	 * <p>Getter for the field <code>watchdogCorrect</code>.</p>
	 *
	 * @return a {@link org.scictrl.remotestate.RemoteStateNotifier} object
	 */
	public RemoteStateNotifier getWatchdogCorrect() {
		if (watchdogCorrect==null) {
			watchdogCorrect= new RemoteStateNotifier(watchdogCorrectPV, 100);
			if (watchdogCorrectPV!=null) {
				try {
					watchdogCorrect.activate(false);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
		return watchdogCorrect;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public IDataConnector<Double> connect(String name, boolean block) throws ControlSystemException {

		if (name==null) {
			throw new NullPointerException("Parameter name is null!");
		}
		String dev= name;
		
		if (test) {
			dev="T"+dev.substring(1);
		}
		
		try {
			if (test) {
				getSimulator().getDatabase().addRecord(MemoryValueProcessor.newDoubleProcessor(dev, "Test", 0.0, false).getRecord());
			}
			if (DataBush.debug || debug) System.out.println("EPICSEngine:connecting: "+dev);
			EDataConnectorD p= new EDataConnectorD(this, null, (EPICSConnection<Double>) getConnector().newConnection(dev, DataType.DOUBLE));
			if (block) {
				p.getConnection().waitTillConnected();
			}
			return p;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ControlSystemException("Connect failed", e);
		}
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public <T> IDataConnector<T> connect(IBindedElement<T> el) throws ControlSystemException {

		if (el==null) {
			throw new NullPointerException("Parameter el is null!");
		}
		
		String name= el.getName();

		String dev= name;

		if (test) {
			dev="T"+dev.substring(1);
		}
		
		if (DataBush.debug || debug) System.out.println("EPICSEngine:connecting: "+dev);
	
		if (el instanceof BPMonitor) {
			try {
				String dev1=dev+":SA:X";
				String dev2=dev+":SA:Y";
				if (test) {
					getSimulator().getDatabase().addRecord(MemoryValueProcessor.newDoubleProcessor(dev1, "Test", 0.0, false).getRecord());
					getSimulator().getDatabase().addRecord(MemoryValueProcessor.newDoubleProcessor(dev2, "Test", 0.0, false).getRecord());
				}
				if (DataBush.debug || debug) {
					System.out.println("EPICSEngine:connecting: "+dev1);
					System.out.println("EPICSEngine:connecting: "+dev2);
				}
				EPICSConnection<Double>[] connectors= new EPICSConnection[2];
				connectors[0]= (EPICSConnection<Double>) getConnector().newConnection(dev1, DataType.DOUBLE);
				connectors[1]= (EPICSConnection<Double>) getConnector().newConnection(dev2, DataType.DOUBLE);

				EDataConnectorDD conn= new EDataConnectorDD(this,(BPMonitor)el,connectors);
				return (IDataConnector<T>) conn;
			} catch (Exception e) {
				e.printStackTrace();
				throw new ControlSystemException("Connect failed", e);
			}

		}

		if (el instanceof DataBushInfo) {
			try {
				dev=dev+":Energy";
				if (test) {
					getSimulator().getDatabase().addRecord(MemoryValueProcessor.newDoubleProcessor(dev, "Test", 0.0, false).getRecord());
				}
				if (DataBush.debug || debug) System.out.println("EPICSEngine:connecting: "+dev);
				IDataConnector<T> p= (IDataConnector<T>) new EDataConnectorD(this, (DataBushInfo)el, (EPICSConnection<Double>) getConnector().newConnection(dev, DataType.DOUBLE));
				return p;
			} catch (Exception e) {
				e.printStackTrace();
				throw new ControlSystemException("Connect failed", e);
			}
		}
		if (el instanceof PowerSupply) {
			try {
				dev=dev+":Current:Setpoint";
				if (test) {
					getSimulator().getDatabase().addRecord(MemoryValueProcessor.newDoubleProcessor(dev, "Test", 0.0, false).getRecord());
				}
				if (DataBush.debug || debug) System.out.println("EPICSEngine:connecting: "+dev);
				EDataConnectorD p= new EDataConnectorD(this, (PowerSupply)el, (EPICSConnection<Double>) getConnector().newConnection(dev, DataType.DOUBLE));
				return (IDataConnector<T>) p;
			} catch (Exception e) {
				e.printStackTrace();
				throw new ControlSystemException("Connect failed", e);
			}
		}
		if (el instanceof RFGenerator) {
			try {
				dev=dev+":Frequency";
				if (test) {
					getSimulator().getDatabase().addRecord(MemoryValueProcessor.newDoubleProcessor(dev, "Test", 0.0, false).getRecord());
				}
				if (DataBush.debug || debug) System.out.println("EPICSEngine:connecting: "+dev);
				EDataConnectorD p= new EDataConnectorD(this, (RFGenerator)el, (EPICSConnection<Double>) getConnector().newConnection(dev, DataType.DOUBLE));
				return (IDataConnector<T>) p;
			} catch (Exception e) {
				e.printStackTrace();
				throw new ControlSystemException("Connect failed", e);
			}
		}
		try {
			if (test) {
				getSimulator().getDatabase().addRecord(MemoryValueProcessor.newDoubleProcessor(dev, "Test", 0.0, false).getRecord());
			}
			if (DataBush.debug || debug) System.out.println("EPICSEngine:connecting: "+dev);
			EDataConnectorD p= new EDataConnectorD(this, (IBindedElement<Double>)el, (EPICSConnection<Double>) getConnector().newConnection(dev, DataType.DOUBLE));
			return (IDataConnector<T>) p;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ControlSystemException("Connect failed", e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void setFast(IWriteConnector<Double>[] properties, double[] values) throws ControlSystemException {

		if (values==null) throw new NullPointerException("Parameter 'double[] values' is null!");
		if (properties==null) throw new NullPointerException("Parameter 'RFDoubleProperty[] properties' is null!");
		if (properties.length!=values.length) throw new ArrayIndexOutOfBoundsException("Array of values and array of properties is not of the same length!");

		MyCallback call= new MyCallback();

		for (int i=0; i<properties.length;i++) {
			try {
				call.sendRequest(((EDataConnectorD)properties[i]).connection,values[i]);
			} catch (org.scictrl.csshell.RemoteException e) {
				e.printStackTrace();
				throw new ControlSystemException(e);
			}
		}

		call.waitForCallback();

	}


	/**
	 * <p>test.</p>
	 *
	 * @param connection a {@link org.scictrl.csshell.epics.EPICSConnection} object
	 * @return a boolean
	 */
	public boolean test(EPICSConnection<?> connection) {
		boolean b= isReady(connection);
		if (!b) {
			return false;
		}
		try {
			if(!connection.isConnected()) {
				//System.out.println("Test failed: "+prop.getName()+" not connected!");
				return false;
			}

			// TODO: do proper status check, this is ACS style status, does nto work any more
			//long val= prop.getLastStatus();
			//
			//if (val!=0) {
			//	System.out.println("Test failed: "+prop.getName()+" status value not 0 but "+val);
			//	return false;
			//}
		} catch (Exception e) {
			log.warn("Testing of '"+connection.getName()+"' failed: "+e.toString(), e);
			return false;
		}
		return true;
	}

	/**
	 * <p>testPowerSupply.</p>
	 *
	 * @param connection a {@link org.scictrl.csshell.epics.EPICSConnection} object
	 * @return a boolean
	 */
	public boolean testPowerSupply(EPICSConnection<?> connection) {
		boolean b= isReady(connection);
		if (!b) {
			return false;
		}
		try {
			if(!connection.isConnected()) {
				//System.out.println("Test failed: "+prop.getName()+" not connected!");
				return false;
			}

			// TODO: do proper status check, this is ACS style status, does nto work any more
			//long val= prop.getLastStatus();
			//
			//if (val!=0) {
			//	System.out.println("Test failed: "+prop.getName()+" status value not 0 but "+val);
			//	return false;
			//}
		} catch (Exception e) {
			log.warn("Testing of '"+connection.getName()+"' failed: "+e.toString(), e);
			return false;
		}
		return true;
	}

	/**
	 * <p>testRF.</p>
	 *
	 * @param connection a {@link org.scictrl.csshell.epics.EPICSConnection} object
	 * @return a boolean
	 */
	public boolean testRF(EPICSConnection<?> connection) {
		boolean b= isReady(connection);
		if (!b) {
			return false;
		}
		try {
			if(!connection.isConnected()) {
				//System.out.println("Test failed: "+prop.getName()+" not connected!");
				return false;
			}
			/*String name= prop.getName();
			if (!name.contains(":")) {
				name= "ACS:"+name;
			} else if (name.endsWith(":frequency")) {
				name=name.substring(0, name.length()-":frequency".length());
			}

			int i= name.lastIndexOf("\\");

			if (i>0) {
				name = name.substring(0, i);
			}

			name = name + ":status";

			long val= (Long)connector.getValue(name, DataType.LONG);
			//System.out.println("VAL "+val+" "+Long.toBinaryString(val));

			// int: 24 binary: 11000 is <high precision=1> <power ON=1> <0> <0> <0>
			if (val!=24) {
				System.out.println("Test failed: "+prop.getName()+" status value not "+Long.toBinaryString(24)+"b but "+val+"b.");
				return false;
			}*/
		} catch (Exception e) {
			log.warn("Testing of '"+connection.getName()+"' failed: "+e.toString(), e);
			return false;
		}
		return true;
	}

	/**
	 * <p>isReady.</p>
	 *
	 * @param connection a {@link org.scictrl.csshell.epics.EPICSConnection} object
	 * @return a boolean
	 */
	public boolean isReady(EPICSConnection<?> connection) {
		try {
			if (!connection.isReady() || !connection.getLastPoop().isStatusOK()) {
				return false;
			}

		} catch (Exception e) {
			log.warn("Testing of '"+connection.getName()+"' failed: "+e.toString(), e);
			return false;
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public void notifyOrbitCorrectionEnded() {
		if (watchdogEnabled)
			getWatchdogCorrect().switchStateOff();
	}
	/** {@inheritDoc} */
	@Override
	public void notifyOrbitCorrectionStarted() {
		if (watchdogEnabled)
			getWatchdogCorrect().switchStateOn();
	}

	/** {@inheritDoc} */
	@Override
	public void notifyOrbitCorrectionRunning() {
		if (watchdogEnabled)
			getWatchdogRunning().switchStateOn();
	}
	/** {@inheritDoc} */
	@Override
	public void notifyOrbitCorrectionShuttingDown() {
		if (watchdogEnabled)
			getWatchdogRunning().switchStateOff();
	}

	/** {@inheritDoc} */
	@Override
	public void notifyAutomaticOrbitCorrectionEnded() {
		if (watchdogEnabled)
			getWatchdogAuto().switchStateOff();
	}
	/** {@inheritDoc} */
	@Override
	public void notifyAutomaticOrbitCorrectionStarted() {
		if (watchdogEnabled)
			getWatchdogAuto().switchStateOn();
	}

	/** {@inheritDoc} */
	@Override
	public void configure(Configuration conf) {
		watchdogEnabled= conf.getBoolean(DB_CSENGINE_WATCHDOG_ENABLED, true);
		watchdogAutoPV= conf.getString(DB_CSENGINE_WATCHDOG_AUTOMODE_PV,null);
		watchdogCorrectPV= conf.getString("db.csengine.watchdog.correctPV",null);
		watchdogRunningPV= conf.getString("db.csengine.watchdog.runningPV",null);
	}

	/** {@inheritDoc} */
	@Override
	public <T> T getValue(String pv, Class<T> type) throws ControlSystemException {
		if (pv==null) {
			throw new NullPointerException("Parameter pv is null!");
		}
		
		String dev= pv;
		DataType t= DataType.fromJavaClass(type);
				
		if (test) {
			dev="T"+dev.substring(1);
		}
		
		try {
			if (DataBush.debug || debug) System.out.println("EPICSEngine:retrieveing: "+dev);
			if (test) {
				getSimulator().getDatabase().addRecord(MemoryValueProcessor.newDoubleProcessor(dev, "Test", 0.0, false).getRecord());
			}
			@SuppressWarnings("unchecked")
			T o= (T)getConnector().getValue(pv, t);
			return o;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ControlSystemException("Get failed", e);
		}
	}
	
	/**
	 * <p>connect.</p>
	 *
	 * @param pv a {@link java.lang.String} object
	 * @param type a {@link java.lang.Class} object
	 * @param <T> a T class
	 * @return a {@link org.scictrl.csshell.Connection} object
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	public <T> Connection<?, T, ?> connect(String pv, Class<T> type) throws ControlSystemException {
		if (pv==null) {
			throw new NullPointerException("Parameter pv is null!");
		}

		String dev= pv;
		DataType t= DataType.fromJavaClass(type);
				
		if (test) {
			dev="T"+dev.substring(1);
		}
		
		try {
			if (DataBush.debug || debug) System.out.println("EPICSEngine:connecting: "+dev);
			if (test) {
				getSimulator().getDatabase().addRecord(MemoryValueProcessor.newProcessor(dev, EPICSUtilities.toDBRType(t), 1, "Test", null, false, false).getRecord());
			}
			@SuppressWarnings("unchecked")
			Connection<?, T, ?> c= (Connection<?, T, ?>) getConnector().newConnection(dev, t);
			return c;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ControlSystemException("Connect failed", e);
		}

	}
}
