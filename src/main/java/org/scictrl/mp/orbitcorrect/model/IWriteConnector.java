/**
 *
 */
package org.scictrl.mp.orbitcorrect.model;

/**
 * <p>IWriteConnector interface.</p>
 *
 * @author igor@scictrl.com
 * 
 * @param <T> data type
 */
public interface IWriteConnector<T> extends IDataConnector<T> {
	/**
	 * <p>getMaxValue.</p>
	 *
	 * @return a double
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	double getMaxValue() throws org.scictrl.mp.orbitcorrect.ControlSystemException;
	/**
	 * <p>getMinValue.</p>
	 *
	 * @return a double
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	double getMinValue() throws org.scictrl.mp.orbitcorrect.ControlSystemException;
	/**
	 * <p>set.</p>
	 *
	 * @param d a T object
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	void set(T d) throws org.scictrl.mp.orbitcorrect.ControlSystemException;
}
