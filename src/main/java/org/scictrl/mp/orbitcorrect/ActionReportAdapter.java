package org.scictrl.mp.orbitcorrect;


/**
 * An abstract adapter class for receiving component events. The methods in this class
 * are empty. This class exists as convenience for creating listener objects.
 *
 * @see org.scictrl.mp.orbitcorrect.IActionReportListener
 * @author igor@scictrl.com
 */
public abstract class ActionReportAdapter implements IActionReportListener {
/**
 * This is a default constructor for <code>ActionReportAdapter</code>
 */
public ActionReportAdapter() {
	super();
}
/**
 * Void method, that can be overrided by child class, if necessary.
 *
 * @see org.scictrl.mp.orbitcorrect.IActionReportListener
 * @param e a {@link org.scictrl.mp.orbitcorrect.ActionReportEvent} object
 */
public void applyFailed(ActionReportEvent e) {
}
/**
 * {@inheritDoc}
 *
 * Void method, that can be overrided by child class, if necessary.
 * @see org.scictrl.mp.orbitcorrect.IActionReportListener
 */
@Override
public void applyPerformed(ActionReportEvent e) {
}
/**
 * Void method, that can be overrided by child class, if necessary.
 *
 * @see org.scictrl.mp.orbitcorrect.IActionReportListener
 * @param e a {@link org.scictrl.mp.orbitcorrect.ActionReportEvent} object
 */
public void updateFailed(ActionReportEvent e) {
}
/**
 * {@inheritDoc}
 *
 * Void method, that can be overrided by child class, if necessary.
 * @see org.scictrl.mp.orbitcorrect.IActionReportListener
 */
@Override
public void updatePerformed(ActionReportEvent e) {
}
}
