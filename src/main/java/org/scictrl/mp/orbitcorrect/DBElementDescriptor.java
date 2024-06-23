package org.scictrl.mp.orbitcorrect;

import org.scictrl.mp.orbitcorrect.model.optics.Aperture;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.Cavity;
import org.scictrl.mp.orbitcorrect.model.optics.DataBushInfo;
import org.scictrl.mp.orbitcorrect.model.optics.HorCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.Kicker;
import org.scictrl.mp.orbitcorrect.model.optics.Marker;
import org.scictrl.mp.orbitcorrect.model.optics.PowerSupply;
import org.scictrl.mp.orbitcorrect.model.optics.Quadrupole;
import org.scictrl.mp.orbitcorrect.model.optics.RBending;
import org.scictrl.mp.orbitcorrect.model.optics.RFGenerator;
import org.scictrl.mp.orbitcorrect.model.optics.SBending;
import org.scictrl.mp.orbitcorrect.model.optics.Septum;
import org.scictrl.mp.orbitcorrect.model.optics.Sextupole;
import org.scictrl.mp.orbitcorrect.model.optics.VerCorrector;


/**
 * <p>DBElementDescriptor class.</p>
 *
 * @author igor@scictrl.com
 */
public final class DBElementDescriptor {
	private int value;

	/** Constant <code>EL_DBINFO=0</code> */
	public static final int EL_DBINFO= 0;
	/** Constant <code>EL_POWER_SUPPLY=1</code> */
	public static final int EL_POWER_SUPPLY= 1;
	/** Constant <code>EL_MARKER=2</code> */
	public static final int EL_MARKER= 2;
	/** Constant <code>EL_BPM=3</code> */
	public static final int EL_BPM= 3;
	/** Constant <code>EL_APERTURE=4</code> */
	public static final int EL_APERTURE= 4;
	/** Constant <code>EL_CAVITY=5</code> */
	public static final int EL_CAVITY= 5;
	/** Constant <code>EL_SEPTUM=6</code> */
	public static final int EL_SEPTUM= 6;
	/** Constant <code>EL_KICKER=7</code> */
	public static final int EL_KICKER= 7;
	/** Constant <code>EL_HOR_CORRECTOR=8</code> */
	public static final int EL_HOR_CORRECTOR= 8;
	/** Constant <code>EL_VER_CORRECTOR=9</code> */
	public static final int EL_VER_CORRECTOR= 9;
	/** Constant <code>EL_SBENDING=10</code> */
	public static final int EL_SBENDING= 10;
	/** Constant <code>EL_RBENDING=11</code> */
	public static final int EL_RBENDING= 11;
	/** Constant <code>EL_QUADRUPOLE=12</code> */
	public static final int EL_QUADRUPOLE= 12;
	/** Constant <code>EL_SEXTUPOLE=13</code> */
	public static final int EL_SEXTUPOLE= 13;
	/** Constant <code>EL_RF_GENERATOR=14</code> */
	public static final int EL_RF_GENERATOR= 14;

