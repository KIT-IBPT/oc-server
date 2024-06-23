/**
 *
 */
package org.scictrl.mp.orbitcorrect.model;

import org.scictrl.mp.orbitcorrect.ControlSystemException;

/**
 * <p>IDataConnector interface.</p>
 *
 * @author igor@scictrl.com
 * 
 * @param <T> data type
 */
public interface IDataConnector<T> {

	/**
	 * Update strategy enum
	 */
	public enum UpdateStrategy {
		/** Pull strategy */
		PULL,
		/** Push strategy */
		PUSH}

	/**
	 * Push strategy method.
	 */
	public enum PushStrategy {
		/** No strategy. */
		NONE,
		/** Invalidate but not update. */
		INVALIDATE,
		/** Update values. */
		UPDATE}

	/**
	 * Gets value from remote data source and returns it.
	 *
	 * @return value from remote data source.
	 * @throws java.lang.Exception if remote get fails
	 */
	public T get() throws Exception;

	/**
	 * Destroys remote connection and release all relevant resources.
	 */
	public void destroy();

	/**
	 * Returns latest position delivered to the connector by way of event updates.
	 *
	 * @return lates data update value
	 */
	public T getLatestReceivedValue();

	/**
	 * Tests connection and device if it is in usable state. This may include making remote calls and
	 * checking remote status or other values.
	 *
	 * @return true if connector can be used
	 */
	public boolean test();

	/**
	 * A quick tests of connection and device by examining locally available data.
	 * It is suppose to be lightweight method, avoiding making remote calls and should exit quickly.
	 *
	 * @return true if connector can be used
	 */
	public boolean isReady();

	/**
	 * A thorough tests of connection and device by all relevant data, including making
	 * remote calls.
	 *
	 * @return true if connector can be used
	 */
	public Class<T> dataType();

	/**
	 * <p>getFormat.</p>
	 *
	 * @return a {@link java.lang.String} object
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	String getFormat() throws ControlSystemException;

	/**
	 * <p>getLatestReceivedTimestamp.</p>
	 *
	 * @return a long
	 */
	long getLatestReceivedTimestamp();

	/**
	 * <p>getName.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	String getName();

}
