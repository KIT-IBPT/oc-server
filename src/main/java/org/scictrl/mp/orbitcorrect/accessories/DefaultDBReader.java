package org.scictrl.mp.orbitcorrect.accessories;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.scictrl.mp.orbitcorrect.DBElementDescriptor;
import org.scictrl.mp.orbitcorrect.DataBushException;
import org.scictrl.mp.orbitcorrect.IDataBushReader;
import org.scictrl.mp.orbitcorrect.IMultipleFileReader;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
/**
 * This is straightforward implementation of <code>IDataBushReader</code>.
 *
 * DefaultDBReader is designed for reading input written in DataBush specific format.
 * String, written in this format is obtained by calling <code>DataBush.toString()</code> method.
 *
 * Proper read sequence is
 * <ul>
 * <li> open input stream with any of openInStream method</li>
 * <li> read input with <code>DefaultDBReader.read()</code> method. List elements can be returned by read or obtained with <code>DefaultDBReader.getList()</code> method.</li>
 * <li> close input with <code>DefaultDBReader.closeInStream()</code>.</li>
 * </ul>
 *
 * Example of reading is described in <code>ParameterReader</code>. <br>
 * The only difference is, that you should call <code>read()</code> instead <code>init()</code>.
 * Method <code>init()</code> does not implements any functionallity.
 *
 * Result of reading are instances of DataBush elements, with parameters initialized
 * as described in input format. Elements are stored in <code>ElementList</code>.
 * The list is used to initialize DataBush.
 *
 * Examples, how elements are written in DataBush specific format, can be obtained by calling
 * <code>DefaultDBReader.getSamples()</code>.
 *
 * @see AbstractParameterReader
 * @see IDataBushReader
 * @see DataBush
 * @see ElementList
 * @author igor@scictrl.com
 */