	/** Constant <code>ELDES_DBINFO</code> */
	public static final DBElementDescriptor ELDES_DBINFO= new DBElementDescriptor(EL_DBINFO);
	/** Constant <code>ELDES_POWER_SUPPLY</code> */
	public static final DBElementDescriptor ELDES_POWER_SUPPLY= new DBElementDescriptor(EL_POWER_SUPPLY);
	/** Constant <code>ELDES_MARKER</code> */
	public static final DBElementDescriptor ELDES_MARKER= new DBElementDescriptor(EL_MARKER);
	/** Constant <code>ELDES_BPM</code> */
	public static final DBElementDescriptor ELDES_BPM= new DBElementDescriptor(EL_BPM);
	/** Constant <code>ELDES_APERTURE</code> */
	public static final DBElementDescriptor ELDES_APERTURE= new DBElementDescriptor(EL_APERTURE);
	/** Constant <code>ELDES_CAVITY</code> */
	public static final DBElementDescriptor ELDES_CAVITY= new DBElementDescriptor(EL_CAVITY);
	/** Constant <code>ELDES_SEPTUM</code> */
	public static final DBElementDescriptor ELDES_SEPTUM= new DBElementDescriptor(EL_SEPTUM);
	/** Constant <code>ELDES_KICKER</code> */
	public static final DBElementDescriptor ELDES_KICKER= new DBElementDescriptor(EL_KICKER);
	/** Constant <code>ELDES_HOR_CORRECTOR</code> */
	public static final DBElementDescriptor ELDES_HOR_CORRECTOR= new DBElementDescriptor(EL_HOR_CORRECTOR);
	/** Constant <code>ELDES_VER_CORRECTOR</code> */
	public static final DBElementDescriptor ELDES_VER_CORRECTOR= new DBElementDescriptor(EL_VER_CORRECTOR);
	/** Constant <code>ELDES_SBENDING</code> */
	public static final DBElementDescriptor ELDES_SBENDING= new DBElementDescriptor(EL_SBENDING);
	/** Constant <code>ELDES_RBENDING</code> */
	public static final DBElementDescriptor ELDES_RBENDING= new DBElementDescriptor(EL_RBENDING);
	/** Constant <code>ELDES_QUADRUPOLE</code> */
	public static final DBElementDescriptor ELDES_QUADRUPOLE= new DBElementDescriptor(EL_QUADRUPOLE);
	/** Constant <code>ELDES_SEXTUPOLE</code> */
	public static final DBElementDescriptor ELDES_SEXTUPOLE= new DBElementDescriptor(EL_SEXTUPOLE);
	/** Constant <code>ELDES_RF_GENERATOR</code> */
	public static final DBElementDescriptor ELDES_RF_GENERATOR= new DBElementDescriptor(EL_RF_GENERATOR);

	/** Constant <code>ELEMENT_TAG="element"</code> */
	public static final String ELEMENT_TAG= "element";
	/** Constant <code>TEMPLATE_TAG="template"</code> */
	public static final String TEMPLATE_TAG= "template";

	/** Constant <code>PARAMETERS_TAGS</code> */
	public final static String[][] PARAMETERS_TAGS=
	{
	{"name","virtual","energy","calibrationfile","efrombend","orbitclosed","betax","betaz","alphax","alphaz","qx","qz","d","dp"},
	{"name","virtual","current"},
	{"name","virtual","position","relposition","relfrom"},
	{"name","virtual","position","relposition","relfrom"},
	{"name","virtual","position","relposition","relfrom","length","xsize","zsize"},
	{"name","virtual","position","relposition","relfrom","length","volt","lag","harmon","betrf","pg","shunt","tfill"},
	{"name","virtual","position","relposition","relfrom","length","ps"},
	{"name","virtual","position","relposition","relfrom","length","ps","calibrationentry","angle"},
	{"name","virtual","position","relposition","relfrom","length","ps","calibrationentry","angle"},
	{"name","virtual","position","relposition","relfrom","length","ps","calibrationentry","angle"},
	{"name","virtual","position","relposition","relfrom","length","ps","calibrationentry","quadrupolestrength","radius","dipolefield","energy"},
	{"name","virtual","position","relposition","relfrom","length","ps","calibrationentry","quadrupolestrength","radius","dipolefield","energy","leftwedge","rightwedge"},
	{"name","virtual","position","relposition","relfrom","length","ps","calibrationentry","quadrupolestrength"},
	{"name","virtual","position","relposition","relfrom","length","ps","calibrationentry","sextupolestrength"},
	{"name","virtual","frequency"},
	};

	/** Constant <code>ELEMENTS_CLASS_TAGS</code> */
	public final static String[] ELEMENTS_CLASS_TAGS=
	{"dbinfo","powersupply","marker","bpm","aperture","cavity","septum","kicker","horcorrector","vercorrector","sbending","rbending","quadrupole","sextupole","rfgenerator"};

	private static final Boolean b= Boolean.valueOf(true);
	private static final Double d= Double.valueOf(0.0);
	private static final Integer i= Integer.valueOf(0);
	private static final String s= "";

