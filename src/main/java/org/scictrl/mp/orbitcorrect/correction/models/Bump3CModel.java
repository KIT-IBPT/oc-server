package org.scictrl.mp.orbitcorrect.correction.models;

import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.scictrl.mp.orbitcorrect.DataBushInitializationException;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.correction.Correction;
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
 * <p>Bump3CModel class.</p>
 *
 * @author igor@scictrl.com
 */
public class Bump3CModel extends AbstractOCModel {

	/** Max correction value allowed. */
	protected double maxCorrection=0.0;

	/** Bump height. */
	protected double bumpHeight = 0.0;

	/** Bump location at element. */
	protected AbstractOpticalElement bumpedElement;

	/**
	 * RingKeeper constructor comment.
	 */
	public Bump3CModel() {
		super();
		name= "3 Corrector Local Bump";
	}
	/** {@inheritDoc} */
	@Override
	public Correction calculateCorrection(OrbitCorrectionOperator engine)
			throws InconsistentDataException {

		ElementList<AbstractCorrector> correctors= engine.getCorrectors(getOrientation());

		if (bumpedElement==null) throw new InconsistentDataException("Set bumped object first!");
		if (correctors==null) throw new InconsistentDataException("Select fist 3 correctors!");
		if (correctors.size()!=3) throw new InconsistentDataException("Select exactly 3 correctors!");

		ResponseMatrix responseMatrix = new ResponseMatrix(5,7);
		responseMatrix.correctors[0]="pos index";
		responseMatrix.correctors[1]="position";
		responseMatrix.correctors[2]="beta";
		responseMatrix.correctors[3]="phase";
		responseMatrix.correctors[4]="alpha";
		responseMatrix.correctors[5]="setings";
		responseMatrix.correctors[6]="correction";
		responseMatrix.bpms[4]="bump height";

		Orbit o= engine.getCurrentOrbit();

		return makeCorrection(correctors, o, responseMatrix, engine.getDataBush(), engine);
	}
	/**
	 * <p>makeCorrection.</p>
	 *
	 * @param correctors a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @param orbit a {@link org.scictrl.mp.orbitcorrect.utilities.Orbit} object
	 * @param responseMatrix a {@link org.scictrl.mp.orbitcorrect.utilities.ResponseMatrix} object
	 * @param db a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} object
	 * @param op a {@link org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator} object
	 * @return a {@link org.scictrl.mp.orbitcorrect.correction.Correction} object
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 */
	protected Correction makeCorrection(ElementList<AbstractCorrector> correctors, Orbit orbit, ResponseMatrix responseMatrix, DataBush db, OrbitCorrectionOperator op)
			throws InconsistentDataException {


		testCorrectors(correctors);
		testElements(correctors,db);

		ElementList<AbstractCorrector> l;
		//AbstractDataBushElement el;
		try {
			l= new ElementList<>(correctors);
		} catch (DataBushInitializationException e) {throw new InconsistentDataException(e.toString());}


		if (!db.hasClosedOrbitSolution()) throw new InconsistentDataException("DataBush has no closed orbit solution!");

		boolean bump2= false;

		int c1=0;
		int c2,bb;
		int c3=3;

		int rot=0;

		int bumpInd=0;
		for (int i=0; i<3; i++) if(((AbstractOpticalElement)correctors.get(i)).getIndex()<bumpedElement.getIndex()) bumpInd++;


		switch (bumpInd) {
			case 0 : {
				bump2= getCorrector(l,2).getPosition()-getCorrector(l,1).getPosition() > getCorrector(l,1).getPosition()-getCorrector(l,0).getPosition() ;
				rot= bump2 ? 1 : 2;
			} break;
			case 1 : {
				bump2= getCorrector(l,0).getPosition()+db.getOpticsLength()-getCorrector(l,2).getPosition() > getCorrector(l,2).getPosition()-getCorrector(l,1).getPosition() ;
				rot= bump2 ? 0 : 1;
			} break;
			case 2 : {
				bump2=  getCorrector(l,1).getPosition()-getCorrector(l,0).getPosition() > getCorrector(l,0).getPosition()+db.getOpticsLength()-getCorrector(l,2).getPosition() ;
				rot= bump2 ? 2 : 0;
			} break;
			case 3 : {
				bump2= getCorrector(l,2).getPosition()-getCorrector(l,1).getPosition() > getCorrector(l,1).getPosition()-getCorrector(l,0).getPosition() ;
				rot= bump2 ? 1 : 2;
			} break;
		}

		bb= bump2 ? 1 : 2;
		c2= bump2 ? 2 : 1;

		for (int i=0; i<rot; i++) l.add(0,l.remove(2));

		responseMatrix.bpms[c1]=getCorrector(l,0).getName();
		responseMatrix.bpms[c2]=getCorrector(l,1).getName();
		responseMatrix.bpms[bb]=bumpedElement.getName();
		responseMatrix.bpms[c3]=getCorrector(l,2).getName();

		responseMatrix.matrix[c1][0]= getCorrector(l,0).getIndex();
		responseMatrix.matrix[c2][0]= getCorrector(l,1).getIndex();
		responseMatrix.matrix[bb][0]= bumpedElement.getIndex();
		responseMatrix.matrix[c3][0]= getCorrector(l,2).getIndex();

		responseMatrix.matrix[c1][1]= getCorrector(l,0).getPosition();
		responseMatrix.matrix[c2][1]= getCorrector(l,1).getPosition();
		responseMatrix.matrix[bb][1]= bumpedElement.getPosition();
		responseMatrix.matrix[c3][1]= getCorrector(l,2).getPosition();

	// beta values
		responseMatrix.matrix[c1][2]= read(getCorrector(l,0).getBeta());
		responseMatrix.matrix[c2][2]= read(getCorrector(l,1).getBeta());
		responseMatrix.matrix[bb][2]= read(bumpedElement.getBeta());
		responseMatrix.matrix[c3][2]= read(getCorrector(l,2).getBeta());

	// phase values
		responseMatrix.matrix[c1][3]= read(getCorrector(l,0).getQ());
		if (getCorrector(l,0).getIndex()>bumpedElement.getIndex()) responseMatrix.matrix[c1][3]-= read(db.getQ());
		responseMatrix.matrix[c2][3]= read(getCorrector(l,1).getQ());
		if ((rot==2)&&(!bump2)&&(getCorrector(l,1).getIndex()>bumpedElement.getIndex())) responseMatrix.matrix[c2][3]-= read(db.getQ());
		else if ((rot==1)&&(bump2)&&(getCorrector(l,1).getIndex()<bumpedElement.getIndex())) responseMatrix.matrix[c2][3]+= read(db.getQ());
		responseMatrix.matrix[bb][3]= read(bumpedElement.getQ());
		responseMatrix.matrix[c3][3]= read(getCorrector(l,2).getQ());
		if (getCorrector(l,2).getIndex()<bumpedElement.getIndex()) responseMatrix.matrix[c3][3]+= read(db.getQ());

	// alpha values
		responseMatrix.matrix[c1][4]= read(getCorrector(l,0).getAlpha());
		responseMatrix.matrix[c2][4]= read(getCorrector(l,1).getAlpha());
		responseMatrix.matrix[bb][4]= read(bumpedElement.getAlpha());
		responseMatrix.matrix[c3][4]= read(getCorrector(l,2).getAlpha());

	// current settings
		responseMatrix.matrix[c1][5]= getCorrector(l,0).getAngle();
		responseMatrix.matrix[c2][5]= getCorrector(l,1).getAngle();
		responseMatrix.matrix[bb][5]= 0.0;
		responseMatrix.matrix[c3][5]= getCorrector(l,2).getAngle();

	// kicks

		double b= bumpHeight / (bump2 ? Math.sin((responseMatrix.matrix[bb][3]-responseMatrix.matrix[c1][3]) * 2.0 * Math.PI) : (Math.sin((responseMatrix.matrix[bb][3]-responseMatrix.matrix[c1][3]) * 2.0 * Math.PI)-Math.sin((responseMatrix.matrix[bb][3]-responseMatrix.matrix[c2][3]) * 2.0 * Math.PI)*Math.sin((responseMatrix.matrix[c3][3]-responseMatrix.matrix[c1][3]) * 2.0 * Math.PI)/Math.sin((responseMatrix.matrix[c3][3]-responseMatrix.matrix[c2][3]) * 2.0 * Math.PI)) );

		double[] val= new double[3];
		val[0] =responseMatrix.matrix[c1][6]= b / Math.sqrt(responseMatrix.matrix[bb][2]*responseMatrix.matrix[c1][2]);
		val[1] =responseMatrix.matrix[c2][6]= - b * Math.sin( ( responseMatrix.matrix[c3][3] - responseMatrix.matrix[c1][3] ) * 2.0 * Math.PI ) / Math.sin( ( responseMatrix.matrix[c3][3] - responseMatrix.matrix[c2][3] ) * 2.0 * Math.PI ) / Math.sqrt( responseMatrix.matrix[bb][2] * responseMatrix.matrix[c2][2] );
		responseMatrix.matrix[bb][6]= 0.0;
		val[2] =responseMatrix.matrix[c3][6]= b * ( Math.sin( ( responseMatrix.matrix[c3][3] - responseMatrix.matrix[c1][3] ) * 2.0 * Math.PI ) / Math.tan( ( responseMatrix.matrix[c3][3] - responseMatrix.matrix[c2][3] ) * 2.0 * Math.PI ) - Math.cos( ( responseMatrix.matrix[c3][3] - responseMatrix.matrix[c1][3] ) * 2.0 * Math.PI ) ) / Math.sqrt( responseMatrix.matrix[bb][2] * responseMatrix.matrix[c3][2] );

		responseMatrix.matrix[4][0]= bumpHeight;

		maxCorrection=0.0;
		for (int i=0; i<3; i++) if (Math.abs(val[i])>maxCorrection) maxCorrection= Math.abs(val[i]);

		return new Correction(l, null, val, getOrientation(), op.getCorrectionScale(), 0, new double[0]);

	}
	/**
	 * <p>getCorrector.</p>
	 *
	 * @param l a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @param index a int
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector} object
	 */
	protected AbstractCorrector getCorrector(ElementList<AbstractCorrector> l, int index) {
		return l.get(index);
	}

