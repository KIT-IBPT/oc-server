package org.scictrl.mp.orbitcorrect.epics;

import org.scictrl.mp.orbitcorrect.model.IDataConnector;
import org.scictrl.mp.orbitcorrect.model.optics.IBindedElement;

/**
 * <p>
 * Abstract EAbstractDataConnector class.
 * </p>
 *
 * @author igor@scictrl.com
 * 
 * @param <T> data type for {@link IDataConnector} and {@link IBindedElement}
 */
public abstract class EAbstractDataConnector<T> implements IDataConnector<T> {

	/** Engine. */
	protected EControlSystemEngine engine;

	/** Element connected. */
	protected IBindedElement<T> element;

	private Class<T> dataType;

	/**
	 * <p>
	 * Constructor for EAbstractDataConnector.
	 * </p>
	 *
	 * @param type    a {@link java.lang.Class} object
	 * @param engine  a
	 *                {@link org.scictrl.mp.orbitcorrect.epics.EControlSystemEngine}
	 *                object
	 * @param element a
	 *                {@link org.scictrl.mp.orbitcorrect.model.optics.IBindedElement}
	 *                object
	 * @throws java.lang.Exception if any.
	 */
	public EAbstractDataConnector(Class<T> type, EControlSystemEngine engine, IBindedElement<T> element)
			throws Exception {
		this.dataType = type;
		this.engine = engine;
		this.element = element;

		if (type == null) {
			throw new NullPointerException("Data type is null!");
		}
		if (engine == null) {
			throw new NullPointerException("Databush engine is null!");
		}
	}

	/** {@inheritDoc} */
	@Override
	public Class<T> dataType() {
		return dataType;
	}

}
