package org.scictrl.mp.orbitcorrect.correction.models;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.scictrl.mp.orbitcorrect.DBMath;
import org.scictrl.mp.orbitcorrect.IConfigurable;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.accessories.Utilities;
import org.scictrl.mp.orbitcorrect.correction.Correction;
import org.scictrl.mp.orbitcorrect.correction.ISVDOrbitCorrectionModel;
import org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
import org.scictrl.mp.orbitcorrect.utilities.Orbit;
import org.scictrl.mp.orbitcorrect.utilities.ResponseMatrix;
import org.scictrl.mp.orbitcorrect.utilities.SVDMethod;
/**
 * <p>Abstract AbstractSVDBasedModel class.</p>
 *
 * @author igor@scictrl.com
 */
public abstract class AbstractSVDBasedModel extends AbstractOCModel implements ISVDOrbitCorrectionModel, IConfigurable {
	private static final String PROPERTY_OC_SVDMODEL_MIN_EIGENVALUE = "oc.svdmodel.minEigenvalue";

	/** Calculated max correction */
	protected double maxCorrection=0.0;
	/** Calculated Beam RMS theoretical prediction */
	protected double theoreticalBeamRMS = 0.0;
	/** Calculated correctors RMS */
	protected double correctorRMS = 0.0;
	/** Calculates eigenvalues */
	protected double[] eigenvalues = new double[0];
	/** Minimal eigenvalues threshold */
	protected double minimalEigenvalue = 1.0;
	/** Calculated eigenvectors used */
	protected int eigenvectorsUsed = 0;
	/** Strength reduction enabled */
	protected boolean reductionEnabled=false;
	/** Strength reduction scale */
	protected double reductionScale = 1.0;
	/** Calculated strength reduction corrector change */
	protected double reductedCorrectorChange = 0.0;
	/** Calculated strength reduction BPM change */
	protected double reductedBPMChange = 0.0;
	/** Minimal eigenvalues threshold for strength reduction */
	protected double minimalReductionEigenvalue = 1.0;
	/** Calculated eigenvectors used for strength reduction */
	protected int reductionEigenvectorsUsed = 0;
	/** Eigenvectors used for strength reduction  */
	protected double[] reductionEigenvalues;


	/**
	 * RingKeeper constructor comment.
	 */
	public AbstractSVDBasedModel() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	public double getMinimalEigenvalue() {
		return minimalEigenvalue;
	}

	/** {@inheritDoc} */
	@Override
	public int getEigenvectorsUsed() {
		return eigenvectorsUsed;
	}

