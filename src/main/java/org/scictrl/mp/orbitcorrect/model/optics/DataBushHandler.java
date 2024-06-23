package org.scictrl.mp.orbitcorrect.model.optics;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scictrl.mp.orbitcorrect.ActionReportEvent;
import org.scictrl.mp.orbitcorrect.ConnectionReportEvent;
import org.scictrl.mp.orbitcorrect.ControlSystemException;
import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.DBString;
import org.scictrl.mp.orbitcorrect.DataBushEvent;
import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.accessories.DefaultBeamSimulator;
import org.scictrl.mp.orbitcorrect.model.IDataConnector;
import org.scictrl.mp.orbitcorrect.model.IWriteConnector;
/**
 * <p>DataBushHandler class.</p>
 *
 * @author igor@scictrl.com
 */
public class DataBushHandler {

	/** Thread lock object. */
	public Object lock;

	/** DataBush object */
	public DataBush db;

	/** DataBush structure object. */
	public DataBushEnt ent;

	/** Structure listener. */
	public IListenerEnt lEnt;

	/** linear optics calculator object. */
	public LinearOpticsOperator linOp;

	/**
	  * This field indicates when should DataBush be updated (when update method is manually demanded or when event happen).
	  */

	public int updateMode= DBConst.UPDATE_MANUAL;

	/** Set should fire events. */
	public boolean setFiresEvent = true;

	/** Operation has been aborted. */
	public boolean aborted = false;

	/** Logger. */
	public Logger log= LogManager.getLogger(getClass());

