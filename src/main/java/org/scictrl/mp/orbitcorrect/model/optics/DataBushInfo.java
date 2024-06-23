package org.scictrl.mp.orbitcorrect.model.optics;

import java.util.Iterator;

import org.scictrl.mp.orbitcorrect.ActionReportEvent;
import org.scictrl.mp.orbitcorrect.ConnectionReportEvent;
import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.DBElementDescriptor;
import org.scictrl.mp.orbitcorrect.DBString;
import org.scictrl.mp.orbitcorrect.DataBushEvent;
import org.scictrl.mp.orbitcorrect.DataBushException;
import org.scictrl.mp.orbitcorrect.DataBushInitializationException;
import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.model.IDataConnector;


/**
 * This class presents basic optic configuration parameters and wraps
 * <code>si.ijs.anka.abeans.devices.InfoServerBean</code>. Class implements following
 * interfaces:
 * <ul>
 * <li><code>IBindedElement</code> - used for manipulating connection to InfoServerBean.</li>
 * <li><code>AbstractUpdateableElement</code> - used for retrieving and processing data from InfoServerBean.</li>
 * </ul>
 * Depending on virtual flag, DataBushInof behaves differently. If virtual flag is
 * <ul>
 * <li><code>true</code> - DataBushInfo does not connect to InfoServerBean.
 * depending on readEnergyFromBendings flag, there are two
 * possibilities. If readEnergyFromBendings flag is <code>true</code> - behaves similar as if was not virtual. On update calls (manual or
 * automatic) energy is read from all bending and average number is set to this
 * element.</li>
 * <li><code>false</code> - DataBushInfo's energy is read/write property. It always returns
 * the set value.</li>
 * <li><code>false</code> - connect to InfoServerBean is enabled. Returned energy is value
 * from last update.</li>
 * </ul>
 *
 * @author igor@scictrl.com
 */
public class DataBushInfo extends AbstractDataBushElement implements IBindedElement<Double>, AbstractUpdateableElement {
	private boolean connected= false;
	private boolean a_updateMe=false;			

	private org.scictrl.mp.orbitcorrect.math.TransferMatrix matrix; //transfere matrix for magnet
	private PositionedData q; //beam normalized phase
	private PositionedData beta; //beam Beta
	private PositionedData alpha; //beam Beta
	private DispersionData dispersion;
	private double energy;

	private IDataConnector<Double> connector;
	/**
	 * Index of element's energy value in array of constructor's parameters. Use with
	 * DBElementDescriptor as help for working with elements dynamically.
	 */
	public static final int	PR_ENERGY=2;
	/**
	 * Index of element's read from bendings flag in array of constructor's parameters. Use with
	 * DBElementDescriptor as help for working with elements dynamically.
	 */
	public static final int	PR_READ_E_FROM_BEND=4;
	/**
	 * Index of element's closed orbit flag in array of constructor's parameters. Use with
	 * DBElementDescriptor as help for working with elements dynamically.
	 */
	public static final int	PR_ORBIT_IS_CLOSED=5;
	/**
	 * Index of element's beta in x direction in array of constructor's parameters. Use with
	 * DBElementDescriptor as help for working with elements dynamically.
	 */
	public final static int PR_BETA_X = 6;
	/**
	 * Index of element's beta in z direction in array of constructor's parameters. Use with
	 * DBElementDescriptor as help for working with elements dynamically.
	 */
	public final static int PR_BETA_Z = 7;
	/**			

	 * Index of element's aplha in x direction in array of constructor's parameters. Use with
	 * DBElementDescriptor as help for working with elements dynamically.
	 */
	public final static int PR_ALPHA_X = 8;
	/**
	 * Index of element's aplha in z direction in array of constructor's parameters. Use with
	 * DBElementDescriptor as help for working with elements dynamically.
	 */
	public final static int PR_ALPHA_Z = 9;
	/**
	 * Index of element's phase shift in x direction in array of constructor's parameters. Use with
	 * DBElementDescriptor as help for working with elements dynamically.
	 */
	public final static int PR_Q_X = 10;
	/**
	 * Index of element's phase shift in z direction in array of constructor's parameters. Use with
	 * DBElementDescriptor as help for working with elements dynamically.
	 */
	public final static int PR_Q_Z = 11;
	/**
	 * Index of element's dispersion in array of constructor's parameters. Use with
	 * DBElementDescriptor as help for working with elements dynamically.
	 */
	public final static int PR_DISPERSION = 12;
	/**
	 * Index of element's dispersion prime in array of constructor's parameters. Use with
	 * DBElementDescriptor as help for working with elements dynamically.
	 */
	public final static int PR_dDISPERSION = 13;
	private long minUpdateInterval;
	private Thread updateThread;
	private boolean readEnergyFromBendings = false;
	private boolean dataInvalidated = true;
	private boolean stop = false;
	private boolean orbitClosed = false;
	/** Constant <code>ENERGY="Energy"</code> */			