	/** {@inheritDoc} */
	@Override
	public double[] getEigenvalues() {
		return eigenvalues;
	}
	/**
	 * makeCorrection method comment.
	 *
	 * @param monitors a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @param correctors a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @param responseMatrix a {@link org.scictrl.mp.orbitcorrect.utilities.ResponseMatrix} object
	 * @param correction an array of {@link double} objects
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 */
	protected void addReduction(ElementList<BPMonitor> monitors, ElementList<AbstractCorrector> correctors, ResponseMatrix responseMatrix, double[] correction) throws InconsistentDataException {

	// test lists

	//	if (!getDataBush().hasClosedOrbitSolution()) throw new InconsistentDataException("DataBush has no closed orbit solution!");

		int cs = correctors.size();
		int ms= monitors.size();
		double[] b = new double[ms];
		double[] c = new double[cs];
		int i = -1;
		double a=0.0;

	// calculate current correctors RMS
		Iterator<AbstractCorrector> it = correctors.iterator();
		i=0;
		a=0.0;
		while (it.hasNext()) a+= DBMath.sqr(c[i++]=it.next().getAngle());
		correctorRMS=Math.sqrt(a/cs);


	// get theoretical orbit
		b= DBMath.multiply(responseMatrix.matrix,c);


	// calculate RMS for theoretical orbit
		a=0.0;
		for (i=0; i<ms; a+= DBMath.sqr(b[i++]));
		theoreticalBeamRMS= Math.sqrt(a/ms);

		double[][] U = DBMath.clone(responseMatrix.matrix);

		reductionEigenvalues = new double[cs];
		double[][] V = new double[cs][cs];

	// make SVD
	    try {
		    SVDMethod.svdcmp(U, reductionEigenvalues, V);
	    } catch (Throwable e) {

	    	e.printStackTrace();
	    	System.out.println("SVD error");
	    	System.out.println("U matrix");
			System.out.println(Utilities.printMatrix(U));
	    	System.out.println("reductionEigenvalues");
			System.out.println(Utilities.printMatrix(reductionEigenvalues));
	    	System.out.println("V matrix");
			System.out.println(Utilities.printMatrix(V));

			throw new InconsistentDataException("SVD error: "+e.toString());
	    }

	// correctors eigenvalues
		double[] cein= DBMath.multiply(DBMath.transpond(V),c);



	// exclude to small c eigenvalues
		reductionEigenvectorsUsed=cs;
		for (i=0; i<cs; i++) {
			if (reductionEigenvalues[i]<minimalReductionEigenvalue) {
				cein[i]= 0.0;
				reductionEigenvectorsUsed--;
			}
		}



	// calculate new corrector setting
		double[] correctionData= DBMath.multiply(V,cein);

	// calculate RMS for predicted correctors
		a=0.0;
		for (i=0; i<cs; i++) a+= DBMath.sqr(correctionData[i]);
		double predictedCorrectorRMS=Math.sqrt(a/cs);
		reductedCorrectorChange= (predictedCorrectorRMS-correctorRMS)/correctorRMS;

	// calculate theoretical predicted orbit
		b= DBMath.multiply(responseMatrix.matrix,correctionData);

		a=0.0;
		for (i=0; i<ms; a+= DBMath.sqr(b[i++]));
		double predictedBeamRMS=Math.sqrt(a/ms);
		reductedBPMChange= (predictedBeamRMS-theoreticalBeamRMS)/theoreticalBeamRMS;

	// calculate correction

		double maxCorrection=0.0;
		for (i = 0; i < cs; i++) {
			correctionData[i] = correctionData[i]-c[i];
			if (Math.abs(correctionData[i])>maxCorrection) maxCorrection= Math.abs(correctionData[i]);
		}

	// pack correction

		for (i=0; i< correctionData.length;i++) {
	//		i= correctors.indexOf(correction[ii].getTarget());
			correction[i] = correction[i]+reductionScale*correctionData[i];
		}

	}
	/**
	 * <p>Getter for the field <code>minimalReductionEigenvalue</code>.</p>
	 *
	 * @return double
	 */
	public double getMinimalReductionEigenvalue() {
		return minimalReductionEigenvalue;
	}
	/**
	 * <p>isReductionEnabled.</p>
	 *
	 * @return boolean
	 */
	public boolean isReductionEnabled() {
		return reductionEnabled;
	}
	/**
	 * <p>makeSVDInversion.</p>
	 *
	 * @param B an array of {@link double} objects
	 * @param responseMatrix a {@link org.scictrl.mp.orbitcorrect.utilities.ResponseMatrix} object
	 * @param correction an array of {@link double} objects
	 * @param db a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} object
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 */
	protected void makeSVDInversion(double[] B, ResponseMatrix responseMatrix, double[] correction, DataBush db) throws InconsistentDataException {


		int ms = B.length;
		int cs = responseMatrix.correctors.length;
		double[][] U = new double[ms][cs];

		if (correction.length!=cs) {
			throw new IllegalArgumentException("Number of correctors and corrections does not match!");
		}



		U= DBMath.clone(responseMatrix.matrix);
	//	testMethod(U);

		/*
		 * Until better idea, we don't print anything.
		 */
		PrintWriter pw= new PrintWriter(new Writer() {

			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
			}

			@Override
			public void flush() throws IOException {
			}

			@Override
			public void close() throws IOException {
			}
		});

		if (debug) {
			pw.println();
			pw.println("DEFAULT CORRECTION");
			pw.println();
			pw.println("TARGET "+B.length);
			Utilities.printMatrix(B,pw);
			pw.println();
			pw.println("RM "+U.length+" "+U[0].length);
			Utilities.printMatrix(U,pw);
			pw.println();
		}

		eigenvalues = new double[cs];
		double[][] V = new double[cs][cs];

