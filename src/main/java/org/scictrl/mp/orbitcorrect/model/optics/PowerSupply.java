package org.scictrl.mp.orbitcorrect.model.optics;

import java.util.Iterator;

import org.scictrl.mp.orbitcorrect.ActionReportEvent;
import org.scictrl.mp.orbitcorrect.ConnectionReportEvent;
import org.scictrl.mp.orbitcorrect.ControlSystemException;
import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.DBElementDescriptor;
import org.scictrl.mp.orbitcorrect.DBString;
import org.scictrl.mp.orbitcorrect.DataBushException;
import org.scictrl.mp.orbitcorrect.DataBushInitializationException;
import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.ICurrentApplyModel;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.model.IWriteConnector;


/**
 * This class represents power supply. It connects to <code>PowerSupplyBean</code> and can read
 * current from it and set it. Can also distribute current to dependant magnets or read from it.
 *
 * @author igor@scictrl.com
 */
public class PowerSupply extends AbstractDataBushElement implements IBindedElement<Double>, AbstractUpdateableElement, IApplyableElement {

	/** Constant <code>PR_CURRENT=2</code> */
	public static final int	PR_CURRENT=2;

	/** Constant <code>CURRENT="current"</code> */
	public final static String CURRENT = "current";

	/** Default max value. */
	public double defaultMaxValue= 1000;

	/** Default min value. */
	public double defaultMinValue= -1000;

	/** Data invalidated flag. */
	protected boolean dataInvalidated = true;

	private double current;
	private boolean connected=false;
	private IWriteConnector<Double> connector;
	private MagnetList dependingMagnets;
	//private double rbCurrent;
/**
 * Constructs a <code>PowerSupply</code> with specified name and default parameters vaues
 *
 * @param name a {@link java.lang.String} object
 */
public PowerSupply(String name) {
	super(name);
}
/**
 * Constructs a <code>PowerSupply</code> with specified parameters
 *
 * @param name a {@link java.lang.String} object
 * @param virtual a boolean
 * @param current a double
 */
public PowerSupply(String name, boolean virtual, double current) {
	super(name, virtual);
	this.current= current;
}
int _applyCurrent(DataBushPackedException eh) {
	if (getVirtual()) return lastActionResult=DBConst.RC_IS_VIRTUAL;
	if (!connected) return eh.pack(new DataBushException(this,DBString.RC_DEVICE_NOT_CONNECTED,lastActionResult=DBConst.RC_DEVICE_NOT_CONNECTED));
	if (!dataInvalidated) return lastActionResult=DBConst.RC_NO_CHANGES;
	try {
		connector.set(current);
		dataInvalidated= false;
		return lastActionResult=DBConst.RC_COMPLETED_SUCCESSFULLY;
	} catch(Throwable e) {
		return eh.pack(new DataBushException(this,e.toString(),lastActionResult=DBConst.RC_REMOTE_EXCEPTION));
	}
}
/**
 * This method sets current that is calculated from desired magnet strength at depending magnets.
 * @return int
 */
int _applyFromMagnets() throws InconsistentDataException {
	ICurrentApplyModel model= owner.db.getCurrentApplyModel();
	model.reset();
	int i=0,j=0;
	AbstractMagnetElement mel;
	Iterator<AbstractMagnetElement> it= dependingMagnets.iterator();
	while (it.hasNext()) {
		if ((mel= it.next()) instanceof AbstractCalibratedMagnet) {
			i= ((AbstractCalibratedMagnet)mel)._apply(model);
			if (i<j) j=i;
		}
	}
	if (model.getValueCount()>0) _setCurrent(model.getCurrent());
	return j;
}
/**
 *
 * @return int
 */
int _connect(DataBushPackedException eh) {
	if (getVirtual()) return DBConst.RC_IS_VIRTUAL;
	if (connected) return DBConst.RC_NO_CHANGES;
	try {
		connector= owner.connectPowerSuply(this);
		if (connector==null) throw new Exception("NULL");
		if (DataBush.debug) System.out.println("DB["+getName()+"]> connected");
	} catch(Throwable e) {
		System.out.println("DB["+getName()+"]> failed connecting "+e.toString());
		return eh.pack(new DataBushException(this,e.toString(),DBConst.RC_BIND_FAILED));
	}
	connected= true;
	return DBConst.RC_COMPLETED_SUCCESSFULLY;
}
	/**
	 *
	 * @return int
	 */
	int _disconnect() {

		if (getVirtual()) return DBConst.RC_IS_VIRTUAL;
		if (!connected) return DBConst.RC_NO_CHANGES;
		owner.disconnect(connector);
		connector= null;
		connected= false;
		return DBConst.RC_COMPLETED_SUCCESSFULLY;

	}
	double _getCurrent() {
		return current;
	}
void _setCurrent(double value) {
	if (current== value) return;
	current= value;
	dataInvalidated= true;
	notifyListeners();
}
/**
 *
 */
int _update(DataBushPackedException eh) {
	if (getVirtual()) return lastActionResult=DBConst.RC_IS_VIRTUAL;
	if (!connected) return eh.pack(new DataBushException(this,DBString.RC_DEVICE_NOT_CONNECTED,lastActionResult=DBConst.RC_DEVICE_NOT_CONNECTED));
	if (!dataInvalidated) return lastActionResult=DBConst.RC_NO_CHANGES;
	try{
		current= connector.get();
//		rbCurrent= psBean.getReadback().get();
		dataInvalidated= false;
	} catch (Throwable e) {
		return eh.pack(new DataBushException(this,e.toString(),lastActionResult=DBConst.RC_REMOTE_EXCEPTION));
	}
	notifyListeners();
	return lastActionResult=DBConst.RC_COMPLETED_SUCCESSFULLY;
}


