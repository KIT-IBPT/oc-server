package org.scictrl.mp.orbitcorrect.model.optics;

import java.util.Iterator;

import org.scictrl.mp.orbitcorrect.DataBushInitializationException;
import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.EntryNotFound;
import org.scictrl.mp.orbitcorrect.ICalculatorModelFactory;
import org.scictrl.mp.orbitcorrect.ICurrentApplyModel;
import org.scictrl.mp.orbitcorrect.IMagnetCalculatorModel;
import org.scictrl.mp.orbitcorrect.math.TransferMatrix;


/**
 * This magnet has calibration. It means that can transfers current to magnetic field and back.
 *
 * @author igor@scictrl.com
 */
public abstract class AbstractCalibratedMagnet extends AbstractMagnetElement implements AbstractUpdateableElement, IApplyableElement {

	/** Constant <code>PR_CALIBRATION_ENTRY=7</code> */
	public static final int PR_CALIBRATION_ENTRY=7;

	/** Calibration entry name. */
	protected String calibrationEntry= "";

	/** Calibration calculator object. */
	protected IMagnetCalculatorModel calc;

	/** Data invalidated. */
	protected boolean dataInvalidated = true;

	/** Electrical current in magnets. */
	protected double current = 0.0;

	/**
	 * Constructs <code>AbstractCalibratedMagnet</code> with specified name and default parameter's values.
	 *
	 * @param name a {@link java.lang.String} name of transfer element
	 */
	public AbstractCalibratedMagnet(String name) {
		super(name);
	}
	/**
	 * Constructs the <code>AbstractCalibratedMagnet</code> with specified parameters.
	 *
	 * @param name a {@link java.lang.String} object
	 * @param virtual a boolean
	 * @param position a double
	 * @param relpos a double
	 * @param relFrom a {@link java.lang.String} object
	 * @param length a double
	 * @param ps a {@link java.lang.String} object
	 * @param calibrationEntry a {@link java.lang.String} object
	 */
	public AbstractCalibratedMagnet(String name, boolean virtual, double position, double relpos, String relFrom, double length, String ps, String calibrationEntry) {
		super(name,virtual,position,relpos,relFrom,length,ps);
		this.calibrationEntry= calibrationEntry;
	}
	/**
	 *
	 * @return int
	 */
	abstract int _apply();
	/**
	 *
	 * @return int
	 * @param model ICurrentApplyModel
	 */
	abstract int _apply(ICurrentApplyModel model);
	abstract int _update(DataBushPackedException eh);
	abstract void a_update(double current);
	/**
	 *
	 */
	@Override
	void clear() {
		calc=null;
		super.clear();
	}
	/**
	 * This method return the name of calculator, that do the current-field calculation
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getCalibrationEntry() {
		return calibrationEntry;
	}
	/**
	 * This method return current on this element.
	 *
	 * @return a double
	 */
	public double getCurrent() {
		return current;
	}
	@Override
	void init(DataBushHandler owner) throws DataBushInitializationException {
		super.init(owner);
		owner.ent.calMagnets.getPowerSupplies().add(psH);
		try {
			setCalculator(owner.db.getCalculatorModelFactory());
		} catch (EntryNotFound e) {
			throw new DataBushInitializationException(this,e.toString());
		}
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
	 *
	 */
	void invalidateDataWithEnergy() {dataInvalidated=true;}
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
	 * This method sets <code>ICalculatorModelFactory</code>.
	 * @see org.scictrl.mp.orbitcorrect.ICalculatorModelFactory
	 */
	void setCalculator(ICalculatorModelFactory m) throws EntryNotFound {
		calc= m.getGenericMagnetCalculatorModel(calibrationEntry);
	}
	/**
	 * This method sets current on this element.
	 *
	 * @param newCurrent a double
	 */
	public void setCurrent(double newCurrent) {
		current = newCurrent;
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
		if (par[PR_CALIBRATION_ENTRY]!=null) calibrationEntry= par[PR_CALIBRATION_ENTRY].toString();
	}
	/**
	 * {@inheritDoc}
	 *
	 * <p>toString.</p>
	 * @see AbstractDataBushElement#toString
	 */
	@Override
	public String toString() {
		return super.toString()+" "+descriptor().getParameterTag(PR_CALIBRATION_ENTRY)+"=\""+calibrationEntry+"\"";
	}
	/**
	 * <p>updateMatrices.</p>
	 */
	protected void updateMatrices() {
		updateMatrix();
		if (!isInitialized()) return;
		Iterator<TransferMatrix> it= matrices.iterator();
		org.scictrl.mp.orbitcorrect.math.TransferMatrix m;
		while (it.hasNext()) {
			m= it.next();
			updateMatrix(m);
		}
	}
}
