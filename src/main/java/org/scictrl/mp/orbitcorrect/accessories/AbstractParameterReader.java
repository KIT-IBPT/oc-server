package org.scictrl.mp.orbitcorrect.accessories;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scictrl.mp.orbitcorrect.IDataBushReader;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
/**
 * Class that give basic methodes implemented in all inherited class.
 *
 * @author igor@scictrl.com
 */
public abstract class AbstractParameterReader {

	private java.util.Properties properties = new java.util.Properties();

	/** File tokenizer */
	protected FileTokenizer tok;
	/** Tokenizer status */
	protected int st;
	/** base URL */
	protected java.net.URL uRL;
	/** Logger */
	protected Logger log;


	/**
	 * <p>Constructor for AbstractParameterReader.</p>
	 */
	public AbstractParameterReader() {
		log= LogManager.getLogger(getClass());
	}

/**
 * Closes opened input stream.
 * After stream is closed, new stream may be opened and read.
 *
 * @throws org.scictrl.mp.orbitcorrect.DataBushException if any.
 */
public void close() throws org.scictrl.mp.orbitcorrect.DataBushException {
	try {
		if (tok!=null) tok.close();
	} catch (IOException e) {throw new org.scictrl.mp.orbitcorrect.DataBushException("Error closing reader for "+tok.getURL().toString()+": "+e.toString());}
}
/**
 * Writes in console aditional debug output, if it is enabled.
 *
 * @param s java.lang.String
 * @see DataBush#debug
 */
protected void debugPrint(String s) {
	log.debug(s);
}
/**
 * <p>debugPrintln.</p>
 *
 * @param s java.lang.String
 */
protected void debugPrintln(String s) {
	log.debug(s);
}
/**
 * <p>Getter for the field <code>properties</code>.</p>
 *
 * @return java.util.Properties
 */
public java.util.Properties getProperties() {
	return properties;
}
/**
 * <p>getSourceType.</p>
 *
 * @return int
 */
public int getSourceType() {
	return st;
}
/**
 * <p>Getter for the field <code>uRL</code>.</p>
 *
 * @return java.net.URL
 */
public java.net.URL getURL() {
	return uRL;
}
/**
 * open method comment.
 *
 * @param file a {@link java.io.File} object
 * @throws org.scictrl.mp.orbitcorrect.DataBushException if any.
 */
public void open(File file) throws org.scictrl.mp.orbitcorrect.DataBushException {
	try {
		tok= new FileTokenizer(file);
		st= IDataBushReader.ST_FILE;
		uRL=tok.getURL();
	} catch (FileNotFoundException e) {throw new org.scictrl.mp.orbitcorrect.DataBushException("Error opening file "+file.toString()+" : "+e.toString());}
}
/**
 * open method comment.
 *
 * @param url a {@link java.net.URL} object
 * @throws org.scictrl.mp.orbitcorrect.DataBushException if any.
 */
public void open(java.net.URL url) throws org.scictrl.mp.orbitcorrect.DataBushException {
	try {
		tok= new FileTokenizer(url);
		st= IDataBushReader.ST_URL;
		uRL=url;
	} catch (IOException e) {throw new org.scictrl.mp.orbitcorrect.DataBushException("Error opening URL "+url.toString()+" : "+e.toString());}
}
/**
 * <p>Setter for the field <code>properties</code>.</p>
 *
 * @param newProperties java.util.Properties
 * @return a {@link java.util.Properties} object
 */
public java.util.Properties setProperties(java.util.Properties newProperties) {
	return properties = newProperties;
}
}
