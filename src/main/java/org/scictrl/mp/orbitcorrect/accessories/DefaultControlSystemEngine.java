package org.scictrl.mp.orbitcorrect.accessories;

import org.apache.commons.configuration.Configuration;
import org.scictrl.mp.orbitcorrect.ControlSystemException;
import org.scictrl.mp.orbitcorrect.model.IControlSystemEngine;
import org.scictrl.mp.orbitcorrect.model.IDataConnector;
import org.scictrl.mp.orbitcorrect.model.IWriteConnector;
import org.scictrl.mp.orbitcorrect.model.optics.IBindedElement;
/**
 * DefaultControlSystemEngine implements IControlSystemEngine and gives it some functionality.
 *
 * @see org.scictrl.mp.orbitcorrect.model.IControlSystemEngine
 * @author igor@scictrl.com
 */
public class DefaultControlSystemEngine implements IControlSystemEngine {

	/**
	 * DefaultControlSystemEngine constructor.
	 */
	public DefaultControlSystemEngine() {
		System.out.println("[DefaultControlSystemEngine] created");
	}
	/** {@inheritDoc} */
	@Override
	public <T> IDataConnector<T> connect(IBindedElement<T> el)
			throws ControlSystemException {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * {@inheritDoc}
	 *
	 * Sets new values to properties.
	 */
	@Override
	public synchronized void setFast(IWriteConnector<Double>[] properties, double[] values) throws org.scictrl.mp.orbitcorrect.ControlSystemException {
		for (int i=0; i<properties.length;i++) properties[i].set(values[i]);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>signalOrbitCorrectionEnded.</p>
	 */
	@Override
	public void notifyOrbitCorrectionEnded() {
	}
	/**
	 * {@inheritDoc}
	 *
	 * <p>signalOrbitCorrectionStarted.</p>
	 */
	@Override
	public void notifyOrbitCorrectionStarted() {
	}

	/** {@inheritDoc} */
	@Override
	public void notifyAutomaticOrbitCorrectionEnded() {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void notifyAutomaticOrbitCorrectionStarted() {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void notifyOrbitCorrectionRunning() {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>signalOrbitCorrectionShuttingDown.</p>
	 */
	@Override
	public void notifyOrbitCorrectionShuttingDown() {}
	/** {@inheritDoc} */
	@Override
	public IDataConnector<Double> connect(String name, boolean block) throws ControlSystemException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void configure(Configuration conf) {
	}
	
	/** {@inheritDoc} */
	@Override
	public <T> T getValue(String pv, Class<T> type) throws ControlSystemException {
		// TODO Auto-generated method stub
		return null;
	}

}
