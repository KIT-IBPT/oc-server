/**
 *
 */
package org.scictrl.mp.orbitcorrect.epics;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.scictrl.csshell.Connection;
import org.scictrl.csshell.Poop;
import org.scictrl.csshell.RemoteException;
import org.scictrl.csshell.epics.EPICSConnection;
import org.scictrl.mp.orbitcorrect.ControlSystemException;
import org.scictrl.mp.orbitcorrect.model.IWriteConnector;
import org.scictrl.mp.orbitcorrect.model.optics.IBindedElement;

import gov.aps.jca.dbr.DBR;

/**
 * <p>EDataConnectorD class.</p>
 *
 * @author igor@scictrl.com
 */
public class EDataConnectorD extends EAbstractDataConnector<Double> implements IWriteConnector<Double> {

	/** remote connection/. */
	protected EPICSConnection<Double> connection;


	/**
	 * <p>Constructor for EDataConnectorD.</p>
	 *
	 * @throws java.lang.Exception if fails
	 * @param engine a {@link org.scictrl.mp.orbitcorrect.epics.EControlSystemEngine} object
	 * @param element a {@link org.scictrl.mp.orbitcorrect.model.optics.IBindedElement} object
	 * @param connection a {@link org.scictrl.csshell.epics.EPICSConnection} object
	 */
	public EDataConnectorD(EControlSystemEngine engine, IBindedElement<Double> element, EPICSConnection<Double> connection) throws Exception {
		super(Double.class,engine,element);

		this.connection=connection;
		connection.addPropertyChangeListener(Connection.PROPERTY_VALUE, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {

				Poop<Double, DBR> poop= connection.getLastPoop();
				if (!poop.isStatusOK()) {
					if (element!=null) {
						element.notifyDataUpdate(null);
					}
					return;
				}
				if (element!=null) {
					element.notifyDataUpdate(poop.getValue());
				}
			}
		});

	}

	/** {@inheritDoc} */
	@Override
	public Double get() throws Exception {
		Double data= connection.getValue();
		return data;
	}

	/** {@inheritDoc} */
	@Override
	public Double getLatestReceivedValue() {
		if (connection.hasLastPoop()) {
			Double data= connection.getLastValue();
			return data;
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void destroy() {
		connection.destroy();
	}

	/** {@inheritDoc} */
	@Override
	public boolean test() {
		return engine.test(connection);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isReady() {
		return engine.isReady(connection);
	}

	/** {@inheritDoc} */
	@Override
	public String getFormat() throws ControlSystemException {
		return connection.getMetaData().getFormat();
	}

	/** {@inheritDoc} */
	@Override
	public long getLatestReceivedTimestamp() {
		return connection.getLastPoop().getTimestamp().getMilliseconds();
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return connection.getName();
	}
	/**
	 * {@inheritDoc}
	 *
	 * <p>getMaxValue.</p>
	 */
	@Override
	public double getMaxValue() throws org.scictrl.mp.orbitcorrect.ControlSystemException {
		return connection.getMetaData().getMaximum();
	}
	/**
	 * {@inheritDoc}
	 *
	 * getMinValue method comment.
	 */
	@Override
	public double getMinValue() throws org.scictrl.mp.orbitcorrect.ControlSystemException {
		return connection.getMetaData().getMinimum();

	}

	/** {@inheritDoc} */
	@Override
	public void set(Double d) throws ControlSystemException {
		try {
			connection.setValue(d);
		} catch (RemoteException e) {
			throw new ControlSystemException("Set to '"+getName()+"' failed: "+e.toString(), e);
		}
	}

	/**
	 * <p>Getter for the field <code>connection</code>.</p>
	 *
	 * @return a {@link org.scictrl.csshell.epics.EPICSConnection} object
	 */
	public EPICSConnection<Double> getConnection() {
		return connection;
	}

}
