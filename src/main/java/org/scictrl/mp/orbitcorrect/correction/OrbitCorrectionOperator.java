package org.scictrl.mp.orbitcorrect.correction;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scictrl.mp.orbitcorrect.Orientation;
import org.scictrl.mp.orbitcorrect.accessories.Utilities;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
import org.scictrl.mp.orbitcorrect.utilities.Orbit;
import org.scictrl.mp.orbitcorrect.utilities.ResponseMatrix;
/**
 * <p>
 * Dynamic part of OrbitCorrection procedure, stores active state of procedure.
 * </p>
 *
 * @author igor@scictrl.com
 */
public class OrbitCorrectionOperator {

	/**
	 * Enum for state of operation.
	 */
	public enum State {
		/** Inactive. */
		INACTIVE,
		/** Calculating corrections. */
		CALCULATING,
		/** Applying correction. */
		APPLYING,
		/** Active. */
		ACTIVE,
		/** Stopping. */
		STOPPING,
		/** Aborting. */
		ABORTING,
		/** Waiting. */
		WAITING
	}

	private String name;
	private DataBush db;
	private ElementList<AbstractCorrector>[] corr;
	private ElementList<BPMonitor>[] bpms;
	private OrbitCorrectionController controller;
	private ResponseMatrix[] responseMatrix= new ResponseMatrix[2];
	// private Orbit orbit;
	private final Logger log= LogManager.getLogger(getClass());
	private Orbit orbit;
	private boolean orbitLockActive = false;
	private PropertyChangeListener orbitLock;

	private String message = "";
	private State state = State.INACTIVE;

	private Correction[] lastCorrection= new Correction[2];

	/** If operator should send mail notification on serious problems. */
	protected boolean sendMailNotification=false;


	/**
	 * <p>Constructor for OrbitCorrectionOperator.</p>
	 */
	public OrbitCorrectionOperator() {
	}

	/**
	 * <p>Getter for the field <code>controller</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionController} object
	 */
	public OrbitCorrectionController getController() {
		return controller;
	}

	/**
	 * <p>getCorrectionScale.</p>
	 *
	 * @return a double
	 */
	public double getCorrectionScale() {
		return controller.getCorrectionScale();
	}

	/**
	 * <p>getLastCorrectionH.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.correction.Correction} object
	 */
	public Correction getLastCorrectionH() {
		return lastCorrection[Orientation._H];
	}

	/**
	 * <p>Setter for the field <code>lastCorrection</code>.</p>
	 *
	 * @param lastCorrection a {@link org.scictrl.mp.orbitcorrect.correction.Correction} object
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 */
	public void setLastCorrection(Correction lastCorrection, Orientation ori) {
		this.lastCorrection[ori.ordinal()]=lastCorrection;
		if (controller!=null && lastCorrection!=null) {
			controller.fireNewCorrection(lastCorrection);
		}
	}

	/**
	 * <p>getLastCorrectionV.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.correction.Correction} object
	 */
	public Correction getLastCorrectionV() {
		return lastCorrection[Orientation._V];
	}

	/**
	 * <p>clearLastCorrections.</p>
	 */
	public void clearLastCorrections() {
		this.lastCorrection[0] = null;
		this.lastCorrection[0] = null;
	}

	/**
	 * <p>getBPMonitors.</p>
	 *
	 * @return boolean
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 */
	public ElementList<BPMonitor> getBPMonitors(Orientation ori) {
		return bpms[ori.ordinal()];
	}

	/**
	 * <p>getCorrectors.</p>
	 *
	 * @return boolean
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 */
	public ElementList<AbstractCorrector> getCorrectors(Orientation ori) {
		return corr[ori.ordinal()];
	}

	/**
	 * <p>getDataBush.</p>
	 *
	 * @return DataBush
	 */
	public DataBush getDataBush() {
		return db;
	}

	/**
	 * <p>getMaxCorrectionValue.</p>
	 *
	 * @return double
	 */
	public double getMaxCorrectionValue() {
		return 0;
	}
	/**
	 * <p>Getter for the field <code>name</code>.</p>
	 *
	 * @return java.lang.String
	 */
	public String getName() {
		return name;
	}
	/**
	 * <p>Getter for the field <code>responseMatrix</code>.</p>
	 *
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @return a {@link org.scictrl.mp.orbitcorrect.utilities.ResponseMatrix} object
	 */
	public ResponseMatrix getResponseMatrix(Orientation ori) {

		ResponseMatrix rm= responseMatrix[ori.ordinal()];

		if (rm==null) {
			synchronized (responseMatrix) {
				rm= controller.calculateResponseMatrix(this, ori);
				if (rm.isValid()) {
					responseMatrix[ori.ordinal()]=rm;

					log.debug(rm.printToString());

				}
			}
		}

		return rm;
	}

