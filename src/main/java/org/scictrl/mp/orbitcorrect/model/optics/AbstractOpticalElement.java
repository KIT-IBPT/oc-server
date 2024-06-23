package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DBString;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;


/**
 * This class is base for all optical elements. This elements are positioned on the orbit
 * and have basic optics parameters:
 * <ul>
 * <li>position on the orbit</li>
 * <li><code>machine functions: beta, alpha, phase</code></li>
 * </ul>
 *
 * @author igor@scictrl.com
 */
public abstract class AbstractOpticalElement extends AbstractDataBushElement {
	private double position= -1.0; //distance from initial position in ring
	private double relPosition= -1.0;
	private String relFrom= "";
	private int index= -1;	//position in current collection
	private PositionedData q; //beam normalized phase
	private PositionedData beta; //beam Beta
	private PositionedData alpha; //beam Beta
	private DispersionData dispersion;
	/** Constant <code>PR_POSITION=2</code> */
	public static final int	PR_POSITION=2;
	/** Constant <code>PR_REL_POSITION=3</code> */
	public static final int	PR_REL_POSITION=3;
	/** Constant <code>PR_REL_FROM=4</code> */
	public static final int	PR_REL_FROM= 4;
/**
 * Constructs an optical element with specified name and default parameters vaues
 *
 * @param name a {@link java.lang.String} object
 */
public AbstractOpticalElement(String name) {
	super(name);
	q= new PositionedData(name,this);
	beta= new PositionedData(name,this);
	alpha= new PositionedData(name,this);
	dispersion= new DispersionData(name,this);
}
/**
 * Constructs the optical element with specified parameters.
 *
 * @param name a {@link java.lang.String} object
 * @param virtual a boolean
 * @param position a double
 * @param relpos a double
 * @param relFrom a {@link java.lang.String} object
 */
public AbstractOpticalElement(String name, boolean virtual, double position, double relpos, String relFrom) {
	super(name, virtual);
	this.position= position;
	this.relPosition= relpos;
	this.relFrom= relFrom;
	q= new PositionedData(name,this);
	beta= new PositionedData(name,this);
	alpha= new PositionedData(name,this);
	dispersion= new DispersionData(name,this);
}
/**
 * Calculates position for this element. If relative from element is specified, calculates
 * relative position, otherwise absolute is preserved.
 */
void calculatePosition(OpticsList l) throws InconsistentDataException {
	if ((relFrom!=null)&&(!relFrom.equals(""))) {
		AbstractOpticalElement oe= l.get(relFrom);
		if (oe==null) throw new InconsistentDataException(this,"position refers to \""+relFrom+"\" element, which is NOT found in DataBush");
		oe.calculatePosition(l);
		position= oe.getPosition()+relPosition;
	}
	setPositionToData();
}
/**
 * {@inheritDoc}
 *
 * Returns not initialized clone of this element. All data is copied to the clone.
 */
@Override
public Object clone() {
	AbstractOpticalElement o= (AbstractOpticalElement)super.clone();
	o.position= position;
	o.q= (PositionedData)q.clone(o);
	o.beta= (PositionedData)beta.clone(o);
	o.alpha= (PositionedData)alpha.clone(o);
	o.dispersion= (DispersionData)dispersion.clone(o);
	o.relPosition= relPosition;
	o.relFrom= relFrom;
	o.index= index;	//position in current collection
	return o;
}
/**
 * This method return machine function alpha. If the dimension of element in direction of orbit is
 * not infinitly small, this function return value of function in the center of element.
 *
 * @see si.ijs anka.databush.PositionedData
 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PositionedData} object
 */
public PositionedData getAlpha() {
		return alpha;
}
/**
 * This method return machine function beta. If the dimension of element in direction of orbit is
 * not infinitly small, this function return value of function in the center of element.
 *
 * @see si.ijs anka.databush.PositionedData
 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PositionedData} object
 */
public PositionedData getBeta() {
	return beta;
}
/**
 * This method return machine function dispersion. If the dimension of element in direction of orbit is
 * not infinitly small, this function return value of function in the center of element.
 *
 * @see si.ijs anka.databush.DispersionData
 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.DispersionData} object
 */
public DispersionData getDispersion() {
	return dispersion;
}
/**
 * This method return index of element which represent position in current collection.
 *
 * @return a int
 */
public int getIndex() {
	return index;
}
/**
 * This method return absolute position of element.
 *
 * @return a double
 */
public double getPosition() {
	return position;
}
/**
 * This method return beam normalised phase.
 *
 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PositionedData} object
 */
public PositionedData getQ() {
	return q;
}
/**
 * This method return name of element, which is reference for this element for measuring relative position.
 *
 * @return <code>java.lang.String</code> name of reference element
 * @see #getRelFrom
 */
public String getRelFrom() {
	return relFrom;
}
/**
 * This method return relative position of element.
 * For reference element see:
 *
 * @see #getRelFrom
 * @return a double
 */
public double getRelPosition() {
	return relPosition;
}
void setIndex(int i) {
	index=i;
}
/**
 * Sets position from beginnign point. This operation is valid only when DataBush
 * is ACCESS_ALL_DATA mode, otherwize IllegalAccessException
 * DataBush EXPERIMENTAL mode.
 *
 * @param value a double
 * @throws java.lang.IllegalStateException if any.
 */
public void setPosition(double value) throws IllegalStateException {
	if (!isInitialized()) {
		position=value;
	} else throw new IllegalStateException(DBString.ISE_EL_INIT);
}
/**
 *
 */
void setPositionToData() {
	beta.setPosition(position);
	alpha.setPosition(position);
	q.setPosition(position);
	dispersion.setPosition(position);
}
/**
 * Sets name of element to which is relative position referencing. This operation
 * is valid only if DataBush is in ACCESS_ALL_DATA mode. Otherwize IllegalAccessException
 * is thrown.
 *
 * @param newValue java.lang.String
 * @throws java.lang.IllegalStateException if any.
 */
public void setRelFrom(String newValue) throws IllegalStateException {
	if (!isInitialized()) {
		this.relFrom = newValue;
	} else throw new IllegalStateException(DBString.ISE_EL_INIT);
}
/**
 * Sets the relative position to relative form element. This operation
 * is valid only if DataBush is in ACCESS_ALL_DATA mode. Otherwize IllegalAccessException
 * is thrown.
 *
 * @param newValue java.lang.String
 * @throws java.lang.IllegalStateException if any.
 */
public void setRelPosition(double newValue) throws IllegalStateException {
	if (!isInitialized()) {
		this.relPosition = newValue;
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
	if (par[PR_POSITION]!=null) position= ((Double)par[PR_POSITION]).doubleValue();
	if (par[PR_REL_FROM]!=null) relFrom= par[PR_REL_FROM].toString();
	if (par[PR_REL_POSITION]!=null) relPosition= ((Double)par[PR_REL_POSITION]).doubleValue();
}
/**
 * {@inheritDoc}
 *
 * Returns a String that represents the value of this object.
 */
@Override
public String toString() {
	return super.toString()+" "+descriptor().getParameterTag(PR_POSITION)+"="+position+" "+descriptor().getParameterTag(PR_REL_POSITION)+"="+relPosition+" "+descriptor().getParameterTag(PR_REL_FROM)+"=\""+relFrom+"\"";
}
}
