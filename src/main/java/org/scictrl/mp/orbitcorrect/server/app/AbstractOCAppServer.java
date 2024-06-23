/**
 *
 */
package org.scictrl.mp.orbitcorrect.server.app;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scictrl.csshell.Connection;
import org.scictrl.csshell.RemoteException;
import org.scictrl.csshell.ResponseEvent;
import org.scictrl.csshell.ResponseListener;
import org.scictrl.csshell.epics.server.Record;
import org.scictrl.csshell.epics.server.application.EmbeddedApplicationServer;
import org.scictrl.mp.orbitcorrect.DataBushAdapter;
import org.scictrl.mp.orbitcorrect.DataBushEvent;
import org.scictrl.mp.orbitcorrect.IConfigurable;
import org.scictrl.mp.orbitcorrect.Orientation;
import org.scictrl.mp.orbitcorrect.correction.Correction;
import org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionListener;
import org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionModel;
import org.scictrl.mp.orbitcorrect.correction.ISVDOrbitCorrectionModel;
import org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionController;
import org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator.State;
import org.scictrl.mp.orbitcorrect.correction.automatic.AutomaticOrbitCorrectionOperator;
import org.scictrl.mp.orbitcorrect.correction.automatic.IAutomaticOCModel;
import org.scictrl.mp.orbitcorrect.correction.models.DefaultOrbitCorrectionModel;
import org.scictrl.mp.orbitcorrect.correction.models.FrequencyOrbitCorrectionModel;
import org.scictrl.mp.orbitcorrect.epics.EControlSystemEngine;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitorList;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.DataBushInfo;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
import org.scictrl.mp.orbitcorrect.model.optics.OpticsList;
import org.scictrl.mp.orbitcorrect.mvc.ApplicationEngine;
import org.scictrl.mp.orbitcorrect.server.DataBushServerLocal;
import org.scictrl.mp.orbitcorrect.server.IDataBushServer;
import org.scictrl.mp.orbitcorrect.server.ServerDataModel;
import org.scictrl.mp.orbitcorrect.utilities.Orbit;

import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.Severity;
import gov.aps.jca.dbr.Status;

/**
 * <p>This is top level application server abstract class, loads configuration which binds published PV names with underlying functionality, which is same for all OrbitCorrections application servers.</p>
 *
 * <p>It has own application name and associated configuration, which is distinct from MachinePhysics engine (Databush).</p>
 *
 * @author igor@scictrl.com
 */
public abstract class AbstractOCAppServer implements IDataBushServer {

	class AOCListener implements IOrbitCorrectionListener {
		Record stateRec;
		private Record messageRec;
		private Record logRec;
		private Record correctRec;
		private Record autoRec;
		private Record undoRec;
		public AOCListener(Record stateRec, Record messageRec, Record logRec, Record correctRec, Record autoRec, Record undoRec) {
			this.stateRec=stateRec;
			this.messageRec=messageRec;
			this.logRec=logRec;
			this.correctRec=correctRec;
			this.autoRec=autoRec;
			this.undoRec=undoRec;
		}
		@Override
		public void progressReport(double progress, String message) {
			log.info(message);
			Object o= logRec.getValue();
			if (o instanceof String[]) {
				String[] s= (String[])o;
				String n= "";
				for (String element : s) {
					if (element.trim().length()>0) {
						if (n.length()>0) n+="\n";
						n+=element;
					}
				}
				if (n.length()>0) n+="\n";
				n+=message;
				int pos= n.lastIndexOf('\n');
				int lines=0;
				while (pos>-1 && lines<logLineSize) {
					lines+=1;
					pos= n.lastIndexOf('\n',pos-1);
				}
				if (pos>-1 && lines>=logLineSize) {
					n=n.substring(pos+1);
				}

				if (n.length()>2048) {
					n=n.substring(n.length()-2048, n.length());
				}
				logRec.setValue(n);
			} if (o instanceof byte[]) {
				byte[] s= (byte[])o;
				String n= "";
				n=new String(s);
				if (n.length()>0) n+="\n";
				n+=message;
				int pos= n.lastIndexOf('\n');
				int lines=0;
				while (pos>-1 && lines<logLineSize) {
					lines+=1;
					pos= n.lastIndexOf('\n',pos-1);
				}
				if (pos>-1 && lines>=logLineSize) {
					n=n.substring(pos+1);
				}

				if (n.length()>2048) {
					n=n.substring(n.length()-2048, n.length());
				}
				logRec.setValue(n);
			} else if (o !=null) {
				String[] n= new String[1];
				n[0]=o.toString()+"\n"+message;
				logRec.setValue(n);
			} else {
				String[] n= new String[1];
				n[0]=message;
				logRec.setValue(n);
			}
		}

		@Override
		public void stateChange(State state, String message) {
			//log.info(state.toString()+": "+message);
			stateRec.setValue(state.ordinal());
			messageRec.setValue(message);
			progressReport(0, message);

			if (state==State.APPLYING) {
				correctRec.setValue(1);
			} else {
				correctRec.setValue(0);
			}
			if (state==State.STOPPING || state==State.ABORTING || state==State.INACTIVE) {
				autoRec.setValue(0);
				releaseSemaphore();
			}
			undoRec.setValue(getController().canUndo());
		}

		@Override
		public void newCorrection(Correction correction) {

			if (correction==null) {
				log.error("New correction is Null");
				return;
			}

			int used= correction.getEigenvectorsUsed();
			double[] eigenvalues= correction.getEigenvalues();

			if (eigenvalues==null) {
				log.error("New correction eigenvalues are Null");
				return;
			}

			Arrays.sort(eigenvalues);
			ArrayUtils.reverse(eigenvalues);

			if (correction.getOrientation().isHorizontal()) {
				getRecord(DATA_EIGENVAL_H).setValue(eigenvalues);
				getRecord(DATA_EIGENVAL_USED_H).setValue(used);
			} else {
				getRecord(DATA_EIGENVAL_V).setValue(eigenvalues);
				getRecord(DATA_EIGENVAL_USED_V).setValue(used);
			}

		}

	}


