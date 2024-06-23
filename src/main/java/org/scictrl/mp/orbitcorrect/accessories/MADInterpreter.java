package org.scictrl.mp.orbitcorrect.accessories;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.scictrl.mp.orbitcorrect.DBElementDescriptor;
import org.scictrl.mp.orbitcorrect.DataBushException;
import org.scictrl.mp.orbitcorrect.IDataBushReader;
import org.scictrl.mp.orbitcorrect.IMultipleFileReader;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractBending;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractTransferElement;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.Cavity;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
import org.scictrl.mp.orbitcorrect.model.optics.HorCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.Kicker;
import org.scictrl.mp.orbitcorrect.model.optics.Marker;
import org.scictrl.mp.orbitcorrect.model.optics.Quadrupole;
import org.scictrl.mp.orbitcorrect.model.optics.RBending;
import org.scictrl.mp.orbitcorrect.model.optics.SBending;
import org.scictrl.mp.orbitcorrect.model.optics.Sextupole;
import org.scictrl.mp.orbitcorrect.model.optics.VerCorrector;
/**
 * MADInterpreter has the same functionality as DefaultDBReader.
 * In addition to reading DataBush specific input can also read MAD input.
 * Because DataBush is independent of MAD, or any other accelerator physics calculation program,
 * can't write in MAD specific format. Gadgets like MADInterpreter perform interpretations to other envirements.<br>
 * MADInterpreter also provides storing DataBush data in MAD format code.
 * <br><br>
 * MAD format does not include enough parameters for DataBush elements to initialize
 * functional DataBush. I order to store all information for DataBush, part of data
 * must be stored in DataBush specific format.
 * <br><br>
 * Input for MADInterpreter is assembled from two parts, MAD specific input and
 * DataBush (abbreviated DB) specific input. Both kind of input must be stored in separate files.
 * <br><br>
 * Input for MADInterpreter points to MAD and DB files.
 * This is sample of MADInterpreter input file:
 *
 * <pre>
 * &lt;DBINPUT
 * "C:/DataBush/DBInput.txt"
 * >
 * &lt;MADINPUT
 * "C:/DataBush/MAD/MADInput 1.txt",
 * "C:/DataBush/MAD/MADInput 2.txt"
 * >
 * </pre>
 *
 * DataBush and MAD part is enclosed in-between brackets '&lt;' and '&gt;'.
 * Code name "DBINPUT" and "MADINPUT" tells in which format style are input files inside brackets written.
 * Separate files are closed inside '"'.<br>
 * <br>
 * Files inside "DBINPUT" brackets are passed to DefaultDBReader.<br>
 * <br>
 * Files Inside "MADINPUT" brackets are parsed by MADInterpreter.
 * MADInterpreter writes and reads same format  as MAD, only with few restrictions.
 * <ul>
 * <li>uses MAD comment sign '!', C  and C++ comments</li>
 * <li>recognize only code for definitions of variables, elements and element's positions</li>
 * <li>does not support new element type defining. Only following MAD types may be used:
 * MARKER, RFCAVITY, BPMH, BPMV, HKICK, VKICK, ECOLLIMATOR, RBEND, SBEND, QUADRUPOLE, SEXTUPOLE.
 * Where BPMH and BPMV are: "<code>BPMH: HMONITOR,TYPE=MM</code>" and "<code>BPMV: VMONITOR,TYPE=MM</code>"</li>
 * <li>all elements, except MARKER, must be described in two parts, in definition and position part.
 * In definition part element must be defined with default MAD type. <br>
 * In second part position of element must be defined with "INSTALL" expression.
 * The order of parts is not important to MADInterpreter.</li>
 * <li>MARKER is defined with position expression <code>"mymarker :MARKER, at=13.13"</code>
 * <li>variables must be defined, before they are used</li>
 * <li>MADInterpreter supports multiplication '*' and derivation '/' in numerical expressions</li>
 * <li>code word "TWOPI" is replaced with <code>2*Math.PI</code></li>
 * </ul>
 *
 * DB specific input can store all information necessary to initialize DataBush, and
 * MAD specific only part of it. It is possible, that same property is defined in this two places,
 * even with different value. In this case last value read always overwrites any earlier value applied to same property.<br>
 * <br>
 * <em>IMPORTANT!</em> Structure of data, stored in DataBush so differs from organization of data in MAD code,
 * that can produce conflicts, if few simple rules are neglected.<br>
 * Cause of conflict is:
 * <ul>
 * <li>DB format is case sensitive, MAD isn't. This is critical for recognition of element names.
 * They must be unique, or DataBush won't initialize.<br>
 * <em>possible conflict:</em> If same element name, but in different, case is used in DB and MAD input, MADInterpreter recognizes it as two different elements, instead of applying all properties to same one.</li>
 * <li>transformation between DB and MAD elements is not bijective.
 * This is transformation table. On left are element types, as used in MAD input.
 * On right are class names of DataBush elements created by reader. The same names are used to declare element type in DB input format.
 * <blockquote>
 * <table>
 * <tr><td>MAD element </td><td>DB element</td></tr>
 * <tr><td>MARKER</td><td>Marker</td></tr>
 * <tr><td>RFCAVITY</td><td>Cavity</td></tr>
 * <tr><td>BPMH,BPMV</td><td>BPMonitor</td></tr>
 * <tr><td>HKICK</td><td>HorCorrector,Kicker</td></tr>
 * <tr><td>VKICK</td><td>VerCorrector</td></tr>
 * <tr><td>ECOLLIMATOR</td><td>Apperature</td></tr>
 * <tr><td>RBEND</td><td>RBending</td></tr>
 * <tr><td>SBEND</td><td>SBending</td></tr>
 * <tr><td>QUADRUPOLE</td><td>Quadrupole</td></tr>
 * <tr><td>SEXTUPOLE</td><td>Sextupole</td></tr>
 * <caption>Element mapping</caption>
 * </table>
 * </blockquote>
 * Element with same name and type, defined in MAD and DB formated input, is recognized as same element.<br>
 * <br>
 * Beam position monitors and kickers are handled by MADInterpreter in special way:
 * <ul>
 * <li>MAD elements defined with BPMH type are recognized as BPMonitor. BPMV elements are ignored.
 * String representation of DataBush in MAD format produces declaration of both types.</li>
 * <li>elements of HKICK type in MAD input are automatically recognized as HorCorrector elements.
 * If you want to use HKICK element in MAD format as Kicker, a Kicker must be declared with same name in DB input before MAD declaration.</li>
 * </ul>
 * </li>
 * </ul>
 * MADInterpreter is also able to store DataBush information in MAD format.
 * Use <code>saveToFiles</code> and <code>saveToStrings</code> methods.
 *
 * @see AbstractParameterReader
 * @see IDataBushReader
 * @see DefaultDBReader
 * @see DataBush
 * @author igor@scictrl.com
 */
