package org.scictrl.mp.orbitcorrect;

import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.IBindedElement;

/**
 * A listener interface receiving connection events. Class with this interface can be registred
 * with DataBush to recive events, when DataBush elements are connectiong or disconection
 * from Abeans.
 *
 * @see DataBush
 * @see IBindedElement
 * @author igor@scictrl.com
 */
public interface IConnectionListener extends java.util.EventListener {
/**
 * Invoked when a IBindedElement or group of BindedElements connects.
 *
 * @param e a {@link org.scictrl.mp.orbitcorrect.ConnectionReportEvent} object
 */
void deviceConnected(ConnectionReportEvent e);
/**
 * Invoked when a IBindedElement or group of BindedElements disconnects.
 *
 * @param e a {@link org.scictrl.mp.orbitcorrect.ConnectionReportEvent} object
 */
void deviceDisconnected(ConnectionReportEvent e);
}
