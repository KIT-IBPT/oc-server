package org.scictrl.mp.orbitcorrect.utilities;

import java.util.ArrayList;
import java.util.Iterator;

import org.scictrl.mp.orbitcorrect.Orientation;
import org.scictrl.mp.orbitcorrect.accessories.FileTokenizer;
import org.scictrl.mp.orbitcorrect.accessories.Utilities;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
/**
 * Response matrix of closed orbit
 *
 * @author igor@kriznar.com
 */
public final class ResponseMatrix {
	/** Corrector magnet names in matrix. */
	public final String[] correctors;
	/** BPM names in matrix. */
	public final String[] bpms;
	/** The response matrix. */
	public final double[][] matrix;
	/** If values are valid. */
	public boolean valid=false;
	/** Energy of beam. */
	public double energy;
	/** Time duration of matrix calculation in ms. */
	public long time;
	/**
	 * Creates RM with all arrays and matrix initialized to provided dimensions, but with no data.
	 *
	 * @param rowCount the number of BPMs in RM
	 * @param columnCount the number of Correctors in RM
	 */
	public ResponseMatrix(int rowCount, int columnCount) {
		bpms= new String[rowCount];
		correctors= new String[columnCount];
		matrix= new double[rowCount][columnCount];
	}
	/**
	 * Creates new instance of ResponseMatrix from the specified file.
	 *
	 * @param file a {@link java.io.File} object
	 * @throws java.text.ParseException if any.
	 * @throws java.io.IOException if any.
	 */
	public ResponseMatrix(java.io.File file)
	    throws java.text.ParseException, java.io.IOException {
	    FileTokenizer tok = null;

	    tok = new FileTokenizer(file);
	    tok.wordChars('_', '_');
	    tok.eolIsSignificant(true);
	    tok.lowerCaseMode(false);
	    tok.slashSlashComments(true);
	    tok.slashStarComments(true);
	    tok.commentChar('#');
	    ArrayList<String> list = new ArrayList<>();

	    while (tok.nextToken() == FileTokenizer.TT_EOL) {}

	    //tok.nextToken();
	    while ((tok.ttype != FileTokenizer.TT_EOL) && (tok.ttype != FileTokenizer.TT_EOF)) {
	        if (tok.ttype != FileTokenizer.TT_WORD)
	            throw new java.text.ParseException(
	                "Not a valid response matrix file, corrector name expected instead of: "
	                    + tok.toString(),
	                tok.lineno());
	        //System.out.println(tok.sval);
	        list.add(tok.sval);
	        tok.nextToken();
	    }

	    if (tok.ttype == FileTokenizer.TT_EOF)
	        throw new java.text.ParseException(
	            "Not a valid response matrix file, unexpected end of file!",
	            tok.lineno());

	    correctors = new String[list.size()];
	    for (int i = 0; i < list.size(); i++)
	        correctors[i] = list.get(i).toString();
	    list.clear();
	    ArrayList<double[]> data = new ArrayList<>();
	    double[] a;

	    tok.nextToken();
	    while ((tok.ttype != FileTokenizer.TT_EOL) && (tok.ttype != FileTokenizer.TT_EOF)) {
	        if (tok.ttype != FileTokenizer.TT_WORD)
	            throw new java.text.ParseException(
	                "Not a valid response matrix file, bpm name expected instead of: "
	                    + tok.toString(),
	                tok.lineno());
	        list.add(tok.sval);
	        a = new double[correctors.length];

	        for (int i = 0; i < correctors.length; i++) {
	            if (tok.nextToken() == FileTokenizer.TT_WORD) {
	                a[i] = Double.valueOf(tok.sval).doubleValue();
	            } else
	                if (tok.ttype == FileTokenizer.TT_NUMBER) {
	                    a[i] = tok.nval;
	                } else
	                    throw new java.text.ParseException(
	                        "Not a valid response matrix file, number expected instead of: "
	                            + tok.toString(),
	                        tok.lineno());

	        }
	        data.add(a);
	        if ((tok.nextToken() != FileTokenizer.TT_EOL) && (tok.ttype != FileTokenizer.TT_EOF)) {
	        	/*System.out.println(java.util.Arrays.asList(correctors));
	        	System.out.println(list.toString());
	        	java.util.Iterator it= data.iterator();
	        	while (it.hasNext()) {
		        	a= (double[])it.next();
		        	for (int i=0;i<a.length;i++) {
		        		System.out.print(a[i]+",");
		        	}
		        	System.out.println();
	        	}
	        	System.out.println(data.toString());*/
	            throw new java.text.ParseException(
	                "Not a valid response matrix file, EOF or EOL expected instead of: "
	                    + tok.toString(),
	                tok.lineno());
	        }
	        if (tok.ttype != FileTokenizer.TT_EOF)
	            tok.nextToken();
	    }

	    bpms = new String[list.size()];
	    matrix = new double[bpms.length][];

	    for (int i = 0; i < bpms.length; i++) {
	        matrix[i] = data.get(i);
	        bpms[i] = list.get(i).toString();
	    }

	    valid=true;

	}