	public final static java.lang.String ENERGY = "Energy";
	/**
	 * Constructs DataBush element with specified name and default parameter's values.
	 *
	 * @param name a {@link java.lang.String} name of element
	 */
	public DataBushInfo(String name) {
		super(name);
		q= new PositionedData(name);
		beta= new PositionedData(name);
		alpha= new PositionedData(name);
		dispersion= new DispersionData(name);
		matrix= org.scictrl.mp.orbitcorrect.math.TransferMatrix.identity();
	}
	/**
	 * Constructs DataBush element with specified parameters.
	 *
	 * @param name a {@link java.lang.String} name of element
	 * @param virtual a boolean virtual flag
	 * @param energy a double initial energy
	 * @param fromBend a boolean readFromBendings flag
	 * @param orbitClosed a boolean tells if this <code>DataBush</code> form a closed orbit
	 * @param betaX a double machine function beta in x direction
	 * @param betaZ a double machine function beta in z direction
	 * @param alphaX a double machine function alpha in x direction
	 * @param alphaZ a double machine function alpha in z direction
	 * @param qX a double beam normalised phase in x direction
	 * @param qZ a double beam normalised phase in z direction
	 * @param disp a double machine function dispersion
	 * @param ddisp a double machine function dispersion
	 */
	public DataBushInfo(String name, boolean virtual, double energy, boolean fromBend, boolean orbitClosed, double betaX, double betaZ, double alphaX, double alphaZ, double qX, double qZ, double disp, double ddisp) {
		super(name, virtual);
		this.energy= energy;
		this.readEnergyFromBendings= fromBend;
		q= new PositionedData(name);
		beta= new PositionedData(name);
		alpha= new PositionedData(name);
		dispersion= new DispersionData(name);
		beta.set(betaX,betaZ);
		alpha.set(alphaX,alphaZ);
		q.set(qX,qZ);
		dispersion.set(disp,ddisp);
		this.orbitClosed= orbitClosed;
		matrix= org.scictrl.mp.orbitcorrect.math.TransferMatrix.identity();
	}
	int _connect(DataBushPackedException eh) {
		if (getVirtual()) return DBConst.RC_IS_VIRTUAL;
		if (connected) return DBConst.RC_NO_CHANGES;
		try {
			connector= owner.connectInfoProperty(this);
			if (connector==null) throw new Exception("NULL");
			if (DataBush.debug) System.out.println("DB["+getName()+"]> connected");
		} catch(Throwable e) {
			System.out.println("DB["+getName()+"]> failed connecting "+e.toString());
			return eh.pack(new DataBushException(this,e.toString(),DBConst.RC_BIND_FAILED,e));
		}
		connected= true;
		return DBConst.RC_COMPLETED_SUCCESSFULLY;
	}

