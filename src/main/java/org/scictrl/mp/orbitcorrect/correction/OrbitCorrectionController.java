/**
 *
 */
package org.scictrl.mp.orbitcorrect.correction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scictrl.mail.MailHandler;
import org.scictrl.mp.orbitcorrect.ControlSystemException;
import org.scictrl.mp.orbitcorrect.DataBushAdapter;
import org.scictrl.mp.orbitcorrect.DataBushEvent;
import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.IConfigurable;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.Orientation;
import org.scictrl.mp.orbitcorrect.accessories.Utilities;
import org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator.State;
import org.scictrl.mp.orbitcorrect.correction.automatic.AutomaticOrbitCorrectionOperator;
import org.scictrl.mp.orbitcorrect.epics.EControlSystemEngine;
import org.scictrl.mp.orbitcorrect.model.IDataConnector;
import org.scictrl.mp.orbitcorrect.model.IControlSystemEngine;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
import org.scictrl.mp.orbitcorrect.model.optics.PowerSupply;
import org.scictrl.mp.orbitcorrect.model.optics.PowerSupplyList;
import org.scictrl.mp.orbitcorrect.mvc.ApplicationEngine;
import org.scictrl.mp.orbitcorrect.server.DataBushServerLocal;
import org.scictrl.mp.orbitcorrect.utilities.Orbit;
import org.scictrl.mp.orbitcorrect.utilities.OrbitMonitor;
import org.scictrl.mp.orbitcorrect.utilities.ResponseMatrix;

import si.ijs.anka.config.BootstrapLoader;

/**
 * <p>
 * Static part of OrbitCorrection procedure, holds references to models and data, which does not change through correction procedure.
 * </p>
 *
 * @author igor@scictrl.com
 */
public final class OrbitCorrectionController implements IConfigurable {

	/** Constant <code>AOC_MIN_BEAM_CURRENT="aoc.minBeamCurrent"</code> */
	public static final String AOC_MIN_BEAM_CURRENT = "aoc.minBeamCurrent";
	/** Constant <code>AOC_STEP_WAIT_TIME="aoc.stepWaitTime"</code> */
	public static final String AOC_STEP_WAIT_TIME =	  "aoc.stepWaitTime";
	/** Constant <code>AOC_HORIZONTAL="aoc.horizontal"</code> */
	public static final String AOC_HORIZONTAL =       "aoc.horizontal";
	/** Constant <code>AOC_VERTICAL="aoc.vertical"</code> */
	public static final String AOC_VERTICAL =         "aoc.vertical";

	private static final String PROPERTY_OC_CONTROL_MAX_UNDO = 		"oc.control.maxUndo";
	private static final String PROPERTY_OC_CONTROL_MAX_STEP_VER = 	"oc.control.maxStepVer";
	private static final String PROPERTY_OC_CONTROL_MAX_STEP_HOR = 	"oc.control.maxStepHor";
	private static final String PROPERTY_OC_CONTROL_MAIL_CONF_FILE=	"oc.control.mailConfFile";
	private static final String PROPERTY_OC_CONTROL_SEND_MAIL = 	"oc.control.sendMail";
	private static final String PROPERTY_OC_CONTROL_MAX_STEPS =     "oc.control.maxSteps";

	private static final String DEFAULT_MAIL_CONF_FILE = "MailConfiguration.properties";


	private DataBush db;
	private Logger log= LogManager.getLogger(getClass());
	private final IOrbitCorrectionModel[] model= new IOrbitCorrectionModel[2];
	@SuppressWarnings("unchecked")
	private ElementList<AbstractCorrector>[] correctors = new ElementList[2];
	@SuppressWarnings("unchecked")
	private ElementList<BPMonitor>[] bpms = new ElementList[2];
	@SuppressWarnings("unused")
	private DataBushServerLocal server;
	private double correctionScale;
	private OrbitMonitor orbitMonitor= new OrbitMonitor();
	private double[] maxStep= {0.1,0.1};
	private boolean readonly=false;

	private long stepWaitTime=10;

	private AutomaticOrbitCorrectionOperator _automaticOperator;
	private OrbitCorrectionOperator _operator;

	private MailHandler mailHandler;
	private Configuration configuration;

	private boolean bcEnabled=true;
	private double minimalBeamCurrent=1;
	private IDataConnector<Double> beamCurrentConnector;
	private boolean allPass=false;

	private File dataFolder;
	private ApplicationEngine engine;
	private boolean debugDataEnabled;

	private final List<IOrbitCorrectionListener> listeners = new ArrayList<>(8);
	private IDataConnector<Double> rfSteppingConnector;

	private boolean correctHorizontal = true;
	private boolean correctVertical = true;

	private Deque<CorrectionInstruction> corrections= new ArrayDeque<>(128);
	private int maxUndo=20;
	/**
	 * Maximum number of steps correction will make when applying correctors.
	 * 0 means all steps are applied.
	 */
	protected int maxNumberOfSteps=1;

	/**
	 * <p>Constructor for OrbitCorrectionController.</p>
	 */
	public OrbitCorrectionController() {
		correctors[0] = new ElementList<>(40);
		correctors[1] = new ElementList<>(40);
		bpms[0] = new ElementList<>(40);
		bpms[1] = new ElementList<>(40);
	}


	/**
	 * <p>addAutomaticOrbitCorrectionListener.</p>
	 *
	 * @param listener a {@link org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionListener} object
	 */
	public void addAutomaticOrbitCorrectionListener(IOrbitCorrectionListener listener) {
		listeners.add(listener);
	}
	/**
	 * <p>removeAutomaticOrbitCorrectionListener.</p>
	 *
	 * @param listener a {@link org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionListener} object
	 */
	public void removeAutomaticOrbitCorrectionListener(IOrbitCorrectionListener listener) {
		listeners.remove(listener);
	}