	/** Constant <code>PARAMETERS</code> */
	public static final Object[][] PARAMETERS=
	{
	{s,b,d,s,b,b,d,d,d,d,d,d,d,d},
	{s,b,d},
	{s,b,d,d,s},
	{s,b,d,d,s},
	{s,b,d,d,s,d,d,d},
	{s,b,d,d,s,d,d,d,i,d,d,d,d},
	{s,b,d,d,s,d,s},
	{s,b,d,d,s,d,s,s,d},
	{s,b,d,d,s,d,s,s,d},
	{s,b,d,d,s,d,s,s,d},
	{s,b,d,d,s,d,s,s,d,d,d,d},
	{s,b,d,d,s,d,s,s,d,d,d,d,d,d},
	{s,b,d,d,s,d,s,s,d},
	{s,b,d,d,s,d,s,s,d},
	{s,b,d},
	};

	/** Constant <code>SINGLE_PARAMETER_CLASS</code> */
	public static final Class<?>[] SINGLE_PARAMETER_CLASS=	{String.class};

	/** Constant <code>PARAMETERS_CLASSES</code> */
	public static final Class<?>[][] PARAMETERS_CLASSES=
	{
	{String.class,boolean.class,double.class,String.class,boolean.class,boolean.class,double.class,double.class,double.class,double.class,double.class,double.class,double.class,double.class},
	{String.class,boolean.class,double.class},
	{String.class,boolean.class,double.class,double.class,String.class},
	{String.class,boolean.class,double.class,double.class,String.class},
	{String.class,boolean.class,double.class,double.class,String.class,double.class,double.class,double.class},
	{String.class,boolean.class,double.class,double.class,String.class,double.class,double.class,double.class,int.class,double.class,double.class,double.class,double.class},
	{String.class,boolean.class,double.class,double.class,String.class,double.class,String.class},
	{String.class,boolean.class,double.class,double.class,String.class,double.class,String.class,String.class,double.class},
	{String.class,boolean.class,double.class,double.class,String.class,double.class,String.class,String.class,double.class},
	{String.class,boolean.class,double.class,double.class,String.class,double.class,String.class,String.class,double.class},
	{String.class,boolean.class,double.class,double.class,String.class,double.class,String.class,String.class,double.class,double.class,double.class,double.class},
	{String.class,boolean.class,double.class,double.class,String.class,double.class,String.class,String.class,double.class,double.class,double.class,double.class,double.class,double.class},
	{String.class,boolean.class,double.class,double.class,String.class,double.class,String.class,String.class,double.class},
	{String.class,boolean.class,double.class,double.class,String.class,double.class,String.class,String.class,double.class},
	{String.class,boolean.class,double.class},
	};
	/** Constant <code>ELEMENT_CLASSES</code> */
	public static final String[] ELEMENT_CLASSES=
	{DataBushInfo.class.getName(),
	 PowerSupply.class.getName(),
	 Marker.class.getName(),
	 BPMonitor.class.getName(),
	 Aperture.class.getName(),
	 Cavity.class.getName(),
	 Septum.class.getName(),
	 Kicker.class.getName(),
	 HorCorrector.class.getName(),
	 VerCorrector.class.getName(),
	 SBending.class.getName(),
	 RBending.class.getName(),
	 Quadrupole.class.getName(),
	 Sextupole.class.getName(),
	 RFGenerator.class.getName()};

