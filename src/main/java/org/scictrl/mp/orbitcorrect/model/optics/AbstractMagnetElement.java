package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DBString;
import org.scictrl.mp.orbitcorrect.DataBushInitializationException;


/**
 * <code>AbstractMagnetElement</code> represent element that generate quasi-static magnet field. It has pointer to <code>PowerSupply</code>
 *
 * @author igor@scictrl.com
 */
public abstract class AbstractMagnetElement extends AbstractTransferElement {
	private String ps= "";
	/**
	 * Associated power supply object.
	 * */
	protected PowerSupply psH;
	/** Constant <code>PR_POWER_SUPPLY=6</code> */
	public static final int	PR_POWER_SUPPLY=6;
	/**
	 * Constructs <code>AbstractMagnetElement</code> with specified name and default parameter's values.
	 *
	 * @param name a {@link java.lang.String} name of transfer element
	 */
	public AbstractMagnetElement(String name) {
		super(name);
	}
	/**
	 * Constructs the <code>AbstractMagnetElement</code> with specified parameters.
	 *
	 * @param name a {@link java.lang.String} object
	 * @param virtual a boolean
	 * @param position a double
	 * @param relpos a double
	 * @param relFrom a {@link java.lang.String} object
	 * @param length a double
	 * @param ps a {@link java.lang.String} object
	 */
	public AbstractMagnetElement(String name, boolean virtual, double position, double relpos, String relFrom, double length, String ps) {
		super(name,virtual,position,relpos,relFrom,length);
		this.ps= ps;
	}
	@Override
	void clear() {
		psH=null;
		super.clear();
	}
	/**
	 * This method returns name of power supply.
	 *
	 * @return <code>ps</code> name of power supply, specified by constructor.
	 */
	public String getPS() {
		return ps;
	}
	/**
	 * This method return <code>PowerSupply</code> which controls power supply, physically connected to this element.
	 *
	 * @see PowerSupply
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PowerSupply} object
	 */
	public PowerSupply getPowerSupply() {
		return psH;
	}
	@Override
	void init(DataBushHandler owner) throws DataBushInitializationException {
		psH= owner.getPowerSupply(ps);
		if (psH==null) {
			Object[] args= {ps};
			throw new DataBushInitializationException(this,java.text.MessageFormat.format(DBString.NO_PS_FOUND,args));
		}
		psH.getDependingMagnets().add(this);
		owner.ent.magnets.getPowerSupplies().add(psH);
		super.init(owner);
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
		if (par[PR_POWER_SUPPLY]!=null) ps= par[PR_POWER_SUPPLY].toString();
	}
	/**
	 * {@inheritDoc}
	 *
	 * <p>toString.</p>
	 * @see AbstractDataBushElement#toString
	 */
	@Override
	public String toString() {
		return super.toString()+" "+descriptor().getParameterTag(PR_POWER_SUPPLY)+"=\""+ps+"\"";
	}
}