	    try {
		    SVDMethod.svdcmp(U, eigenvalues, V);
	    } catch (Throwable e) {

	    	e.printStackTrace();
	    	System.out.println("SVD error");
	    	System.out.println("U matrix");
			System.out.println(Utilities.printMatrix(U));
	    	System.out.println("eigenvalues");
			System.out.println(Utilities.printMatrix(eigenvalues));
	    	System.out.println("V matrix");
			System.out.println(Utilities.printMatrix(V));

			throw new InconsistentDataException("SVD error: "+e.toString());
	    }

	    if (debug) {
			pw.println("EIGENVALUES "+eigenvalues.length);
			Utilities.printMatrix(eigenvalues,pw);
			pw.println();
	    }

		eigenvectorsUsed=0;
		double[] tmp = new double[cs];
		for (int j = 0; j < cs; j++) {
			double a = 0.0;
			if (eigenvalues[j] >= minimalEigenvalue) {
				for (int i = 0; i < ms; i++)
					a += U[i][j] * B[i];
				a /= eigenvalues[j];
				eigenvectorsUsed++;
			}
			tmp[j] = a;
		}

		if (debug) {
			pw.println("TMP "+tmp.length);
			Utilities.printMatrix(tmp,pw);
			pw.println();
		}

		maxCorrection=0.0;
		for (int j = 0; j < cs; j++) {
			correction[j]=0.0;
			for (int i = 0; i < cs; i++)
				correction[j] = correction[j] + V[j][i] * tmp[i];
			if (Math.abs(correction[j])>maxCorrection)
				maxCorrection= Math.abs(correction[j]);
		}

	    if (debug) {
			pw.println("CORRECTION "+correction.length);
			for (double element : correction) {
				pw.print(element+" ");
			}
			pw.println();
			pw.flush();
	    }


	}
	/** {@inheritDoc} */
	@Override
	public void setMinimalEigenvalue(double newMinimalEigenvalue) {
		if (minimalEigenvalue != newMinimalEigenvalue) {
			minimalEigenvalue = newMinimalEigenvalue;
		}
	}
	/**
	 * <p>Setter for the field <code>minimalReductionEigenvalue</code>.</p>
	 *
	 * @param newMinimalReductionEigenvalue double
	 */
	public void setMinimalReductionEigenvalue(double newMinimalReductionEigenvalue) {
		minimalReductionEigenvalue = newMinimalReductionEigenvalue;
	}
	/**
	 * <p>Setter for the field <code>reductionEnabled</code>.</p>
	 *
	 * @param newReductionEnabled boolean
	 */
	public void setReductionEnabled(boolean newReductionEnabled) {
		reductionEnabled = newReductionEnabled;
	}
	/**
	 * <p>Setter for the field <code>reductionScale</code>.</p>
	 *
	 * @param newReductionScale double
	 */
	public void setReductionScale(double newReductionScale) {
		reductionScale = newReductionScale;
	}

	/** {@inheritDoc} */
	@Override
	public void configure(Configuration conf) {
		if (conf.containsKey(PROPERTY_OC_SVDMODEL_MIN_EIGENVALUE+"."+getOrientation().getShortName())) {
			minimalEigenvalue= conf.getDouble(PROPERTY_OC_SVDMODEL_MIN_EIGENVALUE+"."+getOrientation().getShortName());
		} else {
			minimalEigenvalue= conf.getDouble(PROPERTY_OC_SVDMODEL_MIN_EIGENVALUE, 1.0);
		}


	}

	/**
	 * <p>makeCorrection.</p>
	 *
	 * @param correctors a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @param orbit a {@link org.scictrl.mp.orbitcorrect.utilities.Orbit} object
	 * @param responseMatrix a {@link org.scictrl.mp.orbitcorrect.utilities.ResponseMatrix} object
	 * @param db a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} object
	 * @param op a {@link org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator} object
	 * @return a {@link org.scictrl.mp.orbitcorrect.correction.Correction} object
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 */
	abstract protected Correction makeCorrection(ElementList<AbstractCorrector> correctors, Orbit orbit, ResponseMatrix responseMatrix, DataBush db, OrbitCorrectionOperator op) throws InconsistentDataException;

}


