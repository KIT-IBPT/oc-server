package org.scictrl.mp.orbitcorrect;

import org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;


/**
 * DatabushPackedException is used to pack together in one exception
 * object more than one exception when complex method is called.
 * This allows to pack together several problems and throw them at appropriate moment.
 *
 * @see DataBush#update()
 * @author igor@scictrl.com
 */
public class DataBushPackedException extends DataBushException {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Array with exception packed into this one.
	 */
	public DataBushException[] packed= {};

	/**
	 * Create empty exception, will not be thrown.
	 */
	public DataBushPackedException() {
		super();
	}

	/**
	 * <code>PackedDBException</code> constructor with specified parameters.
	 *
	 * @param message the message
	 * @param code the error code
	 */
	public DataBushPackedException(String message, int code) {
		this();
		pack(null,message,code);
	}
	/**
	 * <code>PackedDBException</code> constructor with specified parameters.
	 *
	 * @param s java.lang.String
	 * @param source AbstractDataBushElement
	 * @param code int
	 */
	public DataBushPackedException(AbstractDataBushElement source, String s, int code) {
		this();
		pack(source,s,code);
	}
	/**
	 * Created packed exception with provided exception as initial element.
	 *
	 * @param dbe initial exception to be packed
	 */
	public DataBushPackedException(DataBushException dbe) {
		this();
		pack(dbe);
	}
	/**
	 * {@inheritDoc}
	 *
	 * Returns the fully-qualified name of the entity represented by this <code>DataBushException</code>
	 * object, as a <code>String</code>.
	 */
	@Override
	protected String getClassName() {
		return this.getClass().getName();
	}
	/**
	 * PAcks an exception into this one.
	 *
	 * @param dbe to be packed
	 * @return a int
	 */
	public int pack(DataBushException dbe) {
		DataBushException[] ex= new DataBushException[packed.length+1];
		System.arraycopy(packed,0,ex,0,packed.length);
		ex[ex.length-1]= dbe;
		packed=ex;
		return dbe.getCode();

	}

	/**
	 * <p>count.</p>
	 *
	 * @return a int
	 */
	public int count() {
		if (packed==null) {
			return 0;
		}
		return packed.length;
	}

	/**
	 * {@inheritDoc}
	 *
	 * This method return string that describe this class object.
	 */
	@Override
	public java.lang.String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append(getClass().getName());
		sb.append(" { ");
		if (0<packed.length) sb.append(packed[0].formatMessage());
		for (int i=1; i<packed.length; sb.append(packed[i++].formatMessage())) sb.append(", ");
		sb.append(" }");
		return sb.toString();
	}

	/**
	 * This method adds new <code>PackedDBException</code> and return its code.
	 * @return int
	 * @param source AbstractDataBushElement
	 * @param s java.lang.String
	 * @param code int
	 */
	private int pack(AbstractDataBushElement source, String s, int code) {
		pack(new DataBushException(source, s, code));
		return code;
	}

	/**
	 * If <code>DBExceptionHolder</code> has an exception, this method throw it, otherwise do nothing.
	 *
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public void throwIt() throws DataBushPackedException {
		if (count()>0) throw this;
	}

	/**
	 * <p>maxCode.</p>
	 *
	 * @return a int
	 */
	public int maxCode() {
		int i=0;
		for (DataBushException dbe : packed) {
			if (dbe.code>i) {
				i=dbe.code;
			}
		}
		return i;
	}
}