	/**
	 * <code>DataBushHandler</code> constructor.
	 *
	 * @param db a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} object
	 * @param lEnt a {@link org.scictrl.mp.orbitcorrect.model.optics.IListenerEnt} object
	 * @param synchLock a {@link java.lang.Object} object
	 */
	public DataBushHandler(DataBush db, IListenerEnt lEnt, Object synchLock) {
		super();
		this.lock= synchLock;
		this.db=db;
		this.lEnt= lEnt;
	}
	/**
	 * This method sets such current to <code>PowerSupply</code>'s of <code>PowerSupplyList</code> l
	 * that desired magnetic strength on magnets will be achieved.
	 *
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.PowerSupplyList} object
	 * @param eh a {@link org.scictrl.mp.orbitcorrect.DataBushPackedException} object
	 * @return a int
	 */
	public int _applyMagnets(PowerSupplyList l, DataBushPackedException eh) {
		if (aborted) return DBConst.RC_ABORTED;
		Iterator<PowerSupply> it= l.iterator();
		while (it.hasNext()) {
			if (aborted) return DBConst.RC_ABORTED;
			try {
				it.next()._applyFromMagnets();
			} catch (InconsistentDataException e) {
				eh.pack(e);
			}
		}
		return eh.maxCode();
	}
	/**
	 * This method sets the same current to <code>PowerSupplyBean</code>'s of <code>PowerSupply</code>'s of <code>PowerSupplyList</code> l
	 * as is on their <code>PowerSupply</code>'s.
	 *
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.PowerSupplyList} object
	 * @param eh a {@link org.scictrl.mp.orbitcorrect.DataBushPackedException} object
	 * @return a int
	 */
	public int _applyPowerSupplies(PowerSupplyList l, DataBushPackedException eh) {
		if (aborted) return DBConst.RC_ABORTED;
		Iterator<PowerSupply> it= l.iterator();
		while (it.hasNext()) {
			if (aborted) return DBConst.RC_ABORTED;
			it.next()._applyCurrent(eh);
		}
		return eh.maxCode();
	}
	/**
	 * This method call method <code>_connect(DBExceptionHolder)</code> on all <code>BPMonitor</code>'s from <code>BPMonitorList</code>
	 * and consequently connect all <code>BPMonitor</code>'s on <code>BeamPositionMonitorBean</code>.
	 *
	 * @see BPMonitor#_connect(DataBushPackedException)
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.BPMonitorList} object
	 * @param eh a {@link org.scictrl.mp.orbitcorrect.DataBushPackedException} object
	 * @return a int
	 */
	public int _connect(BPMonitorList l, DataBushPackedException eh) {
		if (aborted) return DBConst.RC_ABORTED;
		Iterator<BPMonitor> it= l.iterator();
		while (it.hasNext()) {
			if (aborted) return DBConst.RC_ABORTED;
			it.next()._connect(eh);
		}
		return eh.maxCode();
	}
	/**
	 * <p>clear.</p>
	 */
	public void clear() {
		//nothing to do
	}
	/**
	 * This method call method <code>_connect(DBExceptionHolder)</code> on all <code>PowerSupplay</code>'s from <code>PowerSupplyList</code>
	 * and consequently connect all <code>PowerSupplay</code>'s on <code>PowerSupplyBean</code>.
	 *
	 * @see PowerSupply#_connect(DataBushPackedException)
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.PowerSupplyList} object
	 * @param eh a {@link org.scictrl.mp.orbitcorrect.DataBushPackedException} object
	 * @return a int
	 */
	public int _connect(PowerSupplyList l, DataBushPackedException eh) {
		if (aborted) return DBConst.RC_ABORTED;
		Iterator<PowerSupply> it= l.iterator();
		while (it.hasNext()) {
			if (aborted) return DBConst.RC_ABORTED;
			it.next()._connect(eh);
		}
		return eh.maxCode();
	}
	/**
	 * This method call method <code>_disconnect(DBExceptionHolder)</code> on all <code>BPMonitor</code>'s from <code>BPMonitorList</code>
	 * and consequently disconnect all <code>BPMonitor</code>'s on <code>BeamPositionMonitorBean</code>.
	 *
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.BPMonitorList} object
	 * @return a int
	 */
	public int _disconnect(BPMonitorList l) {
		if (aborted) return DBConst.RC_ABORTED;
		int i=0,j=0;
		Iterator<BPMonitor> it= l.iterator();
		while (it.hasNext()) {
			if (aborted) return DBConst.RC_ABORTED;
			i= it.next()._disconnect();
				if (i>j) j=i;
		}
		return j;
	}
	/**
	 * This method call method <code>_disconnect(DBExceptionHolder)</code> on all <code>PowerSupplay</code>'s from <code>PowerSupplyList</code>
	 * and consequently disconnect all <code>PowerSupplay</code>'s on <code>PowerSupplyBean</code>.
	 *
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.PowerSupplyList} object
	 * @return a int
	 */
	public int _disconnect(PowerSupplyList l) {
		if (aborted) return DBConst.RC_ABORTED;
		int i=0,j=0;
		Iterator<PowerSupply> it= l.iterator();
		while (it.hasNext()) {
			if (aborted) return DBConst.RC_ABORTED;
			i= it.next()._disconnect();
				if (i>j) j=i;
		}
		return j;
	}
	/**
	 * This method call method <code>_update(DBExceptionHolder)</code> on all <code>BPMonitor</code>'s from <code>BPMonitorList</code>
	 * and consequently update all <code>BPMonitor</code>'s on <code>BeamPositionMonitorBean</code>.
	 *
	 * @see BPMonitor#_update(DataBushPackedException)
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.BPMonitorList} object
	 * @param eh a {@link org.scictrl.mp.orbitcorrect.DataBushPackedException} object
	 * @return a int
	 */
	public int _updateBPMs(BPMonitorList l, DataBushPackedException eh) {
		if (aborted) return DBConst.RC_ABORTED;
		Iterator<BPMonitor> it= l.iterator();
		while (it.hasNext()) {
			if (aborted) return DBConst.RC_ABORTED;
			it.next()._update(eh);
		}
		return eh.maxCode();
	}
	/**
	 * This method call method <code>_update(DBExceptionHolder)</code> on <code>AbstractCalibratedMagnet</code>'s from <code>MagnetList</code>'s
	 * of dependent magnet of <code>PowerSupplay</code>'s from <code>PowerSupplayList</code>,
	 * if they are members of <code>AbstractProtectedList</code> l.
	 *
	 * @see AbstractCalibratedMagnet#_update(DataBushPackedException)
	 * @param pl a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractProtectedList} object
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.PowerSupplyList} object
	 * @param eh a {@link org.scictrl.mp.orbitcorrect.DataBushPackedException} object
	 * @return a int
	 */
	public int _updateMagnets(AbstractProtectedList<? extends AbstractMagnetElement> pl, PowerSupplyList l, DataBushPackedException eh) {
		if (aborted) return DBConst.RC_ABORTED;
		Iterator<PowerSupply> it= l.iterator();
		Iterator<AbstractMagnetElement> mit;
		AbstractMagnetElement mel;
		while (it.hasNext()) {
			if (aborted) return DBConst.RC_ABORTED;
			mit= it.next().getDependingMagnets().iterator();
			while (mit.hasNext()) {
				if (aborted) return DBConst.RC_ABORTED;
				if (((mel=mit.next()) instanceof AbstractCalibratedMagnet)&&(pl.contains(mel))) {
					((AbstractCalibratedMagnet)mel)._update(eh);
				}
			}
		}
		return eh.maxCode();
	}
	/**
	 * This method call method <code>_update(DBExceptionHolder)</code> on all <code>PowerSupplay</code>'s from <code>PowerSupplyList</code>
	 * and consequently update all <code>PowerSupplay</code>'s on <code>PowerSupplyBean</code>.
	 *
	 * @see PowerSupply#_update(DataBushPackedException)
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.PowerSupplyList} object
	 * @param eh a {@link org.scictrl.mp.orbitcorrect.DataBushPackedException} object
	 * @return a int
	 */
	public int _updatePowerSupplies(PowerSupplyList l, DataBushPackedException eh) {
		if (aborted) return DBConst.RC_ABORTED;
		Iterator<PowerSupply> it= l.iterator();
		while (it.hasNext()) {
			if (aborted) return DBConst.RC_ABORTED;
			it.next()._update(eh);
		}
		return eh.maxCode();
	}