	boolean _updateOnEvent(Double d) {
		if (getVirtual()||!connected) return false;
		dataInvalidated= false;
		try {
			if (Math.abs(d-current)>owner.db.currentPrecision) {
				if (getName().contains("MB")||getName().contains("MQ")) {
					owner.log.debug("UPDATE "+getName());
				}
				current=d;
				AbstractMagnetElement mel;
				Iterator<AbstractMagnetElement> it= dependingMagnets.iterator();
				while (it.hasNext()) {
					if ((mel= it.next()) instanceof AbstractCalibratedMagnet) ((AbstractCalibratedMagnet)mel).a_update(current);
				}
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public void notifyDataUpdate(Double data) {
		if (owner.getUpdateMode()==DBConst.UPDATE_ON_EVENT) {
			boolean success=_updateOnEvent(data);
			if (success) {
				owner.notifyPSDataUpdate(this);
			}
		} else {
			invalidateData();
		}
	}




/**
 * This method sets current, that <code>PowerSupply</code> currently holds, to <code>PowerSupplyBean</code>.
 *
 * @return a int
 * @throws java.lang.IllegalStateException if any.
 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
 */
public int applyCurrent() throws IllegalStateException, DataBushPackedException {
	if (!isInitialized()) {
		lastActionResult= DBConst.RC_NOT_INITIALIZED;
		throw new IllegalStateException(DBString.ISE_EL_NOT_INIT);
	}
	int i;
	DataBushPackedException eh= new DataBushPackedException();
//	synchronized (owner.lock) {
		i= _applyCurrent(eh);
//	}
	owner.lEnt.updatePerformed(new ActionReportEvent(this,DBString.UPP,i,eh));
	eh.throwIt();
	return i;
}
/**
 *
 */
@Override
void clear() {
	_disconnect();
	if (dependingMagnets!=null) dependingMagnets.clear();
	super.clear();
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
	return DBElementDescriptor.ELDES_POWER_SUPPLY;
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
	return DBElementDescriptor.EL_POWER_SUPPLY;
}
/*
* This method return current, that <code>PowerSupply</code> currently hold.
*/
/**
 * <p>Getter for the field <code>current</code>.</p>
 *
 * @return a double
 */
public double getCurrent() {
	return current;
}
/**
 * This method returns field <code>psBean</code>. If <code>PowerSupply</code> is connected, this field is
 * property of control system <code>PowerSupplyBean</code> and change of it explicitly affect current.
 *
 * @return a {@link org.scictrl.mp.orbitcorrect.model.IWriteConnector} object
 */
public IWriteConnector<Double> getConnector() {
	return connector;
}
/**
 * This method return <code>MagnetList</code> with magnets, that are powered with power supply
 * which is represented by <code>PowerSupply</code>
 *
 * @return MagnetList
 */
public MagnetList getDependingMagnets() {
	return dependingMagnets;
}
/**
 * This method return maximum current, that can power supply safely exceed.
 *
 * @return a double
 * @throws java.lang.IllegalStateException if any.
 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
 */
public double getMaxCurrent() throws IllegalStateException, ControlSystemException {
	if (getVirtual() ) return defaultMaxValue;
	if (!connected) throw new IllegalStateException();
	return connector.getMaxValue();
}
/**
 * This method return minimum current, that can power supply exceed.
 *
 * @return a double
 * @throws java.lang.IllegalStateException if any.
 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
 */
public double getMinCurrent() throws IllegalStateException, ControlSystemException {
	if (getVirtual() ) return defaultMinValue;
	if (!connected) throw new IllegalStateException();
	return connector.getMinValue();
}
/*public double getRBCurrent() {
	return rbCurrent;
}*/
/**
* This method specify which <code>DataBushHandler</code> is an owner of this element. Consequently this method tell us which
* <code>DataBush</code> is an owner of it.
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
	dependingMagnets= new MagnetList(owner);
}
/**
 * {@inheritDoc}
 *
 * This method invalidate data which means that at least one data in this object is not consistent with
 * other object's data. It is necessary to update this object or apply (if possible).
 * @see #update
 * @see #applyCurrent
 */
@Override
public void invalidateData() {dataInvalidated=true;}
/**
 * {@inheritDoc}
 *
 * This method tests if <code>PowerSupply</code> is connected.
 * @see #connect
 * @see #disconnect
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
	 * This method tests if power supply is useable.
	 *
	 * @return <code>True</code> if is.
	 */
	public boolean isUseable() {
		if (getVirtual()) return true;
		if (!connected) return false;
		return this.connector.isReady();
	}
	/**
	 * This method tests if power supply.
	 *
	 * @return <code>True</code> if is.
	 */
	public boolean test() {
		if (getVirtual()) return true;
		if (!connected) return false;
		return this.connector.test();
	}
/**
 *
 */
private void notifyListeners() {
	AbstractMagnetElement mel;
	Iterator<AbstractMagnetElement> it= dependingMagnets.iterator();
	while (it.hasNext()) {
		if ((mel= it.next()) instanceof AbstractCalibratedMagnet) ((AbstractCalibratedMagnet)mel).invalidateData();
	}
}
/**
 * This method sets current.
 *
 * @param value a double
 */
public void setCurrent(double value) {
	_setCurrent(value);
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
	if (par[PR_CURRENT]!=null) current= ((Double)par[PR_CURRENT]).doubleValue();
}
/**
 * {@inheritDoc}
 *
 * Returns a String that represents the value of this object.
 */
@Override
public String toString() {
	return super.toString()+" "+descriptor().getParameterTag(PR_CURRENT)+"="+current+" >"+DBConst.EOL;
}
/**
 * {@inheritDoc}
 *
 * This method read current from <code>PowerSupplyBean</code> and save it to field current
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
	eh.throwIt();
	return i;
}
}
