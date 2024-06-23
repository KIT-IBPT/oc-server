package org.scictrl.mp.orbitcorrect.model.optics;

import java.util.ArrayList;
import java.util.List;

import org.scictrl.mp.orbitcorrect.DBString;
import org.scictrl.mp.orbitcorrect.math.DriftMatrix;
import org.scictrl.mp.orbitcorrect.math.TransferMatrix;
/**
 * Transfer elements are all elements that transfer electron beam. Their dimension in
 * direction of electron beam is not zero.
 *
 * @author igor@scictrl.com
 */
public abstract class AbstractTransferElement extends AbstractOpticalElement {
	/** Constant <code>PR_LENGTH=5</code> */
	public static final int PR_LENGTH=5;

	/** Main transfer matrix. */
	protected TransferMatrix matrix;

	/** Matrices which depend on this element for updates. */
	protected List<TransferMatrix> matrices = new ArrayList<>();

	/** Length of this element. */
	protected double length;

	private PositionedData q1; //beam normalized phase
	private PositionedData beta1; //beam Beta
	private PositionedData alpha1; //beam Beta
	private DispersionData dispersion1;
	private PositionedData q2; //beam normalized phase
	private PositionedData beta2; //beam Beta
	private PositionedData alpha2; //beam Beta
	private DispersionData dispersion2;

	/**
	 * Constructs transfer element with specified name and default parameter's values.
	 *
	 * @param name a {@link java.lang.String} name
	 */
	public AbstractTransferElement(String name) {
		super(name);
		q1= new PositionedData(name+"-1",this);
		beta1= new PositionedData(name+"-1",this);
		alpha1= new PositionedData(name+"-1",this);
		dispersion1= new DispersionData(name+"-1",this);
		q2= new PositionedData(name+"-2",this);
		beta2= new PositionedData(name+"-2",this);
		alpha2= new PositionedData(name+"-2",this);
		dispersion2= new DispersionData(name+"-2",this);
		matrix= newMatrix();
	}

	/**
	 * Constructs the <code>AbstractTransferElement</code> with specified parameters.
	 *
	 * @param name a {@link java.lang.String} object
	 * @param virtual a boolean
	 * @param position a double
	 * @param relpos a double
	 * @param relFrom a {@link java.lang.String} object
	 * @param length a double
	 */
	public AbstractTransferElement(String name, boolean virtual, double position, double relpos, String relFrom, double length) {
		super(name,virtual,position,relpos,relFrom);
		this.length= length;
		q1= new PositionedData(name+"-1",this);
		beta1= new PositionedData(name+"-1",this);
		alpha1= new PositionedData(name+"-1",this);
		dispersion1= new DispersionData(name+"-1",this);
		q2= new PositionedData(name+"-2",this);
		beta2= new PositionedData(name+"-2",this);
		alpha2= new PositionedData(name+"-2",this);
		dispersion2= new DispersionData(name+"-2",this);
		matrix= newMatrix();
	}

	/**
	 *
	 */
	TransferMatrix addMatrix(TransferMatrix m) {
		matrices.add(m);
		return m;
	}

