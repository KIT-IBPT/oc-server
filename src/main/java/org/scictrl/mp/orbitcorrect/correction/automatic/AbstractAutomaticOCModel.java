package org.scictrl.mp.orbitcorrect.correction.automatic;

import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator.State;
/**
 * <p>Abstract AbstractAutomaticOCModel class.</p>
 *
 * @author igor@scictrl.com
 */
public abstract class AbstractAutomaticOCModel implements IAutomaticOCModel {

	/** Constant <code>NOT_READY_MESSAGE="Some devices not ready, skipping correc"{trunked}</code> */
	public static final String NOT_READY_MESSAGE="Some devices not ready, skipping correction. Could be timeout.";
	/** Constant <code>NOT_READY_ABORT_MESSAGE="Some devices not ready, borting. Could "{trunked}</code> */
	public static final String NOT_READY_ABORT_MESSAGE="Some devices not ready, borting. Could be timeout.";

	/** Update timestamp timeout */
	protected int timestampTimeout=30000;
	/** Model name */
	protected String name = "Automatic Orbit Correction";
	/** Logger */
	protected Logger log= LogManager.getLogger(getClass());


	/**
	 * AbstractAOCModel constructor comment.
	 */
	public AbstractAutomaticOCModel() {
		super();
	}

	/**
	 * Executes single correction loop, which is doing correcting until no optimization is necessary.
	 *
	 * @param operator the operator visitor
	 * @param dryRun a boolean
	 * @throws java.lang.Exception if any.
	 * @param steps a int
	 */
	protected abstract void executeLoop(final AutomaticOrbitCorrectionOperator operator, final int steps, final boolean dryRun) throws Exception;

	/**
	 * {@inheritDoc}
	 *
	 * Executes single correction loop once, which is doing correcting until no optimization is necessary or specified number of steps..
	 */
	@Override
	public void executeSingleLoop(final AutomaticOrbitCorrectionOperator operator, final int steps, final boolean dryRun) {
		if (steps>0) {
			operator.setState(State.ACTIVE,"TEST RUN START FOR "+steps+" STEP(S)");
		} else {
			operator.setState(State.ACTIVE,"TEST RUN START FOR WHOLE LOOP");
		}

		operator.getController().testOnStart(operator);

		if (operator.isStateActive()) {
			try {
				prepareOnStart(operator);
			} catch(Throwable e) {
				String m= "ERROR while preparing to start: '" + e+"'!";
				operator.requestAbort(m,this.getClass(),e);
				log.error(m, e);
			}
		}

		if (operator.isStateActive()) {
			try {
				operator.invalidateResponseMatrix();
				executeLoop(operator, steps, dryRun);
			} catch (Throwable e) {
				String m= "ERROR while executing loop: '" + e+"'!";
				operator.requestAbort(m,this.getClass(),e);
				log.error(m, e);
			}
		}

		if (operator.isStateAborting()) {
			operator.abortExecution();
		} else {
			operator.requestStop("TEST RUN ENDS",true);
			operator.stopExecution();
		}

	}

	/** {@inheritDoc} */
	@Override
	public void executeContinuous(final AutomaticOrbitCorrectionOperator operator) {
		operator.setState(State.ACTIVE,"CONTINUOUS CORRECTION STARTED.");

		operator.getController().testOnStart(operator);

		if (operator.isStateActive()) {
			try {
				prepareOnStart(operator);
			} catch(Throwable e) {
				String m= "ERROR while preparing to start: '" + e+"'!";
				operator.requestAbort(m,this.getClass(),e);
				log.error(m, e);
			}
		}

		while (operator.isStateActive()) {

			try {
				executeLoop(operator, -1, false);
				if (operator.isStateActive()) {
					waitForNextLoop(operator);
				}
			} catch (Throwable e) {
				String m= "ERROR while executing loop: '" + e+"'!";
				operator.requestAbort(m,this.getClass(),e);
				log.error(m, e);
			}
		}

		if (operator.isStateAborting()) {
			operator.abortExecution();
		} else if (operator.isStateStopping()) {
			operator.stopExecution();
		}
	}
	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}
	/**
	 * This method is called once when automatic model is started,
	 * implementator migth override this method to add own initialization.
	 *
	 * @param operator a {@link org.scictrl.mp.orbitcorrect.correction.automatic.AutomaticOrbitCorrectionOperator} object
	 */
	protected void prepareOnStart(final AutomaticOrbitCorrectionOperator operator) {
	}
	/**
	 * <p>Setter for the field <code>name</code>.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 */
	protected void setName(String name) {
		this.name= name;
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
		return name;
	}
	/**
	 * <p>waitForNextLoop.</p>
	 *
	 * @param controller a {@link org.scictrl.mp.orbitcorrect.correction.automatic.AutomaticOrbitCorrectionOperator} object
	 */
	protected abstract void waitForNextLoop(final AutomaticOrbitCorrectionOperator controller);


	/** {@inheritDoc} */
	@Override
	public void configure(Configuration conf) {

	}

}
