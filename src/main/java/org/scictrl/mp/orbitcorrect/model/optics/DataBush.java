package org.scictrl.mp.orbitcorrect.model.optics;

import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.scictrl.mp.orbitcorrect.ControlSystemException;
import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.DBString;
import org.scictrl.mp.orbitcorrect.DataBushEvent;
import org.scictrl.mp.orbitcorrect.DataBushInitializationException;
import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.IActionReportListener;
import org.scictrl.mp.orbitcorrect.IBeamSimulator;
import org.scictrl.mp.orbitcorrect.ICalculatorModelFactory;
import org.scictrl.mp.orbitcorrect.IConnectionListener;
import org.scictrl.mp.orbitcorrect.ICurrentApplyModel;
import org.scictrl.mp.orbitcorrect.IDataBushListener;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.accessories.DefaultBeamSimulator;
import org.scictrl.mp.orbitcorrect.accessories.DefaultCalculatorModelFactory;
import org.scictrl.mp.orbitcorrect.accessories.DefaultControlSystemEngine;
import org.scictrl.mp.orbitcorrect.accessories.DefaultCurrentApplyModel;
import org.scictrl.mp.orbitcorrect.model.IControlSystemEngine;
/**
 * DataBush holds together objects representation of physical view of Control System
 * (CS for short). It provides organized access to all elements, manages their
 * cooindependance and synchronization. DataBush also perform group operations and linear
 * optics calculations.
 * <br><br>
 * DataBush keeps persistance of basic settings and event listeners while DataBush is
 * initialized and cleared as many times as needed.
 *
 * @author igor@scictrl.com
 */
public class DataBush {


	private DataBushHandler treebeard;
	private DataBushEnt ent;
	private IListenerEnt lEnt;
	private LinearOpticsOperator linOp;

	

	private int status= DBConst.DB_EMPTY;
	double positionPrecision = DEFAULT_POSITION_PRECISION;
	private long minUpdateInterval= DEFAULT_MIN_UPDATE_INTERVAL;

	/**
	 * Enables/disables aditional debug output.
	 */
	public static boolean debug = false;
	/**
	 * DataBush default value for position precision.
	 */
	public static final double DEFAULT_POSITION_PRECISION= 1E-6;
	/**
	 * DataBush default value for PowerSupply's current precision.
	 */
	public static final double DEFAULT_CURRENT_PRECISION= 1E-5;
	/**
	 * DataBush default value for BPM's value precision.
	 */
	public static final double DEFAULT_BPM_POSITION_PRECISION= 1E-20;
	/**
	 * DataBush default value for minimal interval between linear optics updates.
	 */
	public static final long DEFAULT_MIN_UPDATE_INTERVAL= 500;

