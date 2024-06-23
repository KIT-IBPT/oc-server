package org.scictrl.mp.orbitcorrect.correction.automatic;

import org.apache.commons.configuration.Configuration;
import org.scictrl.mp.orbitcorrect.ControlSystemException;
import org.scictrl.mp.orbitcorrect.Orientation;
import org.scictrl.mp.orbitcorrect.accessories.Utilities;
import org.scictrl.mp.orbitcorrect.correction.Correction;
import org.scictrl.mp.orbitcorrect.utilities.Orbit;


/**
 * <p>FastModeAutomaticOC class.</p>
 *
 * @author igor@scictrl.com
 */
public class FastModeAutomaticOC extends AbstractAutomaticOCModel {

	/** Constant <code>AOC_FASTMODE_MIN_WAIT_TIME="aoc.fastmode.minWaitTime"</code> */
	public static final String AOC_FASTMODE_MIN_WAIT_TIME =      "aoc.fastmode.minWaitTime";

	/** Constant <code>AOC_FASTMODE_MIN_CORRECTOR="aoc.fastmode.minCorrector"</code> */
	public static final String AOC_FASTMODE_MIN_CORRECTOR =      "aoc.fastmode.minCorrector";

	/** Constant <code>AOC_FASTMODE_MIN_CORRECTION_RMS="aoc.fastmode.minCorrectionRMS"</code> */
	public static final String AOC_FASTMODE_MIN_CORRECTION_RMS = "aoc.fastmode.minCorrectionRMS";

	/** Constant <code>AOC_FASTMODE_MAX_RELAXATION_RMS="aoc.fastmode.maxRelaxationRMS"</code> */
	public static final String AOC_FASTMODE_MAX_RELAXATION_RMS = "aoc.fastmode.maxRelaxationRMS";

	/** Constant <code>AOC_FASTMODE_MAX_RELAXATION_BPM="aoc.fastmode.maxRelaxationBPM"</code> */
	public static final String AOC_FASTMODE_MAX_RELAXATION_BPM = "aoc.fastmode.maxRelaxationBPM";

	/** Constant <code>AOC_FASTMODE_E_MIN_H="aoc.fastmode.energyMin.h"</code> */
	public static final String AOC_FASTMODE_E_MIN_H =            "aoc.fastmode.energyMin.h";

	/** Constant <code>AOC_FASTMODE_E_MIN_V="aoc.fastmode.energyMin.v"</code> */
	public static final String AOC_FASTMODE_E_MIN_V =            "aoc.fastmode.energyMin.v";

	/** Execution thread. */
	protected java.lang.Thread thread;

	//protected double[] lastRMS={1000.0,1000.0};

	/** If RMS should be checked before start. */
	protected boolean checkRMSOnStartEnabled = true;

	/**
	 * If after correction RMS changed less than this minimal, next correction will be
	 * skipped until relaxation conditions are right.
	 */
	protected double minCorrectionRMSChange=0.0001;

	/**
	 * If orbit RMS changes during relaxation for more than this value, the correction will kick in.
	 */
	protected double maxRelaxationRMSChange=0.0001;

	/**
	 * If orbit BPM position changes during relaxation for more than this value, the correction will kick in.
	 */
	protected double maxRelaxationBPMChange=0.01;

	/**
	 * If calculated corrector changes in mrad are lower than this, then correction is skipped and
	 * goes into relaxation.
	 */
	protected double minCorrectorChange=0.001;

	/**
	 * Minimal energy, below which corresponding correction is skipped.
	 */
	protected final double[] energyMin = {0.0,0.0};

	/**
	 * Minimum time in milliseconds that loop waits in relaxation wait mode.
	 */
	protected long minWaitTime=100;

