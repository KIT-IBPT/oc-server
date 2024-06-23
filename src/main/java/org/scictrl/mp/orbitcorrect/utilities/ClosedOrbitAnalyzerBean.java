package org.scictrl.mp.orbitcorrect.utilities;

import java.util.Iterator;

import org.scictrl.mp.orbitcorrect.DBMath;
import org.scictrl.mp.orbitcorrect.DataBushInitializationException;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.Orientation;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCalibratedMagnet;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
import org.scictrl.mp.orbitcorrect.model.optics.Quadrupole;
/**
 * <p>ClosedOrbitAnalyzerBean class.</p>
 *
 * @author igor@scictrl.com
 */
public class ClosedOrbitAnalyzerBean extends PropertyChangeSupportable implements IBeamTraceProvider {
	private org.scictrl.mp.orbitcorrect.model.optics.DataBush dataBush;
	private org.scictrl.mp.orbitcorrect.model.optics.Quadrupole[] quadrupoles = new Quadrupole[0];
	private double[][] quadrupoleKicks = new double[2][0];
	private double[][][] approximatedBeamData = new double[2][2][0];
	/** Constant <code>BEAM_DISPLACMENT_ARRAY=1</code> */
	public final static int BEAM_DISPLACMENT_ARRAY = 1;
	/** Constant <code>BEAM_DISPLACMENT_LOCATION_ARRAY=0</code> */
	public final static int BEAM_DISPLACMENT_LOCATION_ARRAY = 0;
	private boolean averagedBeamUsed = false;
	private ResponseMatrix[] correctorsRM = new ResponseMatrix[2];
	private ResponseMatrix[] quadRM= new ResponseMatrix[2];
	private ElementList<AbstractCorrector>[] correctors;
	private double[] b = new double[0];
	private double[][] correctorKicks = new double[2][0];
	/** Constant <code>PROPERTY_ALL_DATA="ALL_DATA"</code> */
	public final static java.lang.String PROPERTY_ALL_DATA = "ALL_DATA";
	/** Constant <code>debug=true</code> */
	public static boolean debug = true;
	private int[] eigenvectorsUsed = {0,0};
	private double[] minimalEigenvalue = {0.001,0.001};
/**
 * ClosedOrbitAnalyzerBean constructor comment.
 */
public ClosedOrbitAnalyzerBean() {
	super();
}
/**
 * <p>Constructor for ClosedOrbitAnalyzerBean.</p>
 *
 * @param dataBush DataBush
 */
@SuppressWarnings("unchecked")
public ClosedOrbitAnalyzerBean(DataBush dataBush) {
	this();
	this.dataBush= dataBush;
	/*beamAnalyzer.addPropertyChangeListener(AbstractManipulatorBean.PROPERTY_ALL_DATA,new java.beans.PropertyChangeListener() {
		public void propertyChange(java.beans.PropertyChangeEvent e) {
			_rebuild();
		}
	});*/
	correctors= new ElementList[2];
	correctors[0]= new ElementList<>();
	correctors[1]= new ElementList<>();
}
/**
 *
 */
@SuppressWarnings("unused")
private void _rebuild() {
	ElementList<Quadrupole> l= new ElementList<>();

	AbstractOpticalElement el;

	Iterator<AbstractCalibratedMagnet> cmIt= dataBush.getCalMagnets().iterator();

	while(cmIt.hasNext()) if ((el=cmIt.next()) instanceof Quadrupole) l.add((Quadrupole) el);

	quadrupoles = new Quadrupole[l.size()];

	for (int i=0; i< l.size(); i++) quadrupoles[i]= l.get(i);

	quadrupoleKicks[Orientation._HORIZONTAL] = new double[quadrupoles.length];
	quadrupoleKicks[Orientation._VERTICAL] = new double[quadrupoles.length];

	approximatedBeamData[Orientation._HORIZONTAL][BEAM_DISPLACMENT_LOCATION_ARRAY] = new double[dataBush.getBetaList().size()];
	approximatedBeamData[Orientation._VERTICAL][BEAM_DISPLACMENT_LOCATION_ARRAY] = approximatedBeamData[Orientation._HORIZONTAL][BEAM_DISPLACMENT_LOCATION_ARRAY];
	approximatedBeamData[Orientation._HORIZONTAL][BEAM_DISPLACMENT_ARRAY] = new double[dataBush.getBetaList().size()];
	approximatedBeamData[Orientation._VERTICAL][BEAM_DISPLACMENT_ARRAY] = new double[dataBush.getBetaList().size()];

	for (int i=0; i< dataBush.getBetaList().size(); i++) approximatedBeamData[Orientation._HORIZONTAL][BEAM_DISPLACMENT_LOCATION_ARRAY][i]= dataBush.getBetaList().get(i).getPosition();

/*
	approximatedBeamData[HORIZONTAL][BEAM_DISPLACMENT_LOCATION_ARRAY] = new double[dataBush.getBPMonitors().size()];
	approximatedBeamData[VERTICAL][BEAM_DISPLACMENT_LOCATION_ARRAY] = approximatedBeamData[HORIZONTAL][BEAM_DISPLACMENT_LOCATION_ARRAY];
	approximatedBeamData[HORIZONTAL][BEAM_DISPLACMENT_ARRAY] = new double[dataBush.getBPMonitors().size()];
	approximatedBeamData[VERTICAL][BEAM_DISPLACMENT_ARRAY] = new double[dataBush.getBPMonitors().size()];

	for (int i=0; i< dataBush.getBPMonitors().size(); i++) approximatedBeamData[HORIZONTAL][BEAM_DISPLACMENT_LOCATION_ARRAY][i]= dataBush.getBPMonitors().get(i).getPosition();
*/
	ElementList<BPMonitor> m=null;
	try {
		m= new ElementList<>((BPMonitor[]) dataBush.getBPMonitors().toArray());
	} catch (DataBushInitializationException e) {
		e.printStackTrace();
	}


	correctors[Orientation._HORIZONTAL].clear();
	correctors[Orientation._VERTICAL].clear();

	Iterator<AbstractCorrector> cit= getDataBush().getCorrectors().iterator();
	AbstractCorrector cor;
	while (cit.hasNext()) {
		cor= cit.next();
		if (cor instanceof org.scictrl.mp.orbitcorrect.model.optics.HorCorrector) correctors[Orientation._HORIZONTAL].add(cor);
		else correctors[Orientation._VERTICAL].add(cor);
	}

	correctorsRM[Orientation._HORIZONTAL]= new ResponseMatrix(m,correctors[Orientation._HORIZONTAL]);
	correctorsRM[Orientation._VERTICAL]= new ResponseMatrix(m,correctors[Orientation._VERTICAL]);

	// TOSO fix this and enable
	//quadRM[Orientation._HORIZONTAL]= new ResponseMatrix(m,l);
	//quadRM[Orientation._VERTICAL]= new ResponseMatrix(m,l);

	b= new double[m.size()];

	correctorKicks[Orientation._HORIZONTAL]= new double[correctors[Orientation._HORIZONTAL].size()];
	correctorKicks[Orientation._VERTICAL]= new double[correctors[Orientation._VERTICAL].size()];

	firePropertyChange(PROPERTY_ALL_DATA);
}
/**
 *
 */
private void _updateBeam(Orientation orientation) {


	double q = read(dataBush.getQ(),orientation);
	Iterator<AbstractCorrector> it;
	AbstractCorrector c;

/*	double b=0.0;

	double[] b1= DBMath.multiply(correctorsRM[orientation].matrix,correctorKicks[orientation]);
	double[] b2= DBMath.multiply(quadRM[orientation].matrix,quadrupoleKicks[orientation]);
	for (int i=0; i<b1.length; i++) approximatedBeamData[orientation][BEAM_DISPLACMENT_ARRAY][i]= b1[i]+b2[i];

	double[] b3= new double[b1.length];
	double[] b4= new double[b1.length];


	double d=0.0;
	for (int i=0; i<dataBush.getBPMonitors().size(); i++) {
		b= Math.sqrt(read(dataBush.getBPMonitors().get(i).getBeta(),orientation)) / 2.0 / Math.sin(q * Math.PI); // 1000.0;
		it= correctors[orientation].elementIterator();
		int k=0;
		while (it.hasNext()) {
			c= (AbstractCorrector)it.next();
			b3[i] += (d=Math.sqrt(read(c.getBeta(),orientation)) * Math.cos((Math.abs(read(dataBush.getBPMonitors().get(i).getQ(),orientation) - read(c.getQ(),orientation)) * 2.0 - q) * Math.PI)) * c.getAngle();
		}
		b1[i]-=(b3[i]*b);
		for (int j=0; j<quadrupoles.length; j++) b4[i] += Math.sqrt(read(quadrupoles[j].getBeta(),orientation)) * Math.cos((Math.abs(read(dataBush.getBPMonitors().get(i).getQ(),orientation) - read(quadrupoles[j].getQ(),orientation)) * 2.0 - q) * Math.PI) * quadrupoleKicks[orientation][j];
		b2[i]-=(b4[i]*b);
		b3[i] = approximatedBeamData[orientation][BEAM_DISPLACMENT_ARRAY][i] - ((b3[i]+b4[i])*b);
	}

	int j=0;
		it= correctors[orientation].elementIterator();
		while (it.hasNext()) {
			c= (AbstractCorrector)it.next();
			j++;
		}




	for (int i=0; i<dataBush.getBPMonitors().size(); i++) {
		approximatedBeamData[orientation][BEAM_DISPLACMENT_ARRAY][i]=0.0;
		it= correctors[orientation].elementIterator();
		while (it.hasNext()) {
			c= (AbstractCorrector)it.next();
			approximatedBeamData[orientation][BEAM_DISPLACMENT_ARRAY][i] += Math.sqrt(read(c.getBeta(),orientation)) * Math.cos((Math.abs(read(dataBush.getBPMonitors().get(i).getQ(),orientation) - read(c.getQ(),orientation)) * 2.0 - q) * Math.PI) * c.getAngle();
		}
		for (int j=0; j<quadrupoles.length; j++) approximatedBeamData[orientation][BEAM_DISPLACMENT_ARRAY][i] += Math.sqrt(read(quadrupoles[j].getBeta(),orientation)) * Math.cos((Math.abs(read(dataBush.getBPMonitors().get(i).getQ(),orientation) - read(quadrupoles[j].getQ(),orientation)) * 2.0 - q) * Math.PI) * quadrupoleKicks[orientation][j];
		approximatedBeamData[orientation][BEAM_DISPLACMENT_ARRAY][i] *= Math.sqrt(read(dataBush.getBPMonitors().get(i).getBeta(),orientation)) / 2.0 / Math.sin(q * Math.PI); // 1000.0;
	}
*/
	for (int i=0; i<dataBush.getBetaList().size(); i++) {

		approximatedBeamData[orientation.ordinal()][BEAM_DISPLACMENT_ARRAY][i]=0.0;

		it= correctors[orientation.ordinal()].iterator();
		while (it.hasNext()) {
			c= it.next();
			approximatedBeamData[orientation.ordinal()][BEAM_DISPLACMENT_ARRAY][i] += Math.sqrt(read(c.getBeta(),orientation)) * Math.cos((Math.abs(read(dataBush.getQList().get(i),orientation) - read(c.getQ(),orientation)) * 2.0 - q) * Math.PI) * c.getAngle();
		}

		for (int j=0; j<quadrupoles.length; j++) approximatedBeamData[orientation.ordinal()][BEAM_DISPLACMENT_ARRAY][i] += Math.sqrt(read(quadrupoles[j].getBeta(),orientation)) * Math.cos((Math.abs(read(dataBush.getQList().get(i),orientation) - read(quadrupoles[j].getQ(),orientation)) * 2.0 - q) * Math.PI) * quadrupoleKicks[orientation.ordinal()][j];

		approximatedBeamData[orientation.ordinal()][BEAM_DISPLACMENT_ARRAY][i] *= Math.sqrt(read(dataBush.getBetaList().get(i),orientation)) / 2.0 / Math.sin(q * Math.PI); // 1000.0;

	}

/*	if (debug) {
		double[] b1= DBMath.multiply(correctorsRM[orientation].matrix,correctorKicks[orientation]);
		double[] b2= DBMath.multiply(quadRM[orientation].matrix,quadrupoleKicks[orientation]);
		for (int i=0; i<b1.length; i++) b1[i]+=b2[i];

		for (int i=0; i<dataBush.getBPMonitors().size(); i++) {
			b2[i]=0.0;
			it= correctors[orientation].elementIterator();
			while (it.hasNext()) {
				c= (AbstractCorrector)it.next();
				b2[i] += Math.sqrt(read(c.getBeta(),orientation)) * Math.cos((Math.abs(read(dataBush.getBPMonitors().get(i).getQ(),orientation) - read(c.getQ(),orientation)) * 2.0 - q) * Math.PI) * c.getAngle();
			}
			for (int j=0; j<quadrupoles.length; j++) b2[i] += Math.sqrt(read(quadrupoles[j].getBeta(),orientation)) * Math.cos((Math.abs(read(dataBush.getBPMonitors().get(i).getQ(),orientation) - read(quadrupoles[j].getQ(),orientation)) * 2.0 - q) * Math.PI) * quadrupoleKicks[orientation][j];
			b2[i] *= Math.sqrt(read(dataBush.getBPMonitors().get(i).getBeta(),orientation)) / 2.0 / Math.sin(q * Math.PI); // 1000.0;
		}
		System.out.println("Difference test");
		for (int i=0; i<b1.length; i++) System.out.println(b1[i]-b2[i]);
		System.out.println();
	}*/

}
/**
 *
 */
private void _updateQuadKicks(Orientation orientation) {
	// assamble aray of kicks from correctors
	for (int i=0; i<correctorKicks[orientation.ordinal()].length; i++) correctorKicks[orientation.ordinal()][i]= correctors[orientation.ordinal()].get(i).getAngle();

	// get closed orbit for correctors kicks
	double[] b1= DBMath.multiply(correctorsRM[orientation.ordinal()].matrix,correctorKicks[orientation.ordinal()]);

	if (debug) {
		System.out.println("Calculated orbit");
		for (int i=0; i<b1.length; System.out.println(b1[i++]));
		System.out.println();
	}

	// what is the difference betvean measured orbit
	// TODO fix and enable
	//if (averagedBeamUsed) for (int i=0; i<b1.length; i++) b[i]=beamAnalyzer.getAvgPositions(orientation)[i]-b1[i];
	//else for (int i=0; i<b1.length; i++) b[i]=beamAnalyzer.getPositions(orientation)[i]-b1[i];

	if (debug) {
		System.out.println("Difference= measured-calculated orbit");
		for (int i=0; i<b.length; System.out.println(b[i++]));
		System.out.println();
	}


	// from difference get kicks on quadrupoles
	double[][] u= DBMath.clone(quadRM[orientation.ordinal()].matrix);
	double[][] v= new double[quadRM[orientation.ordinal()].correctors.length][quadRM[orientation.ordinal()].correctors.length];
	double[] ein= new double[quadRM[orientation.ordinal()].correctors.length];

	SVDMethod.svdcmp(u,ein,v);

	eigenvectorsUsed[orientation.ordinal()]= ein.length;
	for (int i=0; i<ein.length; i++) if (ein[i]<minimalEigenvalue[orientation.ordinal()]) {
		ein[i]= 0.0;
		eigenvectorsUsed[orientation.ordinal()]--;
	}

	quadrupoleKicks[orientation.ordinal()]= SVDMethod.solve(u,ein,v,b);

	if (debug) {
		b1= DBMath.multiply(correctorsRM[orientation.ordinal()].matrix,correctorKicks[orientation.ordinal()]);
		double[] b2= DBMath.multiply(quadRM[orientation.ordinal()].matrix,quadrupoleKicks[orientation.ordinal()]);
		for (int i=0; i<b1.length; i++) b1[i]+=b2[i];
		System.out.println("Approximated orbit");
		for (int i=0; i<b1.length; System.out.println(b1[i++]));
		System.out.println();

		// TODO fix and enable
		//if (averagedBeamUsed) for (int i=0; i<b1.length; i++) b1[i]=beamAnalyzer.getAvgPositions(orientation)[i]-b1[i];
		//else for (int i=0; i<b1.length; i++) b1[i]=beamAnalyzer.getPositions(orientation)[i]-b1[i];

		System.out.println("Difference= measured-approximated orbit");
		for (int i=0; i<b1.length; System.out.println(b1[i++]));
		System.out.println();
	}
}
/**
 *
 */
private void _updateRM() throws InconsistentDataException {

	if (!dataBush.hasClosedOrbitSolution()) throw new InconsistentDataException("DataBush has not closed orbit solution at this moment!");

	ResponseMatrix.fillWithCloseOrbitCalculation(correctorsRM[Orientation._HORIZONTAL],dataBush,Orientation.HORIZONTAL);
	ResponseMatrix.fillWithCloseOrbitCalculation(correctorsRM[Orientation._VERTICAL],dataBush,Orientation.VERTICAL);

	ResponseMatrix.fillWithCloseOrbitCalculation(quadRM[Orientation._HORIZONTAL],dataBush,Orientation.HORIZONTAL);
	ResponseMatrix.fillWithCloseOrbitCalculation(quadRM[Orientation._VERTICAL],dataBush,Orientation.VERTICAL);
}
/**
 * <p>Getter for the field <code>approximatedBeamData</code>.</p>
 *
 * @return double[][][]
 * @param orientation a {@link org.scictrl.mp.orbitcorrect.Orientation} object
 */
public double[][] getApproximatedBeamData(Orientation orientation) {
	return approximatedBeamData[orientation.ordinal()];
}
/** {@inheritDoc} */
@Override
public double[][] getBeamTrace(Orientation orientation) {
	return getApproximatedBeamData(orientation);
}
/**
 * <p>Getter for the field <code>correctorKicks</code>.</p>
 *
 * @return double[][]
 * @param orientation a {@link org.scictrl.mp.orbitcorrect.Orientation} object
 */
public double[] getCorrectorKicks(Orientation orientation) {
	return correctorKicks[orientation.ordinal()];
}
/**
 * <p>Getter for the field <code>correctors</code>.</p>
 *
 * @return ElementList[]
 * @param orientation a {@link org.scictrl.mp.orbitcorrect.Orientation} object
 */
public ElementList<AbstractCorrector> getCorrectors(Orientation orientation) {
	return correctors[orientation.ordinal()];
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
 * <p>Getter for the field <code>eigenvectorsUsed</code>.</p>
 *
 * @return int
 * @param orientation a {@link org.scictrl.mp.orbitcorrect.Orientation} object
 */
public int getEigenvectorsUsed(Orientation orientation) {
	return eigenvectorsUsed[orientation.ordinal()];
}
/**
 * <p>Getter for the field <code>minimalEigenvalue</code>.</p>
 *
 * @return double
 * @param orientation a {@link org.scictrl.mp.orbitcorrect.Orientation} object
 */
public double getMinimalEigenvalue(Orientation orientation) {
	return minimalEigenvalue[orientation.ordinal()];
}
/**
 * <p>Getter for the field <code>quadrupoleKicks</code>.</p>
 *
 * @return double[][]
 * @param orientation a {@link org.scictrl.mp.orbitcorrect.Orientation} object
 */
public double[] getQuadrupoleKicks(Orientation orientation) {
	return quadrupoleKicks[orientation.ordinal()];
}
/**
 * <p>Getter for the field <code>quadrupoles</code>.</p>
 *
 * @return Quadrupole[][]
 */
public org.scictrl.mp.orbitcorrect.model.optics.Quadrupole[] getQuadrupoles() {
	return quadrupoles;
}
/**
 * <p>isAveragedBeamUsed.</p>
 *
 * @return boolean
 */
public boolean isAveragedBeamUsed() {
	return averagedBeamUsed;
}
/**
 *
 * @return double
 * @param data PositionedData
 * @param orientation int
 */
private static double read(org.scictrl.mp.orbitcorrect.model.optics.PositionedData data, Orientation orientation) {
	if (orientation==Orientation.VERTICAL) return data.z();
	else return data.x();
}
/**
 * <p>Setter for the field <code>averagedBeamUsed</code>.</p>
 *
 * @param newAveragedBeamUsed boolean
 */
public void setAveragedBeamUsed(boolean newAveragedBeamUsed) {
	averagedBeamUsed = newAveragedBeamUsed;
}
/**
 * <p>Setter for the field <code>minimalEigenvalue</code>.</p>
 *
 * @param newMinimalEigenvalue double
 * @param orientation a {@link org.scictrl.mp.orbitcorrect.Orientation} object
 */
public void setMinimalEigenvalue(double newMinimalEigenvalue, Orientation orientation) {
	minimalEigenvalue[orientation.ordinal()] = newMinimalEigenvalue;
}
/**
 * <p>updateBeam.</p>
 *
 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
 */
public void updateBeam() throws InconsistentDataException {
	_updateRM();
	_updateQuadKicks(Orientation.HORIZONTAL);
	_updateQuadKicks(Orientation.VERTICAL);
	_updateBeam(Orientation.HORIZONTAL);
	_updateBeam(Orientation.VERTICAL);
}
}
