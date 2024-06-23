package org.scictrl.mp.orbitcorrect.correction.models;

import java.util.Iterator;

import org.scictrl.mp.orbitcorrect.DataBushAdapter;
import org.scictrl.mp.orbitcorrect.DataBushEvent;
import org.scictrl.mp.orbitcorrect.DataBushPackedException;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.correction.Correction;
import org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
import org.scictrl.mp.orbitcorrect.utilities.Orbit;
import org.scictrl.mp.orbitcorrect.utilities.ResponseMatrix;
/**
 * <p>EmpiricOrbitCorrectionModel class.</p>
 *
 * @author igor@scictrl.com
 */
public class EmpiricOrbitCorrectionModel extends AbstractSVDOrbitCorrectionModel {
	private double deltaAngle = 0.2;
	private int averageBPMsOver = 10;
	private ResponseMatrix couplingResponseMatrix= new ResponseMatrix(0,0);
	/**
	 * RingKeeper constructor comment.
	 */
	public EmpiricOrbitCorrectionModel() {
		super();
		name= "Empiric OC";
	}

	/**
	 *
	 *
	 * @return ResponceMatrix
	 * @param showProgress boolean
	 */
	private ResponseMatrix _measureResponseMatrix(boolean showProgress, ElementList<AbstractCorrector> correctors, ElementList<BPMonitor> monitors, DataBush databush) throws DataBushPackedException {
		class Ticker extends DataBushAdapter {
			int tick=0;
			int ticks=0;
			double[][] store;
			double[][] oStore;
			ElementList<BPMonitor> bpms;
			DataBush db;
			Thread t;
			Ticker(DataBush db) {
				this.db=db;
			}
			public void tick(int ticks, double[][] store, double[][] oStore, ElementList<BPMonitor> bpms) {
				tick=0;
				this.ticks=ticks;
				if (tick==ticks) return;
				this.store=store;
				this.oStore=oStore;
				this.bpms=bpms;
				System.out.print("TICKING>");
				store();
				if (tick==ticks) return;
				t=Thread.currentThread();
				db.addDataBushListener(this);
				try {
					synchronized(t){
						t.wait();
					}
				} catch (InterruptedException e) {e.printStackTrace();}
			}
			void store() {
				for (int i=0; i<store.length; i++) {
					store[i][tick]=read(bpms.get(i).getBeamPos());
					oStore[i][tick]=readOther(bpms.get(i).getBeamPos());
				}
				System.out.print(" ["+(tick++)+"]");
			}
			@Override
			public void beamChanged(DataBushEvent e) {
				if (tick==ticks) {
					db.removeDataBushListener(this);
					System.out.println();
					synchronized(t){t.notify();}
					return;
				}
				store();
				if (tick==ticks) {
					db.removeDataBushListener(this);
					System.out.println();
					synchronized(t){t.notify();}
				}
			}
		}
		Ticker ticker = new Ticker(databush);
		int mc = monitors.size();
		int cc = correctors.size();

		ResponseMatrix responseMatrix= new ResponseMatrix(mc,cc);
		couplingResponseMatrix= new ResponseMatrix(mc,cc);

		int i=0;
		Iterator<BPMonitor> mit= monitors.iterator();
		AbstractDataBushElement el;
		while (mit.hasNext()) {
			responseMatrix.bpms[i]=(el=mit.next()).getName();
			couplingResponseMatrix.bpms[i++]=el.getName();
		}
		i=0;
		Iterator<AbstractCorrector> cit= correctors.iterator();
		while (cit.hasNext()) {
			responseMatrix.correctors[i]=(el=cit.next()).getName();
			couplingResponseMatrix.correctors[i++]=el.getName();
		}

		ElementList<AbstractCorrector> cl= new ElementList<>(cc);
		ElementList<BPMonitor> ml= new ElementList<>(mc);

		for (int k=0; k<cc; k++) cl.add(databush.getCorrectors().get(responseMatrix.correctors[k]));
		for (int k=0; k<mc; k++) ml.add(databush.getBPMonitors().get(responseMatrix.bpms[k]));

		double[][] avg = new double[mc][averageBPMsOver];
		double[][] oAvg = new double[mc][averageBPMsOver];

		double[] initialPos= new double[mc];
		double[] oInitialPos= new double[mc];

	// measure initial BPM positions

		ticker.tick(averageBPMsOver,avg,oAvg,ml);

		for (int k=0; k<mc; k++) {
			for (int l=0; l<averageBPMsOver; l++) {
				initialPos[k]+=avg[k][l];
				oInitialPos[k]+=oAvg[k][l];
			}
			initialPos[k]= initialPos[k]/averageBPMsOver;
			oInitialPos[k]= oInitialPos[k]/averageBPMsOver;
		}

	// measure response matrix step by step

		double a=0;
		double d=0;
		double od=0;
		AbstractCorrector cor=null;

		i=-1;
		cit= cl.iterator();
		while (cit.hasNext()) {
			cor= cit.next();
			i++;

			System.out.println("EOC> measuring "+cor.getName());

			a= cor.getAngle();
			synchronized (databush.getSynchronizationLock()) {
				cor.setAngle(a+deltaAngle);
				cor.applyAngle();
				try {
					cor.getPowerSupply().applyCurrent();
				} catch(DataBushPackedException ex) {
					cor.setAngle(a);
					cor.applyAngle();
					try {
						cor.getPowerSupply().applyCurrent();
					} catch (DataBushPackedException ex1) {
						throw ex;
					}
				}
			}
			try {
				Thread.currentThread();
				Thread.sleep(20000);
			} catch (InterruptedException e) {e.printStackTrace();}

	//		if (Math.abs(cor.getAngle()-a-deltaAngle)>0.0001) {
	//			throw new PackedDBException(cor,"Angle "+cor.getAngle()+" of "+cor.getName()+" corrector differs from seted value "+(a+deltaAngle)+"!",DBConst.RC_REMOTE_EXCEPTION);
	//		}

			ticker.tick(averageBPMsOver,avg,oAvg,ml);

			for (int k=0; k<mc; k++) {
				d=0;
				od=0;
				for (int l=0; l<averageBPMsOver; l++) {
					d+=avg[k][l];
					od+=oAvg[k][l];
				}
				d= d/averageBPMsOver;
				od= od/averageBPMsOver;
				responseMatrix.matrix[k][i]=(d-initialPos[k])/deltaAngle;
				couplingResponseMatrix.matrix[k][i]=(od-oInitialPos[k])/deltaAngle;
			}

			synchronized (databush.getSynchronizationLock()) {
				cor.setAngle(a);
				cor.applyAngle();
				try {
					cor.getPowerSupply().applyCurrent();
				} catch(DataBushPackedException ex) {
					cor.setAngle(a);
					cor.applyAngle();
					try {
						cor.getPowerSupply().applyCurrent();
					} catch (DataBushPackedException ex1) {
						throw ex;
					}
				}
			}
		}

		//getVisualComponent().firePropertyChange(RESPONSE_MATRIX_PROPERTY,0,1);

		return responseMatrix;
	}
	/**
	 * <p>Getter for the field <code>averageBPMsOver</code>.</p>
	 *
	 * @return int
	 */
	public int getAverageBPMsOver() {
		return averageBPMsOver;
	}
	/**
	 * <p>getCoupledResponseMatrix.</p>
	 *
	 * @return ResponseMatrix
	 */
	public ResponseMatrix getCoupledResponseMatrix() {
		return couplingResponseMatrix;
	}
	/**
	 * <p>Getter for the field <code>deltaAngle</code>.</p>
	 *
	 * @return double
	 */
	public double getDeltaAngle() {
		return deltaAngle;
	}
	/** {@inheritDoc} */
	@Override
	protected Correction makeCorrection(ElementList<AbstractCorrector> correctors, Orbit orbit, ResponseMatrix responseMatrix, DataBush db, OrbitCorrectionOperator oc) throws InconsistentDataException {

		testMonitors(orbit.getBPMs());
		testCorrectors(correctors);

		if ((responseMatrix==null)||(responseMatrix.bpms==null)||(responseMatrix.correctors==null)) matrixNotSynchronized(correctors,orbit.getBPMs(),db);

		int ms = orbit.getBPMs().size();
		int cs = correctors.size();
		int i=0;

		String[] bpms= new String[ms];
		String[] cors= new String[cs];

		for (i=0; i<ms; i++) bpms[i]= orbit.getBPMs().get(i).getName();
		for (i=0; i<cs; i++) cors[i]= correctors.get(i).getName();

		ResponseMatrix rm= null;

		while (true) {
			try {
				rm= responseMatrix.submatrix(bpms,cors);
			} catch (IllegalArgumentException e) {
				responseMatrix= matrixNotSynchronized(correctors,orbit.getBPMs(),db);
				continue;
			}
			break;
		}

		return super.makeCorrection(correctors,orbit,rm,db,oc);

	}
/**
 *
 */
private ResponseMatrix matrixNotSynchronized(ElementList<AbstractCorrector> correctors, ElementList<BPMonitor> monitors, DataBush databush) throws InconsistentDataException {
	if (javax.swing.JOptionPane.OK_OPTION==javax.swing.JOptionPane.showConfirmDialog(null,"Response matrix is not consistent with selected Correctors \nand BPMonitors.\n\nPress OK to measure new response matrix or Cancel to abort the correction!","Empiric IOrbitCorrectionModel Question",javax.swing.JOptionPane.OK_CANCEL_OPTION)) {
		try {
			return _measureResponseMatrix(false,correctors,monitors,databush);
		} catch (DataBushPackedException e) {throw new InconsistentDataException(e.getMessage());}
	} else throw new InconsistentDataException("User aborted!");

}
	/**
	 * <p>measureResponseMatrix.</p>
	 *
	 * @return ResponceMatrix
	 * @param showProgress boolean
	 * @param correctors a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @param monitors a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @param databush a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} object
	 * @throws org.scictrl.mp.orbitcorrect.DataBushPackedException if any.
	 */
	public synchronized ResponseMatrix measureResponseMatrix(boolean showProgress, ElementList<AbstractCorrector> correctors, ElementList<BPMonitor> monitors, DataBush databush) throws DataBushPackedException {
		if ((correctors==null)||(monitors==null)||(correctors.size()==0)||(monitors.size()==0)) {
			javax.swing.JOptionPane.showMessageDialog(null,"Seems that there is no correctors or BPMs selected.\nPress 'Calculate Correction' button to update selection.","No selection",javax.swing.JOptionPane.INFORMATION_MESSAGE);
			return new ResponseMatrix(0,0);
		}
		return _measureResponseMatrix(showProgress,correctors,monitors,databush);
	}
	/**
	 * <p>Setter for the field <code>averageBPMsOver</code>.</p>
	 *
	 * @param newAverageBPMsOver int
	 */
	public synchronized void setAverageBPMsOver(int newAverageBPMsOver) {
		if (averageBPMsOver != newAverageBPMsOver) {
			averageBPMsOver = newAverageBPMsOver;
		}
	}
	/**
	 * <p>Setter for the field <code>deltaAngle</code>.</p>
	 *
	 * @param newDeltaAngle double
	 */
	public synchronized void setDeltaAngle(double newDeltaAngle) {
		if (deltaAngle != newDeltaAngle) {
			deltaAngle = newDeltaAngle;
		}
	}
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "EmpiricOrbitCorrectionModel["+name+","+getOrientation()+","+averageBPMsOver+","+deltaAngle+"]";
	}

	/** {@inheritDoc} */
	@Override
	public org.scictrl.mp.orbitcorrect.correction.Correction calculateCorrection(OrbitCorrectionOperator engine) throws InconsistentDataException {

		Correction correction= makeCorrection(engine.getCorrectors(getOrientation()),engine.getCurrentOrbit(), engine.getResponseMatrix(getOrientation()),engine.getDataBush(),engine);

		return correction;
	}
}