	/**
	 * <p>_updateSpecials.</p>
	 *
	 * @param ent a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBushEnt} object
	 * @param eh a {@link org.scictrl.mp.orbitcorrect.DataBushPackedException} object
	 * @return a int
	 * @throws java.lang.IllegalStateException if any.
	 */
	public int _updateSpecials(DataBushEnt ent, DataBushPackedException eh) throws IllegalStateException {
		if (aborted) return DBConst.RC_ABORTED;

		ent.dataBushInfo._update(eh);

		if (aborted) return DBConst.RC_ABORTED;

		if (ent.rfGenerator!=null) {
			ent.rfGenerator._update(eh);
		}

		return eh.maxCode();
	}
	/**
	 * This method applaies magnets and power supplaies by calling methods <code>_applyMagnets</code>
	 * and <code>_applyPowerSupplies</code>. Both are called on all suitable <code>DataBushHandler</code>'s <code>DataBush</code>'s list.
	 *
	 * @see #_applyMagnets
	 * @see #_applyPowerSupplies
	 * @return a int
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public int apply() throws InconsistentDataException, IllegalStateException, DataBushPackedException {
		aborted=false;
		DataBushPackedException eh= new DataBushPackedException();
	//	synchronized (lock) {
			if (db.getStatus()==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);

			_applyMagnets(ent.magnets.getPowerSupplies(),eh);
			_applyPowerSupplies(ent.powerSupplies,eh);

			if (ent.rfGenerator!=null) {
				ent.rfGenerator._applyFrequency(eh);
			}
	//	}
		int j=(aborted ? DBConst.RC_ABORTED : eh.maxCode());
		lEnt.applyPerformed(new ActionReportEvent(this.db,DBString.APP,j,eh));
		eh.throwIt();
		return j;
	}
	/**
	 * This method applies magnets by calling method <code>_applyMagnets</code>.
	 * Method is called on <code>PowerSupplays</code> that are dependent of magnets in parameter specified list.
	 * @see #_applyMagnets
	 * @see #_applyPowerSupplies
	 */
	int apply(AbstractProtectedMagnetList<? extends AbstractMagnetElement> l) throws InconsistentDataException, IllegalStateException, DataBushPackedException {
		aborted=false;
		DataBushPackedException eh= new DataBushPackedException();
	//	synchronized (lock) {
			if ((db.getStatus()==DBConst.DB_EMPTY)) throw new IllegalStateException(DBString.ISE_DB_STATE);

			_applyMagnets(l.getPowerSupplies(),eh);
	//	}
		int i=(aborted ? DBConst.RC_ABORTED : eh.maxCode());
		lEnt.applyPerformed(new ActionReportEvent(this.db,DBString.APP,i));
		eh.throwIt();
		return i;
	}
	/**
	 * This method applies magnets by calling method <code>_applyMagnets</code>.
	 * Method is called on patrameter specified <code>PowerSupplay</code> list.
	 *
	 * @see #_applyMagnets
	 * @see #_applyPowerSupplies
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.PowerSupplyList} object
	 * @return a int
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public int apply(PowerSupplyList l) throws InconsistentDataException, IllegalStateException, DataBushPackedException {
		aborted=false;
		DataBushPackedException eh= new DataBushPackedException();
	//	synchronized (lock) {
			if (db.getStatus()==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);

			_applyPowerSupplies(l,eh);
	//	}
		int i=(aborted ? DBConst.RC_ABORTED : eh.maxCode());
		lEnt.applyPerformed(new ActionReportEvent(this.db,DBString.APP,i,eh));
		eh.throwIt();
		return i;
	}
	/**
	 * This method applies correctors by calling method <code>_applyFromMagnets</code> on them.
	 *
	 * @param cor a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @return a int
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	public int applyFast(ElementList<AbstractDataBushElement> cor) throws InconsistentDataException, IllegalStateException, DataBushPackedException, ControlSystemException {
		int i=0,j=0,k=0;
		DataBushPackedException eh= new DataBushPackedException();

		if (db.getStatus()==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);

		@SuppressWarnings("unchecked")
		IWriteConnector<Double>[] props= new IWriteConnector[cor.size()];
		double[] values= new double[cor.size()];

		Object o;

		boolean corr=false;
		boolean freq= false;

	//	ElementList psList= new ElementList(cor.size());
		Iterator<AbstractDataBushElement> it= cor.iterator();
		while (it.hasNext()) {
			o=it.next();
			if (o instanceof AbstractCorrector) {
				AbstractCorrector c;
				boolean result= true;
				try {
					result = (c=(AbstractCorrector)o).getPowerSupply().isUseable();
				} catch (Exception e) {
					e.printStackTrace();
					throw new ControlSystemException(ControlSystemException.TEST_FAILED_MESSAGE+((AbstractCorrector)o).getPowerSupply().getName()+".",e);
				}
				if (!result) {
					throw new ControlSystemException(ControlSystemException.TEST_FAILED_MESSAGE+((AbstractCorrector)o).getPowerSupply().getName()+".");
				}
				props[i]=c.getPowerSupply().getConnector();
				k=c.getPowerSupply()._applyFromMagnets();
				values[i]=c.getPowerSupply().getCurrent();
				if (k>j) j=k;
				i++;
				corr=true;
			} else if (o instanceof RFGenerator) {
				RFGenerator c;
				boolean result= true;
				try {
					result = (c=(RFGenerator)o).isUseable();
				} catch (Exception e) {
					e.printStackTrace();
					throw new ControlSystemException(ControlSystemException.TEST_FAILED_MESSAGE+((RFGenerator)o).getName()+".",e);
				}
				if (!result) {
					throw new ControlSystemException(ControlSystemException.TEST_FAILED_MESSAGE+((RFGenerator)o).getName()+".");
				}
				props[i]=c.getConnector();
				values[i]=c.getFrequency();
				i++;
				freq=true;
			} else {
				throw new InconsistentDataException("Element '"+o+"'in list is not AbstractCorrector or RFGenerator, fast set aborted!");
			}
		}

		db.getControlSystemEngine().setFast(props,values);


		lEnt.applyPerformed(new ActionReportEvent(this.db,DBString.APP,j,eh));
		if (corr) lEnt.fieldChanged(new DataBushEvent(this.db));
		if (freq) lEnt.rfChanged(new DataBushEvent(this.db));
		eh.throwIt();
		return j;
	}
	/**
	 * This method connects <code>DataBush</code>'s <code>PowerSupplyList</code>, <code>BPMonitorList</code> and <code>DataBushInfo</code> to control system.
	 *
	 * @return a int
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public int connect() throws IllegalStateException, DataBushPackedException {
		aborted=false;
		DataBushPackedException eh= new DataBushPackedException();
	//	synchronized (lock) {
			if (db.getStatus()==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);

			_connect(ent.powerSupplies,eh);
			_connect(ent.monitors,eh);
			ent.dataBushInfo._connect(eh);
			if (ent.rfGenerator!=null) {
				ent.rfGenerator._connect(eh);
			}
	//	}
		int j=(aborted ? DBConst.RC_ABORTED : eh.maxCode());
		lEnt.deviceConnected(new ConnectionReportEvent(this.db,DBString.DEV_CONN,j));
		eh.throwIt();
		return j;
	}
	/**
	 * This method connects parameter specified <code>BPMonitorList</code> to control system.
	 *
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.BPMonitorList} object
	 * @return a int
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public int connect(BPMonitorList l) throws IllegalStateException, DataBushPackedException {
		int j=0;
		aborted=false;
		DataBushPackedException eh= new DataBushPackedException();
	//	synchronized (lock) {
			if (db.getStatus()==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);

			j= _connect(l,eh);

	//	}
		j=(aborted ? DBConst.RC_ABORTED : j);
		lEnt.deviceConnected(new ConnectionReportEvent(this.db,DBString.DEV_CONN,j));
		eh.throwIt();
		return j;
	}
	/**
	 * This method connects parameter specified <code>PowerSupplyList</code> to control system.
	 *
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.PowerSupplyList} object
	 * @return a int
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public int connect(PowerSupplyList l) throws IllegalStateException, DataBushPackedException {
		int j=0;
		aborted=false;
		DataBushPackedException eh= new DataBushPackedException();
	//	synchronized (lock) {
			if (db.getStatus()==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);

			j= _connect(ent.powerSupplies,eh);

	//	}
		j=(aborted ? DBConst.RC_ABORTED : j);
		lEnt.deviceConnected(new ConnectionReportEvent(this.db,DBString.DEV_CONN,j));
		eh.throwIt();
		return j;
	}
	/**
	 * This method on control system finds <code>RWDoubleProperty</code>, which represent current (which current define parameter string),
	 * and return it.
	 * This property is also added to <code>psMonitor</code>.
	 *
	 * @return RWDoubleProperty
	 * @param ps a {@link org.scictrl.mp.orbitcorrect.model.optics.PowerSupply} object
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	public IWriteConnector<Double> connectPowerSuply(PowerSupply ps) throws ControlSystemException {
		IWriteConnector<Double> c= (IWriteConnector<Double>)db.getControlSystemEngine().connect(ps);
		if (Double.class.isAssignableFrom(c.dataType())) {
			return c;
		}
		c.destroy();
		throw new ControlSystemException("["+ps.getName()+"] internal error: data type does not match!");
	}


	/**
	 * <p>connectFrequencyProperty.</p>
	 *
	 * @param rf a {@link org.scictrl.mp.orbitcorrect.model.optics.RFGenerator} object
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.IWriteConnector} object
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	public IWriteConnector<Double> connectFrequencyProperty(RFGenerator rf) throws org.scictrl.mp.orbitcorrect.ControlSystemException {

		IWriteConnector<Double> c= (IWriteConnector<Double>)db.getControlSystemEngine().connect(rf);
		if (Double.class.isAssignableFrom(c.dataType())) {
			return c;
		}
		c.destroy();
		throw new ControlSystemException("["+rf.getName()+"] internal error: data type does not match!");
	}
	/**
	 * This method on control system finds <code>DoubleProperty</code>, which represent energy,
	 * and return it.
	 * This property is also added to <code>psMonitor</code>.
	 *
	 * @return DoubleProperty
	 * @param info a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBushInfo} object
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	public IDataConnector<Double> connectInfoProperty(DataBushInfo info) throws org.scictrl.mp.orbitcorrect.ControlSystemException {
		IDataConnector<Double> c= db.getControlSystemEngine().connect(info);
		if (Double.class.isAssignableFrom(c.dataType())) {
			return c;
		}
		c.destroy();
		throw new ControlSystemException("["+info.getName()+"] internal error: data type does not match!");
	}

	/**
	 * This method disconnects <code>DataBush</code>'s <code>PowerSupplyList</code>, <code>BPMonitorList</code> and <code>DataBushInfo</code> from control system.
	 *
	 * @return a int
	 * @throws java.lang.IllegalStateException if any.
	 */
	public int disconnect() throws IllegalStateException {
		int i=0,j=0;
		aborted=false;
	//	synchronized (lock) {
		if (db.getStatus()==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);

			i= _disconnect(ent.powerSupplies);
				if (i>j) j=i;
			i= _disconnect(ent.monitors);
				if (i>j) j=i;
			i= ent.dataBushInfo._disconnect();
				if (i>j) j=i;
			if (ent.rfGenerator!=null) {
				i= ent.rfGenerator._disconnect();
					if (i>j) j=i;
			}
	//	}
		j=(aborted ? DBConst.RC_ABORTED : j);
		lEnt.deviceDisconnected(new ConnectionReportEvent(this.db,DBString.DEV_DISC,j));
		return j;
	}
	/**
	 * This method disconnects parameter specified <code>BPMonitorList</code> from control system.
	 *
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.BPMonitorList} object
	 * @return a int
	 * @throws java.lang.IllegalStateException if any.
	 */
	public int disconnect(BPMonitorList l) throws IllegalStateException {
		int j=0;
		aborted=false;
		if (db.getStatus()==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);
			j= _disconnect(l);
		j=(aborted ? DBConst.RC_ABORTED : j);
		lEnt.deviceDisconnected(new ConnectionReportEvent(this.db,DBString.DEV_DISC,j));
		return j;
	}
	/**
	 * This method disconnects parameter specified <code>PowerSupplyList</code> from control system.
	 *
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.PowerSupplyList} object
	 * @return a int
	 * @throws java.lang.IllegalStateException if any.
	 */
	public int disconnect(PowerSupplyList l) throws IllegalStateException {
		int j=0;
		aborted=false;
		if (db.getStatus()==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);
			j= _disconnect(l);
		j=(aborted ? DBConst.RC_ABORTED : j);
		lEnt.deviceDisconnected(new ConnectionReportEvent(this.db,DBString.DEV_DISC,j));
		return j;
	}
	/**
	 * This method returns <code>AbstractDataBushElement</code>, whose name is parametr specified name, if is
	 * contained in any of <code>DataBushEnt</code> lists: powerSupplies, optics, or is its <code>DataBushInfo</code>.
	 *
	 * @param name a {@link java.lang.String} object
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement} object
	 */
	public AbstractDataBushElement getElement(String name) {
		AbstractDataBushElement el= ent.optics.get(name);
		if (el==null) el= (AbstractDataBushElement)ent.binded.get(name);
		return el;
	}
	/**
	 * This method returns <code>PowerSupply</code>, whose name is parametr specified name, if is
	 * contained in <code>DataBushEnt</code> list powerSupplies.
	 *
	 * @param name a {@link java.lang.String} object
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PowerSupply} object
	 */
	public PowerSupply getPowerSupply(String name) {
		return ent.powerSupplies.get(name);
	}
	/**
	 * This method returns update mode.
	 *
	 * @return a int
	 */
	public int getUpdateMode() {
		return updateMode;
	}
	/**
	 * <p>isSetFiresEvent.</p>
	 *
	 * @return boolean
	 */
	public boolean isSetFiresEvent() {
		return setFiresEvent;
	}
	/**
	 * <p>notifyBPMDataUpdate.</p>
	 */
	public void notifyBPMDataUpdate() {

		if (aborted) return;

		try {
			if (updateMode==DBConst.UPDATE_ON_EVENT) {
				lEnt.beamChanged(new DataBushEvent(db,"beam changed on event"));
			}
		} catch (Throwable t) {
			t.printStackTrace();
			log.error("Notify failed "+t.toString(), t);
		}
	}

