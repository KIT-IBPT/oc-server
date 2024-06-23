package org.scictrl.mp.orbitcorrect;

import org.scictrl.mp.orbitcorrect.model.optics.DataBush;

/**
 * A listener interface receiving DataBush events. Class with this interface can be registered
 * with DataBush to receive events, when DataBush properties are changed.
 *
 * @see DataBush
 * @author igor@scictrl.com
 */
public interface IDataBushListener extends java.util.EventListener {
	/**
	 * Invoked when BPM positions change.
	 *
	 * @see org.scictrl.mp.orbitcorrect.DataBushEvent
	 * @param e a {@link org.scictrl.mp.orbitcorrect.DataBushEvent} object
	 */
	void beamChanged(DataBushEvent e);
	/**
	 * Invoked when manget fields change.
	 *
	 * @see org.scictrl.mp.orbitcorrect.DataBushEvent
	 * @param e a {@link org.scictrl.mp.orbitcorrect.DataBushEvent} object
	 */
	void fieldChanged(DataBushEvent e);
	/**
	 * Invoked when InconsistentDataException occurs. For example, when there is not solution
	 * for closed orbit.
	 *
	 * @see org.scictrl.mp.orbitcorrect.DataBushEvent
	 * @param e a {@link org.scictrl.mp.orbitcorrect.DataBushEvent} object
	 */
	void inconsistentData(DataBushEvent e);
	/**
	 * Invoked when machine functions change.
	 *
	 * @see org.scictrl.mp.orbitcorrect.DataBushEvent
	 * @param e a {@link org.scictrl.mp.orbitcorrect.DataBushEvent} object
	 */
	void machineFunctionsChanged(DataBushEvent e);
	/**
	 * Invoked when RF frequency change.
	 *
	 * @see org.scictrl.mp.orbitcorrect.DataBushEvent
	 * @param e a {@link org.scictrl.mp.orbitcorrect.DataBushEvent} object
	 */
	void rfChanged(DataBushEvent e);
	/**
	 * Invoked when DataBush status change.
	 *
	 * @see org.scictrl.mp.orbitcorrect.DataBushEvent
	 * @param e a {@link org.scictrl.mp.orbitcorrect.DataBushEvent} object
	 */
	void statusChanged(DataBushEvent e);
}
