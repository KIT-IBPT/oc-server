package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.ActionReportEvent;
import org.scictrl.mp.orbitcorrect.ControlSystemException;
import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.DBMath;
import org.scictrl.mp.orbitcorrect.DBString;
import org.scictrl.mp.orbitcorrect.DataBushEvent;
import org.scictrl.mp.orbitcorrect.DataBushInitializationException;
import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.EntryNotFound;
import org.scictrl.mp.orbitcorrect.IBendingCalculatorModel;
import org.scictrl.mp.orbitcorrect.ICalculatorModelFactory;
import org.scictrl.mp.orbitcorrect.ICurrentApplyModel;
import org.scictrl.mp.orbitcorrect.math.DipoleMatrix;
import org.scictrl.mp.orbitcorrect.math.SDipoleMatrix;
import org.scictrl.mp.orbitcorrect.math.TransferMatrix;
/**
 * Bending magnet has three dynamical values, the values that are changed as
 * current changes. These are energy, dipole field, quadrupole field. These
 * values are combined, for this reason is setting these values to bending
 * magnet synchronized.
 *
 * @author igor@scictrl.com
 */
public abstract class AbstractBending extends AbstractCalibratedMagnet {

	private double dipoleField;
	private double energy;
	private IBendingCalculatorModel calc;
	private final double Q= 1000000000/DBMath.C;

	/** Bending radius. */
	protected double radius;
	/** Quadruple strength */
	protected double quadrupoleStrength;


	/** Constant <code>PR_QUADRUPOLE_STRENGTH=8</code> */
	public static final int	PR_QUADRUPOLE_STRENGTH=8;
	/** Constant <code>PR_RADIUS=9</code> */
	public static final int	PR_RADIUS=9;
	/** Constant <code>PR_DFIELD=10</code> */
	public static final int	PR_DFIELD=10;
	/** Constant <code>PR_ENERGY=11</code> */
	public static final int	PR_ENERGY=11;

	private double defaultMin_E=0.0;
	private double defaultMax_E=2.5;
	private boolean energyInvalidated=true;

	/**
	 * Constructs <code>AbstractBending</code> with specified name and default parameter's values.
	 *
	 * @param name a {@link java.lang.String} the name of abstract bending
	 */
	public AbstractBending(String name) {
		super(name);
	}
	/**
	 * Constructs the <code>AbstractBending</code> with specified parameters.
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
	 * @param radius a double
	 * @param dipoleField a double
	 * @param energy a double
	 */
	public AbstractBending(String name, boolean virtual, double position, double relpos, String relFrom, double length, String ps, String calibrationEntry, double quadrupoleStrength, double radius, double dipoleField, double energy) {
		super(name,virtual,position,relpos,relFrom,length,ps,calibrationEntry);
		this.radius= radius;
		this.dipoleField= dipoleField;
		this.quadrupoleStrength= quadrupoleStrength;
		this.energy= energy;
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
	//	if (calc==null) return owner.reportActionError(new ActionReportEvent(this,DBString.ERR_AP_+DBString.NO_CALC+"\n"));
	//	if (psH==null) return owner.reportActionError(new ActionReportEvent(this,DBString.ERR_AP_+DBString.NO_PS+"\n"));
	//	calc.setQuadrupoleStrength(quadrupoleStrength);
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
		if (!dataInvalidated && !energyInvalidated) {
			return lastActionResult=DBConst.RC_NO_CHANGES;
		}
		int r;
		if (getVirtual()) {
			r= DBConst.RC_IS_VIRTUAL;
		} else {
			r= DBConst.RC_COMPLETED_SUCCESSFULLY;
			if (dataInvalidated) {
				energy= calc.calcEnergy(current= psH._getCurrent());
				dipoleField= calcDipoleField();
			}
			if (energyInvalidated) {
				quadrupoleStrength= calc.calcQuadrupoleStrength(current);
			}
		}
		if (dataInvalidated || energyInvalidated) {
			updateMatrices();
		}
		dataInvalidated= false;
		energyInvalidated= false;
		return lastActionResult= r;
	}

