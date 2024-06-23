package org.scictrl.mp.orbitcorrect.correction.automatic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionController;
import org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
/**
 * This is controller type of class, intance presents active orbit correcction progress.
 *
 * @author igor@scictrl.com
 */
public class AutomaticOrbitCorrectionOperator extends OrbitCorrectionOperator {

	/**
	 * Thread that can be terminated.
	 */
	public class OperatorThread extends Thread {
		boolean expired=false;

		/**
		 * Constructor.
		 * @param run Runnable
		 */
		public OperatorThread(Runnable run) {
			super(run,"AOCOperator-"+System.currentTimeMillis());
		}

		@Override
		public void run() {
			log.debug("Thread started: "+thread);
			try {
				super.run();
			} catch (Throwable t) {
				String m="Fatal error executing automatic mode: "+t;
				log.error(m, t);
			} finally {
				if (!expired  && !isStateInactive()) {
					if (!isStateStopping() && !isStateAborting()) {
						String m="Abnormal automatic mode termination.";
						requestAbort(m, this.getClass(), null);
					}
					if(isStateStopping()) {
						stopExecution();
					} else if (isStateAborting()) {
						abortExecution();
					}
				}
			}
			log.debug("Thread ended: "+this);
		}

		/**
		 * Terminates thread gracefully.
		 */
		public void expire() {
			expired=true;
		}
	}

	private Logger log= LogManager.getLogger(AutomaticOrbitCorrectionOperator.class);
	private boolean waiting=false;

	private OperatorThread thread;
	private IAutomaticOCModel automaticOCModel;

	/**
	 * <p>Constructor for AutomaticOrbitCorrectionOperator.</p>
	 */
	public AutomaticOrbitCorrectionOperator() {
	}

	void abortExecution() {
		setState(State.INACTIVE,"");
		if (thread!=null) {
			thread.expire();
		}
	}

	/**
	 * Aborts current correction and puts this model in status ST_ABORTING. If correction apply is in progress, it is aborted.
	 */
	public void abortByUser() {
		requestAbort("User abort request.",this.getClass(),null,true);
	}
	/**
	 * <p>stopByUser.</p>
	 */
	public void stopByUser() {
		requestStop("User stop request.",true);
	}

	/**
	 * <p>requestAbort.</p>
	 *
	 * @param message a {@link java.lang.String} object
	 * @param requestor a {@link java.lang.Class} object
	 * @param error a {@link java.lang.Throwable} object
	 * @param skipMail a boolean
	 */
	public void requestAbort(String message, Class<?> requestor, Throwable error, boolean skipMail) {

		if (getState()==State.INACTIVE) {
			fireProgressReported(0, "Abort ignored, already in INACTIVE state.");
			return;
		}

		if (message==null || message.length()==0) {
			message="Unknown reason for abort request!";
		}
		setState(State.ABORTING,message);

		if (sendMailNotification && !skipMail) {
			String beamCurrentMessage;
			try {
				beamCurrentMessage=Double.toString(getController().getBeamCurrent());
			} catch (Throwable e) {
				e.printStackTrace();
				beamCurrentMessage= "Read failed: '"+e.toString()+"'";
			}
			sendMailNotification(message,beamCurrentMessage,requestor,error);
		}

		unlockWait();
	}

	/**
	 * <p>requestAbort.</p>
	 *
	 * @param message a {@link java.lang.String} object
	 * @param requestor a {@link java.lang.Class} object
	 * @param error a {@link java.lang.Throwable} object
	 */
	public void requestAbort(String message, Class<?> requestor, Throwable error) {
		this.requestAbort(message, requestor, error, false);
	}


	void stopExecution() {
		setState(State.INACTIVE,"");
		if (thread!=null) {
			thread.expire();
		}
	}

	/**
	 * <p>requestStop.</p>
	 *
	 * @param text a {@link java.lang.String} object
	 */
	public void requestStop(String text) {
		requestStop(text,true);
	}

	/**
	 * <p>requestStop.</p>
	 *
	 * @param message a {@link java.lang.String} object
	 * @param skipMail a boolean
	 */
	public void requestStop(String message, boolean skipMail) {

		if (getState()==State.INACTIVE) {
			fireProgressReported(0, "Stop ignored, already in INACTIVE state.");
			return;
		}


		if (message==null || message.length()==0) {
			message="Unknown reason for stop request!";
		}


		setState(State.STOPPING,message);

		if (sendMailNotification && !skipMail) {
			String beamCurrentMessage;
			try {
				beamCurrentMessage=Double.toString(getController().getBeamCurrent());
			} catch (Throwable e) {
				e.printStackTrace();
				beamCurrentMessage= "Read failed: '"+e.toString()+"'";
			}
			sendMailNotification(message,beamCurrentMessage,this.getClass(),null);
		}

		unlockWait();
	}

