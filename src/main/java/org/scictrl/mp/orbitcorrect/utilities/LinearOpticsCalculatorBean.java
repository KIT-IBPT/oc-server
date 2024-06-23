package org.scictrl.mp.orbitcorrect.utilities;

import java.util.Iterator;

import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.Orientation;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractBending;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement;
import org.scictrl.mp.orbitcorrect.model.optics.Quadrupole;
/**
 * <p>LinearOpticsCalculatorBean class.</p>
 *
 * @author igor@scictrl.com
 */
public class LinearOpticsCalculatorBean {
	private org.scictrl.mp.orbitcorrect.model.optics.DataBush dataBush;
	private double[] chromaticity = new double[2];
	private int dataPointsPerElement = 10;
	private double[][] data;
	private javax.swing.event.SwingPropertyChangeSupport listeners = new javax.swing.event.SwingPropertyChangeSupport(this);
	private double[] weights;
	private double momentumCompactFactor = 0.0;
/**
 * LinearOpticsCalculatorBean constructor comment.
 */
public LinearOpticsCalculatorBean() {
	super();
	setDataPointsPerElement(10);
}
/**
 * <p>addPropertyChangeListener.</p>
 *
 * @param l java.beans.PropertyChangeListener
 */
public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
	listeners.addPropertyChangeListener(l);
}
/**
 * <p>Getter for the field <code>chromaticity</code>.</p>
 *
 * @return double[]
 * @param ori a short
 */
public double getChromaticity(short ori) {
	return chromaticity[ori];
}
/**
 * <p>Getter for the field <code>dataBush</code>.</p>
 *
 * @return DataBush
 */
public org.scictrl.mp.orbitcorrect.model.optics.DataBush getDataBush() {
	return dataBush;
}
/**
 * <p>Getter for the field <code>dataPointsPerElement</code>.</p>
 *
 * @return int
 */
public int getDataPointsPerElement() {
	return dataPointsPerElement;
}
/**
 * <p>Getter for the field <code>momentumCompactFactor</code>.</p>
 *
 * @return double
 */
public double getMomentumCompactFactor() {
	return momentumCompactFactor;
}
/**
 * <p>init.</p>
 *
 * @param newDataBush DataBush
 */
public void init(org.scictrl.mp.orbitcorrect.model.optics.DataBush newDataBush) {
	dataBush = newDataBush;
}
/**
 * <p>printReport.</p>
 *
 * @return a {@link java.lang.String} object
 */
public String printReport() {
	java.io.StringWriter s= new java.io.StringWriter(100);
	java.io.PrintWriter p= new java.io.PrintWriter(s);
	printReport(p);
	return s.toString();
}
/**
 * <p>printReport.</p>
 *
 * @param p java.io.PrintWriter
 */
public void printReport(java.io.PrintWriter p) {
	p.print("chromaticity X\t");
	p.println(chromaticity[Orientation.HORIZONTAL.ordinal()]);
	p.print("chromaticity Z\t");
	p.println(chromaticity[Orientation.VERTICAL.ordinal()]);
	p.print("compactum factor\t");
	p.println(momentumCompactFactor);
}
/**
 *
 * @param k double
 * @param data double[][]
 */
private void processDipole(double r, double[][] data) {
	double d=0.0;
	for (int i=0; i<dataPointsPerElement; i++) {
		d+= data[DBConst.MF_D][i]*weights[i];
	}
	double h= data[DBConst.MF_POSITION][1] - data[DBConst.MF_POSITION][0];
	momentumCompactFactor+= (d*h/r);
}
/**
 *
 * @param k double
 * @param data double[][]
 */
private void processQuadrupole(double k, double[][] data) {
	double x=0.0;
	double z=0.0;
	for (int i=0; i<dataPointsPerElement; i++) {
		x+= data[DBConst.MF_BETA_X][i]*weights[i];
		z+= data[DBConst.MF_BETA_Z][i]*weights[i];
	}
	double h= data[DBConst.MF_POSITION][1] - data[DBConst.MF_POSITION][0];
	chromaticity[Orientation.HORIZONTAL.ordinal()]+= (x*k*h);
	chromaticity[Orientation.VERTICAL.ordinal()]+= (z*k*h);
}
/**
 * <p>removePropertyChangeListener.</p>
 *
 * @param l java.beans.PropertyChangeListener
 */
public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
	listeners.removePropertyChangeListener(l);
}
/**
 * <p>Setter for the field <code>dataPointsPerElement</code>.</p>
 *
 * @param newDataPointsPerElement int
 */
public void setDataPointsPerElement(int newDataPointsPerElement) {
	dataPointsPerElement = newDataPointsPerElement;
	data= new double[9][dataPointsPerElement];
	weights= new double[dataPointsPerElement];

	for (int i=3; i<dataPointsPerElement-3; i++) weights[i]=1.0;
	weights[0]=3.0/8.0;
	weights[dataPointsPerElement-1]=3.0/8.0;
	weights[1]=7.0/6.0;
	weights[dataPointsPerElement-2]=7.0/6.0;
	weights[2]=23.0/24.0;
	weights[dataPointsPerElement-3]=23.0/24.0;

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
	return super.toString();
}
/**
 * <p>update.</p>
 *
 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
 */
public void update() throws InconsistentDataException {
	if (!dataBush.getDataBushInfo().isOrbitClosed()) throw new InconsistentDataException("Orbit is not closed");

	chromaticity[0]=0.0;
	chromaticity[1]=0.0;
	momentumCompactFactor=0.0;

	AbstractOpticalElement oel;
	Iterator<AbstractOpticalElement> oit=dataBush.getOptics().iterator();

	while (oit.hasNext()) {
		if ((oel=oit.next()) instanceof Quadrupole) {
			processQuadrupole(((Quadrupole)oel).getQuadrupoleStrength(),dataBush.getMachineFunctions(data,oel,oel,10));
		} else if (oel instanceof AbstractBending) {
			processDipole(((AbstractBending)oel).getRadius(),dataBush.getMachineFunctions(data,oel,oel,10));
		}
	}

	chromaticity[0]*= -1.0/4.0/Math.PI;
	chromaticity[1]*= -1.0/4.0/Math.PI;
	momentumCompactFactor/=dataBush.getOpticsLength();

	listeners.firePropertyChange("chomaticity","0","1");
	listeners.firePropertyChange("momentumCompactFactor","0","1");
}
}
