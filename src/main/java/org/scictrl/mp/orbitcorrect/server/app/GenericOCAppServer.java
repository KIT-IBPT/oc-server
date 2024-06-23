/**
 *
 */
package org.scictrl.mp.orbitcorrect.server.app;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.scictrl.csshell.epics.server.application.EmbeddedApplicationServer;
import org.scictrl.mp.orbitcorrect.Orientation;
import org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionModel;
import org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator.State;
import org.scictrl.mp.orbitcorrect.correction.automatic.IAutomaticOCModel;
import org.scictrl.mp.orbitcorrect.correction.automatic.StepAutomaticOC;
import org.scictrl.mp.orbitcorrect.correction.models.Bump3CModel;
import org.scictrl.mp.orbitcorrect.correction.models.Bump4CModel;
import org.scictrl.mp.orbitcorrect.mvc.ApplicationEngine;

import si.ijs.anka.config.BootstrapLoader;

/**
 * <p>This is top level application server, loads configuration which binds published PV names with underlying functionality.
 * Among other things loads correct Databush machine physics engine.</p>
 *
 * <p>It has own application name and associated configuration, which is distinct from MachinePhysics engine (Databush).</p>
 *
 * @author igor@scictrl.com
 */
public class GenericOCAppServer extends AbstractOCAppServer {

	private static final String CONTROL_BUMP_H_HIGHT = 			"Control:BumpH:Hight";
	private static final String CONTROL_BUMP_H_ANGLE = 			"Control:BumpH:Angle";
	private static final String CONTROL_BUMP_H_ELEMENT = 		"Control:BumpH:Element";
	private static final String CMD_BUMP_H_SELECT_CORR = 		"Cmd:BumpH:SelectCorr";

	private static final String CONTROL_BUMP_V_HIGHT = 			"Control:BumpV:Hight";
	private static final String CONTROL_BUMP_V_ANGLE = 			"Control:BumpV:Angle";
	private static final String CONTROL_BUMP_V_ELEMENT = 		"Control:BumpV:Element";
	private static final String CMD_BUMP_V_SELECT_CORR = 		"Cmd:BumpV:SelectCorr";

	static private Logger log;
	private StepAutomaticOC automaticOC;

