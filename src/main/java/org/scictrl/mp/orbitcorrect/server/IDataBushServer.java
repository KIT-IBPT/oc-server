/**
 *
 */
package org.scictrl.mp.orbitcorrect.server;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.scictrl.mp.orbitcorrect.DataBushException;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;

/**
 * <p>IDataBushServer interface.</p>
 *
 * @author igor@kriznar.com
 */
public interface IDataBushServer {

	/**
	 * <p>getDataBush.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} object
	 */
	public DataBush getDataBush();
	/**
	 * <p>getDataModel.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.server.ServerDataModel} object
	 */
	public ServerDataModel getDataModel();
	/**
	 * <p>initialize.</p>
	 *
	 * @throws org.apache.commons.configuration.ConfigurationException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 * @throws java.lang.InstantiationException if any.
	 * @throws java.lang.IllegalAccessException if any.
	 * @throws java.io.IOException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushException if any.
	 */
	public abstract void initialize() throws ConfigurationException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, IOException, DataBushException;
	/**
	 * <p>shutdown.</p>
	 */
	public abstract void shutdown();
}
