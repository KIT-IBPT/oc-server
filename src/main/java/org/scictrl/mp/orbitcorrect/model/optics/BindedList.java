package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DataBushPackedException;


/**
 * <p>BindedList class.</p>
 *
 * @author igor@scictrl.com
 */
@SuppressWarnings("rawtypes")
public class BindedList extends AbstractProtectedList<IBindedElement> {

	/**
	 * This is a default constructor for <code>BindedElementList</code>.
	 */
	BindedList(DataBushHandler t) {
		super(t);
		type=IBindedElement.class;
	}

	/**
	 * This method generate and return array of <code>IBindedElement</code> elements.
	 *
	 * @return an array of {@link org.scictrl.mp.orbitcorrect.model.optics.IBindedElement} objects
	 */
	public IBindedElement[] toBindedElementArray() {
		Object[] o= dl.toArray();
		IBindedElement[] e= new IBindedElement[o.length];
		for (int i=0; i<o.length;i++) e[i]=(IBindedElement)o[i];
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