	@Override
	void a_update(double current) {
		if (!getVirtual()) {
			this.current=current;
			energy= calc.calcEnergy(current);
			dipoleField= calcDipoleField();
			quadrupoleStrength= calc.calcQuadrupoleStrength(current);
		} else current= calc.calcCurrentFromEnergy(energy);
		updateMatrices();
	}
	/**
	 * This method transforms magnetic properties to current and sends it to <code>PowerSupply</code>
	 *
	 * @return <code>int</code>  indicate if method succeded
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
	 *
	 * @return double
	 */
	private double calcDipoleField() {
		return (energy<DBMath.MC2) ? 0 : Math.sqrt(DBMath.sqr(energy)-DBMath.MC2*DBMath.MC2)*Q/radius;
	}
	/**
	 *
	 * @return double
	 */
	private double calcEnergy() {
		return Math.sqrt(DBMath.sqr(DBMath.MC2)+ DBMath.sqr(radius*dipoleField/Q));
	}
	/**
	 *
	 *
	 */
	@Override
	void clear() {
		calc=null;
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
		AbstractBending o= (AbstractBending)super.clone();
		o.radius= radius;
		o.quadrupoleStrength= quadrupoleStrength;
		return o;
	}
	/**
	 * This method return <code>IBendingCalculatorModel</code>.
	 *
	 * @see org.scictrl.mp.orbitcorrect.IBendingCalculatorModel
	 * @return a {@link org.scictrl.mp.orbitcorrect.IBendingCalculatorModel} object
	 */
	public IBendingCalculatorModel getCalculator() {
		return calc;
	}
	/**
	 * This method return dipole field strength
	 *
	 * @return a double
	 */
	public double getDipoleField() {
		return dipoleField;
	}
	/**
	 * This method return energy, which is specified by dipole magnetic strength.
	 *
	 * @return a double
	 */
	public double getEnergy() {
		return energy;
	}
	/**
	 * This method return miximal energy that can be safely exceeded by this magnet.
	 *
	 * @return a double
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	public double getMaxEnergy() throws ControlSystemException {
		if (getVirtual()) return defaultMax_E;
		return calc.calcEnergy(psH.getMaxCurrent());
	}
	/**
	 * This method return minimal energy that can be exceeded by this magnet.
	 *
	 * @return a double
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	public double getMinEnergy() throws ControlSystemException {
		if (getVirtual()) return defaultMin_E;
		return calc.calcEnergy(psH.getMinCurrent());
	}
	/**
	 * This method return quadruploe strength.
	 *
	 * @return a double
	 */
	public double getQuadrupoleStrength() {
		return quadrupoleStrength;
	}
	/**
	 * This method return radius, which is specified by dipole magnetic strength.
	 *
	 * @return a double
	 */
	public double getRadius() {
		return radius;
	}
	@Override
	void init(DataBushHandler owner) throws DataBushInitializationException {
		super.init(owner);
		owner.ent.bendings.getPowerSupplies().add(psH);
		a_update(psH.getCurrent());
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method return transfer matrix, calculated with current length, quadrupole strength and radius.
	 */
	@Override
	public TransferMatrix newMatrix() {
		return new SDipoleMatrix(length,quadrupoleStrength,radius);
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method return transfer matrix, calculated with current quadrupole strength, radius and as parameter specified length.
	 * @see org.scictrl.mp.orbitcorrect.math.TransferMatrix
	 */
	@Override
	public TransferMatrix newMatrix(double length) {
		return new SDipoleMatrix(length,quadrupoleStrength,radius);
	}
	/**
	 *
	 * @param m CalculatorManagerModel
	 * @throws EntryNotFound not found.
	 */
	@Override
	void setCalculator(ICalculatorModelFactory m) throws EntryNotFound {
		calc= m.getBendingCalculatorModel(calibrationEntry);
		if (energy>0.0) {
			current= calc.calcCurrentFromEnergy(energy);
			dipoleField= calcDipoleField();
			quadrupoleStrength= calc.calcQuadrupoleStrength(current);
		} else if (dipoleField>0.0) {
			energy= calcEnergy();
			current= calc.calcCurrentFromEnergy(energy);
			quadrupoleStrength= calc.calcQuadrupoleStrength(current);
		} else {
			current= calc.calcCurrentFromQuadrupoleStrength(quadrupoleStrength);
			energy= calc.calcEnergy(current);
			dipoleField= calcDipoleField();
		}
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method sets current.
	 */
	@Override
	public void setCurrent(double value) {
	//	if (current== value) return;
		current= value;
		dataInvalidated= true;
		if (isInitialized()) {
			quadrupoleStrength= calc.calcQuadrupoleStrength(current);
			energy= calc.calcEnergy(current);
			dipoleField= calcDipoleField();
			updateMatrices();
			owner.linOp.invalidateData();
			if (owner.setFiresEvent) owner.lEnt.fieldChanged(new DataBushEvent(this,DBString.BENDING_FIELD_CHANGE));
		} else updateMatrix();
	}
	/**
	 * This method sets dipole field.
	 *
	 * @param value a double
	 */
	public void setDipoleField(double value) {
	//	if (dipoleField== value) return;
		dipoleField= value;
		energy= calcEnergy();
		dataInvalidated= true;
		if (isInitialized()) {
			current= calc.calcCurrentFromEnergy(energy);
			quadrupoleStrength= calc.calcQuadrupoleStrength(current);
			updateMatrices();
			owner.linOp.invalidateData();
			if (owner.setFiresEvent) owner.lEnt.fieldChanged(new DataBushEvent(this,DBString.BENDING_FIELD_CHANGE));
		} else updateMatrix();
	}
	/**
	 * This method sets energy.
	 *
	 * @param value a double
	 */
	public void setEnergy(double value) {
	//	if (energy== value) return;
		energy= value;
		dipoleField= calcDipoleField();
		dataInvalidated= true;
		if (isInitialized()) {
			current= calc.calcCurrentFromEnergy(energy);
			quadrupoleStrength= calc.calcQuadrupoleStrength(current);
			updateMatrices();
			owner.linOp.invalidateData();
			if (owner.setFiresEvent) owner.lEnt.fieldChanged(new DataBushEvent(this,DBString.BENDING_FIELD_CHANGE));
		} else updateMatrix();
	}
	/**
	 * This method sets quadrupole strength.
	 *
	 * @param value a double
	 */
	public void setQuadrupoleStrength(double value) {
	//	if (quadrupoleStrength== value);
		quadrupoleStrength= value;
		dataInvalidated= true;
		if (isInitialized()) {
			current= calc.calcCurrentFromQuadrupoleStrength(quadrupoleStrength);
			energy= calc.calcEnergy(current);
			dipoleField= calcDipoleField();
			updateMatrices();
			owner.linOp.invalidateData();
			if (owner.setFiresEvent) owner.lEnt.fieldChanged(new DataBushEvent(this,DBString.BENDING_FIELD_CHANGE));
		} else updateMatrix();
	}
	/**
	 * This method sets radius.
	 *
	 * @param value a double
	 * @throws java.lang.IllegalStateException if any.
	 */
	public void setRadius(double value) throws IllegalStateException {
		if (!isInitialized()) {
			radius= value;
			updateMatrices();
			if (isInitialized()) owner.linOp.invalidateData();
		} else throw new IllegalStateException(DBString.ISE_EL_INIT);
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
		if (par[PR_QUADRUPOLE_STRENGTH]!=null) quadrupoleStrength= ((Double)par[PR_QUADRUPOLE_STRENGTH]).doubleValue();
		if (par[PR_RADIUS]!=null) if (0.0!=((Double)par[PR_RADIUS]).doubleValue()) setRadius(((Double)par[PR_RADIUS]).doubleValue());
		if (par[PR_DFIELD]!=null) dipoleField= ((Double)par[PR_DFIELD]).doubleValue();
		if (par[PR_ENERGY]!=null) energy= ((Double)par[PR_ENERGY]).doubleValue();
	}
	/**
	 * {@inheritDoc}
	 *
	 * <p>toString.</p>
	 * @see AbstractDataBushElement#toString
	 */
	@Override
	public String toString() {
		return super.toString()+" "+descriptor().getParameterTag(PR_QUADRUPOLE_STRENGTH)+"="+quadrupoleStrength+" "+descriptor().getParameterTag(PR_RADIUS)+"="+radius+" "+descriptor().getParameterTag(PR_DFIELD)+"="+dipoleField+" "+descriptor().getParameterTag(PR_ENERGY)+"="+energy;
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method reads current from <code>PowerSupply</code> and transform it to magnetic properties.
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
		if (i==DBConst.RC_COMPLETED_SUCCESSFULLY) owner.lEnt.fieldChanged(new DataBushEvent(this,DBString.BENDING_FIELD_CHANGE));
		eh.throwIt();
		return i;
	}
	/**
	 *
	 */
	@Override
	void updateMatrix() {
		((DipoleMatrix)matrix).set(length,quadrupoleStrength,radius);
	}
	/**
	 *
	 */
	@Override
	TransferMatrix updateMatrix(TransferMatrix m) {
		if (m instanceof DipoleMatrix) ((DipoleMatrix)m).set(m.getLength(),quadrupoleStrength,radius);
		return m;
	}

	@Override
	void invalidateDataWithEnergy() {
		super.invalidateDataWithEnergy();
		energyInvalidated=true;
	}
}
