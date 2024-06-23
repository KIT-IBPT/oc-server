package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.DBElementDescriptor;
import org.scictrl.mp.orbitcorrect.math.RDipoleMatrix;
import org.scictrl.mp.orbitcorrect.math.TransferMatrix;
/**
 * This type represent rectangular dipole magnet. It has parallel side faces and additional wedge foccusing.
 *
 * @author igor@scictrl.com
 */
public class RBending extends AbstractBending {

	/** Constant <code>PR_LEFT_WEDGE_ANGLE=12</code> */
	public static final int	PR_LEFT_WEDGE_ANGLE=12;
	/** Constant <code>PR_RIGHT_WEDGE_ANGLE=13</code> */
	public static final int	PR_RIGHT_WEDGE_ANGLE=13;

	private double leftWedgeAngle;
	private double rightWedgeAngle;



/**
 * Constructs <code>RBending</code> with specified name and default parameter's values.
 *
 * @param name a {@link java.lang.String} name of abstract bending
 */
public RBending(String name) {
	super(name);
}
/**
 * Constructs the <code>RBending</code> with specified parameters.
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
 * @param lwedge a double
 * @param rwedge a double
 */
public RBending(String name, boolean virtual, double position, double relpos, String relFrom, double length, String ps, String calibrationEntry, double quadrupoleStrength, double radius, double dipoleField, double energy, double lwedge, double rwedge) {
	super(name, virtual, position, relpos, relFrom,length, ps, calibrationEntry, quadrupoleStrength, radius, dipoleField, energy);

	this.rightWedgeAngle= rwedge;
	this.leftWedgeAngle= lwedge;

	if (rightWedgeAngle<0.0) {
		rightWedgeAngle= length/radius/2.0;
	}

	if (leftWedgeAngle<0.0) {
		leftWedgeAngle= length/radius/2.0;
	}
}
/**
 * {@inheritDoc}
 *
 * <p>descriptor.</p>
 * @see AbstractDataBushElement#descriptor
 */
@Override
public DBElementDescriptor descriptor() {
	return DBElementDescriptor.ELDES_RBENDING;
}
/**
 * {@inheritDoc}
 *
 * <p>elType.</p>
 * @see AbstractDataBushElement#elType
 */
@Override
public int elType() {
	return DBElementDescriptor.EL_RBENDING;
}
/**
 * {@inheritDoc}
 *
 * This method return transfer matrix, calculated with current length, quadrupole strength and radius.
 * @see org.scictrl.mp.orbitcorrect.math.TransferMatrix
 */
@Override
public TransferMatrix newMatrix() {
	return new RDipoleMatrix(length,quadrupoleStrength,radius,leftWedgeAngle,rightWedgeAngle);
}
/**
 * {@inheritDoc}
 *
 * This method return transfer matrix, calculated with current quadrupole strength, radius and as parameter specified length.
 * @see org.scictrl.mp.orbitcorrect.math.TransferMatrix
 */
@Override
public TransferMatrix newMatrix(double length) {
	return new RDipoleMatrix(length,quadrupoleStrength,radius,0.0,0.0);
}
	/**
	 * {@inheritDoc}
	 *
	 * Returns a String that represents the value of this object.
	 */
	@Override
	public String toString() {
		// Insert code to print the receiver here.
		// This implementation forwards the message to super. You may replace or supplement this.
		return super.toString()+" "+descriptor().getParameterTag(PR_LEFT_WEDGE_ANGLE)+"="+leftWedgeAngle+" "+descriptor().getParameterTag(PR_RIGHT_WEDGE_ANGLE)+"="+rightWedgeAngle+" >"+DBConst.EOL;
	}
	/**
	 *
	 */
	@Override
	void updateMatrix() {
		((RDipoleMatrix)matrix).set(length,getQuadrupoleStrength(),radius,leftWedgeAngle,rightWedgeAngle);
	}

	/**
	 * <p>Getter for the field <code>leftWedgeAngle</code>.</p>
	 *
	 * @return a double
	 */
	public double getLeftWedgeAngle() {
		return leftWedgeAngle;
	}

	/**
	 * <p>Getter for the field <code>rightWedgeAngle</code>.</p>
	 *
	 * @return a double
	 */
	public double getRightWedgeAngle() {
		return rightWedgeAngle;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>setWith.</p>
	 */
	@Override
	public void setWith(Object[] par) throws IllegalStateException {
		super.setWith(par);
		if (par[PR_LEFT_WEDGE_ANGLE]!=null) leftWedgeAngle= ((Double)par[PR_LEFT_WEDGE_ANGLE]).doubleValue();
		if (par[PR_RIGHT_WEDGE_ANGLE]!=null) rightWedgeAngle= ((Double)par[PR_RIGHT_WEDGE_ANGLE]).doubleValue();

		if (rightWedgeAngle<0.0) {
			rightWedgeAngle= length/radius/2.0;
		}

		if (leftWedgeAngle<0.0) {
			leftWedgeAngle= length/radius/2.0;
		}

		updateMatrix();

	}

}