	/** Constant <code>CONTROL_MAX_STEP_H="Control:MaxStepH"</code> */
	public static final String CONTROL_MAX_STEP_H = 		"Control:MaxStepH";
	/** Constant <code>CONTROL_MAX_STEP_V="Control:MaxStepV"</code> */
	public static final String CONTROL_MAX_STEP_V = 		"Control:MaxStepV";
	/** Constant <code>CONTROL_SCALE="Control:Scale"</code> */
	public static final String CONTROL_SCALE = 				"Control:Scale";
	/** Constant <code>CONTROL_CORRECT_H="Control:CorrectH"</code> */
	public static final String CONTROL_CORRECT_H = 			"Control:CorrectH";
	/** Constant <code>CONTROL_CORRECT_V="Control:CorrectV"</code> */
	public static final String CONTROL_CORRECT_V = 			"Control:CorrectV";
	/** Constant <code>CONTROL_MODEL_H="Control:ModelH"</code> */
	public static final String CONTROL_MODEL_H = 			"Control:ModelH";
	/** Constant <code>CONTROL_MODEL_V="Control:ModelV"</code> */
	public static final String CONTROL_MODEL_V = 			"Control:ModelV";
	/** Constant <code>CONTROL_BPMS_H_SEL="Control:BPMsH:Sel"</code> */
	public static final String CONTROL_BPMS_H_SEL = 		"Control:BPMsH:Sel";
	/** Constant <code>CONTROL_BPMS_V_SEL="Control:BPMsV:Sel"</code> */
	public static final String CONTROL_BPMS_V_SEL = 		"Control:BPMsV:Sel";
	/** Constant <code>CONTROL_BPMS_NAMES="Control:BPMs:Names"</code> */
	public static final String CONTROL_BPMS_NAMES = 		"Control:BPMs:Names";
	/** Constant <code>CONTROL_BPMS_POS="Control:BPMs:Pos"</code> */
	public static final String CONTROL_BPMS_POS = 			"Control:BPMs:Pos";
	/** Constant <code>CONTROL_CORR_H_SEL="Control:CorrH:Sel"</code> */
	public static final String CONTROL_CORR_H_SEL = 		"Control:CorrH:Sel";
	/** Constant <code>CONTROL_CORR_V_SEL="Control:CorrV:Sel"</code> */
	public static final String CONTROL_CORR_V_SEL = 		"Control:CorrV:Sel";
	/** Constant <code>CONTROL_CORR_H_NAMES="Control:CorrH:Names"</code> */
	public static final String CONTROL_CORR_H_NAMES = 		"Control:CorrH:Names";
	/** Constant <code>CONTROL_CORR_V_NAMES="Control:CorrV:Names"</code> */
	public static final String CONTROL_CORR_V_NAMES = 		"Control:CorrV:Names";
	/** Constant <code>CONTROL_CORR_H_POS="Control:CorrH:Pos"</code> */
	public static final String CONTROL_CORR_H_POS = 		"Control:CorrH:Pos";
	/** Constant <code>CONTROL_CORR_V_POS="Control:CorrV:Pos"</code> */
	public static final String CONTROL_CORR_V_POS = 		"Control:CorrV:Pos";
	/** Constant <code>CONTROL_OPTIC_NAMES="Control:Optic:Names"</code> */
	public static final String CONTROL_OPTIC_NAMES = 		"Control:Optic:Names";
	/** Constant <code>CONTROL_OPTIC_POS="Control:Optic:Pos"</code> */
	public static final String CONTROL_OPTIC_POS = 			"Control:Optic:Pos";
	/** Constant <code>CONTROL_DEBUG_DATA_ENABLE="Control:DataEnabled"</code> */
	public static final String CONTROL_DEBUG_DATA_ENABLE = 	"Control:DataEnabled";
	/** Constant <code>CONTROL_MINIMAL_EIGENVALUE_H="Control:MinEigenvalH"</code> */
	public static final String CONTROL_MINIMAL_EIGENVALUE_H =	"Control:MinEigenvalH";
	/** Constant <code>CONTROL_MINIMAL_EIGENVALUE_V="Control:MinEigenvalV"</code> */
	public static final String CONTROL_MINIMAL_EIGENVALUE_V = 	"Control:MinEigenvalV";

	/** Constant <code>DATA_EIGENVAL_H="Data:EigenvalH"</code> */
	public static final String DATA_EIGENVAL_H = 		"Data:EigenvalH";
	/** Constant <code>DATA_EIGENVAL_V="Data:EigenvalV"</code> */
	public static final String DATA_EIGENVAL_V = 		"Data:EigenvalV";
	/** Constant <code>DATA_EIGENVAL_USED_H="Data:EigenvalUsedH"</code> */
	public static final String DATA_EIGENVAL_USED_H = 	"Data:EigenvalUsedH";
	/** Constant <code>DATA_EIGENVAL_USED_V="Data:EigenvalUsedV"</code> */
	public static final String DATA_EIGENVAL_USED_V = 	"Data:EigenvalUsedV";
	/** Constant <code>DATA_ENERGY="Data:Energy"</code> */
	public static final String DATA_ENERGY = 			"Data:Energy";
	/** Constant <code>DATA_QBAD="Data:QBAD"</code> */
	public static final String DATA_QBAD = 				"Data:QBAD";
	/** Constant <code>DATA_TUNE_H="Data:TuneH"</code> */
	public static final String DATA_TUNE_H = 			"Data:TuneH";
	/** Constant <code>DATA_TUNE_V="Data:TuneV"</code> */
	public static final String DATA_TUNE_V = 			"Data:TuneV";
	/** Constant <code>DATA_BETA_H="Data:BetaH"</code> */
	public static final String DATA_BETA_H = 			"Data:BetaH";
	/** Constant <code>DATA_BETA_V="Data:BetaV"</code> */
	public static final String DATA_BETA_V = 			"Data:BetaV";
	/** Constant <code>DATA_ALPHA_H="Data:AlphaH"</code> */
	public static final String DATA_ALPHA_H = 			"Data:AlphaH";
	/** Constant <code>DATA_ALPHA_V="Data:AlphaV"</code> */
	public static final String DATA_ALPHA_V = 			"Data:AlphaV";
	/** Constant <code>DATA_DISPERSION="Data:Dispersion"</code> */
	public static final String DATA_DISPERSION = 		"Data:Dispersion";
	/** Constant <code>DATA_DISPERSION_DP="Data:DispersionDP"</code> */
	public static final String DATA_DISPERSION_DP = 	"Data:DispersionDP";

	/** Constant <code>STATUS_CORRECTING="Status:Correcting"</code> */
	public static final String STATUS_CORRECTING = 		"Status:Correcting";
	/** Constant <code>STATUS_AUTO_MODE="Status:AutoMode"</code> */
	public static final String STATUS_AUTO_MODE = 		"Status:AutoMode";
	/** Constant <code>STATUS_LOG="Status:Log"</code> */
	public static final String STATUS_LOG = 			"Status:Log";
	/** Constant <code>STATUS_MESSAGE="Status:Message"</code> */
	public static final String STATUS_MESSAGE = 		"Status:Message";
	/** Constant <code>STATUS_STATE="Status:State"</code> */
	public static final String STATUS_STATE = 			"Status:State";
	/** Constant <code>STATUS_CONFIG_REMOTE="Status:ConfigRemote"</code> */
	public static final String STATUS_CONFIG_REMOTE = 	"Status:ConfigRemote";
	/** Constant <code>STATUS_CAN_UNDO="Status:CanUndo"</code> */
	public static final String STATUS_CAN_UNDO = 		"Status:CanUndo";

	/** Constant <code>INFO_ID="Info:ID"</code> */
	public static final String INFO_ID = 				"Info:ID";

	/** Constant <code>CMD_START_SINGLE_STEP="Cmd:StartSingleStep"</code> */
	public static final String CMD_START_SINGLE_STEP =  "Cmd:StartSingleStep";
	/** Constant <code>CMD_START_SINGLE_LOOP="Cmd:StartSingleLoop"</code> */
	public static final String CMD_START_SINGLE_LOOP =  "Cmd:StartSingleLoop";
	/** Constant <code>CMD_TEST_ALL="Cmd:TestAll"</code> */
	public static final String CMD_TEST_ALL = 			"Cmd:TestAll";
	/** Constant <code>CMD_START_CONTINUOUS="Cmd:StartContinuous"</code> */
	public static final String CMD_START_CONTINUOUS = 	"Cmd:StartContinuous";
	/** Constant <code>CMD_STOP="Cmd:Stop"</code> */
	public static final String CMD_STOP = 				"Cmd:Stop";
	/** Constant <code>CMD_ABORT="Cmd:Abort"</code> */
	public static final String CMD_ABORT = 				"Cmd:Abort";
	/** Constant <code>CMD_UNDO="Cmd:Undo"</code> */
	public static final String CMD_UNDO = 				"Cmd:Undo";
	/** Constant <code>CMD_CALC_CORR="Cmd:CalcCorr"</code> */
	public static final String CMD_CALC_CORR = 			"Cmd:CalcCorr";

	/** Constant <code>CMD_BPMS_H_SELECT_ALL="Cmd:BPMsH:SelectAll"</code> */
	public static final String CMD_BPMS_H_SELECT_ALL = "Cmd:BPMsH:SelectAll";
	/** Constant <code>CMD_BPMS_V_SELECT_ALL="Cmd:BPMsV:SelectAll"</code> */
	public static final String CMD_BPMS_V_SELECT_ALL = "Cmd:BPMsV:SelectAll";
	/** Constant <code>CMD_CORR_H_SELECT_ALL="Cmd:CorrH:SelectAll"</code> */
	public static final String CMD_CORR_H_SELECT_ALL = "Cmd:CorrH:SelectAll";
	/** Constant <code>CMD_CORR_V_SELECT_ALL="Cmd:CorrV:SelectAll"</code> */
	public static final String CMD_CORR_V_SELECT_ALL = "Cmd:CorrV:SelectAll";
	/** Constant <code>CMD_BPMS_H_SELECT_NONE="Cmd:BPMsH:SelectNone"</code> */
	public static final String CMD_BPMS_H_SELECT_NONE= "Cmd:BPMsH:SelectNone";
	/** Constant <code>CMD_BPMS_V_SELECT_NONE="Cmd:BPMsV:SelectNone"</code> */
	public static final String CMD_BPMS_V_SELECT_NONE= "Cmd:BPMsV:SelectNone";
	/** Constant <code>CMD_CORR_H_SELECT_NONE="Cmd:CorrH:SelectNone"</code> */
	public static final String CMD_CORR_H_SELECT_NONE= "Cmd:CorrH:SelectNone";
	/** Constant <code>CMD_CORR_V_SELECT_NONE="Cmd:CorrV:SelectNone"</code> */
	public static final String CMD_CORR_V_SELECT_NONE= "Cmd:CorrV:SelectNone";
	/** Constant <code>CMD_DUMP_DATA="Cmd:DumpData"</code> */
	public static final String CMD_DUMP_DATA=		   "Cmd:DumpData";

