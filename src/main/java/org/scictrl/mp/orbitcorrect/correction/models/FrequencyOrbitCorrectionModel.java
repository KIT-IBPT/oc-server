package org.scictrl.mp.orbitcorrect.correction.models;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.scictrl.mp.orbitcorrect.DBMath;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.Orientation;
import org.scictrl.mp.orbitcorrect.accessories.Utilities;
import org.scictrl.mp.orbitcorrect.correction.Correction;
import org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
import org.scictrl.mp.orbitcorrect.utilities.LinearOpticsCalculatorBean;
import org.scictrl.mp.orbitcorrect.utilities.Orbit;
import org.scictrl.mp.orbitcorrect.utilities.ResponseMatrix;
import org.scictrl.mp.orbitcorrect.utilities.SVDMethod;
/**
 * <p>FrequencyOrbitCorrectionModel class.</p>
 *
 * @author igor@scictrl.com
 */
public class FrequencyOrbitCorrectionModel extends AbstractSVDOrbitCorrectionModel {
	private double beamAverage = 0.0;
	private double offset = 0.0;
	private LinearOpticsCalculatorBean linearOpticsCalculator = new LinearOpticsCalculatorBean();
	private double frequencyCorrectionAmplification = 0;
	private boolean useExternalResponseMatrix = false;
	private ResponseMatrix externalResponseMatrix;
	private ResponseMatrix mainResponseMatrix;
	private ResponseMatrix usedResponseMatrix;

	/**
	 * FrequencyOrbitCorrectionModel constructor comment.
	 */
	public FrequencyOrbitCorrectionModel() {
		super();
		name = "Frequency OC";
	}

	/**
	 * <p>addColumnToResponseMatrix.</p>
	 *
	 * @return ResponseMatrix
	 * @param responseMatrix ResponseMatrix
	 * @param correctors a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @param monitors a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @param databush a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} object
	 */
	public ResponseMatrix addColumnToResponseMatrix(ResponseMatrix responseMatrix, ElementList<AbstractCorrector> correctors, ElementList<BPMonitor> monitors, DataBush databush) {
		if (isVertical())
			return responseMatrix;

		long t= System.currentTimeMillis();
		ResponseMatrix rm= new ResponseMatrix(monitors.size(), correctors.size() + 1);

		int k= 0;
		int l= responseMatrix.bpms.length;
		double dispersionValue, nc;

		linearOpticsCalculator.init(databush);
		try {
			linearOpticsCalculator.update();
		} catch (InconsistentDataException e) {
			e.printStackTrace();
		}

		/*
		 * From 27.1.2009
		 * Calculation by the formula (- disp/(mom.comp. * RF-frequency))
		 */
		nc= linearOpticsCalculator.getMomentumCompactFactor();

		for (int i= 0; i < l; i++) {
			int j= 0;
			for (; j < correctors.size(); j++) {
				rm.matrix[i][j]= responseMatrix.matrix[i][j];
				//rm.matrix[i][j]= 0;
			}
			dispersionValue= monitors.get(k).getDispersion().d();
			rm.matrix[i][j]= - dispersionValue * 1000 / nc;
			//rm.matrix[i][j]= 0;

			// dispersionValue[m]*1000=dispersionValue[mm]

			//System.out.println("RF RM index:"+i+"/"+k+" disp:"+dispersionValue+" BPM:"+((BPMonitor) monitors.get(k)).getName()+" MCF:"+nc);

			k++;
		}

		/* version up to 27.1.2009
		double energy= getDataBush().getDataBushInfo().getEnergy();
		double energy0= DBMath.MC2;

		nc= (energy0 / energy) * (energy0 / energy);
		nc= nc - linearOpticsCalculator.getMomentumCompactFactor();

		//System.out.print("RM:");
		for (int i= 0; i < l; i++) {
			int j= 0;
			for (; j < correctors.size(); j++) {
				rm.matrix[i][j]= responseMatrix.matrix[i][j];
				//rm.matrix[i][j]= 0;
			}
			dispersionValue= ((BPMonitor) monitors.get(k)).getDispersion().d();
			rm.matrix[i][j]= -(1 / nc) * dispersionValue * 1000;
			//rm.matrix[i][j]= 0;

			// dispersionValue[m]*1000=dispersionValue[mm]

			k++;
		}
		version up to 27.1.2009 */

		for (int i=0; i< rm.bpms.length; i++) rm.bpms[i]=responseMatrix.bpms[i];

		for (int i = 0; i < responseMatrix.correctors.length; i++) {
			rm.correctors[i]=responseMatrix.correctors[i];
		}
		rm.correctors[rm.correctors.length-1]= databush.getRFGenerator().getName();

		rm.valid=responseMatrix.valid;
		rm.energy=responseMatrix.energy;
		rm.time=responseMatrix.time+System.currentTimeMillis()-t;

		return rm;

	}
	/*public double calculateCharmonicRFFrequency() {
	    double h = Math.rint(getDataBush().getOpticsLength()*getDataBush().getRFGenerator().getFrequency()*1000000.0/DBMath.C); // harmonic number
	    double frev; // revolution frequency in MHz
	    double C = 110.4; // circumference in m

	    double energy = getDataBush().getDataBushInfo().getEnergy();

	    frev = (DBMath.C) * Math.sqrt(1 - ( DBMath.MC2 / energy ) * (DBMath.MC2 / energy ))  / C  / 1000000;

	    return h * frev;
	}*/
	/**
	 * <p>calculateRFFrequencyCorrection.</p>
	 *
	 * @param correction a double
	 * @param databush a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} object
	 * @return a double
	 */
	public double calculateRFFrequencyCorrection(double correction, DataBush databush) {
	    return correction*databush.getRFGenerator().getFrequency();
	}
	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>beamAverage</code>.</p>
	 */
	@Override
	public double getBeamAverage() {
		return beamAverage;
	}
	/**
	 * <p>Getter for the field <code>externalResponseMatrix</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.utilities.ResponseMatrix} object
	 */
	public ResponseMatrix getExternalResponseMatrix() {
		return externalResponseMatrix;
	}
	/**
	 * <p>Getter for the field <code>frequencyCorrectionAmplification</code>.</p>
	 *
	 * @return a double
	 */
	public double getFrequencyCorrectionAmplification() {
		return frequencyCorrectionAmplification;
	}
	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>offset</code>.</p>
	 */
	@Override
	public double getOffset() {
		return offset;
	}
	/**
	 * <p>isUseExternalResponseMatrix.</p>
	 *
	 * @return a boolean
	 */
	public boolean isUseExternalResponseMatrix() {
		return useExternalResponseMatrix;
	}

