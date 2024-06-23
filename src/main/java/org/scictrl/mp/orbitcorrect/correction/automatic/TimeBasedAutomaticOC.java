package org.scictrl.mp.orbitcorrect.correction.automatic;

import java.util.Date;

import org.scictrl.mp.orbitcorrect.ControlSystemException;
import org.scictrl.mp.orbitcorrect.Orientation;
import org.scictrl.mp.orbitcorrect.accessories.Utilities;
import org.scictrl.mp.orbitcorrect.correction.Correction;


/**
 * <p>TimeBasedAutomaticOC class.</p>
 *
 * @author igor@scictrl.com
 */
public class TimeBasedAutomaticOC extends AbstractAutomaticOCModel {

	/** Execution thread. */
	protected java.lang.Thread thread;

	/** Time wait between correction loops, in seconds */
	protected int idleTime = 30;

	/** Correct in horizontal. */
	protected boolean correctHorizontal = true;

	/** Correct in vertical. */
	protected boolean correctVertical = true;

	/** Minimal beam RMS change to trigger correction. */
	protected double minimalRMSChange = 0.0001;

	/** Last RMS change. */
	protected double[] lastRMS={1000.0,1000.0};

	/** Check RMS before start. */
	protected boolean checkRMSOnStartEnabled = true;

	//protected boolean continueOnTimeout = true;
	//protected boolean saveInitialAsReference= false;