	private ICurrentApplyModel iCurrentApplyModel;
	private Object synchronizationLock= Boolean.valueOf(true);
	private ICalculatorModelFactory iCalculatorModelFactory;
	double currentPrecision;
	double bPMPositionPrecision;
	private double opticsLength = 0.0;
	private IBeamSimulator iBeamSimulator = new DefaultBeamSimulator();
	private IControlSystemEngine iControlSystemEngine;
	/**
	 * Constructs empty instance of DataBush. Status of DataBush is set to
	 * <code>DBConst.EMPTY</code>. All lists are accessible, but none of DataBush elements,
	 * since they don't exsists.
	 * <br><br>
	 * AbeanInitializer is initialized with DataBush. DataBush application by default does
	 * not need any aditional ABean initializations in order to work with Abeans.
	 *
	 * @see getStatus()
	 */
	public DataBush() {
		super();
		treebeard= new DataBushHandler(this,lEnt= new IListenerEnt(),synchronizationLock);
		treebeard.ent= ent= new DataBushEnt(treebeard);
		treebeard.linOp= linOp= new LinearOpticsOperator(treebeard,ent);
		iCurrentApplyModel= new DefaultCurrentApplyModel();
		iCalculatorModelFactory= new DefaultCalculatorModelFactory();
		debugOut("debug mode is on");
	}
	/**
	 * This method sets aborted flag on own <code>DataBushHandler</code> to true.
	 */
	public void abort() {
		treebeard.aborted=true;
	}
	/**
	 * Adds listener for performed actions on DataBush or its elements.
	 *
	 * @see removeActionReportListener(IActionReportListener)
	 * @see org.scictrl.mp.orbitcorrect.IActionReportListener
	 * @param listener a {@link org.scictrl.mp.orbitcorrect.IActionReportListener} object
	 */
	public void addActionReportListener(IActionReportListener listener) {
		lEnt.addARListener(listener);
	}
	/**
	 * Adds listener for connection events on DataBush elements.
	 *
	 * @param listener a {@link org.scictrl.mp.orbitcorrect.IConnectionListener} object
	 */
	public void addConnectionListener(IConnectionListener listener) {
		lEnt.addConListener(listener);
	}
	/**
	 * Adds listener to DataBush.
	 *
	 * @see removeDataBushListener(IDataBushListener)
	 * @see org.scictrl.mp.orbitcorrect.IDataBushListener
	 * @param listener a {@link org.scictrl.mp.orbitcorrect.IDataBushListener} object
	 */
	public void addDataBushListener(IDataBushListener listener) {
		lEnt.addDBListener(listener);
	}
	/**
	 * Sends values from all DataBush elements to the Control System. First are applied
	 * CalibratedMagnets to PoweSupplies, than currents on PowerSupplies to CS.
	 * Returns the code of element with best result returned.
	 * Apply is allways performed till last element, than all caught exceptions are thrown
	 * with one PackedDBException.
	 *
	 * @return a int the code of element with best result returned
	 * @throws java.lang.IllegalStateException thrown if status of DataBush equals <code>DB_EMPTY</code>
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException contains all exception thrown or caught during invoking apply on all elements
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException thrown before values are actully send to CS, if PowerSupply is assigned to more than one Magnet and diference betwean different desired current values is to high.
	 */
	public int apply() throws IllegalStateException, DataBushPackedException, InconsistentDataException {
		if (status==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);
		return treebeard.apply();
	}
	/**
	 * Perform fast apply for elements in list. Values are sent to CS one after another as fast
	 * as possible with asynchronus sets. If any exception was cought during proccess, it is
	 * rethrown with PackedDBException after all sets are finished.
	 * Returns the code of element with best result returned.
	 * <b>NOTE!</b> This method is not synchronized to synchronization lock.
	 *
	 * @return a int the code of element with best result returned
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException contains all exception thrown or caught during invoking apply on specified elements
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException contains all exception thrown or caught during invoking apply on specified elements
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException thrown before values are actully send to CS, if PowerSupply is assigned to more than one Magnet and diference betwean different desired current values is to high.
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 * @param cor a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 *  if any.
	 * @throws java.lang.IllegalStateException if any.
	 */
	public int applyFast(ElementList<AbstractDataBushElement> cor) throws IllegalStateException, DataBushPackedException, InconsistentDataException, ControlSystemException {
		if (status==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);
		return treebeard.applyFast(cor);
	}
	/**
	 * Clears all elements from DataBush. <code>status</code> of DataBush is
	 * set to <code>DB_EMPTY</code>. All listeners persist.
	 *
	 * @see getStatus()
	 */
	public synchronized void clear() {
		treebeard.aborted=true;
		if (treebeard!=null) treebeard.setUpdateMode(DBConst.UPDATE_MANUAL);
		if (ent!=null) ent.clear();
		if (linOp!=null) linOp.clear();
		setStatus(DBConst.DB_EMPTY);
		if (treebeard!=null) treebeard.clear();
		debugOut("DataBush is cleared!");
	}
	/**
	 * Connects all connectable DataBush elements to appropriate Abean objects.
	 * Returns the code of element with best result returned.
	 *
	 * @return a int the code of element with best result returned
	 * @throws java.lang.IllegalStateException thrown if status of DataBush equals <code>DB_EMPTY</code>
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException contains all exception thrown or caught during invoking connect on all elements
	 * @see disconnect()
	 */
	public synchronized int connect() throws IllegalStateException, DataBushPackedException {
		if (status==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);
		return treebeard.connect();
	}
	private void debugOut(String s) {
		if (debug) System.out.println("DB> "+s);
	}
	private DataBushInitializationException debugOut(DataBushInitializationException t) {
		if (debug) {
			System.out.println("DB> --- EXCEPTION ---");
			t.printStackTrace();
		}
		return t;
	}
	/**
	 * Disconnects all connectable DataBush elements.
	 * Returns the code of element with best result returned.
	 *
	 * @return a int the code of element with best result returned
	 * @throws java.lang.IllegalStateException thrown if status of DataBush equals <code>DB_EMPTY</code>
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException contains all exception thrown or caught during invoking disconnect on all elements
	 * @see connect()
	 */
	public synchronized int disconnect() throws IllegalStateException, DataBushPackedException {
		if (status==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);
		return treebeard.disconnect();
	}
	/**
	 * Returns closed orbit solution for alpha-function. This is convenience method for
	 * <code>getDataBushInfo().getAlpha()</code>.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PositionedData} closed orbit solution for alpha-function
	 */
	public PositionedData getAlpha() {
		return ent.dataBushInfo.getAlpha();
	}
	/**
	 * Returns list with values of alpha funtion for all optical elements in DataBush.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PositionedDataList} list with values of alpha funtion for all optical elements in DataBushect
	 */
	public PositionedDataList getAlphaList() {
		return ent.alphaList;
	}
	/**
	 * This method return <code>IBeamSimulator</code>, which is used. If nobody, returns <code>DefaultBeamSimulator</code>
	 *
	 * @return IBeamSimulator
	 */
	public IBeamSimulator getBeamSimulator() {
		if (iBeamSimulator==null) iBeamSimulator= new DefaultBeamSimulator();
		return iBeamSimulator;
	}
	/**
	 * Returns list with all bending magnets in DataBush.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.BendingList} list with all bending magnets
	 */
	public BendingList getBendings() {
		return ent.bendings;
	}
	/**
	 * Returns closed orbit solution for beta-function. This is convenience method for
	 * <code>getDataBushInfo().getBeta()</code>.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PositionedData} closed orbit solution for beta-function
	 */
	public PositionedData getBeta() {
		return ent.dataBushInfo.getBeta();
	}
	/**
	 * Returns list with values of beta-funtion for all optical elements in DataBush.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PositionedDataList} list with values of beta-funtion for all optical elements in DataBush
	 */
	public PositionedDataList getBetaList() {
		return ent.betaList;
	}
	/**
	 * Returns list with all IBindedElement elements in DataBush.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.BindedList} list with all IBindedElement elements in DataBush
	 */
	public BindedList getBinded() {
		return ent.binded;
	}
	/**
	 * Returns the BPMonitor at the specified index.
	 *
	 * @param index a int index of BPMmonitor to return.
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.BPMonitor} the BPMonitor at the specified index
	 */
	public BPMonitor getBPMonitor(int index) {
		return ent.monitors.get(index);
	}
	/**
	 * Returns list with all BPMonitor elements in DataBush.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.BPMonitorList} list with all BPMonitor elements in DataBush
	 */
	public BPMonitorList getBPMonitors() {
		return ent.monitors;
	}
	/**
	 * Returns array of BPMonitor elements at the specified indices.
	 *
	 * @param pick an array of {@link int} indices of BPMonitor elements to return.
	 * @return an array of {@link org.scictrl.mp.orbitcorrect.model.optics.BPMonitor} array of BPMonitor elements at the specified indices
	 */
	public BPMonitor[] getBPMonitors(int[] pick) {
		BPMonitor[] m= new BPMonitor[pick.length];
		for (int i=0; i<pick.length; i++) m[i]= ent.monitors.get(pick[i]);
		return m;
	}
	/**
	 * Returns precision of BPMonitor beam position value handling.
	 *
	 * @see setBPMPositionPrecision(double)
	 * @return a double precision of BPMonitor bean position value handling
	 */
	public double getBPMPositionPrecision() {
		return bPMPositionPrecision;
	}
	/**
	 * Returns factory for calibration calculators.
	 *
	 * @see setCalculatorModelFactory(ICalculatorModelFactory)
	 * @return a {@link org.scictrl.mp.orbitcorrect.ICalculatorModelFactory} factory for calibration calculators
	 */
	public ICalculatorModelFactory getCalculatorModelFactory() {
		if (iCalculatorModelFactory==null) iCalculatorModelFactory= new DefaultCalculatorModelFactory();
		return iCalculatorModelFactory;
	}
	/**
	 * Returns list with all AbstractCalibratedMagnet elements in DataBush.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.CalMagnetList} list with all AbstractCalibratedMagnet elements in DataBush
	 */
	public CalMagnetList getCalMagnets() {
		return ent.calMagnets;
	}
	/**
	 * This method return <code>IControlSystemEngine</code>, that is used. If no <code>IControlSystemEngine</code> is in use,
	 * method return default <code>IControlSystemEngine</code>.
	 *
	 * @return IControlSystemEngine
	 * @see org.scictrl.mp.orbitcorrect.model.IControlSystemEngine
	 */
	public IControlSystemEngine getControlSystemEngine() {
		if (iControlSystemEngine==null) {
			iControlSystemEngine= new DefaultControlSystemEngine();
		}
		return iControlSystemEngine;
	}
	/**
	 * Returns the AbstractCorrector at the specified index.
	 *
	 * @param index a int index of AbstractCorrector to return.
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector} the AbstractCorrector at the specified index
	 */
	public AbstractCorrector getCorrector(int index) {
		return ent.correctors.get(index);
	}
	/**
	 * Returns list with all AbstractCorrector elements in DataBush.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.CorrectorList} list with all AbstractCorrector elements in DataBush
	 */
	public CorrectorList getCorrectors() {
		return ent.correctors;
	}
	/**
	 * Returns array of AbstractCorrector elements at the specified indices.
	 *
	 * @param pick an array of {@link int} indices of AbstractCorrector elements to return.
	 * @return an array of {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector} array of AbstractCorrector elements at the specified indices
	 */
	public AbstractCorrector[] getCorrectors(int[] pick) {
		AbstractCorrector[] c= new AbstractCorrector[pick.length];
		for (int i=0; i<pick.length; i++) c[i]= ent.correctors.get(pick[i]);
		return c;
	}
	/**
	 * Returns model for applying current.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.ICurrentApplyModel} model for applying current
	 */
	public ICurrentApplyModel getCurrentApplyModel() {
		if (iCurrentApplyModel==null) iCurrentApplyModel= new DefaultCurrentApplyModel();
		return iCurrentApplyModel;
	}
	/**
	 * Returns the precision of PowerSupply electric current value handling.
	 *
	 * @see setCurrentPrecision(double)
	 * @return a double the precision of PowerSupply electric current value handling
	 */
	public double getCurrentPrecision() {
		return currentPrecision;
	}
	/**
	 * Returns AbstractDataBushElement with specified name. If not exsist, <code>null</code> is returned.
	 *
	 * @param name a {@link java.lang.String} the name of element to return
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement} AbstractDataBushElement with specified name
	 */
	public AbstractDataBushElement getDataBushElement(String name) {
		return treebeard.getElement(name);
	}
	/**
	 * Returns DataBushInfo element if present, otherwise <code>null</code>.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBushInfo} e;ement
	 */
	public DataBushInfo getDataBushInfo() {
		return ent.dataBushInfo;
	}
	/**
	 * Returns closed orbit solution for horizontal momentum dispersion function.
	 * This is convenience method for <code>getDataBushInfo().getDispersion()</code>.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.DispersionData} closed orbit solution for horizontal momentum dispersion function
	 */
	public DispersionData getDispersion() {
		return ent.dataBushInfo.getDispersion();
	}
	/**
	 * Returns list with values of horizontal momentum dispersion function for all optical
	 * elements in DataBush.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.DispersionDataList} list with values of horizontal momentum dispersion function for all optical elements in DataBush
	 */
	public DispersionDataList getDispersionList() {
		return ent.dispersionList;
	}
	/**
	 * Returns element with specified name, if contained in this DataBush. If not, null is reurned.
	 *
	 * @return element, if contained int this DataBush, <code>null</code> otherwise.
	 * @param name a {@link java.lang.String} object
	 */
	public AbstractDataBushElement getElement(String name) {
		if (status==DBConst.DB_EMPTY) return null;
		return treebeard.getElement(name);
	}