	/**
	 * <p>fireStateChange.</p>
	 *
	 * @param state a {@link org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator.State} object
	 * @param message a {@link java.lang.String} object
	 */
	public void fireStateChange(State state, String message){
		for (IOrbitCorrectionListener l : listeners) {
			l.stateChange(state,message);
		}
	}
	/**
	 * <p>fireNewCorrection.</p>
	 *
	 * @param correction a {@link org.scictrl.mp.orbitcorrect.correction.Correction} object
	 */
	public void fireNewCorrection(Correction correction){
		for (IOrbitCorrectionListener l : listeners) {
			l.newCorrection(correction);
		}
	}

	/**
	 * <p>fireProgressReport.</p>
	 *
	 * @param progress a double
	 * @param message a {@link java.lang.String} object
	 */
	public void fireProgressReport(double progress, String message) {
		StringBuilder sb= new StringBuilder(128);
		sb.append(Utilities.formatdMHmsS());
		if (message!=null && message.length()>0) {
			sb.append(' ');
			sb.append(message);
		}
		String s= sb.toString();
		for (IOrbitCorrectionListener l : listeners) {
			l.progressReport(progress,s);
		}
	}

	/**
	 * <p>Getter for the field <code>dataFolder</code>.</p>
	 *
	 * @return a {@link java.io.File} object
	 */
	public File getDataFolder() {
		if (dataFolder == null) {
			dataFolder = new File(BootstrapLoader.getInstance().getBundleHomeDir(),"data");
			try {
				dataFolder.mkdirs();
			} catch (Exception e) {
				log.error("Failed to create data dir '"+dataFolder+"': "+e.toString(), e);
			}
		}
		return dataFolder;
	}

	/**
	 * Creates new PrintWirtte on new file, where data should be written. After data is being written,
	 * the stream must be closed in order data to be preserved.
	 *
	 * @return a {@link java.io.PrintWriter} object
	 */
	public PrintWriter newDataFile() {
		File dir= getDataFolder();
		PrintWriter pw=null;
		if (dir!=null) {
			File f= new File(dir,engine.getName()+"-"+DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now())+".txt");
			try {
				pw= new PrintWriter(new BufferedWriter(new FileWriter(f), 32768), false);
				log.info("Dumping data to '"+f.getAbsolutePath()+"'");
				fireProgressReport(0.0, "Dumping data to '"+f.getAbsolutePath()+"'");
			} catch (IOException e) {
				log.error("Failed to create data file '"+f.getAbsolutePath()+"': "+e.toString(),e);
				fireProgressReport(0.0, "Failed to create data file '"+f.getAbsolutePath()+"'");
			}
		}
		if (pw==null) {
			pw= new PrintWriter(System.out) {
				@Override
				public void close() {
					super.flush();
				}
			};
		}