	int _disconnect() {
		if (getVirtual()) return DBConst.RC_IS_VIRTUAL;
		if (!connected) return DBConst.RC_NO_CHANGES;
		owner.disconnect(connector);
		connector= null;
		connected= false;
		return DBConst.RC_COMPLETED_SUCCESSFULLY;
	}
	private void _setEnergy(double value) {
		if (energy==value) {
			return;
		}
		energy= value;
		if (isInitialized()) {

			owner.db.getCalculatorModelFactory().setEnergyToAll(energy);

			// invalidate dependant magnets
			Iterator<AbstractCalibratedMagnet> it= owner.ent.calMagnets.iterator();
			AbstractCalibratedMagnet m;
			while (it.hasNext())
				if (!((m=it.next()) instanceof AbstractBending)) m.invalidateDataWithEnergy();
		}
	}
	int _update(DataBushPackedException eh) {
		lastActionResult= DBConst.RC_COMPLETED_SUCCESSFULLY;
		if (getVirtual()) {
			lastActionResult= DBConst.RC_IS_VIRTUAL;
			if (readEnergyFromBendings) {
				_setEnergy(readEnergyFromBendings());
			}
		} else {
			if (!connected) return eh.pack(new DataBushException(this,DBString.RC_DEVICE_NOT_CONNECTED,lastActionResult= DBConst.RC_DEVICE_NOT_CONNECTED));
			if (!dataInvalidated) lastActionResult= DBConst.RC_NO_CHANGES;
			else {
				try{
					_setEnergy(connector.get());
				} catch (Throwable e) {
					lastActionResult= DBConst.RC_REMOTE_EXCEPTION;
					return eh.pack(new DataBushException(this,e.toString(),DBConst.RC_REMOTE_EXCEPTION));
				}
			}
		}
		return lastActionResult;
	}
	int _updateLinearOptics(DataBushPackedException eh) {
		return owner.linOp.update();
	}

	private void a_setEnergy(double value) {
		if (energy==value) {
			return;
		}
		energy=value;

		owner.db.getCalculatorModelFactory().setEnergyToAll(energy);

		Iterator<AbstractCalibratedMagnet> it= owner.ent.calMagnets.iterator();
		AbstractCalibratedMagnet m;
		while (it.hasNext())
			if (!((m=it.next()) instanceof AbstractBending)) m.a_update(m.getCurrent());

	}

	/**
	 * Performs on event update with energy from connected device or from bendings.
	 * New energy is set internaly and distributed to magnets.
	 * Does not trigger linear optics calculations
	 * @return
	 */
	boolean a_update() {
		double d=0.0;
		if (!getVirtual()) {
			if (connected && connector.getLatestReceivedValue()!=null) {
				if (energy!=(d=connector.getLatestReceivedValue())) {
					a_setEnergy(d);
					return true;
				}
			}
		} else {
			if (readEnergyFromBendings) {
				if (energy!=(d=readEnergyFromBendings())) {
					a_setEnergy(d);
					return true;
				}
			}
		}
		return false;
	}
	void a_updateLinearOptics() {
		owner.log.debug("UPDATE Optics request");
		a_updateMe= true;
		synchronized (updateThread) {
			updateThread.notify();
		}
	}

