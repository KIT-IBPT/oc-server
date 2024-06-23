package org.scictrl.mp.orbitcorrect.model.optics;

import java.util.Comparator;
import java.util.ListIterator;

import org.scictrl.mp.orbitcorrect.DataBushInitializationException;
import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
/**
 * This list contains elements type <code>AbstractOpticalElement</code>.
 *
 * @see OpticsList
 * @see AbstractOpticalElement
 * @author igor@scictrl.com
 */
public class OpticsList extends AbstractProtectedList<AbstractOpticalElement> {

	/**
	 * This is a default constructor for <code>OpticsList</code>.
	 */
	OpticsList(DataBushHandler t) {
		super(t);
		type=AbstractOpticalElement.class;
	}
	/**
	 * This method execute apply method on owner of this list.
	 *
	 * @see DataBush#apply()
	 * @return a int integer which indicate if method succeded
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public int apply() throws InconsistentDataException, IllegalStateException, DataBushPackedException {
		return owner.apply(owner.ent.magnets);
	}
	/**
	 *
	 */
	void reindex() {
		ListIterator<AbstractOpticalElement> it= dl.listIterator();
		int i=0;
		while (it.hasNext()) it.next().setIndex(i++);
	}
	/**
	 *
	 * @param c java.util.Comparator
	 */
	@Override
	@SuppressWarnings({ "rawtypes" })
	void sort(Comparator c) {
		super.sort(c);
		reindex();
	}
	/**
	 * <p>update.</p>
	 *
	 * @return a int
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public int update() throws InconsistentDataException, IllegalStateException, DataBushPackedException {
		return owner.update(this);
	}
	@Override
	void fillMatching(HashList<?> l) throws DataBushInitializationException {
		super.fillMatching(l);
		reindex();
	}
}