public class MADInterpreter extends AbstractParameterReader implements IDataBushReader, IMultipleFileReader {
	private ElementList<AbstractDataBushElement> list;
	private Object[] files;

/**
 * HashMap containing constants read from MAD input files. HashMap
 * object is created, when <code>read</code> method is called. Constant
 * definition in MAD looks like this
 * <code>constant_name=some_double_value</code>.
 * @see #getConstant
 * @see #addConstant
 */
	protected Map<String, Double> consts;
/**
 * Index of DB specific fromat, stored in HEADER and <code>String[]</code> array, returned by <code>saveToFiles</code> and <code>saveToStrings</code> methods.
 * @see #HEADER
 * @see #saveToFiles
 * @see #saveToStrings
 */
	public static final int DB=0;
/**
 * Index of MARKER elements in MAD formated string, stored in HEADER and <code>String[]</code> array, returned by <code>saveToFiles</code> and <code>saveToStrings</code> methods.
 * @see #HEADER
 * @see #saveToFiles
 * @see #saveToStrings
 */
	public static final int MARK=1;
/**
 * Index of BPMV and BPMH elements in MAD formated string, stored in HEADER and <code>String[]</code> array, returned by <code>saveToFiles</code> and <code>saveToStrings</code> methods.
 * @see #HEADER
 * @see #saveToFiles
 * @see #saveToStrings
 */
	public static final int BPM=2;
/**
 * Index of RFCAVITY elements in MAD formated string, stored in HEADER and <code>String[]</code> array, returned by <code>saveToFiles</code> and <code>saveToStrings</code> methods.
 * @see #HEADER
 * @see #saveToFiles
 * @see #saveToStrings
 */
	public static final int CAV=4;
/**
 * Index of SEXTUPOLE elements in MAD formated string, stored in HEADER and <code>String[]</code> array, returned by <code>saveToFiles</code> and <code>saveToStrings</code> methods.
 * @see #HEADER
 * @see #saveToFiles
 * @see #saveToStrings
 */
	public static final int SE=6;
/**
 * Index of QUADRUPOLE elements in MAD formated string, stored in HEADER and <code>String[]</code> array, returned by <code>saveToFiles</code> and <code>saveToStrings</code> methods.
 * @see #HEADER
 * @see #saveToFiles
 * @see #saveToStrings
 */
	public static final int QU=8;
/**
 * Index of RBENDING and SBENDING elements in MAD formated string, stored in HEADER and <code>String[]</code> array, returned by <code>saveToFiles</code> and <code>saveToStrings</code> methods.
 * @see #HEADER
 * @see #saveToFiles
 * @see #saveToStrings
 */
	public static final int BE=10;
/**
 * Index of VerCorrector and HorCorrector elements in MAD formated string, stored in HEADER and <code>String[]</code> array, returned by <code>saveToFiles</code> and <code>saveToStrings</code> methods.
 * @see #HEADER
 * @see #saveToFiles
 * @see #saveToStrings
 */
	public static final int COR=12;
/**
 * Index of Kicker elements in MAD formated string, stored in HEADER and <code>String[]</code> array, returned by <code>saveToFiles</code> and <code>saveToStrings</code> methods.
 * @see #HEADER
 * @see #saveToFiles
 * @see #saveToStrings
 */
	public static final int KICK=14;
/**
 * <code>String[]</code> array of MAD commented string, describing contents of <code>String[]</code> array, returned by <code>saveToFiles</code> and <code>saveToStrings</code> methods.
 * Correspondent description in header (<code>HEADER[index]</code>) and contents (<code>saveToStrings()[index]</code>)  mach in index.
 * @see #saveToFiles
 * @see #saveToStrings
 */
	public static final String[] HEADER={
		"!DataBush specific input\n\r",
		"!Marker position\n\r",
		"!BPM definition\n\r",
		"!BPM position\n\r",
		"!Cavity definition\n\r",
		"!Cavity position\n\r",
		"!Sextupole definition\n\r",
		"!Sextupole position\n\r",
		"!Quadrupole definition\n\r",
		"!Quadrupole position\n\r",
		"!Bending definition\n\r",
		"!Bending position\n\r",
		"!Corrector definition\n\r",
		"!Corrector position\n\r",
		"!Kicker definition\n\r",
		"!Kicker position\n\r"};
	private static String
	NAME_USED= "Name \"{0}\" referenced by {1} defining expression in line {2} is allready used for non {1} element";