	/**
	 * <p>getLog.</p>
	 *
	 * @return a {@link org.apache.logging.log4j.Logger} object
	 */
	public Logger getLog() {
		return treebeard.log;
	}
	/**
	 * Returns transfer matrix of AbstractTransferElement with specified name. If there is no such
	 * element, null pointer exception is thrown.
	 *
	 * @param name a {@link java.lang.String} the name of element to return transfer matrix
	 * @return a {@link org.scictrl.mp.orbitcorrect.math.TransferMatrix} transfer matrix of AbstractTransferElement with specified name
	 */
	public org.scictrl.mp.orbitcorrect.math.TransferMatrix getElementMatrix(String name) {
		return ent.transfers.get(name).getMatrix();
	}
	/**
	 * Returns list with all HorCorrector elements in DataBush.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.HorCorrectorList} list with all HorCorrector elements in DataBush
	 */
	public HorCorrectorList getHorCorrectors() {
		return ent.horCorrectors;
	}
	/**
	 * Calculates machine functions on specified interval at <code>count</code> points. DataBush
	 * tries to spread the points as much equidistant as possible. It allways takes beginning
	 * and end position of each DataBush element in interval.
	 * <br><br>
	 * Result is stored in <code>dataArray</code> matrix which should have dimension
	 * 9&times;<code>count</code>. Rows with values are accessed with DBConst
	 * constants: <code>MF_POSITION</code>, <code>MF_Q_X</code>, <code>MF_Q_Z</code>, <code>MF_BETA_X</code>
	 * <code>MF_BETA_Z</code>, <code>MF_ALPHA_X</code>, <code>MF_ALPHA_Z</code>, <code>MF_D</code>,
	 * <code>MF_DP</code>.
	 *
	 * @return <code>dataArray</code> parameter
	 * @throws java.lang.IllegalStateException thrown if status of DataBush equals <code>DB_EMPTY</code>
	 * @throws java.lang.IllegalArgumentException thrown if dataArray does not mach specification or number of points is to low (at least one per optical element)
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException thrown if no closed orbit solutin exsists
	 * @see org.scictrl.mp.orbitcorrect.DBConst
	 * @param dataArray an array of {@link double} a 9&times;<code>count</code> matrix to recive the result
	 * @param oe1 a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement} the begining element of interval
	 * @param oe2 a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement} the ending element of interval
	 * @param count a int number of points machine functions to be calculated
	 */
	public double[][] getMachineFunctions(double[][] dataArray, AbstractOpticalElement oe1, AbstractOpticalElement oe2, int count) throws IllegalStateException, IllegalArgumentException, InconsistentDataException {
		if (status==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);
		return linOp.getMachineFunctions(dataArray,oe1,oe2,count);
	}
	/**
	 * Calculates machine functions on specified interval at <code>count</code> points. DataBush
	 * tries to spread the points as much equidistant as possible. It allways takes beginning
	 * and end position of each DataBush element in interval.
	 * <br><br>
	 * Result is stored in <code>dataVectors</code>. <code>dataVectors</code> must be array of 9 empty
	 * <code>java.util.Vector</code> objects. Vectors with machine function values
	 * are accessed with DBConst
	 * constants: <code>MF_POSITION</code>, <code>MF_Q_X</code>, <code>MF_Q_Z</code>, <code>MF_BETA_X</code>
	 * <code>MF_BETA_Z</code>, <code>MF_ALPHA_X</code>, <code>MF_ALPHA_Z</code>, <code>MF_D</code>,
	 * <code>MF_DP</code>.
	 *
	 * @return <code>dataVectors</code> parameter
	 * @throws java.lang.IllegalStateException thrown if status of DataBush equals <code>DB_EMPTY</code>
	 * @throws java.lang.IllegalArgumentException thrown if dataVectors does not mach specification or number of points is to low (at least one per optical element)
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException thrown if no closed orbit solutin exsists
	 * @see org.scictrl.mp.orbitcorrect.DBConst
	 * @see AbstractOpticalElement
	 * @param dataVectors an array of {@link java.util.List} array of 9 empty <code>java.util.Vector</code> objects
	 * @param oe1 a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement} the begining element of interval
	 * @param oe2 a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement} the ending element of interval
	 * @param count a int number of points machine functions to be calculated
	 */
	public List<Double>[] getMachineFunctions(List<Double>[] dataVectors, AbstractOpticalElement oe1, AbstractOpticalElement oe2, int count) throws IllegalStateException, IllegalArgumentException, InconsistentDataException {
		if (status==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);
		return linOp.getMachineFunctions(dataVectors,oe1,oe2,count);
	}
	/**
	 * Returns list with all AbstractMagnetElement elements in DataBush.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.MagnetList} list with all AbstractMagnetElement elements in DataBush
	 */
	public MagnetList getMagnets() {
		return ent.magnets;
	}
	/**
	 * Returns minimal optical path length. This is the shortest distance between any
	 * optical element beginning and end. For example shortest element length or shortest
	 * distance between two elements. Helps to determine minimum required number of
	 * points for machine function calculation.
	 *
	 * @throws java.lang.IllegalStateException thrown if status of DataBush equals <code>DB_EMPTY</code>
	 * @see getMachineFunctions
	 * @return a double minimal optical path length
	 */
	public double getMinFreeOpticalLength() throws IllegalStateException {
		if (status==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);
		return linOp.getMinPathLength();
	}
	/**
	 * Returns the minimal allowed interval in milliseconds between two linear optic calculations.
	 *
	 * @return a long the minimal allowed interval in milliseconds between two linear optic calculations
	 */
	public long getMinUpdateInterval() {
		return minUpdateInterval;
	}
	/**
	 * Returns list with all AbstractOpticalElement elements in DataBush.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.OpticsList} list with all AbstractOpticalElement elements in DataBush
	 */
	public OpticsList getOptics() {
		return ent.optics;
	}
	/**
	 * Returns length of orbit.
	 *
	 * @return a double length of orbit.
	 */
	public double getOpticsLength() {
		return opticsLength;
	}
	/**
	 * Returns the precision of element's position handling.
	 *
	 * @see AbstractOpticalElement#getPosition()
	 * @return a double the precision of element's position handling
	 */
	public double getPositionPrecision() {
		return positionPrecision;
	}
	/**
	 * Returns list with all PowerSupply elements in DataBush.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PowerSupplyList} list with all PowerSupply elements in DataBush
	 */
	public PowerSupplyList getPowerSupplies() {
		return ent.powerSupplies;
	}
	/**
	 * Returns tune of closed orbit.
	 * This is convenience method for <code>getDataBushInfo().getQ()</code>.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PositionedData} tune of closed orbit
	 */
	public PositionedData getQ() {
		return ent.dataBushInfo.getQ();
	}
	/**
	 * Returns list with values of phase of beta-functions, divided by 2 pi, for all optical
	 * elements in DataBush.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.PositionedDataList} list with values of phase of beta-functions, divided by 2 pi, for all optical elements in DataBush
	 */
	public PositionedDataList getQList() {
		return ent.qList;
	}
	/**
	 * Returns RFGenerator element if present, otherwise <code>null</code>.
	 *
	 * @see RFGenerator
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.RFGenerator} element
	 */
	public RFGenerator getRFGenerator() {
		return ent.rfGenerator;
	}
	/**
	 * Returns status of DataBush. Status has two possible values
	 * <ul>
	 * <li><code>DBConst.DB_EMPTY</code> - when DataBush in not
	 * initialized and contains no elements.</li>
	 * <li><code>DBConst.DB_OPERATIONAL</code> - after successful
	 * initialization</li>
	 * </ul>
	 *
	 * @return status of DataBush
	 * @see DBConst#DB_EMPTY
	 * @see DBConst#DB_OPERATIONAL
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Returns <code>true</code> if status of DataBush is <code>DBConst.DB_EMPTY</code> - when DataBush in not
	 * initialized and contains no elements.
	 *
	 * @return <code>true</code> if status of DataBush is <code>DBConst.DB_EMPTY</code>
	 * @see DBConst#DB_EMPTY
	 */
	public boolean isStatusEmpty() {
		return status == DBConst.DB_EMPTY;
	}
	/**
	 * Returns <code>true</code> if status of DataBush is <code>DBConst.DB_OPERATIONAL</code> - after successful
	 * initialization.
	 *
	 * @return <code>true</code> if status of DataBush is <code>DBConst.DB_OPERATIONAL</code>
	 * @see DBConst#DB_OPERATIONAL
	 */
	public boolean isStatusOperational() {
		return status == DBConst.DB_OPERATIONAL;
	}

