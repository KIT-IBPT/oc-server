package org.scictrl.mp.orbitcorrect.accessories;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.scictrl.mp.orbitcorrect.DBElementDescriptor;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractBending;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCalibratedMagnet;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractMagnetElement;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractTransferElement;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitorList;
import org.scictrl.mp.orbitcorrect.model.optics.CalMagnetList;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.DataBushInfo;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
import org.scictrl.mp.orbitcorrect.model.optics.MagnetList;
import org.scictrl.mp.orbitcorrect.model.optics.OpticsList;
import org.scictrl.mp.orbitcorrect.model.optics.PowerSupply;
import org.scictrl.mp.orbitcorrect.model.optics.Quadrupole;
import org.scictrl.mp.orbitcorrect.model.optics.Sextupole;
import org.scictrl.mp.orbitcorrect.model.optics.TransferList;

/**
 * Class of useful methods, which are printing lists, matrices etc, that are in
 * use in DataBush.
 *
 * @author igor@scictrl.com
 */
public final class Utilities {

	private static DecimalFormat format4D = new DecimalFormat("0.0000");
	private static DecimalFormat format3D = new DecimalFormat("0.000");
	private static DecimalFormat format2D = new DecimalFormat("0.00");
	private static DecimalFormat format1D = new DecimalFormat("0.0");
	private static DecimalFormat format4E = new DecimalFormat("0.0000E0");
	private static DecimalFormat format3E = new DecimalFormat("0.000E0");
	private static DecimalFormat format2E = new DecimalFormat("0.00E0");
	private static DecimalFormat format1E = new DecimalFormat("0.0E0");
	private static SimpleDateFormat formatdMHmsS = new SimpleDateFormat("(dd.MM HH:mm:ss.SSS)");
	private static SimpleDateFormat formatmsS = new SimpleDateFormat("mm:ss.SSS");
	private static SimpleDateFormat formatms = new SimpleDateFormat("mm:ss");

	/**
	 * <p>
	 * format4E.
	 * </p>
	 *
	 * @param d a double
	 * @return a {@link java.lang.String} object
	 */
	public static String format4E(double d) {
		return format4E.format(d);
	}

	/**
	 * <p>
	 * format3E.
	 * </p>
	 *
	 * @param d a double
	 * @return a {@link java.lang.String} object
	 */
	public static String format3E(double d) {
		return format3E.format(d);
	}

	/**
	 * <p>
	 * format2E.
	 * </p>
	 *
	 * @param d a double
	 * @return a {@link java.lang.String} object
	 */
	public static String format2E(double d) {
		return format2E.format(d);
	}

	/**
	 * <p>
	 * format1E.
	 * </p>
	 *
	 * @param d a double
	 * @return a {@link java.lang.String} object
	 */
	public static String format1E(double d) {
		return format1E.format(d);
	}

	/**
	 * <p>
	 * format3D.
	 * </p>
	 *
	 * @param d a double
	 * @return a {@link java.lang.String} object
	 */
	public static String format3D(double d) {
		return format3D.format(d);
	}

	/**
	 * <p>
	 * format2D.
	 * </p>
	 *
	 * @param d a double
	 * @return a {@link java.lang.String} object
	 */
	public static String format2D(double d) {
		return format2D.format(d);
	}

	/**
	 * <p>
	 * format1D.
	 * </p>
	 *
	 * @param d a double
	 * @return a {@link java.lang.String} object
	 */
	public static String format1D(double d) {
		return format1D.format(d);
	}

	/**
	 * <p>
	 * format4D.
	 * </p>
	 *
	 * @param d a double
	 * @return a {@link java.lang.String} object
	 */
	public static String format4D(double d) {
		return format4D.format(d);
	}

	/**
	 * <p>
	 * formatdMHmsS.
	 * </p>
	 *
	 * @param d a {@link java.util.Date} object
	 * @return a {@link java.lang.String} object
	 */
	public static String formatdMHmsS(Date d) {
		return formatdMHmsS.format(d);
	}