	/**
	 * <p>initialize.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @param db a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} object
	 * @param correctors an array of {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} objects
	 * @param bpms an array of {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} objects
	 * @param contrl a {@link org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionController} object
	 */
	public void initialize(String name, DataBush db, ElementList<AbstractCorrector>[] correctors, ElementList<BPMonitor>[] bpms, OrbitCorrectionController contrl) {
		this.name=name;
		this.db=db;
		this.corr= correctors;
		this.bpms= bpms;
		this.controller= contrl;

		orbitLock= new PropertyChangeListener() {

			@Override
			public synchronized void propertyChange(PropertyChangeEvent evt) {
				if (orbitLockActive) {
					notify();
				}
			}
		};
		controller.getOrbitMonitor().addPropertyChangeListener(orbitLock);

	}

	/**
	 * Returns orbit, which is newer than last returned orbit. If necessary blocks this call and waits up to
	 * one second for new orbit event. If within one second there is no new orbit event, it stops waiting and
	 * returns whatever is current orbit.
	 * blocks until new orbit is available
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.utilities.Orbit} object
	 */
	public Orbit getNewOrbit() {

		Orbit o= controller.getOrbitMonitor().getUserOrbit();
		if (o.getTime().isGreaterThan(orbit.getTime())) {
			//log.debug(o.toStringStatistics());
			return orbit=o;
		} else {
			synchronized (orbitLock) {
				Orbit o1= controller.getOrbitMonitor().getUserOrbit();
				if (o1.getTime().isGreaterThan(orbit.getTime())) {
					//log.debug(o1.toStringStatistics());
					orbitLockActive=false;
					return orbit=o1;
				}
				orbitLockActive=true;
				try {
					orbitLock.wait(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				orbitLockActive=false;
			}
			orbit= controller.getOrbitMonitor().getUserOrbit();
		}
		return orbit;
	}

	/**
	 * Returns orbit as it is currently loaded on Databush, returns immediatelly.
	 *
	 * @return current orbit
	 */
	public Orbit getCurrentOrbit() {
		orbit= controller.getOrbitMonitor().getUserOrbit();
		//log.debug(orbit.toStringStatistics());
		return orbit;
	}

	/**
	 * Returns last orbit, which was requested/returned by this operator by calling {@link #getNewOrbit()} or {@link #getCurrentOrbit()}
	 *
	 * @return last orbit returned by this operator
	 */
	public Orbit getLastOrbit() {
		return orbit;
	}

	/**
	 * <p>Getter for the field <code>state</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator.State} object
	 */
	public State getState() {
		return state;
	}

	/**
	 * <p>Getter for the field <code>message</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public java.lang.String getMessage() {
		return message;
	}

	/**
	 * <p>isStateActive.</p>
	 *
	 * @return a boolean
	 */
	public boolean isStateActive() {
		return getState()==State.ACTIVE;
	}

	/**
	 * <p>setStateActive.</p>
	 */
	public void setStateActive() {
		setState(State.ACTIVE,"");
	}
	/**
	 * <p>setStateActive.</p>
	 *
	 * @param message a {@link java.lang.String} object
	 */
	public void setStateActive(String message) {
		setState(State.ACTIVE,message);
	}
	/**
	 * <p>isStateStopping.</p>
	 *
	 * @return a boolean
	 */
	public boolean isStateStopping() {
		return getState()==State.STOPPING;
	}
	/**
	 * <p>setStateCalculating.</p>
	 */
	public void setStateCalculating() {
		setState(State.CALCULATING,"");
	}

	/**
	 * <p>isStateCalculating.</p>
	 *
	 * @return a boolean
	 */
	public boolean isStateCalculating() {
		return getState()==State.CALCULATING;
	}

	/**
	 * <p>setStateApplying.</p>
	 */
	public void setStateApplying() {
		setState(State.APPLYING,"");
	}

	/**
	 * <p>setStateApplying.</p>
	 *
	 * @param message a {@link java.lang.String} object
	 */
	public void setStateApplying(String message) {
		setState(State.APPLYING,message);
	}

	/**
	 * <p>isStateApplying.</p>
	 *
	 * @return a boolean
	 */
	public boolean isStateApplying() {
		return getState()==State.APPLYING;
	}

	/**
	 * <p>isStateAborting.</p>
	 *
	 * @return a boolean
	 */
	public boolean isStateAborting() {
		return getState()==State.ABORTING;
	}

	/**
	 * <p>isStateInactive.</p>
	 *
	 * @return a boolean
	 */
	public boolean isStateInactive() {
		return getState() == null || getState()==State.INACTIVE;
	}


	/**
	 * <p>isStateWaiting.</p>
	 *
	 * @return a boolean
	 */
	public boolean isStateWaiting() {
		return getState()==State.WAITING;
	}

	/**
	 * <p>setStateWaiting.</p>
	 */
	public void setStateWaiting() {
		setState(State.WAITING,"");
	}

	/**
	 * <p>setStateWaiting.</p>
	 *
	 * @param message a {@link java.lang.String} object
	 */
	public void setStateWaiting(String message) {
		setState(State.WAITING,message);
	}

	/**
	 * <p>Setter for the field <code>state</code>.</p>
	 *
	 * @param state the state to set
	 * @param message a {@link java.lang.String} object
	 */
	public synchronized void setState(State state, String message) {
		this.state = state;
		this.message= message;
		fireStateChange(message);
	}

	/**
	 * <p>invalidateResponseMatrix.</p>
	 */
	public void invalidateResponseMatrix() {
		synchronized (responseMatrix) {
			responseMatrix[0]=null;
			responseMatrix[1]=null;
		}
	}

	private void fireStateChange(String message){
		StringBuilder sb= new StringBuilder(128);
		sb.append(Utilities.formatdMHmsS());
		sb.append(' ');
		sb.append(state.toString());
		if (message!=null && message.length()>0) {
			sb.append(": ");
			sb.append(message);
		}
		String s= sb.toString();

		if (controller!=null) {
			controller.fireStateChange(this.state,s);
		}
	}

	/**
	 * <p>fireProgressReported.</p>
	 *
	 * @param progress a double
	 * @param message a {@link java.lang.String} object
	 */
	public void fireProgressReported(double progress, String message) {
		if (controller!=null) {
			controller.fireProgressReport(progress,message);
		}
	}

	/**
	 * <p>sendMailNotification.</p>
	 *
	 * @param message a {@link java.lang.String} object
	 * @param beamCurrent a {@link java.lang.String} object
	 * @param requestor a {@link java.lang.Class} object
	 * @param error a {@link java.lang.Throwable} object
	 */
	protected void sendMailNotification(final String message, final String beamCurrent, final Class<?> requestor, final Throwable error) {
		try {
			StringWriter msw= new StringWriter(message.length()*2);
			PrintWriter mpw= new PrintWriter(msw);

			mpw.print("Message: '");
			mpw.print(message);
			mpw.print("'\nBeam current: '");
			mpw.print(beamCurrent);
			mpw.println("'");
			mpw.flush();
			mpw.close();

			StringWriter dsw= new StringWriter(message.length()*2);
			PrintWriter dpw= new PrintWriter(dsw);

			dpw.print("Requestor class: '");
			dpw.print(requestor == null ? "NULL" : requestor.getName());
			dpw.println("'");
			dpw.print("Error: '");
			if (error!=null) {
				error.printStackTrace(dpw);
			} else {
				dpw.print("None");
			}
			dpw.print("'");
			dpw.flush();
			dpw.close();

			getController().sendMailNotification(msw.toString(), dsw.toString());
		} catch (Exception e) {
			log.error("Mailing message failed:"+e, e);
		}
	}

	/**
	 * <p>Setter for the field <code>sendMailNotification</code>.</p>
	 *
	 * @param sendMailNotification a boolean
	 */
	public void setSendMailNotification(boolean sendMailNotification) {
		this.sendMailNotification = sendMailNotification;
	}

	/**
	 * <p>isSendMailNotification.</p>
	 *
	 * @return a boolean
	 */
	public boolean isSendMailNotification() {
		return sendMailNotification;
	}

	/**
	 * <p>Execute undo operation, if possible.</p>
	 */
	public void applyUndo() {

		if (!isStateInactive() || getController().canUndo()) {
			return;
		}

		try {
			getController().applyUndo(OrbitCorrectionOperator.this);
		} catch (Exception e) {
			log.error("Start failed: "+e, e);
			//e.printStackTrace();
			getController().signalOrbitCorrectionEnded();
		}
	}

	/**
	 * Returns <code>true</code> if undo operation can be executed.
	 *
	 * @return <code>true</code> if undo operation can be executed
	 */
	public boolean canUndo() {
		return getController().canUndo();
	}
}