	/**
	 * TimeBasedAutomaticOC constructor comment.
	 */
	public TimeBasedAutomaticOC() {
		super();
		name= "Time Based Correction";
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
	 * <p>executeCorectionStep.</p>
	 *
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @param prog a {@link javax.swing.JProgressBar} object
	 * @param operator a {@link org.scictrl.mp.orbitcorrect.correction.automatic.AutomaticOrbitCorrectionOperator} object
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	protected void executeCorectionStep(Orientation ori, javax.swing.JProgressBar prog, AutomaticOrbitCorrectionOperator operator) throws org.scictrl.mp.orbitcorrect.InconsistentDataException, org.scictrl.mp.orbitcorrect.DataBushPackedException, org.scictrl.mp.orbitcorrect.ControlSystemException {
		if (!operator.isStateActive()) {
			return;
		}
		operator.setStateCalculating();
		Correction c= operator.getController().getOrbitCorrectionModel(ori).calculateCorrection(operator);
		if (!operator.isStateCalculating()) {
			return;
		}
		operator.setStateApplying();
		operator.getController().applyCorrection(operator, c);
		if (!operator.isStateApplying()) {
			return;
		}

		operator.setStateActive();

	}

	/**
	 * Implementing method, which executes correction for specified orientation until
	 * new rms is not worse or equal.
	 *
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @param operator a {@link org.scictrl.mp.orbitcorrect.correction.automatic.AutomaticOrbitCorrectionOperator} object
	 */
	protected synchronized void executeCorrectionLoop(Orientation ori, AutomaticOrbitCorrectionOperator operator) {

		if (!operator.isStateActive()) {
			return;
		}

		try {
			operator.getController().testBeamCurrent(operator);
		} catch (Exception e) {
			e.printStackTrace();
			operator.requestAbort("Failed to read beam current, aborting: '"+e+"'",this.getClass(),e);
			return;
		}
		if (!operator.isStateActive()) {
			return;
		}

		lastRMS[ori.ordinal()]= operator.getController().getOrbitMonitor().getUserOrbit().getRms(ori);

		if (Double.isNaN(lastRMS[ori.ordinal()])||Double.isInfinite(lastRMS[ori.ordinal()])) {
			operator.requestAbort("RMS is NaN in " + (ori == Orientation.HORIZONTAL ? "Horizontal" : "Vertical") + ", might be no beam",this.getClass(),null);
			return;
		}

		operator.fireProgressReported(
			0.0,
			"correcting in " + (ori == Orientation.HORIZONTAL ? "Horizontal" : "Vertical"));
		try {
			executeCorectionStep(ori, null, operator);
		} catch (ControlSystemException e) {

			if (e.getMessage().startsWith(ControlSystemException.TEST_FAILED_MESSAGE)) {

				operator.fireProgressReported(0.0,"Warning: "+e.getMessage());
				operator.fireProgressReported(0.0,"Apply failed, could be timeout, skipping turn.");
				return;

			} else {
				operator.requestAbort("- error: " + e,this.getClass(),e);
				System.out.println("--- Error in " + name + " ---");
				e.printStackTrace();
				System.out.println("--- * ---");
				return;
			}

		} catch (Throwable e) {
			operator.requestAbort("- error: " + e,this.getClass(),e);
			System.out.println("--- Error in " + name + " ---");
			e.printStackTrace();
			System.out.println("--- * ---");
			return;
		}

		operator.waitIdle(20000);

		double d= operator.getController().getOrbitMonitor().getUserOrbit().getRms(ori);
		operator.fireProgressReported(
			0.0,
			"RMS change in "
				+ (ori == Orientation.HORIZONTAL ? "Horizontal" : "Vertical")
				+ ": "
				+ Utilities.format4E(d - lastRMS[ori.ordinal()])+", required at least: -"+Utilities.format4E(minimalRMSChange));

		if (((lastRMS[ori.ordinal()] - d) > minimalRMSChange)
				&& (!Double.isInfinite(lastRMS[ori.ordinal()]))
				&& (!Double.isNaN(lastRMS[ori.ordinal()])))
		{
			executeCorrectionLoop(ori, operator);
		} else {
			/*if (operator.getOrbitManipulator().isStepBackPossible(ori)) {
				operator.fireProgressReported(
						0.0,
						"RMS change in "
							+ (ori == Orientation.HORIZONTAL ? "Horizontal" : "Vertical")
							+ " not good enough - undoing last change");
				try {
					operator.getOrbitManipulator().stepBack(ori, null);
				} catch (ControlSystemException e) {

					if (e.getMessage().startsWith(ControlSystemException.TEST_FAILED_MESSAGE) && continueOnTimeout) {

						operator.fireProgressReported(0.0,"Warning: "+e.getMessage());
						operator.fireProgressReported(0.0,"Step-back failed, could be timeout, skipping turn.");
						return;

					} else {
						operator.requestAbort("- error: " + e,this.getClass(),e);
						System.out.println("--- Error in " + name + " ---");
						e.printStackTrace();
						System.out.println("--- * ---");
						return;
					}

				} catch (Throwable e) {
					operator.requestAbort("- error: " + e,this.getClass(),e);
					System.out.println("--- Error in " + name + " ---");
					e.printStackTrace();
					System.out.println("--- * ---");
					return;
				}
			}*/
		}
	}
	/** {@inheritDoc} */
	@Override
	protected void executeLoop(final AutomaticOrbitCorrectionOperator operator, final int steps, final boolean dryRun) {

		if ((operator.getController().getOrbitCorrectionModel(Orientation.H)==null)||(operator.getController().getOrbitCorrectionModel(Orientation.V)==null)) {
			operator.requestAbort("Selected IOrbitCorrectionModel is missing!",this.getClass(),null);
			return;
		}

		if (!(correctHorizontal||correctVertical)) {
			operator.requestAbort("Select at least one, horizontal or vertical, correction",this.getClass(),null);
			return;
		}

		try {
			operator.getController().testBeamCurrent(operator);
		} catch (Exception e) {
			e.printStackTrace();
			operator.requestAbort("Failed to read beam current, aborting. Error: "+e.toString(),this.getClass(),e);
			return;
		}
		if (!operator.isStateActive()) {
			return;
		}

		if (correctHorizontal)	{

			if (!operator.getController().checkDevices(operator,Orientation.H,true)) {
				return;
			}

			double r = operator.getController().getOrbitMonitor().getUserOrbit().getRms(Orientation.HORIZONTAL);

			if (Double.isNaN(r)||Double.isInfinite(r)) {
				operator.requestAbort("RMS is NaN in Horizontal, might be no beam.",this.getClass(),null);
				return;
			}

			if (checkRMSOnStartEnabled && ((r-lastRMS[Orientation.HORIZONTAL.ordinal()]) < minimalRMSChange)) {
				operator.fireProgressReported(0.0,"Skipping Horizontal correction, RMS change "+Utilities.format4E(r-lastRMS[Orientation.HORIZONTAL.ordinal()])+" is too small.");
			} else {
				executeCorrectionLoop(Orientation.HORIZONTAL,operator);
			}
		}

		if (!operator.isStateActive()) {
			return;
		}

		if (correctVertical)	{

			if (!operator.getController().checkDevices(operator,Orientation.V,true)) {
				return;
			}

			double r = operator.getController().getOrbitMonitor().getUserOrbit().getRms(Orientation.VERTICAL);

			if (Double.isNaN(r)||Double.isInfinite(r)) {
				operator.requestAbort("RMS is NaN Vertical, might be no beam.",this.getClass(),null);
				return;
			}

			if (checkRMSOnStartEnabled && ((r-lastRMS[Orientation.VERTICAL.ordinal()]) < minimalRMSChange)) {
				operator.fireProgressReported(0.0,"Skiping Vertical correction, RMS change "+Utilities.format4E(r-lastRMS[Orientation.VERTICAL.ordinal()])+" is too small.");
			} else {
				executeCorrectionLoop(Orientation.VERTICAL,operator);
			}
		}

	}
	/**
	 * Idle time wait between correction loops, in seconds.
	 *
	 * @return idle time wait between correction loops, in seconds
	 */
	public int getIdleTime() {
		return idleTime;
	}
	/**
	 * <p>Getter for the field <code>minimalRMSChange</code>.</p>
	 *
	 * @return double
	 */
	public double getMinimalRMSChange() {
		return minimalRMSChange;
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
	 * <p>isCorrectHorizontal.</p>
	 *
	 * @return boolean
	 */
	public boolean isCorrectHorizontal() {
		return correctHorizontal;
	}
	/**
	 * <p>isCorrectVertical.</p>
	 *
	 * @return boolean
	 */
	public boolean isCorrectVertical() {
		return correctVertical;
	}
	/** {@inheritDoc} */
	/*public boolean isSaveInitialAsReference() {
		return saveInitialAsReference;
	}*/
	@Override
	protected void prepareOnStart(AutomaticOrbitCorrectionOperator operator) {
		lastRMS[0]=0.0;
		lastRMS[1]=0.0;

		/*if (saveInitialAsReference) {
			operator.fireProgressReported(0.0,"Saving current orbit as reference.");

			operator.getBeamAnalyzer().setRelativeToReference(false);

			operator.getBeamAnalyzer().resetAveraging(Orientation.HORIZONTAL);
			operator.getBeamAnalyzer().resetAveraging(Orientation.VERTICAL);

			operator.waitIdle(10000);

			operator.getBeamAnalyzer().saveAveragedPositions();
			operator.getBeamAnalyzer().setSavedAsReference();
			operator.getBeamAnalyzer().setRelativeToReference(true);

			operator.getBeamAnalyzer().resetAveraging(Orientation.HORIZONTAL);
			operator.getBeamAnalyzer().resetAveraging(Orientation.VERTICAL);

			operator.waitIdle(10000);
		}*/
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
	 * <p>Setter for the field <code>correctHorizontal</code>.</p>
	 *
	 * @param newCorrectHorizontal boolean
	 */
	public void setCorrectHorizontal(boolean newCorrectHorizontal) {
		if (correctHorizontal != newCorrectHorizontal) {
			correctHorizontal = newCorrectHorizontal;
		}
	}
	/**
	 * <p>Setter for the field <code>correctVertical</code>.</p>
	 *
	 * @param newCorrectVertical boolean
	 */
	public void setCorrectVertical(boolean newCorrectVertical) {
		if (correctVertical != newCorrectVertical) {
			correctVertical = newCorrectVertical;
		}
	}
	/**
	 * Idle time wait between correction loops, in seconds.
	 *
	 * @param newIdleTime time wait between correction loops, in seconds
	 */
	public void setIdleTime(int newIdleTime) {
		if (idleTime != newIdleTime) {
			idleTime = newIdleTime;
		}
	}
	/**
	 * <p>Setter for the field <code>minimalRMSChange</code>.</p>
	 *
	 * @param newMinimalRMSChange a double
	 */
	public void setMinimalRMSChange(double newMinimalRMSChange) {
		newMinimalRMSChange = Math.abs(newMinimalRMSChange);
		if (newMinimalRMSChange!=minimalRMSChange) {
			minimalRMSChange=newMinimalRMSChange;
		}

	}
	/** {@inheritDoc} */
	@Override
	protected synchronized void waitForNextLoop(AutomaticOrbitCorrectionOperator operator) {
		operator.fireProgressReported(0.0,"Waiting idle for "+idleTime+" minutes - till "+Utilities.formatdMHmsS(new Date(System.currentTimeMillis()+((long)idleTime*60000))));
		operator.waitIdle(idleTime*60000);
	}
}