	/**
	 * <p>
	 * formatdMHmsS.
	 * </p>
	 *
	 * @param d a long
	 * @return a {@link java.lang.String} object
	 */
	public static String formatdMHmsS(long d) {
		return formatdMHmsS.format(new Date(d));
	}

	/**
	 * <p>
	 * formatdMHmsS.
	 * </p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public static String formatdMHmsS() {
		return formatdMHmsS.format(new Date());
	}

	/**
	 * Format duration in milliseconds as &lt;hours (can be more than
	 * 24)&gt;.&lt;minutes in hour&gt;:&lt;secondsin minute&gt;
	 *
	 * @param duration a long
	 * @return HHH:mm:ss.SSS
	 */
	public static String formatHHHHmsS(long duration) {
		if (duration > 3600000L) {
			long hours = Math.floorDiv(duration, 3600000L);
			long rest = Math.floorMod(duration, 3600000L);
			return hours + formatmsS.format(rest) + " hours";
		} else if (duration > 60000L) {
			return formatmsS.format(duration) + " min";
		} else if (duration > 1000L) {
			return format3D(duration / 1000.0) + " s";
		} else {
			return duration + " ms";
		}
	}

	/**
	 * Format duration in seconds as &lt;hours (can be more than 24)&gt;.&lt;minutes
	 * in hour&gt;:&lt;secondsin minute&gt;
	 *
	 * @param duration a long
	 * @return HHH:mm:ss.SSS
	 */
	public static String formatHHHHms(long duration) {
		if (duration > 3600000L) {
			long hours = Math.floorDiv(duration, 3600000L);
			long rest = Math.floorMod(duration, 3600000L);
			return hours + formatms.format(rest) + " hours";
		} else if (duration > 60000L) {
			return formatms.format(duration) + " min";
		} else {
			return format1D(duration / 1000.0) + " s";
		}
	}

	/**
	 * Compares elements from two diffrent ElementLists and returns report.Method
	 * compares if there are same elements in both lists.They represent same
	 * physical element in storage ring.If this is true, method compares diffrent
	 * values of these two elements.The diffrences in lists and elements (same) are
	 * written in StringBuffer.
	 *
	 * @return java.lang.String
	 * @param first  ElementList
	 * @param second ElementList
	 * @see #compareValue(StringBuffer, String, AbstractDataBushElement,
	 *      AbstractDataBushElement)
	 */
	public static String compareImputs(ElementList<AbstractDataBushElement> first,
			ElementList<AbstractDataBushElement> second) {
		StringBuffer s = new StringBuffer();
		StringBuffer s1 = new StringBuffer();
		s1.append("#1 only has\r\n");
		Iterator<AbstractDataBushElement> it = first.iterator();
		AbstractDataBushElement el1, el2;
		// Class<?>[] cl= new Class[0];
		while (it.hasNext()) {
			el1 = it.next();
			if (second.contains(el1)) {
				el2 = second.get(el1.getName());
				compareValue(s, "getVirtual", el1, el2);
				if (el1 instanceof DataBushInfo) {
					compareValue(s, "getEnergy", el1, el2);
					compareValue(s, "getCalibrationFile", el1, el2);
				} else if (el1 instanceof PowerSupply) {
					compareValue(s, "getCurrent", el1, el2);
				} else if (el1 instanceof AbstractOpticalElement) {
					compareValue(s, "getPosition", el1, el2);
					compareValue(s, "getRelPosition", el1, el2);
					compareValue(s, "getRelFrom", el1, el2);
					if (el1 instanceof AbstractTransferElement) {
						compareValue(s, "getLength", el1, el2);
						if (el1 instanceof AbstractMagnetElement) {
							compareValue(s, "getPS", el1, el2);
							if (el1 instanceof AbstractCalibratedMagnet) {
								compareValue(s, "getCalibrationEntry", el1, el2);
								if (el1 instanceof AbstractBending) {
									compareValue(s, "getQuadrupoleStrength", el1, el2);
									compareValue(s, "getRadius", el1, el2);
									compareValue(s, "getDipoleField", el1, el2);
									compareValue(s, "getAngle", el1, el2);
								}
								if (el1 instanceof AbstractCorrector) {
									compareValue(s, "getAngle", el1, el2);
								}
								if (el1 instanceof Quadrupole) {
									compareValue(s, "getQuadrupoleStrength", el1, el2);
								}
								if (el1 instanceof Sextupole) {
									compareValue(s, "getSextupoleStrength", el1, el2);
								}
							}
						}
					}
				}
			} else {
				s1.append(el1.getName());
				s1.append("\r\n");
			}
		}
		s1.append("\r\n#2 only has\r\n");
		it = second.iterator();
		while (it.hasNext()) {
			el2 = it.next();
			if (!first.contains(el2)) {
				s1.append(el2.getName());
				s1.append("\r\n");
			}
		}
		s.append("\r\n");
		s.append(s1);
		return s.toString();
	}