	interface SimpleIterator {
		boolean hasNext();
		AbstractDataBushElement next();
	}
//	private boolean invertSign=true;
	/**
	 * Default MADInterpreter constructor.
	 */
	public MADInterpreter() {
		super();
	}
	/**
	 * Puts new key <code>name</code> with value <code>value</code> to
	 * constant's HashMap.
	 *
	 * @param name java.lang.String constant's name
	 * @param value double constant's value
	 * @see consts
	 */
	protected void addConstant(String name, double value) {
		consts.put(name,Double.valueOf(value));
	}
	/**
	 *
	 * @return java.lang.String
	 * @param name java.lang.String
	 * @param pos double
	 */
	private static String formatInstall(String name, double pos) {
		String st="INSTALL, ELEMENT="+name+", ";
		while (st.length()<28) st+=" ";
		st+="AT="+pos+"\r\n";
		return st;
	}
	/**
	 * Returnes value of constant with key <code>name</code>. If key is not found,
	 * return value is 0.0.
	 *
	 * @return double value of <code>name</code> key
	 * @param name java.lang.String constant name
	 * @see consts
	 */
	protected double getConstant(String name) {
		if (consts.containsKey(name)) return consts.get(name).doubleValue();
		return 0.0;
	}
	/**
	 *
	 * @return DataBushInterface.DataBushElement
	 * @param s java.lang.String
	 * @throws DataBushException if fails
	 */
	private AbstractDataBushElement getEl(String s) throws DataBushException {
		AbstractDataBushElement el=null;
		String s1;
		el= list.get(s);
		if (el==null) {
			if (s.endsWith("h")||s.endsWith("v")) {
				s1= s.substring(0,s.length()-1);
				el= list.get(s1);
				if (el==null) throw new DataBushException("element \""+s1+"\" not found");
			} else throw new DataBushException("element \""+s+"\" not found");
		}
		return el;
	}
	/**
	 *
	 * @return DataBushInterface.DataBushElement
	 * @param s java.lang.String
	 * @throws DataBushException if fails
	 */
	private AbstractDataBushElement getEl(String s, int type) throws DataBushException {
		AbstractDataBushElement el=null;
		el= list.get(s);
		if (el==null) {
			try {
				Object[] par= new String[1];
				par[0]= s;
				el=((AbstractDataBushElement)Class.forName(DBElementDescriptor.fromInt(type).getElementClass()).getDeclaredConstructor(DBElementDescriptor.SINGLE_PARAMETER_CLASS).newInstance(par));
			} catch (Throwable e) {throw new DataBushException("error inst \""+s+"\": "+e.toString());}
			list.add(el);
		}
		return el;
	}
	/**
	 * {@inheritDoc}
	 *
	 * Returns elements created by MADInterpreter.
	 * Elements are stored in <code>ElementList</code> list.
	 * List is created and filled after call to <code>read()</code> method.
	 */
	@Override
	public ElementList<AbstractDataBushElement> getList() {
		return list;
	}
	/**
	 *
	 * @return java.lang.String
	 * @param db DataBushInterface.DataBush
	 */
	private static String[] makeString(SimpleIterator it) {
		String[] st= new String[16];
		String s="";
		for (int i=1; i<16; i++) st[i]="";

		AbstractOpticalElement oel;
		AbstractDataBushElement el;
		while (it.hasNext()) {
			el= it.next();
			if (el instanceof AbstractOpticalElement) oel= (AbstractOpticalElement)el;
			else continue;
			if (oel instanceof Marker) {

				s= oel.getName()+" ";
				while (s.length()<10) s+=" ";
				st[MARK]+=s+": MARKER, AT="+oel.getPosition()+"\r\n";

			} else if (oel instanceof BPMonitor) {

				s= oel.getName()+" ";
				while (s.length()<10) s+=" ";
				s+=": BPMH; "+oel.getName();
				while (s.length()<26) s+=" ";
				st[BPM]+=s+": BPMV\r\n";
				st[BPM+1]+=formatInstall(oel.getName(),oel.getPosition());

			} else if (oel instanceof Cavity) {

				s= oel.getName();
				while (s.length()<10) s+=" ";
				st[CAV]+=s+": RFCAVITY;\r\n";
				st[CAV+1]+=formatInstall(oel.getName(),oel.getPosition());

			} else if (oel instanceof Sextupole) {

				s= oel.getName()+" ";
				while (s.length()<10) s+=" ";
				s+= ": SEXTUPOLE, L="+((AbstractTransferElement)oel).getLength()+",";
				while (s.length()<45) s+=" ";
				st[SE]+=s+"K2="+((Quadrupole)oel).getQuadrupoleStrength()+"\r\n";
				st[SE+1]+=formatInstall(oel.getName(),oel.getPosition());

			} else if (oel instanceof Quadrupole) {

				s= oel.getName()+" ";
				while (s.length()<10) s+=" ";
				s+= ": QUADRUPOLE, L="+((AbstractTransferElement)oel).getLength()+",";
				while (s.length()<46) s+=" ";
				st[QU]+=s+"K1="+((Quadrupole)oel).getQuadrupoleStrength()+"\r\n";
				st[QU+1]+=formatInstall(oel.getName(),oel.getPosition());

			} else if (oel instanceof SBending) {

				s= oel.getName()+" ";
				while (s.length()<10) s+=" ";
				s+= ": SBEND, L="+((AbstractTransferElement)oel).getLength()+",";
				while (s.length()<41) s+=" ";
				st[BE]+=s+"ANGLE="+(((AbstractBending)oel).getLength()/((AbstractBending)oel).getRadius())+"\r\n";
				st[BE+1]+=formatInstall(oel.getName(),oel.getPosition());

			} else if (oel instanceof RBending) {

				s= oel.getName()+" ";
				while (s.length()<10) s+=" ";
				s+= ": RBEND, L="+((AbstractTransferElement)oel).getLength()+",";
				while (s.length()<41) s+=" ";
				st[BE]+=s+"ANGLE="+(((AbstractBending)oel).getLength()/((AbstractBending)oel).getRadius())+"\r\n";
				st[BE+1]+=formatInstall(oel.getName(),oel.getPosition());

			} else if (oel instanceof VerCorrector) {

				s= oel.getName()+" ";
				while (s.length()<10) s+=" ";
				s+= ": VKICK, L="+((AbstractTransferElement)oel).getLength()+",";
				while (s.length()<41) s+=" ";
				st[COR]+=s+"KICK="+((AbstractCorrector)oel).getAngle()+"\r\n";
				st[COR+1]+=formatInstall(oel.getName(),oel.getPosition());

			} else if (oel instanceof HorCorrector) {

				s= oel.getName()+" ";
				while (s.length()<10) s+=" ";
				s+= ": HKICK, L="+((AbstractTransferElement)oel).getLength()+",";
				while (s.length()<41) s+=" ";
				st[COR]+=s+"KICK="+((AbstractCorrector)oel).getAngle()+"\r\n";
				st[COR+1]+=formatInstall(oel.getName(),oel.getPosition());

			} else if (oel instanceof Kicker) {

				s= oel.getName()+" ";
				while (s.length()<10) s+=" ";
				s+= ": HKICK, L="+((AbstractTransferElement)oel).getLength()+",";
				while (s.length()<41) s+=" ";
				st[COR]+=s+"KICK="+((AbstractCorrector)oel).getAngle()+"\r\n";
				st[COR+1]+=formatInstall(oel.getName(),oel.getPosition());

			}
		}
		return st;
	}
	/**
	 *
	 * @return java.io.StreamTokenizer
	 * @param r java.io.Reader
	 */
	private FileTokenizer newFileTokenizer(java.net.URL u) throws DataBushException {
		try {
			FileTokenizer t= new FileTokenizer(u);
	//	t.lowerCaseMode(true);
		t.slashSlashComments(true);
		t.slashStarComments(true);
		t.eolIsSignificant(true);
		t.parseNumbers();
		t.quoteChar('"');
		t.ordinaryChar('<');
		t.ordinaryChar('>');
		t.ordinaryChar('=');
		t.ordinaryChar(':');
		t.ordinaryChar(',');
		t.ordinaryChar('/');
		t.ordinaryChar('&');
		t.commentChar('!');
		t.wordChars('_','_');
		return t;
		} catch(IOException e) {throw new DataBushException("Error opening file "+u.toString()+": "+e.toString());}
	}
	/**
	 * {@inheritDoc}
	 *
	 * Openes files.
	 */
	@Override
	public void open(File[] files) throws org.scictrl.mp.orbitcorrect.DataBushException {
		this.files= files;
		st= ST_FILE;
	}
	/**
	 * {@inheritDoc}
	 *
	 * Openes URL.
	 */
	@Override
	public void open(java.net.URL[] url) throws org.scictrl.mp.orbitcorrect.DataBushException {
		files= url;
		st= ST_URL;
	}
	/**
	 *
	 * @return DataBushInterface.ElementList
	 */
	private void parseMADStream(FileTokenizer ft) throws DataBushException {
		debugPrintln("MADInterpreter ["+ft.getURL().toString()+"]");

		AbstractOpticalElement oel;
		AbstractDataBushElement el;
		//Object ob;
		String name="";
		//double d= 0.0;

		try{
			ft.nextToken();
			while(ft.ttype!=StreamTokenizer.TT_EOF) {
				if (ft.ttype==StreamTokenizer.TT_WORD) {
					if (ft.sval.toLowerCase().equals("install")) {
						if ((ft.nextToken()==',')&&(ft.nextToken()==StreamTokenizer.TT_WORD)&&(ft.sval.equals("element"))&&(ft.nextToken()=='=')&&(ft.nextToken()==StreamTokenizer.TT_WORD)){
							if (((el=getEl(ft.sval)) instanceof AbstractOpticalElement)) {
								oel= (AbstractOpticalElement)el;
								if ((ft.nextToken()==',')&&(ft.nextToken()==StreamTokenizer.TT_WORD)&&(ft.sval.equals("at"))){
									oel.setRelPosition(readValue(ft));
									if (ft.nextToken()==',') {
										if ((ft.nextToken()==StreamTokenizer.TT_WORD)&&(ft.sval.equals("from"))&&(ft.nextToken()=='=')&&(ft.nextToken()==StreamTokenizer.TT_WORD)) {
											oel.setRelFrom(ft.sval);
										} else throw new DataBushException("\"INSTALL\" expression sintax error, bad relative position reference in line "+ft.lineno());
									} else oel.setPosition(oel.getRelPosition());
								} else throw new DataBushException("\"INSTALL\" expression sintax error, bad position expresion in line "+ft.lineno());
							} else {
								Object[] args= {name,"AbstractOpticalElement",Integer.toString(ft.lineno())};
								throw new DataBushException(java.text.MessageFormat.format(NAME_USED,args));
							}
						} else throw new DataBushException("\"INSTALL\" expression sintax error in line "+ft.lineno());
					} else {
						name= ft.sval;
						if (ft.nextToken()=='=') {
								ft.pushBack();
								addConstant(name,readValue(ft));
						} else if (ft.ttype==':') {
							if (ft.nextToken()=='=') {
								ft.pushBack();
								addConstant(name,readValue(ft));
							} else if (ft.ttype==StreamTokenizer.TT_WORD) {
								if (ft.sval.toLowerCase().equals("marker")) {
									//this is marker definition
									if ((ft.nextToken()==',')&&(ft.nextToken()==StreamTokenizer.TT_WORD)&&(ft.sval.toLowerCase().equals("at"))) {
										if (((el=getEl(name,DBElementDescriptor.EL_MARKER)) instanceof Marker)) {
											oel= (AbstractOpticalElement)el;
											oel.setPosition(readValue(ft));
										} else {
											Object[] args= {name,"Marker",Integer.toString(ft.lineno())};
											throw new DataBushException(java.text.MessageFormat.format(NAME_USED,args));
										}
									} else throw new DataBushException("\"INSTALL\" expression sintax error in line "+ft.lineno());

								} else if (ft.sval.toLowerCase().equals("bpmh")||ft.sval.toLowerCase().equals("bpmv")) {
									//this is BPM definition
									if (name.toLowerCase().endsWith("h")||name.toLowerCase().endsWith("v")) name= name.substring(0,name.length()-1);
									if (getEl(name,DBElementDescriptor.EL_BPM) instanceof BPMonitor);
									else {
										Object[] args= {name,"BPMonitor",Integer.toString(ft.lineno())};
										throw new DataBushException(java.text.MessageFormat.format(NAME_USED,args));
									}

								} else if (ft.sval.toLowerCase().equals("sbend")) {
									//this is bending definition
									if ((el=getEl(name,DBElementDescriptor.EL_SBENDING)) instanceof SBending);
									else {
										Object[] args= {name,"SBending",Integer.toString(ft.lineno())};
										throw new DataBushException(java.text.MessageFormat.format(NAME_USED,args));
									}
									ft.nextToken();
									while ((ft.ttype!=StreamTokenizer.TT_EOF)&&(ft.ttype!=StreamTokenizer.TT_EOL)&&(ft.ttype!=';')) {//till end seearch for parameters
										if (ft.ttype==StreamTokenizer.TT_WORD) { //parameter found
											if (ft.sval.toLowerCase().equals("l")) ((AbstractTransferElement)el).setLength(readValue(ft));
											else if (ft.sval.toLowerCase().equals("angle")) ((SBending)el).setRadius(((SBending)el).getLength()/readValue(ft));
											else throw new DataBushException("Unrecognized parameter expression \""+ft.sval+"\" for SBEND in line "+ft.lineno());
										}
										else if (ft.ttype=='&') while ((ft.ttype!=StreamTokenizer.TT_EOF)&&(ft.ttype!=StreamTokenizer.TT_EOL)) ft.nextToken();
										ft.nextToken();
									}

								} else if (ft.sval.toLowerCase().equals("rbend")) {
									//this is bending definition
									if ((el=getEl(name,DBElementDescriptor.EL_RBENDING)) instanceof RBending);
									else {
										Object[] args= {name,"RBending",Integer.toString(ft.lineno())};
										throw new DataBushException(java.text.MessageFormat.format(NAME_USED,args));
									}
									ft.nextToken();
									while ((ft.ttype!=StreamTokenizer.TT_EOF)&&(ft.ttype!=StreamTokenizer.TT_EOL)&&(ft.ttype!=';')) {//till end seearch for parameters
										if (ft.ttype==StreamTokenizer.TT_WORD) { //parameter found
											if (ft.sval.toLowerCase().equals("l")) ((AbstractTransferElement)el).setLength(readValue(ft));
											else if (ft.sval.toLowerCase().equals("angle")) ((RBending)el).setRadius(((RBending)el).getLength()/readValue(ft));
											else throw new DataBushException("Unrecognized parameter expression \""+ft.sval+"\" for RBEND in line "+ft.lineno());
										}
										else if (ft.ttype=='&') while ((ft.ttype!=StreamTokenizer.TT_EOF)&&(ft.ttype!=StreamTokenizer.TT_EOL)) ft.nextToken();
										ft.nextToken();
									}

								} else if (ft.sval.toLowerCase().equals("quadrupole")) {
									//this is quadrupole definition
									if ((el=getEl(name,DBElementDescriptor.EL_QUADRUPOLE)) instanceof Quadrupole);
									else {
										Object[] args= {name,"Quadrupole",Integer.toString(ft.lineno())};
										throw new DataBushException(java.text.MessageFormat.format(NAME_USED,args));
									}
									ft.nextToken();
									while ((ft.ttype!=StreamTokenizer.TT_EOF)&&(ft.ttype!=StreamTokenizer.TT_EOL)&&(ft.ttype!=';')) {//till end seearch for parameters
										if (ft.ttype==StreamTokenizer.TT_WORD) { //parameter found
											if (ft.sval.toLowerCase().equals("l")) {
												((AbstractTransferElement)el).setLength(readValue(ft));
											}
											else if (ft.sval.toLowerCase().equals("k1")) ((Quadrupole)el).setQuadrupoleStrength(readValue(ft));
											else throw new DataBushException("Unrecognized parameter expression \""+ft.sval+"\" for QUADRUPOLE in line "+ft.lineno());
										}
										else if (ft.ttype=='&') while ((ft.ttype!=StreamTokenizer.TT_EOF)&&(ft.ttype!=StreamTokenizer.TT_EOL)) ft.nextToken();
										ft.nextToken();
									}

								} else if (ft.sval.toLowerCase().equals("sextupole")) {
									//this is sextupole definition
									if ((el=getEl(name,DBElementDescriptor.EL_SEXTUPOLE)) instanceof Sextupole);
									else {
										Object[] args= {name,"Sextupole",Integer.toString(ft.lineno())};
										throw new DataBushException(java.text.MessageFormat.format(NAME_USED,args));
									}
									ft.nextToken();
									while ((ft.ttype!=StreamTokenizer.TT_EOF)&&(ft.ttype!=StreamTokenizer.TT_EOL)&&(ft.ttype!=';')) {//till end search for parameters
										if (ft.ttype==StreamTokenizer.TT_WORD) { //parameter found
											if (ft.sval.toLowerCase().equals("l")) ((AbstractTransferElement)el).setLength(readValue(ft));
											else if (ft.sval.toLowerCase().equals("k2")) ((Sextupole)el).setSextupoleStrength(readValue(ft));
											else throw new DataBushException("Unrecognized parameter expression \""+ft.sval+"\" for SEXTUPOLE in line "+ft.lineno());
										}
										else if (ft.ttype=='&') while ((ft.ttype!=StreamTokenizer.TT_EOF)&&(ft.ttype!=StreamTokenizer.TT_EOL)) ft.nextToken();
										ft.nextToken();
									}

								} else if (ft.sval.toLowerCase().equals("hkick")) {
									//this is HorCorrector definition
									if ((el=getEl(name,DBElementDescriptor.EL_HOR_CORRECTOR)) instanceof HorCorrector);
									else {
										Object[] args= {name,"HorCorrector",Integer.toString(ft.lineno())};
										throw new DataBushException(java.text.MessageFormat.format(NAME_USED,args));
									}
									ft.nextToken();
									while ((ft.ttype!=StreamTokenizer.TT_EOF)&&(ft.ttype!=StreamTokenizer.TT_EOL)&&(ft.ttype!=';')) {//till end search for parameters
										if (ft.ttype==StreamTokenizer.TT_WORD) { //parameter found
											if (ft.sval.toLowerCase().equals("l")) ((AbstractCorrector)el).setLength(readValue(ft));
											else if (ft.sval.toLowerCase().equals("kick")) ((AbstractCorrector)el).setAngle(readValue(ft));
											else throw new DataBushException("Unrecognized parameter expression \""+ft.sval+"\" for HKICK in line "+ft.lineno());
										}
										else if (ft.ttype=='&') while ((ft.ttype!=StreamTokenizer.TT_EOF)&&(ft.ttype!=StreamTokenizer.TT_EOL)) ft.nextToken();
										ft.nextToken();
									}

								} else if (ft.sval.toLowerCase().equals("vkick")) {
									//this is HorCorrector definition
									if (((el=getEl(name,DBElementDescriptor.EL_VER_CORRECTOR)) instanceof VerCorrector)||(el instanceof Kicker));
									else {
										Object[] args= {name,"HorCorrector of Kicker",Integer.toString(ft.lineno())};
										throw new DataBushException(java.text.MessageFormat.format(NAME_USED,args));
									}
									ft.nextToken();
									while ((ft.ttype!=StreamTokenizer.TT_EOF)&&(ft.ttype!=StreamTokenizer.TT_EOL)) {//till end search for parameters
										if (ft.ttype==StreamTokenizer.TT_WORD) { //parameter found
											if (ft.sval.toLowerCase().equals("l")) ((AbstractCorrector)el).setLength(readValue(ft));
											else if (ft.sval.toLowerCase().equals("kick")) ((AbstractCorrector)el).setAngle(readValue(ft));
											else throw new DataBushException("Unrecognized parameter expression \""+ft.sval+"\" for VKICK in line "+ft.lineno());
										}
										else if (ft.ttype=='&') while ((ft.ttype!=StreamTokenizer.TT_EOF)&&(ft.ttype!=StreamTokenizer.TT_EOL)) ft.nextToken();
										ft.nextToken();
									}
								} else if (ft.sval.toLowerCase().equals("vmonitor") || ft.sval.toLowerCase().equals("hmonitor")) {
									// this is MAD's BPM type definition in a single line, like: BPMV: VMONITOR
									ft.skipLine();
								} else if ((!ft.sval.toLowerCase().equals("rfcavity"))&&(!ft.sval.toLowerCase().equals("aperture")))
									throw new DataBushException("Unrecognized MAD element expression \""+ft.sval+"\" in line "+ft.lineno());
							}
						} else throw new DataBushException("Sintax error in line "+ft.lineno()+", after element_name or variable_name is \":\" expected");
					}
				} else if ((ft.ttype!=';')&&(ft.ttype!=StreamTokenizer.TT_EOF)&&(ft.ttype!=StreamTokenizer.TT_EOL))
					throw new DataBushException("Sintax error "+ft.toString()+" in not rekognized by pharser");
				if (ft.ttype!=StreamTokenizer.TT_EOF) ft.nextToken();
			}
		} catch (DataBushException e) {
			throw e;
		} catch (java.text.ParseException e) {
			throw new DataBushException("Prereader complained "+ft.toString()+": "+e.toString());
		} catch (Throwable e) {
			throw new DataBushException("Unsoported exception was thrown pharsing "+ft.toString()+" : "+e.toString());
		}
	}
	/**
	 * {@inheritDoc}
	 *
	 * Reads input and creates DataBush elements. Input must be opened with
	 * one of <code>ParameterReader.openInStream</code> methods. Created elements
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
	 * one of <code>ParameterReader.openInStream</code> methods. Created elements
	 * are stored and returned in <code>ElementList</code>.
	 * Call read only once per new opened stream.
	 */
	@Override
	public ElementList<AbstractDataBushElement> read(ElementList<AbstractDataBushElement> l) throws DataBushException {
		list= l;
		consts= new HashMap<>();
		FileTokenizer t=null;
		if (files!=null)
			for (Object file : files) {
				try {
//			parseMADStream(t=new FileTokenizer((st==ST_URL) ? ((java.net.URL)files[i]) : ((File)files[i])));
					if (st==ST_URL) parseMADStream(t=newFileTokenizer((java.net.URL)file));
					else parseMADStream(t=new FileTokenizer((File)file));
				} catch (DataBushException e) {
					throw e;
				} catch (Exception e) {throw new DataBushException("Unsoported exception thrown pharsing "+file.toString()+" : "+e.toString());}
//		} catch (Exception e) {if (!(e instanceof DataBushException)) throw new DataBushException("Unsoported exception thrown pharsing "+files[i].toString()+" : "+e.toString());}
				finally {
					try {
						t.close();
					} catch (Exception e) {}
				}
			}
		else {
			try {
				parseMADStream(newFileTokenizer(tok.getURL()));
			} catch (DataBushException e) {
				throw e;
			} catch (Exception e) {throw new DataBushException("Unsoported exception thrown pharsing "+tok.getURL().toString()+" : "+e.toString());}
			finally {
				try {
					t.close();
				} catch (Exception e) {}
			}
		}
		return list;
	}
	/**
	 *
	 * @return int
	 */
	private double readValue(FileTokenizer ft) throws DataBushException, java.text.ParseException {
		double d= 0.0;
		int operation=0;
		try {
			if (ft.ttype!=StreamTokenizer.TT_EOF)
				if (ft.nextToken()=='=')
					if (ft.nextToken()!=StreamTokenizer.TT_EOF) {
						while ((ft.ttype!=StreamTokenizer.TT_EOF)&&(ft.ttype!=',')&&(ft.ttype!=StreamTokenizer.TT_EOL)) {
							if (ft.ttype==StreamTokenizer.TT_NUMBER) {
								if (operation=='/') d/= ft.nval;
								else if (operation=='*') d*= ft.nval;
								else d= ft.nval;
								operation=0;
							} else if (ft.ttype=='/') operation='/';
							else if (ft.ttype==StreamTokenizer.TT_WORD) {
								if (ft.sval.toLowerCase().equals("twopi")) {
									if (operation=='/') d/= (Math.PI*2.0);
									else if (operation=='*') d*= (Math.PI*2.0);
									else d= (Math.PI*2.0);
									operation=0;
								} else {//if (constants.containsKey(ft.sval)) {
									if (operation=='/') d/= getConstant(ft.sval);
									else if (operation=='*') d*= getConstant(ft.sval);
									else d= getConstant(ft.sval);
									operation=0;
								}
	//							else if (operation!=0) throw new DataBushException("Expected number value after \"/\" in line "+ft.lineno()+" but \""+ft.toString()+"\" was found");
							}
							else if (ft.ttype!='+') throw new DataBushException("Unexpected "+ft.toString()+", pharsing double value expresion");
							ft.nextToken();
						}
					}
		} catch(IOException e) {throw new DataBushException("Expected \"=<double value expression>\" in line "+ft.lineno()+" but IOException was thrown: "+e.getMessage());}
		ft.pushBack();
		return d;
	}
	/**
	 * Saves to files string representation of DataBush in MAD specific format, as is obtained with <code>saveToStrings</code> methods.
	 * Three files must be specified to save DataBush.
	 * <ul>
	 * <li>inputFile is input file for MADInterpreter.</li>
	 * <li>dbFile is file with DataBush represented in its specific format.
	 * inputFile points to this file in DBINPUT part of code. dbFile may be used as input for DefaultDBReader.</li>
	 * <li>madInput is file with MAD specific representation of DataBush. This is only naked data with no MAD command lines.</li>
	 * </ul>
	 *
	 * @param db the {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} to be saved
	 * @param inputFile <code>java.io.File</code> with input for MADInterpreter
	 * @param dbFile <code>java.io.File</code> with DB specific DataBush representation
	 * @param madFile <code>java.io.File</code> with MAD specific DataBush representation
	 * @throws java.io.IOException signals problems during writing to files.
	 */
	public static void saveToFiles(DataBush db, File inputFile, File dbFile, File madFile) throws IOException {//, DataBushException {
		FileWriter out;
		String[] st= saveToStrings(db);

		String input="<dbinput\r\n\""+dbFile.toString()+"\"\r\n>\r\n<madinput\r\n\""+madFile.toString()+"\"\r\n>";
		out= new FileWriter(inputFile);
		out.write(input);
		out.close();

		out= new FileWriter(dbFile);
		out.write(st[0]);
		out.close();

		input="";
		for (int i=1; i<st.length; i++) input+=st[i]+"\r\n";

		out= new FileWriter(madFile);
		out.write(input);
		out.close();

	}
	/**
	 * Saves DataBush  in to <code>String[]</code> array.
	 * At 0 index, <code>returnedArray[0]</code>, is saved DB specific representation of DataBush.
	 * At further indexes are saved MAD specific representations of each group of elements (markers, bendings,...) separately.
	 * Each group is saved on two consecutive positions in array. In one definition of element from group, in other description of position.
	 * Only MARKER is describe under one array index, since its enough to define marker with position only.<br>
	 * <br>
	 * Strings in array may be accessed with use of static fields, declared in MADInterpreter.<br>
	 * <em>EXAMPLE</em>
	 * <pre>
	 * String[] st= MADInterpreter.saveToStrings(myDataBush);
	 * <br>
	 * //this prints out header and description of all elements from myDataBush in DB specific code.
	 * System.out.println(HEADER[DB]);
	 * System.out.println(st[DB]);
	 * <br>
	 * //this prints out header and MAD description of markers
	 * //do not use MARK+1, markers are described only under MARK position
	 * System.out.println(HEADER[MARK]);
	 * System.out.println(st[MARK]);
	 * <br>
	 * //this prints out header and MAD coded string with definition of quadrupoles
	 * System.out.println(HEADER[QU]);
	 * System.out.println(st[QU]);
	 * <br>
	 * //this prints out header and MAD coded string with positions of quadrupoles
	 * System.out.println(HEADER[QU+1]);
	 * System.out.println(st[QU+1]);
	 * </pre>
	 *
	 * @return an <code>java.lang.String[]</code> array with description of elements in DataBush
	 * @param db the {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} to be represented in strings
	 */
	public static String[] saveToStrings(DataBush db) {
		class DBIt implements SimpleIterator{
			Iterator<AbstractOpticalElement> it;
			DBIt(DataBush db) {
				super();
				it= db.getOptics().iterator();
			}
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}
			@Override
			public AbstractDataBushElement next() {
				return it.next();
			}
		}
		String[] st= makeString(new DBIt(db));
		st[0]= db.toString();
		return st;
	}
	/**
	 * Saves elements from <code>ElementList</code> in same way, as they are with <code>saveToStrings(DataBush)</code>.
	 * Difference is, that elements taken from list are not initialized and do not guarantee to be able to initialize DataBush.
	 *
	 * @return an <code>java.lang.String[]</code> array with description of elements in DataBush
	 * @see #saveToStrings(DataBush)
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public static String[] saveToStrings(ElementList<AbstractDataBushElement> l) {
		class ListIt implements SimpleIterator {
			Iterator<AbstractDataBushElement> it;
			ListIt(ElementList<AbstractDataBushElement> l) {
				super();
				it= l.iterator();
			}
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}
			@Override
			public AbstractDataBushElement next() {
				return it.next();
			}
		}
		String[] st= makeString(new ListIt(l));
		st[0]= l.toString();
		return st;
	}
}
