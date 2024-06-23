package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.ISimpleElement;


/**
 * Marks DataBush element class that is connected to Abean. Class has to implement connection controling
 * methods.
 *
 * @author igor@scictrl.com
 * 
 * @param <T> data type, such as Double or Double[]
 */
public interface IBindedElement<T> extends ISimpleElement {
	/**
	 * Binds to remote object. Returns the code indication success of bind.
	 *
	 * @return the code indication success of bind.
	 * @throws java.lang.IllegalAccessException element is not in appropriate access state.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if bind exception occurs.
	 */
	int connect() throws IllegalAccessException, DataBushPackedException;
	/**
	 * Destroys connection. Returns the code indication success of destroy.
	 *
	 * @return the code indication success of destroy.
	 * @throws java.lang.IllegalAccessException if element is not in appropriate access state.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if destroy exception occurs.
	 */
	int disconnect() throws IllegalAccessException, DataBushPackedException;
	/**
	 * Returns <code>true</code> if element is binded, <code>false</code> otherwise.
	 *
	 * @return <code>true</code> if element is binded, <code>false</code> otherwise.
	 */
	boolean isConnected();
	/**
	 * Called by data connection when new value update arrives
	 *
	 * @param data new data, if value is OK, <code>null</code> if last arrive value signals problems and can not be thrusted
	 */
	void notifyDataUpdate(T data);

}