	/**
	 * Compares return values, returned by the method, invoked on
	 * AbstractDataBushElement.If returned values are diffrent, a message is written
	 * in StringBuffer s, containing DataBushElements names, method and returned
	 * values.
	 * 
	 * @param s      java.lang.StringBuffer
	 * @param method java.lang.reflect.Method method that is invoked on
	 *               AbstractDataBushElement el1, el2
	 * @param el1    AbstractDataBushElement
	 * @param el2    AbstractDataBushElement
	 */
	private static void compareValue(StringBuffer s, String method, AbstractDataBushElement el1,
			AbstractDataBushElement el2) {
		Object[] par = new Object[0];
		Class<?>[] cl = new Class[0];
		try {
			java.lang.reflect.Method m = el1.getClass().getMethod(method, cl);
			Object o1 = m.invoke(el1, par);
			Object o2 = m.invoke(el2, par);
			if (!o1.equals(o2)) {
				s.append(el1.getName());
				s.append("->");
				s.append(method);
				s.append(" #1 ");
				s.append(o1);
				s.append(" #2 ");
				s.append(o2);
				s.append("\r\n");
			}
		} catch (IllegalAccessException e) {
			handleException(e);
		} catch (java.lang.reflect.InvocationTargetException e) {
			handleException(e);
		} catch (NoSuchMethodException e) {
			handleException(e);
		}
	}

	/**
	 *
	 * @param e java.lang.Throwable
	 */
	private static void handleException(Throwable e) {
		System.out.println("Error in " + Utilities.class.toString());
		e.printStackTrace();
	}

	/**
	 * Searches for Object o in array of objects.If Object is found method returns
	 * index of object in the array.Otherwise -1 is returned.
	 *
	 * @return boolean
	 * @param array java.lang.Object[]
	 * @param o     java.lang.Object
	 */
	public static final int lazySearch(Object[] array, Object o) {
		for (int i = 0; i < array.length; i++)
			if (array[i].equals(o))
				return i;
		return -1;
	}

	/**
	 * Returns formated text of beam data.
	 *
	 * @return java.lang.String
	 * @param list DataBushInterface.ProtectedControlList
	 * @see #printBeamData(OpticsList, PrintWriter)
	 */
	public static String printBeamData(OpticsList list) {
		java.io.StringWriter st = new java.io.StringWriter();
		java.io.PrintWriter pr = new java.io.PrintWriter(st);
		printBeamData(list, pr);
		return st.toString();
	}