	/**
	 * <p>unlockWait.</p>
	 */
	public synchronized void unlockWait() {
		if (waiting) {
			this.notifyAll();
		}
	}

	/**
	 * Waits for provided number of milliseconds
	 *
	 * @param milliseconds the wait time
	 */
	public synchronized void waitIdle(long milliseconds) {
		waiting=true;
		long ms= Math.max(10, milliseconds);
		try {
			this.wait(ms);
		} catch (Exception e) {
			requestAbort("Unhandled ERROR "+e,this.getClass(),e);
			log.error("Unhandled ERROR "+e, e);
		} finally {
			waiting=false;
		}
	}


	/** {@inheritDoc} */
	@Override
	public void initialize(String name, DataBush db, ElementList<AbstractCorrector>[] correctors, ElementList<BPMonitor>[] bpms, OrbitCorrectionController contrl) {
		super.initialize(name, db, correctors, bpms, contrl);
	}

	/**
	 * <p>isCurrentThreadExpired.</p>
	 *
	 * @return a boolean
	 */
	public boolean isCurrentThreadExpired() {
		Thread t= Thread.currentThread();
		if (t instanceof OperatorThread) {
			return ((OperatorThread)t).expired;
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public State getState() {
		if (isCurrentThreadExpired()) {
			return State.INACTIVE;
		}
		return super.getState();
	}

	/**
	 * <p>newOperatorThread.</p>
	 *
	 * @param run a {@link java.lang.Runnable} object
	 * @return a {@link org.scictrl.mp.orbitcorrect.correction.automatic.AutomaticOrbitCorrectionOperator.OperatorThread} object
	 */
	private OperatorThread startNewOperatorThread(Runnable run) {
		if (!isStateInactive()) {
			throw new IllegalStateException("Operator is still not inactive");
		}

		if (thread!=null) {
			thread.expire();
		}

		thread= new OperatorThread(run);
		thread.start();

		return thread;
	}

	/**
	 * Starts the AutomaticOC operation by creating and starting new execution thread with provided AutomaticOCModel
	 * as main operation strategy.
	 *
	 * @param aocModel operation strategy implementation
	 */
	public synchronized void startContinunous(final IAutomaticOCModel aocModel) {

		if (!isStateInactive()) {
			return;
		}

		this.automaticOCModel=aocModel;

		startNewOperatorThread(new Runnable() {

			@Override
			public void run() {
				try {
					getController().signalAutomaticOrbitCorrectionStarted();
					aocModel.executeContinuous(AutomaticOrbitCorrectionOperator.this);
				} catch (Exception e) {
					log.error("Start failed: "+e, e);
					//e.printStackTrace();
					if (isStateAborting()) {
						abortExecution();
					} else if (isStateStopping()) {
						stopExecution();
					} else {
						requestAbort("Serious execution error", this.getClass(), e, false);
						abortExecution();
					}
				}
				getController().signalAutomaticOrbitCorrectionEnded();
			}
		});

	}

	/**
	 * <p>
	 * Starts the AutomaticOC operation by creating and starting new execution thread with provided AutomaticOCModel
	 * as main operation strategy.
	 * </p>
	 * <p>
	 * The correction loop is performed until
	 * correction model decides that can correct no more. Some correction loops
	 * are executed in steps, the number of steps to go is controlled with steps
	 * parameter.
	 *
	 * @param aocModel operation strategy implementation
	 * @param steps
	 *            controls how many steps deep the loop recursion goes. If less
	 *            then 0, then there is no step limit and loop is executed till
	 *            end, if more than 0, then can go deeper for given count.
	 * @param dryRun a boolean
	 */
	public void startSingleLoop(IAutomaticOCModel aocModel, int steps, boolean dryRun) {

		if (!isStateInactive()) {
			return;
		}

		this.automaticOCModel=aocModel;

		startNewOperatorThread(new Runnable() {
			@Override
			public void run() {
				try {
					getController().signalAutomaticOrbitCorrectionStarted();
					aocModel.executeSingleLoop(AutomaticOrbitCorrectionOperator.this, steps, dryRun);
				} catch (Exception e) {
					log.error("Start failed: "+e, e);
					//e.printStackTrace();
					if (isStateAborting()) {
						abortExecution();
					} else if (isStateStopping()) {
						stopExecution();
					} else {
						requestAbort("Serious execution error", this.getClass(), e, false);
						abortExecution();
					}
				}
				getController().signalAutomaticOrbitCorrectionEnded();
			}
		});

	}

	/**
	 * <p>Getter for the field <code>automaticOCModel</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.correction.automatic.IAutomaticOCModel} object
	 */
	public IAutomaticOCModel getAutomaticOCModel() {
		return automaticOCModel;
	}

}