	/**
	 * <p>notifyRFDataUpdate.</p>
	 */
	public void notifyRFDataUpdate() {

		if (aborted) return;

		try {
			if (updateMode==DBConst.UPDATE_ON_EVENT) {
				lEnt.rfChanged(new DataBushEvent(db,"RF frequency changed on event"));
			}
		} catch (Throwable t) {
			t.printStackTrace();
			log.error("Notify failed "+t.toString(), t);
		}
	}

	/**
	 * <p>notifyPSDataUpdate.</p>
	 *
	 * @param ps a {@link org.scictrl.mp.orbitcorrect.model.optics.PowerSupply} object
	 */
	public void notifyPSDataUpdate(PowerSupply ps) {

		if (aborted) return;

		try {
			if (updateMode==DBConst.UPDATE_ON_EVENT) {
				boolean info=false;
				if (ps.getDependingMagnets().get(0) instanceof AbstractBending
						 && ent.dataBushInfo.isReadEnergyFromBendings()) {
					info= ent.dataBushInfo.a_update();
				}
				lEnt.fieldChanged(new DataBushEvent(db,"field changed on event"));
				if (info || ps.getDependingMagnets().get(0) instanceof AbstractBending || ps.getDependingMagnets().get(0) instanceof Quadrupole) {
					ent.dataBushInfo.a_updateLinearOptics();
				}

			} else { //is not in UPDAT_ON_EVENT mode, only setting flag is necessary
				if (ps.getDependingMagnets().get(0) instanceof AbstractBending
						 && ent.dataBushInfo.isReadEnergyFromBendings()) {
					ent.dataBushInfo.invalidateData();
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
			log.error("Notify failed "+t.toString(), t);
		}
	}

	/**
	 * <p>notifyDBInfoDataUpdate.</p>
	 */
	public void notifyDBInfoDataUpdate() {

		if (aborted) return;

		try {
			if (updateMode==DBConst.UPDATE_ON_EVENT) {
				lEnt.fieldChanged(new DataBushEvent(db,"field changed on event"));
				ent.dataBushInfo.a_updateLinearOptics();
			}
		} catch (Throwable t) {
			t.printStackTrace();
			log.error("Notify failed "+t.toString(), t);
		}
	}

	/**
	 * <p>Setter for the field <code>setFiresEvent</code>.</p>
	 *
	 * @param newSetFiresEvent boolean
	 */
	public void setSetFiresEvent(boolean newSetFiresEvent) {
		setFiresEvent = newSetFiresEvent;
	}
	/**
	 * <p>Setter for the field <code>updateMode</code>.</p>
	 *
	 * @param mode byte
	 */
	public void setUpdateMode(byte mode) {
		updateMode= mode;
	}
	/**
	 * <p>Setter for the field <code>updateMode</code>.</p>
	 *
	 * @param mode byte
	 */
	public void setUpdateMode(int mode) {
		updateMode= mode;
	}
	/**
	 * <p>simulateBPMs.</p>
	 */
	public void simulateBPMs() {
	//	synchronized(lock) {
	//		if (updateMode==DBConst.UPDATE_ON_EVENT) return;

		if (db.getBeamSimulator()==null) db.setBeamSimulator(new DefaultBeamSimulator());

		double[] hor= db.getBeamSimulator().simulateHorizontal(db);
		double[] ver= db.getBeamSimulator().simulateVertical(db);

		for (int i=0; i<ent.monitors.size(); i++) {
			ent.monitors.get(i).getBeamPos().set(hor[i],ver[i]);
		}

		lEnt.beamChanged(new DataBushEvent(db,"Beam simulated"));
	}
	/**
	 * <p>testPrecision.</p>
	 *
	 * @return double
	 * @param value double
	 */
	public double testPrecision(double value) {
		if (Math.abs(value)<db.getPositionPrecision()) return 0.0;
		else return value;
	}
	/**
	 * <p>update.</p>
	 *
	 * @return a int
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public int update() throws IllegalStateException, DataBushPackedException {
		aborted=false;
		DataBushPackedException eh= new DataBushPackedException();
		int lo=0;
	//	synchronized (lock) {
		while (true) {
			if ((updateMode!=DBConst.UPDATE_MANUAL)||(db.getStatus()==DBConst.DB_EMPTY))
				throw new IllegalStateException(DBString.ISE_DB_UP_MODE);

			if (aborted) break;
			if (DataBush.debug) System.out.println("DB> updating BPMs "+new java.util.Date(System.currentTimeMillis()).toString());
			_updateBPMs(ent.monitors,eh);
			if (aborted) break;
			if (DataBush.debug) System.out.println("DB> updating PSies "+new java.util.Date(System.currentTimeMillis()).toString());
			_updatePowerSupplies(ent.powerSupplies,eh);
			if (aborted) break;
			if (DataBush.debug) System.out.println("DB> updating bendings "+new java.util.Date(System.currentTimeMillis()).toString());
			Iterator<AbstractBending> bit= ent.bendings.iterator();
			while (bit.hasNext()) {
				if (aborted) break;
				bit.next()._update(eh);
			}
			if (aborted) break;
			if (DataBush.debug) System.out.println("DB> updating DBInfo and RFGenerator "+new java.util.Date(System.currentTimeMillis()).toString());
			_updateSpecials(ent,eh);

			if (aborted) break;
			AbstractCalibratedMagnet mag;
			Iterator<AbstractCalibratedMagnet> it= ent.calMagnets.iterator();
			if (DataBush.debug) System.out.println("DB> updating magnets "+new java.util.Date(System.currentTimeMillis()).toString());
			while (it.hasNext()) {
				if (aborted) break;
				//if (!((mag=it.next()) instanceof AbstractBending)) {
				mag=it.next();
					if (aborted) break;
					mag._update(eh);
				//}
			}
			if (aborted) break;
			if (DataBush.debug) System.out.println("DB> updating linear optics "+new java.util.Date(System.currentTimeMillis()).toString());
			lo= ent.dataBushInfo._updateLinearOptics(eh);
			if (DataBush.debug) System.out.println("DB> updating DONE "+new java.util.Date(System.currentTimeMillis()).toString());
			break;
		}

		int j=(aborted ? DBConst.RC_ABORTED : eh.maxCode());
		lEnt.updatePerformed(new ActionReportEvent(this.db,DBString.UPP,j,eh));
		if (j<=DBConst.RC_IS_VIRTUAL) {
			lEnt.fieldChanged(new DataBushEvent(this.db,DBString.FIELD_CHANGE));
			lEnt.machineFunctionsChanged(new DataBushEvent(db,DBString.MACHINE_FUNCTIONS_CHANGE));
			lEnt.beamChanged(new DataBushEvent(db,DBString.BEAM_CHANGE));
			lEnt.rfChanged(new DataBushEvent(db,"RF frequency changed on event"));
		}
		if (lo==DBConst.RC_INCONSISTANT_DATA) lEnt.inconsistentData(new DataBushEvent(db,DBString.RC_INCONSISTENT_DATA_NO_SOLUTION));
		eh.throwIt();
		return j;
	}
	/**
	 * <p>update.</p>
	 *
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractProtectedMagnetList} object
	 * @return a int
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public int update(AbstractProtectedMagnetList<? extends AbstractMagnetElement> l) throws IllegalStateException, DataBushPackedException {
		aborted=false;
		DataBushPackedException eh= new DataBushPackedException();
			if ((updateMode!=DBConst.UPDATE_MANUAL)||(db.getStatus()==DBConst.DB_EMPTY))
				throw new IllegalStateException(DBString.ISE_DB_UP_MODE);
			_updateMagnets(l,l.getPowerSupplies(),eh);
		int j=(aborted ? DBConst.RC_ABORTED : eh.maxCode());
		lEnt.updatePerformed(new ActionReportEvent(this.db,DBString.UPP,j,eh));
		if (j<DBConst.RC_IS_VIRTUAL) lEnt.fieldChanged(new DataBushEvent(this.db,DBString.FIELD_CHANGE));
		eh.throwIt();
		return j;
	}
	/**
	 * <p>update.</p>
	 *
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.BindedList} object
	 * @return a int
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public int update(BindedList l) throws IllegalStateException, DataBushPackedException {
		aborted=false;
		DataBushPackedException eh= new DataBushPackedException();
		if ((updateMode!=DBConst.UPDATE_MANUAL)||(db.getStatus()==DBConst.DB_EMPTY))
			throw new IllegalStateException(DBString.ISE_DB_UP_MODE);
		_updateBPMs(ent.monitors,eh);
		_updatePowerSupplies(ent.powerSupplies,eh);
		_updateSpecials(ent,eh);
		int j=(aborted ? DBConst.RC_ABORTED : eh.maxCode());
		lEnt.updatePerformed(new ActionReportEvent(this.db,DBString.UPP,j,eh));
		if (j<DBConst.RC_IS_VIRTUAL) {
			lEnt.fieldChanged(new DataBushEvent(this.db,DBString.FIELD_CHANGE));
			lEnt.beamChanged(new DataBushEvent(db,DBString.BEAM_CHANGE));
			lEnt.rfChanged(new DataBushEvent(db,"RF frequency changed on event"));
		}
		eh.throwIt();
		return j;
	}
	/**
	 * <p>update.</p>
	 *
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.BPMonitorList} object
	 * @return a int
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public int update(BPMonitorList l) throws IllegalStateException, DataBushPackedException {
		aborted=false;
		DataBushPackedException eh= new DataBushPackedException();
		if ((updateMode!=DBConst.UPDATE_MANUAL)||(db.getStatus()==DBConst.DB_EMPTY))
			throw new IllegalStateException(DBString.ISE_DB_UP_MODE);
		_updateBPMs(l,eh);
		int j=(aborted ? DBConst.RC_ABORTED : eh.maxCode());
		lEnt.updatePerformed(new ActionReportEvent(this.db,DBString.UPP,j,eh));
		if (j<DBConst.RC_IS_VIRTUAL) lEnt.beamChanged(new DataBushEvent(db,DBString.BEAM_CHANGE));
		eh.throwIt();
		return j;
	}
	/**
	 * <p>update.</p>
	 *
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.OpticsList} object
	 * @return a int
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public int update(OpticsList l) throws IllegalStateException, DataBushPackedException {
		aborted=false;
		DataBushPackedException eh= new DataBushPackedException();
		if ((updateMode!=DBConst.UPDATE_MANUAL)||(db.getStatus()==DBConst.DB_EMPTY))
			throw new IllegalStateException(DBString.ISE_DB_UP_MODE);
		_updateBPMs(ent.monitors,eh);
		_updateMagnets(ent.magnets,ent.magnets.getPowerSupplies(),eh);
		int j=(aborted ? DBConst.RC_ABORTED : eh.maxCode());
		lEnt.updatePerformed(new ActionReportEvent(this.db,DBString.UPP,j,eh));
		if (j<DBConst.RC_IS_VIRTUAL) {
			lEnt.beamChanged(new DataBushEvent(db,DBString.BEAM_CHANGE));
		}
		eh.throwIt();
		return j;
	}
	/**
	 * <p>update.</p>
	 *
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.PowerSupplyList} object
	 * @return a int
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public int update(PowerSupplyList l) throws IllegalStateException, DataBushPackedException {
		aborted=false;
		DataBushPackedException eh= new DataBushPackedException();
		if ((updateMode!=DBConst.UPDATE_MANUAL)||(db.getStatus()==DBConst.DB_EMPTY))
			throw new IllegalStateException(DBString.ISE_DB_UP_MODE);
		_updatePowerSupplies(l,eh);
		int j=(aborted ? DBConst.RC_ABORTED : eh.maxCode());
		lEnt.updatePerformed(new ActionReportEvent(this.db,DBString.UPP,j,eh));
		eh.throwIt();
		return j;
	}

	/**
	 * <p>connectBPM.</p>
	 *
	 * @param el a {@link org.scictrl.mp.orbitcorrect.model.optics.BPMonitor} object
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.IDataConnector} object
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	public IDataConnector<Double[]> connectBPM(BPMonitor el) throws ControlSystemException {
		IDataConnector<Double[]> c= db.getControlSystemEngine().connect(el);
		if (Double[].class.isAssignableFrom(c.dataType())) {
			return c;
		}

		c.destroy();
		throw new ControlSystemException("["+el.getName()+"] internal error: data type does not match!");
	}
	/**
	 * <p>disconnect.</p>
	 *
	 * @param connector a {@link org.scictrl.mp.orbitcorrect.model.IDataConnector} object
	 */
	public void disconnect(IDataConnector<?> connector) {
		connector.destroy();
	}
}
