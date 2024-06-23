package org.scictrl.mp.orbitcorrect;

import java.io.File;

import org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;

/**
 * This is interface for DataBush structure readers.
 * DataBush reader should store elements in ElementList.
 * This list is used to initialized DataBush. <br>
 * <br>
 * <em>about DataBush</em><br>
 * DataBush gives physical view to accelerator.
 * Accelerator devices or elements, as power supplies, magnets..., are represented as individual instances of JAVA objects.
 * These are called DataBush elements.
 * DataBush does not provides only organized access to DataBush elements, it also bind them together and with accelerator devices and perform some linear optics calculations.
 * Before use of DataBush, elements must be introduced to DataBush with initialization process.
 * DataBush does not know, how elements are obtained, this is task ob IDataBushReader.
 * IDataBushReader gets description, what kind of elements to instantiate from input file.<br>
 * <br>
 * <em>about ElementList</em><br>
 * <code>ElementList</code> is simple collection for holding DataBush elements.
 * It can hold only elements with unique names. Elements can be accessed through their names or position in list.<br>
 * <br>
 * <em>about elements</em><br>
 * Under name "element" could be understood at least three things.
 * One is accelerator device. Other is DataBush element, or instance of JAVA object, that can connect itself to accelerator devices and perform operations.
 * Third is description of accelerator element in input file.<br>
 * DataBush can operate only with its DataBush elements. Each DataBush element represents individual accelerator device.<br>
 * Input file hold elements, that are no more than lines of formatted text.
 * Since they represents accelerator devices, they can be transformed by IDataBushReader into DataBush elements.<br>
 * So line in input file is not really an element in our sense, just description of one.
 * When we say <em>element</em> or <em>DataBush element</em>, we mean instance of JAVA object in DataBush, representing accelerator device.<br>
 * To <em>read an element</em> in IDataBushReader means to read line of description an instantiate an DataBush element with read parameters.
 * This interface is used as basic interface for all elements in package
 * <code>util</code> that must read from file or any other
 * kind of input stream. This interface introduces some basic behaviour of reading
 * input stream.<br>
 * <br>
 * Reading input stream should be performed in following three steps:
 * <ul>
 * <li>open input stream with <code>openInStream</code>. There are provided
 * three different ways of opening input stream</li>
 * <li>read input stream and initialize reader with <code>init()</code></li>
 * <li>close input stream with <code>closeInStream()</code></li>
 * </ul>
 * <i>EXAMPLE</i>
 * <pre>
 * ParameterReader myreader= ...;
 * <br>
 * // prepare input and open input
 * // case #1, from file
 * String[] path= {"C:\my folder\","D:\my other folder\"}
 * String file= "my file.txt";
 * boolean usePath= true;
 * <br>
 * myreader.openInStream(path,file,usePath);
 * <br>
 * // or case #2, from generic input stream
 * java.io.Reader inputStream= ...;
 * <br>
 * myreader.onpenInputStream(inputStream);
 * <br>
 * //or case #3, from text
 * java.lang.String inputString= "This text is to be read";
 * <br>
 * myreader.onpenInputStream(inputString);
 * <br>
 * // ParameterReader is intended to be used only with one of this three methods.<br>
 * // Read stream and initialize internal structure<br>
 * myreader.init();
 * <br>
 * //close input stream
 * myreader.closeInStream();
 * </pre>
 * Methods for opening and closing streams should not throw exceptions. Instead they return
 * completion code.
 *
 * @author igor@scictrl.com
 */
public interface IDataBushReader {

	/** Constant <code>ST_FILE=1</code> */
	int ST_FILE= 1;

	/** Constant <code>ST_URL=2</code> */
	int ST_URL= 2;

	/**
	 * <p>getProperties.</p>
	 *
	 * @return java.util.Properties
	 */
	java.util.Properties getProperties();

	/**
	 * <p>setProperties.</p>
	 *
	 * @return java.util.Properties
	 * @param properties java.util.Properties
	 */
	java.util.Properties setProperties(java.util.Properties properties);

	/**
	 * Close opened input stream. Input stream must be opened with one of
	 * openInStream method.
	 *
	 * @throws org.scictrl.mp.orbitcorrect.DataBushException if any.
	 */
	public void close() throws org.scictrl.mp.orbitcorrect.DataBushException;
	/**
	 * Returns list of instances of DataBush elements created by <code>read()</code> method.
	 *
	 * @return <code>ElementList</code> list of read elements
	 */
	ElementList<AbstractDataBushElement> getList();
	/**
	 * <p>getSourceType.</p>
	 *
	 * @return int
	 */
	int getSourceType();
	/**
	 * Opens input file. Name of the file is specified by
	 * <code>name</code> parameter. Parameter <code>path</code> is an array of
	 * file's paths. Path must end with '/'. Parameter <code>usePath</code>
	 * tells to use path. If <code>name</code> is fully qualified file name, use
	 * <code>false</code> for <code>usePath</code> flag.
	 * Close stream with <code>closeInStream()</code> method.
	 *
	 * @param file a {@link java.io.File} object
	 * @throws org.scictrl.mp.orbitcorrect.DataBushException if any.
	 */
	public void open(File file) throws org.scictrl.mp.orbitcorrect.DataBushException;
	/**
	 * Opens input file. Name of the file is specified by
	 * <code>name</code> parameter. Parameter <code>path</code> is an array of
	 * file's paths. Path must end with '/'. Parameter <code>usePath</code>
	 * tells to use path. If <code>name</code> is fully qualified file name, use
	 * <code>false</code> for <code>usePath</code> flag.
	 * Close stream with <code>closeInStream()</code> method.
	 *
	 * @param url a {@link java.net.URL} object
	 * @throws org.scictrl.mp.orbitcorrect.DataBushException if any.
	 */
	public void open(java.net.URL url) throws org.scictrl.mp.orbitcorrect.DataBushException;
	/**
	 * Reads input and creates DataBush elements. Input must be opened with
	 * one of <code>ParameterReader.openInStream</code> methods. Created elements
	 * are stored and returned in <code>ElementList</code>.
	 *
	 * @return <code>ElementList</code> list of created elements
	 * @throws org.scictrl.mp.orbitcorrect.DataBushException if any.
	 */
	public ElementList<AbstractDataBushElement> read() throws DataBushException;
	/**
	 * Reads input and creates DataBush elements. Input must be opened with
	 * one of <code>ParameterReader.openInStream</code> methods. Created elements
	 * are stored and returned in <code>ElementList</code>.
	 *
	 * @return {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} list of created elements
	 * @param list a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @throws org.scictrl.mp.orbitcorrect.DataBushException if any.
	 */
	public ElementList<AbstractDataBushElement> read(ElementList<AbstractDataBushElement> list) throws DataBushException;
}