	/**
	 * <p>Constructor for GenericOCAppServer.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 */
	public GenericOCAppServer(String name) {
		super(name);
	}

	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects
	 */
	public static void main(String[] args) {

		try {

			String name= "GenericOC";
			if (args.length==1) {
				String s=args[0];
				if (s!=null) {
					s=s.trim();
				}
				if (s!=null && s.length()>0) {
					name=s;
				}
			}

			BootstrapLoader.checkLogging();

			final GenericOCAppServer fastOC= new GenericOCAppServer(name);

			fastOC.initialize();

			//System.out.println(fastOC.getDataBush().toString());

			//OrbitCorrectionController ctr= fastOC.getController();

			/*PrintWriter pw= new PrintWriter(System.out);

			ctr.getOrbitMonitor().printReport(pw);
			pw.flush();*/

			if (fastOC.getEngine().getState()!=ApplicationEngine.State.ACTIVE) {
				log.error("EXIT! Failed to start: "+fastOC.getEngine().getState());
				return;
			}

			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {

					if (fastOC.isActive()) {
						log.info("Shutdown initiated from shutdown hook.");
						fastOC.shutdown();
					}
				}
			});


		} catch (Exception e) {
			e.printStackTrace();
			log.error("EXIT! Failed to start: "+e,e);
		}


	}

	/** {@inheritDoc} */
	@Override
	public IAutomaticOCModel getAutomaticOC() {
		if (automaticOC == null) {
			automaticOC = new StepAutomaticOC();
			automaticOC.configure(getEngine().getConfiguration());
		}

		return automaticOC;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>initialize.</p>
	 */
	@Override
	public void initialize() {
		super.initialize();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Does initialization for specific correction model.
	 */
	@Override
	protected void initializeFor(IOrbitCorrectionModel m) {
		super.initializeFor(m);

		if (m instanceof Bump3CModel) {
			EmbeddedApplicationServer eas= getServer().getEpicsServer();
			if (m.getOrientation().isHorizontal() && eas.getRecord(CONTROL_BUMP_H_HIGHT)==null) {
				eas.createRecord(CONTROL_BUMP_H_HIGHT, "H Bump hight", -100.0, 100.0, "mm", (short)2, 0.0);
				eas.createRecord(CONTROL_BUMP_H_ANGLE, "H Bump angle", -Math.PI/2.0*1000.0, Math.PI/2.0*1000.0, "mrad", (short)2, 0.0);
				eas.createRecord(CONTROL_BUMP_H_ELEMENT, "H Bump element", 0, getDataBush().getOptics().size(), "index", 0);
				eas.createRecord(CMD_BUMP_H_SELECT_CORR, "H select correctors", 1000);
			}
			if (m.getOrientation().isVertical() && eas.getRecord(CONTROL_BUMP_V_HIGHT)==null) {
				eas.createRecord(CONTROL_BUMP_V_HIGHT, "V Bump hight", -100.0, 100.0, "mm", (short)2, 0.0);
				eas.createRecord(CONTROL_BUMP_V_ANGLE, "V Bump angle", -Math.PI/2.0*1000.0, Math.PI/2.0*1000.0, "mrad", (short)2, 0.0);
				eas.createRecord(CONTROL_BUMP_V_ELEMENT, "V Bump element", 0, getDataBush().getOptics().size(), "index", 0);
				eas.createRecord(CMD_BUMP_V_SELECT_CORR, "V select correctors", 1000);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void notifyRecordChange(final PropertyChangeEvent evt) {

		super.notifyRecordChange(evt);

		final String name= evt.getPropertyName();

		if (getOperator().getState()==State.INACTIVE) {
			if (name==CONTROL_BUMP_H_ANGLE || name==CONTROL_BUMP_H_ELEMENT || name==CONTROL_BUMP_H_HIGHT) {
				configure(getDataModel().getCorrectionModelH());
			} else if (name==CONTROL_BUMP_V_ANGLE || name==CONTROL_BUMP_V_ELEMENT || name==CONTROL_BUMP_V_HIGHT) {
				configure(getDataModel().getCorrectionModelV());
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	protected void notifyRecordWrite(final PropertyChangeEvent evt) {

		super.notifyRecordWrite(evt);

		final String name= evt.getPropertyName();

		log.info("Command '"+name+"' received.");

		try {

			if (getOperator().getState()==State.INACTIVE) {
				if (name==CMD_BUMP_H_SELECT_CORR) {
					cmdBumpSelectCorrectors(Orientation.HORIZONTAL);
					getRecord(CMD_BUMP_H_SELECT_CORR).setValue(0);
				} else if (name==CMD_BUMP_V_SELECT_CORR) {
					cmdBumpSelectCorrectors(Orientation.VERTICAL);
					getRecord(CMD_BUMP_V_SELECT_CORR).setValue(0);
				}
			}
		} catch (Throwable t) {
			log.error("Command '"+name+"' failed: "+t.toString(), t);
		}
	}

	private void cmdBumpSelectCorrectors(Orientation ori) {

		double[] posC;
		double posB;
		Double[] sel= new Double[4];

		if (ori.isHorizontal()) {
			posB= getDataBush().getOptics().get(getRecord(CONTROL_BUMP_H_ELEMENT).getValueAsInt()).getPosition();
			posC= getRecord(CONTROL_CORR_H_POS).getValueAsDoubleArray();
		} else {
			posB= getDataBush().getOptics().get(getRecord(CONTROL_BUMP_V_ELEMENT).getValueAsInt()).getPosition();
			posC= getRecord(CONTROL_CORR_V_POS).getValueAsDoubleArray();
		}

		Map<Double, Integer> difs= new HashMap<>(posC.length*3);

		double len= getDataBush().getOpticsLength();

		for (int i=0; i<posC.length; i++) {
			double d= posC[i];
			difs.put(d-posB, i);
			difs.put(d-posB-len, i);
			difs.put(d-posB+len, i);
		}

		Double[] pos= new Double[difs.size()];

		pos= difs.keySet().toArray(pos);
		Arrays.sort(pos);

		sel[0]=pos[0];
		sel[1]=pos[0];
		sel[2]=pos[pos.length-1];
		sel[3]=pos[pos.length-1];

		for (Double d : pos) {
			if (d<0) {
				if (d>sel[1]) {
					sel[0]=sel[1];
					sel[1]=d;
				}
			} else if (d>0) {
				if (d<sel[2]) {
					sel[3]=sel[2];
					sel[2]=d;
				}
			}
		}

		IOrbitCorrectionModel m;
		boolean[] b= new boolean[posC.length];
		for (int i = 0; i < b.length; i++) {
			b[i]= false;
		}

		if (ori.isHorizontal()) {
			m= getDataModel().getCorrectionModelH();
		} else {
			m= getDataModel().getCorrectionModelH();
		}

		if (m instanceof Bump3CModel) {
			int i= difs.get(sel[1]);
			b[i]= true;

			i= difs.get(sel[2]);
			b[i]= true;

			if (m instanceof Bump4CModel) {
				i= difs.get(sel[0]);
				b[i]= true;

				i= difs.get(sel[3]);
				b[i]= true;

			} else {
				if (Math.abs(sel[0])<Math.abs(sel[3])) {
					i= difs.get(sel[0]);
					b[i]= true;
				} else {
					i= difs.get(sel[3]);
					b[i]= true;
				}
			}

			for (Double d : sel) {
				i= difs.get(d);
				b[i]= true;
			}

			if (ori.isHorizontal()) {
				getDataModel().setCorrectorsHSelection(b);
			} else {
				getDataModel().setCorrectorsVSelection(b);
			}

		}

	}


	/** {@inheritDoc} */
	@Override
	protected void configure(IOrbitCorrectionModel model) {
		if (model instanceof Bump3CModel) {
			Bump3CModel m= (Bump3CModel)model;
			if (m.getOrientation().isHorizontal()) {
				m.setBumpHeight(getRecord(CONTROL_BUMP_H_HIGHT).getValueAsDouble());
				m.setBumpedElement(getDataBush().getOptics().get(getRecord(CONTROL_BUMP_H_ELEMENT).getValueAsInt()));
			} else {
				m.setBumpHeight(getRecord(CONTROL_BUMP_V_HIGHT).getValueAsDouble());
				m.setBumpedElement(getDataBush().getOptics().get(getRecord(CONTROL_BUMP_V_ELEMENT).getValueAsInt()));
			}
		}
		if (model instanceof Bump4CModel) {
			Bump4CModel m= (Bump4CModel)model;
			if (m.getOrientation().isHorizontal()) {
				m.setBumpAngle(getRecord(CONTROL_BUMP_H_ANGLE).getValueAsDouble());
			} else {
				m.setBumpAngle(getRecord(CONTROL_BUMP_V_ANGLE).getValueAsDouble());
			}
		}
	}

}