	/**
	 * Prints parameters (beam data) of OpticElements, that are in OpticsList, in
	 * PrintWriter.
	 *
	 * @param list DataBushInterface.ProtectedControlList
	 * @param st   a {@link java.io.PrintWriter} object
	 */
	public static void printBeamData(OpticsList list, java.io.PrintWriter st) {
		st.println("groupindex\tname\tposition\tq_X\tq_Z\tbeta_X\tbeta_Z\talpha_X\taplha_Z\tD\tD'");
		Iterator<AbstractOpticalElement> it = list.iterator();
		AbstractOpticalElement el;
		AbstractTransferElement te;
		while (it.hasNext()) {
			el = it.next();
			if (el instanceof AbstractTransferElement) {
				te = (AbstractTransferElement) el;
				st.print(el.getIndex());
				st.print("\t");
				st.print(el.getName());
				st.print("\t");
				st.print(el.getPosition() - te.getLength() / 2.0);
				st.print("\t");
				st.print(te.getQ1().toString());
				st.print("\t");
				st.print(te.getBeta1().toString());
				st.print("\t");
				st.print(te.getAlpha1().toString());
				st.print("\t");
				st.print(te.getDispersion1().toString());
				st.println();

				st.print(el.getIndex());
				st.print("\t");
				st.print(el.getName());
				st.print("\t");
				st.print(el.getPosition());
				st.print("\t");
				st.print(el.getQ().toString());
				st.print("\t");
				st.print(el.getBeta().toString());
				st.print("\t");
				st.print(el.getAlpha().toString());
				st.print("\t");
				st.print(el.getDispersion().toString());
				st.println();

				st.print(el.getIndex());
				st.print("\t");
				st.print(el.getName());
				st.print("\t");
				st.print(el.getPosition() + te.getLength() / 2.0);
				st.print("\t");
				st.print(te.getQ2().toString());
				st.print("\t");
				st.print(te.getBeta2().toString());
				st.print("\t");
				st.print(te.getAlpha2().toString());
				st.print("\t");
				st.print(te.getDispersion2().toString());
				st.println();

			} else {

				st.print(el.getIndex());
				st.print("\t");
				st.print(el.getName());
				st.print("\t");
				st.print(el.getPosition());
				st.print("\t");
				st.print(el.getQ().toString());
				st.print("\t");
				st.print(el.getBeta().toString());
				st.print("\t");
				st.print(el.getAlpha().toString());
				st.print("\t");
				st.print(el.getDispersion().toString());
				st.println();
			}
		}
	}

	/**
	 * Returns formatted text of names and position BMPs in BPMonitorList and
	 * position of beam at BPMs.
	 *
	 * @return java.lang.String
	 * @param list DataBushInterface.ProtectedControlList
	 */
	public static String printBPMs(BPMonitorList list) {
		java.io.StringWriter st = new java.io.StringWriter();
		java.io.PrintWriter pr = new java.io.PrintWriter(st);
		printBPMs(list, pr);
		return st.toString();
	}

	/**
	 * Prints in PrintWriter name and position of BMPs and position of the beam at
	 * BPMs.
	 *
	 * @param list DataBushInterface.ProtectedControlList
	 * @param pr   a {@link java.io.PrintWriter} object
	 */
	public static void printBPMs(BPMonitorList list, java.io.PrintWriter pr) {

		pr.println("name\tposition\tHOR_pos\tVER_pos");
		Iterator<BPMonitor> it = list.iterator();
		BPMonitor el;
		while (it.hasNext()) {
			el = it.next();
			pr.print(el.getName());
			pr.print("\t");
			pr.print(el.getPosition());
			pr.print("\t");
			pr.print(el.getBeamPos().toString());
			pr.println();
		}
	}