	/** Constant <code>EL_CLASSES</code> */
	public static final String[] EL_CLASSES=
	{"DataBushInfo",
	 "PowerSupply",
	 "Marker",
	 "BPMonitor",
	 "Aperture",
	 "Cavity",
	 "Septum",
	 "Kicker",
	 "HorCorrector",
	 "VerCorrector",
	 "SBending",
	 "RBending",
	 "Quadrupole",
	 "Sextupole",
	 "RFGenerator"};

/**
 * DBElementDescriptor constructor. With parameter <code>i</code> is specified descripted element for this <code>DBElementDescriptor</code>.
 * It is important in most methods.
 */
private DBElementDescriptor(int i) {
	super();
	value= i;
}
/**
 * This method returns default values for descripted element.
 *
 * @return java.lang.Object[]
 */
public Object[] defaultParameters() {
	return PARAMETERS[value];
}
/**
 * This method returns <code>DBElementDescriptor</code> for element, whose tag is specified with parameter.
 * It checks all element from ELEMENT_CLASS_TAGS and if any of them has the same tag as specified with parameter,
 * it is returned.
 *
 * @return DBDescriptor
 * @see org.scictrl.mp.orbitcorrect.DBElementDescriptor#fromInt
 * @see org.scictrl.mp.orbitcorrect.DBElementDescriptor#ELEMENTS_CLASS_TAGS
 * @param tag a {@link java.lang.String} object
 * @throws java.lang.IllegalArgumentException if any.
 */
public static DBElementDescriptor fromElementClassTag(String tag) throws IllegalArgumentException{
	for (int i=0;i<ELEMENTS_CLASS_TAGS.length;i++)
		if (tag.equals(ELEMENTS_CLASS_TAGS[i])) return fromInt(i);
	throw new IllegalArgumentException();
}
/**
 * This method returns <code>DBElementDescriptor</code> for element, whose code is specified with parameter.
 *
 * @return DBDescriptor
 * @param code int
 * @see org.scictrl.mp.orbitcorrect.DBElementDescriptor#EL_DBINFO
 * @throws java.lang.IllegalArgumentException if any.
 */
public static DBElementDescriptor fromInt(int code) throws IllegalArgumentException{
	switch (code) {
		case EL_DBINFO: return ELDES_DBINFO;
		case EL_POWER_SUPPLY: return ELDES_POWER_SUPPLY;
		case EL_MARKER: return ELDES_MARKER;
		case EL_BPM: return ELDES_BPM;
		case EL_APERTURE: return ELDES_APERTURE;
		case EL_CAVITY: return ELDES_CAVITY;
		case EL_SEPTUM: return ELDES_SEPTUM;
		case EL_KICKER: return ELDES_KICKER;
		case EL_HOR_CORRECTOR: return ELDES_HOR_CORRECTOR;
		case EL_VER_CORRECTOR: return ELDES_VER_CORRECTOR;
		case EL_SBENDING: return ELDES_SBENDING;
		case EL_RBENDING: return ELDES_RBENDING;
		case EL_QUADRUPOLE: return ELDES_QUADRUPOLE;
		case EL_SEXTUPOLE: return ELDES_SEXTUPOLE;
		case EL_RF_GENERATOR: return ELDES_RF_GENERATOR;
	}
	throw new IllegalArgumentException("element with code \""+code+"\" not recognized");
}
/**
 * This method return class name of descripted element.
 *
 * @return java.lang.String
 */
public String getElClass() {
	return EL_CLASSES[value];
}
/**
 * This method return class name of descripted element, with full classpath.
 *
 * @return java.lang.String
 */
public String getElementClass() {
	return ELEMENT_CLASSES[value];
}
/**
 * This method return class tag of descripted element.
 *
 * @return java.lang.String
 */
public String getElementClassTag() {
	return ELEMENTS_CLASS_TAGS[value];
}
/**
 * This method returns specified parameters tag of descripted element.
 *
 * @return java.lang.String  tag
 * @param par int   specification, which tag (name, energy, virtual, ...)
 * @see DBElementDescriptor#PARAMETERS_TAGS
 * @throws java.lang.IllegalArgumentException if any.
 */
public String getParameterTag(int par) throws IllegalArgumentException {
	if (par<0||par>=PARAMETERS_TAGS[value].length) throw new IllegalArgumentException();
	return PARAMETERS_TAGS[value][par];
}
/**
 * <p>parametersClasses.</p>
 *
 * @return java.lang.String
 */
public Class<?>[] parametersClasses() {
	return PARAMETERS_CLASSES[value];
}
/**
 * This method return number of descriptor tags of descripted element.
 *
 * @return int
 */
public int parametersCount() {
	return PARAMETERS[value].length;
}
/**
 * This method returns array of tags of descripted element.
 *
 * @return java.lang.String  tag
 * @see DBElementDescriptor#PARAMETERS_TAGS
 */
public String[] parametersTags() {
	return PARAMETERS_TAGS[value];
}
/**
 * This method returns code of descripted element. Code is provided by constructor and with this code is determinated
 * element, because each element has it unique code, which can be seen at fields, starting with EL_ (EL_KICKER, EL_MARKER,..., except EL_CLASSES).
 *
 * @return int
 */
public int value() {
	return value;
}
}
