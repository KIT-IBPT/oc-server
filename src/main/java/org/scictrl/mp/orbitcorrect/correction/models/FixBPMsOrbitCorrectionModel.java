package org.scictrl.mp.orbitcorrect.correction.models;

import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.Orientation;
import org.scictrl.mp.orbitcorrect.correction.Correction;
import org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
import org.scictrl.mp.orbitcorrect.utilities.Orbit;
import org.scictrl.mp.orbitcorrect.utilities.ResponseMatrix;
/**
 * <p>FixBPMsOrbitCorrectionModel class.</p>
 *
 * @author igor@scictrl.com
 */
public class FixBPMsOrbitCorrectionModel extends AbstractSVDOrbitCorrectionModel{

	static private Logger log= LogManager.getLogger(FixBPMsOrbitCorrectionModel.class);

	String[] bpmNames= {"A:SR-S4:BPM:01","A:SR-S4:BPM:03"};
	double[] bpmRefH= {-0.209,0.014};
	double[] bpmRefV= {0.049,0.260};

	/**
	 * RingKeeper constructor comment.
	 */
	public FixBPMsOrbitCorrectionModel() {
		super();
		name = "FixBPM OC";
	}


	/** {@inheritDoc} */
	@Override
	public void configure(Configuration conf) {
		super.configure(conf);

		String[] s=conf.getStringArray("oc.fixbpm.bpms");
		if (s!=null && s.length==2) {
			bpmNames=s;
		}
		log.info("BPM names "+Arrays.toString(bpmNames));

		s= conf.getStringArray("oc.fixbpm.refh");
		if (s!=null && s.length==2) {
			bpmRefH[0]= Double.valueOf(s[0]);
			bpmRefH[1]= Double.valueOf(s[1]);
		}
		log.info("BPM ref H  "+Arrays.toString(bpmRefH));

		s= conf.getStringArray("oc.fixbpm.refv");
		if (s!=null && s.length==2) {
			bpmRefV[0]= Double.valueOf(s[0]);
			bpmRefV[1]= Double.valueOf(s[1]);
		}
		log.info("BPM ref V  "+Arrays.toString(bpmRefV));
	}

	/** {@inheritDoc} */
	@Override
	public Correction calculateCorrection(OrbitCorrectionOperator engine) throws InconsistentDataException {

		ElementList<AbstractCorrector> cor = engine.getCorrectors(getOrientation());

		Orbit orbit = engine.getCurrentOrbit();

		//log.debug(orbit.toStringFull());

		double[] x= {orbit.getPosition(Orientation.H, bpmNames[0]),orbit.getPosition(Orientation.H, bpmNames[1])};
		double[] z= {orbit.getPosition(Orientation.V, bpmNames[0]),orbit.getPosition(Orientation.V, bpmNames[1])};

		ElementList<BPMonitor> bpml= new ElementList<>();
		bpml.add(orbit.getBPMs().get(bpmNames[0]));
		bpml.add(orbit.getBPMs().get(bpmNames[1]));

		// orbit with just selected bpms
		orbit= new Orbit(bpml, x, z);
		Orbit ref= new Orbit(bpml, bpmRefH, bpmRefV);

		orbit= new Orbit(orbit, ref);

		//log.debug(orbit.toStringFull());

		// part of RM that corresp[onds to selected BPMs
		ResponseMatrix rm= engine.getResponseMatrix(getOrientation()).submatrix(bpmNames);

		Correction correction= makeCorrection(cor, orbit, rm ,engine.getDataBush(), engine);

		//log.debug(correction);

		return correction;

	}
}