	/**
	 * Returns text of information that are hold in DataBushInfo.
	 *
	 * @param info a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBushInfo}
	 *             DataBushInfo holds information about energy of baem.
	 * @param pr   a {@link java.io.PrintWriter} object
	 */
	public static void printDBInfo(DataBushInfo info, PrintWriter pr) {
		pr.print(info.getName() + "\n");
		pr.print("energy from ");
		pr.print((info.getReadEnergyFromBendings()) ? "bendings= " : "Info Server= \t");
		pr.print(info.getEnergy() + " \tGeV\n\n");
		pr.print("q=[\t" + info.getQ().toString() + "\t]\n");
		pr.print("b=[\t" + info.getBeta().toString() + "\t]\n");
		pr.print("a=[\t" + info.getAlpha().toString() + "\t]\n");
		pr.print("d=[\t" + info.getDispersion().toString() + "\t]\n");
	}

	/**
	 * <p>
	 * printDBInfo.
	 * </p>
	 *
	 * @param info a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBushInfo}
	 *             object
	 * @return a {@link java.lang.String} object
	 */
	public static String printDBInfo(DataBushInfo info) {
		java.io.StringWriter st = new java.io.StringWriter();
		java.io.PrintWriter pr = new java.io.PrintWriter(st);
		printDBInfo(info, pr);
		return st.toString();
	}

	/**
	 * Returns string describing elements in DataBush. String is formated in
	 * input-file code.
	 *
	 * @return java.lang.String
	 * @param db DataBush
	 */
	public static String printFormated(DataBush db) {
		java.io.StringWriter s = new java.io.StringWriter();
		printFormated(db, new java.io.PrintWriter(s));
		return s.toString();

	}

	/**
	 * Prints to PrintWriter string describing elements in DataBush. String is
	 * formated in input-file code.
	 *
	 * @param db DataBush
	 * @param p  a {@link java.io.PrintWriter} object
	 */
	public static void printFormated(DataBush db, java.io.PrintWriter p) {

		StringBuffer[] el = new StringBuffer[DBElementDescriptor.EL_CLASSES.length];
		for (int i = 0; i < DBElementDescriptor.EL_CLASSES.length; i++)
			el[i] = new StringBuffer();

		el[db.getDataBushInfo().descriptor().value()].append(db.getDataBushInfo().toString());

		Iterator<AbstractOpticalElement> it = db.getOptics().iterator();
		AbstractDataBushElement oe;
		while (it.hasNext()) {
			oe = it.next();
			el[oe.descriptor().value()].append(oe.toString());
		}

		Iterator<PowerSupply> pit = db.getPowerSupplies().iterator();
		while (pit.hasNext()) {
			oe = pit.next();
			el[oe.descriptor().value()].append(oe.toString());
		}

		if (db.getRFGenerator() != null)
			el[db.getRFGenerator().descriptor().value()].append(db.getRFGenerator().toString());

		for (int i = 0; i < DBElementDescriptor.EL_CLASSES.length; i++)
			p.println(el[i].toString());

	}

	/**
	 * Returns string describing elements in ElementList. String is formated in
	 * input-file code.
	 *
	 * @return java.lang.String
	 * @param l ElementList
	 */
	public static String printFormated(ElementList<AbstractDataBushElement> l) {
		java.io.StringWriter s = new java.io.StringWriter(1000);
		printFormated(l, new java.io.PrintWriter(s));
		return s.toString();
	}

	/**
	 * Prints to PrintWriter string describing elements in ElementList. String is
	 * formated in input-file code.
	 *
	 * @param l ElementList
	 * @param p a {@link java.io.PrintWriter} object
	 */
	public static void printFormated(ElementList<AbstractDataBushElement> l, java.io.PrintWriter p) {

		StringBuffer[] el = new StringBuffer[DBElementDescriptor.EL_CLASSES.length];
		for (int i = 0; i < DBElementDescriptor.EL_CLASSES.length; i++)
			el[i] = new StringBuffer();

		Iterator<AbstractDataBushElement> it = l.iterator();
		AbstractDataBushElement oe;
		while (it.hasNext()) {
			oe = it.next();
			el[oe.descriptor().value()].append(oe.toString());
		}

		for (int i = 0; i < DBElementDescriptor.EL_CLASSES.length; i++)
			p.println(el[i].toString());

	}

