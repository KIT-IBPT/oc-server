/**
 *
 */
package org.scictrl.mp.orbitcorrect.correction.models;

import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.Orientation;
import org.scictrl.mp.orbitcorrect.correction.Correction;
import org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionModel;
import org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator;
import org.scictrl.mp.orbitcorrect.utilities.Orbit;

/**
 * <p>CombinedOrbitCorrectionModel class.</p>
 *
 * @author igor@scictrl.com
 */
public class CombinedOrbitCorrectionModel extends AbstractOCModel {

	private static Logger log = LogManager.getLogger(CombinedOrbitCorrectionModel.class);

	IOrbitCorrectionModel modelA;
	IOrbitCorrectionModel modelB;

	double scaleA=0.7;
	double scaleB=0.3;

	double switchRMS=0.150;

	/**
	 * <p>Constructor for CombinedOrbitCorrectionModel.</p>
	 */
	public CombinedOrbitCorrectionModel() {
		name = "Combo OC";

	}

	/* (non-Javadoc)
	 * @see org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionModel#calculateCorrection(org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator)
	 */
	/** {@inheritDoc} */
	@Override
	public Correction calculateCorrection(OrbitCorrectionOperator engine)
			throws InconsistentDataException {

		Orbit o= engine.getCurrentOrbit();

		if (o.getRms(Orientation.H)>switchRMS || o.getRms(Orientation.V)>switchRMS) {
			log.debug("No FixBPM optimization, RMS '"+Math.max(o.getRms(Orientation.H), o.getRms(Orientation.V))+"' is above '"+switchRMS+"'.");
			Correction corA= modelA.calculateCorrection(engine);
			return corA;
		}

		// this works only wih freq OC, because has extra position at end for frequency
		Correction corA= modelA.calculateCorrection(engine);
		Correction corB= modelB.calculateCorrection(engine);


		//log.debug("A  "+corA.getCorrection().length+" "+corA.getCorrectors().size());
		//log.debug("B  "+corB.getCorrection().length+" "+corB.getCorrectors().size());

		//log.debug("A  "+corA);
		//log.debug("B  "+corB);


		double[] cor= new double[corA.getCorrection().length];

		for (int i = 0; i < corA.getCorrectors().size(); i++) {
			cor[i]= corA.getCorrection()[i]*scaleA+corB.getCorrection()[i]*scaleB;
		}

		if (corA.getCorrection().length-corA.getCorrectors().size()==1) {
			cor[cor.length-1]=corA.getCorrection()[cor.length-1];
		}

		Correction corAB= new Correction(corA.getCorrectors(), corA.getRfGenerator(), cor, getOrientation(), corA.getScale(), corA.getEigenvectorsUsed(), corA.getEigenvalues());

		//log.debug("AB "+corAB);

		return corAB;
	}

	/* (non-Javadoc)
	 * @see org.scictrl.mp.orbitcorrect.IConfigurable#configure(org.apache.commons.configuration.Configuration)
	 */
	/** {@inheritDoc} */
	@Override
	public void configure(Configuration conf) {

		scaleA= conf.getDouble("oc.combo.scalea",scaleA);
		log.info("Scale A "+scaleA);
		scaleB= conf.getDouble("oc.combo.scaleb",scaleB);
		log.info("Scale B "+scaleB);
		switchRMS= conf.getDouble("oc.combo.switchrms",switchRMS);
		log.info("Switch RMS "+switchRMS);


		if (getOrientation().isHorizontal()) {
			modelA= new FrequencyOrbitCorrectionModel();
		} else {
			modelA= new DefaultOrbitCorrectionModel();
		}
		modelB= new FixBPMsOrbitCorrectionModel();

		modelA.initialize(getOrientation());
		modelB.initialize(getOrientation());

		modelA.configure(conf);
		modelB.configure(conf);

	}

}