	/** Constant <code>PROPERTY_OC_CORRECTION_SCALE="oc.correctionScale"</code> */
	public static final String PROPERTY_OC_CORRECTION_SCALE = 			"oc.correctionScale";
	/** Constant <code>PROPERTY_OC_REFERENCE_ORBIT_PV="oc.referenceOrbitPV"</code> */
	public static final String PROPERTY_OC_REFERENCE_ORBIT_PV = 		"oc.referenceOrbitPV";
	/** Constant <code>PROPERTY_OC_BPM_NAMES_PV="oc.bpmNamesPV"</code> */
	public static final String PROPERTY_OC_BPM_NAMES_PV = 				"oc.bpmNamesPV";
	/** Constant <code>PROPERTY_OC_CORRECTION_MODEL_H="oc.correctionModelH"</code> */
	public static final String PROPERTY_OC_CORRECTION_MODEL_H = 		"oc.correctionModelH";
	/** Constant <code>PROPERTY_OC_CORRECTION_MODEL_V="oc.correctionModelV"</code> */
	public static final String PROPERTY_OC_CORRECTION_MODEL_V = 		"oc.correctionModelV";
	/** Constant <code>PROPERTY_OC_AVAILABLE_CORR_MODELS_H="oc.availableCorrModelsH"</code> */
	public static final String PROPERTY_OC_AVAILABLE_CORR_MODELS_H = 	"oc.availableCorrModelsH";
	/** Constant <code>PROPERTY_OC_AVAILABLE_CORR_MODELS_V="oc.availableCorrModelsV"</code> */
	public static final String PROPERTY_OC_AVAILABLE_CORR_MODELS_V = 	"oc.availableCorrModelsV";
	/** Constant <code>PROPERTY_OC_PV_PREFIX="oc.pvPrefix"</code> */
	public static final String PROPERTY_OC_PV_PREFIX = 					"oc.pvPrefix";
	/** Constant <code>PROPERTY_OC_CONFIGURABLE_REMOTELY="oc.configurableRemotely"</code> */
	public static final String PROPERTY_OC_CONFIGURABLE_REMOTELY = 		"oc.configurableRemotely";
	/** Constant <code>PROPERTY_OC_SEMAPHORE_PV="oc.semaphorePV"</code> */
	public static final String PROPERTY_OC_SEMAPHORE_PV = 				"oc.semaphorePV";

	static private Logger log;


	private ApplicationEngine engine;
	private OrbitCorrectionController controller;
	private AutomaticOrbitCorrectionOperator operator;
	private Connection<?, double[], ?> conX;
	private Connection<?, double[], ?> conY;
	private Connection<?, String, ?> semaphore;
	private Orbit referenceOrbit;
	private int logLineSize=20;
	private String name;
	private boolean configurableRemotely;
	private String id;
	private Thread semaphoreLock;
	private int[] ref2oc;

	/**
	 * <p>Constructor for AbstractOCAppServer.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 */
	public AbstractOCAppServer(String name) {
		this.name=name;
		log = LogManager.getLogger(AbstractOCAppServer.class);

		InetAddress ia;
		String host= "somewhere";
		try {
			ia = InetAddress.getLocalHost();
			host= ia.getCanonicalHostName();
			int i = host.indexOf('.');
			if (i>0) {
				host=host.substring(0, i);
			}
		} catch (UnknownHostException e) {
			//e.printStackTrace();
		}
		this.id= name+"-"+new GregorianCalendar().get(Calendar.MILLISECOND)+"@"+host;

		log.info("ID: '"+this.id+"'.");
	}

	/**
	 * <p>getAutomaticOC.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.correction.automatic.IAutomaticOCModel} object
	 */
	public abstract IAutomaticOCModel getAutomaticOC();


	/**
	 * <p>Getter for the field <code>operator</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.correction.automatic.AutomaticOrbitCorrectionOperator} object
	 */
	public AutomaticOrbitCorrectionOperator getOperator() {
		if (operator == null) {
			operator = getController().startAutomaticOperator();
		}
		return operator;
	}

	/**
	 * <p>Getter for the field <code>controller</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionController} object
	 */
	public OrbitCorrectionController getController() {
		if (controller == null) {
			controller = new OrbitCorrectionController();
			controller.initialize(getEngine(),getServer());
			controller.configure(getEngine().getConfiguration());


			controller.setBPMonitors(getDataModel().getSelectedBpmsH(), Orientation.H);
			controller.setBPMonitors(getDataModel().getSelectedBpmsV(), Orientation.V);
			controller.setCorrectors(getDataModel().getSelectedCorrectorsH(), Orientation.H);
			controller.setCorrectors(getDataModel().getSelectedCorrectorsV(), Orientation.V);

			getDataModel().addPropertyChangeListener(ServerDataModel.SELECTED_BPMS_H, new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					reportElementsUsage(getDataModel().getSelectedBpmsH());
					controller.setBPMonitors(getDataModel().getSelectedBpmsH(), Orientation.H);
				}
			});