	/**
	 * Returns String of names,positions, and other properties of elements, held in
	 * CalMagnetList.
	 *
	 * @return java.lang.String
	 * @param list a {@link org.scictrl.mp.orbitcorrect.model.optics.CalMagnetList}
	 *             object
	 */
	public static String printMagnets(CalMagnetList list) {
		java.io.StringWriter st = new java.io.StringWriter();
		java.io.PrintWriter pr = new java.io.PrintWriter(st);
		printMagnets(list, pr);
		return st.toString();
	}

	/**
	 * Prints to PrintWriter names, positions, and other properties of elements,
	 * held in CalMagnetList.
	 *
	 * @param list a {@link org.scictrl.mp.orbitcorrect.model.optics.CalMagnetList}
	 *             object
	 * @param pr   a {@link java.io.PrintWriter} object
	 */
	public static void printMagnets(CalMagnetList list, java.io.PrintWriter pr) {
		pr.println("index\tname\tposition\tR\tk\tm");
		Iterator<AbstractCalibratedMagnet> it = list.iterator();
		AbstractCalibratedMagnet el;
		while (it.hasNext()) {
			el = it.next();
			if (el instanceof AbstractBending) {
				pr.print(el.getIndex());
				pr.print("\t");
				pr.print(el.getName());
				pr.print("\t");
				pr.print(el.getPosition());
				pr.print("\t");
				pr.print(((AbstractBending) el).getRadius());
				pr.print("\t");
				pr.print(((AbstractBending) el).getQuadrupoleStrength());
				pr.println("\tN/D\t");
			} else if (el instanceof Quadrupole) {
				pr.print(el.getIndex());
				pr.print("\t");
				pr.print(el.getName());
				pr.print("\t");
				pr.print(el.getPosition());
				pr.print("\tN/D\t");
				pr.print(((Quadrupole) el).getQuadrupoleStrength());
				pr.println("\tN/D");
			} else if (el instanceof Sextupole) {
				pr.print(el.getIndex());
				pr.print("\t");
				pr.print(el.getName());
				pr.print("\t");
				pr.print(el.getPosition());
				pr.print("\tN/D\tN/D\t");
				pr.print(((Sextupole) el).getSextupoleStrength());
				pr.println();
			}
		}
	}

	/**
	 * Returns String of names,positions, and other properties of elements, held in
	 * MagnetList.
	 *
	 * @return java.lang.String
	 * @param list a {@link org.scictrl.mp.orbitcorrect.model.optics.MagnetList}
	 *             object
	 */
	public static String printMagnets(MagnetList list) {
		java.io.StringWriter st = new java.io.StringWriter();
		java.io.PrintWriter pr = new java.io.PrintWriter(st);
		printMagnets(list, pr);
		return st.toString();
	}

	/**
	 * Prints to PrintWriter names,positions, and other properties of elements, held
	 * in MagnetList.
	 *
	 * @param list a {@link org.scictrl.mp.orbitcorrect.model.optics.MagnetList}
	 *             object
	 * @param pr   a {@link java.io.PrintWriter} object
	 */
	public static void printMagnets(MagnetList list, java.io.PrintWriter pr) {
		pr.println("index\tname\tposition\tR\tk\tm");
		Iterator<AbstractMagnetElement> it = list.iterator();
		AbstractMagnetElement el;
		while (it.hasNext()) {
			el = it.next();
			if (el instanceof AbstractBending) {
				pr.print(el.getIndex());
				pr.print("\t");
				pr.print(el.getName());
				pr.print("\t");
				pr.print(el.getPosition());
				pr.print("\t");
				pr.print(((AbstractBending) el).getRadius());
				pr.print("\t");
				pr.print(((AbstractBending) el).getQuadrupoleStrength());
				pr.println("\tN/D");
			} else if (el instanceof Quadrupole) {
				pr.print(el.getIndex());
				pr.print("\t");
				pr.print(el.getName());
				pr.print("\t");
				pr.print(el.getPosition());
				pr.print("\tN/D\t");
				pr.print(((Quadrupole) el).getQuadrupoleStrength());
				pr.println("\tN/D");
			} else if (el instanceof Sextupole) {
				pr.print(el.getIndex());
				pr.print("\t");
				pr.print(el.getName());
				pr.print("\t");
				pr.print(el.getPosition());
				pr.print("\tN/D\tN/D\t");
				pr.print(((Sextupole) el).getSextupoleStrength());
				pr.println();
			}
		}
	}

