package org.scictrl.mp.orbitcorrect.utilities;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;

import org.scictrl.csshell.Timestamp;
import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.DBMath;
import org.scictrl.mp.orbitcorrect.Orientation;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;

/**
 * Orbit is class that incapsulates orbit at some timepoint. For execution efficiency it is by design non-modifiable class.
 * Orbit information is provided trough constructor and stored in final arrays and should not be changed afterwards.
 *
 * @author igor@kriznar.com
 */
public final class Orbit {
	final ElementList<BPMonitor> bpms;
	final double[][] positions= new double[2][];
	final double[] avg= new double[2];
	final double[] rms= new double[2];
	final double[] std= new double[2];
	final double[] max= new double[2];
	@SuppressWarnings("unused")
	final private Orbit ref;
	final private boolean relative;
	private Timestamp time;

	/**
	 * Creates new Orbit object from BPMs and reference.
	 *
	 * @param bpms the orbit provided as BPM objects.
	 * @param ref the reference, can be <code>null</code>.
	 */
	public Orbit(ElementList<BPMonitor> bpms, Orbit ref) {
		this.time= new Timestamp();
		this.bpms=bpms;
		this.ref=ref;
		this.relative=ref!=null;
		positions[Orientation._H]= new double[bpms.size()];
		positions[Orientation._V]= new double[bpms.size()];

		Iterator<BPMonitor> it= bpms.iterator();
		BPMonitor bpm;
		double x,z;
		int i=0;

		while (it.hasNext()) {
			bpm= it.next();
			x= bpm.getBeamPos().x();
			z= bpm.getBeamPos().z();
			positions[Orientation._H][i]= (relative) ? x-ref.positions[Orientation._H][i] : x;
			positions[Orientation._V][i]= (relative) ? z-ref.positions[Orientation._V][i] : z;
			i++;
		}
		init();
	}

	/**
	 * Creates new orbit from raw orbit and reference orbit.
	 *
	 * @param raw the raw orbit information
	 * @param ref the reference for the orbit
	 */
	public Orbit(Orbit raw, Orbit ref) {
		this.time= new Timestamp();
		this.bpms=raw.bpms;
		this.ref=ref;
		this.relative=ref!=null;
		positions[Orientation._H]= new double[bpms.size()];
		positions[Orientation._V]= new double[bpms.size()];

		if (relative) {
			for (int i = 0; i < bpms.size(); i++) {
				positions[Orientation._H][i]= raw.positions[Orientation._H][i]-ref.positions[Orientation._H][i];
				positions[Orientation._V][i]= raw.positions[Orientation._V][i]-ref.positions[Orientation._V][i];
			}
		} else {
			System.arraycopy(raw.positions[Orientation._H], 0, positions[Orientation._H], 0, positions[Orientation._H].length);
			System.arraycopy(raw.positions[Orientation._V], 0, positions[Orientation._V], 0, positions[Orientation._V].length);
		}

		init();
	}

	/**
	 * <p>Constructor for Orbit.</p>
	 *
	 * @param bpms a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @param x an array of {@link double} objects
	 * @param z an array of {@link double} objects
	 */
	public Orbit(ElementList<BPMonitor> bpms, double[] x, double[] z) {
		this.time= new Timestamp();
		this.bpms=bpms;
		this.ref=null;
		this.relative=false;
		positions[Orientation._H]= x;
		positions[Orientation._V]= z;

		init();
	}
	private void init() {

		double x,z;
		int i=0;

		int count=bpms.size();

		if (count<2) {
			avg[Orientation._H]= Double.NaN;
			rms[Orientation._H]= Double.NaN;
			std[Orientation._H]= Double.NaN;
			max[Orientation._H]= Double.NaN;
			avg[Orientation._V]= Double.NaN;
			rms[Orientation._V]= Double.NaN;
			std[Orientation._V]= Double.NaN;
			max[Orientation._V]= Double.NaN;
			return;
		} else {
			avg[Orientation._H]= 0.0;
			rms[Orientation._H]= 0.0;
			std[Orientation._H]= 0.0;
			max[Orientation._H]= 0.0;
			avg[Orientation._V]= 0.0;
			rms[Orientation._V]= 0.0;
			std[Orientation._V]= 0.0;
			max[Orientation._V]= 0.0;
		}

		for (;i<count;i++) {
			x = positions[Orientation._H][i];
			z = positions[Orientation._V][i];

			if (Math.abs(x)>Math.abs(max[Orientation._H])) max[Orientation._H]= x;
			if (Math.abs(z)>Math.abs(max[Orientation._V])) max[Orientation._V]= z;

			avg[Orientation._H]+=x;
			avg[Orientation._V]+=z;

			rms[Orientation._H]+=DBMath.sqr(x);
			rms[Orientation._V]+=DBMath.sqr(z);
		}

		if (count>1) {
			avg[Orientation._H]/=count;
			rms[Orientation._H]/=count;
			std[Orientation._H]=Math.sqrt(Math.abs(rms[Orientation._H]-DBMath.sqr(avg[Orientation._H])));
			rms[Orientation._H]=Math.sqrt(rms[Orientation._H]);

			avg[Orientation._V]/=count;
			rms[Orientation._V]/=count;
			std[Orientation._V]=Math.sqrt(Math.abs(rms[Orientation._V]-DBMath.sqr(avg[Orientation._V])));
			rms[Orientation._V]=Math.sqrt(rms[Orientation._V]);
		}
	}