	/**
	 * Returns synchronization lock for DataBush. All DataBush updating, applying is
	 * locked inside this lock.
	 *
	 * @return synchronization lock for DataBush
	 */
	public Object getSynchronizationLock() {
		return synchronizationLock;
	}
	/**
	 * Returns list with all AbstractTransferElement elements in DataBush.
	 *
	 * @see TransferList
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.TransferList} list with all AbstractTransferElement elements in DataBush
	 */
	public TransferList getTransfers() {
		return ent.transfers;
	}
	/**
	 * Returns update mode code. Update mode controls how DataBush is updated.
	 *
	 * @see setUpdateMode(int)
	 * @see DBConst
	 * @return a int update mode code
	 */
	public synchronized int getUpdateMode() {
		return (status==DBConst.DB_EMPTY) ? DBConst.UPDATE_MANUAL : treebeard.getUpdateMode();
	}
	/**
	 * Returns list with all VerCorrector elements in DataBush.
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.VerCorrectorList} list with all VerCorrector elements in DataBush
	 */
	public VerCorrectorList getVerCorrectors() {
		return ent.verCorrectors;
	}
	/**
	 *
	 * @param message java.lang.String
	 */
	static void handleException(Throwable t) {
		System.out.println("DB> "+DBString.UNC_EX);
		t.printStackTrace();
	}
	/**
	 * Returns <code>true</code> if closed orbit solution was foud when last linear optics
	 * calculation was performed.
	 *
	 * @return <code>true</code> if closed orbit solution was foud when last linear optics
	 */
	public boolean hasClosedOrbitSolution() {
		if (status==DBConst.DB_OPERATIONAL) return linOp.getLastUpdateResult()!=DBConst.RC_INCONSISTANT_DATA;
		return false;
	}
	/**
	 * Initialize DataBush with elements from list. If initialization sucseds, DataBush become
	 * operational (<code>DB_OPERATIONAL</code>).
	 *
	 * @throws java.lang.IllegalStateException thrown if DataBush allready operational.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushInitializationException thrown, if DataBush was not able to initialize with given elements
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} list of elements to put in DataBush
	 */
	public synchronized void init(ElementList<AbstractDataBushElement> l)  throws IllegalStateException, DataBushInitializationException {
		if (status==DBConst.DB_OPERATIONAL) throw new IllegalStateException("DataBush allready initialized!");
		if (l==null) throw new DataBushInitializationException("ElementList is null");
		try {
			setRing(l);
			setStatus(DBConst.DB_OPERATIONAL);
		} catch (DataBushInitializationException e) {
			clear();
			throw e;
		}
	}
	/**
	 * This method perform <code>isSetFiresEvent()</code> method on own <code>DataBushHandler</code>.
	 *
	 * @see DataBushHandler#isSetFiresEvent()
	 * @return boolean
	 */
	public boolean isProperySetFiresEvent() {
		return treebeard.isSetFiresEvent();
	}
	/**
	 * Removes listener for performed actions on DataBush or its elements.
	 *
	 * @see addActionReportListener(IActionReportListener)
	 * @see org.scictrl.mp.orbitcorrect.IActionReportListener
	 * @param listener a {@link org.scictrl.mp.orbitcorrect.IActionReportListener} object
	 */
	public void removeActionReportListener(IActionReportListener listener) {
		lEnt.removeARListener(listener);
	}
	/**
	 * Removes listener for connection events on DataBush elements.
	 *
	 * @see addActionReportListener(IActionReportListener)
	 * @see org.scictrl.mp.orbitcorrect.IActionReportListener
	 * @param listener a {@link org.scictrl.mp.orbitcorrect.IConnectionListener} object
	 */
	public void removeConnectionListener(IConnectionListener listener) {
		lEnt.removeConListener(listener);
	}
	/**
	 * Removes listener from DataBush.
	 *
	 * @see removeDataBushListener(IDataBushListener)
	 * @see org.scictrl.mp.orbitcorrect.IDataBushListener
	 * @param listener a {@link org.scictrl.mp.orbitcorrect.IDataBushListener} object
	 */
	public void removeDataBushListener(IDataBushListener listener) {
		lEnt.removeDBListener(listener);
	}
	/**
	 *
	 * @param message java.lang.String
	 * @throws org.scictrl.mp.orbitcorrect.DataBushInitializationException if any.
	 */
	private void rethrowException(String message, Throwable t) throws DataBushInitializationException {
		String m= DBString.UNC_EX+" "+message;
		System.out.println("DB> "+m);
		t.printStackTrace();
		throw new DataBushInitializationException(m);
	}
	/**
	 * This method sets new <code>IBeamSimulator</code>
	 *
	 * @param newBeamSimulator IBeamSimulator
	 */
	public void setBeamSimulator(IBeamSimulator newBeamSimulator) {
		iBeamSimulator = newBeamSimulator;
	}
	/**
	 * Sets precision of BPMonitor beam position value handling. If in
	 * <code>UPDATE_ON_EVENT</code> update mode BPMonitor gets new value
	 * from CS, that is different from current less them precision, it is assumed as same and
	 * no event fires.
	 * <br><br>
	 * This value is independent of DataBush status. Can be seted at any time.
	 *
	 * @see getBPMPositionPrecision()
	 * @param precision a double new precision
	 */
	public void setBPMPositionPrecision(double precision) {
		this.bPMPositionPrecision = precision;
	}
	/**
	 * Sets the factory for calibration calculators. If none is seted, defaul one is used. To
	 * use default one, set to DataBush input file location. It is needed by default factory.
	 * <br><br>
	 * This property can be setted only vhen DataBush is empty, otherwise
	 * <code>IllegalStateException</code> is thrown.
	 *
	 * @throws java.lang.IllegalStateException thrown if status of DataBush equals <code>DB_OPERATIONAL</code>
	 * @see getCalculatorModelFactory()
	 * @param newValue a {@link org.scictrl.mp.orbitcorrect.ICalculatorModelFactory} new factory for calibration calculators
	 */
	public void setCalculatorModelFactory(ICalculatorModelFactory newValue) throws IllegalStateException {
		if (status==DBConst.DB_OPERATIONAL) throw new IllegalStateException(DBString.ISE_CALC_MODEL);
		this.iCalculatorModelFactory = newValue;
	}
	/**
	 * This method sets <code>IControlSystemEngine</code>.
	 *
	 * @see org.scictrl.mp.orbitcorrect.model.IControlSystemEngine
	 * @param newControlSystemEngine a {@link org.scictrl.mp.orbitcorrect.model.IControlSystemEngine} object
	 * @throws java.lang.IllegalStateException if any.
	 */
	public void setControlSystemEngine(IControlSystemEngine newControlSystemEngine) throws IllegalStateException {
		if (status==DBConst.DB_OPERATIONAL) throw new IllegalStateException(DBString.ISE_CSE);
		iControlSystemEngine = newControlSystemEngine;
	}
	/**
	 * Sets the model for applying current.
	 * <br><br>
	 * This value is independent of DataBush status. Can be seted at any time.
	 *
	 * @see getCurrentApplyModel()
	 * @param newValue a {@link org.scictrl.mp.orbitcorrect.ICurrentApplyModel} new model for applying current
	 */
	public void setCurrentApplyModel(ICurrentApplyModel newValue) {
		this.iCurrentApplyModel = newValue;
	}
	/**
	 * Sets the precision of PowerSupply electric current value handling.
	 * If in <code>UPDATE_ON_EVENT</code> update mode PowerSupply gets new value
	 * from CS, that is different from current one less then precision, it is assumed as same and
	 * no event fires.
	 * <br><br>
	 * This value is independent of DataBush status. Can be seted at any time.
	 *
	 * @see getCurrentPrecision()
	 * @param newValue a double new precision of PowerSupply current value handling
	 */
	public void setCurrentPrecision(double newValue) {
		this.currentPrecision = newValue;
	}
	/**
	 * Sets the minimal allowed interval in milliseconds between two linear optic calculations.
	 * This value is used in <code>UPDAT_ON_EVENT</code> update mode.
	 * <br><br>
	 * This value is independent of DataBush status. Can be seted at any time.
	 *
	 * @param newValue a long new minimal allowed interval in milliseconds
	 */
	public void setMinUpdateInterval(long newValue) {
		this.minUpdateInterval = newValue;
		if (status!=DBConst.DB_EMPTY) ent.dataBushInfo.setMinUpdateInterval(newValue);
	}
	void setOpticsLength(double newOpticsLength) {
		opticsLength = newOpticsLength;
	}
	/**
	 * Sets the precision of element's position handling. This value is used when element
	 * positions are recalculated. If one element ends and other starts within position of
	 * this precision, then is assumed, that there is no gap in between.
	 * <br><br>
	 * This value is independent of DataBush status. Can be seted at any time.
	 *
	 * @see AbstractOpticalElement#getPosition()
	 * @param newValue a double
	 */
	public void setPositionPrecision(double newValue) {
		if (status==DBConst.DB_EMPTY)
			if (newValue>0.0) this.positionPrecision = newValue;
	}
	/**
	 * This method perform <code>setSetFiresEvent()</code> method on own <code>DataBushHandler</code>.
	 *
	 * @param newSetProperyFiresEvent a boolean
	 */
	public void setProperySetFiresEvent(boolean newSetProperyFiresEvent) {
		treebeard.setSetFiresEvent(newSetProperyFiresEvent);
	}
	/**
	 *
	 * @param list DataBushInterface.ElementList
	 */
	private void setRing(ElementList<AbstractDataBushElement> l) throws DataBushInitializationException {
		DataBushInfo[] info;
		RFGenerator[] rf;
		debugOut("Initialization Started");
		try {
			info= l.getDataBushInfo();
		} catch (Throwable e) {
			info=null;
			rethrowException("extracting DataBushInfo ",e);
		}

		if (info.length==0) throw new DataBushInitializationException("Input ElementList contains no DataBushInfo element");
		if (info.length>1) throw new DataBushInitializationException("Input ElementList contains more than one DataBushInfo element");

		try {
			rf= l.getRFGenerator();
		} catch (Throwable e) {
			rf=null;
			rethrowException("extracting RFGenerator",e);
		}

		if (rf.length>1) throw new DataBushInitializationException("Input ElementList contains more than one RFGenerator element");

		try {
			if (rf.length==1) ent.rfGenerator=rf[0];
			ent.dataBushInfo= info[0];
			ent.dataBushInfo.setMinUpdateInterval(minUpdateInterval);
			ent.addAll(l);
			debugOut("element and data lists created");
		} catch (DataBushInitializationException e) {
			throw debugOut(e);
		} catch (InconsistentDataException e) {
			throw debugOut(new DataBushInitializationException(e));
		} catch (Throwable e) {
			rethrowException("initializating DataBushInfo, PowerSupplies, Optics",e);
		}

		if (l.size()>1) {
			try {
				linOp.assambleOpticsLine();
				debugOut("linear optics calculation structure created");
			} catch (InconsistentDataException e) {
				throw debugOut(new DataBushInitializationException(e));
			} catch (Throwable e) {
				rethrowException("assambling optics line",e);
			}
		} else {
			throw new DataBushInitializationException("Not enough elements to assamble optics model.");
		}

		try {
			Iterator<PowerSupply> pit= ent.powerSupplies.iterator();
			while (pit.hasNext()) pit.next().init(treebeard);
			debugOut("PowerSupply elements initialized");
		} catch (DataBushInitializationException e) {
			throw debugOut(e);
		} catch (Throwable e) {
			rethrowException("initializating power supplies",e);
		}

		try {
			Iterator<AbstractOpticalElement> oit= ent.optics.iterator();
			while (oit.hasNext())
				oit.next().init(treebeard);
			debugOut("optical elements initialized");
		} catch (DataBushInitializationException e) {
			throw debugOut(e);
		} catch (Throwable e) {
			rethrowException("initializating optical elements",e);
		}

		try {
			ent.dataBushInfo.init(treebeard);
			debugOut("DataBushInfo element initialized");
		} catch (DataBushInitializationException e) {
			throw debugOut(e);
		} catch (Throwable e) {
			rethrowException("initializating DataBushInfo",e);
		}

		if (ent.rfGenerator!=null) {
			try {
				ent.rfGenerator.init(treebeard);
				debugOut("RFGenerator element initialized");
			} catch (DataBushInitializationException e) {
				throw debugOut(e);
			} catch (Throwable e) {
				rethrowException("initializating RFGenerator",e);
			}
		}

		debugOut("Initialization finished");
	}
	/**
	 *
	 * @param newValue byte
	 */
	private void setStatus(int newValue) {
		this.status = newValue;
		lEnt.statusChanged(new DataBushEvent(this,DBString.STATUS_CHANGE+" ("+DBConst.DB_STATUS_STRINGS[status]+")"));
	}
	/**
	 * Sets update mode code. Update mode controls how DataBush is updated. There are two
	 * update modes:
	 * <ul>
	 * <li><code>DBConst.UPDATE_MANUAL</code> - DataBush is updated,
	 * when update method is called. This is defualt mode.</li>
	 * <li><code>DBConst.UPDATE_ON_EVENT</code> - DataBush is updated upon
	 * monitor events from Abean family. Manual updating will thow an exception.</li>
	 * </ul>
	 * <br><br>
	 * This operation can be preformed only on operational DataBush. If DataBush is empty,
	 * When DataBush is cleared, update mode is reset to <code>UPDATE_MANUAL</code>.
	 * <code>IllegalStateException</code> will be thrown.
	 *
	 * @throws java.lang.IllegalStateException thrown if status of DataBush equals <code>DB_EMPTY</code>
	 * @see getUpdateMode()
	 * @see org.scictrl.mp.orbitcorrect.DBConst
	 * @param mode a int update mode code
	 */
	public synchronized void setUpdateMode(int mode) throws IllegalStateException {
		if (status==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);
		if ((mode!=DBConst.UPDATE_MANUAL)&(mode!=DBConst.UPDATE_ON_EVENT)) throw new IllegalArgumentException(DBString.IAE_WRONG_UPDATE_MODE+mode);
		treebeard.setUpdateMode(mode);
		lEnt.statusChanged(new DataBushEvent(this,DBString.UPDATE_MODE_CHANGE+" ("+DBConst.UPDATE_MODE_STRINGS[mode]+")"));
	}
	/**
	 * Symulates BPM beam positions with closed orbit calculation.
	 * This operation can be preformed only on operational DataBush. If DataBush is empty,
	 * When DataBush is cleared, update mode is reset to <code>UPDATE_MANUAL</code>.
	 * <code>IllegalStateException</code> will be thrown.
	 *
	 * @throws java.lang.IllegalStateException thrown if status of DataBush equals <code>DB_EMPTY</code>
	 */
	public void simulateBPMs() throws IllegalStateException {
		if (status==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);
		treebeard.simulateBPMs();
	}
	/**
	 * Tests power supplies or magnets in lust if are usable.
	 *
	 * @return if <code>true</code> if all powers supplies are usable, <code>false</code> otherwise
	 * @throws java.lang.IllegalArgumentException thrown if lust contains element, which is not <code>PowerSupply</code> nor <code>AbstractMagnetElement</code> element
	 * @throws java.lang.IllegalStateException thrown if status of DataBush equals <code>DB_EMPTY</code>
	 * @param list a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public boolean testPowerSupplies(ElementList<AbstractDataBushElement> list) throws IllegalArgumentException, IllegalStateException {
		if (status==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);
		AbstractDataBushElement el;
		for (AbstractDataBushElement element : list) {
			if ((el= element) instanceof PowerSupply) {
				if (!((PowerSupply)el).isUseable()) return false;
			} else if (el instanceof AbstractMagnetElement) {
				if (!((AbstractMagnetElement)el).getPowerSupply().isUseable()) return false;
			} else throw new IllegalArgumentException("element \""+el.getName()+"\" is not PowerSupply or AbstractMagnetElement");
		}
		return true;
	}
	/**
	 * {@inheritDoc}
	 *
	 * Returns string representation of DataBush. First is returned string representation of
	 * DataBushInfo element, than all optical element in optical order and than power supplies.
	 * Returned string if formated and can be used as input for
	 * <code>DefaultDBReader</code>.
	 */
	@Override
	public String toString() {
		return (status==DBConst.DB_EMPTY) ? "" : ent.dataBushInfo.toString()+(ent.rfGenerator !=null ? ent.rfGenerator.toString() : "")+ent.optics.toString()+ent.powerSupplies.toString();
	}
	/**
	 * This method returns first optical element to last.
	 *
	 * @return TransferMatrix
	 * @see org.scictrl.mp.orbitcorrect.math.TransferMatrix
	 * @param first a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement} object
	 * @param last a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement} object
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 */
	public org.scictrl.mp.orbitcorrect.math.TransferMatrix transferMatrix(AbstractOpticalElement first, AbstractOpticalElement last) throws IllegalStateException, InconsistentDataException {
		if (status==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);
		return linOp.calculateMatrix(
			(first instanceof AbstractTransferElement) ? (AbstractTransferElement)first : first,
			(last instanceof AbstractTransferElement) ? (AbstractTransferElement)last : last);
	}
	/**
	 * This method returns first optical element to last. Elements are represented by <code>SimpleData</code>
	 *
	 * @return TransferMatrix
	 * @see org.scictrl.mp.orbitcorrect.math.TransferMatrix
	 * @param first a {@link org.scictrl.mp.orbitcorrect.model.optics.SimpleData} object
	 * @param last a {@link org.scictrl.mp.orbitcorrect.model.optics.SimpleData} object
	 * @throws java.lang.IllegalStateException if any.
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 */
	public org.scictrl.mp.orbitcorrect.math.TransferMatrix transferMatrix(SimpleData first, SimpleData last) throws IllegalStateException, InconsistentDataException {
		if (status==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);
		return linOp.calculateMatrix(first,last);
	}
	/**
	 * Reads values from Control System and updates DataBush elements. First are updated
	 * power supplies, BPM's nad DataBushInfo. Than magnets and linear optics data.
	 * Returns the code of element with best result returned.
	 * Update is allways performed till last element, than all caught exceptions are thrown
	 * with one PackedDBException.
	 *
	 * @throws java.lang.IllegalStateException thrown if status of DataBush equals <code>DB_EMPTY</code>
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException contains all exception thrown or caught during updating all elements
	 * @return a int the code of element with best result returned
	 */
	public int update() throws IllegalStateException, DataBushPackedException {
		if (status==DBConst.DB_EMPTY) throw new IllegalStateException(DBString.ISE_DB_STATE);
		return treebeard.update();
	}
}