		return pw;
	}

	private MailHandler getMailHandler() {
		if (mailHandler == null) {
			try {
				String fn= configuration.getString(PROPERTY_OC_CONTROL_MAIL_CONF_FILE, DEFAULT_MAIL_CONF_FILE);
				File f=new File(fn);
				if (!f.exists()) {
					if (configuration instanceof FileConfiguration) {
						FileConfiguration fc= (FileConfiguration)configuration;
						String base= fc.getBasePath();
						f= new File(base,fn);
					}
				}
				Properties p= new Properties(ConfigurationConverter.getProperties(configuration));
				if (f.exists()) {
					Reader fr= new BufferedReader(new FileReader(f));
					p.load(fr);
					fr.close();

					mailHandler = new MailHandler(p);
					log.info("Mail handler initialized from '"+f.toString()+"'.");

				} else {
					log.error("Can not initialize mail handler, configuration file '"+fn+"' or '"+f.toString()+"' not found!");
					mailHandler = new MailHandler();
				}
			} catch (Exception e) {
				log.error("Failed to initialize mail handler.", e);
			}
		}
		return mailHandler;
	}

	/**
	 * <p>sendMailNotification.</p>
	 *
	 * @param message a {@link java.lang.String} object
	 * @param debug a {@link java.lang.String} object
	 */
	public void sendMailNotification(final String message, final String debug) {
		MailHandler mh= getMailHandler();
		if (mh!=null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						mailHandler.sendMail(message,debug);
						log.info("Mail sent '"+message+"'.");
					} catch (Exception e) {
						log.error("Mailing message failed:"+e, e);
						log.info("Mail not sent: '"+message+"'.");
					}
				}
			}).start();
		}
	}
	/**
	 * <p>Getter for the field <code>orbitMonitor</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.utilities.OrbitMonitor} object
	 */
	public OrbitMonitor getOrbitMonitor() {
		return orbitMonitor;
	}

	/**
	 * <p>Getter for the field <code>correctors</code>.</p>
	 *
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public ElementList<AbstractCorrector> getCorrectors(Orientation ori) {
		return correctors[ori.ordinal()];
	}

	/**
	 * <p>getDataBush.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} object
	 */
	public DataBush getDataBush() {
		return db;
	}
	/**
	 * <p>getBPMonitors.</p>
	 *
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public ElementList<BPMonitor> getBPMonitors(Orientation ori) {
		return bpms[ori.ordinal()];
	}

	/**
	 * <p>setOrbitCorrectionModel.</p>
	 *
	 * @param model a {@link org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionModel} object
	 */
	public void setOrbitCorrectionModel(IOrbitCorrectionModel model) {
		log.info("Using "+model.getOrientation().getShortName()+" model: "+model.getName()+" ("+model.getClass().getName()+")");
		this.model[model.getOrientation().ordinal()]=model;
	}

	/**
	 * <p>getOrbitCorrectionModel.</p>
	 *
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @return a {@link org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionModel} object
	 */
	public IOrbitCorrectionModel getOrbitCorrectionModel(Orientation ori) {
		return this.model[ori.ordinal()];
	}

	/**
	 * <p>Setter for the field <code>stepWaitTime</code>.</p>
	 *
	 * @param stepWaitTime a long
	 */
	public void setStepWaitTime(long stepWaitTime) {
		this.stepWaitTime = stepWaitTime;
	}

	/**
	 * <p>Getter for the field <code>stepWaitTime</code>.</p>
	 *
	 * @return a long
	 */
	public long getStepWaitTime() {
		return stepWaitTime;
	}

	/**
	 * <p>initialize.</p>
	 *
	 * @param engine a {@link org.scictrl.mp.orbitcorrect.mvc.ApplicationEngine} object
	 * @param dbs a {@link org.scictrl.mp.orbitcorrect.server.DataBushServerLocal} object
	 */
	public void initialize(ApplicationEngine engine, DataBushServerLocal dbs) {
		this.server=dbs;
		this.engine=engine;
		this.db= dbs.getDataBush();

		db.addDataBushListener(new DataBushAdapter() {
			@Override
			public void machineFunctionsChanged(DataBushEvent e) {
				invalidateAll();
			}
			@Override
			public void beamChanged(DataBushEvent e) {
				invalidateAll();
			}
		});

		orbitMonitor.initialize(db);
	}

	/**
	 * <p>Getter for the field <code>engine</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.mvc.ApplicationEngine} object
	 */
	public ApplicationEngine getEngine() {
		return engine;
	}

	/**
	 * <p>invalidateAll.</p>
	 */
	protected void invalidateAll() {
		if (_operator!=null) {
			_operator.invalidateResponseMatrix();
		}
		if (_automaticOperator!=null) {
			_automaticOperator.invalidateResponseMatrix();
		}
	}

	/**
	 * <p>startOperator.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator} object
	 */
	public OrbitCorrectionOperator startOperator() {
		if (_operator!=null) {
			throw new IllegalStateException("Operator already exists!");
		}

		if (_operator == null) {
			_operator = new OrbitCorrectionOperator();
			_operator.initialize("OCOperator", db, correctors, bpms, this);
		}

		return _operator;
	}

	/**
	 * <p>startAutomaticOperator.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.correction.automatic.AutomaticOrbitCorrectionOperator} object
	 */
	public AutomaticOrbitCorrectionOperator startAutomaticOperator() {
		if (_automaticOperator!=null) {
			throw new IllegalStateException("Operator already exists!");
		}

		if (_automaticOperator == null) {
			_automaticOperator = new AutomaticOrbitCorrectionOperator();
			_automaticOperator.initialize("AOCOperator", db, correctors, bpms, this);
			if (configuration!=null) {
				_automaticOperator.setSendMailNotification(configuration.getBoolean(PROPERTY_OC_CONTROL_SEND_MAIL, false));
			}
			/*StringWriter sw= new StringWriter();
			Utilities.printFormated(getDataBush(), new java.io.PrintWriter(sw));

			log.info(sw.toString());*/
		}

		return _automaticOperator;
	}

	/**
	 * <p>startTest.</p>
	 *
	 * @param operator a {@link org.scictrl.mp.orbitcorrect.correction.automatic.AutomaticOrbitCorrectionOperator} object
	 * @return a boolean
	 */
	public boolean startTest(AutomaticOrbitCorrectionOperator operator) {
		operator.setState(State.ACTIVE, "Testing...");
		try {
			boolean ok=true;

			boolean b= testDevices(operator,Orientation.H,false);
			ok = ok && b;
			b= testDevices(operator,Orientation.V,false);
			ok = ok && b;
			b= testMain(operator,false);
			ok = ok && b;

			if (ok) {
				operator.setState(State.INACTIVE, "All tests are OK.");
			} else {
				operator.setState(State.INACTIVE, "Some tests have FAILED!");
			}

			if (getMailHandler().isDumpMessage()) {
				sendMailNotification("Testa have passed: "+ok, "This is a debug message.");
			}

			return ok;
		} catch(Exception e) {
			log.error("Testing failed: "+e.toString(),e);
			operator.fireProgressReported(0, "Testing failed: "+e.toString());
			operator.setState(State.INACTIVE, "Tests have FAILED!");
			return false;
		}

	}

	private boolean checkSize(ElementList<?> list, String name) {
		if (list.size()>0) return true;
		log.warn("No "+name+" selected!");
		return false;
	}



	/**
	 * <p>calculateCorrection.</p>
	 *
	 * @param op a {@link org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator} object
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @return a {@link org.scictrl.mp.orbitcorrect.correction.Correction} object
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 */
	public Correction calculateCorrection(OrbitCorrectionOperator op, Orientation ori) throws InconsistentDataException {

		IOrbitCorrectionModel model= this.model[ori.ordinal()];

		Correction correction= model.calculateCorrection(op);

		StringWriter sw= new StringWriter();
		correction.print(new PrintWriter(sw));
		log.debug(sw.toString());

		return correction;
	}

	/**
	 * <p>Setter for the field <code>correctors</code>.</p>
	 *
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 */
	public void setCorrectors(ElementList<AbstractCorrector> l, Orientation ori) {
		if ((l==null)||(l.size()==0)) throw new IllegalArgumentException("No correctors provided!");
		correctors[ori.ordinal()]=l;
		invalidateAll();
	}

	/**
	 * <p>setBPMonitors.</p>
	 *
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 */
	public void setBPMonitors(ElementList<BPMonitor> l, Orientation ori) {
		if ((l==null)||(l.size()==0)) throw new IllegalArgumentException("No BPMs provided!");
		bpms[ori.ordinal()]=l;
		orbitMonitor.setBPMs(l);
		invalidateAll();
	}

	/**
	 * <p>calculateResponseMatrix.</p>
	 *
	 * @param op a {@link org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator} object
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @return a {@link org.scictrl.mp.orbitcorrect.utilities.ResponseMatrix} object
	 */
	public ResponseMatrix calculateResponseMatrix(OrbitCorrectionOperator op, Orientation ori) {

		ElementList<BPMonitor> bpm= bpms[ori.ordinal()];
		ElementList<AbstractCorrector> cor= correctors[ori.ordinal()];

		if (bpm==null
				||bpm.size()==0
				||cor==null
				||cor.size()==0) {
			return new ResponseMatrix(0,0);
		}

		if (getDataBush().isStatusOperational()) {
			return ResponseMatrix.fillWithCloseOrbitCalculation(
				new ResponseMatrix(bpm,cor),getDataBush(),ori);
		}

		return new ResponseMatrix(bpm,cor);
	}

	/**
	 * <p>Getter for the field <code>correctionScale</code>.</p>
	 *
	 * @return a double
	 */
	public double getCorrectionScale() {
		return correctionScale;
	}

	/**
	 * <p>Setter for the field <code>correctionScale</code>.</p>
	 *
	 * @param correctionScale a double
	 */
	public void setCorrectionScale(double correctionScale) {
		this.correctionScale = correctionScale;
	}

	/**
	 * <p>applyCorrection.</p>
	 *
	 * @param op a {@link org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator} object
	 * @param corr a {@link org.scictrl.mp.orbitcorrect.correction.Correction} object
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	public void applyCorrection(final OrbitCorrectionOperator op, final Correction corr) throws IllegalStateException, DataBushPackedException, InconsistentDataException, ControlSystemException {
		signalOrbitCorrectionStarted();
		_applyCorrection(op, corr, maxNumberOfSteps,null);
		signalOrbitCorrectionEnded();
	}

	/**
	 * <p>Execute undo operation, if possible.</p>
	 *
	 * @param op a {@link org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator} object
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	public void applyUndo(final OrbitCorrectionOperator op) throws IllegalStateException, DataBushPackedException, InconsistentDataException, ControlSystemException {

		if (!canUndo()) {
			log.warn("UNDO is not possible");
			return;
		}

		CorrectionInstruction ci= corrections.removeLast();

		CorrectionInstruction cir= ci.invert();

		signalOrbitCorrectionStarted();
		_applyCorrection(op, null, maxNumberOfSteps,cir);
		signalOrbitCorrectionEnded();
	}

	/**
	 * Internal apply correction
	 *
	 * @param op
	 * @param correction
	 * @param maxSteps
	 * @throws IllegalStateException
	 * @throws DataBushPackedException
	 * @throws InconsistentDataException
	 * @throws ControlSystemException
	 */
	private void _applyCorrection(final OrbitCorrectionOperator op, final Correction correction,
			final int maxNumberOfSteps, final CorrectionInstruction corrInstr)
			throws IllegalStateException, DataBushPackedException, InconsistentDataException, ControlSystemException {

		CorrectionInstruction ci = null;

		if (corrInstr!=null) {
			ci = corrInstr;
		} else {
			ci = new CorrectionInstruction(correction);
			addCorrection(ci);
		}


		ci.calculateSteps(maxStep[ci.ori()], maxNumberOfSteps);

		for (int i = 0; i < ci.steps(); i++) {

			if (op.isStateAborting() || op.isStateInactive())
				return;

			ElementList<AbstractDataBushElement> set = ci.prepareStep();

			if (op.isStateAborting() || op.isStateInactive())
				return;

			if (!readonly) {
				if ((getDataBush().applyFast(set)) > 0) {
					return;
				}
			}

			if (stepWaitTime > 0) {
				try {
					synchronized (this) {
						wait(stepWaitTime);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (ci.hasRF()) {
				try {
					int k = 0;
					while (isRFStepping() && k++ < 100) {
						try {
							synchronized (this) {
								wait(100);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} catch (Exception e) {
					log.error("RF Stepping check failed: " + e, e);
				}
			}

		}

		return;
	}

	/**
	 * <p>Setter for the field <code>maxNumberOfSteps</code>.</p>
	 *
	 * @param maxNumberOfSteps a int
	 */
	public void setMaxNumberOfSteps(int maxNumberOfSteps) {
		this.maxNumberOfSteps = maxNumberOfSteps;
	}

	/**
	 * <p>signalOrbitCorrectionStarted.</p>
	 */
	public void signalOrbitCorrectionStarted() {
		getDataBush().getControlSystemEngine().notifyOrbitCorrectionStarted();
	}

	/**
	 * <p>signalOrbitCorrectionEnded.</p>
	 */
	public void signalOrbitCorrectionEnded() {
		getDataBush().getControlSystemEngine().notifyOrbitCorrectionEnded();
	}

	/**
	 * <p>signalAutomaticOrbitCorrectionStarted.</p>
	 */
	public void signalAutomaticOrbitCorrectionStarted() {
		getDataBush().getControlSystemEngine().notifyAutomaticOrbitCorrectionStarted();
	}

	/**
	 * <p>signalAutomaticOrbitCorrectionEnded.</p>
	 */
	public void signalAutomaticOrbitCorrectionEnded() {
		getDataBush().getControlSystemEngine().notifyAutomaticOrbitCorrectionEnded();
	}

	/**
	 * <p>Getter for the field <code>maxStep</code>.</p>
	 *
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @return a double
	 */
	public double getMaxStep(Orientation ori) {
		return maxStep[ori.ordinal()];
	}

	/**
	 * <p>Setter for the field <code>maxStep</code>.</p>
	 *
	 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @param step a double
	 */
	public void setMaxStep(Orientation ori, double step) {
		maxStep[ori.ordinal()]=step;
	}

	/** {@inheritDoc} */
	@Override
	public void configure(Configuration conf) {
		this.configuration=conf;

		setCorrectHorizontal(conf.getBoolean(AOC_HORIZONTAL, true));
		setCorrectVertical(conf.getBoolean(AOC_VERTICAL, true));

		setMaxNumberOfSteps(conf.getInt(PROPERTY_OC_CONTROL_MAX_STEPS, 100));
		setMaxStep(Orientation.H, conf.getDouble(PROPERTY_OC_CONTROL_MAX_STEP_HOR, 0.1));
		setMaxStep(Orientation.V, conf.getDouble(PROPERTY_OC_CONTROL_MAX_STEP_VER, 0.1));
		setMaxUndo(conf.getInt(PROPERTY_OC_CONTROL_MAX_UNDO, 20));
		setMinimalBeamCurrent(conf.getDouble(AOC_MIN_BEAM_CURRENT, 1.0));
		setStepWaitTime(conf.getLong(AOC_STEP_WAIT_TIME, 10));
		if (_automaticOperator!=null) {
			_automaticOperator.setSendMailNotification(configuration.getBoolean(PROPERTY_OC_CONTROL_SEND_MAIL, false));
		}
	}

	/**
	 * Sets max count of undo history.
	 *
	 * @param maxUndo max count of undo history
	 */
	public void setMaxUndo(int maxUndo) {
		this.maxUndo=maxUndo;
	}

	/**
	 * Max count of undo history.
	 *
	 * @return max count of undo history
	 */
	public int getMaxUndo() {
		return maxUndo;
	}


	/**
	 * <p>Getter for the field <code>minimalBeamCurrent</code>.</p>
	 *
	 * @return double
	 */
	public double getMinimalBeamCurrent() {
		return minimalBeamCurrent;
	}

	/**
	 * <p>isMinimalBeamCurrentEnabled.</p>
	 *
	 * @return a boolean
	 */
	public boolean isMinimalBeamCurrentEnabled() {
		return bcEnabled;
	}

	/**
	 * <p>Setter for the field <code>minimalBeamCurrent</code>.</p>
	 *
	 * @param newMinimalBeamCurrent double
	 */
	public void setMinimalBeamCurrent(double newMinimalBeamCurrent) {
		newMinimalBeamCurrent = Math.abs(newMinimalBeamCurrent);
		if (newMinimalBeamCurrent!=minimalBeamCurrent) {
			minimalBeamCurrent=newMinimalBeamCurrent;
		}
	}

	/**
	 * <p>setMinimalBeamCurrentEnabled.</p>
	 *
	 * @param b a boolean
	 */
	public void setMinimalBeamCurrentEnabled(boolean b) {
		if (bcEnabled!=b) {
			bcEnabled=b;
		}
	}
	/**
	 * <p>testBeamCurrent.</p>
	 *
	 * @param operator a {@link org.scictrl.mp.orbitcorrect.correction.automatic.AutomaticOrbitCorrectionOperator} object
	 */
	public void testBeamCurrent(AutomaticOrbitCorrectionOperator operator) {
		if (allPass || !bcEnabled) return;
		try {
			double d = getBeamCurrent();
			if (d<minimalBeamCurrent) {
				operator.requestAbort("Beam current ("+Utilities.format2D(d)+") is below minimal level ("+Utilities.format2D(minimalBeamCurrent)+")!",this.getClass(),null);
				return;
			}
		} catch (Exception e) {
			log.error("Failed to read beam current: "+e, e);
			operator.requestAbort("Beam current can not be read!",this.getClass(),null);
		}
	}

	/**
	 * Trys to connect to info server and return current
	 *
	 * @throws java.lang.Exception if remote get fails
	 * @return a double
	 */
	public double getBeamCurrent() throws Exception {
		return getBeamCurrentConnector().get();
	}

	private IDataConnector<Double> getBeamCurrentConnector() throws ControlSystemException {
		if (beamCurrentConnector==null) {
			synchronized (this) {
				if (beamCurrentConnector==null) {
					beamCurrentConnector= getDataBush().getControlSystemEngine().connect(EControlSystemEngine.BEAM_CURRENT_NAME,true);
					synchronized (beamCurrentConnector) {
						try {
							beamCurrentConnector.wait(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return beamCurrentConnector;
	}

	/**
	 * Trys to connect to info server and return current
	 * @throws ControlSystemException
	 */
	double getLastBeamCurrent() {
		try {
			return getBeamCurrentConnector().getLatestReceivedValue();
		} catch (ControlSystemException e) {
			log.error("Failed to connect to beam current: "+e.toString(),e);
			return 0.0;
		}
	}

	private IDataConnector<Double> getRFSteppingConnector() throws ControlSystemException {
		if (rfSteppingConnector==null) {
			synchronized (this) {
				if (rfSteppingConnector==null) {
					rfSteppingConnector= getDataBush().getControlSystemEngine().connect(EControlSystemEngine.RF_STEPPING_NAME,true);
				}
			}
		}
		return rfSteppingConnector;
	}

	/**
	 * <p>isRFStepping.</p>
	 *
	 * @return a boolean
	 * @throws java.lang.Exception if any.
	 */
	public boolean isRFStepping() throws Exception {
		return getRFSteppingConnector().getLatestReceivedValue()>0.0;
	}

	/**
	 * <p>testOnStart.</p>
	 *
	 * @param operator a {@link org.scictrl.mp.orbitcorrect.correction.automatic.AutomaticOrbitCorrectionOperator} object
	 */
	public void testOnStart(AutomaticOrbitCorrectionOperator operator) {
		if (allPass) return;
		if (!(isCorrectHorizontal()||isCorrectVertical())) {
			operator.requestAbort("Select at least one, horizontal or vertical, correction",this.getClass(),null);
			return;
		}
		try {
			testDevices(operator, Orientation.H, true);
			testDevices(operator, Orientation.V, true);
			testMain(operator, true);
			testBeamCurrent(operator);
		} catch (Exception e1) {
			operator.requestAbort("Error: " + e1,this.getClass(),e1);
			log.error("Tests failed:"+e1.toString(), e1);
			return;
		}
	}

	/**
	 * <p>testDevices.</p>
	 *
	 * @param operator a {@link org.scictrl.mp.orbitcorrect.correction.automatic.AutomaticOrbitCorrectionOperator} object
	 * @param o a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @param fail a boolean
	 * @return a boolean
	 */
	public boolean testDevices(AutomaticOrbitCorrectionOperator operator, Orientation o, boolean fail) {
		if (allPass) return true;
		boolean ok=true;
		try {

			DataBush db= operator.getDataBush();
			if (db==null) {
				if (fail) {
					operator.requestAbort("ERROR: internal inconsistancy.",this.getClass(),null);
					return false;
				} else {
					operator.fireProgressReported(0,"ERROR: internal inconsistancy.");
					ok&=false;
				}
			}
			IControlSystemEngine cse= db.getControlSystemEngine();
			if (cse==null) {
				if (fail) {
					operator.requestAbort("ERROR: internal inconsistancy.",this.getClass(),null);
					return false;
				} else {
					operator.fireProgressReported(0,"ERROR: internal inconsistancy.");
					ok&=false;
				}
			}


			if (!checkSize(correctors[o.ordinal()],"correctors")) {
				if (fail) {
					operator.requestAbort("No "+o.getShortName()+" correcctors selected.",this.getClass(),null);
					return false;
				} else {
					operator.fireProgressReported(0,"FAIL: no "+o.getShortName()+" correcctors selected.");
					ok&=false;
				}
			} else {
				String[] failed= testPowerSupplies(correctors[o.ordinal()]);
				if (failed.length==0) {
					if (!fail) {
						operator.fireProgressReported(0, "OK for "+correctors[o.ordinal()].size()+" "+o.getShortName()+" correctors");
					}
				} else {
					if (fail) {
						operator.requestAbort("For "+failed.length+" "+o.getShortName()+" correctors fail: "+Arrays.toString(failed), this.getClass(), null);
						return false;
					} else {
						operator.fireProgressReported(0, "FAIL: "+failed.length+" "+o.getShortName()+" correctors test FAILS "+Arrays.toString(failed)+"!");
						log.error("For "+failed.length+" "+o.getShortName()+" correctors test FAILS "+Arrays.toString(failed)+"!");
						ok&=false;
					}
				}
			}

			if (!checkSize(bpms[o.ordinal()],"BPMs")) {
				if (fail) {
					operator.requestAbort("No "+o.getShortName()+" BPMs selected.",this.getClass(),null);
					return false;
				} else {
					operator.fireProgressReported(0,"FAIL: no "+o.getShortName()+" BPMs selected.");
					ok&=false;
				}
			} else {
				String[] failed= testBPMs(bpms[o.ordinal()]);
				if (failed.length==0) {
					if (!fail) {
						operator.fireProgressReported(0, "OK for "+bpms[o.ordinal()].size()+" "+o.getShortName()+" BPMs");
					}
				} else {
					if (fail) {
						operator.requestAbort("For "+bpms[o.ordinal()].size()+" "+o.getShortName()+" BPMs fail: "+Arrays.toString(failed), this.getClass(), null);
						return false;
					} else {
						operator.fireProgressReported(0, "FAIL: "+failed.length+" "+o.getShortName()+" BPMs test FAILS "+Arrays.toString(failed)+"!");
						log.error("For "+failed.length+" "+o.getShortName()+" BPMs test FAILS "+Arrays.toString(failed)+"!");
						ok&=false;
					}
				}
			}
			return ok;
		} catch (Exception e1) {
			operator.requestAbort("ERROR: " + e1,this.getClass(),e1);
			log.error("Tests failed:"+e1.toString(), e1);
			return false;
		}
	}

	/**
	 * <p>testMain.</p>
	 *
	 * @param operator a {@link org.scictrl.mp.orbitcorrect.correction.automatic.AutomaticOrbitCorrectionOperator} object
	 * @param fail a boolean
	 * @return a boolean
	 */
	public boolean testMain(AutomaticOrbitCorrectionOperator operator, boolean fail) {
		if (allPass) return true;
		boolean ok=true;
		try {

			DataBush db= operator.getDataBush();
			if (db==null) {
				if (fail) {
					operator.requestAbort("ERROR: internal inconsistancy.",this.getClass(),null);
					return false;
				} else {
					operator.fireProgressReported(0,"ERROR: internal inconsistancy.");
					ok&=false;
				}
			}
			IControlSystemEngine cse= db.getControlSystemEngine();
			if (cse==null) {
				if (fail) {
					operator.requestAbort("ERROR: internal inconsistancy.",this.getClass(),null);
					return false;
				} else {
					operator.fireProgressReported(0,"ERROR: internal inconsistancy.");
					ok&=false;
				}
			}

			PowerSupplyList l= db.getPowerSupplies();

			List<String> f= new ArrayList<>(l.size());

			Iterator<PowerSupply> i= l.iterator();

			while(i.hasNext()) {

				PowerSupply ps= i.next();

				if(ps.getDependingMagnets().size()>0 && ps.getDependingMagnets().get(0) instanceof AbstractCorrector) {
					continue;
				}

				try {
					if (!ps.test()) {
						f.add(ps.getName());
					}
				} catch (Exception e) {
					log.error("["+ps.getName()+"] testing failed: "+e.toString(), e);
					f.add(ps.getName());
				}
			}

			String[] failed= f.toArray(new String[f.size()]);

			if (failed.length==0) {
				if (!fail) {
					operator.fireProgressReported(0, "OK for main PSs");
				}
			} else {
				if (fail) {
					operator.requestAbort("For "+failed.length+" PS fail: "+Arrays.toString(failed), this.getClass(), null);
					return false;
				} else {
					operator.fireProgressReported(0, "FAIL: "+failed.length+" main PS test FAILS "+Arrays.toString(failed)+"!");
					log.error("For "+failed.length+" PS test FAILS "+Arrays.toString(failed)+"!");
					ok&=false;
				}
			}

			boolean test=false;
			try {
				test= db.getRFGenerator().getConnector().test();
			} catch (Exception e) {
				log.error("["+db.getRFGenerator().getName()+"] testing failed: "+e.toString(), e);
				test=false;
			}
			if (test) {
				if (!fail) {
					operator.fireProgressReported(0, "OK for RF signal");
				}
			} else {
				if (fail) {
					operator.requestAbort("RF signal fail: "+db.getRFGenerator().getName(), this.getClass(), null);
					return false;
				} else {
					operator.fireProgressReported(0, "FAIL: RF signal test FAILS "+db.getRFGenerator().getName()+"!");
					log.error("RF signal test FAILS "+db.getRFGenerator().getName()+"!");
					ok&=false;
				}
			}

			test=false;
			try {
				test= getBeamCurrentConnector().test();
			} catch (Exception e) {
				log.error("["+getBeamCurrentConnector().getName()+"] testing failed: "+e.toString(), e);
				test=false;
			}
			if (test) {
				if (!fail) {
					operator.fireProgressReported(0, "OK for Beam current");
				}
			} else {
				if (fail) {
					operator.requestAbort("Beam current fail: "+getBeamCurrentConnector().getName(), this.getClass(), null);
					return false;
				} else {
					operator.fireProgressReported(0, "FAIL: Beam current test FAILS "+getBeamCurrentConnector().getName()+"!");
					log.error("Beam current test FAILS "+getBeamCurrentConnector().getName()+"!");
					ok&=false;
				}
			}

			return ok;
		} catch (Exception e1) {
			operator.requestAbort("ERROR: " + e1,this.getClass(),e1);
			log.error("Tests failed:"+e1.toString(), e1);
			return false;
		}
	}

	/**
	 * <p>checkDevices.</p>
	 *
	 * @param operator a {@link org.scictrl.mp.orbitcorrect.correction.automatic.AutomaticOrbitCorrectionOperator} object
	 * @param o a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @param fail a boolean
	 * @return a boolean
	 */
	public boolean checkDevices(AutomaticOrbitCorrectionOperator operator, Orientation o, boolean fail) {
		if (allPass) return true;
		boolean ok=true;
		try {

			DataBush db= operator.getDataBush();
			if (db==null) {
				if (fail) {
					operator.requestAbort("ERROR: internal inconsistancy.",this.getClass(),null);
					return false;
				} else {
					operator.fireProgressReported(0,"ERROR: internal inconsistancy.");
					ok&=false;
				}
			}
			IControlSystemEngine cse= db.getControlSystemEngine();
			if (cse==null) {
				if (fail) {
					operator.requestAbort("ERROR: internal inconsistancy.",this.getClass(),null);
					return false;
				} else {
					operator.fireProgressReported(0,"ERROR: internal inconsistancy.");
					ok&=false;
				}
			}


			if (!checkSize(correctors[o.ordinal()],"correctors")) {
				if (fail) {
					operator.requestAbort("No "+o.getShortName()+" correcctors selected.",this.getClass(),null);
					return false;
				} else {
					operator.fireProgressReported(0,"FAIL: no "+o.getShortName()+" correcctors selected.");
					ok&=false;
				}
			} else {
				String[] failed= checkPowerSupplies(correctors[o.ordinal()]);
				if (failed.length!=0) {
					if (fail) {
						operator.requestAbort("Some "+o.getShortName()+" correctors fail: "+Arrays.toString(failed), this.getClass(), null);
						return false;
					} else {
						operator.fireProgressReported(0, "FAIL: some "+o.getShortName()+" correctors check FAILS "+Arrays.toString(failed)+"!");
						log.error("For some "+o.getShortName()+" correctors check FAILS "+Arrays.toString(failed)+"!");
						ok&=false;
					}
				}
			}

			if (!checkSize(bpms[o.ordinal()],"BPMs")) {
				if (fail) {
					operator.requestAbort("No "+o.getShortName()+" BPMs selected.",this.getClass(),null);
					return false;
				} else {
					operator.fireProgressReported(0,"FAIL: no "+o.getShortName()+" BPMs selected.");
					ok&=false;
				}
			} else {
				String[] failed= checkBPMs(bpms[o.ordinal()]);
				if (failed.length!=0) {
					if (fail) {
						operator.requestAbort("Some "+o.getShortName()+" BPMs fail: "+Arrays.toString(failed), this.getClass(), null);
						return false;
					} else {
						operator.fireProgressReported(0, "FAIL: some "+o.getShortName()+" BPMs check FAILS "+Arrays.toString(failed)+"!");
						log.error("For some "+o.getShortName()+" BPMs check FAILS "+Arrays.toString(failed)+"!");
						ok&=false;
					}
				}
			}
			return ok;
		} catch (Exception e1) {
			if (fail) {
				operator.requestAbort("ERROR: " + e1,this.getClass(),e1);
			} else {
				operator.fireProgressReported(0,"ERROR: " + e1);
			}
			log.error("Tests failed:"+e1.toString(), e1);
			return false;
		}
	}

	/**
	 * <p>checkBeamCurrent.</p>
	 *
	 * @param operator a {@link org.scictrl.mp.orbitcorrect.correction.automatic.AutomaticOrbitCorrectionOperator} object
	 */
	public void checkBeamCurrent(AutomaticOrbitCorrectionOperator operator) {
		if (allPass || !bcEnabled) return;
		double d= getLastBeamCurrent();
		if (d<minimalBeamCurrent) {
			operator.requestAbort("Beam current ("+Utilities.format2D(d)+") is below minimal level ("+Utilities.format2D(minimalBeamCurrent)+")!",this.getClass(),null);
			return;
		}
	}

	/**
	 * Makes test of devices and returns array with names of failed devices.
	 *
	 * @return array with list of failed devices, never <code>null</code>
	 * @param bpms a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public String[] testBPMs(ElementList<BPMonitor> bpms) {
		List<String> f= new ArrayList<>(bpms.size());
		Iterator<BPMonitor> ei = bpms.iterator();
		BPMonitor bpm;
		while(ei.hasNext()) {
			bpm = ei.next();
			try {
				if (!bpm.test()) {
					f.add(bpm.getName());
				}
			} catch (Exception e) {
				log.error("["+bpm.getName()+"] testing failed: "+e.toString(), e);
				f.add(bpm.getName());
			}
		}
		return f.toArray(new String[f.size()]);
	}

	/**
	 * Makes quick local check of usability of devices and returns array with names of failed devices.
	 *
	 * @param bpms a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @return an array of {@link java.lang.String} objects
	 */
	public String[] checkBPMs(ElementList<BPMonitor> bpms) {
		List<String> f= new ArrayList<>(bpms.size());
		Iterator<BPMonitor> ei = bpms.iterator();
		BPMonitor bpm;
		while(ei.hasNext()) {
			bpm = ei.next();
			try {
				if (!bpm.isUseable()) {
					f.add(bpm.getName());
				}
			} catch (Exception e) {
				log.error("["+bpm.getName()+"] testing failed: "+e.toString(), e);
				f.add(bpm.getName());
			}
		}
		return f.toArray(new String[f.size()]);
	}

	/**
	 * Makes test of devices and returns array with names of failed devices.
	 *
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @return array with list of failed devices, never <code>null</code>
	 */
	public String[] testPowerSupplies(ElementList<AbstractCorrector> l) {
		List<String> f= new ArrayList<>(l.size());
		for (AbstractCorrector c: l) {
			try {
				if (!c.getPowerSupply().test()) {
					f.add(c.getName());
				}
			} catch (Exception e) {
				log.error("["+c.getName()+"] testing failed: "+e.toString(), e);
				f.add(c.getName());
			}
		}
		return f.toArray(new String[f.size()]);
	}

	/**
	 * Makes quick local check of usability of devices and returns array with names of failed devices.
	 *
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @return an array of {@link java.lang.String} objects
	 */
	public String[] checkPowerSupplies(ElementList<AbstractCorrector> l) {
		List<String> f= new ArrayList<>(l.size());
		for (AbstractCorrector c: l) {
			try {
				if (!c.getPowerSupply().isUseable()) {
					f.add(c.getName());
				}
			} catch (Exception e) {
				log.error("["+c.getName()+"] testing failed: "+e.toString(), e);
				f.add(c.getName());
			}
		}
		return f.toArray(new String[f.size()]);
	}

	/**
	 * <p>debugDataDump.</p>
	 *
	 * @param operator a {@link org.scictrl.mp.orbitcorrect.correction.automatic.AutomaticOrbitCorrectionOperator} object
	 */
	public void debugDataDump(AutomaticOrbitCorrectionOperator operator) {
		if (debugDataEnabled) {
			dumpData(operator);
		}
	}

	/**
	 * Dumps all available data into a file for debuggin purposes.
	 *
	 * @param operator a {@link org.scictrl.mp.orbitcorrect.correction.automatic.AutomaticOrbitCorrectionOperator} object
	 */
	public void dumpData(AutomaticOrbitCorrectionOperator operator) {

		PrintWriter pw = newDataFile();

		Utilities.printFormated(db, pw);

		Utilities.printDBInfo(db.getDataBushInfo(),pw);

		Utilities.printMagnets(db.getMagnets(),pw);
		pw.println();

		Utilities.printBeamData(db.getOptics(),pw);
		pw.println();

		Utilities.printMatrices(db.getTransfers(),pw);
		pw.println();

		pw.println("Selected H correctors "+correctors[0].size()+" [name, current, angle]");
		for (AbstractCorrector c: correctors[0]) {
			pw.print(c.getName());
			pw.print("\t");
			pw.print(c.getCurrent());
			pw.print("\t");
			pw.println(c.getAngle());
		}
		pw.println();

		pw.println("Selected V correctors "+correctors[1].size()+" [name, current, angle]");
		for (AbstractCorrector c: correctors[1]) {
			pw.print(c.getName());
			pw.print("\t");
			pw.print(c.getCurrent());
			pw.print("\t");
			pw.println(c.getAngle());
		}
		pw.println();

		pw.println("Selected H BPMs "+bpms[0].size()+" [name, X, Z]");
		for (BPMonitor c: bpms[0]) {
			pw.print(c.getName());
			pw.print("\t");
			pw.println(c.getBeamPos().toString());
		}
		pw.println();

		if (_automaticOperator!=null) {

			pw.println();
			pw.println("LAST CORRECTION DATA");
			pw.println();
			Correction r= _automaticOperator.getLastCorrectionH();
			if (r!=null) {
				r.print(pw);
			} else {
				pw.println("Last H correction not available.");
			}
			r= _automaticOperator.getLastCorrectionV();
			if (r!=null) {
				r.print(pw);
			} else {
				pw.println("Last V correction not available.");
			}
			Orbit o= _automaticOperator.getLastOrbit();
			if (o!=null) {
				o.print(pw);
			} else {
				pw.println("Last orbit  not available.");
			}



		}
		pw.close();
	}

	/**
	 * <p>Setter for the field <code>debugDataEnabled</code>.</p>
	 *
	 * @param b a boolean
	 */
	public void setDebugDataEnabled(boolean b) {
		this.debugDataEnabled=b;
	}

	/**
	 * <p>isDebugDataEnabled.</p>
	 *
	 * @return a boolean
	 */
	public boolean isDebugDataEnabled() {
		return debugDataEnabled;
	}

	/**
	 * <p>isCorrectHorizontal.</p>
	 *
	 * @return boolean
	 */
	public boolean isCorrectHorizontal() {
		return correctHorizontal;
	}
	/**
	 * <p>isCorrectVertical.</p>
	 *
	 * @return boolean
	 */
	public boolean isCorrectVertical() {
		return correctVertical;
	}
	/**
	 * <p>Setter for the field <code>correctHorizontal</code>.</p>
	 *
	 * @param newCorrectHorizontal boolean
	 */
	public void setCorrectHorizontal(boolean newCorrectHorizontal) {
		if (correctHorizontal != newCorrectHorizontal) {
			correctHorizontal = newCorrectHorizontal;
		}
	}
	/**
	 * <p>Setter for the field <code>correctVertical</code>.</p>
	 *
	 * @param newCorrectVertical boolean
	 */
	public void setCorrectVertical(boolean newCorrectVertical) {
		if (correctVertical != newCorrectVertical) {
			correctVertical = newCorrectVertical;
		}
	}

	/**
	 * <p>addCorrection.</p>
	 *
	 * @param ci a {@link org.scictrl.mp.orbitcorrect.correction.CorrectionInstruction} object
	 */
	public void addCorrection(CorrectionInstruction ci) {
		if (!corrections.isEmpty()) {
			if (corrections.getLast()==ci) {
				return;
			}
		}
		corrections.addLast(ci);

		while(corrections.size()>maxUndo) {
			corrections.removeFirst();
		}
	}

	/**
	 * Returns <code>true</code> if undo operation can be executed.
	 *
	 * @return <code>true</code> if undo operation can be executed
	 */
	public boolean canUndo() {
		return corrections.size()>0;
	}

}
