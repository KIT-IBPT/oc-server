package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.DBElementDescriptor;


/**
 * Cavity represent cavity.
 *
 * @author igor@scictrl.com
 */
public class Cavity extends AbstractTransferElement {
	private double voltage;
	private double lag;
	private int harmon;
	private double betRF;
	private double pG;
	private double shunt;
	private double tFill;

	/** Constant <code>PR_VOLTAGE=6</code> */
	public static final int	PR_VOLTAGE= 6;
	/** Constant <code>PR_LAG=7</code> */
	public static final int	PR_LAG= 7;
	/** Constant <code>PR_HARMON=8</code> */
	public static final int	PR_HARMON= 8;
	/** Constant <code>PR_BETRF=9</code> */
	public static final int	PR_BETRF= 9;
	/** Constant <code>PR_PG=10</code> */
	public static final int	PR_PG= 10;
	/** Constant <code>PR_SHUNT=11</code> */
	public static final int	PR_SHUNT= 11;
	/** Constant <code>PR_TFILL=12</code> */
	public static final int	PR_TFILL=12;

/**
 * Cavity constructor that takes only name as parameter.
 *
 * @param name a {@link java.lang.String} object
 */
public Cavity(String name) {
	super(name);
}
/**
 * Constructs the <code>Cavity</code> with specified parameters.
 *
 * @param name a {@link java.lang.String} object
 * @param virtual a boolean
 * @param position a double
 * @param relpos a double
 * @param relFrom a {@link java.lang.String} object
 * @param length a double
 * @param voltage a double
 * @param lag a double
 * @param harmon a int
 * @param betRF a double
 * @param pG a double
 * @param shunt a double
 * @param tFill a double
 */
public Cavity(String name, boolean virtual, double position, double relpos, String relFrom, double length, double voltage, double lag, int harmon, double betRF, double pG, double shunt, double tFill) {
	super(name, virtual, position, relpos, relFrom, length);
	this.voltage= voltage;
	this.lag= lag;
	this.harmon= harmon;
	this.betRF= betRF;
	this.pG= pG;
	this.shunt= shunt;
	this.tFill= tFill;
}
/**
 * {@inheritDoc}
 *
 * <p>descriptor.</p>
 * @see AbstractDataBushElement#descriptor
 */
@Override
public DBElementDescriptor descriptor() {
	return DBElementDescriptor.ELDES_CAVITY;
}
/**
 * {@inheritDoc}
 *
 * <p>elType.</p>
 * @see AbstractDataBushElement#elType
 */
@Override
public int elType() {
	return DBElementDescriptor.EL_CAVITY;
}
/**
 * <p>Getter for the field <code>betRF</code>.</p>
 *
 * @return double
 */
public double getBetRF() {
	return betRF;
}
/**
 * <p>Getter for the field <code>harmon</code>.</p>
 *
 * @return int
 */
public int getHarmon() {
	return harmon;
}
/**
 * <p>Getter for the field <code>lag</code>.</p>
 *
 * @return double
 */
public double getLag() {
	return lag;
}
/**
 * <p>Getter for the field <code>pG</code>.</p>
 *
 * @return double
 */
public double getPG() {
	return pG;
}
/**
 * <p>Getter for the field <code>shunt</code>.</p>
 *
 * @return double
 */
public double getShunt() {
	return shunt;
}
/**
 * <p>Getter for the field <code>tFill</code>.</p>
 *
 * @return double
 */
public double getTFill() {
	return tFill;
}
/**
 * This method return voltage on cavity.
 *
 * @return <code>double</code> voltage value
 */
public double getVoltage() {
	return voltage;
}
/**
 * <p>Setter for the field <code>betRF</code>.</p>
 *
 * @param newValue double
 */
public void setBetRF(double newValue) {
	this.betRF = newValue;
}
/**
 * <p>Setter for the field <code>harmon</code>.</p>
 *
 * @param newValue int
 */
public void setHarmon(int newValue) {
	this.harmon = newValue;
}
/**
 * <p>Setter for the field <code>lag</code>.</p>
 *
 * @param newValue double
 */
public void setLag(double newValue) {
	this.lag = newValue;
}
/**
 * <p>Setter for the field <code>pG</code>.</p>
 *
 * @param newValue double
 */
public void setPG(double newValue) {
	this.pG = newValue;
}
/**
 * <p>Setter for the field <code>shunt</code>.</p>
 *
 * @param newValue double
 */
public void setShunt(double newValue) {
	this.shunt = newValue;
}
/**
 * <p>Setter for the field <code>tFill</code>.</p>
 *
 * @param newValue double
 */
public void setTFill(double newValue) {
	this.tFill = newValue;
}
/**
 * This method set voltage to cavity.
 *
 * @param newValue a double
 */
public void setVoltage(double newValue) {
	this.voltage = newValue;
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
	if (par[PR_VOLTAGE]!=null) voltage= ((Double)par[PR_VOLTAGE]).doubleValue();
	if (par[PR_LAG]!=null) lag= ((Double)par[PR_LAG]).doubleValue();
	if (par[PR_HARMON]!=null) harmon= ((Integer)par[PR_HARMON]).intValue();
	if (par[PR_BETRF]!=null) betRF= ((Double)par[PR_BETRF]).doubleValue();
	if (par[PR_PG]!=null) pG= ((Double)par[PR_PG]).doubleValue();
	if (par[PR_SHUNT]!=null) shunt= ((Double)par[PR_SHUNT]).doubleValue();
	if (par[PR_TFILL]!=null) tFill= ((Double)par[PR_TFILL]).doubleValue();
}
/**
 * {@inheritDoc}
 *
 * <p>toString.</p>
 * @see AbstractDataBushElement#toString
 */
@Override
public String toString() {
	return super.toString()+" "+descriptor().getParameterTag(PR_VOLTAGE)+"="+voltage+" "+descriptor().getParameterTag(PR_LAG)+"="+lag+" "+descriptor().getParameterTag(PR_HARMON)+"="+harmon+" "+descriptor().getParameterTag(PR_BETRF)+"="+betRF+" "+descriptor().getParameterTag(PR_PG)+"="+pG+" "+descriptor().getParameterTag(PR_SHUNT)+"="+shunt+" "+descriptor().getParameterTag(PR_TFILL)+"="+tFill+" >"+DBConst.EOL;
}
}