public class DefaultDBReader extends AbstractParameterReader implements IDataBushReader {
	private ElementList<AbstractDataBushElement> list;
	/**
	 * Holdes a set of properties.
	*/
	public java.util.Properties readers;
	/** Template. */
	protected Object[][] template = new Object[DBElementDescriptor.PARAMETERS.length][];
	/**
	 * DefaultDBReader constructor, that sets default keys and values to java.util.Properties.
	 */
	public DefaultDBReader() {
		super();
		readers= new java.util.Properties();
		readers.put("DBINPUT",DefaultDBReader.class.getName());
		readers.put("MADINPUT",MADInterpreter.class.getName());
		readers.put("PROPERTIES",PropertiesReader.class.getName());
	}
	/**
	 * Adds new reader to java.util.Properties.
	 *
	 * @param tag java.lang.String
	 * @param reader java.lang.Class
	 */
	public void addReader(String tag, String reader) {
		readers.put(tag,reader);
	}
	/**
	 * {@inheritDoc}
	 *
	 * Close opened input stream.
	 * After stream is closed, new stream may be opened and read.<br>
	 * This is implementation of <code>ParameterReader.closeInStream()</code>.
	 */
	@Override
	public void close() throws DataBushException {
		super.close();
	}
	/**
	 *
	 * @return AbstractDataBushElement
	 * @param d DBElementDescriptor
	 * @param name java.lang.String
	 */
	private AbstractDataBushElement forceElement(DBElementDescriptor d, String name) throws DataBushException {
		AbstractDataBushElement el= list.get(name);
		if (el!=null) return el;
		Class<?> cl;
		try {
			cl= Class.forName(d.getElementClass());//ok its element tag, prepare element
		} catch (Exception e) {throw new DataBushException("Error instantiating Class for"+d.getElementClass()+" in line "+(tok.lineno()+1)+" : "+e.toString());}
		try {
			Object[] par= {name};
			el= ((AbstractDataBushElement) cl.getDeclaredConstructor(DBElementDescriptor.SINGLE_PARAMETER_CLASS).newInstance(par));
		} catch (Exception e) {
			throw new DataBushException("Error instantiating element " + d.getElClass() + ": " + e.toString());
		}
		list.add(el);
		return el;
	}
	/**
	 * {@inheritDoc}
	 *
	 * Returns list of elements created by DefaultDBReader.
	 * Elements are stored in <code>ElementList</code> list.
	 * List is created and filled after call to <code>read()</code> method.
	 */
	@Override
	public ElementList<AbstractDataBushElement> getList() {
		return list;
	}
	/**
	 * Returnes IDataBushReader with given key.To IDataBushReader are propeties also set.
	 *
	 * @return accessories.DataBushReader
	 * @param tag java.lang.String key to reader
	 * @throws java.lang.SecurityException if read fails
	 * @throws java.lang.NoSuchMethodException if read fails
	 * @throws java.lang.reflect.InvocationTargetException  if read fails
	 * @throws java.lang.IllegalArgumentException  if read fails
	 * @throws java.lang.ClassNotFoundException  if read fails
	 * @throws java.lang.InstantiationException  if read fails
	 * @throws java.lang.IllegalAccessException  if read fails
	 * @throws java.lang.ClassCastException  if read fails
	 */
	public IDataBushReader getReader(String tag) throws IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (!readers.containsKey(tag)) throw new IllegalArgumentException("No such reader");
		IDataBushReader r=(IDataBushReader)Class.forName(readers.get(tag).toString()).getDeclaredConstructor().newInstance();
		r.setProperties(getProperties());
		return r;
	}
	/**
	 * Returnes readers.
	 *
	 * @return java.util.Properties
	 */
	public java.util.Properties getReaders() {
		return readers;
	}
	/**
	 * Returns samples of all elements recognized by DataBush and DefaultDBReader.
	 * Elements are stored in <code>ElementList</code> list.
	 *
	 * @return {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} of sample elements
	 */
	public static ElementList<AbstractDataBushElement> getSamples() {
		ElementList<AbstractDataBushElement> ll= new ElementList<>();
		Object[] par;
		try {
			for (int i=0;i<DBElementDescriptor.PARAMETERS.length;i++) {
				par= new Object[DBElementDescriptor.fromInt(i).defaultParameters().length];
				for (int j=0; j<DBElementDescriptor.fromInt(i).defaultParameters().length;j++) par[j]= DBElementDescriptor.fromInt(i).defaultParameters()[j];
				par[0]= "My"+DBElementDescriptor.fromInt(i).getElClass();
	//			ll.add(new DataBushInfo("test1",true,0.0,"1"));
	//			ll.add(new DataBushInfo("test2"));
				ll.add((AbstractDataBushElement)Class.forName(DBElementDescriptor.fromInt(i).getElementClass()).getDeclaredConstructor(DBElementDescriptor.fromInt(i).parametersClasses()).newInstance(par));
			}
		} catch (Throwable e) {System.out.println(e);}
		return ll;
	}
	/**
	 *
	 * @return int
	 */
	private boolean nextValue() throws DataBushException {
		try {
			if (tok.ttype!=StreamTokenizer.TT_EOF)
				if (tok.nextToken()==61)
					if (tok.nextToken()!=StreamTokenizer.TT_EOF)
						return true;
		} catch(IOException e) {throw new DataBushException("Expected \"=<some value>\" in line "+tok.lineno()+" but IOException was thrown: "+e.getMessage());}
		return false;
	}
	/**
	 * <p>open.</p>
	 *
	 * @param reader a {@link java.io.Reader} object
	 * @throws org.scictrl.mp.orbitcorrect.DataBushException if any.
	 */
	public void open(Reader reader) throws org.scictrl.mp.orbitcorrect.DataBushException {
		try {
			tok= new FileTokenizer(reader,uRL=new java.net.URL("file:///"));
			st= IDataBushReader.ST_FILE;
		} catch (IOException e) {throw new org.scictrl.mp.orbitcorrect.DataBushException("Error opening URL "+uRL.toString()+" : "+e.toString());}
	}
	/**
	 * Reads input and creates DataBush elements. Input must be opened with
	 * one of <code>ParameterReader.openInStream</code> methods. Created instances of DataBush elements
	 * are stored and returned in <code>ElementList</code>.
	 * Call read only once per new opened stream.
	 * @see ParameterReader#openInStream
	 * @see ElementList
	 * @param tag element tag name
	 * @throws DataBushException if fails
	 * @throws IOException if fails
	 * @throws java.text.ParseException if fails
	 */
	private void parseElementTag(String tag) throws DataBushException, IOException, java.text.ParseException {
		int i;
		boolean isTemplate = false;
		Object[] par;
		//Field fi = null;
		DBElementDescriptor el;
		if (tok.sval.equals(DBElementDescriptor.TEMPLATE_TAG))
			isTemplate = true;
		if (nextValue()) { //is value succesfully returned
			try {
				el = DBElementDescriptor.fromElementClassTag(tok.sval);
			} catch (IllegalArgumentException e) {
				throw new DataBushException("Unrekognized element class name: " + tok.toString()); //it is not known element tag
			}
			tok.nextToken(); //this shuld be property specifyer
			par = new Object[el.parametersCount()];
			while (true) { //am i inside tag
				if (tok.ttype == StreamTokenizer.TT_EOF)
					throw new DataBushException("Unespected end of file before end of element tag for " + el.getElClass() + " in line " + (tok.lineno() + 1) + "\n");
				if (tok.ttype == '>') { //check for end of tag
					if (isTemplate) {
						if (template[el.value()]==null) template[el.value()] = par;
						else for (int k=0; k<par.length; k++) if (par[k]!=null) template[el.value()][k]= par[k];
					} else {
						if (template[el.value()]!=null) for (int k=0; k<par.length; k++) if (par[k]==null) par[k]= template[el.value()][k];
						forceElement(el,par[AbstractDataBushElement.PR_ELEMENT_NAME].toString()).setWith(par);
					}
					break;
				}
				if (tok.ttype != FileTokenizer.TT_WORD)
					throw new DataBushException("Expected parameter tag, but " + tok.toString() + " was found");
				for (i = 0; i < par.length; i++) { //ok now check for properties
					if (tok.sval.equals(el.getParameterTag(i))) { //is it this property
						if (!nextValue())
							throw new DataBushException("Expected \"=<some value>\" after " + el.getParameterTag(i) + " specified but " + tok.toString() + " was found");
						//try {
						if (tok.ttype == 34)
							par[i] = new String(tok.sval);
						else
							if (tok.ttype == StreamTokenizer.TT_NUMBER)
								par[i] = Double.valueOf(tok.nval);
							else
								if (tok.ttype == StreamTokenizer.TT_WORD)
									par[i] = Boolean.valueOf(tok.sval);
								//} catch (Exception e) {throw new DataBushException("Error instantiating e.getMessage());}
						break;
					}
				}
				if (i == par.length)
					throw new DataBushException("Property tag \"" + tok.sval + "\" in line " + (tok.lineno() + 1) + " is not recognized");
				tok.nextToken();
			}
		}
	}
	private void parseReaderTag(String r) throws DataBushException, IOException, java.text.ParseException {
		IDataBushReader reader;
		try {
			reader= (IDataBushReader)Class.forName(readers.get(r).toString()).getDeclaredConstructor().newInstance();
			reader.setProperties(getProperties());
		} catch (Exception e) {throw new DataBushException("Failed to create IDataBushReader \""+r+"\" (\""+readers.get(r)+"\"): "+e.toString());}

		boolean multi=false;
		List<URL> l=null;
		if (reader instanceof IMultipleFileReader) {
			multi=true;
			l= new ArrayList<>();
		}
		while(tok.nextToken()!=StreamTokenizer.TT_EOF&&tok.ttype!='>') {
			if (tok.ttype=='"') {
				if (multi) {
					l.add(new java.net.URL(tok.getURL(),tok.sval));
				} else {
					reader.open(new java.net.URL(tok.getURL(),tok.sval));
					try {
						reader.read(list);
					} catch (DataBushException e) {
						throw new DataBushException("Error reading "+r+" file \""+tok.sval+"\" from line "+(tok.lineno()+1)+": "+e.getMessage());
					}
					reader.close();//) throw new DataBushException("Error closing DBINPUT file \""+tok.sval+"\"");
				}
			} else if (tok.ttype!=StreamTokenizer.TT_EOL) throw new DataBushException("Expected file name in brackets \", but "+tok.toString()+" was found");
		}
		if (multi) {
			java.net.URL[] u= new java.net.URL[l.size()];
			for (int i=0; i<u.length; u[i]= l.get(i++));
			((IMultipleFileReader)reader).open(u);
			try {
			reader.read(list);
			} catch (DataBushException e) {
				throw new DataBushException("Error reading "+r+" file \""+tok.sval+"\" from line "+(tok.lineno()+1)+": "+e.getMessage());
			}
			reader.close();//) throw new DataBushException("Error closing DBINPUT file \""+tok.sval+"\"");
		}
	}
	/**
	 * {@inheritDoc}
	 *
	 * Reads input and creates DataBush elements. Input must be opened with
	 * one of <code>ParameterReader.openInStream</code> methods. Created instances of DataBush elements
	 * are stored and returned in <code>ElementList</code>.
	 * Call read only once per new opened stream.
	 */
	@Override
	public ElementList<AbstractDataBushElement> read() throws DataBushException {
		return read(new ElementList<>());
	}
	/**
	 * {@inheritDoc}
	 *
	 * Reads input and creates DataBush elements. Input must be opened with
	 * one of <code>ParameterReader.openInStream</code> methods. Created instances of DataBush elements
	 * are stored and returned in <code>ElementList</code>.
	 * Call read only once per new opened stream.
	 * @see ElementList
	 */
	@Override
	public ElementList<AbstractDataBushElement> read(ElementList<AbstractDataBushElement> l) throws DataBushException {
		debugPrintln("Reading "+tok.getURL().toString());
		list= l;
	//	tok.lowerCaseMode(true);
		tok.lowerCaseMode(false);
		tok.slashSlashComments(true);
		tok.slashStarComments(true);
		tok.eolIsSignificant(false);
		tok.parseNumbers();
		tok.quoteChar('"');
		tok.ordinaryChar('<');
		tok.ordinaryChar('>');
		tok.ordinaryChar('=');

		try {
			while(tok.nextToken()!=StreamTokenizer.TT_EOF) {
				if (tok.ttype=='<') {// is it word
						if (tok.nextToken()==StreamTokenizer.TT_WORD) { //is nex one word
							if (tok.sval.equals(DBElementDescriptor.ELEMENT_TAG)||tok.sval.equals(DBElementDescriptor.TEMPLATE_TAG)) { //is it element tag
								parseElementTag(tok.sval);
							} else {
								Enumeration<Object> enumeration= readers.keys();
								while (enumeration.hasMoreElements()) {
									if(tok.sval.equals(enumeration.nextElement())) {
										parseReaderTag(tok.sval);
										break;
									}
								}
							}
						}
				}
			//	if (st.ttype==StreamTokenizer.TT_EOF) break;
			}
		} catch (IOException e) {
			throw new DataBushException("Reading line "+(tok.lineno()+1)+" exception was thrown:"+e.getMessage());
		}	catch (java.text.ParseException e) {
			throw new DataBushException("Prereader complained in line "+(tok.lineno()+1)+" : "+e.getMessage());
		}

		return list;
	}
}
