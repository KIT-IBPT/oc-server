package org.scictrl.mp.orbitcorrect.model.optics;

import java.util.Comparator;
import java.util.Iterator;

import org.scictrl.mp.orbitcorrect.DataBushInitializationException;
import org.scictrl.mp.orbitcorrect.ISimpleElement;

/**
 * Abstract list for holding DataBush elements in DataBush. Implements basic behaviour for
 * all cast-save readonly lists. This list is backed upon DoubleList.
 *
 * @author igor@scictrl.com
 * 
 * @param <T> elements type
 */
public abstract class AbstractProtectedList<T extends ISimpleElement> {

	/** Core operation object*/
	protected DataBushHandler owner;
	/** Elements list */
	protected HashList<T> dl;
	/** Elements type */
	protected Class<T> type;
	/**
	 * This method constructs <code>AbsrtactProtectedList</code>
	 * @param <code>owner</code> is an owner of constructed list
	 */
	AbstractProtectedList(DataBushHandler owner) {
		super();
		dl= new HashList<>(50,(float)0.5);
		this.owner=owner;
	}
	void clear() {
		dl.clear();
	}
	/**
	 * Returns <code>true</code> if this list contains the specified element.
	 *
	 * @return returns <code>true</code> if list contains element <code>o</code>, <code>false</code> otherwise.
	 * @param o a {@link java.lang.Object} element whose presence in this List is to be tested.
	 */
	public boolean contains(Object o) {
		return dl.contains(o);
	}
	/**
	 * Returns DataBush that owns this list.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} owner of this list
	 */
	public DataBush getOwner() {
		return owner.db;
	}
	/**
	 * Tests if this list has no elements.
	 *
	 * @return <code>true</code> if this list has no elements, <code>false</code> otherwise.
	 */
	public boolean isEmpty() {
		return dl.isEmpty();
	}
	/**
	 * Returns the number of elements in this list.
	 *
	 * @return the number of elements in this list
	 */
	public int size() {
		return dl.size();
	}
	void sort(Comparator<?> c) {
		dl.sort(c);
	}
	/**
	 * Returns an array containing all of the elements in this list in the correct order.
	 *
	 * @return an array of {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement} an array containing all of the elements in this list in the correct order
	 */
	public AbstractDataBushElement[] toArray() {
		Object[] o= dl.toArray();
		AbstractDataBushElement[] e= new AbstractDataBushElement[o.length];
		for (int i=0; i<o.length;i++) e[i]=(AbstractDataBushElement)o[i];
		return e;
	}

	/**
	 * <p>toElementList.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public ElementList<?> toElementList() {
		ElementList<?> l= new ElementList<>();
		l.addAll(dl);
		return l;
	}
	/**
	 * {@inheritDoc}
	 *
	 * Returns a string representation of the object. Returns string representation of
	 * all contained elements (calls <code>toString()</code> on each) in correct order seperated by
	 * EOL sequence.
	 */
	@Override
	public String toString() {
		return dl.toString();
	}

	void fillMatching(AbstractProtectedList<?> l) throws DataBushInitializationException {
		fillMatching(l.dl);
	}

	@SuppressWarnings("unchecked")
	void fillMatching(HashList<?> l) throws DataBushInitializationException {
		Object el;
		Iterator<?> it= l.iterator();
		while (it.hasNext()) {
			el= it.next();
			if (type.isInstance(el))
				if (!dl.add((T) el)) {
					dl=null;
					throw new DataBushInitializationException("There is more than one element with name "+el);
				}
		}
	}
	/**
	 * <p>get.</p>
	 *
	 * @param i a int
	 * @return a T object
	 */
	public T get(int i) {
		return dl.get(i);
	}
	/**
	 * <p>get.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @return a T object
	 */
	public T get(String name) {
		return dl.get(name);
	}
	/**
	 * <p>add.</p>
	 *
	 * @param a a T object
	 * @return a boolean
	 */
	public boolean add(T a) {
		return dl.add(a);
	}
	/**
	 * This method returns <code>HorCorrectorIterator</code> of the elements in the list. Iteraotr starts at the begining.
	 *
	 * @return a {@link java.util.Iterator} object
	 */
	public Iterator<T> iterator() {
		return dl.iterator();
	}
	/**
	 * This method returns <code>HorCorrectorIterator</code> of the elements in the list. Iteraotr starts at the
	 * element with position <code>index</code> in list.
	 *
	 * @param index a int
	 * @return a {@link java.util.Iterator} object
	 */
	public Iterator<T> iterator(int index) {
		return dl.listIterator(index);
	}
	/**
	 * This method returns <code>HorCorrectorIterator</code> of the elements in the list. Iteraotr starts at the
	 * element, which is specified as parameter.
	 *
	 * @param element a {@link org.scictrl.mp.orbitcorrect.model.optics.HorCorrector} object
	 * @return a {@link java.util.Iterator} object
	 */
	public Iterator<T> iterator(HorCorrector element) {
		return dl.listIterator(element);
	}

}
