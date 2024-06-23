package org.scictrl.mp.orbitcorrect.model.optics;

import java.util.Iterator;

import org.scictrl.mp.orbitcorrect.DataBushInitializationException;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.accessories.DataPositionComparator;
import org.scictrl.mp.orbitcorrect.accessories.OpticsPositionComparator;
/**
 * This type contains all elements of <code>DataBush</code> and some information of it. Elements are contained in lists (except <code>DataBushInfo</code>),
 * information about machine functions are in <code>PositionedDataList</code>'s and <code>DispersionDataList</code>.
 *
 * For information about fields see comments at field types.
 */
class DataBushEnt {
	public DataBushInfo dataBushInfo;
	public RFGenerator rfGenerator;
	public OpticsList optics;
	public PowerSupplyList powerSupplies;
	public BPMonitorList monitors;
	public CorrectorList correctors;
	public VerCorrectorList verCorrectors;
	public HorCorrectorList horCorrectors;
	public TransferList transfers;
	public MagnetList magnets;
	public CalMagnetList calMagnets;
	public BendingList bendings;
	public BindedList binded;

	public PositionedDataList betaList;
	public PositionedDataList alphaList;
	public PositionedDataList qList;
	public DispersionDataList dispersionList;
/**
 * Constructor that constructs <code>DataBushEnt</code> with <code>DataBushHandler</code>.
 *
 * @param t a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBushHandler} object
 */
public DataBushEnt(DataBushHandler t) {
	super();
	optics = new OpticsList(t);
	powerSupplies= new PowerSupplyList(t);
	monitors= new BPMonitorList(t);
	correctors= new CorrectorList(t);
	horCorrectors= new HorCorrectorList(t);
	verCorrectors= new VerCorrectorList(t);
	transfers= new TransferList(t);
	magnets= new MagnetList(t);
	calMagnets= new CalMagnetList(t);
	bendings= new BendingList(t);
	betaList= new PositionedDataList(t);
	binded= new BindedList(t);
	alphaList= new PositionedDataList(t);
	qList= new PositionedDataList(t);
	dispersionList= new DispersionDataList(t);
}
/**
 * This method adds parameter's specified <code>DoubleList</code> to the field optics type <code>OpticalList</code>,
 * using its method <code>addAll(DoubleList)</code>. Then all lists (e.g. magnets, monitors, ...) gets elements of their type from
 * <code>DoubleList</code> of optics.
 *
 * @param l DoubleList
 * @throws org.scictrl.mp.orbitcorrect.DataBushInitializationException if any.
 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
 */
public void addAll(ElementList<AbstractDataBushElement> l) throws DataBushInitializationException, InconsistentDataException {
	optics.fillMatching(l);
	AbstractOpticalElement oel;
	Iterator<AbstractOpticalElement> it= optics.iterator();
	while (it.hasNext()) {
		(oel= it.next()).calculatePosition(optics);
		betaList.add(oel.getBeta());
		alphaList.add(oel.getAlpha());
		qList.add(oel.getQ());
		dispersionList.add(oel.getDispersion());
		if (oel instanceof AbstractTransferElement) {
				betaList.add(((AbstractTransferElement)oel).getBeta1());
				alphaList.add(((AbstractTransferElement)oel).getAlpha1());
				qList.add(((AbstractTransferElement)oel).getQ1());
				dispersionList.add(((AbstractTransferElement)oel).getDispersion1());
				betaList.add(((AbstractTransferElement)oel).getBeta2());
				alphaList.add(((AbstractTransferElement)oel).getAlpha2());
				qList.add(((AbstractTransferElement)oel).getQ2());
				dispersionList.add(((AbstractTransferElement)oel).getDispersion2());
		}
	}
	optics.sort(new OpticsPositionComparator());
	DataPositionComparator dpc= new DataPositionComparator();
	betaList.sort(dpc);
	alphaList.sort(dpc);
	qList.sort(dpc);
	dispersionList.sort(dpc);
	powerSupplies.fillMatching(l);
	monitors.fillMatching(optics);
	transfers.fillMatching(optics);
	magnets.fillMatching(transfers);
	calMagnets.fillMatching(magnets);
	correctors.fillMatching(calMagnets);
	horCorrectors.fillMatching(correctors);
	verCorrectors.fillMatching(correctors);
	bendings.fillMatching(calMagnets);
	binded.fillMatching(l);
}
/**
 * This method removes all elements from lists.
 */
public void clear() {
	if (dataBushInfo!=null) dataBushInfo.clear();
	dataBushInfo=null;
	if (rfGenerator!=null) rfGenerator.clear();
	rfGenerator=null;
	Iterator<AbstractOpticalElement> oit= optics.iterator();
	while (oit.hasNext()) oit.next().clear();
	if (optics!=null) optics.clear();
	if (powerSupplies!=null) {
		Iterator<PowerSupply> pit= powerSupplies.iterator();
		while (pit.hasNext()) pit.next().clear();
		powerSupplies.clear();
	}
	if (monitors!=null) monitors.clear();
	if (correctors!=null) correctors.clear();
	if (horCorrectors!=null) horCorrectors.clear();
	if (verCorrectors!=null) verCorrectors.clear();
	if (transfers!=null) transfers.clear();
	if (magnets!=null) magnets.clear();
	if (calMagnets!=null) calMagnets.clear();
	if (bendings!=null) bendings.clear();
	if (binded!=null) binded.clear();
	if (betaList!=null) betaList.clear();
	if (alphaList!=null) alphaList.clear();
	if (qList!=null) qList.clear();
	if (dispersionList!=null) dispersionList.clear();
}
}
