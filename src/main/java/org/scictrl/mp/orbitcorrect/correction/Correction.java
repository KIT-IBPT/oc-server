package org.scictrl.mp.orbitcorrect.correction;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.scictrl.mp.orbitcorrect.Orientation;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
import org.scictrl.mp.orbitcorrect.model.optics.RFGenerator;
/**
 * Class holding correction information.
 * It is result of correction calculation and it is applied to the beam by the OrbitCorrectionController.
 *
 * @author igor@scictrl.com
 */
public final class Correction {

	private final int eigenvectorsUsed;
	private final double[] correction;
	private final double[] scaledCorrection;
	private final double maxScaledCorrection;
	private final double scale;
	private final double usedScale;
	private final ElementList<AbstractCorrector> correctors;
	private final RFGenerator rfGenerator;
	//private final List<Integer> outOfRangeIndeces;
	private final Orientation ori;
	private final boolean nan;
	private final double[] eigenvalues;
	/**
	 * Constructor
	 *
	 * @param corr a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @param rf a {@link org.scictrl.mp.orbitcorrect.model.optics.RFGenerator} object
	 * @param correction an array of {@link double} objects
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @param scale a double
	 * @param eigenvaluesUsed a int
	 * @param eigenvalues an array of {@link double} objects
	 */
	public Correction(final ElementList<AbstractCorrector> corr, final RFGenerator rf, final double[] correction, final Orientation ori, final double scale, final int eigenvaluesUsed, final double[] eigenvalues) {
		super();
		this.correctors=corr;
		this.rfGenerator=rf;
		this.correction=correction;
		this.ori=ori;
		this.scale=scale;
		this.eigenvectorsUsed=eigenvaluesUsed;
		this.eigenvalues=eigenvalues;

		scaledCorrection= new double[correction.length];

		//outOfRangeIndeces= new ArrayList<Integer>(correction.length);

		double s= testValuesInRange();

		if (s<scale) {
			usedScale=s;
		} else {
			usedScale=scale;
		}

		boolean n=false;
		double max=0.0;
		for (int i=0; i< correction.length ; i++) {
			n = n || Double.isNaN(correction[i]);
			scaledCorrection[i] = usedScale * correction[i];
			double d= Math.abs(scaledCorrection[i]);
			if (d>max) {
				max=d;
			}
		}
		maxScaledCorrection=max;
		nan=n;
	}

	/**
	 * <p>Getter for the field <code>eigenvectorsUsed</code>.</p>
	 *
	 * @return a int
	 */
	public int getEigenvectorsUsed() {
		return eigenvectorsUsed;
	}

	/**
	 * Maximal absolute correction value in this correction array.
	 *
	 * @return a double
	 */
	public double getMaxScaledCorrection() {
		return maxScaledCorrection;
	}

	/**
	 * <p>getOrientation.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 */
	public Orientation getOrientation() {
		return ori;
	}

	/**
	 * <p>getTarget.</p>
	 *
	 * @param i a int
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement} object
	 */
	public AbstractDataBushElement getTarget(int i) {
		if (i<correctors.size()) {
			return correctors.get(i);
		} else {
			return rfGenerator;
		}
	}
	/**
	 * <p>Getter for the field <code>correction</code>.</p>
	 *
	 * @return double[][]
	 */
	public double[] getCorrection() {
		return correction;
	}

	/**
	 * <p>Getter for the field <code>eigenvalues</code>.</p>
	 *
	 * @return an array of {@link double} objects
	 */
	public double[] getEigenvalues() {
		return eigenvalues;
	}

	/**
	 * <p>Getter for the field <code>correctors</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public ElementList<AbstractCorrector> getCorrectors() {
		return correctors;
	}

	/**
	 * <p>Getter for the field <code>rfGenerator</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.RFGenerator} object
	 */
	public RFGenerator getRfGenerator() {
		return rfGenerator;
	}

	/*public List<Integer> getOutOfRangeIndeces() {
		return outOfRangeIndeces;
	}*/
	/**
	 * <p>Getter for the field <code>scale</code>.</p>
	 *
	 * @return a double
	 */
	public double getScale() {
		return scale;
	}
	/**
	 * <p>Getter for the field <code>scaledCorrection</code>.</p>
	 *
	 * @return an array of {@link double} objects
	 */
	public double[] getScaledCorrection() {
		return scaledCorrection;
	}
	/**
	 * <p>Getter for the field <code>usedScale</code>.</p>
	 *
	 * @return a double
	 */
	public double getUsedScale() {
		return usedScale;
	}
	/**
	 * <p>testValuesInRange.</p>
	 *
	 * @return a double
	 */
	public double testValuesInRange() {

		double s=1.0;
		double sm=1.0;

		List<Integer> out= new ArrayList<>(correction.length);

		for (int i=0; i< correction.length; i++) {
			if (i<correctors.size()) {

				AbstractCorrector ac= correctors.get(i);
				double val= correction[i];
				try {
					if (val>0.0) {
						s= (ac.getMaxAngle()-ac.getAngle())/val;
					} else {
						s= (ac.getMinAngle()-ac.getAngle())/val;
					}
					s= Math.floor(Math.abs(s)*1000.0)/1000.0;
					if (Double.isNaN(s)||Double.isInfinite(s)) s= Double.MAX_VALUE;
					if (s<scale) out.add(i);
				} catch (Exception ex) {
					out.add(i);
					LogManager.getLogger(this.getClass()).error("Erroe while checking range: "+ex.toString(), ex);
				}
				if (i==0) sm=s;
				else if (s<sm) sm= s;

			} else if (rfGenerator!=null) {
				double val= correction[i];
				try {
					if (val>0.0) {
						s= (rfGenerator.getMaxFrequency()-rfGenerator.getFrequency())/val;
					} else {
						s= (rfGenerator.getMinFrequency()-rfGenerator.getFrequency())/val;
					}
					s= Math.floor(Math.abs(s)*1000.0)/1000.0;
					if (Double.isNaN(s)||Double.isInfinite(s)) {
						s= Double.MAX_VALUE;
					}
					if (s<scale) out.add(i);
				} catch (Exception ex) {
					out.add(i);
					LogManager.getLogger(this.getClass()).error("Error while checking range: "+ex.toString(), ex);
				}
				if (i==0) sm=s;
				else if (s<sm) sm= s;
			}

		}
		return sm;
	}

	/**
	 * <p>print.</p>
	 *
	 * @param pw a {@link java.io.PrintWriter} object
	 */
	public void print(PrintWriter pw) {
		Formatter f= new Formatter(pw);
		pw.print("Correction:{");
		pw.print("Ori: ");
		pw.print(ori);
		pw.print(", Req. Scale: ");
		pw.print(scale);
		pw.print(", Allow. Scale: ");
		pw.print(usedScale);
		pw.print(", Eigenvec. Used: ");
		pw.print(eigenvectorsUsed);
		pw.print(", Max Corr.: ");
		pw.print(maxScaledCorrection);
		pw.print(", Scaled Corr.: ");
		f.format("%.3f", scaledCorrection[0]);
		for (int i = 1; i < correction.length; i++) {
			pw.print(", ");
			f.format("%.3f", scaledCorrection[i]);
		}
		pw.println("}");
		f.close();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringWriter sw= new StringWriter();
		PrintWriter pw= new PrintWriter(sw);
		print(pw);
		return sw.toString();
	}

	/**
	 * <p>isNaN.</p>
	 *
	 * @return a boolean
	 */
	public boolean isNaN() {
		return nan;
	}
}
