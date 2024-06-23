package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.ActionReportEvent;
import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.DBElementDescriptor;
import org.scictrl.mp.orbitcorrect.DBString;
import org.scictrl.mp.orbitcorrect.DataBushEvent;
import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.EntryNotFound;
import org.scictrl.mp.orbitcorrect.ICalculatorModelFactory;
import org.scictrl.mp.orbitcorrect.ICurrentApplyModel;
import org.scictrl.mp.orbitcorrect.IMagnetCalculatorModel;


/**
 * <p>Sextupole class.</p>
 *
 * @author igor@scictrl.com
 */
public class Sextupole extends AbstractCalibratedMagnet {
	private double sextupoleStrength;
	/** Constant <code>PR_SEXTUPOLE_STRENGTH=8</code> */
	public static final int	PR_SEXTUPOLE_STRENGTH=8;
/**
 * Constructs <code>Sextupole</code> with specified name and default parameter's values.
 *
 * @param name a {@link java.lang.String} name
 */
public Sextupole(String name) {
	super(name);
}
/**
 * Constructs the <code>Sextupole</code> with specified parameters.
 *
 * @param name a {@link java.lang.String} object
 * @param virtual a boolean
 * @param position a double
 * @param relpos a double
 * @param relFrom a {@link java.lang.String} object
 * @param length a double
 * @param ps a {@link java.lang.String} object
 * @param calibrationEntry a {@link java.lang.String} object
 * @param sextupoleStrength a double
 */
public Sextupole(String name, boolean virtual, double position, double relpos, String relFrom, double length, String ps, String calibrationEntry, double sextupoleStrength) {
	super(name,virtual,position,relpos,relFrom,length,ps,calibrationEntry);
	this.sextupoleStrength= sextupoleStrength;
}
/**
 *
 * @return int
 */
@Override
int _apply() {
	if (getVirtual()) return lastActionResult=DBConst.RC_IS_VIRTUAL;
	if (!dataInvalidated) return lastActionResult=DBConst.RC_NO_CHANGES;
	dataInvalidated= false;
	psH._setCurrent(current);
	return lastActionResult=DBConst.RC_COMPLETED_SUCCESSFULLY;
}
/**
 *
 * @return int
 */
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
	if (!dataInvalidated) return lastActionResult=DBConst.RC_NO_CHANGES;
	dataInvalidated= false;
	int r;
	if (getVirtual()) r= DBConst.RC_IS_VIRTUAL;
		else {
			r= DBConst.RC_COMPLETED_SUCCESSFULLY;
			sextupoleStrength= calc.calcFieldStrength(current=psH._getCurrent());
		}
	updateMatrices();
	return lastActionResult= r;
}
@Override
void a_update(double current) {
	if (getVirtual()) current= calc.calcCurrent(sextupoleStrength);
	else sextupoleStrength= calc.calcFieldStrength(this.current=current);
}
/**
 * This method sends current to <code>PowerSupply</code>
 *
 * @return <code>int</code> indicate if method succeded
 * @throws java.lang.IllegalStateException if any.
 */
public int applyField() throws IllegalStateException {
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
	Sextupole o= (Sextupole)super.clone();
	o.sextupoleStrength= sextupoleStrength;
	return o;
}
/**
 * {@inheritDoc}
 *
 * <p>descriptor.</p>
 * @see AbstractDataBushElement#descriptor
 */
@Override
public DBElementDescriptor descriptor() {
	return DBElementDescriptor.ELDES_SEXTUPOLE;
}
/**
 * {@inheritDoc}
 *
 * <p>elType.</p>
 * @see AbstractDataBushElement#elType
 */
@Override
public int elType() {
	return DBElementDescriptor.EL_QUADRUPOLE;
}
/**
 * This method returns <code>IMagnetCalculatorModel</code>
 *
 * @return a {@link org.scictrl.mp.orbitcorrect.IMagnetCalculatorModel} object
 */
public IMagnetCalculatorModel getCalculator() {
	return calc;
}
/**
 * This method returns sextupole strength.
 *
 * @return a double
 */
public double getSextupoleStrength() {
	return sextupoleStrength;
}
/**
 *
 */
@Override
void invalidateDataWithEnergy() {
	dataInvalidated=true;
	current= calc.calcCurrent(sextupoleStrength);
}
/**
 *
 * @param m CalculatorManagerModel
 * @throws EntryNotFound not found.
 */
@Override
void setCalculator(ICalculatorModelFactory m) throws EntryNotFound {
	super.setCalculator(m);
	current= calc.calcCurrent(sextupoleStrength);
}
/**
 * {@inheritDoc}
 *
 * This method sets current.
 */
@Override
public void setCurrent(double value) {
//	if (current==value) return;
	current=value;
	dataInvalidated= true;
	if (isInitialized()) {
		sextupoleStrength=calc.calcFieldStrength(current);
		if (owner.setFiresEvent) owner.lEnt.fieldChanged(new DataBushEvent(this,DBString.SEXTUPOLE_FIELD_CHANGE));
	}
}
/**
 * This method sets sextupole strength.
 *
 * @param value a double
 */
public void setSextupoleStrength(double value) {
//	if (sextupoleStrength== value) return;
	sextupoleStrength= value;
	dataInvalidated= true;
	if (isInitialized()) {
		current= calc.calcCurrent(sextupoleStrength);
		if (owner.setFiresEvent) owner.lEnt.fieldChanged(new DataBushEvent(this,DBString.SEXTUPOLE_FIELD_CHANGE));
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
	if (par[PR_SEXTUPOLE_STRENGTH]!=null) sextupoleStrength= ((Double)par[PR_SEXTUPOLE_STRENGTH]).doubleValue();
}
/**
 * {@inheritDoc}
 *
 * <p>toString.</p>
 * @see AbstractDataBushElement#toString
 */
@Override
public String toString() {
	return super.toString()+" "+descriptor().getParameterTag(PR_SEXTUPOLE_STRENGTH)+"="+sextupoleStrength+" >"+DBConst.EOL;
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
	owner.lEnt.updatePerformed(new ActionReportEvent(this,DBString.UPP,i,eh));
	if (i==DBConst.RC_COMPLETED_SUCCESSFULLY) owner.lEnt.fieldChanged(new DataBushEvent(this,DBString.SEXTUPOLE_FIELD_CHANGE));
	eh.throwIt();
	return i;
}
}