	/**
	 * Creates new RM which has dimensions and device names initialized with provided parameters.
	 * The matrix itself is of right dimension but with 0.0 values.
	 *
	 * @param bpms the BPMs defining the RM
	 * @param correctors the Correctors defining the RM
	 */
	public ResponseMatrix(ElementList<BPMonitor> bpms, ElementList<AbstractCorrector> correctors) {
		this(bpms.size(),correctors.size());

		int i=0;
		Iterator<BPMonitor> it= bpms.iterator();
		while (it.hasNext()) this.bpms[i++]= it.next().getName();

		i=0;
		Iterator<AbstractCorrector> cit= correctors.iterator();
		while (cit.hasNext()) this.correctors[i++]= cit.next().getName();
	}

	/**
	 * Creates new RM which has dimensions and device names initialized with provided parameters.
	 * The matrix itself is of right dimension but with 0.0 values.
	 *
	 * @param bpms the BPMs defining the RM
	 * @param correctors the Correctors defining the RM
	 */
	public ResponseMatrix(String[] bpms, final String[] correctors) {
		this(bpms.length,correctors.length);

		System.arraycopy(bpms, 0, this.bpms, 0, bpms.length);
		System.arraycopy(correctors, 0, this.correctors, 0, correctors.length);
	}

	/**
	 * Fills the provided RM with closed-orbit calculated matrix data.
	 *
	 * @param rm the RM to be filled with data
	 * @param dataBush the DataBush to provide calculation
	 * @param orientation the Orientation for which calculation should be done
	 * @return a {@link org.scictrl.mp.orbitcorrect.utilities.ResponseMatrix} object
	 * @throws java.lang.IllegalStateException if any.
	 */
	public static final ResponseMatrix fillWithCloseOrbitCalculation(final ResponseMatrix rm, final DataBush dataBush, Orientation orientation) throws IllegalStateException {

		if(dataBush.isStatusEmpty()) {
			throw new IllegalStateException("Databush has not yet bean initialized!");
		}
		long t= System.currentTimeMillis();

		synchronized (dataBush.getSynchronizationLock()) {

			rm.energy=dataBush.getDataBushInfo().getEnergy();

			final double q = read(dataBush.getQ(),orientation);
			final int ms = rm.bpms.length;
			final int cs = rm.correctors.length;
			final double[][] U = rm.matrix;

			for (int i=0; i<ms; i++) {
				AbstractOpticalElement elM = dataBush.getOptics().get(rm.bpms[i]);

				double b = Math.sqrt(read(elM.getBeta(),orientation)) / 2.0 / Math.sin(q * Math.PI); // 1000.0;

				for (int j=0; j<cs; j++) {
					AbstractOpticalElement el = dataBush.getOptics().get(rm.correctors[j]);
					U[i][j] = b * Math.sqrt(read(el.getBeta(),orientation)) * Math.cos((Math.abs(read(elM.getQ(),orientation) - read(el.getQ(),orientation)) * 2.0 - q) * Math.PI);
				}
			}

		//	testMethod(U);
			rm.valid=true;
			rm.time=System.currentTimeMillis()-t;

			dataBush.getLog().debug("UPDATE RM "+rm.time);
		}
		return rm;
	}

	/**
	 * Returns <code>true</code> if this RM contains valid RM data. By default new RM instance is created with <code>false</code>.
	 *
	 * @return <code>true</code> if this RM contains valid RM data
	 */
	public boolean isValid() {
		return valid;
	}
/**
 * <p>main.</p>
 *
 * @param args java.lang.String[]
 */
public static void main(String[] args) {

	try {

		ResponseMatrix rm = new ResponseMatrix(new java.io.File("U:\\Storage Ring\\ORBITS\\Response_Matrix\\2004\\RMH_cser_fill554_5ticks_0_01mrad"));
		System.out.println(rm.toString());
	} catch (Exception e) {
		e.printStackTrace();
	}
}
/**
 * Prints this ResponceMatrix to the specified file. Data is formated in table. In first row are
 * names of correctors, in first column names of beam position monitors. Items in line are separated
 * with tabulators.
 *
 * @param file a {@link java.io.File} object
 * @throws java.io.IOException if any.
 */
public void printToFile(java.io.File file) throws java.io.IOException {
	printToFile(null,file);
}
/**
 * Prints this ResponceMatrix to the specified file. Data is formated in table. In first row are
 * names of correctors, in first column names of beam position monitors. Items in line are separated
 * with tabulators.
 *
 * @param label a {@link java.lang.String} object
 * @param file a {@link java.io.File} object
 * @throws java.io.IOException if any.
 */
public void printToFile(String label, java.io.File file) throws java.io.IOException {
	java.io.PrintWriter wr= null;
	try {
		wr=  new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(file)));
		if (label!=null) wr.println(label);
		printToStream(wr);
	} catch (java.io.IOException e) {
		throw e;
	} finally {
		try {
			if (wr!=null) wr.close();
		} catch (Exception e1) {e1.printStackTrace();}
	}
}
/**
 * Prints this ResponceMatrix to the specified file. Data is formated in table. In first row are
 * names of correctors, in first column names of beam position monitors. Items in line are separated
 * with tabulators.
 *
 * @param print a {@link java.io.PrintWriter} object
 * @throws java.io.IOException if any.
 */