			getDataModel().addPropertyChangeListener(ServerDataModel.SELECTED_BPMS_V, new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					reportElementsUsage(getDataModel().getSelectedBpmsV());
					controller.setBPMonitors(getDataModel().getSelectedBpmsV(), Orientation.V);
				}
			});

			getDataModel().addPropertyChangeListener(ServerDataModel.SELECTED_CORRECTORS_H, new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					reportElementsUsage(getDataModel().getSelectedCorrectorsH());
					controller.setCorrectors(getDataModel().getSelectedCorrectorsH(), Orientation.H);
				}
			});

			getDataModel().addPropertyChangeListener(ServerDataModel.SELECTED_CORRECTORS_V, new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					reportElementsUsage(getDataModel().getSelectedCorrectorsV());
					controller.setCorrectors(getDataModel().getSelectedCorrectorsV(), Orientation.V);
				}
			});

			controller.setCorrectionScale(getEngine().getConfiguration().getDouble(PROPERTY_OC_CORRECTION_SCALE, 0.5));


			String cn= getEngine().getProperty(PROPERTY_OC_CORRECTION_MODEL_H, FrequencyOrbitCorrectionModel.class.getName());
			try {
				Class<?> c = Class.forName(cn);
				IOrbitCorrectionModel ocm= (IOrbitCorrectionModel) c.getDeclaredConstructor().newInstance();
				ocm.initialize(Orientation.H);
				ocm.configure(getEngine().getConfiguration());
				controller.setOrbitCorrectionModel(ocm);
				log.info("Correction model in H: "+cn);
				getDataModel().setCorrectionModel(ocm);
			} catch (Exception e) {
				log.error("Coud not instantiate H correction model '"+cn+"':"+e.toString(), e);
				throw new RuntimeException("Coud not instantiate H correction model '"+cn+"':"+e.toString(), e);
			}

			cn= getEngine().getProperty(PROPERTY_OC_CORRECTION_MODEL_V, DefaultOrbitCorrectionModel.class.getName());
			try {
				Class<?> c = Class.forName(cn);
				IOrbitCorrectionModel ocm= (IOrbitCorrectionModel) c.getDeclaredConstructor().newInstance();
				ocm.initialize(Orientation.V);
				ocm.configure(getEngine().getConfiguration());
				controller.setOrbitCorrectionModel(ocm);
				log.info("Correction model in V: "+cn);
				getDataModel().setCorrectionModel(ocm);
			} catch (Exception e) {
				log.error("Coud not instantiate V correction model '"+cn+"':"+e.toString(), e);
				throw new RuntimeException("Coud not instantiate V correction model '"+cn+"':"+e.toString(), e);
			}

			//File f= BootstrapLoader.getInstance().getApplicationConfigFile("FastModeOC", "Ref_orbit_2.5.txt");

			//System.out.println(f+" "+f.exists());H

			//Orbit ref= OrbitMonitor.loadFromFile(f, bpms);


			String pvNames= getEngine().getProperty(PROPERTY_OC_BPM_NAMES_PV);
			String pvRef= getEngine().getProperty(PROPERTY_OC_REFERENCE_ORBIT_PV);


			connectToReferenceServer(pvNames,pvRef);
			controller.getOrbitMonitor().setExternalAsReference();

			getDataModel().addPropertyChangeListener(ServerDataModel.CORRECTION_MODEL_H, new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					IOrbitCorrectionModel m= getDataModel().getCorrectionModelH();
					configure(m);
					getController().setOrbitCorrectionModel(m);
					if (m instanceof ISVDOrbitCorrectionModel) {
						getRecord(CONTROL_MINIMAL_EIGENVALUE_H).setValue(((ISVDOrbitCorrectionModel)m).getMinimalEigenvalue());
					}
				}
			});
			getDataModel().addPropertyChangeListener(ServerDataModel.CORRECTION_MODEL_V, new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					IOrbitCorrectionModel m= getDataModel().getCorrectionModelV();
					configure(m);
					getController().setOrbitCorrectionModel(m);
					if (m instanceof ISVDOrbitCorrectionModel) {
						getRecord(CONTROL_MINIMAL_EIGENVALUE_V).setValue(((ISVDOrbitCorrectionModel)m).getMinimalEigenvalue());
					}
				}
			});

			String[] names= getEngine().getConfiguration().getStringArray(PROPERTY_OC_AVAILABLE_CORR_MODELS_H);
			if (names!=null && names.length>0) {
				for (String name2 : names) {
					try {
						getServer().addOrbitCorrectionModelClass(name2, Orientation.H);
					} catch (Exception e) {
						log.error("Coud not load H correction model '"+name2+"':"+e.toString(), e);
					}
				}
			}
			names= getEngine().getConfiguration().getStringArray(PROPERTY_OC_AVAILABLE_CORR_MODELS_V);
			if (names!=null && names.length>0) {
				for (String name2 : names) {
					try {
						getServer().addOrbitCorrectionModelClass(name2, Orientation.V);
					} catch (Exception e) {
						log.error("Coud not load V correction model '"+name2+"':"+e.toString(), e);
					}
				}
			}

		}

		return controller;
	}

	/**
	 * <p>reportElementsUsage.</p>
	 *
	 * @param selected a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	protected void reportElementsUsage(ElementList<?> selected) {
		if (!log.isEnabled(Level.INFO)) {
			return;
		}
		StringBuilder sb= new StringBuilder(1024);
		sb.append("Using ");
		sb.append(selected.size());
		sb.append(" elements: [");
		@SuppressWarnings("unchecked")
		Iterator<AbstractDataBushElement> it= (Iterator<AbstractDataBushElement>) selected.iterator();
		if (it.hasNext()) {
			sb.append(it.next().getName());
		}
		while (it.hasNext()) {
			sb.append(",");
			sb.append(it.next().getName());
		}
		sb.append("]");
		log.info(sb.toString());
	}

	/**
	 * <p>Getter for the field <code>engine</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.mvc.ApplicationEngine} object
	 */
	public ApplicationEngine getEngine() {
		if (engine == null) {
			engine = new ApplicationEngine(name);
		}
		return engine;
	}

	/**
	 * <p>getServer.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.server.DataBushServerLocal} object
	 */
	public DataBushServerLocal getServer() {
		return (DataBushServerLocal) getEngine().getServer();
	}

	/* (non-Javadoc)
	 * @see org.scictrl.mp.orbitcorrect.utilities.server.DataBushServer#getDataBush()
	 */
	/** {@inheritDoc} */
	@Override
	public DataBush getDataBush() {
		return getServer().getDataBush();
	}

	/* (non-Javadoc)
	 * @see org.scictrl.mp.orbitcorrect.utilities.server.DataBushServer#getDataModel()
	 */
	/** {@inheritDoc} */
	@Override
	public ServerDataModel getDataModel() {
		return getServer().getDataModel();
	}



	/**
	 * <p>testAll.</p>
	 */
	public void testAll() {
		getRecord(STATUS_LOG).setValue("");
		getController().startTest(getOperator());
	}


	/**
	 * <p>startSingleLoop.</p>
	 */
	public void startSingleLoop() {
		if (!acquireSemaphore()) {
			getController().fireProgressReport(0, "Ignored, could not lock semaphore!");
			return;
		}
		getRecord(STATUS_LOG).setValue("");
		getOperator().startSingleLoop(getAutomaticOC(),-1,false);
	}

	/**
	 * <p>startSingleStep.</p>
	 */
	public void startSingleStep() {
		if (!acquireSemaphore()) {
			getController().fireProgressReport(0, "Ignored, could not lock semaphore!");
			return;
		}
		getRecord(STATUS_LOG).setValue("");
		getOperator().startSingleLoop(getAutomaticOC(),1,false);
	}

	/**
	 * <p>startSingleStep.</p>
	 */
	public void undoCorrection() {
		if (!acquireSemaphore()) {
			getController().fireProgressReport(0, "Ignored, could not lock semaphore!");
			return;
		}
		if (getOperator().canUndo()) {
			getRecord(STATUS_LOG).setValue("");
			getOperator().applyUndo();
		} else {
			getRecord(STATUS_MESSAGE).setValueAsString("Undo not possible right now!");
		}
	}

	/**
	 * <p>startContinunous.</p>
	 */
	public void startContinunous() {
		if (!acquireSemaphore()) {
			getController().fireProgressReport(0, "Ignored, could not lock semaphore!");
			return;
		}
		getRecord(STATUS_LOG).setValue("");
		getOperator().startContinunous(getAutomaticOC());
	}

	/**
	 * <p>startCalcCorr.</p>
	 */
	public void startCalcCorr() {
		if (!acquireSemaphore()) {
			getController().fireProgressReport(0, "Ignored, could not lock semaphore!");
			return;
		}
		getRecord(STATUS_LOG).setValue("");
		getOperator().startSingleLoop(getAutomaticOC(),1,true);
	}

	/**
	 * <p>stopCorrection.</p>
	 */
	public void stopCorrection() {
		getOperator().stopByUser();
		if (getOperator().getState()==State.INACTIVE) {
			releaseSemaphore();
		}
	}

	/**
	 * <p>abortCorrection.</p>
	 */
	public void abortCorrection() {
		getOperator().abortByUser();
		if (getOperator().getState()==State.INACTIVE) {
			releaseSemaphore();
		}
	}


	/**
	 * {@inheritDoc}
	 *
	 * <p>initialize.</p>
	 */
	@Override
	public void initialize() {
		getEngine().initialize();

		if (getEngine().getState()!=ApplicationEngine.State.INITIALIZED) {
			log.error("EXIT! Engine failed to initialize: "+getEngine().getState().toString());
			return;
		}

		if (getDataBush().getControlSystemEngine() instanceof IConfigurable ) {
			((IConfigurable)getDataBush().getControlSystemEngine()).configure(getEngine().getConfiguration());
		}

		configurableRemotely= getEngine().getConfiguration().getBoolean(PROPERTY_OC_CONFIGURABLE_REMOTELY, false);

		initializeSemaphore();

		String pv= getEngine().getProperty(PROPERTY_OC_PV_PREFIX, "A:SR:OrbitCorrection:01");

		log.info("Starting EPICS server API with prefix '"+pv+"'.");
		try {
			getServer().startEpicsServer(pv);
			String[] names= getServer().getEpicsServer().getDatabase().getNames();
			log.info("Exported: '"+Arrays.toString(names)+"'.");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			log.error("EXIT! EPICS server failed to start: "+e.toString(),e);
			return;
		}

		getDataBush().getControlSystemEngine().notifyOrbitCorrectionRunning();

		EmbeddedApplicationServer eas= getServer().getEpicsServer();

		eas.addRecordWriteListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				notifyRecordWrite(evt);
			}
		});

		eas.addRecordChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				notifyRecordChange(evt);
			}
		});

		eas.createRecord(INFO_ID, "Server unique ID", id);

		eas.createRecord(CMD_TEST_ALL, "Tests all correction devices", 1000);
		eas.createRecord(CMD_START_SINGLE_LOOP, "Starts single correction loop", 1000);
		eas.createRecord(CMD_START_SINGLE_STEP, "Starts single correction step", 1000);
		eas.createRecord(CMD_START_CONTINUOUS, "Starts continuous correction loop", 1000);
		eas.createRecord(CMD_STOP, "Stops correction loop", 1000);
		eas.createRecord(CMD_ABORT, "Aborts correction loop", 1000);
		eas.createRecord(CMD_UNDO, "Reverts last correction", 1000);

		eas.createRecord(CMD_BPMS_H_SELECT_ALL, "Select all BPM H", 1000);
		eas.createRecord(CMD_BPMS_V_SELECT_ALL, "Select all BPM V", 1000);
		eas.createRecord(CMD_CORR_H_SELECT_ALL, "Select all Correctors H", 1000);
		eas.createRecord(CMD_CORR_V_SELECT_ALL, "Select all Correctors V", 1000);
		eas.createRecord(CMD_BPMS_H_SELECT_NONE, "Deselect all BPM H", 1000);
		eas.createRecord(CMD_BPMS_V_SELECT_NONE, "Deselect all BPM V", 1000);
		eas.createRecord(CMD_CORR_H_SELECT_NONE, "Deselect all Correctors H", 1000);
		eas.createRecord(CMD_CORR_V_SELECT_NONE, "Deselect all Correctors V", 1000);

		eas.createRecord(CMD_DUMP_DATA, "Dumps all available data to a file for debugging", 1000);
		eas.createRecord(CONTROL_DEBUG_DATA_ENABLE, "Enables debug data dump to a file for debugging", false, false);

		eas.createRecord(CONTROL_MAX_STEP_H, "Maximal step in H", 0.0, 1.0, "mrad", (short)1, getController().getMaxStep(Orientation.H));
		eas.createRecord(CONTROL_MAX_STEP_V, "Maximal step in V", 0.0, 1.0, "mrad", (short)1, getController().getMaxStep(Orientation.V));
		eas.createRecord(CONTROL_SCALE, "Scale of applied correction", 0.0, 1.0, "(0-1)", (short)1, getController().getCorrectionScale());
		eas.createRecord(CONTROL_CORRECT_H, "Correction done in H",getController().isCorrectHorizontal());
		eas.createRecord(CONTROL_CORRECT_V, "Correction done in V",getController().isCorrectVertical());
		eas.createRecord(CONTROL_BPMS_H_SEL, "Selected BPMs in H",DBRType.BYTE,getDataModel().getBpmsHSelection());
		eas.createRecord(CONTROL_BPMS_V_SEL, "Selected BPMs in V",DBRType.BYTE,getDataModel().getBpmsVSelection());
		eas.createRecord(CONTROL_CORR_H_SEL, "Selected Correctors in H",DBRType.BYTE,getDataModel().getCorrectorsHSelection());
		eas.createRecord(CONTROL_CORR_V_SEL, "Selected Correctors in V",DBRType.BYTE,getDataModel().getCorrectorsVSelection());

		IOrbitCorrectionModel ocm= getController().getOrbitCorrectionModel(Orientation.H);
		eas.createRecord(CONTROL_MINIMAL_EIGENVALUE_H, "Minimal used eigenvalue", 0.0, 100.0, "", (short) 2, ocm instanceof ISVDOrbitCorrectionModel ? ((ISVDOrbitCorrectionModel)ocm).getMinimalEigenvalue() : 1.0);

		ocm= getController().getOrbitCorrectionModel(Orientation.V);
		eas.createRecord(CONTROL_MINIMAL_EIGENVALUE_V, "Minimal used eigenvalue", 0.0, 100.0, "", (short) 2, ocm instanceof ISVDOrbitCorrectionModel ? ((ISVDOrbitCorrectionModel)ocm).getMinimalEigenvalue() : 1.0);

		OpticsList op= getDataBush().getOptics();
		String[] s= new String[op.size()];
		double[] d= new double[op.size()];
		for (int i = 0; i < s.length; i++) {
			AbstractOpticalElement el= op.get(i);
			s[i]= el.getName();
			d[i]= el.getPosition();
		}
		eas.createRecord(CONTROL_OPTIC_NAMES, "Optical el. names",s);
		eas.createRecord(CONTROL_OPTIC_POS, "Optical el. positions",0.0, 115.0,"m",(short)2,d);

		ElementList<BPMonitor> elb= getDataModel().getAvailableBpmsH();
		s= new String[elb.size()];
		d= new double[elb.size()];
		for (int i = 0; i < s.length; i++) {
			BPMonitor el= elb.get(i);
			s[i]= el.getName();
			d[i]= el.getPosition();
		}
		eas.createRecord(CONTROL_BPMS_NAMES, "BPMs names",s);
		eas.createRecord(CONTROL_BPMS_POS, "BPMs positions",0.0, 115.0,"m",(short)2,d);

		ElementList<AbstractCorrector> elc= getDataModel().getAvailableCorrectorsH();
		s= new String[elc.size()];
		d= new double[elc.size()];
		for (int i = 0; i < s.length; i++) {
			AbstractCorrector el= elc.get(i);
			s[i]= el.getName();
			d[i]= el.getPosition();
		}
		eas.createRecord(CONTROL_CORR_H_NAMES, "H Correctors names",s);
		eas.createRecord(DATA_EIGENVAL_H, "Last calculated eigenvalues", 0.0, 1000.0, "", (short)2, new double[s.length]);
		eas.createRecord(DATA_EIGENVAL_USED_H, "Last calculated eigenvectors used", 0, s.length, "", 0);
		eas.createRecord(CONTROL_CORR_H_POS, "Corr HOR positions",0.0, 115.0,"m",(short)2,d);

		elc= getDataModel().getAvailableCorrectorsV();
		s= new String[elc.size()];
		d= new double[elc.size()];
		for (int i = 0; i < s.length; i++) {
			AbstractCorrector el= elc.get(i);
			s[i]= el.getName();
			d[i]= el.getPosition();
		}
		eas.createRecord(CONTROL_CORR_V_NAMES, "V Correctors names",s);
		eas.createRecord(DATA_EIGENVAL_V, "Last calculated eigenvalues", 0.0, 1000.0, "", (short)2, new double[s.length]);
		eas.createRecord(DATA_EIGENVAL_USED_V, "Last calculated eigenvectors used", 0, s.length, "", 0);
		eas.createRecord(CONTROL_CORR_V_POS, "Corr VER positions",0.0, 115.0,"m",(short)2,d);

		List<IOrbitCorrectionModel> models= getDataModel().getAvailableCorrectionModels(Orientation.H);
		int sel=-1;
		String[] labels= new String[models.size()];
		for (int i = 0; i < labels.length; i++) {
			initializeFor(models.get(i));
			labels[i]=models.get(i).getName();
			if (sel<0 && getDataModel().getCorrectionModelH().getName().equals(models.get(i).getName())) {
				sel=i;
			}
		}
		eas.createRecord(CONTROL_MODEL_H, "Correction model in H",labels,(short)sel);

		models= getDataModel().getAvailableCorrectionModels(Orientation.V);
		sel=-1;
		labels= new String[models.size()];
		for (int i = 0; i < labels.length; i++) {
			initializeFor(models.get(i));
			labels[i]=models.get(i).getName();
			if (sel<0 && getDataModel().getCorrectionModelV().getName().equals(models.get(i).getName())) {
				sel=i;
			}
		}
		eas.createRecord(CONTROL_MODEL_V, "Correction model in V",labels,(short)sel);

		eas.createRecord(STATUS_CONFIG_REMOTE, "If it is remotely configurble", configurableRemotely, true);

		
		Record u= eas.createRecord(STATUS_CAN_UNDO, "Undo is available.", false);
		Record a= eas.createRecord(STATUS_AUTO_MODE, "OC is running in auto mode.", false);
		Record c= eas.createRecord(STATUS_CORRECTING, "OC is changing correctors.", false);
		//eas.createRecord(STATUS_STATE, "State of correction process",new String[]{"IDLE","CALCULATING","APPLYING","WAITING","ABORTING"}, (short)0);
		Record m=eas.createRecord(STATUS_MESSAGE, "Last status message", new byte[128]);
		Record l=eas.createRecord(STATUS_LOG, "Last status message", new byte[2048]);

		labels= new String[State.values().length];

		for (int i = 0; i < labels.length; i++) {
			labels[i]=State.values()[i].name();
		}

		Record r= eas.createRecord(STATUS_STATE, "Correction loop state.", labels, (short)0);

		getController().addAutomaticOrbitCorrectionListener(new AOCListener(r,m,l,c,a,u));

		getDataModel().addPropertyChangeListener(ServerDataModel.BPMS_H_SELECTION, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				eas.getRecord(CONTROL_BPMS_H_SEL).setValue(getDataModel().getBpmsHSelection());
			}
		});
		getDataModel().addPropertyChangeListener(ServerDataModel.BPMS_V_SELECTION, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				eas.getRecord(CONTROL_BPMS_V_SEL).setValue(getDataModel().getBpmsVSelection());
			}
		});
		getDataModel().addPropertyChangeListener(ServerDataModel.CORRECTORS_H_SELECTION, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				eas.getRecord(CONTROL_CORR_H_SEL).setValue(getDataModel().getCorrectorsHSelection());
			}
		});
		getDataModel().addPropertyChangeListener(ServerDataModel.CORRECTORS_V_SELECTION, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				eas.getRecord(CONTROL_CORR_V_SEL).setValue(getDataModel().getCorrectorsVSelection());
			}
		});


		eas.createRecord(DATA_ENERGY, "Linear model energy", 0.0, 2.4, "GeV", (short)1, 0.0);
		eas.createRecord(DATA_QBAD, "Total Q, Beta, Alpha (H,V) and Dispersion (d,dp)", 0.0, 100.0, "", (short)6, new double[8]);
		eas.createRecord(DATA_TUNE_H, "Closed orbit Tune in H plane (Q)", 0.0, 10.0, "", (short)6, 0.0);
		eas.createRecord(DATA_TUNE_V, "Closed orbit Tune in V plane (Q)", 0.0, 10.0, "", (short)6, 0.0);
		eas.createRecord(DATA_BETA_H, "Closed orbit Beta in H plane", 0.0, 100.0, "", (short)6, 0.0);
		eas.createRecord(DATA_BETA_V, "Closed orbit Beta in V plane", 0.0, 100.0, "", (short)6, 0.0);
		eas.createRecord(DATA_ALPHA_H, "Closed orbit Alpha in H plane", 0.0, 100.0, "", (short)6, 0.0);
		eas.createRecord(DATA_ALPHA_V, "Closed orbit Alpha in V plane", 0.0, 100.0, "", (short)6, 0.0);
		eas.createRecord(DATA_DISPERSION, "Closed orbit dispertion (p)", 0.0, 10.0, "", (short)6, 0.0);
		eas.createRecord(DATA_DISPERSION_DP, "Closed orbit dispersion delta (dp)", 0.0, 10.0, "", (short)6, 0.0);

		getDataBush().addDataBushListener(new DataBushAdapter() {
			@Override
			public void machineFunctionsChanged(DataBushEvent e) {
				DataBushInfo info= getDataBush().getDataBushInfo();

				eas.getRecord(DATA_ENERGY).setValue(info.getEnergy());
				eas.getRecord(DATA_ENERGY).updateAlarm(Severity.NO_ALARM, Status.NO_ALARM);

				double[] d= new double[8];

				d[0]=info.getQ().x();
				d[1]=info.getQ().z();
				d[2]=info.getBeta().x();
				d[3]=info.getBeta().z();
				d[4]=info.getAlpha().x();
				d[5]=info.getAlpha().z();
				d[6]=info.getDispersion().d();
				d[7]=info.getDispersion().dp();

				eas.getRecord(DATA_TUNE_H).setValue(d[0]);
				eas.getRecord(DATA_TUNE_V).setValue(d[1]);
				eas.getRecord(DATA_BETA_H).setValue(d[2]);
				eas.getRecord(DATA_BETA_V).setValue(d[3]);
				eas.getRecord(DATA_ALPHA_H).setValue(d[4]);
				eas.getRecord(DATA_ALPHA_V).setValue(d[5]);
				eas.getRecord(DATA_DISPERSION).setValue(d[6]);
				eas.getRecord(DATA_DISPERSION_DP).setValue(d[7]);

				eas.getRecord(DATA_QBAD).setValue(d);

				updateAlarm(Severity.NO_ALARM, Status.NO_ALARM);

			}

			@Override
			public void inconsistentData(DataBushEvent e) {
				updateAlarm(Severity.INVALID_ALARM, Status.CALC_ALARM);
			}

			private void updateAlarm(Severity sev, Status stat) {
				eas.getRecord(DATA_ENERGY).updateAlarm(sev, stat);
				eas.getRecord(DATA_QBAD).updateAlarm(sev, stat);
				eas.getRecord(DATA_TUNE_H).updateAlarm(sev, stat);
				eas.getRecord(DATA_TUNE_V).updateAlarm(sev, stat);
				eas.getRecord(DATA_BETA_H).updateAlarm(sev, stat);
				eas.getRecord(DATA_BETA_V).updateAlarm(sev, stat);
				eas.getRecord(DATA_ALPHA_H).updateAlarm(sev, stat);
				eas.getRecord(DATA_ALPHA_V).updateAlarm(sev, stat);
				eas.getRecord(DATA_DISPERSION).updateAlarm(sev, stat);
				eas.getRecord(DATA_DISPERSION_DP).updateAlarm(sev, stat);
			}
		});

		//printDBDebug();
		getEngine().activate();

		log.info("Server is active.");

		getController().dumpData(null);

		//startSingleStep();

		//getController().dumpData();

		/*new Thread() {
			public void run() {
				try {
					getServer().getDataBush().update();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (PackedDBException e) {
					e.printStackTrace();
				}
			};
		}.start();*/
	}

	/**
	 * Does initialization for specific correction model.
	 *
	 * @param m a {@link org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionModel} object
	 */
	protected void initializeFor(IOrbitCorrectionModel m) {
	}


	/**
	 * <p>notifyRecordChange.</p>
	 *
	 * @param evt a {@link java.beans.PropertyChangeEvent} object
	 */
	protected void notifyRecordChange(final PropertyChangeEvent evt) {
		final String name= evt.getPropertyName();

		if (name==CONTROL_DEBUG_DATA_ENABLE) {
			boolean b= getRecord(CONTROL_DEBUG_DATA_ENABLE).getValueAsBoolean();
			getController().setDebugDataEnabled(b);
		} else if (name==CONTROL_MINIMAL_EIGENVALUE_H) {
			IOrbitCorrectionModel m= getController().getOrbitCorrectionModel(Orientation.H);
			if (m instanceof ISVDOrbitCorrectionModel) {
				double d= getRecord(CONTROL_MINIMAL_EIGENVALUE_H).getValueAsDouble();
				((ISVDOrbitCorrectionModel)m).setMinimalEigenvalue(d);
			}
		} else if (name==CONTROL_MINIMAL_EIGENVALUE_V) {
			IOrbitCorrectionModel m= getController().getOrbitCorrectionModel(Orientation.V);
			if (m instanceof ISVDOrbitCorrectionModel) {
				double d= getRecord(CONTROL_MINIMAL_EIGENVALUE_V).getValueAsDouble();
				((ISVDOrbitCorrectionModel)m).setMinimalEigenvalue(d);
			}
		}

	}

	/**
	 * <p>notifyRecordWrite.</p>
	 *
	 * @param evt a {@link java.beans.PropertyChangeEvent} object
	 */
	protected void notifyRecordWrite(final PropertyChangeEvent evt) {
		final String name= evt.getPropertyName();

		log.info("Command '"+name+"' received.");

		try {

			if (name==CMD_STOP) {
				stopCorrection();
				getRecord(CMD_STOP).setValue(0);
			} else if (name==CMD_ABORT) {
				abortCorrection();
				getRecord(CMD_ABORT).setValue(0);
			} else if (name==CMD_DUMP_DATA) {
				getController().dumpData(null);
			} else if (getOperator().getState()==State.INACTIVE) {
				if (name==CMD_TEST_ALL) {
					testAll();
					getRecord(CMD_TEST_ALL).setValue(0);
				} else if (name==CMD_UNDO) {
					undoCorrection();
					getRecord(CMD_UNDO).setValue(0);
				} else if (name==CMD_START_SINGLE_LOOP) {
					startSingleLoop();
					getRecord(CMD_START_SINGLE_LOOP).setValue(0);
				} else if (name==CMD_START_SINGLE_STEP) {
					startSingleStep();
					getRecord(CMD_START_SINGLE_STEP).setValue(0);

				} else if (name==CMD_START_CONTINUOUS) {
					startContinunous();
					getRecord(CMD_START_CONTINUOUS).setValue(0);
					getRecord(STATUS_AUTO_MODE).setValue(1);

				} else if (name==CMD_CALC_CORR) {
					startCalcCorr();
					getRecord(CMD_CALC_CORR).setValue(1);

				} else if (name==CONTROL_BPMS_H_SEL) {
					byte[] set= (byte[]) getRecord(CONTROL_BPMS_H_SEL).getValue();
					boolean[] b= new boolean[set.length];
					for (int i = 0; i < b.length; i++) {
						b[i]= set[i]!=0;
					}
					getDataModel().setBpmsHSelection(b);
					//getDataModel().fireChange(ServerDataModel.BPMS_H_SELECTION);
				} else if (name==CONTROL_BPMS_V_SEL) {
					byte[] set= (byte[]) getRecord(CONTROL_BPMS_V_SEL).getValue();
					boolean[] b= new boolean[set.length];
					for (int i = 0; i < b.length; i++) {
						b[i]= set[i]!=0;
					}
					getDataModel().setBpmsVSelection(b);
					//getDataModel().fireChange(ServerDataModel.BPMS_V_SELECTION);
				} else if (name==CONTROL_CORR_H_SEL) {
					byte[] set= (byte[]) getRecord(CONTROL_CORR_H_SEL).getValue();
					boolean[] b= new boolean[set.length];
					for (int i = 0; i < b.length; i++) {
						b[i]= set[i]!=0;
					}
					getDataModel().setCorrectorsHSelection(b);
					//getDataModel().fireChange(ServerDataModel.CORRECTORS_H_SELECTION);
				} else if (name==CONTROL_CORR_V_SEL) {
					byte[] set= (byte[]) getRecord(CONTROL_CORR_V_SEL).getValue();
					boolean[] b= new boolean[set.length];
					for (int i = 0; i < b.length; i++) {
						b[i]= set[i]!=0;
					}
					getDataModel().setCorrectorsVSelection(b);
					//getDataModel().fireChange(ServerDataModel.CORRECTORS_V_SELECTION);
				} else if (name==CMD_BPMS_H_SELECT_ALL) {
					byte[] set= (byte[]) getRecord(CONTROL_BPMS_H_SEL).getValue();
					boolean[] b= new boolean[set.length];
					boolean change=false;
					for (int i = 0; i < b.length; i++) {
						change = change || set[i]==0;
						b[i]= true;
					}
					if (change) {
						getDataModel().setBpmsHSelection(b);
						getRecord(CONTROL_BPMS_H_SEL).setValue(b);
					}
					getRecord(CMD_BPMS_H_SELECT_ALL).setValue(0);
				} else if (name==CMD_BPMS_V_SELECT_ALL) {
					byte[] set= (byte[]) getRecord(CONTROL_BPMS_V_SEL).getValue();
					boolean[] b= new boolean[set.length];
					boolean change=false;
					for (int i = 0; i < b.length; i++) {
						change = change || set[i]==0;
						b[i]= true;
					}
					if (change) {
						getDataModel().setBpmsVSelection(b);
						getRecord(CONTROL_BPMS_V_SEL).setValue(b);
					}
					getRecord(CMD_BPMS_V_SELECT_ALL).setValue(0);
				} else if (name==CMD_BPMS_H_SELECT_NONE) {
					byte[] set= (byte[]) getRecord(CONTROL_BPMS_H_SEL).getValue();
					boolean[] b= new boolean[set.length];
					boolean change=false;
					for (int i = 0; i < b.length; i++) {
						change = change || set[i]!=0;
						b[i]= false;
					}
					if (change) {
						getDataModel().setBpmsHSelection(b);
						getRecord(CONTROL_BPMS_H_SEL).setValue(b);
					}
					getRecord(CMD_BPMS_H_SELECT_NONE).setValue(0);
				} else if (name==CMD_BPMS_V_SELECT_NONE) {
					byte[] set= (byte[]) getRecord(CONTROL_BPMS_V_SEL).getValue();
					boolean[] b= new boolean[set.length];
					boolean change=false;
					for (int i = 0; i < b.length; i++) {
						change = change || set[i]!=0;
						b[i]= false;
					}
					if (change) {
						getDataModel().setBpmsVSelection(b);
						getRecord(CONTROL_BPMS_V_SEL).setValue(b);
					}
					getRecord(CMD_BPMS_V_SELECT_NONE).setValue(0);
				} else if (name==CMD_CORR_H_SELECT_ALL) {
					byte[] set= (byte[]) getRecord(CONTROL_CORR_H_SEL).getValue();
					boolean[] b= new boolean[set.length];
					boolean change=false;
					for (int i = 0; i < b.length; i++) {
						change = change || set[i]==0;
						b[i]= true;
					}
					if (change) {
						getDataModel().setCorrectorsHSelection(b);
						getRecord(CONTROL_CORR_H_SEL).setValue(b);
					}
					getRecord(CMD_CORR_H_SELECT_ALL).setValue(0);
				} else if (name==CMD_CORR_V_SELECT_ALL) {
					byte[] set= (byte[]) getRecord(CONTROL_CORR_V_SEL).getValue();
					boolean[] b= new boolean[set.length];
					boolean change=false;
					for (int i = 0; i < b.length; i++) {
						change = change || set[i]==0;
						b[i]= true;
					}
					if (change) {
						getDataModel().setCorrectorsVSelection(b);
						getRecord(CONTROL_CORR_V_SEL).setValue(b);
					}
					getRecord(CMD_CORR_V_SELECT_ALL).setValue(0);
				} else if (name==CMD_CORR_H_SELECT_NONE) {
					byte[] set= (byte[]) getRecord(CONTROL_CORR_H_SEL).getValue();
					boolean[] b= new boolean[set.length];
					boolean change=false;
					for (int i = 0; i < b.length; i++) {
						change = change || set[i]!=0;
						b[i]= false;
					}
					if (change) {
						getDataModel().setCorrectorsHSelection(b);
						getRecord(CONTROL_CORR_H_SEL).setValue(b);
					}
					getRecord(CMD_CORR_H_SELECT_NONE).setValue(0);
				} else if (name==CMD_CORR_V_SELECT_NONE) {
					byte[] set= (byte[]) getRecord(CONTROL_CORR_V_SEL).getValue();
					boolean[] b= new boolean[set.length];
					boolean change=false;
					for (int i = 0; i < b.length; i++) {
						change = change || set[i]!=0;
						b[i]= false;
					}
					if (change) {
						getDataModel().setCorrectorsVSelection(b);
						getRecord(CONTROL_CORR_V_SEL).setValue(b);
					}
					getRecord(CMD_CORR_V_SELECT_NONE).setValue(0);
				} else if (configurableRemotely) {
					if (name == CONTROL_MAX_STEP_H) {
						getController().setMaxStep(
								Orientation.H,
								getRecord(CONTROL_MAX_STEP_H).getValueAsDouble());
					} else if (name == CONTROL_MAX_STEP_V) {
						getController().setMaxStep(
								Orientation.V,
								getRecord(CONTROL_MAX_STEP_V).getValueAsDouble());
					} else if (name == CONTROL_SCALE) {
						getController().setCorrectionScale(
								getRecord(CONTROL_SCALE).getValueAsDouble());
					} else if (name == CONTROL_CORRECT_H) {
						getController().setCorrectHorizontal(
								getRecord(CONTROL_CORRECT_H).getValueAsBoolean());
					} else if (name == CONTROL_CORRECT_V) {
						getController().setCorrectVertical(
								getRecord(CONTROL_CORRECT_V).getValueAsBoolean());
					} else if (name == CONTROL_MODEL_H) {
						getDataModel().setCorrectionModelH(getDataModel().getAvailableCorrectionModelsH().get(getRecord(CONTROL_MODEL_H).getValueAsInt()));
					} else if (name == CONTROL_MODEL_V) {
						getDataModel().setCorrectionModelV(getDataModel().getAvailableCorrectionModelsV().get(getRecord(CONTROL_MODEL_V).getValueAsInt()));
					}
				}
			}
		} catch (Throwable t) {
			log.error("Command '"+name+"' failed: "+t.toString(), t);
		}
	}

	private void initializeSemaphore() {
		String semaphorePV= getEngine().getConfiguration().getString(PROPERTY_OC_SEMAPHORE_PV);

		if (semaphorePV!=null) {
			try {
				semaphore= ((EControlSystemEngine)getDataBush().getControlSystemEngine()).connect(semaphorePV, String.class);
				log.info("Using semaphore '"+semaphorePV+"'");

				semaphoreLock=new Thread("SemaphoreLock") {
					@Override
					public synchronized void run() {
						try {
							wait(10000);
						} catch (InterruptedException e) {
							//e.printStackTrace();
						}
						while(isActive()) {
							try {
								wait(1000);
							} catch (InterruptedException e) {
								//e.printStackTrace();
							}

							try {
								String lock= semaphore.getValue();
								if (id.equals(lock)) {
									semaphore.setValue(id);
								}
							} catch (RemoteException e) {
								//e.printStackTrace();
							}

						}
					}
				};
				semaphoreLock.setDaemon(false);
				semaphoreLock.start();

			} catch (Exception e) {
				log.error("Failed to connecto to semaphore '"+semaphorePV+"': "+e.toString(),e);
			}
		}
	}

	/**
	 * <p>isActive.</p>
	 *
	 * @return a boolean
	 */
	public boolean isActive() {
		return engine.getState().isActive();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>shutdown.</p>
	 */
	@Override
	public void shutdown() {
		if (engine.getState().isClosed()) {
			return;
		}
		releaseSemaphore();
		getDataBush().getControlSystemEngine().notifyOrbitCorrectionShuttingDown();
		engine.getServer().shutdown();
		engine.shutdown();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void connectToReferenceServer(String pvNames, String pvRef) {

		try {
			String[] names = (String[])getDataBush().getControlSystemEngine().getValue(pvNames, String[].class);
			BPMonitorList l= getDataBush().getBPMonitors();

			ref2oc= new int[l.size()];

			List<String> nl= new ArrayList<>(names.length);
			Collections.addAll(nl, names);

			for (int i = 0; i < ref2oc.length; i++) {
				BPMonitor bpm= l.get(i);
				String n= bpm.getName();
				if (n.endsWith(":SA")) {
					n= n.substring(0,n.length()-3);
				}
				int j= nl.indexOf(n);
				if (j<0) {
					log.error("Reference orbit from server has different BPMs (missing "+n+"), fix configuration!");
					throw new IllegalStateException("Reference orbit from server has different BPMs (missing "+n+"), fix configuration!");
				}
				ref2oc[i]=j;
			}

			double[] d= new double[names.length];
			referenceOrbit= new Orbit((ElementList<BPMonitor>) l.toElementList(),d, d);
			getController().getOrbitMonitor().setExternalReference(referenceOrbit);

		} catch (Exception e1) {
			log.error("Failed to obtain the reference orbit from server:"+e1,e1);
			throw new IllegalStateException("Failed to obtain the reference orbit from server:"+e1,e1);
		}

		try {
			conX = ((EControlSystemEngine)getDataBush().getControlSystemEngine()).connect(pvRef+":X", double[].class); 
			conY = ((EControlSystemEngine)getDataBush().getControlSystemEngine()).connect(pvRef+":Y", double[].class); 
		} catch (Exception e) {
			log.error("Failed to obtain the reference orbit from server:"+e,e);
			throw new IllegalStateException("Failed to obtain the reference orbit from server:"+e,e);
		}

		PropertyChangeListener monX = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				double[] d= conX.getLastValue();
				updateReference(d,Orientation.H);
			}
		};
		PropertyChangeListener monY = new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				double[] d= conY.getLastValue();
				updateReference(d,Orientation.V);
			}
		};

		try {
			conX.addPropertyChangeListener(Connection.PROPERTY_VALUE, monX);
			conY.addPropertyChangeListener(Connection.PROPERTY_VALUE, monY);
			conX.getValue(new ResponseListener() {
				@Override
				public void responseReceived(ResponseEvent event) {
					if (event.isSuccess()) {
						double[] d= (double[]) event.getResponse().getPoop().getValue();
						updateReference(d,Orientation.H);
					}
				}
			} );
			conY.getValue(new ResponseListener() {
				@Override
				public void responseReceived(ResponseEvent event) {
					if (event.isSuccess()) {
						double[] d= (double[]) event.getResponse().getPoop().getValue();
						updateReference(d,Orientation.V);
					}
				}
			} );
		} catch (RemoteException e) {
			log.error("Failed to obtain the reference orbit from server:"+e,e);
			throw new IllegalStateException("Failed to obtain the reference orbit from server:"+e,e);
		}
	}

	/**
	 * <p>updateReference.</p>
	 *
	 * @param d an array of {@link double} objects
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 */
	protected void updateReference(double[] d, Orientation ori) {
		double[] dd= new double[ref2oc.length];

		for (int i = 0; i < dd.length; i++) {
			dd[i]=d[ref2oc[i]];
		}

		if (ori.isHorizontal()) {
			referenceOrbit = new Orbit(referenceOrbit.getBPMs(),dd, referenceOrbit.getPositions(Orientation.V));
			getController().getOrbitMonitor().setExternalReference(referenceOrbit);
 		} else {
			referenceOrbit = new Orbit(referenceOrbit.getBPMs(),referenceOrbit.getPositions(Orientation.H),dd);
			getController().getOrbitMonitor().setExternalReference(referenceOrbit);
 		}
	}

	/**
	 * Tries to acquire remote semaphore. If success or semaphore is not set, returns true.
	 * @return true if semaphore not used or succesfully acquired.
	 */
	private boolean acquireSemaphore() {
		if (semaphore==null) {
			return true;
		}
		synchronized (semaphoreLock) {
			try {
				semaphore.setValue(id);
				String lock= semaphore.getValue();
				return id.equals(lock);
			} catch (RemoteException e) {
				//e.printStackTrace();
				return true;
			}
		}
	}

	private void releaseSemaphore() {
		if (semaphore==null) {
			return;
		}
		synchronized (semaphoreLock) {
			try {
				String lock= semaphore.getValue();
				if (id.equals(lock)) {
					semaphore.setValue("");
				}
			} catch (RemoteException e) {
				//e.printStackTrace();
			}
		}
	}

	/**
	 * <p>getRecord.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @return a {@link org.scictrl.csshell.epics.server.Record} object
	 */
	protected Record getRecord(String name) {
		return getServer().getEpicsServer().getRecord(name);
	}

	/**
	 * <p>configure.</p>
	 *
	 * @param model a {@link org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionModel} object
	 */
	protected abstract void configure(IOrbitCorrectionModel model);

	/**
	 * <p>isConfigurableRemotely.</p>
	 *
	 * @return a boolean
	 */
	public boolean isConfigurableRemotely() {
		return configurableRemotely;
	}
}
