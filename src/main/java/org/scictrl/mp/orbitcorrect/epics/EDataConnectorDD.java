/**
 *
 */
package org.scictrl.mp.orbitcorrect.epics;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.scictrl.csshell.Connection;
import org.scictrl.csshell.Poop;
import org.scictrl.csshell.epics.EPICSConnection;
import org.scictrl.mp.orbitcorrect.ControlSystemException;
import org.scictrl.mp.orbitcorrect.model.optics.IBindedElement;

import gov.aps.jca.dbr.DBR;

/**
 * <p>
 * EDataConnectorDD class.
 * </p>
 *
 * @author igor@scictrl.com
 */
public class EDataConnectorDD extends EAbstractDataConnector<Double[]> {

	private EPICSConnection<Double>[] connections;
	private boolean[] lastUpdate = new boolean[] { false, false };

	/**
	 * <p>
	 * Constructor for EDataConnectorDD.
	 * </p>
	 *
	 * @throws java.lang.Exception if fails
	 * @param engine      a
	 *                    {@link org.scictrl.mp.orbitcorrect.epics.EControlSystemEngine}
	 *                    object
	 * @param element     a
	 *                    {@link org.scictrl.mp.orbitcorrect.model.optics.IBindedElement}
	 *                    object
	 * @param connections an array of
	 *                    {@link org.scictrl.csshell.epics.EPICSConnection} objects
	 */
	public EDataConnectorDD(EControlSystemEngine engine, IBindedElement<Double[]> element,
			EPICSConnection<Double>[] connections) throws Exception {
		super(Double[].class, engine, element);

		if (connections == null || connections.length != 2 || connections[0] == null || connections[1] == null) {
			throw new NullPointerException("[" + element.getName() + "] Internal error, connections are null!");
		}

		this.connections = connections;

		connections[0].addPropertyChangeListener(Connection.PROPERTY_VALUE, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				boolean u = false;
				synchronized (lastUpdate) {
					u = lastUpdate[1];
					lastUpdate[0] = !u;
					lastUpdate[1] = false;
				}
				if (u) {
					notifyDataUpdate();
				}
			}
		});
		connections[1].addPropertyChangeListener(Connection.PROPERTY_VALUE, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				boolean u = false;
				synchronized (lastUpdate) {
					u = lastUpdate[0];
					lastUpdate[1] = !u;
					lastUpdate[0] = false;
				}
				if (u) {
					notifyDataUpdate();
				}
			}
		});
	}

	private void notifyDataUpdate() {

		Double[] data = new Double[2];

		for (int i = 0; i < data.length; i++) {
			Poop<Double, DBR> poop = connections[i].getLastPoop();
			if (!poop.isStatusOK()) {
				if (element != null) {
					element.notifyDataUpdate(null);
				}
				return;
			}
			data[i] = poop.getValue();
		}
		if (element != null) {
			element.notifyDataUpdate(data);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void destroy() {
		for (EPICSConnection<?> conn : connections) {
			conn.destroy();
		}
	}

	/** {@inheritDoc} */
	@Override
	public Double[] get() throws Exception {
		Double[] data = new Double[2];
		data[0] = connections[0].getValue();
		data[1] = connections[1].getValue();
		return data;
	}

	/** {@inheritDoc} */
	@Override
	public Double[] getLatestReceivedValue() {
		Double[] data = new Double[2];
		data[0] = connections[0].getLastValue();
		data[1] = connections[1].getLastValue();
		return data;
	}

	/** {@inheritDoc} */
	@Override
	public boolean test() {
		return isReady();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isReady() {
		for (EPICSConnection<Double> c : connections) {
			if (!c.isReady() || !c.getLastPoop().isStatusOK()) {
				return false;
			}
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String getFormat() throws ControlSystemException {
		return connections[0].getMetaData().getFormat();
	}

	/** {@inheritDoc} */
	@Override
	public long getLatestReceivedTimestamp() {
		return connections[0].getLastPoop().getTimestamp().getMilliseconds();
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return connections[0].getName();
	}

	/**
	 * <p>
	 * getMaxValue.
	 * </p>
	 *
	 * @return a double
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	public double getMaxValue() throws org.scictrl.mp.orbitcorrect.ControlSystemException {
		return connections[0].getMetaData().getMaximum();
	}

	/**
	 * getMinValue method comment.
	 *
	 * @return a double
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	public double getMinValue() throws org.scictrl.mp.orbitcorrect.ControlSystemException {
		return connections[0].getMetaData().getMinimum();

	}

	/**
	 * <p>
	 * Getter for the field <code>connections</code>.
	 * </p>
	 *
	 * @return an array of {@link org.scictrl.csshell.epics.EPICSConnection} objects
	 */
	public EPICSConnection<Double>[] getConnections() {
		return connections;
	}

}
