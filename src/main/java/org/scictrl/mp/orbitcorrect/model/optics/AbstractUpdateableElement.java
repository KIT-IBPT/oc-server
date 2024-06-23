package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DataBushPackedException;


/**
 * Marks DataBush element class that can be updated. Class has method <code>update()</code>.
 * Class with this interface is connected to another or to Abean, from which can read value
 * and update it's own value with it.
 *
 * @author igor@scictrl.com
 */
public interface AbstractUpdateableElement{
	/**
	 * Sets <code>DataInvalidated</code> flag to <code>true</code>.
	 *
	 * @see isDataInvalidated()
	 */
	void invalidateData();
	/**
	 * Returns <code>true</code> if element's data is not synchronized with associated
	 * element or Abean. This fag goes to false after update or apply.
	 *
	 * @return <code>true</code> if element's data is not synchronized with associated element or Abean.
	 */
	boolean isDataInvalidated();
	/**
	 * Reads value from associated element or Abean and calculates it's new values.
	 * Metod reads data from other databush elements or from remote devices represented with
	 * Abean. Metod also checks <code>isDataInvalidated()</code> on depending databush elements,
	 * if value has changed.
	 * Return value is identification code of error.
	 * <b>Note!</b> Update is performed only if virtual flag is set to <code>false</code>.
	 * <b>Note!</b> All update methods are synchronized, so only one update is called at time.
	 * For update od group of elements call update on appropriate element-list or DataBush.
	 *
	 * @throws java.lang.IllegalStateException thrown if status of DataBush equals <code>DB_EMPTY</code>
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException contains all exception thrown or caught during updating element
	 * @return a int the return code indicating success
	 */
	public abstract int update() throws IllegalStateException, DataBushPackedException;
}
