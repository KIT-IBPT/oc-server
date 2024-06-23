package org.scictrl.mp.orbitcorrect.correction.automatic;

import org.apache.commons.configuration.Configuration;
import org.scictrl.mp.orbitcorrect.ControlSystemException;
import org.scictrl.mp.orbitcorrect.Orientation;
import org.scictrl.mp.orbitcorrect.accessories.Utilities;
import org.scictrl.mp.orbitcorrect.correction.Correction;
import org.scictrl.mp.orbitcorrect.utilities.Orbit;


/**
 * <p> Step based automation model. Does predefined steps of selected correction method.
 *
 * @author igor@scictrl.com
 */
public class StepAutomaticOC extends AbstractAutomaticOCModel {
	/** Constant <code>AOC_MIN_CORRECTOR="aoc.minCorrector"</code> */
	public static final String AOC_MIN_CORRECTOR = "aoc.minCorrector";

	/**
	 * If calculated corrector changes in mrad are lower than this, then correction is skipped and
	 * goes into relaxation.
	 */
	protected double minCorrectorChange=0.001;

	/**
	 * TimeBasedAutomaticOC constructor comment.
	 */
	public StepAutomaticOC() {
		super();
		name= "Step Correction";
	}

	/**
	 * Implementing method, which executes correction for specified orientation
	 * until new rms is not worse or equal.
	 *
	 * @param operator
	 *            the operator for this process
	 * @param count
	 *            controls how deep this recursion goes. If less then 0, then
	 *            there is no limit, if more than 0, then can go deeper for
	 *            given count.
	 * @param dryRun a boolean
	 */
	protected void executeLoopStep(final AutomaticOrbitCorrectionOperator operator, final int count, final boolean dryRun) {

		if ((count == 0) || !operator.isStateActive()) {
			return;
		}

		try {
			operator.getController().checkBeamCurrent(operator);
		} catch (Exception e) {
			log.error("Failed to read beam current: '" + e+"'.", e);
			operator.requestAbort(
				"Failed to read beam current, aborting: '" + e+"'.",
				this.getClass(), e);
			return;
		}

		if (!operator.isStateActive()) {
			return;
		}

		Orbit preOrbit = operator.getCurrentOrbit();

		double rh = preOrbit.getRms(Orientation.HORIZONTAL);
		double rv = preOrbit.getRms(Orientation.VERTICAL);

		if (Double.isNaN(rh) || Double.isInfinite(rh) || Double.isNaN(rv) || Double.isInfinite(rv)) {
			operator.requestAbort("RMS is NaN, might be no beam.", this.getClass(), null);
			return;
		}

		@SuppressWarnings("unused")
		boolean doneH=false;
		if (operator.getController().isCorrectHorizontal())	{

			if (!operator.getController().checkDevices(operator, Orientation.H, true)) {
				return;
			}

			doneH=executeCorectionStep(Orientation.H, operator, dryRun);
		}

		if (!operator.isStateActive()) {
			return;
		}

		@SuppressWarnings("unused")
		boolean doneV = false;
		if (operator.getController().isCorrectVertical()) {

			if (!operator.getController().checkDevices(operator, Orientation.V, true)) {
				return;
			}

			doneV = executeCorectionStep(Orientation.V, operator, dryRun);
		}

		Orbit postOrbit = operator.getNewOrbit();

		double rh1 = postOrbit.getRms(Orientation.HORIZONTAL);
		double rv1 = postOrbit.getRms(Orientation.VERTICAL);

		if (Double.isNaN(rh1) || Double.isInfinite(rh1) || Double.isNaN(rv1)
				|| Double.isInfinite(rv1)) {
			operator.requestAbort("RMS is NaN, might be no beam.", this.getClass(), null);
			return;
		}

		if ((rh1 < rh) && (rv1 < rv)) {
			operator.fireProgressReported(0.0,
					"After correction RMS change is H: " + Utilities.format2E(rh1 - rh)
							+ ", V: " + Utilities.format2E(rv1 - rv));
		} else {
			operator.fireProgressReported(0.0,
					"After correction RMS change is H: " + Utilities.format2E(rh1 - rh)
							+ ", V: " + Utilities.format2E(rv1 - rv)
							+ ", orbit got worse.");
		}

	}