	/** {@inheritDoc} */
	@Override
	protected Correction makeCorrection(ElementList<AbstractCorrector> correctors, Orbit orbit, ResponseMatrix responseMatrix, DataBush db, OrbitCorrectionOperator op)
			throws InconsistentDataException {

		// TODO do this tests when Operator starts
		//testMonitors(orbit.getBPMs());
	    //testCorrectors(correctors);

	    if (!db.hasClosedOrbitSolution()) {
	        throw new InconsistentDataException("DataBush has no closed orbit solution!");
	    }

		if (!responseMatrix.isValid()) {
			throw new InconsistentDataException("ResponseMatrix is not valid!");
		}


	    int ms = orbit.getBPMs().size();
	    int cs = correctors.size();
	    double[] B = new double[ms];
	    int i = -1;

	    //Iterator<BPMonitor> itM;

	    /*
	     * Until better idea, we don't print anywhere.
	     */
	    @SuppressWarnings("resource")
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

	    beamAverage = 0.0;
	    if (isMakeShift()) {
	        for (i = 0; i < ms; B[i++] = -offset);
	    } else {
	        beamAverage = orbit.getAvg(getOrientation());

	        if (debug) {
				pw.println("AVERAGE "+beamAverage);
				pw.println("OFFSET "+offset);
				pw.println("HOR "+(beamAverage*2 - offset));
				pw.println("VER "+offset);
	        }



			boolean hack= Boolean.getBoolean("oc.hack.horf");

			double a=0.0;
	        if (!isModelCalculatesOffset()) {
				if (getOrientation()==Orientation.HORIZONTAL) {
					if (hack) {
						a= beamAverage*2 - offset;
						if (debug) pw.println("OFFSET WITH HACK "+a);
					} else {
						a= offset;
						if (debug) pw.println("OFFSET NO HACK "+a);
					}
				} else {
					a= offset;
					if (debug) pw.println("OFFSET "+a);
				}
	        }

	        double[] orb= orbit.getPositions(getOrientation());
	        for (int j = 0; j < orb.length; j++) {
	            B[j] = a - orb[j];
			}

	        if (debug) {
				Utilities.printMatrix(B,pw);
				pw.flush();
	        }
	    }

        double[] correction= new double[correctors.size() + 1];

	    makeSVDInversion(B, responseMatrix,correction,db);


	    if (reductionEnabled) {

			String[] bpms= new String[ms];
			String[] cors= new String[cs];
			for (i=0; i<ms; i++) bpms[i]= orbit.getBPMs().get(i).getName();
			for (i=0; i<cs; i++) cors[i]= correctors.get(i).getName();

			try {
				responseMatrix= responseMatrix.submatrix(bpms,cors);
			} catch (IllegalArgumentException e) {
				throw new InconsistentDataException(e.getMessage());
			}

	    	addReduction(orbit.getBPMs(),correctors,responseMatrix,correction);
	    }

	    return new Correction(correctors, db.getRFGenerator(), correction, getOrientation(), op.getCorrectionScale(),eigenvectorsUsed,eigenvalues);

	}
	/** {@inheritDoc} */
	@Override
	protected void makeSVDInversion(double[] B, ResponseMatrix responseMatrix, double[] correction, DataBush db)
			throws InconsistentDataException {
	    int ms = B.length;
	    int cs = responseMatrix.correctors.length;
	    double[][] U = new double[ms][cs];

	    if (isVertical()) {
	        super.makeSVDInversion(B, responseMatrix,correction,db);
	        return;
	    }

		U= DBMath.clone(responseMatrix.matrix);

		/*
		 * Until better idea we don't print anything.
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
			pw.println("FREQUENCY CORRECTION");
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

		// weights
	    weightFrequency2(eigenvalues, V);

	    if (debug) {
			pw.println("EIGENVALUES-2 "+eigenvalues.length);
			Utilities.printMatrix(eigenvalues,pw);
			pw.println();
	    }

	    eigenvectorsUsed = 0;
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

	    maxCorrection = 0.0;
	    for (int j = 0; j < cs; j++) {
	        correction[j]= 0.0;
	        for (int i = 0; i < cs; i++) {
	            correction[j] = correction[j] + V[j][i] * tmp[i];
	        }
	        if (Math.abs(correction[j]) > maxCorrection) {
	            maxCorrection = Math.abs(correction[j]);
	        }
	        if (j==cs-1) {
		        correction[j] = calculateRFFrequencyCorrection(correction[j],db);
	        }
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
	/**
	 * <p>Setter for the field <code>externalResponseMatrix</code>.</p>
	 *
	 * @param newExternalResponseMatrix a {@link org.scictrl.mp.orbitcorrect.utilities.ResponseMatrix} object
	 */
	public void setExternalResponseMatrix(ResponseMatrix newExternalResponseMatrix) {
		externalResponseMatrix = newExternalResponseMatrix;
	}
	/**
	 * <p>Setter for the field <code>frequencyCorrectionAmplification</code>.</p>
	 *
	 * @param newFrequencyCorrectionAmplification a double
	 */
	public void setFrequencyCorrectionAmplification(double newFrequencyCorrectionAmplification) {
		if (frequencyCorrectionAmplification != newFrequencyCorrectionAmplification) {
			frequencyCorrectionAmplification = newFrequencyCorrectionAmplification;
		}
	}
	/** {@inheritDoc} */
	@Override
	public void setOffset(double newShiftConstant) {
		if (offset != newShiftConstant) {
			offset = newShiftConstant;
		}
	}
	/**
	 * <p>Setter for the field <code>useExternalResponseMatrix</code>.</p>
	 *
	 * @param newUseExternalResponseMatrix a boolean
	 */
	public void setUseExternalResponseMatrix(boolean newUseExternalResponseMatrix) {
		if (useExternalResponseMatrix != newUseExternalResponseMatrix) {
			useExternalResponseMatrix = newUseExternalResponseMatrix;
		}
	}
	/*private void weightFrequency(double[] eigenvalues, double[][] v) {
	    int size = v.length;
	    double maxValue = 0;
	    int indexAtMaxValue = 0;
	    double weight = 1;

	    for (int i = 0; i < size; i++) {
	        if (Math.abs(maxValue) <= Math.abs(v[i][size - 1])) {
	            maxValue = v[i][size - 1];
	            indexAtMaxValue = i;
	        }
	    }

	    eigenvalues[indexAtMaxValue] = eigenvalues[indexAtMaxValue] * weight;
	}*/
	/*private void weightFrequency1(double[] eigenvalues, double[][] v) {
	    int size = v.length;
	    double maxValue = 0;
	    double weight = 200;
	    Vector mostHihghEigenvalues = new Vector(1, 1);

	    class ValueHolder {
	        int indexOfEigenValue;
	        public ValueHolder(int i) {
	            indexOfEigenValue = i;
	        };
	        public int getIndexOfEigenValue() {
	            return indexOfEigenValue;
	        };
	    };

	    for (int i = 0; i < size; i++) {
	        if (Math.abs(maxValue) <= Math.abs(v[i][size - 1])) {
	            maxValue = v[i][size - 1];
	        }
	    }

	    for (int i = 0; i < size; i++) {
	        if (0.1 * Math.abs(maxValue) <= Math.abs(v[i][size - 1])) {
	            mostHihghEigenvalues.addElement(new ValueHolder(i));
	        }
	    }

	    for (int i = 0; i < mostHihghEigenvalues.size(); i++) {
	        eigenvalues[((ValueHolder) mostHihghEigenvalues.get(i)).getIndexOfEigenValue()] =
	            eigenvalues[((ValueHolder) mostHihghEigenvalues.get(i)).getIndexOfEigenValue()]
	                * weight;
	    }

	}*/
	private void weightFrequency2(double[] eigenvalues, double[][] v) {
	    int size = v.length;
	    double maxValue = 0;
	    //double weight = 1;
	    //int indexAtMaxValue = 0;
	    //int indexAtMaxEigenValue = 0;
	    double maxEigenValue = 0;

	    for (int i = 0; i < size; i++) {
	        if (Math.abs(maxValue) <= Math.abs(v[i][size - 1])) {
	            maxValue = v[i][size - 1];
	            //indexAtMaxValue = i;
	        }
	    }

	    for (double eigenvalue : eigenvalues) {
	        if (Math.abs(maxEigenValue) <= Math.abs(eigenvalue)) {
	            maxEigenValue = eigenvalue;
	            //indexAtMaxEigenValue = i;
	        }
	    }

	/*    double Ko = 0;
	    double K = 0;

	    Ko = eigenvalues[indexAtMaxEigenValue] / eigenvalues[indexAtMaxValue];
	    K = Ko * weight;
	*/

	}

	/** {@inheritDoc} */
	@Override
	public Correction calculateCorrection(OrbitCorrectionOperator engine)
			throws InconsistentDataException {

		if (engine.getDataBush().getStatus()!=org.scictrl.mp.orbitcorrect.DBConst.DB_OPERATIONAL) {
	    	throw new InconsistentDataException("DB is not operational!");
		}

		ElementList<AbstractCorrector> correctors= engine.getCorrectors(getOrientation());
		ElementList<BPMonitor> monitors= engine.getBPMonitors(getOrientation());


		ResponseMatrix rm = null;


		if (useExternalResponseMatrix) {
			if (externalResponseMatrix == null) throw new InconsistentDataException("Measured Response Matrix is not loaded yet!\nLoad Respnse Matrix or use default one!");
		    int ms = monitors.size();
		    int cs = correctors.size();
			String[] bpms= new String[ms];
			String[] cors= new String[cs];
			for (int i=0; i<ms; i++) bpms[i]= monitors.get(i).getName();
			for (int i=0; i<cs; i++) cors[i]= correctors.get(i).getName();

			rm= externalResponseMatrix.submatrix(bpms,cors);
		} else {
			rm= engine.getResponseMatrix(getOrientation());
			if (mainResponseMatrix!=rm) {
				mainResponseMatrix=rm;
				try {
					usedResponseMatrix= addColumnToResponseMatrix(mainResponseMatrix,correctors,monitors,engine.getDataBush());
				} catch (IllegalArgumentException e) {
					throw new InconsistentDataException(e.getMessage());
				}
				engine.fireProgressReported(0.0, "["+getOrientation().getShortName()+"]: New RM at "+Utilities.format2D(usedResponseMatrix.energy)+" GeV in "+usedResponseMatrix.time+" ms.");
			}
			rm = usedResponseMatrix;
		}



		Correction correction= makeCorrection(correctors, engine.getCurrentOrbit(), rm,engine.getDataBush(),engine);

		return correction;

	}
}
