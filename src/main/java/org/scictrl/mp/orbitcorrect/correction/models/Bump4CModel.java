package org.scictrl.mp.orbitcorrect.correction.models;

import java.util.Iterator;

import org.scictrl.mp.orbitcorrect.DataBushInitializationException;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.correction.Correction;
import org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionModel;
import org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
import org.scictrl.mp.orbitcorrect.model.optics.HorCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.VerCorrector;
import org.scictrl.mp.orbitcorrect.utilities.Orbit;
import org.scictrl.mp.orbitcorrect.utilities.ResponseMatrix;
/**
 * <p>Bump4CModel class.</p>
 *
 * @author igor@scictrl.com
 */
public class Bump4CModel extends Bump3CModel implements IOrbitCorrectionModel{
	private double bumpAngle = 0.0;
	/**
	 * RingKeeper constructor comment.
	 */
	public Bump4CModel() {
		super();
		name= "4 Corrector Local Bump";
	}
	/** {@inheritDoc} */
	@Override
	protected Correction makeCorrection(ElementList<AbstractCorrector> correctors, Orbit orbit, ResponseMatrix responseMatrix, DataBush db, OrbitCorrectionOperator op)
			throws InconsistentDataException {
		// TODO Auto-generated method stub

		if (bumpedElement==null) throw new InconsistentDataException("Set bumped object first!");
		if (correctors==null) throw new InconsistentDataException("Select first 4 correctors!");
		if (correctors.size()!=4) throw new InconsistentDataException("Select exactly 4 correctors!");

		testCorrectors(correctors);
		testElements(correctors,db);

		ElementList<AbstractCorrector> l;
		//AbstractDataBushElement el;
		try {
			l= new ElementList<>(correctors);
		} catch (DataBushInitializationException e) {throw new InconsistentDataException(e.toString());}

		int less=0;
		for (int i=0; i<4; i++) if(((AbstractOpticalElement)correctors.get(i)).getIndex()<bumpedElement.getIndex()) less++;

		for (int i=2-less; i>0; i--) l.add(0,l.remove(3));
		for (int i=less-2; i>0; i--) l.add(l.remove(0));


		if (!db.hasClosedOrbitSolution()) throw new InconsistentDataException("DataBush has no closed orbit solution!");

		responseMatrix.bpms[0]=getCorrector(l,0).getName();
		responseMatrix.bpms[1]=getCorrector(l,1).getName();
		responseMatrix.bpms[2]=bumpedElement.getName();
		responseMatrix.bpms[3]=getCorrector(l,2).getName();
		responseMatrix.bpms[4]=getCorrector(l,3).getName();

		responseMatrix.matrix[0][0]= getCorrector(l,0).getIndex();
		responseMatrix.matrix[1][0]= getCorrector(l,1).getIndex();
		responseMatrix.matrix[2][0]= bumpedElement.getIndex();
		responseMatrix.matrix[3][0]= getCorrector(l,2).getIndex();
		responseMatrix.matrix[4][0]= getCorrector(l,3).getIndex();

		responseMatrix.matrix[0][1]= getCorrector(l,0).getPosition();
		responseMatrix.matrix[1][1]= getCorrector(l,1).getPosition();
		responseMatrix.matrix[2][1]= bumpedElement.getPosition();
		responseMatrix.matrix[3][1]= getCorrector(l,2).getPosition();
		responseMatrix.matrix[4][1]= getCorrector(l,3).getPosition();

	// beta values
		responseMatrix.matrix[0][2]= read(getCorrector(l,0).getBeta());
		responseMatrix.matrix[1][2]= read(getCorrector(l,1).getBeta());
		responseMatrix.matrix[2][2]= read(bumpedElement.getBeta());
		responseMatrix.matrix[3][2]= read(getCorrector(l,2).getBeta());
		responseMatrix.matrix[4][2]= read(getCorrector(l,3).getBeta());

	// phase values
		responseMatrix.matrix[0][3]= read(getCorrector(l,0).getQ());
		if (getCorrector(l,0).getIndex()>bumpedElement.getIndex()) responseMatrix.matrix[0][3]-= read(db.getQ());
		responseMatrix.matrix[1][3]= read(getCorrector(l,1).getQ());
		if (getCorrector(l,1).getIndex()>bumpedElement.getIndex()) responseMatrix.matrix[1][3]-= read(db.getQ());
		responseMatrix.matrix[2][3]= read(bumpedElement.getQ());
		responseMatrix.matrix[3][3]= read(getCorrector(l,2).getQ());
		if (getCorrector(l,2).getIndex()<bumpedElement.getIndex()) responseMatrix.matrix[3][3]+= read(db.getQ());
		responseMatrix.matrix[4][3]= read(getCorrector(l,3).getQ());
		if (getCorrector(l,3).getIndex()<bumpedElement.getIndex()) responseMatrix.matrix[4][3]+= read(db.getQ());

	// alpha values
		responseMatrix.matrix[0][4]= read(getCorrector(l,0).getAlpha());
		responseMatrix.matrix[1][4]= read(getCorrector(l,1).getAlpha());
		responseMatrix.matrix[2][4]= read(bumpedElement.getAlpha());
		responseMatrix.matrix[3][4]= read(getCorrector(l,2).getAlpha());
		responseMatrix.matrix[4][4]= read(getCorrector(l,3).getAlpha());

	// current settings
		responseMatrix.matrix[0][5]= getCorrector(l,0).getAngle();
		responseMatrix.matrix[1][5]= getCorrector(l,1).getAngle();
		responseMatrix.matrix[2][5]= 0.0;
		responseMatrix.matrix[3][5]= getCorrector(l,2).getAngle();
		responseMatrix.matrix[4][5]= getCorrector(l,3).getAngle();

	// kicks
		double[] d= new double[4];
		d[0]=responseMatrix.matrix[0][6]= bumpHeight * ( Math.cos( ( responseMatrix.matrix[2][3] - responseMatrix.matrix[1][3] ) * 2.0 * Math.PI ) - responseMatrix.matrix[2][4] * Math.sin( ( responseMatrix.matrix[2][3] - responseMatrix.matrix[1][3] ) * 2.0 * Math.PI ) / 1000.0 ) / Math.sin( ( responseMatrix.matrix[1][3] - responseMatrix.matrix[0][3] ) * 2.0 * Math.PI ) / Math.sqrt( responseMatrix.matrix[0][2] * responseMatrix.matrix[2][2] ) - Math.sqrt( responseMatrix.matrix[2][2] / responseMatrix.matrix[0][2] ) * bumpAngle * Math.sin( ( responseMatrix.matrix[2][3] - responseMatrix.matrix[1][3] ) * 2.0 * Math.PI ) / Math.sin( ( responseMatrix.matrix[1][3] - responseMatrix.matrix[0][3] ) * 2.0 * Math.PI );
		d[1]=responseMatrix.matrix[1][6]= - bumpHeight * ( Math.cos( ( responseMatrix.matrix[2][3] - responseMatrix.matrix[0][3] ) * 2.0 * Math.PI ) - responseMatrix.matrix[2][4] * Math.sin( ( responseMatrix.matrix[2][3] - responseMatrix.matrix[0][3] ) * 2.0 * Math.PI ) / 1000.0 ) / Math.sin( ( responseMatrix.matrix[1][3] - responseMatrix.matrix[0][3] ) * 2.0 * Math.PI ) / Math.sqrt( responseMatrix.matrix[1][2] * responseMatrix.matrix[2][2] ) + Math.sqrt( responseMatrix.matrix[2][2] / responseMatrix.matrix[1][2] ) * bumpAngle * Math.sin( ( responseMatrix.matrix[2][3] - responseMatrix.matrix[0][3] ) * 2.0 * Math.PI ) / Math.sin( ( responseMatrix.matrix[1][3] - responseMatrix.matrix[0][3] ) * 2.0 * Math.PI );
		responseMatrix.matrix[2][6]= 0.0;
		d[2]=responseMatrix.matrix[3][6]= - bumpHeight * ( Math.cos( ( responseMatrix.matrix[4][3] - responseMatrix.matrix[2][3] ) * 2.0 * Math.PI ) + responseMatrix.matrix[2][4] * Math.sin( ( responseMatrix.matrix[4][3] - responseMatrix.matrix[2][3] ) * 2.0 * Math.PI ) / 1000.0 ) / Math.sin( ( responseMatrix.matrix[4][3] - responseMatrix.matrix[3][3] ) * 2.0 * Math.PI ) / Math.sqrt( responseMatrix.matrix[3][2] * responseMatrix.matrix[2][2] ) - Math.sqrt( responseMatrix.matrix[2][2] / responseMatrix.matrix[3][2] ) * bumpAngle * Math.sin( ( responseMatrix.matrix[4][3] - responseMatrix.matrix[2][3] ) * 2.0 * Math.PI ) / Math.sin( ( responseMatrix.matrix[4][3] - responseMatrix.matrix[3][3] ) * 2.0 * Math.PI );
		d[3]=responseMatrix.matrix[4][6]= bumpHeight * ( Math.cos( ( responseMatrix.matrix[3][3] - responseMatrix.matrix[2][3] ) * 2.0 * Math.PI ) + responseMatrix.matrix[2][4] * Math.sin( ( responseMatrix.matrix[3][3] - responseMatrix.matrix[2][3] ) * 2.0 * Math.PI ) / 1000.0 ) / Math.sin( ( responseMatrix.matrix[4][3] - responseMatrix.matrix[3][3] ) * 2.0 * Math.PI ) / Math.sqrt( responseMatrix.matrix[4][2] * responseMatrix.matrix[2][2] ) + Math.sqrt( responseMatrix.matrix[2][2] / responseMatrix.matrix[4][2] ) * bumpAngle * Math.sin( ( responseMatrix.matrix[3][3] - responseMatrix.matrix[2][3] ) * 2.0 * Math.PI ) / Math.sin( ( responseMatrix.matrix[4][3] - responseMatrix.matrix[3][3] ) * 2.0 * Math.PI );

		responseMatrix.matrix[5][0]= bumpHeight;
		responseMatrix.matrix[6][0]= bumpAngle;

		maxCorrection=0.0;
		for (int i=0; i<4; i++) if (Math.abs(d[i])>maxCorrection) maxCorrection= Math.abs(d[i]);


		return new Correction(l, null, d, getOrientation(), op.getCorrectionScale(),0,new double[0]);

	}
	/**
	 * <p>Getter for the field <code>bumpAngle</code>.</p>
	 *
	 * @return double
	 */
	public double getBumpAngle() {
		return bumpAngle;
	}
	/**
	 * <p>Setter for the field <code>bumpAngle</code>.</p>
	 *
	 * @param newBumpAngle double
	 */
	public void setBumpAngle(double newBumpAngle) {
		if (bumpAngle!=newBumpAngle) {
			bumpAngle = newBumpAngle;
		}
	}
	/**
	 * {@inheritDoc}
	 *
	 * setCorrectors method comment.
	 */
	@Override
	protected void testCorrectors(ElementList<AbstractCorrector> cor) throws InconsistentDataException {
		if ((cor==null)||(cor.size()==0)) throw new InconsistentDataException("Select first 4 correctors");
		if (cor.size()!=4) throw new InconsistentDataException("Select exactely 4 correctors!");
		Iterator<AbstractCorrector> it= cor.iterator();
		if (isHorizontal()) {
			while (it.hasNext()&&!(it.next() instanceof HorCorrector)) throw new InconsistentDataException("Not all elements in list are of type HorCorrector!");
		} else {
			while (it.hasNext()&&!(it.next() instanceof VerCorrector)) throw new InconsistentDataException("Not all elements in list are of type VerCorrector!");
		}
	}
	/** {@inheritDoc} */
	@Override
	protected void testElements(ElementList<AbstractCorrector> correctors, DataBush db) throws org.scictrl.mp.orbitcorrect.InconsistentDataException {
		if ((correctors==null)||(bumpedElement==null)) return;
		if (!db.getDataBushInfo().isOrbitClosed()) if ((getCorrector(correctors,0).getIndex()>getCorrector(correctors,1).getIndex())||(getCorrector(correctors,1).getIndex()>bumpedElement.getIndex())||(bumpedElement.getIndex()>getCorrector(correctors,2).getIndex())||(getCorrector(correctors,2).getIndex()>getCorrector(correctors,3).getIndex())) throw new InconsistentDataException("Selected BPM in not in the middle of selected correctors");
	}
}
