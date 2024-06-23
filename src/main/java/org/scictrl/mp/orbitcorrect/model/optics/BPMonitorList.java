package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DataBushPackedException;


/**
 * <p>BPMonitorList class.</p>
 *
 * @author igor@scictrl.com
 */
public class BPMonitorList extends AbstractProtectedList<BPMonitor> {

	/**
	 * This is a default constructor for <code>BPMonitorList</code>.
	 */
	BPMonitorList(DataBushHandler t) {
		super(t);
		type= BPMonitor.class;
	}
	/**
	 * This method generate and return array of <code>BPMonitor</code> elements.
	 *
	 * @return an array of {@link org.scictrl.mp.orbitcorrect.model.optics.BPMonitor} objects
	 */
	public BPMonitor[] toBPMonitorArray() {
		Object[] o= dl.toArray();
		BPMonitor[] e= new BPMonitor[o.length];
		for (int i=0; i<o.length;i++) e[i]=(BPMonitor)o[i];
		return e;
	}
	/**
	 * <p>update.</p>
	 *
	 * @return a int
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public int update() throws IllegalStateException, DataBushPackedException {
		return owner.update(this);
	}
}
