package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.ActionReportEvent;
import org.scictrl.mp.orbitcorrect.ConnectionReportEvent;
import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.DBElementDescriptor;
import org.scictrl.mp.orbitcorrect.DBString;
import org.scictrl.mp.orbitcorrect.DataBushEvent;
import org.scictrl.mp.orbitcorrect.DataBushException;
import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.model.IDataConnector;



/**
 * This class represent beam position monitor. It hold position of electron beam.
 * It connects to BeamPositionMonitor and reads beam displacment
 *
 * @author igor@scictrl.com
 */
public class BPMonitor extends AbstractOpticalElement implements IBindedElement<Double[]>, AbstractUpdateableElement {

	/** Constant <code>HOR_POSITION="horPosition"</code> */
	public final static java.lang.String HOR_POSITION = "horPosition";
	/** Constant <code>VER_POSITION="verPosition"</code> */
	public final static java.lang.String VER_POSITION = "verPosition";

	/** Data is invalidated. */
	protected boolean dataInvalidated = true;

	private boolean connected= false;
	private PositionedData beamPos;
	private IDataConnector<Double[]> connector;

	/**
	 * Constructs a <code>BPMonitor</code> with specified name and default parameters vaues
	 *
	 * @param name a {@link java.lang.String} object
	 */
	public BPMonitor(String name) {
		super(name);
		beamPos= new PositionedData(name,this);
	}
	/**
	 * Constructs the <code>BPMonitor</code> with specified parameters.
	 *
	 * @param name a {@link java.lang.String} object
	 * @param virtual a boolean
	 * @param position a double
	 * @param relpos a double
	 * @param relFrom a {@link java.lang.String} object
	 */
	public BPMonitor(String name, boolean virtual, double position, double relpos, String relFrom) {
		super(name,virtual,position,relpos,relFrom);
		beamPos= new PositionedData(name,this);
	}
	int _connect(DataBushPackedException eh) {
		if (getVirtual()) return DBConst.RC_IS_VIRTUAL;
		if (connected) return DBConst.RC_NO_CHANGES;
		try {
			connector= owner.connectBPM(this);
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
	int _update(DataBushPackedException eh) {
		if (getVirtual()) return lastActionResult=DBConst.RC_IS_VIRTUAL;
		if (!connected) return eh.pack(new DataBushException(this,DBString.RC_DEVICE_NOT_CONNECTED,lastActionResult=DBConst.RC_DEVICE_NOT_CONNECTED));
		if (!dataInvalidated) return lastActionResult=DBConst.RC_NO_CHANGES;
		try{
			beamPos.set(connector.get());
			dataInvalidated= false;
		} catch (Throwable e) {
			return eh.pack(new DataBushException(this,e.toString(),lastActionResult=DBConst.RC_REMOTE_EXCEPTION));
		}
		return lastActionResult=DBConst.RC_COMPLETED_SUCCESSFULLY;
	}
	boolean _updateOnEvent(Double[] d) {
		if (getVirtual()||!connected) return false;
		dataInvalidated= false;
		try {
			if (isNewPos(d[0],beamPos.x())||isNewPos(d[1],beamPos.z())) {
				beamPos.set(d[0],d[1]);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	/** {@inheritDoc} */
	@Override
	public void notifyDataUpdate(Double[] data) {
		if (owner.getUpdateMode()==DBConst.UPDATE_ON_EVENT) {
			boolean success=_updateOnEvent(data);
			if (success) {
				owner.notifyBPMDataUpdate();
			}
		} else {
			invalidateData();
		}
	}
	@Override
	void clear() {
		_disconnect();
		super.clear();
	}
	/**
	 * {@inheritDoc}
	 *
	 * <p>clone.</p>
	 * @see AbstractDataBushElement#clone
	 */
	@Override
	public Object clone() {
		BPMonitor o= (BPMonitor)super.clone();
		o.beamPos= (PositionedData)beamPos.clone(o);
		// FIXME: this workaround for bad desing, clonned object in fact
		// inherits live connection, so it's sort of connected, but
		// connection can be controlled only if object is
		// inide DataBush structure, but after clone it is not.
		// THis is inconsistant situation.
		//o.connected= false;
		return o;
	}
	/**
	 * {@inheritDoc}
	 *
	 * Connects element to InfoServerBean device.
	 * Returns the success code of operation.
	 * @see disconnect()
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
	 * <p>descriptor.</p>
	 * @see AbstractDataBushElement#descriptor()
	 */
	@Override
	public DBElementDescriptor descriptor() {
		return DBElementDescriptor.ELDES_BPM;
	}
	/**
	 * {@inheritDoc}
	 *
	 * Disconnects element from InfoServerBean device.
	 * Returns the success code of operation.
	 * @see connect()
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
		return DBElementDescriptor.EL_BPM;
	}
	/**
	 * This method return the position of the electron beam, that was measured before last <code>update()</code> call.
	 *
	 * @return PositionedData
	 */
	public PositionedData getBeamPos() {
		return beamPos;
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
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method tests if <code>BPMonitor</code> is connected.
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
	 *
	 *
	 * @return boolean
	 * @param newPos double
	 * @param oldPos double
	 */
	private boolean isNewPos(double newPos, double oldPos) {
		if ((Double.isNaN(oldPos)||Double.isNaN(newPos))&&(!(Double.isNaN(oldPos)&&Double.isNaN(newPos)))) return true;
		return Math.abs(newPos-oldPos)>owner.db.bPMPositionPrecision;
	}
	/**
	 * This method sets value, that holds beam position to specified value.
	 *
	 * @param x double	position in x direction
	 * @param z double	position in z direction
	 * @throws java.lang.IllegalStateException if any.
	 */
	public void setBeamPosTo(double x, double z) throws IllegalStateException {
		if (isInitialized()) throw new IllegalStateException("Can not set beam position if element inside DataBush!");
		beamPos.set(x,z);
	}
	/**
	 * {@inheritDoc}
	 *
	 * Returns a String that represents the value of this object.
	 */
	@Override
	public String toString() {
		return super.toString()+" >"+DBConst.EOL;
	}
	/**
	 * {@inheritDoc}
	 *
	 *This method reads electron beam position from <code>BeamPositionMonitorBean</code> and save it to private field of this class
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
		if (i==DBConst.RC_COMPLETED_SUCCESSFULLY) owner.lEnt.beamChanged(new DataBushEvent(owner.db,DBString.BEAM_CHANGE));
		eh.throwIt();
		return i;
	}
	/**
	 * This method tests if BPM is usable.
	 *
	 * @return <code>true</code> if it is.
	 */
	public boolean test() {
		if (getVirtual()) return true;
		if (!connected) return false;
		return connector.test();
	}
	/**
	 * <p>isUseable.</p>
	 *
	 * @return a boolean
	 */
	public boolean isUseable() {
		if (getVirtual()) return true;
		if (!connected) return false;
		return connector.isReady();
	}
}
