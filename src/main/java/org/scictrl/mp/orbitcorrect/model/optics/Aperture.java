package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.DBElementDescriptor;


/**
 * Aperture is drift space element. It has lead shields, which can be moved toward
 * the beam and stops outer electrons.
 *
 * @author igor@scictrl.com
 */
public class Aperture extends AbstractTransferElement {
	private double xsize;
	private double zsize;

	/** Constant <code>PR_X_SIZE=6</code> */
	public static final int	PR_X_SIZE= 6;
	/** Constant <code>PR_Z_SIZE=7</code> */
	public static final int	PR_Z_SIZE= 7;

/**
 * Aperture constructor that takes only name as parameter.
 *
 * @param name a {@link java.lang.String} object
 */
public Aperture(String name) {
	super(name);
}
/**
 * Constructs the <code>Aperture</code> with specified parameters.
 *
 * @param name a {@link java.lang.String} object
 * @param virtual a boolean
 * @param position a double
 * @param relpos a double
 * @param relFrom a {@link java.lang.String} object
 * @param length a double
 * @param xSize a double
 * @param zSize a double
 */
public Aperture(String name, boolean virtual, double position, double relpos, String relFrom, double length, double xSize, double zSize) {
	super(name, virtual, position, relpos, relFrom, length);
	this.xsize= xSize;
	this.zsize= zSize;
}
/**
 * {@inheritDoc}
 *
 * <p>descriptor.</p>
 * @see AbstractDataBushElement#descriptor
 */
@Override
public DBElementDescriptor descriptor() {
	return DBElementDescriptor.ELDES_APERTURE;
}
/**
 * {@inheritDoc}
 *
 * <p>elType.</p>
 * @see AbstractDataBushElement#elType
 */
@Override
public int elType() {
	return DBElementDescriptor.EL_APERTURE;
}
/**
 * This method returns aperture of lead shields in x direction.
 *
 * @return a double
 */
public double getXsize() {
	return xsize;
}
/**
 * <p>Getter for the field <code>zsize</code>.</p>
 *
 * @return double
 */
public double getZsize() {
	return zsize;
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
	if (par[PR_X_SIZE]!=null) xsize= ((Double)par[PR_X_SIZE]).doubleValue();
	if (par[PR_Z_SIZE]!=null) zsize= ((Double)par[PR_Z_SIZE]).doubleValue();
}
/**
 * This method sets aperture in x direction.
 *
 * @see #getXsize
 * @param newValue a double
 */
public void setXsize(double newValue) {
	this.xsize = newValue;
}
/**
 * This method sets aperture in z direction.
 *
 * @see #getZsize
 * @param newValue a double
 */
public void setZsize(double newValue) {
	this.zsize = newValue;
}
/**
 * {@inheritDoc}
 *
 * <p>toString.</p>
 * @see AbstractDataBushElement#toString
 */
@Override
public String toString() {
	return super.toString()+" "+descriptor().getParameterTag(PR_X_SIZE)+"="+xsize+" "+descriptor().getParameterTag(PR_Z_SIZE)+"="+zsize+" >"+DBConst.EOL;
}
}