	/**
	 * <p>executeCorectionStep.</p>
	 *
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @param operator a {@link org.scictrl.mp.orbitcorrect.correction.automatic.AutomaticOrbitCorrectionOperator} object
	 * @param dryRun a boolean
	 * @return a boolean
	 */
	protected boolean executeCorectionStep(final Orientation ori, final AutomaticOrbitCorrectionOperator operator, final boolean dryRun) {

		operator.setLastCorrection(null, ori);

		if (!operator.isStateActive()) {
			return false;
		}

		try {
			if (!operator.isStateActive()) {
				return false;
			}

			Correction c= operator.getController().calculateCorrection(operator, ori);
			operator.setLastCorrection(c, ori);

			operator.getController().debugDataDump(operator);

			if (!operator.isStateActive()) {
				return false;
			}

			double d= c.getMaxScaledCorrection();
			if (d<0.0) {
				log.error("MAX CORR IS NEGATIVE: "+d+" "+c.toString());
				d=Math.abs(d);
			}
			if (minCorrectorChange>0.0 && d<minCorrectorChange) {
				operator.fireProgressReported(0.0,"SKIP:["+ori.getShortName()+"]: Max corr " + Utilities.format1E(c.getMaxScaledCorrection())+ " is below "+Utilities.format1E(minCorrectorChange));
				return false;
			}

			if (c.isNaN()) {
				operator.fireProgressReported(0.0,"SKIP:["+ori.getShortName()+"]: Calculation problem, correction is NaN.");
				return false;
			}

			if (!operator.isStateActive()) {
				return false;
			}

			operator.setStateApplying("[" + ori.getShortName()+"]:  Max corr " + Utilities.format1E(c.getMaxScaledCorrection()));
			long start= System.currentTimeMillis();

			if (!dryRun) {
				operator.getController().applyCorrection(operator, c);
			} else {
				operator.fireProgressReported(0.0,"Dry-run, correction skipped!");
			}

			if (!operator.isStateApplying()) {
				return false;
			}

			operator.setStateActive("["+ori.getShortName()+"]: Corrected in "+(System.currentTimeMillis()-start)+" ms");
			return true;
		} catch (Exception e) {

			if (e.getMessage()!=null && e.getMessage().startsWith(ControlSystemException.TEST_FAILED_MESSAGE)) {

				operator.fireProgressReported(0.0,"Warning: "+e.getMessage());
				operator.fireProgressReported(0.0,"Apply failed, could be timeout, skipping turn.");
				return false;

			} else {
				String m= "ERROR while executing step: '" + e+"'!";
				operator.requestAbort(m,this.getClass(),e);
				log.error(m, e);
				return false;
			}

		} catch (Throwable e) {
			String m= "ERROR while executing step: '" + e+"'!";
			operator.requestAbort(m,this.getClass(),e);
			log.error(m, e);
			return false;
		}



	}

	/**
	 * {@inheritDoc}
	 *
	 *  Creation date: (09.04.2001
	 * 16:55:55)
	 */
	@Override
	protected void executeLoop(final AutomaticOrbitCorrectionOperator operator, final int count, final boolean dryRun) {

		if (!operator.isStateActive()) {
			return;
		}

		executeLoopStep(operator, count, dryRun);

		operator.requestStop("Ending after one step.");
	}

	/** {@inheritDoc} */
	@Override
	protected void prepareOnStart(final AutomaticOrbitCorrectionOperator operator) {

	}

	/**
	 * <p>Setter for the field <code>minCorrectorChange</code>.</p>
	 *
	 * @param ch a double
	 */
	public void setMinCorrectorChange(double ch) {
		ch = Math.abs(ch);
		if (ch!=minCorrectorChange) {
			minCorrectorChange=ch;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void configure(Configuration conf) {
		super.configure(conf);

		setMinCorrectorChange(conf.getDouble(AOC_MIN_CORRECTOR, 0.001));
	}

	/** {@inheritDoc} */
	@Override
	protected void waitForNextLoop(final AutomaticOrbitCorrectionOperator controller) {
		// not supported
	}
}
