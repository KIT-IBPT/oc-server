package org.scictrl.mp.orbitcorrect.model;

import org.scictrl.mp.orbitcorrect.ControlSystemException;
import org.scictrl.mp.orbitcorrect.IConfigurable;
import org.scictrl.mp.orbitcorrect.model.optics.IBindedElement;


/**
 * This interface should be implemented by clases that will comunicate with control system (e.g. with Abeans)
 *
 * @author igor@scictrl.com
 */
public interface IControlSystemEngine extends IConfigurable {

	//public Property connect(String name) throws org.scictrl.mp.orbitcorrect.ControlSystemException ;

	/**
	 * <p>setFast.</p>
	 *
	 * @param properties an array of {@link org.scictrl.mp.orbitcorrect.model.IWriteConnector} objects
	 * @param values an array of {@link double} objects
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	public void setFast(IWriteConnector<Double>[] properties, double[] values) throws org.scictrl.mp.orbitcorrect.ControlSystemException ;

	/**
	 * <p>signalOrbitCorrectionStarted.</p>
	 */
	public void notifyOrbitCorrectionStarted();

	/**
	 * <p>signalOrbitCorrectionEnded.</p>
	 */
	public void notifyOrbitCorrectionEnded();

	/**
	 * <p>signalAutomaticOrbitCorrectionStarted.</p>
	 */
	public void notifyAutomaticOrbitCorrectionStarted();

	/**
	 * <p>signalAutomaticOrbitCorrectionEnded.</p>
	 */
	public void notifyAutomaticOrbitCorrectionEnded();

	/**
	 * <p>signalOrbitCorrectionRunning.</p>
	 */
	void notifyOrbitCorrectionRunning();

	/**
	 * <p>signalOrbitCorrectionShuttingDown.</p>
	 */
	void notifyOrbitCorrectionShuttingDown();

	/**
	 * <p>connect.</p>
	 *
	 * @param el a {@link org.scictrl.mp.orbitcorrect.model.optics.IBindedElement} object
	 * @param <T> a T class
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.IDataConnector} object
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	public <T> IDataConnector<T> connect(IBindedElement<T> el)  throws org.scictrl.mp.orbitcorrect.ControlSystemException ;
	/**
	 * <p>connect.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @param block a boolean
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.IDataConnector} object
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	public IDataConnector<Double> connect(String name, boolean block)  throws org.scictrl.mp.orbitcorrect.ControlSystemException ;

	/**
	 * <p>getValue.</p>
	 *
	 * @param pv a {@link java.lang.String} object
	 * @param type a {@link java.lang.Class} object
	 * @param <T> a T class
	 * @return a T object
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	public <T> T getValue(String pv, Class<T> type) throws ControlSystemException;
}