	boolean _updateOnEvent(Double data) {
		if (!getVirtual()) {
			if (energy!=data) {
				a_setEnergy(data);
				return true;
			}
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public void notifyDataUpdate(Double data) {
		if (owner.getUpdateMode()==DBConst.UPDATE_ON_EVENT) {
			boolean success=_updateOnEvent(data);
			if (success) {
				owner.notifyDBInfoDataUpdate();
			}
		} else {
			invalidateData();
		}

	}
	@Override
	void clear() {
		_disconnect();
		stop= true;
		if (updateThread!=null) {
			updateThread.interrupt();
		}
		super.clear();
	}
/**
 * {@inheritDoc}
 *
 * Returns not initialized clone of this element. All data is copied to the clone.
 */
@Override
public Object clone() {
	DataBushInfo o= (DataBushInfo)super.clone();
	o.matrix= (org.scictrl.mp.orbitcorrect.math.TransferMatrix)matrix.clone();
	o.q= (PositionedData)q.clone();
	o.beta= (PositionedData)beta.clone();
	o.alpha= (PositionedData)alpha.clone();
	return o;
}
/**
 * {@inheritDoc}
 *
 * Connects element to InfoServerBean device.
 * Returns the success code of operation.
 * @see #disconnect()
 */
@Override
public int connect() throws IllegalStateException, DataBushPackedException {
	if (!isInitialized()) throw new IllegalStateException(DBString.ISE_EL_NOT_INIT);
	int r= DBConst.RC_COMPLETED_SUCCESSFULLY;
	DataBushPackedException eh= new DataBushPackedException();
//	synchronized(owner.lock) {
		r= _connect(eh);
//	}
	owner.lEnt.deviceConnected(new ConnectionReportEvent(this,DBString.DEV_CONN,r));
	eh.throwIt();
	return r;
}
/**
 * {@inheritDoc}
 *
 * Returns the descriptor object for this element. <code>DBElementDescriptor</code> helps
 * manipulating DataBush elements dynamically.
 * @see org.scictrl.mp.orbitcorrect.DBElementDescriptor
 */
@Override
public DBElementDescriptor descriptor() {
	return DBElementDescriptor.ELDES_DBINFO;
}
/**
 * {@inheritDoc}
 *
 * Disconnects element from InfoServerBean device.
 * Returns the success code of operation.
 * @see #connect()
 */
@Override
public int disconnect() throws IllegalStateException {
	if (!isInitialized()) throw new IllegalStateException(DBString.ISE_EL_NOT_INIT);
	int r= DBConst.RC_COMPLETED_SUCCESSFULLY;
//	synchronized(owner.lock) {
		r= _disconnect();
//	}
	owner.lEnt.deviceDisconnected(new ConnectionReportEvent(this,DBString.DEV_DISC,r));
	return r;
}
/**
 * {@inheritDoc}
 *
 * Returns the code of element's type.
 * @see org.scictrl.mp.orbitcorrect.DBConst
 * @see org.scictrl.mp.orbitcorrect.DBElementDescriptor
 */
@Override
public int elType() {
	return DBElementDescriptor.EL_DBINFO;
}
/**
 * Returns closed orbit solution for alpha-function.
 *
 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PositionedData} closed orbit solution for alpha-function
 */
public PositionedData getAlpha() {
		return alpha;
}
/**
 * Returns closed orbit solution for beta-function.
 *
 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PositionedData} closed orbit solution for beta-function
 */
public PositionedData getBeta() {
	return beta;
}
/**
 * Returns closed orbit solution for horizontal momentum dispersion function.
 *
 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.DispersionData} closed orbit solution for horizontal momentum dispersion function
 */
public DispersionData getDispersion() {
	return dispersion;
}
/**
 * Returns the energy value for this element.
 *
 * @return a double the energy value for this element.
 */
public double getEnergy() {
	return energy;
}
	/**
	 * This method returns energy server, which is object that gets necessary data and calculate energy.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.IDataConnector} object
	 */
	public IDataConnector<Double> getConnector() {
		return connector;
	}
/**
 * This method return transfer matrix of the system.
 *
 * @see org.scictrl.mp.orbitcorrect.math.TransferMatrix
 * @return a {@link org.scictrl.mp.orbitcorrect.math.TransferMatrix} object
 */
public org.scictrl.mp.orbitcorrect.math.TransferMatrix getMatrix() {
	return matrix;
}
/**
 * Delay between two updating cannot be below one limits, which can be specified.
 *
 * @see #setMinUpdateInterval
 * This method return this limits.
 * @return long
 */
public long getMinUpdateInterval() {
	return minUpdateInterval;
}
/**
 *This method return beam normalised phase
 *
 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PositionedData} object
 */
public PositionedData getQ() {
	return q;
}
/**
 * If <code>DataBushInfo</code> is virtual, then, for simulation manners only, energy is
 * read from bendings.
 *
 * @return boolean <code>True</code> if this should be done, otherwise <code>false</code>.
 */
public boolean getReadEnergyFromBendings() {
	return readEnergyFromBendings;
}
/**
* This method specify which <code>DataBushHandler</code> is an owner of this element. Consequently this method tell us which
* <code>DataBush</code> is an owner of it. If energy should be read from bendings, this method
* do this and save value to field <code>energy</code>.
*
* @param <code>DataBushHandler</code>
* @throws <code>DataBushInitializationException</code> if this element already has an owner
*
* @see <code>DataBushHandler</code>
* @see <code>DataBushInitializationException</code>
*/
@Override
void init(DataBushHandler owner) throws DataBushInitializationException {
	super.init(owner);
	if (getVirtual()&&readEnergyFromBendings) energy=readEnergyFromBendings();
	owner.db.getCalculatorModelFactory().setEnergyToAll(energy);
	Iterator<AbstractCalibratedMagnet> it= owner.ent.calMagnets.iterator();
	AbstractCalibratedMagnet m;
	while (it.hasNext())
		if (!((m=it.next()) instanceof AbstractBending)) m.a_update(m.getPowerSupply().getCurrent());
	owner.linOp.update();

	updateThread= newUpdateThread();
	updateThread.start();
}
/**
 * {@inheritDoc}
 *
 * This method invalidate data which means that at least one data in this object is not consistent with
 * other object's data. It is necessary to update this object or apply (if possible).
 * @see #update
 */
@Override
public void invalidateData() {
	dataInvalidated=true;
	if (isInitialized()) owner.linOp.invalidateData();
}
/**
 * {@inheritDoc}
 *
 * Test if element is connected.
 */
@Override
public boolean isConnected() {
	return connected;
}
/**
 * {@inheritDoc}
 *
 * This method tests if data is invalidated.
 * @see #invalidateData
 */
@Override
public boolean isDataInvalidated() {
	return dataInvalidated;
}
/**
 * <p>isReadEnergyFromBendings.</p>
 *
 * @return a boolean
 */
public boolean isReadEnergyFromBendings() {
	return readEnergyFromBendings;
}
/**
 * Gets flag that orbit closes on itself after one revolution.
 *
 * @return boolean <code>True</code> if closes.
 */
public boolean isOrbitClosed() {
	return orbitClosed;
}
/**
 *
 * @return java.lang.Thread
 */
private Thread newUpdateThread() {
	return new Thread() {
		public int r=0;
		@Override
		public void run() {
			while (!stop) {
				while (a_updateMe) {
					if (stop) return;
					synchronized (owner.lock) {
						a_updateMe= false;
						r= owner.linOp.update();
					}
//					owner.lEnt.updatePerformed(new ActionReportEvent(this,DBString.UPP,DataBushEvent.T_UPDATE_PERFORMED,i));
					if (r!=DBConst.RC_NO_CHANGES) owner.lEnt.machineFunctionsChanged(new DataBushEvent(owner.db,DBString.MACHINE_FUNCTIONS_CHANGE));
					if (r==DBConst.RC_INCONSISTANT_DATA) owner.lEnt.inconsistentData(new DataBushEvent(owner.db,DBString.RC_INCONSISTENT_DATA_NO_SOLUTION));
/*					synchronized (owner) {
						owner.putOnHold();
						r= DBConst.RC_COMPLETED_SUCCESSFULLY;
						a_updateMe= false;
						if (a_energyChanged) {
							a_energyChanged=false;
							owner.setEnergyToDB(energy);
						}
						r= owner.updateDataBushInfo();
						owner.updateLine();
						owner.putOffHold();
					}
					if (r<=DBConst.RC_COMPLETED_SUCCESSFULLY) owner.lEnt.betaChanged(new DataBushEvent(this,"beta functions changed on event"));
					*/
					try {
						sleep(minUpdateInterval);
					} catch (InterruptedException e) {}
				}
				synchronized (this) {
					try {
						wait();
					} catch (InterruptedException e) {}
				}
			}
		}
	};
}
/**
 * Reads energy from bendings and sets the energy to DataBushInfo and distributes
 * to DataBush magnets. This operation is valid only if the DataBushInfo is virtual.
 * otherwize IllegalStateException is thrown.
 */
private double readEnergyFromBendings() {
	Iterator<AbstractBending> it= owner.ent.bendings.iterator();
	double e=0.0;
	while (it.hasNext()) e+= it.next().getEnergy();
	return e/owner.ent.bendings.size();
}
/**
 * This method sets beam alpha function.
 *
 * @param alphaX a double
 * @param alphaZ a double
 * @throws java.lang.IllegalStateException if any.
 */
public void setAlpha(double alphaX, double alphaZ) throws IllegalStateException {
	if (!orbitClosed) {
		alpha.set(alphaX,alphaZ);
	} else throw new IllegalStateException(DBString.ISE_ONDBINFO);
}
/**
 * This method sets beam beta function.
 *
 * @param betaX a double
 * @param betaZ a double
 * @throws java.lang.IllegalStateException if any.
 */
public void setBeta(double betaX, double betaZ) throws IllegalStateException {
	if (!orbitClosed) {
		beta.set(betaX,betaZ);
	} else throw new IllegalStateException(DBString.ISE_ONDBINFO);
}
/**
 * This method sets beam dispersion function.
 *
 * @param d a double
 * @param dp a double
 * @throws java.lang.IllegalStateException if any.
 */
public void setDispersion(double d, double dp) throws IllegalStateException {
	if (!orbitClosed) {
		dispersion.set(d,dp);
	} else throw new IllegalStateException(DBString.ISE_ONDBINFO);
}
/**
 * Sets energy to DataBushInfo element and distribute energy to magnets.
 * This operation is valid only if DataBushInfo is virtual. Otherwize
 * IllegalStateException is thrown.
 *
 * @param value a double
 * @throws java.lang.IllegalStateException if any.
 */
public void setEnergy(double value) throws IllegalStateException {
	if (!isInitialized()||getVirtual()) {
		_setEnergy(value);
	} else throw new IllegalStateException(DBString.ISE_ONDBINFO);
}
/**
 * Reads energy from bendings and sets the energy to DataBushInfo and distributes
 * to DataBush magnets. This operation is valid only if the DataBushInfo is virtual.
 * otherwize IllegalStateException is thrown.
 *
 * @throws java.lang.IllegalStateException if any.
 */
public void setEnergyFromBendings() throws IllegalStateException {
	if ((isInitialized())&&getVirtual()) {
		_setEnergy(energy=readEnergyFromBendings());
	} else throw new IllegalStateException(DBString.ISE_ONDBINFO);
}
/**
* This method sets transfer matrix.
* @see org.scictrl.mp.orbitcorrect.math.TransferMatrix
*/
void setMatrix(org.scictrl.mp.orbitcorrect.math.TransferMatrix m) {
	matrix= m;
}
/**
 * This method sets minimal update interval.
 *
 * @see #getMinUpdateInterval
 * @param newValue a long
 */
public void setMinUpdateInterval(long newValue) {
	this.minUpdateInterval = newValue;
}
/**
 * Sets flag that orbit closes on itself after one revolution. This preprty can not be set
 * when element is initialized and DataBush is operational.
 *
 * @param newOrbitClosed boolean does orbit closes on itself after one revolution
 * @throws java.lang.IllegalStateException if any.
 */
public void setOrbitClosed(boolean newOrbitClosed) throws IllegalStateException {
	if (!isInitialized())
		orbitClosed = newOrbitClosed;
	else throw new IllegalStateException(DBString.ISE_EL_INIT);
}
/**
 * This method sets beam normalized phaze.
 *
 * @param qX a double
 * @param qZ a double
 * @throws java.lang.IllegalStateException if any.
 */
public void setQ(double qX, double qZ) throws IllegalStateException {
	if (!orbitClosed) {
		q.set(qX,qZ);
	} else throw new IllegalStateException(DBString.ISE_ONDBINFO);
}
/**
 * This method specify if energy is read from bendings.
 *
 * @see #getReadEnergyFromBendings
 * @param newValue a boolean
 */
public void setReadEnergyFromBendings(boolean newValue) {
	this.readEnergyFromBendings = newValue;
}
/**
 * {@inheritDoc}
 *
 * <p>setWith.</p>
 * @see AbstractDataBushElement#setWith(Object[])
 */
@Override
public void setWith(Object[] par) throws IllegalStateException {
	super.setWith(par);
	if (par[PR_ENERGY]!=null) energy= ((Double)par[PR_ENERGY]).doubleValue();
	if (par[PR_READ_E_FROM_BEND]!=null) readEnergyFromBendings= ((Boolean)par[PR_READ_E_FROM_BEND]).booleanValue();
	if (par[PR_ORBIT_IS_CLOSED]!=null) orbitClosed= ((Boolean)par[PR_ORBIT_IS_CLOSED]).booleanValue();
	double a=0.0;
	double b=0.0;
	if (par[PR_BETA_X]!=null) a= ((Double)par[PR_BETA_X]).doubleValue();
	if (par[PR_BETA_Z]!=null) b= ((Double)par[PR_BETA_Z]).doubleValue();
	beta.set(a,b);
	a=0.0;
	b=0.0;
	if (par[PR_ALPHA_X]!=null) a= ((Double)par[PR_ALPHA_X]).doubleValue();
	if (par[PR_ALPHA_Z]!=null) b= ((Double)par[PR_ALPHA_Z]).doubleValue();
	alpha.set(a,b);
	a=0.0;
	b=0.0;
	if (par[PR_DISPERSION]!=null) a= ((Double)par[PR_DISPERSION]).doubleValue();
	if (par[PR_dDISPERSION]!=null) b= ((Double)par[PR_dDISPERSION]).doubleValue();
	dispersion.set(a,b);
}
/**
 * {@inheritDoc}
 *
 * Returns a String that represents the value of this object.
 */
@Override
public String toString() {
	return super.toString()+" "+descriptor().getParameterTag(PR_ENERGY)+"="+energy+" "+descriptor().getParameterTag(PR_READ_E_FROM_BEND)+"="+readEnergyFromBendings+" "+descriptor().getParameterTag(PR_ORBIT_IS_CLOSED)+"="+orbitClosed+" "+descriptor().getParameterTag(PR_BETA_X)+"="+beta.x()+" "+descriptor().getParameterTag(PR_BETA_Z)+"="+beta.z()+" "+descriptor().getParameterTag(PR_ALPHA_X)+"="+alpha.x()+" "+descriptor().getParameterTag(PR_ALPHA_Z)+"="+alpha.z()+" "+descriptor().getParameterTag(PR_DISPERSION)+"="+dispersion.d()+" "+descriptor().getParameterTag(PR_dDISPERSION)+"="+dispersion.dp()+" >"+DBConst.EOL;
}
/**
 * {@inheritDoc}
 *
 * Reads energy from InfoServerBean and updates machine functions with new energy.
 * Return code is returned.
 * Exceptions are thrown in following situations:
 * <ul>
 * <li> <code>IllegalStateException</code> if</li>
 * <li> element is not initialized in DataBush</li>
 * <li> DataBush is not in UPDATE_MANUAL update mode</li>
 * <li> DataBush is in ACCESS_ALL_DATA access mode</li>
 * <li> <code>PackedDBException if</code>
 * <li> element is not virtual and is not connected</li>
 * <li> remote exception is caught</li>
 * </ul>
 */
@Override
public int update() throws IllegalStateException, DataBushPackedException {
	if (!isInitialized()) {
		lastActionResult= DBConst.RC_NOT_INITIALIZED;
		throw new IllegalStateException(DBString.ISE_EL_NOT_INIT);
	}
	int i;
	DataBushPackedException eh= new DataBushPackedException();
//	synchronized (owner.lock) {
		if (owner.updateMode!=DBConst.UPDATE_MANUAL) throwISE(DBString.ISE_DB_UP_MODE);
		i= _update(eh);
//	}
	owner.lEnt.updatePerformed(new ActionReportEvent(this,DBString.UPP,i,eh));
	if (i<=DBConst.RC_IS_VIRTUAL) owner.lEnt.machineFunctionsChanged(new DataBushEvent(owner.db,DBString.MACHINE_FUNCTIONS_CHANGE));
	eh.throwIt();
	return i;
}
/**
 * Reads energy from InfoServerBean and updates machine functions with new energy.
 * Return code is returned.
 * Exceptions are thrown in following situations:
 * <ul>
 *   <li> <code>IllegalStateException</code> if element is not initialized in DataBush</li>
 *   <li> DataBush is not in UPDATE_MANUAL update mode</li>
 *   <li> DataBush is in ACCESS_ALL_DATA access mode</li>
 *   <li> <code>PackedDBException if</code> element is not virtual and is not connected remote exception is caught</li>
 * </ul>
 *
 * @return a int
 * @throws java.lang.IllegalStateException if any.
 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
 */
public int updateLinearOptics() throws IllegalStateException, DataBushPackedException {
	if (!isInitialized()) {
		lastActionResult= DBConst.RC_NOT_INITIALIZED;
		throw new IllegalStateException(DBString.ISE_EL_NOT_INIT);
	}
	int i;
	DataBushPackedException eh= new DataBushPackedException();
//	synchronized (owner.lock) {
		if (owner.updateMode!=DBConst.UPDATE_MANUAL) throwISE(DBString.ISE_DB_UP_MODE);
		i= _updateLinearOptics(eh);
//	}
	owner.lEnt.machineFunctionsChanged(new DataBushEvent(owner.db,DBString.MACHINE_FUNCTIONS_CHANGE));
	if (i==DBConst.RC_INCONSISTANT_DATA) owner.lEnt.inconsistentData(new DataBushEvent(owner.db,DBString.RC_INCONSISTENT_DATA_NO_SOLUTION));
	eh.throwIt();
	return i;
}
}
