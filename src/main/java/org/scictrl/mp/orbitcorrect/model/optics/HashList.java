package org.scictrl.mp.orbitcorrect.model.optics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.scictrl.mp.orbitcorrect.DataBushInitializationException;
import org.scictrl.mp.orbitcorrect.ISimpleElement;
/**
 * This type contain <code>ArrayList</code> and <code>HashMap</code> and enables searching for elements if we know
 * position in list or hash code. It can contain only <code>ISimpleElement</code>
 *
 * @see org.scictrl.mp.orbitcorrect.ISimpleElement
 * @author igor@scictrl.com
 * 
 * @param <T> element type
 */
public class HashList<T extends ISimpleElement> implements List<T>, Cloneable {
	private final ArrayList<T> list;
	private final HashMap<String,T> hash;
	/**
	 * DoubleList default constructor.
	 */
	public HashList() {
		super();
		list= new ArrayList<>();
		hash= new HashMap<>();
	}
	/**
	 * This constructor construct <code>DoubleList</code> from <code>ArrayList</code>
	 *
	 * @param el an array of S[] objects
	 * @param <S> a S class
	 * @throws org.scictrl.mp.orbitcorrect.DataBushInitializationException if any.
	 */
	public <S extends T> HashList(S[] el) throws DataBushInitializationException{
		super();
		list= new ArrayList<>(el.length);
		hash= new HashMap<>(el.length);
		for (S element : el)
			if (!add(element)) {
				throw new DataBushInitializationException("There is more than one element with name "+element.getName());
			}
	}
	/**
	 * Constructor, that require initial capacity.
	 *
	 * @param initialCapacity a int
	 */
	public HashList(int initialCapacity) {
		super();
		list= new ArrayList<>(initialCapacity);
		hash= new HashMap<>(initialCapacity);
	}
	/**
	 * Constructor, that require initial capacity and load factor.
	 *
	 * @param initialCapacity a int
	 * @param loadFactor a float
	 * @throws java.lang.IllegalArgumentException if any.
	 */
	public HashList(int initialCapacity, float loadFactor) throws IllegalArgumentException {
		super();
		list= new ArrayList<>(initialCapacity);
		hash= new HashMap<>(initialCapacity, loadFactor);
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method add element to <code>DoubleList</code> on specified location on internal <code>ArrayList</code>
	 */
	@Override
	public void add(int index, T element) {
		if (hash.containsKey(element.getName())) {
			throw new IllegalArgumentException("Element with name "+element.getName()+" already esist in list.");
		}
		list.add(index, element);
		hash.put(element.getName(), element);
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method add element to <code>DoubleList</code>
	 */
	@Override
	public boolean add(T element) {
		if (hash.containsKey(element.getName())) {
			return false;
		}
		list.add(element);
		hash.put(element.getName(), element);
		return true;
	}
	/**
	 * This method adds all elements from <code>DoubleList</code>, taken as a parameter, to this <code>DoubleList</code>
	 *
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.HashList} object
	 * @return a boolean
	 */
	@SuppressWarnings("unchecked")
	public boolean addAll(HashList<?> l) {
		for (ISimpleElement element : l) {
			if (hash.containsKey(element.getName())) return false;
		}
		list.addAll((Collection<? extends T>) l.list);
		hash.putAll((Map<? extends String, ? extends T>) l.hash);
		return true;
	}
	/** {@inheritDoc} */
	@Override
	public boolean addAll(Collection<? extends T> c) {
		Iterator<? extends T> it= c.iterator();
		while (it.hasNext()) {
			if (hash.containsKey(((ISimpleElement)it.next()).getName())) return false;
		}
		it= c.iterator();
		while (it.hasNext()) {
			add(it.next());
		}
		return true;
	}
	/** {@inheritDoc} */
	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		Iterator<? extends T> it= c.iterator();
		while (it.hasNext()) {
			if (hash.containsKey(((ISimpleElement)it.next()).getName())) return false;
		}
		it= c.iterator();
		int i=index;
		while (it.hasNext()) {
			add(i++,it.next());
		}
		return true;
	}
	/** {@inheritDoc} */
	@Override
	public boolean containsAll(Collection<?> c) {
		return hash.values().containsAll(c);
	}
	/** {@inheritDoc} */
	@Override
	public boolean removeAll(Collection<?> c) {
		boolean b= list.removeAll(c);
		Iterator<?> it= c.iterator();
		while (it.hasNext()) {
			Object o= it.next();
			if (o instanceof ISimpleElement) {
				b|=hash.remove(((ISimpleElement)it.next()).getName())!=null;
			}
		}
		return b;
	}
	/** {@inheritDoc} */
	@Override
	public boolean retainAll(Collection<?> c) {
		ListIterator<T> lit= list.listIterator();
		boolean b= false;
		while (lit.hasNext()) {
			T el= lit.next();
			if (!c.contains(el)) {
				hash.remove(el.getName());
				lit.remove();
				b=true;
			}
		}
		return b;
	}

	/**
	 * {@inheritDoc}
	 *
	 * This method removes all elements from this <code>DoubleList</code>
	 */
	@Override
	public void clear() {
		list.clear();
		hash.clear();
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method return not initialized clone of this <code>DoubleList</code>
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object clone() {
		HashList<T> l;
		try {
			l = (HashList<T>)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			l = new HashList<>(list.size());
			l.list.addAll(list);
			l.hash.putAll(hash);
		}
		return l;
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method tests, if our list contains object, given as a parameter.
	 */
	@Override
	public boolean contains(Object o) {
		return (o!=null) ? ( (o instanceof ISimpleElement) ? contains(o) : false ) : false;
	}
	/**
	 * This method tests, if our list contains <code>ISimpleElement</code>, given as a parameter.
	 *
	 * @param l a T object
	 * @return a boolean
	 */
	public boolean contains(T l) {
		return (l!=null) ? hash.containsKey(l.getName()) : false;
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method returns reference to element from our <code>DoubleList</code> with index, specified by parameter.
	 */
	@Override
	public T get(int index) {
		return list.get(index);
	}
	/**
	 * This method returns reference to element from our <code>DoubleList</code> with name, specified by parameter.
	 *
	 * @param name a {@link java.lang.String} object
	 * @return a T object
	 */
	public T get(String name) {
		return hash.get(name);
	}
	/**
	 * This method returns index of specified element.
	 *
	 * @return index of element
	 * @param element a T object
	 */
	public int indexOf(T element) {
		return list.indexOf(element);
	}
	/** {@inheritDoc} */
	@Override
	public int indexOf(Object o) {
		if (o instanceof ISimpleElement) {
			return list.indexOf(o);
		}
		if (o instanceof String) {
			T t= hash.get(o);
			if (t==null) return -1;
			return list.indexOf(t);
		}
		return -1;
	}
	/**
	 * <p>indexOf.</p>
	 *
	 * @param o a {@link java.lang.String} object
	 * @return a int
	 */
	public int indexOf(String o) {
		T t= hash.get(o);
		if (t==null) return -1;
		return list.indexOf(t);
	}
	/**
	 * <p>lastIndexOf.</p>
	 *
	 * @param element a T object
	 * @return a int
	 */
	public int lastIndexOf(T element) {
		return list.lastIndexOf(element);
	}
	/** {@inheritDoc} */
	@Override
	public int lastIndexOf(Object o) {
		if (o instanceof ISimpleElement) {
			return list.lastIndexOf(o);
		}
		return -1;
	}
	/** {@inheritDoc} */
	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		List<T> l= list.subList(fromIndex, toIndex);
		HashList<T> dl= new HashList<>(l.size());
		dl.addAll(l);
		return dl;
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method tests, if (<code>ArrayList</code> of) our <code>DoubleList</code> is empty.
	 */
	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method returns iterator of DoubleList's ArrayList.
	 * @see java.util.Iterator
	 */
	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method returns ListIterator of DoubleList's ArrayList.
	 * @see java.util.ListIterator
	 */
	@Override
	public ListIterator<T> listIterator() {
		return list.listIterator();
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method returns ListIterator of DoubleList's ArrayList, starting at element with <code>index</code>.
	 * @see java.util.ListIterator
	 */
	@Override
	public ListIterator<T> listIterator(int index) {
		return list.listIterator(index);
	}
	/**
	 * This method returns ListIterator of DoubleList's ArrayList, starting at element eith name <code>name</code>.
	 *
	 * @see java.util.ListIterator
	 * @param name a {@link java.lang.String} object
	 * @return a {@link java.util.ListIterator} object
	 */
	public ListIterator<T> listIterator(String name) {
		return list.listIterator(list.indexOf(hash.get(name)));
	}
	/**
	 * This method returns ListIterator of DoubleList's ArrayList, starting at ISimpleElement <code>element</code>.
	 *
	 * @see java.util.ListIterator
	 * @param element a {@link org.scictrl.mp.orbitcorrect.ISimpleElement} object
	 * @return a {@link java.util.ListIterator} object
	 */
	public ListIterator<T> listIterator(ISimpleElement element) {
		return list.listIterator(list.indexOf(element));
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method removes object whose index is <code>index</code>
	 */
	@Override
	public T remove(int index) {
		hash.remove(((ISimpleElement)list.get(index)).getName());
		return list.remove(index);
	}
	/**
	 * Removes object from list.
	 *
	 * @param element a T object
	 * @return a boolean
	 */
	public boolean remove(T element) {
		if (!hash.containsKey(element.getName())) return false;
		hash.remove(element.getName());
		list.remove(element);
		return true;
	}
	/** {@inheritDoc} */
	@Override
	public boolean remove(Object o) {
		if (o instanceof ISimpleElement) {
			return remove(o);
		}
		return false;
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method removes element at specified position <code>index</code> and put on this position
	 * new ISimpleElement <code>element</code>.
	 */
	@Override
	public T set(int index, T element) {
		hash.remove(((ISimpleElement)list.get(index)).getName());
		hash.put(element.getName(), element);
		return list.set(index, element);
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method returns size of this list.
	 */
	@Override
	public int size() {
		return list.size();
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method sort <code>DoubleList</code> with respect to specified comparator as parameter.
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void sort(Comparator c) {
		Collections.sort(list,c);
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method returns this DoubleList, converted to Array.
	 */
	@Override
	public Object[] toArray() {
		return list.toArray();
	}
	/**
	 * {@inheritDoc}
	 *
	 * <p>toArray.</p>
	 */
	@Override
	public <U extends Object> U[] toArray(U[] a) {
		return list.toArray(a);

	}
	/**
	 * {@inheritDoc}
	 *
	 * This method returns string that describe <code>DoubleList</code>.
	 */
	@Override
	public String toString() {
		String st= new String();
		Iterator<T> it= list.iterator();
		while (it.hasNext())
			st+= it.next().toString();
		return st;
	}
}