	/**
	 * <p>Getter for the field <code>positions</code>.</p>
	 *
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @return an array of {@link double} objects
	 */
	public double[] getPositions(Orientation ori) {
		return positions[ori.ordinal()];
	}
	/**
	 * <p>Getter for the field <code>avg</code>.</p>
	 *
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @return a double
	 */
	public double getAvg(Orientation ori) {
		return avg[ori.ordinal()];
	}
	/**
	 * <p>Getter for the field <code>rms</code>.</p>
	 *
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @return a double
	 */
	public double getRms(Orientation ori) {
		return rms[ori.ordinal()];
	}
	/**
	 * <p>Getter for the field <code>std</code>.</p>
	 *
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @return a double
	 */
	public double getStd(Orientation ori) {
		return std[ori.ordinal()];
	}
	/**
	 * <p>Getter for the field <code>max</code>.</p>
	 *
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @return a double
	 */
	public double getMax(Orientation ori) {
		return max[ori.ordinal()];
	}
	/**
	 * <p>getBPMs.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public ElementList<BPMonitor> getBPMs() {
		return bpms;
	}

	/**
	 * <p>toStringStatistics.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String toStringStatistics() {

		StringWriter sw= new StringWriter(1024);
		PrintWriter pw= new PrintWriter(sw);
		pw.print(time);
		pw.print(" ");
		printCompactStats(pw);
		pw.close();
		return sw.toString();
	}

	/**
	 * <p>toStringFull.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String toStringFull() {
		StringWriter sw= new StringWriter(1024);
		PrintWriter pw= new PrintWriter(sw);
		printCompact(pw);
		pw.close();
		return sw.toString();
	}

	/**
	 * <p>printCompact.</p>
	 *
	 * @param pw a {@link java.io.PrintWriter} object
	 */
	public void printCompact(PrintWriter pw) {
		pw.print(time);
		pw.print(" x:");
		pw.print(Arrays.toString(positions[0]));
		pw.print(" z:");
		pw.print(Arrays.toString(positions[1]));
		pw.print(" ");
		printCompactStats(pw);
	}

	/**
	 * <p>printCompactStats.</p>
	 *
	 * @param pw a {@link java.io.PrintWriter} object
	 */
	public void printCompactStats(PrintWriter pw) {
		pw.print("avg: ");
		pw.print(DBConst.FORMAT_F4.format(avg[0]));
		pw.print(",");
		pw.print(DBConst.FORMAT_F4.format(avg[1]));
		pw.print(" rms:");
		pw.print(DBConst.FORMAT_F4.format(rms[0]));
		pw.print(",");
		pw.print(DBConst.FORMAT_F4.format(rms[1]));
		pw.print(" std:");
		pw.print(DBConst.FORMAT_F4.format(std[0]));
		pw.print(",");
		pw.print(DBConst.FORMAT_F4.format(std[1]));
	}

	/**
	 * <p>print.</p>
	 *
	 * @param pw a {@link java.io.PrintWriter} object
	 */
	public void print(PrintWriter pw) {
		pw.print("Orbit on ");
		pw.println(time);
		pw.print("x:");
		pw.print(Arrays.toString(positions[0]));
		pw.println();
		pw.print("z:");
		pw.print(Arrays.toString(positions[1]));
		pw.println();
		pw.print("avg: ");
		pw.print(DBConst.FORMAT_F4.format(avg[0]));
		pw.print(",");
		pw.print(DBConst.FORMAT_F4.format(avg[1]));
		pw.println();
		pw.print(" rms:");
		pw.print(DBConst.FORMAT_F4.format(rms[0]));
		pw.print(",");
		pw.print(DBConst.FORMAT_F4.format(rms[1]));
		pw.println();
		pw.print(" std:");
		pw.print(DBConst.FORMAT_F4.format(std[0]));
		pw.print(",");
		pw.print(DBConst.FORMAT_F4.format(std[1]));
		pw.println();
	}

	/**
	 * <p>Getter for the field <code>time</code>.</p>
	 *
	 * @return a {@link org.scictrl.csshell.Timestamp} object
	 */
	public Timestamp getTime() {
		return time;
	}

	/**
	 * <p>getPosition.</p>
	 *
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @param i a int
	 * @return a double
	 */
	public double getPosition(Orientation ori,int i) {
		return positions[ori.ordinal()][i];
	}

	/**
	 * <p>getPosition.</p>
	 *
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @param bpm a {@link java.lang.String} object
	 * @return a double
	 */
	public double getPosition(Orientation ori,String bpm) {
		return positions[ori.ordinal()][bpms.indexOf(bpm)];
	}
}