	/**
	 * Returns String of names of elements in TransferList, their position in
	 * storage ring and their matrices.
	 *
	 * @return java.lang.String
	 * @param list TransferList
	 */
	public static String printMatrices(TransferList list) {
		java.io.StringWriter st = new java.io.StringWriter();
		java.io.PrintWriter pr = new java.io.PrintWriter(st);
		printMatrices(list, pr);
		return st.toString();
	}

	/**
	 * Prints to PrintWriter names of elements, their position and
	 * matrices.TransferList holds elements.
	 *
	 * @param list TransferList
	 * @param pr   a {@link java.io.PrintWriter} object
	 */
	public static void printMatrices(TransferList list, java.io.PrintWriter pr) {
		Iterator<AbstractTransferElement> it = list.iterator();
		AbstractTransferElement te = null;
		while (it.hasNext()) {
			te = it.next();
			pr.print(te.getName());
			pr.print("\t");
			pr.println(te.getPosition());
			printMatrix(te.getMatrix(), pr);
			pr.println();
		}
	}

	/**
	 * <p>
	 * printMatrix.
	 * </p>
	 *
	 * @param m an array of {@link double} objects
	 * @return a {@link java.lang.String} object
	 */
	public static String printMatrix(double[][] m) {
		java.io.StringWriter st = new java.io.StringWriter();
		java.io.PrintWriter pr = new java.io.PrintWriter(st);
		printMatrix(m, pr);
		return st.toString();
	}

	/**
	 * Prints matrix to PrintWriter.
	 *
	 * @param m  an array of {@link double} matrix
	 * @param pr a {@link java.io.PrintWriter} object
	 */
	public static void printMatrix(double[][] m, java.io.PrintWriter pr) {
		if ((m.length == 0) || (m[0].length == 0)) {
			pr.println("Empty Matrix");
			return;
		}
		int i, j;
		for (i = 0; i < m.length; i++) {
			for (j = 0; j < m[0].length; j++) {
				if (j != 0)
					pr.print("\t");
				pr.print(m[i][j]);
			}
			pr.println();
		}
	}

	/**
	 * Returns String of matrix elements.
	 *
	 * @param m an array of {@link double} matrix
	 * @return a {@link java.lang.String} object
	 */
	public static String printMatrix(double[] m) {
		java.io.StringWriter st = new java.io.StringWriter();
		java.io.PrintWriter pr = new java.io.PrintWriter(st);
		printMatrix(m, pr);
		return st.toString();
	}

	/**
	 * <p>
	 * printMatrix.
	 * </p>
	 *
	 * @param m  an array of {@link double} objects
	 * @param pr a {@link java.io.PrintWriter} object
	 */
	public static void printMatrix(double[] m, java.io.PrintWriter pr) {
		if (m.length == 0) {
			pr.println("Empty Array");
			return;
		}
		int i;
		pr.print(m[0]);
		for (i = 1; i < m.length; i++) {
			pr.print("\t");
			pr.print(m[i]);
		}
		pr.println();
	}

