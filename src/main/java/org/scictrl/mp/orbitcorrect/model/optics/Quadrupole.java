package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.ActionReportEvent;
import org.scictrl.mp.orbitcorrect.ControlSystemException;
import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.DBElementDescriptor;
import org.scictrl.mp.orbitcorrect.DBString;
import org.scictrl.mp.orbitcorrect.DataBushEvent;
import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.EntryNotFound;
import org.scictrl.mp.orbitcorrect.ICalculatorModelFactory;
import org.scictrl.mp.orbitcorrect.ICurrentApplyModel;
import org.scictrl.mp.orbitcorrect.IMagnetCalculatorModel;
import org.scictrl.mp.orbitcorrect.math.QuadrupoleMatrix;
import org.scictrl.mp.orbitcorrect.math.TransferMatrix;
/**
 * <code>Quadrupole</code> is magnetic element that holds mainly quadrupole strength.
 *
 * @author igor@scictrl.com
 */
public class Quadrupole extends AbstractCalibratedMagnet {
	private double quadrupoleStrength;
	/** Constant <code>PR_QUADRUPOLE_STRENGTH=8</code> */
	public static final int	PR_QUADRUPOLE_STRENGTH=8;
	private double defaultMin_k=0.0;
	private double defaultMax_k=4.0;
/**
 * Constructs <code>Quadrupole</code> with specified name and default parameter's values.
 *
 * @param name a {@link java.lang.String} name of quadrupole
 */
public Quadrupole(String name) {
	super(name);
}
/**
 * Constructs the <code>Quadrupole</code> with specified parameters.
 *
 * @param name a {@link java.lang.String} object
 * @param virtual a boolean
 * @param position a double
 * @param relpos a double
 * @param relFrom a {@link java.lang.String} object
 * @param length a double
 * @param ps a {@link java.lang.String} object
 * @param calibrationEntry a {@link java.lang.String} object
 * @param quadrupoleStrength a double
 */
public Quadrupole(String name, boolean virtual, double position, double relpos, String relFrom, double length, String ps, String calibrationEntry, double quadrupoleStrength) {
	super(name,virtual,position,relpos,relFrom,length,ps,calibrationEntry);
	this.quadrupoleStrength= quadrupoleStrength;
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
			quadrupoleStrength= calc.calcFieldStrength(current=psH._getCurrent());
		}
	updateMatrices();
	return lastActionResult= r;
}
@Override
void a_update(double current) {
	if (!getVirtual()) {
		quadrupoleStrength= calc.calcFieldStrength(this.current=current);
	} else 	current= calc.calcCurrent(quadrupoleStrength);
	updateMatrices();
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
 * @see AbstractDataBushElement#clone()
 */
@Override
public Object clone() {
	Quadrupole o= (Quadrupole)super.clone();
	o.quadrupoleStrength= quadrupoleStrength;
	return o;
}
/**
 * {@inheritDoc}
 *
 * <p>descriptor.</p>
 * @see AbstractDataBushElement#descriptor()
 */
@Override
public DBElementDescriptor descriptor() {
	return DBElementDescriptor.ELDES_QUADRUPOLE;
}
/**
 * {@inheritDoc}
 *
 * <p>elType.</p>
 * @see AbstractDataBushElement#elType()
 */
@Override
public int elType() {
	return DBElementDescriptor.EL_QUADRUPOLE;
}
/**
 * This method returns <code>IMagnetCalculatorModel</code>.
 *
 * @see org.scictrl.mp.orbitcorrect.IMagnetCalculatorModel
 * @return a {@link org.scictrl.mp.orbitcorrect.IMagnetCalculatorModel} object
 */
public IMagnetCalculatorModel getCalculator() {
	return calc;
}
/**
 * This method returns maximal quadrupole strength, which can be achived. It depends of maximal current.
 *
 * @return a double
 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
 */
public double getMaxQuadrupoleStrength() throws ControlSystemException {
	if (getVirtual()) return defaultMax_k;
	return calc.calcFieldStrength(psH.getMaxCurrent());
}
/**
 * This method returns minimal quadrupole strength, which can be achived. It depends of minimal current.
 *
 * @return a double
 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
 */
public double getMinQuadrupoleStrength() throws ControlSystemException {
	if (getVirtual()) return defaultMin_k;
	return calc.calcFieldStrength(psH.getMinCurrent());
}
/**
 * This method returns quadrupole strength.
 *
 * @return a double
 */
public double getQuadrupoleStrength() {
	return quadrupoleStrength;
}
/**
 *
 */
@Override
void invalidateDataWithEnergy() {
	dataInvalidated=true;
	current= calc.calcCurrent(quadrupoleStrength);
}
/**
 * {@inheritDoc}
 *
 * This method return transfer matrix, calculated with current length, quadrupole strength and radius.
 * @see org.scictrl.mp.orbitcorrect.math.TransferMatrix
 */
@Override
public TransferMatrix newMatrix() {
	return new QuadrupoleMatrix(length,quadrupoleStrength);
}
/**
 * {@inheritDoc}
 *
 * This method return transfer matrix, calculated with current quadrupole strength, radius and as parameter specified length.
 * @see org.scictrl.mp.orbitcorrect.math.TransferMatrix
 */
@Override
public TransferMatrix newMatrix(double length) {
	return new QuadrupoleMatrix(length,quadrupoleStrength);
}
/**
 *
 * @param m CalculatorManagerModel
 * @throws EntryNotFound not found.
 */
@Override
void setCalculator(ICalculatorModelFactory m) throws EntryNotFound {
	super.setCalculator(m);
	current= calc.calcCurrent(quadrupoleStrength);
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
	if (calc!=null) quadrupoleStrength= calc.calcFieldStrength(current);
	updateMatrices();
	dataInvalidated= true;
	if (isInitialized()) {
		owner.linOp.invalidateData();
		if (owner.setFiresEvent) owner.lEnt.fieldChanged(new DataBushEvent(this,DBString.QUADRUPOLE_FIELD_CHANGE));
	}
}
/**
 * This method sets quadrupole strength. <code>ICalculatorModelFactory</code> calculate necessary current and this method sets it.
 *
 * @param value a double
 */
public void setQuadrupoleStrength(double value) {
//	if (quadrupoleStrength== value) return;
	quadrupoleStrength= value;
	if (calc!=null) current= calc.calcCurrent(quadrupoleStrength);
	updateMatrices();
	dataInvalidated= true;
	if (isInitialized()) {
		owner.linOp.invalidateData();
		if (owner.setFiresEvent) owner.lEnt.fieldChanged(new DataBushEvent(this,DBString.QUADRUPOLE_FIELD_CHANGE));
	}
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
	if (par[PR_QUADRUPOLE_STRENGTH]!=null) quadrupoleStrength= ((Double)par[PR_QUADRUPOLE_STRENGTH]).doubleValue();
}
/**
 * {@inheritDoc}
 *
 * <p>toString.</p>
 * @see AbstractDataBushElement#toString
 */
@Override
public String toString() {
	return super.toString()+" "+descriptor().getParameterTag(PR_QUADRUPOLE_STRENGTH)+"="+quadrupoleStrength+" >"+DBConst.EOL;
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
	if (i==DBConst.RC_COMPLETED_SUCCESSFULLY) owner.lEnt.fieldChanged(new DataBushEvent(this,DBString.QUADRUPOLE_FIELD_CHANGE));
	eh.throwIt();
	return i;
}
/**
 *
 */
@Override
void updateMatrix() {
	((QuadrupoleMatrix)matrix).set(length,quadrupoleStrength);
}
/**
 *
 */
@Override
TransferMatrix updateMatrix(TransferMatrix m) {
	((QuadrupoleMatrix)m).set(m.getLength(),quadrupoleStrength);
	return m;
}
}
