package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.ActionReportEvent;
import org.scictrl.mp.orbitcorrect.ControlSystemException;
import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.DBString;
import org.scictrl.mp.orbitcorrect.DataBushEvent;
import org.scictrl.mp.orbitcorrect.DataBushInitializationException;
import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.EntryNotFound;
import org.scictrl.mp.orbitcorrect.ICalculatorModelFactory;
import org.scictrl.mp.orbitcorrect.ICurrentApplyModel;


/**
 * <code>AbstractCorrector</code> steers magnet, which gives kick to the beam, specified by the angle.
 *
 * @author igor@scictrl.com
 */
public abstract class AbstractCorrector extends AbstractCalibratedMagnet {
	private double angle;	//corrector angle in mrad
	//private double rbAngle,
	private double defaultMaxAngle, defaultMinAngle;
	/** Constant <code>PR_ANGLE=8</code> */
	public static final int	PR_ANGLE= 8;
/**
 * Constructs <code>AbstractCorrector</code> with specified name and default parameter's values.
 *
 * @param name a {@link java.lang.String} the name of abstract corrector
 */
public AbstractCorrector(String name) {
	super(name);
}
/**
 * Constructs the <code>AbstractCorrector</code> with specified parameters.
 *
 * @param name a {@link java.lang.String} object
 * @param virtual a boolean
 * @param position a double
 * @param relpos a double
 * @param relFrom a {@link java.lang.String} object
 * @param length a double
 * @param ps a {@link java.lang.String} object
 * @param calibrationEntry a {@link java.lang.String} object
 * @param angle a double
 */
public AbstractCorrector(String name, boolean virtual, double position, double relpos, String relFrom, double length, String ps, String calibrationEntry, double angle) {
	super(name,virtual,position,relpos,relFrom,length,ps, calibrationEntry);
	this.angle= angle;
}
@Override
int _apply() {
	if (getVirtual()) return lastActionResult=DBConst.RC_IS_VIRTUAL;
	if (!dataInvalidated) return lastActionResult=DBConst.RC_NO_CHANGES;
	dataInvalidated= false;
	psH._setCurrent(current);
	return lastActionResult=DBConst.RC_COMPLETED_SUCCESSFULLY;
}
@Override
int _apply(ICurrentApplyModel model) {
	if (getVirtual()) return lastActionResult=DBConst.RC_IS_VIRTUAL;
	if (!dataInvalidated) return lastActionResult=DBConst.RC_NO_CHANGES;
	dataInvalidated= false;
	model.addCurrentValue(current,this);
	return lastActionResult=DBConst.RC_COMPLETED_SUCCESSFULLY;
}
@Override
int _update(DataBushPackedException eh) {
	if (getVirtual()) return lastActionResult= DBConst.RC_IS_VIRTUAL;
	if (!dataInvalidated) return lastActionResult= DBConst.RC_NO_CHANGES;
	dataInvalidated= false;
	//rbAngle=calc.calcFieldStrength(psH.getRBCurrent());
	angle=calc.calcFieldStrength(current=psH.getCurrent());
	return lastActionResult= DBConst.RC_COMPLETED_SUCCESSFULLY;
}
@Override
void a_update(double current) {
	if (getVirtual()) {
		current= calc.calcCurrent(angle);
		return;
	}
	angle=calc.calcFieldStrength(this.current=current);
	//rbAngle=calc.calcFieldStrength(psH.getRBCurrent());
}
/**
 * This method increment the angle for a value specified by parameter
 *
 * @param value a double
 */
public void addToAngle(double value) {
	angle+= value;
	dataInvalidated= true;
	if (isInitialized()) {
		current= calc.calcCurrent(angle);
	}
}
 /**
  * This method sends current to <code>PowerSupply</code>
  *
  * @return <code>int</code> indicate if method succeded
  * @throws java.lang.IllegalStateException if any.
  */
 public int applyAngle() throws IllegalStateException {
	if (!isInitialized()) {
		lastActionResult= DBConst.RC_NOT_INITIALIZED;
		throw new IllegalStateException(DBString.ISE_EL_NOT_INIT);
	}
	int i;
//	synchronized (owner.lock) {
		i= _apply();
//	}
	owner.lEnt.updatePerformed(new ActionReportEvent(this,DBString.UPP,i));
	return i;
}
/**
 * {@inheritDoc}
 *
 * <p>clone.</p>
 * @see AbstractDataBushElement#clone
 */
@Override
public Object clone() {
	AbstractCorrector o= (AbstractCorrector)super.clone();
	o.angle= angle;
	//o.rbAngle= rbAngle;
	return o;
}
/**
 * This method return angle of change beam path.
 *
 * @return a double
 */
public double getAngle() {
	return angle;
}
/**
 * This method return maximal angle of change beam path, that can be caused by this element.
 *
 * @return a double
 * @throws java.lang.IllegalStateException if any.
 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
 */
public double getMaxAngle() throws IllegalStateException, ControlSystemException {
	if (getVirtual()) return defaultMaxAngle;
	return calc.calcFieldStrength(psH.getMaxCurrent());
}
/**
 * This method return minimal angle of change beam path, that can be caused by this element.
 *
 * @return a double
 * @throws java.lang.IllegalStateException if any.
 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
 */
public double getMinAngle() throws IllegalStateException, ControlSystemException {
	if (getVirtual()) return defaultMinAngle;
	return calc.calcFieldStrength(psH.getMinCurrent());
}
/*public double getRBAngle() {
	return rbAngle;
}*/
@Override
void init(DataBushHandler owner) throws DataBushInitializationException {
	super.init(owner);
	owner.ent.correctors.getPowerSupplies().add(psH);
}
/**
 *
 */
@Override
void invalidateDataWithEnergy() {
	dataInvalidated=true;
	current= calc.calcCurrent(angle);
}
/**
 * This method sets angle of change beam path.
 *
 * @param value a double
 */
public void setAngle(double value) {
//	if (angle== value);
	angle= value;
	dataInvalidated= true;
	if (isInitialized()) {
		current= calc.calcCurrent(angle);
		if (owner.setFiresEvent) owner.lEnt.fieldChanged(new DataBushEvent(this,DBString.CORRECTOR_FIELD_CHANGE));
	}
}
/**
 *
 * @param m CalculatorManagerModel
 * @throws EntryNotFound not found.
 */
@Override
void setCalculator(ICalculatorModelFactory m) throws EntryNotFound {
	super.setCalculator(m);
	current= calc.calcCurrent(angle);
}
/**
 * {@inheritDoc}
 *
 * This method sets current.
 */
@Override
public void setCurrent(double value) {
//	if (current== value);
	dataInvalidated= true;
	if (isInitialized()) {
		angle= calc.calcFieldStrength(current);
		if (owner.setFiresEvent) owner.lEnt.fieldChanged(new DataBushEvent(this,DBString.CORRECTOR_FIELD_CHANGE));
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
	if (par[PR_ANGLE]!=null) angle= ((Double)par[PR_ANGLE]).doubleValue();
}
/**
 * {@inheritDoc}
 *
 * <p>toString.</p>
 * @see AbstractDataBushElement#toString
 */
@Override
public String toString() {
	return super.toString()+" "+descriptor().getParameterTag(PR_ANGLE)+"="+angle+" >"+DBConst.EOL;
}
/**
 * {@inheritDoc}
 *
 * This method reads energy from <code>PowerSupply</code> and transform it to magnetic properties of this element.
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
	if (i==DBConst.RC_COMPLETED_SUCCESSFULLY) owner.linOp.invalidateData();
	owner.lEnt.updatePerformed(new ActionReportEvent(this,DBString.UPP,i,eh));
	if (i==DBConst.RC_COMPLETED_SUCCESSFULLY) owner.lEnt.fieldChanged(new DataBushEvent(this,DBString.CORRECTOR_FIELD_CHANGE));
	eh.throwIt();
	return i;
}

}
