package org.scictrl.mp.orbitcorrect;


/**
 * An abstract adapter class for receiving component events. The methods in this class
 * are empty. This class exists as convenience for creating listener objects.
 *
 * @see org.scictrl.mp.orbitcorrect.IConnectionListener
 * @author igor@scictrl.com
 */
public abstract class ConnectionAdapter implements IConnectionListener {
/**
 * ConnectionReportAdapter default constructor.
 */
public ConnectionAdapter() {
	super();
}
/**
 * Void method, that can be overrided by child class, if necessary.
 *
 * @see org.scictrl.mp.orbitcorrect.IConnectionListener
 * @param e a {@link org.scictrl.mp.orbitcorrect.ConnectionReportEvent} object
 */
public void connected(ConnectionReportEvent e) {
}
/**
 * Void method, that can be overrided by child class, if necessary.
 *
 * @see org.scictrl.mp.orbitcorrect.IConnectionListener
 * @param e a {@link org.scictrl.mp.orbitcorrect.ConnectionReportEvent} object
 */
public void disconneted(ConnectionReportEvent e) {
}
}
