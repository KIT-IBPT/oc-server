package org.scictrl.mp.orbitcorrect.model.optics;

/**
 * Marks DataBush element class that can be applied. Class has method of type <code>apply*()</code>.
 * Class with this interface is connected to another
 * DataBush element, to which can send it's data. For example, AbstractCalibratedMagnet is connected
 * to PowerSupply and can set current on it.
 *
 * @author igor@scictrl.com
 */
public interface IApplyableElement {
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
}
