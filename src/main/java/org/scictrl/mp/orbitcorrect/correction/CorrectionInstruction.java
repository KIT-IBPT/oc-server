/**
 *
 */
package org.scictrl.mp.orbitcorrect.correction;

import java.time.Instant;

import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
import org.scictrl.mp.orbitcorrect.model.optics.RFGenerator;

/**
 * CorrectionInstruction is created for particular correction and holds state of
 * correction while and after has been applied. Can reverse a partial or full
 * correction.
 *
 * @author igor@scictrl.com
 */
public final class CorrectionInstruction {

	private final Correction correction;
	private final int ori;

	private int steps;
	private double[] increments;
	private double[] initial;
	private double[] last;
	private boolean rf=false;
	private Instant start;

	/**
	 * <p>Constructor for CorrectionInstruction.</p>
	 *
	 * @param correction correction object
	 */
	public CorrectionInstruction(Correction correction) {
		this.correction = correction;
		this.ori = correction.getOrientation().ordinal();
		this.start= Instant.now();
	}

	/**
	 * <p>invert.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.correction.CorrectionInstruction} object
	 */
	public CorrectionInstruction invert() {
		double[] corr= new double[initial.length];

		for (int i = 0; i < corr.length; i++) {
			corr[i] = initial[i] - last[i];
		}

		Correction c= new Correction(correction.getCorrectors(), correction.getRfGenerator(), corr, correction.getOrientation(), 1.0, 0, null);

		CorrectionInstruction ci= new CorrectionInstruction(c);

		return ci;
	}

	/**
	 * <p>Getter for the field <code>correction</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.correction.Correction} object
	 */
	public Correction getCorrection() {
		return correction;
	}

	/**
	 * <p>steps.</p>
	 *
	 * @return a int
	 */
	public int steps() {
		return steps;
	}

	/**
	 * <p>Getter for the field <code>increments</code>.</p>
	 *
	 * @return an array of {@link double} objects
	 */
	public double[] getIncrements() {
		return increments;
	}

	/**
	 * <p>ori.</p>
	 *
	 * @return a int
	 */
	public int ori() {
		return ori;
	}

	/**
	 * <p>calculateSteps.</p>
	 *
	 * @param maxStep a double
	 * @param maxNumberOfSteps a int
	 */
	public void calculateSteps(final double maxStep, final int maxNumberOfSteps) {

		final double[] corr= correction.getScaledCorrection();

		double a=0.0;

		for (double element : corr) {
			if ((Math.abs(element)>a)) {
				a= Math.abs(element);
			}
		}

		a= Math.ceil(a/maxStep);

		steps = (int)a;

		increments= new double[corr.length];

		for (int i=0; i<increments.length; i++) increments[i]=corr[i]/a;

		if (maxNumberOfSteps>0 && steps>maxNumberOfSteps) {
			steps=maxNumberOfSteps;
		}
	}

	/**
	 * <p>prepareStep.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public ElementList<AbstractDataBushElement> prepareStep() {
		boolean init = false;

		final double[] corr = correction.getScaledCorrection();

		if (initial == null) {
			init = true;
			initial = new double[increments.length];
			last = new double[increments.length];
		}

		ElementList<AbstractDataBushElement> set = new ElementList<>();

		for (int j = 0; j < corr.length; j++) {
			AbstractDataBushElement dbe = correction.getTarget(j);

			set.add(dbe);

			if (dbe instanceof AbstractCorrector) {

				double val = ((AbstractCorrector) dbe).getAngle();

				if (init) {
					initial[j] = val;
					last[j] = val;
				}
				((AbstractCorrector) dbe).setAngle(last[j] = val + increments[j]);

			} else if (dbe instanceof RFGenerator) {
				double val = ((RFGenerator) dbe).getFrequency();
				if (init) {
					initial[j] = val;
					last[j] = val;
				}
				((RFGenerator) dbe).setFrequency(last[j] = val + increments[j]);
				rf = true;
			}
		}
		return set;
	}

	/**
	 * <p>hasRF.</p>
	 *
	 * @return a boolean
	 */
	public boolean hasRF() {
		return rf;
	}

	/**
	 * <p>Getter for the field <code>start</code>.</p>
	 *
	 * @return a {@link java.time.Instant} object
	 */
	public Instant getStart() {
		return start;
	}

}
