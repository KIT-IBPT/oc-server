package org.scictrl.mp.orbitcorrect.accessories;

import org.scictrl.mp.orbitcorrect.DataBushException;
import org.scictrl.mp.orbitcorrect.IDataBushReader;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;

/**
 * PropertiesReader is used to read input files and return the ElementList of
 * elements in storage ring.
 *
 * @author igor@scictrl.com
 */
public class PropertiesReader extends AbstractParameterReader implements IDataBushReader {
	private ElementList<AbstractDataBushElement> list;


	/**
	 * Constructor.
	 */
	public PropertiesReader() {
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * Returns list of elements created by DefaultDBReader. Elements are stored in
	 * <code>ElementList</code> list. List is created and filled after call to
	 * <code>read()</code> method.
	 */
	@Override
	public ElementList<AbstractDataBushElement> getList() {
		return list;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Openes input file.
	 */
	@Override
	public void open(java.io.File file) throws org.scictrl.mp.orbitcorrect.DataBushException {
		try {
			uRL = new java.net.URL("file:///" + file.toString());
			st = ST_FILE;
		} catch (java.net.MalformedURLException e) {
			throw new DataBushException("Unable to create URL from file name: " + e.toString());
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Openes input URL.
	 */
	@Override
	public void open(java.net.URL url) throws org.scictrl.mp.orbitcorrect.DataBushException {
		uRL = url;
		st = ST_URL;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Reads input and creates DataBush elements. Input must be opened with one of
	 * <code>ParameterReader.openInStream</code> methods. Created instances of
	 * DataBush elements are stored and returned in <code>ElementList</code>. Call
	 * read only once per new opened stream.
	 */
	@Override
	public ElementList<AbstractDataBushElement> read() throws DataBushException {
		return read(new ElementList<>());
	}

	/**
	 * {@inheritDoc}
	 *
	 * Reads input and creates DataBush elements. Input must be opened with one of
	 * <code>ParameterReader.openInStream</code> methods. Created instances of
	 * DataBush elements are stored and returned in <code>ElementList</code>. Call
	 * read only once per new opened stream.
	 */
	@Override
	public ElementList<AbstractDataBushElement> read(ElementList<AbstractDataBushElement> l) throws DataBushException {
		debugPrintln("ParameterReader [" + getURL().toString() + "]");
		try {
			getProperties().load(new java.io.BufferedInputStream(uRL.openStream()));
		} catch (Exception e) {
			throw new DataBushException(e.toString());
		}
		return list;
	}
}
