package org.scictrl.mp.orbitcorrect.correction.models;

import java.util.Iterator;

import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.Orientation;
import org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionModel;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
import org.scictrl.mp.orbitcorrect.model.optics.HorCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.PositionedData;
import org.scictrl.mp.orbitcorrect.model.optics.VerCorrector;
/**
 * <p>Abstract AbstractOCModel class.</p>
 *
 * @author igor@scictrl.com
 */
public abstract class AbstractOCModel implements IOrbitCorrectionModel {
	private Orientation ori;
	private static final String EMPTY_COR_LIST= "Correctors list is empty!";
	private static final String EMPTY_MON_LIST= "Monitors list is empty!";

	/** Model name */
	protected java.lang.String name = "OC Model";
	/** Debug flag */
	protected boolean debug= false;

	/**
	 * RingKeeper constructor comment.
	 */
	public AbstractOCModel() {
		super();
	}
	/** {@inheritDoc} */
	@Override
	public java.lang.String getName() {
		return name;
	}
	/** {@inheritDoc} */
	@Override
	public Orientation getOrientation() {
		return ori;
	}
	/**
	 * <p>read.</p>
	 *
	 * @return double
	 * @param data PositionedData
	 */
	protected double read(PositionedData data) {
		if (ori==Orientation.VERTICAL) return data.z();
		else return data.x();
	}
	/**
	 * <p>readOther.</p>
	 *
	 * @return double
	 * @param data PositionedData
	 */
	protected double readOther(PositionedData data) {
		if (ori==Orientation.VERTICAL) return data.x();
		else return data.z();
	}
	/**
	 * setCorrectors method comment.
	 *
	 * @param cor a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 */
	protected void testCorrectors(ElementList<AbstractCorrector> cor) throws InconsistentDataException {
		if ((cor==null)||(cor.size()==0)) throw new InconsistentDataException(EMPTY_COR_LIST);
		Iterator<AbstractCorrector> it= cor.iterator();
		if (ori==Orientation.HORIZONTAL) while (it.hasNext()&&!(it.next() instanceof HorCorrector)) throw new InconsistentDataException("Not all elements in list are of type HorCorrector!");
		else if (ori==Orientation.VERTICAL) while (it.hasNext()&&!(it.next() instanceof VerCorrector)) throw new InconsistentDataException("Not all elements in list are of type VerCorrector!");
		else throw new InconsistentDataException("Value "+ori+"is not valid orientation specifyer!");
	}
	/**
	 * <p>testMonitors.</p>
	 *
	 * @param monitors a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 */
	protected void testMonitors(ElementList<BPMonitor> monitors) throws InconsistentDataException {
		if ((monitors==null)||(monitors.size()==0)) throw new InconsistentDataException(EMPTY_MON_LIST);
		Iterator<BPMonitor> it= monitors.iterator();
		while (it.hasNext()&&!(it.next() instanceof BPMonitor)) throw new InconsistentDataException("Not all elements in list are of type BPMonitor!");
	}

	/** {@inheritDoc} */
	@Override
	public boolean isHorizontal() {
		return ori.isHorizontal();
	}
	/** {@inheritDoc} */
	@Override
	public boolean isVertical() {
		return ori.isVertical();
	}
	/** {@inheritDoc} */
	@Override
	public void initialize(Orientation ori) {
		this.ori=ori;
	}
}