	/**
	 * <p>Getter for the field <code>bumpedElement</code>.</p>
	 *
	 * @return AbstractOpticalElement
	 */
	public org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement getBumpedElement() {
		return bumpedElement;
	}
	/**
	 * <p>Getter for the field <code>bumpHeight</code>.</p>
	 *
	 * @return double
	 */
	public double getBumpHeight() {
		return bumpHeight;
	}
	/**
	 * <p>getMaxCorrectionValue.</p>
	 *
	 * @return double
	 */
	public synchronized double getMaxCorrectionValue() {
		return maxCorrection;
	}
	/**
	 * <p>Setter for the field <code>bumpedElement</code>.</p>
	 *
	 * @param newBumpedElement AbstractOpticalElement
	 */
	public synchronized void setBumpedElement(org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement newBumpedElement) {
		if (bumpedElement != newBumpedElement) {
			bumpedElement = newBumpedElement;
		}
	}
	/**
	 * <p>Setter for the field <code>bumpHeight</code>.</p>
	 *
	 * @param newBumpHeight double
	 */
	public void setBumpHeight(double newBumpHeight) {
		if (bumpHeight!=newBumpHeight) {
			bumpHeight = newBumpHeight;
		}
	}
	/**
	 * {@inheritDoc}
	 *
	 * setCorrectors method comment.
	 */
	@Override
	protected void testCorrectors(ElementList<AbstractCorrector> cor) throws InconsistentDataException {
		if ((cor==null)||(cor.size()==0)) throw new InconsistentDataException("Select firt 3 correctors!");
		if (cor.size()!=3) throw new InconsistentDataException("Select exactely 3 correctors!");
		Iterator<AbstractCorrector> it= cor.iterator();
		if (isHorizontal()) {
			while (it.hasNext()&&!(it.next() instanceof HorCorrector)) throw new InconsistentDataException("Not all elements in list are of type HorCorrector!");
		} else {
			while (it.hasNext()&&!(it.next() instanceof VerCorrector)) throw new InconsistentDataException("Not all elements in list are of type VerCorrector!");
		}
	}
	/**
	 * <p>testElements.</p>
	 *
	 * @param correctors a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @param db a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} object
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 */
	protected void testElements(ElementList<AbstractCorrector> correctors, DataBush db) throws org.scictrl.mp.orbitcorrect.InconsistentDataException {
		if ((correctors==null)||(bumpedElement==null)) return;
		if (!db.getDataBushInfo().isOrbitClosed()) if ((getCorrector(correctors,0).getIndex()>getCorrector(correctors,1).getIndex())||(getCorrector(correctors,1).getIndex()>bumpedElement.getIndex())||(bumpedElement.getIndex()>getCorrector(correctors,2).getIndex())||(getCorrector(correctors,2).getIndex()>getCorrector(correctors,3).getIndex())) throw new InconsistentDataException("Selected BPM in not in the middle of selected correctors");
	}
	/** {@inheritDoc} */
	@Override
	public void configure(Configuration conf) {
		// TODO Auto-generated method stub

	}
}