public void printToStream(java.io.PrintWriter print) throws java.io.IOException {
	int i=0;
	int j=0;
	print.print("\t");
	for (i=0; i<correctors.length; print.print("\t"+correctors[i++]));
	print.println();
	for (i=0; i<bpms.length; i++) {
		print.print(bpms[i]);
		for (j=0; j<correctors.length; print.print("\t"+matrix[i][j++]));
		print.println();
	}
}
/**
 * Prints this ResponceMatrix to the specified file. Data is formated in table. In first row are
 * names of correctors, in first column names of beam position monitors. Items in line are separated
 * with tabulators.
 *
 * @return a {@link java.lang.String} object
 */
public String printToString() {
	java.io.StringWriter sw= new java.io.StringWriter();
	java.io.PrintWriter wr= new java.io.PrintWriter(sw);
	try {
		printToStream(wr);
	} catch (java.io.IOException e) {
		e.printStackTrace();
	} finally {
		try {
			if (wr!=null) wr.close();
		} catch (Exception e1) {e1.printStackTrace();}
	}
	return sw.toString();
}
/**
 *
 * @return double
 * @param data PositionedData
 * @param orientation int
 */
private static double read(org.scictrl.mp.orbitcorrect.model.optics.PositionedData data, Orientation orientation) {
	if (orientation==Orientation.VERTICAL) return data.z();
	else return data.x();
}
	/**
	 * <p>submatrix.</p>
	 *
	 * @return ResponseMatrix
	 * @param b an array of {@link java.lang.String} objects
	 * @param c an array of {@link java.lang.String} objects
	 * @throws java.lang.IllegalArgumentException if any.
	 */
	public ResponseMatrix submatrix(String[] b, String[] c) throws IllegalArgumentException {

		int ms = b.length;
		int cs = c.length;
		ResponseMatrix rm= new ResponseMatrix(b,c);

		int i = 0;
		int j = 0;
		//int k = 0;

		double[][]tm= new double[ms][];

		for (i=0; i<ms; i++) {
			if ((j=Utilities.lazySearch(this.bpms,b[i]))<0) throw new IllegalArgumentException("BPM "+bpms[i]+" is not found in this ResponseMatrix!");
			tm[i]= this.matrix[j];
		}

		for (i=0; i<cs; i++) {
			if ((j=Utilities.lazySearch(this.correctors,c[i]))<0) throw new IllegalArgumentException("Corrector "+bpms[i]+" is not found in this ResponseMatrix!");
			for (int ii=0; ii< ms; ii++) {
				rm.matrix[ii][i]= tm[ii][j];
			}
		}

		rm.valid=valid;
		return rm;
	}
	/**
	 * <p>submatrix.</p>
	 *
	 * @param b an array of {@link java.lang.String} objects
	 * @return a {@link org.scictrl.mp.orbitcorrect.utilities.ResponseMatrix} object
	 * @throws java.lang.IllegalArgumentException if any.
	 */
	public ResponseMatrix submatrix(String[] b) throws IllegalArgumentException {

		int ms = b.length;
		int cs = correctors.length;
		ResponseMatrix rm= new ResponseMatrix(b,correctors);

		int i = 0;
		int j = 0;
		//int k = 0;

		for (i=0; i<ms; i++) {
			if ((j=Utilities.lazySearch(this.bpms,b[i]))<0) throw new IllegalArgumentException("BPM "+bpms[i]+" is not found in this ResponseMatrix!");
			System.arraycopy(this.matrix[j], 0, rm.matrix[i], 0, cs);
		}

		rm.valid=valid;
		return rm;
	}
/**
 * {@inheritDoc}
 *
 * Returns a String that represents the value of this object.
 */
@Override
public String toString() {
	// Insert code to print the receiver here.
	// This implementation forwards the message to super. You may replace or supplement this.
	return "ResponseMatrix["+((bpms!=null) ? String.valueOf(bpms.length) : "null" )+","+((correctors!=null) ? String.valueOf(correctors.length) : "null" )+"]";
}

}