	/**
	 *
	 */
	void clearMatrices() {
		matrices.clear();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>clone.</p>
	 * @see AbstractDataBushElement#clone
	 */
	@Override
	public Object clone() {
		AbstractTransferElement o= (AbstractTransferElement)super.clone();
		o.matrix= (TransferMatrix)matrix.clone();
		o.length= length;
	//	o.insidePartition= insidePartition;
		return o;
	}

	/**
	 * This method return machine function alpha in the beginning of the transfer element.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PositionedData} object
	 */
	public PositionedData getAlpha1() {
			return alpha1;
	}

	/**
	 * This method  return machine function alpha in the end of the transfer element.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PositionedData} object
	 */
	public PositionedData getAlpha2() {
			return alpha2;
	}

	/**
	 * This method return machine function beta in the beginning of the transfer element.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PositionedData} object
	 */
	public PositionedData getBeta1() {
		return beta1;
	}

	/**
	 * This method return machine function beta in the end of the transfer element.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PositionedData} object
	 */
	public PositionedData getBeta2() {
		return beta2;
	}

	/**
	 * This method return machine function dispersion in the beginning of the transfer element.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.DispersionData} object
	 */
	public DispersionData getDispersion1() {
		return dispersion1;
	}

	/**
	 * This method return machine function dispersion in the end of the transfer element.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.DispersionData} object
	 */
	public DispersionData getDispersion2() {
			return dispersion2;
	}

	/**
	 * This method return length of the element.
	 *
	 * @return a double
	 */
	public double getLength() {
		return length;
	}

	/**
	 * This method return transfer matrix of the element.
	 *
	 * @see org.scictrl.mp.orbitcorrect.math.TransferMatrix
	 * @return a {@link org.scictrl.mp.orbitcorrect.math.TransferMatrix} object
	 */
	public TransferMatrix getMatrix() {
		return matrix;
	}

	/**
	 * This method return normalised beam phase in teh beginning of the elemnet.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PositionedData} object
	 */
	public PositionedData getQ1() {
		return q1;
	}

	/**
	 * This method return normalised beam phase in teh end of the elemnet.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PositionedData} object
	 */
	public PositionedData getQ2() {
		return q2;
	}

	/**
	 * This method return transfer matrix of drift with the same length as this element.
	 *
	 * @see org.scictrl.mp.orbitcorrect.math.DriftMatrix
	 * @return a {@link org.scictrl.mp.orbitcorrect.math.TransferMatrix} object
	 */
	public TransferMatrix newMatrix() {
		return new DriftMatrix(length);
	}

	/**
	 * This method return transfer matrix of drift with the specified length.
	 *
	 * @see org.scictrl.mp.orbitcorrect.math.DriftMatrix
	 * @param length a double
	 * @return a {@link org.scictrl.mp.orbitcorrect.math.TransferMatrix} object
	 */
	public TransferMatrix newMatrix(double length) {
		return new DriftMatrix(length);
	}

	/**
	 *
	 */
	TransferMatrix removeMatrix(TransferMatrix m) {
		matrices.remove(m);
		return m;
	}

	/**
	 * Sets the length of element. This operation is valid only when DataBush is in
	 * ACCESS_ALL_DATA mode, otherwize IllegalAccessException is thrown.
	 *
	 * @param value a double
	 * @throws java.lang.IllegalStateException if any.
	 */
	public void setLength(double value) throws IllegalStateException {
		if (!isInitialized()) {
			length= value;
			updateMatrix();
		} else throw new IllegalStateException(DBString.ISE_EL_INIT);
	}

	/**
	 *
	 */
	@Override
	void setPositionToData() {
		super.setPositionToData();
		double pos=getPosition()-length/2.0;
		beta1.setPosition(pos);
		alpha1.setPosition(pos);
		q1.setPosition(pos);
		dispersion1.setPosition(pos);
		pos=getPosition()+length/2.0;
		beta2.setPosition(pos);
		alpha2.setPosition(pos);
		q2.setPosition(pos);
		dispersion2.setPosition(pos);
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
		if (par[PR_LENGTH]!=null) if (0.0!=((Double)par[PR_LENGTH]).doubleValue()) setLength(((Double)par[PR_LENGTH]).doubleValue());
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>toString.</p>
	 * @see AbstractDataBushElement#toString
	 */
	@Override
	public String toString() {
		return super.toString()+" "+descriptor().getParameterTag(PR_LENGTH)+"="+length;
	}

	/**
	 *
	 */
	void updateMatrix() {
		((DriftMatrix)matrix).setLength(length);
	}

	/**
	 *
	 */
	TransferMatrix updateMatrix(TransferMatrix m) {
		return m;
	}
}
