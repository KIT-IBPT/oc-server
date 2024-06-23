package org.scictrl.mp.orbitcorrect.accessories;


/**
 * <p>LinkedNode class.</p>
 *
 * @author igor@scictrl.com
 */
public class LinkedNode {
	private LinkedNode next = null;
	private LinkedNode previous = null;
	private java.lang.Object object = null;
/**
 * LinkedNode constructor comment.
 *
 * @param o a {@link java.lang.Object} object
 */
public LinkedNode(Object o) {
	this(o,null,null);
}
/**
 * LinkedNode constructor comment.
 */
private LinkedNode(Object o, LinkedNode n, LinkedNode p) {
	super();
	object=o;
	previous= p;
	next= n;
}
/**
 *
 * @return boolean
 * @param o java.lang.Object
 */
private LinkedNode _removeNext(Object o) {
	LinkedNode good=null,_good=null;
	if (object.equals(o)) {
		if (previous!=null) {
			previous.next= next;
			good=previous;
		}
		if (next!=null) {
			next.previous=previous;
			if (good==null) good= next;
			_good= next._removeNext(o);
		}
		previous=null;
		next=null;
	}
	if (next!=null) {
		_good= next._removeNext(o);
	}
	if (good==null) return _good;
	return good;
}
/**
 *
 * @return boolean
 * @param o java.lang.Object
 */
private LinkedNode _removePrevious(Object o) {
	LinkedNode good=null,_good=null;
	if (object.equals(o)) {
		if (previous!=null) {
			previous.next= next;
			good=previous;
			_good= next._removePrevious(o);
		}
		if (next!=null) {
			next.previous=previous;
			if (good==null) good= next;
		}
		previous=null;
		next=null;
	}
	if (previous!=null) {
		_good= previous._removeNext(o);
	}
	if (good==null) return _good;
	return good;
}
/**
 * Adds new nod after this node.
 *
 * @return LinkedNode new node
 * @param o java.lang.Object
 */
public synchronized LinkedNode add(Object o) {
	if (next==null) return next=new LinkedNode(o,null,this);
	return next=next.previous=new LinkedNode(o,next,this);
}
/**
 * <p>first.</p>
 *
 * @return LinkedNode
 */
public synchronized LinkedNode first() {
	return previous==null ? this : previous.first();
}
/**
 * <p>get.</p>
 *
 * @return java.lang.Object
 */
public Object get() {
	return object;
}
/**
 * <p>hasNext.</p>
 *
 * @return boolean
 */
public synchronized boolean hasNext() {
	return next!=null;
}
/**
 * <p>hasPrevious.</p>
 *
 * @return boolean
 */
public synchronized boolean hasPrevious() {
	return previous!=null;
}
/**
 * Inserts new node before this node.
 *
 * @return LinkedNode new node
 * @param o java.lang.Object
 */
public synchronized LinkedNode insert(Object o) {
	if (previous==null) return previous=new LinkedNode(o,this,null);
	return previous=previous.next=new LinkedNode(o,this,previous);
}
/**
 * <p>last.</p>
 *
 * @return LinkedNode
 */
public LinkedNode last() {
	return next==null ? this : next.last();
}
/**
 * <p>next.</p>
 *
 * @return LinkedNode
 */
public LinkedNode next() {
	return next;
}
/**
 * <p>previous.</p>
 *
 * @return LinkedNode
 */
public LinkedNode previous() {
	return previous;
}
/**
 * <p>remove.</p>
 *
 * @return boolean
 * @param o java.lang.Object
 */
public synchronized LinkedNode remove(Object o) {
	LinkedNode p=null,n=null;
	if (next!=null) next._removeNext(o);
	p= _removePrevious(o);
	if (p!=null) return p;
	return n;
}
/**
 * <p>size.</p>
 *
 * @return int
 */
public synchronized int size() {
	if (next==null) return 1;
	return 1+next.size();
}
/**
 * <p>toArray.</p>
 *
 * @return java.lang.Object[]
 */
public synchronized Object[] toArray() {
	Object[] o= new Object[size()];
	LinkedNode n=this;
	for (int i=0;i<o.length; i++) {
		o[i]=n.object;
		n=n.next;
	}
	return o;
}
}
