package org.scictrl.mp.orbitcorrect;


/**
 * An abstract adapter class for receiving events. The methods in this class
 * are empty. This class exists as convenience for creating listener objects.
 *
 * @see org.scictrl.mp.orbitcorrect.IDataBushListener
 * @author igor@scictrl.com
 */
public abstract class DataBushAdapter implements IDataBushListener {
/**
 * DataBushAdapter constructor comment.
 */
public DataBushAdapter() {
	super();
}
/**
 * {@inheritDoc}
 *
 * beamChanged method comment.
 */
@Override
public void beamChanged(DataBushEvent e) {
}
/**
 * {@inheritDoc}
 *
 * fieldChanged method comment.
 */
@Override
public void fieldChanged(DataBushEvent e) {
}
/**
 * {@inheritDoc}
 *
 * inconsistentData method comment.
 */
@Override
public void inconsistentData(DataBushEvent e) {
}
/**
 * {@inheritDoc}
 *
 * betaChanged method comment.
 */
@Override
public void machineFunctionsChanged(DataBushEvent e) {
}
/**
 * {@inheritDoc}
 *
 * fieldChanged method comment.
 */
@Override
public void rfChanged(DataBushEvent e) {
}
/**
 * {@inheritDoc}
 *
 * statusChanged method comment.
 */
@Override
public void statusChanged(DataBushEvent e) {
}
}
