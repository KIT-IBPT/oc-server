package org.scictrl.mp.orbitcorrect;

import org.scictrl.mp.orbitcorrect.model.optics.DataBush;

/**
 * A listener interface receiving DataBush actions. Class with this interface can be registred
 * with DataBush to recive events, when DataBush is applyied or manually updated.
 *
 * @see DataBush
 * @author igor@scictrl.com
 */
public interface IActionReportListener extends java.util.EventListener {
/**
 * Invoked when apply is performed.
 *
 * @see org.scictrl.mp.orbitcorrect.ActionReportEvent
 * @param e a {@link org.scictrl.mp.orbitcorrect.ActionReportEvent} object
 */
void applyPerformed(ActionReportEvent e);
/**
 * Invoked when manual update is performed.
 *
 * @see org.scictrl.mp.orbitcorrect.ActionReportEvent
 * @param e a {@link org.scictrl.mp.orbitcorrect.ActionReportEvent} object
 */
void updatePerformed(ActionReportEvent e);
}
