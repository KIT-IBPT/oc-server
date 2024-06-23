package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.ISimpleElement;


/**
 * This is class that contains data for some <code>AbstractOpticalElement</code>.
 *
 * @author igor@scictrl.com
 */
public class SimpleData implements ISimpleElement {
	/** X position */
	protected double x;

	/** Z position */
	protected double z;

	/** Data source element. */
	protected AbstractOpticalElement source;

	/** Data holder. */
	protected DataHolder dataHolder;

	private String name;
	private double position= 0.0;
	/**
	 * <code>SimpleData</code> constructor.
	 *
	 * @param name String
	 */
	public SimpleData(String name) {
		this(name,null);
	}
	/**
	 * <code>SimpleData</code> constructor.
	 *
	 * @param name String
	 * @param source AbstractOpticalElement
	 */
	public SimpleData(String name, AbstractOpticalElement source) {
		this.name= name;
		this.source= source;
	}
	/**
	 * <code>SimpleData</code> constructor.
	 *
	 * @param name String
	 * @param source AbstractOpticalElement
	 * @param x double
	 * @param z double
	 */
	public SimpleData(String name, AbstractOpticalElement source, double x, double z) {
		this(name,source);
		set(x,z);
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method creates and returns copy of this object. Source of returned copy is source of this class.
	 */
	@Override
	public Object clone() {
		SimpleData o= null;
		try {
			o= (SimpleData)super.clone();
			o.source=null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return o;
	}
	/**
	 * This method creates and returns copy of this object. Source of returned copy is specified as parameter.
	 * @param source AbstractOpticalElement
	 */
	Object clone(AbstractOpticalElement source) {
		SimpleData o= (SimpleData)clone();
		o.source= source;
		return o;
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method returns name, specified by constructor.
	 */
	@Override
	public String getName() {
		return name;
	}
	/**
	 * This method returns field values x and z as array.
	 *
	 * @return an array of {@link double} objects
	 */
	public double[] getPair() {
		double[] a= new double[2];
		a[0]= x;
		a[1]= z;
		return a;
	}
	/**
	 * This method returns position of optical element, whose data is this <code>SimpleData</code>.
	 *
	 * @return double position
	 */
	public double getPosition() {
		return position;
	}
	/**
	 * This method returns source <code>AbstractOpticalElement</code> (AbstractOpticalElement, whose data is this SimpleData).
	 *
	 * @return AbstractOpticalElement
	 */
	public AbstractOpticalElement getSource() {
		return source;
	}
	/**
	 * This method sets field values x and z.
	 */
	void set(double x, double z) {
		this.x=x;
		this.z=z;
	}
	/**
	 * This method sets position.
	 */
	void setPosition(double pos) {
		position= pos;
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method returns string that describe this object. Actually it returns fields x, z and tab space between as a string.
	 */
	@Override
	public String toString() {
		return x+"\t"+z;
	}
}