	/**
	 * TimeBasedAutomaticOC constructor comment.
	 */
	public FastModeAutomaticOC() {
		super();
		name= "Fast Mode Correction";
		/*setSendMailNotification(true);
		addAutomaticOrbitCorrectionListener(new AutomaticOrbitCorrectionListener(){
			@Override
			public void stopped(String message) {
				visible.lockWindow(false);
			}
			@Override
			public void aborted(String message) {
				visible.lockWindow(false);
			}
			@Override
			public void started(String message) {
				visible.lockWindow(true);
			}
			@Override
			public void progressReported(double d, String m) {}
		});*/
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

		if (
				Double.isNaN(rh1) ||
				Double.isInfinite(rh1) ||
				Double.isNaN(rv1) ||
				Double.isInfinite(rv1))
		{
			operator.requestAbort("RMS is NaN, might be no beam.", this.getClass(), null);
			return;
		}

		if ((rh1 - rh < -minCorrectionRMSChange) && (rv1 - rv < -minCorrectionRMSChange)) {
			operator.fireProgressReported(0.0,
					"After correction RMS change is H: " + Utilities.format2E(rh1 - rh)
							+ ", V: " + Utilities.format2E(rv1 - rv)
							+ ", less then: " + Utilities.format2E(-minCorrectionRMSChange));
		} else if ((rh1 < rh) && (rv1 < rv)) {
			operator.fireProgressReported(0.0,
					"After correction RMS change is H: " + Utilities.format2E(rh1 - rh)
							+ ", V: " + Utilities.format2E(rv1 - rv)
							+ ", more than: " + Utilities.format2E(-minCorrectionRMSChange));
		} else {
			operator.fireProgressReported(0.0,
					"After correction RMS change is H: " + Utilities.format2E(rh1 - rh)
							+ ", V: " + Utilities.format2E(rv1 - rv)
							+ ", orbit got worse.");
		}


		if (
				(count < 0 || count > 1) &&
				(rh1 < rh) && (rv1 < rv) &&
				(
						(doneH && (rh1 - rh < -minCorrectionRMSChange)) ||
						(doneV && (rv1 - rv < -minCorrectionRMSChange))
				)
			)
		{
			executeLoopStep(operator, count - 1, dryRun);
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

			double e= operator.getDataBush().getDataBushInfo().getEnergy();
			double emin=energyMin[ori.ordinal()];
			if (e < emin) {
				operator.fireProgressReported(0.0,"SKIP:["+ori.getShortName()+"]: energy " + Utilities.format2D(e)+ " is below "+Utilities.format2D(emin));
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
	}

	/**
	 * <p>isCheckRMSOnStartEnabled.</p>
	 *
	 * @return boolean
	 */
	public boolean isCheckRMSOnStartEnabled() {
		return checkRMSOnStartEnabled;
	}
	/**
	 * <p>Setter for the field <code>checkRMSOnStartEnabled</code>.</p>
	 *
	 * @param newCheckRMSOnStartEnabled boolean
	 */
	public void setCheckRMSOnStartEnabled(boolean newCheckRMSOnStartEnabled) {
		checkRMSOnStartEnabled = newCheckRMSOnStartEnabled;
	}

	/**
	 * <p>Setter for the field <code>minCorrectionRMSChange</code>.</p>
	 *
	 * @param ch a double
	 */
	public void setMinCorrectionRMSChange(double ch) {
		ch = Math.abs(ch);
		if (ch!=minCorrectionRMSChange) {
			minCorrectionRMSChange=ch;
		}
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

	/**
	 * <p>Setter for the field <code>maxRelaxationRMSChange</code>.</p>
	 *
	 * @param ch a double
	 */
	public void setMaxRelaxationRMSChange(double ch) {
		ch = Math.abs(ch);
		if (ch!=maxRelaxationRMSChange) {
			maxRelaxationRMSChange=ch;
		}
	}

	/**
	 * <p>Setter for the field <code>maxRelaxationBPMChange</code>.</p>
	 *
	 * @param ch a double
	 */
	public void setMaxRelaxationBPMChange(double ch) {
		ch = Math.abs(ch);
		if (ch!=maxRelaxationBPMChange) {
			maxRelaxationBPMChange=ch;
		}
	}


	/**
	 * <p>Getter for the field <code>minWaitTime</code>.</p>
	 *
	 * @return a long
	 */
	public long getMinWaitTime() {
		return minWaitTime;
	}

	/**
	 * <p>Setter for the field <code>minWaitTime</code>.</p>
	 *
	 * @param minWaitTime a long
	 */
	public void setMinWaitTime(long minWaitTime) {
		this.minWaitTime = Math.abs(minWaitTime);
	}

	/** {@inheritDoc} */
	@Override
	protected synchronized void waitForNextLoop(final AutomaticOrbitCorrectionOperator operator) {

		operator.setStateWaiting("till RMS change is over "+Utilities.format2E(maxRelaxationRMSChange)+" or BPM change is over "+Utilities.format2E(maxRelaxationBPMChange));

		long start= System.currentTimeMillis();

		final Orbit o1= operator.getCurrentOrbit();

		operator.waitIdle(minWaitTime);

		Orbit o2= operator.getNewOrbit();

		Orbit rel= new Orbit(o2,o1);

		while (operator.isStateWaiting()
				&& (Math.abs(o2.getRms(Orientation.H)-o1.getRms(Orientation.H))<maxRelaxationRMSChange)
				&& (Math.abs(o2.getRms(Orientation.V)-o1.getRms(Orientation.V))<maxRelaxationRMSChange)
				&& (Math.abs(rel.getMax(Orientation.H))<maxRelaxationBPMChange)
				&& (Math.abs(rel.getMax(Orientation.V))<maxRelaxationBPMChange)) {

			//operator.fireProgressReported(0.0,"Waiting idle while RMS change H: "+Math.abs(o2.getRms(Orientation.H)-o1.getRms(Orientation.H))+" V: "+Math.abs(o2.getRms(Orientation.V)-o1.getRms(Orientation.V))+" is smaller than "+maxRelaxationRMSChange);
			//System.out.println("Waiting idle while RMS change H: "+format.format(Math.abs(o2.getRms(Orientation.H)-o1.getRms(Orientation.H)))+" V: "+format.format(Math.abs(o2.getRms(Orientation.V)-o1.getRms(Orientation.V)))+" is smaller than "+maxRelaxationRMSChange);
			operator.waitIdle(minWaitTime);

			o2= operator.getNewOrbit();
			rel= new Orbit(o2,o1);
		}
		if (operator.isStateWaiting()) {
			if (Math.abs(o2.getRms(Orientation.H)-o1.getRms(Orientation.H))>=maxRelaxationRMSChange
					|| Math.abs(o2.getRms(Orientation.V)-o1.getRms(Orientation.V))>=maxRelaxationRMSChange) {
				operator.setStateActive("After "+Utilities.formatHHHHms(System.currentTimeMillis()-start)+" RMS change ("+Utilities.format1E(Math.abs(o2.getRms(Orientation.H)-o1.getRms(Orientation.H)))+","+Utilities.format1E(Math.abs(o2.getRms(Orientation.V)-o1.getRms(Orientation.V)))+") is over "+Utilities.format1E(maxRelaxationRMSChange)+", max BPM change ("+Utilities.format1E(Math.abs(rel.getMax(Orientation.H)))+","+Utilities.format1E(Math.abs(rel.getMax(Orientation.V)))+")");
			} else if (Math.abs(rel.getMax(Orientation.H))>=maxRelaxationBPMChange
				|| Math.abs(rel.getMax(Orientation.V))>=maxRelaxationBPMChange) {
				operator.setStateActive("After "+Utilities.formatHHHHms(System.currentTimeMillis()-start)+" max BPM change ("+Utilities.format1E(Math.abs(rel.getMax(Orientation.H)))+","+Utilities.format1E(Math.abs(rel.getMax(Orientation.V)))+") is over "+Utilities.format1E(maxRelaxationBPMChange)+", RMS change ("+Utilities.format1E(Math.abs(o2.getRms(Orientation.H)-o1.getRms(Orientation.H)))+","+Utilities.format1E(Math.abs(o2.getRms(Orientation.V)-o1.getRms(Orientation.V)))+")");
			}
		}
	}

	/**
	 * <p>Setter for the field <code>energyMin</code>.</p>
	 *
	 * @param energyMinH a double
	 * @param energyMinV a double
	 */
	public void setEnergyMin(double energyMinH, double energyMinV) {
		this.energyMin[Orientation._H] = energyMinH;
		this.energyMin[Orientation._V] = energyMinV;
	}

	/**
	 * <p>Setter for the field <code>energyMin</code>.</p>
	 *
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @param energyMin a double
	 */
	public void setEnergyMin(Orientation ori, double energyMin) {
		this.energyMin[ori.ordinal()] = energyMin;
	}

	/**
	 * <p>Getter for the field <code>energyMin</code>.</p>
	 *
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @return a double
	 */
	public double getEnergyMin(Orientation ori) {
		return this.energyMin[ori.ordinal()];
	}

	/** {@inheritDoc} */
	@Override
	public void configure(final Configuration conf) {
		super.configure(conf);

		setMaxRelaxationRMSChange(conf.getDouble(AOC_FASTMODE_MAX_RELAXATION_RMS, 0.0001));
		setMaxRelaxationBPMChange(conf.getDouble(AOC_FASTMODE_MAX_RELAXATION_BPM, 0.01));
		setMinCorrectionRMSChange(conf.getDouble(AOC_FASTMODE_MIN_CORRECTION_RMS, 0.0001));
		setMinCorrectorChange(conf.getDouble(AOC_FASTMODE_MIN_CORRECTOR, 0.001));
		setMinWaitTime(conf.getLong(AOC_FASTMODE_MIN_WAIT_TIME, 100));
		setEnergyMin(Orientation.H, conf.getDouble(AOC_FASTMODE_E_MIN_H, 0.0));
		setEnergyMin(Orientation.V, conf.getDouble(AOC_FASTMODE_E_MIN_V, 0.0));
	}
}
