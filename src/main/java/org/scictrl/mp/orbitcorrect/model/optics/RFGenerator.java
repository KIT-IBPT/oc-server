package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.ActionReportEvent;
import org.scictrl.mp.orbitcorrect.ConnectionReportEvent;
import org.scictrl.mp.orbitcorrect.ControlSystemException;
import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.DBElementDescriptor;
import org.scictrl.mp.orbitcorrect.DBString;
import org.scictrl.mp.orbitcorrect.DataBushEvent;
import org.scictrl.mp.orbitcorrect.DataBushException;
import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.model.IWriteConnector;


/**
 * This class represents RF signal generator. It should connect to control system device for generating main RF signal
 * for cavitiesin storage ring. Databush expects and uses frequency in units of <b>MHz</b>.
 *
 * @author igor@scictrl.com
 */
public class RFGenerator extends AbstractDataBushElement implements IBindedElement<Double>, AbstractUpdateableElement, IApplyableElement {

	/** Constant <code>PR_FREQUENCY=2</code> */
	public static final int	PR_FREQUENCY=2;

	/** Constant <code>FREQUENCY="frequency"</code> */
	public final static String FREQUENCY = "frequency";

	/** Default max frequency value.*/
	public double defaultMaxValue= 1000.0;
	/** Default min frequency value.*/
	public double defaultMinValue= 100.0;

	/** Data invalidated flag. */
	protected boolean dataInvalidated = true;

	private double frequency;
	private boolean connected=false;
	private IWriteConnector<Double> connector;
/**
 * Constructs a <code>RFGenerator</code> with specified name and default parameters vaues
 *
 * @param name a {@link java.lang.String} object
 */
public RFGenerator(String name) {
	super(name);
}
/**
 * Constructs a <code>RFGenerator</code> with specified parameters
 *
 * @param name a {@link java.lang.String} object
 * @param virtual a boolean
 * @param frequency a double
 */
public RFGenerator(String name, boolean virtual, double frequency) {
	super(name, virtual);
	this.frequency= frequency;
}
int _applyFrequency(DataBushPackedException eh) {
	if (getVirtual()) return lastActionResult=DBConst.RC_IS_VIRTUAL;
	if (!connected) return eh.pack(new DataBushException(this,DBString.RC_DEVICE_NOT_CONNECTED,lastActionResult=DBConst.RC_DEVICE_NOT_CONNECTED));
	if (!dataInvalidated) return lastActionResult=DBConst.RC_NO_CHANGES;
	try {
		System.out.println("SET "+frequency);
		connector.set(frequency);
		dataInvalidated= false;
		return lastActionResult=DBConst.RC_COMPLETED_SUCCESSFULLY;
	} catch(Throwable e) {
		return eh.pack(new DataBushException(this,e.toString(),lastActionResult=DBConst.RC_REMOTE_EXCEPTION));
	}
}
	int _connect(DataBushPackedException eh) {
		if (getVirtual()) return DBConst.RC_IS_VIRTUAL;
		if (connected) return DBConst.RC_NO_CHANGES;
		try {
			connector= owner.connectFrequencyProperty(this);
			if (connector==null) throw new Exception("NULL");
			if (DataBush.debug) System.out.println("DB["+getName()+"]> connected");
		} catch(Throwable e) {
			System.out.println("DB["+getName()+"]> failed connecting "+e.toString());
			return eh.pack(new DataBushException(this,e.toString(),DBConst.RC_BIND_FAILED));
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
/**
 *
 */
int _update(DataBushPackedException eh) {
	if (getVirtual()) return lastActionResult=DBConst.RC_IS_VIRTUAL;
	if (!connected) return eh.pack(new DataBushException(this,DBString.RC_DEVICE_NOT_CONNECTED,lastActionResult=DBConst.RC_DEVICE_NOT_CONNECTED));
	if (!dataInvalidated) return lastActionResult=DBConst.RC_NO_CHANGES;
	try{
		frequency= connector.get();
		dataInvalidated= false;
	} catch (Throwable e) {
		return eh.pack(new DataBushException(this,e.toString(),lastActionResult=DBConst.RC_REMOTE_EXCEPTION));
	}
	return lastActionResult=DBConst.RC_COMPLETED_SUCCESSFULLY;
}




	boolean _updateOnEvent(Double d) {
		if (getVirtual()||!connected) return false;
		dataInvalidated= false;
		try {
			if (d!=frequency) {
				//System.out.println("A UPDATE "+d);
				frequency=d;
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
				owner.notifyRFDataUpdate();
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
public int applyFrequency() throws IllegalStateException, DataBushPackedException {
	if (!isInitialized()) {
		lastActionResult= DBConst.RC_NOT_INITIALIZED;
		throw new IllegalStateException(DBString.ISE_EL_NOT_INIT);
	}
	int i;
	DataBushPackedException eh= new DataBushPackedException();
//	synchronized (owner.lock) {
		i= _applyFrequency(eh);
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
	return DBElementDescriptor.ELDES_RF_GENERATOR;
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
	return DBElementDescriptor.EL_RF_GENERATOR;
}
/*
* This method return frequency setting at this object. Use <code>isInvalidated()</code> to find out, if value is valid.
*/
/**
 * <p>Getter for the field <code>frequency</code>.</p>
 *
 * @return a double
 */
public double getFrequency() {
	return frequency;
}
	/**
	 * If <code>RFGenerator</code> is connected, returns control system property for RFGenerator.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.IWriteConnector} object
	 */
	public IWriteConnector<Double> getConnector() {
		return connector;
	}
	/**
	 * This method return maximum current, that can power supply safely exceed.
	 *
	 * @return a double
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	public double getMaxFrequency() throws IllegalStateException, ControlSystemException {
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
	public double getMinFrequency() throws IllegalStateException, ControlSystemException {
		if (getVirtual() ) return defaultMinValue;
		if (!connected) throw new IllegalStateException();
		return connector.getMinValue();
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method invalidate data which means that at least one data in this object is not consistent with
	 * other object's data. It is necessary to update this object or apply (if possible).
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
 * Sets local reference for frequency.
 *
 * @param value a double
 */
public void setFrequency(double value) {
	if (frequency== value) return;
	frequency= value;
	dataInvalidated= true;
	if (isInitialized()) {
		if (owner.setFiresEvent) owner.lEnt.rfChanged(new DataBushEvent(this,"RF frequency changed"));
	}
}
/**
 * {@inheritDoc}
 *
 * <p>setWith.</p>
 * @see AbstractDataBushElement#setWith
 */
@Override
public void setWith(Object[] par) throws IllegalStateException {
	super.setWith(par);
	if (par[PR_FREQUENCY]!=null) frequency= ((Double)par[PR_FREQUENCY]).doubleValue();
}
/**
 * {@inheritDoc}
 *
 * Returns a String that represents the value of this object.
 */
@Override
public String toString() {
	return super.toString()+" "+descriptor().getParameterTag(PR_FREQUENCY)+"="+frequency+" >"+DBConst.EOL;
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
}
