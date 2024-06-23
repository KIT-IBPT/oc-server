package org.scictrl.mp.orbitcorrect.model.optics;

import java.util.Iterator;

import org.scictrl.mp.orbitcorrect.DataBushInitializationException;
/**
 * This class contains <code>DoubleList</code> and therefore collection of SimpleElements. It has Inner Class to support iterator.
 *
 * @see HashList
 * @author igor@scictrl.com
 * 
 * @param <T> elements type
 */
public class ElementList<T extends AbstractDataBushElement> extends HashList<T> {

	private class Link {
		Link(Object el) {
			super();
			this.el= el;
		}
		public Object el;
		public Link next;
	}

	/**
	 * Default constructor.
	 */
	public ElementList() {
		super();
	}
	/**
	 * This constructor constructs <code>ElementList</code> from Array of DataBushElements.
	 *
	 * @param el an array of T[] objects
	 * @throws org.scictrl.mp.orbitcorrect.DataBushInitializationException if any.
	 */
	public ElementList(T[] el) throws DataBushInitializationException {
		super(el);
	}
	/**
	 * This constructor constructs <code>ElementList</code> from Array of DataBushElements.
	 *
	 * @param el a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @throws org.scictrl.mp.orbitcorrect.DataBushInitializationException if any.
	 */
	public ElementList(ElementList<T> el) throws DataBushInitializationException {
		super(el.size());
		super.addAll((HashList<?>)el);
	}
	/**
	 * This constructor constructs <code>ElementList</code> with specified initial capacity.
	 *
	 * @param initialCapacity int
	 */
	public ElementList(int initialCapacity) {
		super(initialCapacity);
	}
	/**
	 * This constructor constructs <code>ElementList</code> with specified initial capacity and load factor.
	 *
	 * @param initialCapacity int
	 * @param loadFactor float
	 * @throws java.lang.IllegalArgumentException if any.
	 */
	public ElementList(int initialCapacity, float loadFactor) throws IllegalArgumentException {
		super(initialCapacity,loadFactor);
	}
	/**
	 * This method return array of <code>DataBushInfo</code> elements, contained by <code>ElementList</code>.
	 * If none is contained, it return void array whose size is 1.
	 *
	 * @return an array of {@link org.scictrl.mp.orbitcorrect.model.optics.DataBushInfo} objects
	 */
	public synchronized DataBushInfo[] getDataBushInfo() {
		Iterator<T> it= iterator();
		Object o;
		int count;
		Link first=null;
		Link run;
		while (it.hasNext()){
			o= it.next();
			if (o instanceof DataBushInfo) {
				first= new Link(o);
				run=first;
				count=1;
				while (it.hasNext()){
					o=it.next();
					if (o instanceof DataBushInfo) {
						run.next= new Link(o);
						run=run.next;
						count++;
					}
				}
				run=first;
				DataBushInfo[] r= new DataBushInfo[count];
				for (int i=0; i<count; i++) {
					r[i]=(DataBushInfo)run.el;
					run=run.next;
				}
				return r;
			}
		}
		return new DataBushInfo[0];
	}
	/**
	 * This method return array of <code>DataBushInfo</code> elements, contained by <code>ElementList</code>.
	 * If none is contained, it return void array whose size is 1.
	 *
	 * @return an array of {@link org.scictrl.mp.orbitcorrect.model.optics.RFGenerator} objects
	 */
	public synchronized RFGenerator[] getRFGenerator() {
		Iterator<T> it= iterator();
		Object o;
		int count;
		Link first=null;
		Link run;
		while (it.hasNext()){
			o= it.next();
			if (o instanceof RFGenerator) {
				first= new Link(o);
				run=first;
				count=1;
				while (it.hasNext()){
					o=it.next();
					if (o instanceof RFGenerator) {
						run.next= new Link(o);
						run=run.next;
						count++;
					}
				}
				run=first;
				RFGenerator[] r= new RFGenerator[count];
				for (int i=0; i<count; i++) {
					r[i]=(RFGenerator)run.el;
					run=run.next;
				}
				return r;
			}
		}
		return new RFGenerator[0];
	}

}
