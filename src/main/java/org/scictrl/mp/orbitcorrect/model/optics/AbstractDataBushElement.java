package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.DBElementDescriptor;
import org.scictrl.mp.orbitcorrect.DBString;
import org.scictrl.mp.orbitcorrect.DataBushInitializationException;
import org.scictrl.mp.orbitcorrect.ISimpleElement;


/**
 * Basic abstract element for all DataBush elements. <code>AbstractDataBushElement</code> has number
 * of properties common to all DataBush elements. Some of them are stored in DataBush input
 * file and are necessary to initialize elements:
 * <ul>
 * <li><code>name</code> - is required by ISimpleElement interface. Has to be unique for each element
 * in DataBush.</li>
 * <li><code>virtual</code> - flag specifyes basic behaviour of element, can it read and
 * send values to other elements.</li>
 * </ul>
 *
 * @see DataBush
 * @see org.scictrl.mp.orbitcorrect.ISimpleElement
 * @author igor@scictrl.com
 */
public abstract class AbstractDataBushElement implements ISimpleElement, Cloneable {

	/**
	 * Index of element's name in array of constructor's parameters. Use with
	 * DBElementDescriptor as help for working with elements dynamically.
	 */
	public static final int	PR_ELEMENT_NAME=0;
	/**
	 * Index of element's virtual flag in array of constructor's parameters. Use with
	 * DBElementDescriptor as help for working with elements dynamically.
	 */
	public static final int	PR_VIRTUAL=1;

	/**
	 * Internal DataBush handler.
	 */
	protected DataBushHandler owner;

	/** Result code for last action. */
	protected int lastActionResult;

	private String name;
	private boolean virtual= true;
	private boolean preInit=true;

	/**
	 * Constructs DataBush element with specified name and default parameter's values.
	 *
	 * @param name a {@link java.lang.String} name of element
	 */
	public AbstractDataBushElement(String name) {
		super();
		this.name= name;
	}
	/**
	 * Constructs DataBush element with specified name and virtual flag.
	 *
	 * @param name a {@link java.lang.String} name of element
	 * @param virtual a boolean virtual flag
	 */
	public AbstractDataBushElement(String name, boolean virtual) {
		super();
		this.name= name;
		this.virtual=virtual;
	}
	/**
	 * This method removes element pointers on DataBush and set it in preinit state.
	 * This method is called, when DataBush is cleared.
	 */
	void clear() {
		preInit= true;
		owner=null;
	}
	/**
	 * {@inheritDoc}
	 *
	 * Returns not initialized clone of this element. All data is copied to the clone.
	 */
	@Override
	public Object clone() {
		AbstractDataBushElement o=null;
		try {
			o= (AbstractDataBushElement)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		o.preInit=true;
		return o;
	}
	/**
	 * Returns the descriptor object for this element. <code>DBElementDescriptor</code> helps
	 * manipulating DataBush elements dynamically.
	 *
	 * @return the descriptor object for this element.
	 * @see org.scictrl.mp.orbitcorrect.DBElementDescriptor
	 */
	public abstract DBElementDescriptor descriptor();
	/**
	 * Returns the code of element's type.
	 *
	 * @return the code of element's type.
	 * @see org.scictrl.mp.orbitcorrect.DBConst
	 * @see org.scictrl.mp.orbitcorrect.DBElementDescriptor
	 */
	public abstract int elType();
	/**
	 * {@inheritDoc}
	 *
	 * Test if some other object is equal to reference object <code>obj</code>
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbstractDataBushElement) return name.equals(((AbstractDataBushElement)obj).getName());
		return false;
	}
	/**
	 * Returns the result produced by last update, apply, connect or disconnect action.
	 *
	 * @return the result produced by last update, apply, connect or disconnect action.
	 */
	public int getLastActionResult() {
		return lastActionResult;
	}
	/**
	 * {@inheritDoc}
	 *
	 * Returns the name of this element.
	 */
	@Override
	public String getName() {
		return name;
	}
	/**
	 * Returns the DataBush instance, in wich is element initialized. Null if element
	 * is not in DataBush.
	 *
	 * @return the DataBush instance, in wich is element initialized.
	 */
	public DataBush getOwner() {
		return (owner!=null) ? owner.db : null;
	}
	/**
	 * Returns virtual switch that prevents element to be updated.
	 *
	 * @return virtual switch
	 * @see setVirtual(boolean)
	 */
	public boolean getVirtual() {
		return virtual;
	}
	/**
	 * {@inheritDoc}
	 *
	 * This method return hash code of name of this element.
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	/**
	* This method specify which <code>DataBushHandler</code> is an owner of this element. Consequently this method tell us which
	* <code>DataBush</code> is an owner of it.
	*
	* @param <code>DataBushHandler</code>
	* @throws <code>DataBushInitializationException</code> if this element already has an owner
	*
	* @see <code>DataBushHandler</code>
	* @see <code>DataBushInitializationException</code>
	*/
	void init(DataBushHandler owner) throws DataBushInitializationException {
		if (this.owner!=null) throw new DataBushInitializationException("AbstractDataBushElement.init() for "+name+" can be called only once per lifetime of element");
		this.owner= owner;
		preInit=false;
	}
	/**
	 * Returns <code>true</code> if this element is currently initialized in DataBush,
	 * <code>false</code> if this element is not in DataBush.
	 *
	 * @return <code>true</code> if this element is currently initialized in DataBush.
	 */
	public boolean isInitialized() {
		return !preInit;
	}
	/**
	 * Returns virtual switch that prevents element to be updated.
	 *
	 * @return virtual switch
	 * @see setVirtual(boolean)
	 */
	public boolean isVirtual() {
		return virtual;
	}
	/**
	 * Sets virtual switch to element.
	 * NOTE! Since virtual element can not be updated, this method must be synchronized
	 * with update metod.
	 *
	 * @param value new virtual value
	 * @throws java.lang.IllegalStateException if any.
	 */
	public void setVirtual(boolean value) throws IllegalStateException {
		if (!isInitialized())
			virtual= value;
		else throw new IllegalStateException(DBString.ISE_EL_INIT);
	}
	/**
	 * Sets parameters values of this element to those in parameter array. As parameter must be
	 * used such array as for dynamical construction of new instance of this class. Current
	 * values are overriden for each not <code>null</code> object in array.
	 *
	 * @param par an array of {@link java.lang.Object} objects
	 * @throws java.lang.IllegalStateException if element is initialized and inside DataBush.
	 */
	public void setWith(Object[] par) throws IllegalStateException {
		if (isInitialized()) throw new IllegalStateException("Element inside DataBush");
		if (par[PR_VIRTUAL]!=null) virtual= ((Boolean)par[PR_VIRTUAL]).booleanValue();
	}
	/**
	 * <p>throwISE.</p>
	 *
	 * @param s java.lang.String
	 * @throws java.lang.IllegalStateException if any.
	 */
	protected void throwISE(String s) throws IllegalStateException {
		lastActionResult= DBConst.RC_ILLEGAL_STATE;
		throw new IllegalStateException(s);
	}
	/**
	 * {@inheritDoc}
	 *
	 * Returns string describing this element. String is formated in input-file code.
	 */
	@Override
	public String toString() {
		descriptor();
		return "<"+DBElementDescriptor.ELEMENT_TAG+"=\""+descriptor().getElementClassTag()+"\" "+descriptor().getParameterTag(PR_ELEMENT_NAME)+"=\""+name+"\" "+descriptor().getParameterTag(PR_VIRTUAL)+"="+virtual;
	}
}