	/**
	 * Returns String of matrix elements.
	 *
	 * @param m a {@link org.scictrl.mp.orbitcorrect.math.DoubleMatrix} matrix
	 * @return a {@link java.lang.String} object
	 */
	public static String printMatrix(org.scictrl.mp.orbitcorrect.math.DoubleMatrix m) {
		java.io.StringWriter st = new java.io.StringWriter();
		java.io.PrintWriter pr = new java.io.PrintWriter(st);
		printMatrix(m, pr);
		return st.toString();
	}

	/**
	 * Prints matrix to PrintWriter.
	 *
	 * @param m  a {@link org.scictrl.mp.orbitcorrect.math.DoubleMatrix} matrix
	 * @param pr a {@link java.io.PrintWriter} object
	 */
	public static void printMatrix(org.scictrl.mp.orbitcorrect.math.DoubleMatrix m, java.io.PrintWriter pr) {
		if ((m.columns() == 0) || (m.rows() == 0)) {
			pr.println("Empty Matrix");
			return;
		}
		pr.print(m.toString());
	}

	/**
	 * <p>
	 * toStringBasics.
	 * </p>
	 *
	 * @return java.lang.String
	 * @param list a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList}
	 *             object
	 */
	public static String toStringBasics(ElementList<AbstractDataBushElement> list) {

		StringBuffer s = new StringBuffer();

		s.append("<template=powersupply virtual= true >\r\n");
		s.append("<template=marker virtual= true >\r\n");
		s.append("<template=bpm virtual= true >\r\n");
		s.append("<template=cavity virtual= true >\r\n");
		s.append("<template=septum virtual= true >\r\n");
		s.append("<template=kicker virtual= true >\r\n");
		s.append("<template=horcorrector virtual= true >\r\n");
		s.append("<template=vercorrector virtual= true >\r\n");
		s.append("<template=sbending virtual= true >\r\n");
		s.append("<template=rbending virtual= true >\r\n");
		s.append("<template=quadrupole virtual= true >\r\n");
		s.append("<template=sextupole virtual= true >\r\n\r\n");

		for (AbstractDataBushElement el : list) {
			s.append("<");
			s.append(DBElementDescriptor.ELEMENT_TAG);
			s.append("=");
			s.append(el.descriptor().getElementClassTag());
			s.append(" ");
			s.append(el.descriptor().getParameterTag(AbstractDataBushElement.PR_ELEMENT_NAME));
			s.append("=\"");
			s.append(el.getName());
			s.append("\" ");
			if (el instanceof AbstractOpticalElement) {
				s.append(el.descriptor().getParameterTag(AbstractOpticalElement.PR_POSITION));
				s.append("=");
				s.append(((AbstractOpticalElement) el).getPosition());
				s.append(" ");
				s.append(el.descriptor().getParameterTag(AbstractOpticalElement.PR_REL_POSITION));
				s.append("=");
				s.append(((AbstractOpticalElement) el).getRelPosition());
				s.append(" ");
				s.append(el.descriptor().getParameterTag(AbstractOpticalElement.PR_REL_FROM));
				s.append("=\"");
				s.append(((AbstractOpticalElement) el).getRelFrom());
				s.append("\" ");
			}
			if (el instanceof AbstractTransferElement) {
				s.append(el.descriptor().getParameterTag(AbstractTransferElement.PR_LENGTH));
				s.append("=");
				s.append(((AbstractTransferElement) el).getLength());
				s.append(" ");
			}
			if (el instanceof AbstractBending) {
				s.append(el.descriptor().getParameterTag(AbstractBending.PR_RADIUS));
				s.append("=");
				s.append(((AbstractBending) el).getRadius());
				s.append(" ");
			}
			if (el instanceof AbstractMagnetElement) {
				s.append(el.descriptor().getParameterTag(AbstractMagnetElement.PR_POWER_SUPPLY));
				s.append("=\"");
				s.append(((AbstractMagnetElement) el).getPS());
				s.append("\" ");
			}
			s.append(">\r\n");
		}
		return s.toString();
	}

	private Utilities() {
	}
}
