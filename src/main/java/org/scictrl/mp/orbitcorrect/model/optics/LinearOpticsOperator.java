package org.scictrl.mp.orbitcorrect.model.optics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.DBMath;
import org.scictrl.mp.orbitcorrect.DBString;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.math.BetaMatrix;
import org.scictrl.mp.orbitcorrect.math.DispersionVector;
import org.scictrl.mp.orbitcorrect.math.TransferMatrix;
/**
 *
 */
class LinearOpticsOperator implements AbstractUpdateableElement {
	private double minPathLength=100.0;
	private List<AbstractPositionHolder> opticsLine;
	DataBushHandler treebeard;
	DataBushEnt ent;
	DataHolder dh;
	MatrixHolder mh;
	AbstractTransferElement te;
	private boolean dataInvalidated = true;
	private int lastUpdateResult= DBConst.RC_COMPLETED_SUCCESSFULLY;

	private interface DataList {
		void setData(int i, double pos, double qx, double qz, BetaMatrix b, DispersionVector d);
		void setData(int i, double pos, PositionedData q, PositionedData b, PositionedData a, DispersionData d);
	}
	private class ArrayDataList implements DataList {
		double[][] da;
		public ArrayDataList setDataHolder(double[][] d, int i) throws IllegalArgumentException {
			if (d.length!=9) new IllegalArgumentException("Array should have 9 rows, not "+d.length+"!");
			if (d[0].length!=i) new IllegalArgumentException("length of row differes from specified "+i+"!");
			da= d;
			return this;
		}
		@Override
		public void setData(int i, double pos, double qx, double qz, BetaMatrix b, DispersionVector d) {
			da[DBConst.MF_POSITION][i]= pos;
			da[DBConst.MF_Q_X][i]= qx;
			da[DBConst.MF_Q_Z][i]= qz;
			da[DBConst.MF_BETA_X][i]= b.getBetaX();
			da[DBConst.MF_BETA_Z][i]= b.getBetaZ();
			da[DBConst.MF_ALPHA_X][i]= b.getAlphaX();
			da[DBConst.MF_ALPHA_Z][i]= b.getAlphaZ();
			da[DBConst.MF_D][i]= d.getDispersion();
			da[DBConst.MF_DP][i]= d.getDispersionPrime();
		}
		@Override
		public void setData(int i, double pos, PositionedData q, PositionedData b, PositionedData a, DispersionData d) {
			da[DBConst.MF_POSITION][i]= pos;
			da[DBConst.MF_Q_X][i]= q.x();
			da[DBConst.MF_Q_Z][i]= q.z();
			da[DBConst.MF_BETA_X][i]= b.x();
			da[DBConst.MF_BETA_Z][i]= b.z();
			da[DBConst.MF_ALPHA_X][i]= a.x();
			da[DBConst.MF_ALPHA_Z][i]= a.z();
			da[DBConst.MF_D][i]= d.d();
			da[DBConst.MF_DP][i]= d.dp();
		}
	}
	private class VectorDataList implements DataList {
		//Double zero= new Double(0.0);
		List<Double>[] dv;
		public VectorDataList setDataHolder(List<Double>[] d, int i) throws IllegalArgumentException {
			if (d.length!=9) throw new IllegalArgumentException("Array should have 9 Vectors, not "+d.length+"!");
			try {
				for (int j=0; j<9; j++) {
					d[j].clear();
					//d[j].ensureCapacity(i);
				}
			} catch (NullPointerException e) {throw new IllegalArgumentException("One of Vectors in array is NULL!");}
			dv= d;
			return this;
		}
		@Override
		public void setData(int i, double pos, double qx, double qz, BetaMatrix b, DispersionVector d) {
			dv[DBConst.MF_POSITION].add(Double.valueOf(pos));
			dv[DBConst.MF_Q_X].add(Double.valueOf(qx));
			dv[DBConst.MF_Q_Z].add(Double.valueOf(qz));
			dv[DBConst.MF_BETA_X].add(Double.valueOf(b.getBetaX()));
			dv[DBConst.MF_BETA_Z].add(Double.valueOf(b.getBetaZ()));
			dv[DBConst.MF_ALPHA_X].add(Double.valueOf(b.getAlphaX()));
			dv[DBConst.MF_ALPHA_Z].add(Double.valueOf(b.getAlphaZ()));
			dv[DBConst.MF_D].add(Double.valueOf(d.getDispersion()));
			dv[DBConst.MF_DP].add(Double.valueOf(d.getDispersionPrime()));
		}
		@Override
		public void setData(int i, double pos, PositionedData q, PositionedData b, PositionedData a, DispersionData d) {
			dv[DBConst.MF_POSITION].add(Double.valueOf(pos));
			dv[DBConst.MF_Q_X].add(Double.valueOf(q.x()));
			dv[DBConst.MF_Q_Z].add(Double.valueOf(q.z()));
			dv[DBConst.MF_BETA_X].add(Double.valueOf(b.x()));
			dv[DBConst.MF_BETA_Z].add(Double.valueOf(b.z()));
			dv[DBConst.MF_ALPHA_X].add(Double.valueOf(a.x()));
			dv[DBConst.MF_ALPHA_Z].add(Double.valueOf(a.z()));
			dv[DBConst.MF_D].add(Double.valueOf(d.d()));
			dv[DBConst.MF_DP].add(Double.valueOf(d.dp()));
		}
	}
	ArrayDataList al= new ArrayDataList();
	VectorDataList vl= new VectorDataList();
	private MatrixHolder opticsMatrix;
	private List<MatrixHolder> opticsSystem = new ArrayList<>(100);
/**
 * LinearOpticsOperator constructor comment.
 *
 * @param t a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBushHandler} object
 * @param e a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBushEnt} object
 */
public LinearOpticsOperator(DataBushHandler t, DataBushEnt e) {
	super();
	treebeard= t;
	ent= e;
	opticsLine= new ArrayList<>(ent.optics.size()*2+10);
}
/**
 *
 * @param dh DataHolder
 */
private void addOptic(DataHolder newdh) {
	if (dh==null) {
		opticsLine.add(dh=newdh);
		return;
	}
	if (treebeard.testPrecision(dh.position-newdh.position)==0.0) {
		dh.add(newdh);
		return;
	}
	if (mh==null) {
		mh= new MatrixHolder(
			dh.position,
			checkLength(newdh.position-dh.position),
			te,
			null,
			newdh);
		mh.setOpticStart(dh);
		opticsLine.add(mh);
		opticsMatrix=mh;
	} else {
		opticsLine.add(mh=new MatrixHolder(
			mh.opticEnd.position,
			checkLength(newdh.position-mh.opticEnd.position),
			te,
			mh,
			newdh));
	}
	opticsLine.add(dh=newdh);
}
	/**
	 *
	 * @param b double[][]
	 */
	/*private static void advanceByDrift(double[][] b, double s) {
		double bx= b[0][0];
		double bz= b[2][2];
		double ax= -b[0][1];
		double az= -b[2][3];

		b[0][0]= bx-2.0*ax*s+(1.0+ax*ax)*s*s/bx;
		b[2][2]= bz-2.0*az*s+(1.0+az*az)*s*s/bz;
		b[0][1]= ax-(1.0+ax*ax)*s/bx;
		b[2][3]= az-(1.0+az*az)*s/bz;
		b[1][0]= b[0][1];
		b[3][2]= b[2][3];
	}*/
	/**
	 * <p>assambleOpticsLine.</p>
	 *
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 */
	public void assambleOpticsLine() throws InconsistentDataException {
		opticsLine.clear();
		opticsSystem.clear();
		opticsMatrix=null;

		//DataHolder dhtmp;
		PositionedData pd;

		mh=null;
		dh=null;
		te=null;

		Iterator<PositionedData> it= ent.betaList.iterator();

		while (it.hasNext()) {
			pd= it.next();

			if (pd.getPosition()<0) { Object[] args= {pd.getSource().toString()}; throw new InconsistentDataException(java.text.MessageFormat.format(DBString.OVER_BEGINNING,args));}

			if (pd.getSource() instanceof Marker) {
				// positioned data belongs to Marker
				addOptic(new DataHolder(pd.getSource()));
			} else
			if (pd.getSource() instanceof BPMonitor) {
				// positioned data belongs to BPMonitor
				// test if valid
				if (te!=null) {
					Object[] args= {pd.getSource().toString(),te.toString()};
					throw new InconsistentDataException(java.text.MessageFormat.format(DBString.BPM_INSIDE,args));
				}
				// standard procedure
				addOptic(new DataHolder(pd.getSource()));
			} else
			if (pd.getSource() instanceof AbstractTransferElement) {
				if (te==null) {
					// new transfer element should start here?
					addOptic(new DataHolder((AbstractTransferElement)pd.getSource(),true));
					te= (AbstractTransferElement)pd.getSource();
				} else {
					if (pd.getSource()!=te) {
						Object[] args= {pd.getSource().toString(),te.toString()};
						throw new InconsistentDataException(java.text.MessageFormat.format(DBString.TE_OVERPLACED,args));
					}
					if (((AbstractTransferElement)pd.getSource()).getBeta2()==pd) {
						addOptic(new DataHolder((AbstractTransferElement)pd.getSource(),false));
						te= null;
					} else {
						addOptic(new DataHolder(pd.getSource()));
					}
				}
			}

		}

		mh= opticsMatrix;
		while(mh!=null) {
			if (mh.next==null) opticsSystem.add(mh);
			else if (mh.opticStart!=mh.next.opticStart)
				opticsSystem.add(mh);
			mh=mh.next;
		}

		treebeard.db.setOpticsLength(ent.betaList.get(ent.betaList.size()-1).getPosition()-ent.betaList.get(0).getPosition());

	}
	/**
	 *
	 * @return double[][]
	 * @param oe1 AbstractOpticalElement
	 * @param oe2 AbstractOpticalElement
	 * @param count int
	 */
	private void calculate(AbstractOpticalElement oe1, AbstractOpticalElement oe2, int count, DataList dlist) {
		AbstractOpticalElement oe;
		if (oe1.getIndex()>oe2.getIndex()) {
			oe= oe1;
			oe1= oe2;
			oe2= oe;
			oe=null;
		}

		TransferMatrix mt=null;
		BetaMatrix mb=new BetaMatrix(0.0,0.0,0.0,0.0);
		//TransferMatrix mTest=null;
		DispersionVector md= new DispersionVector(0.0,0.0);

		double qx=0,qz=0;
		int n;
		int nn=0;
		Object end;

		double pointsPerLength, startPos, endPos, pos, step;
		if (oe2 instanceof AbstractTransferElement) {
			endPos= ((AbstractTransferElement)oe2).getQ2().getPosition();
			end= ((AbstractTransferElement)oe2).getQ2().dataHolder;
		} else {
			endPos= oe2.getPosition();
			end= (oe2.getQ()).dataHolder;
		}

		int i= opticsLine.indexOf(end);
		Object o;
		while (i>0) {
			o=opticsLine.get(--i);
			if (o instanceof MatrixHolder) {
				end= o;
				break;
			}
		}
		if (i==0) return;

		DataHolder dh=null;
		if (oe1 instanceof AbstractTransferElement) {
			AbstractTransferElement te= (AbstractTransferElement)oe1;
			dh= te.getQ1().dataHolder;
			startPos= te.getQ1().getPosition();
		} else {
			dh= oe1.getQ().dataHolder;
			startPos= oe1.getPosition();
		}

		pos= startPos;
		pointsPerLength= (count-1)/(endPos-startPos);

		ListIterator<AbstractPositionHolder> it= opticsLine.listIterator(opticsLine.indexOf(dh));
		MatrixHolder mh;

		boolean endIt=false;
		//double inPos=0.0;

		while (it.hasNext()) {
			o= it.next();
			if (o instanceof MatrixHolder) {
					mh= (MatrixHolder)o;
					if (mh.equals(end)) endIt= true;

					n= (endIt) ? (count-nn-1) : Double.valueOf(pointsPerLength*mh.length).intValue();
					if (n==0) return;

					pos= dh.position;
					dlist.setData(nn++,pos,dh.q,dh.b,dh.a,dh.d);

					if (n>1) {

						step= mh.length/n;

						mt= mh.newMatrix(step);
						if ((mh.source instanceof RBending)&&(mh.isStart)) {
							((org.scictrl.mp.orbitcorrect.math.RDipoleMatrix)mt).setRAngle(((org.scictrl.mp.orbitcorrect.math.RDipoleMatrix)mh.source.getMatrix()).getRAngle());
						}

						qx=dh.q.x();
						qz=dh.q.z();
						mb.set(dh.b.x(),dh.b.z(),dh.a.x(),dh.a.z());
						md.set(dh.d.d(),dh.d.dp());
						for (i=1; i<n; i++) {
							qx += mt.matrixPhaseX(mb);
							qz += mt.matrixPhaseZ(mb);
							mt.transport(mb);
							mt.transport(md);
							pos += step;
							if ((i==1)&&(mh.source instanceof RBending)&&(mh.isStart))
								((org.scictrl.mp.orbitcorrect.math.RDipoleMatrix)mt).setRAngle(0.0);

							dlist.setData(nn++,pos,qx,qz,mb,md);
						}
					}
				if (endIt) {
					if (!it.hasNext()) return;
					o= it.next();
					if (!(o instanceof DataHolder)) return;
					dh= (DataHolder)o;
					dlist.setData(nn++,pos,dh.q,dh.b,dh.a,dh.d);
					return;
				}
			} else {
				dh= (DataHolder)o;
			}
		}
	}
	/**
	 * <p>calculateMatrix.</p>
	 *
	 * @return double[][]
	 * @param a a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement} object
	 * @param b a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement} object
	 */
	public org.scictrl.mp.orbitcorrect.math.TransferMatrix calculateMatrix(AbstractOpticalElement a,AbstractOpticalElement b) {
		return calculateMatrix(a.getQ(),b.getQ());
	}
	/**
	 * <p>calculateMatrix.</p>
	 *
	 * @return double[][]
	 * @param a a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement} object
	 * @param b a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractTransferElement} object
	 */
	public org.scictrl.mp.orbitcorrect.math.TransferMatrix calculateMatrix(AbstractOpticalElement a,AbstractTransferElement b) {
		return calculateMatrix(a.getQ(),b.getQ2());
	}
	/**
	 * <p>calculateMatrix.</p>
	 *
	 * @return double[][]
	 * @param p1 a {@link org.scictrl.mp.orbitcorrect.model.optics.SimpleData} object
	 * @param p2 a {@link org.scictrl.mp.orbitcorrect.model.optics.SimpleData} object
	 */
	public org.scictrl.mp.orbitcorrect.math.TransferMatrix calculateMatrix(SimpleData p1, SimpleData p2) {

		if (p1.getPosition()>p2.getPosition()) throw new IllegalArgumentException("Bad argument order!");

		Iterator<MatrixHolder> it = opticsSystem.iterator();
		MatrixHolder mh=null;

		org.scictrl.mp.orbitcorrect.math.TransferMatrix tm=null;

		//boolean start=false;
		while (it.hasNext()) {
			mh=it.next();
			if (tm==null) {
				if (mh.position+mh.opticLength > p1.getPosition()) {
					if (mh.position+mh.opticLength > p2.getPosition())
						return mh.newMatrix(p2.getPosition()-p1.getPosition());
					tm= mh.newMatrix(mh.position+mh.opticLength - p1.getPosition());
				}
			} else {
				if (mh.position+mh.opticLength >= p2.getPosition()) {
					return tm.multiplay(mh.newMatrix(mh.position+mh.opticLength - p2.getPosition()));
				} else {
					tm= tm.multiplay(mh.newMatrix(mh.length));
				}
			}
		}

		throw new IllegalArgumentException("Arguments not valid, reason unknown!");
	}
	/**
	 * <p>calculateMatrix.</p>
	 *
	 * @return double[][]
	 * @param a a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractTransferElement} object
	 * @param b a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement} object
	 */
	public org.scictrl.mp.orbitcorrect.math.TransferMatrix calculateMatrix(AbstractTransferElement a,AbstractOpticalElement b) {
		return calculateMatrix(a.getQ1(),b.getQ());
	}
	/**
	 * <p>calculateMatrix.</p>
	 *
	 * @return double[][]
	 * @param a a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractTransferElement} object
	 * @param b a {@link org.scictrl.mp.orbitcorrect.model.optics.AbstractTransferElement} object
	 */
	public org.scictrl.mp.orbitcorrect.math.TransferMatrix calculateMatrix(AbstractTransferElement a,AbstractTransferElement b) {
		if (a==b) return a.getMatrix();

		return calculateMatrix(a.getQ1(),b.getQ2());
	}
	/**
	 * <p>calculateSystemMatrix.</p>
	 *
	 * @return double[][]
	 */
	public org.scictrl.mp.orbitcorrect.math.TransferMatrix calculateSystemMatrix() {
		Iterator<MatrixHolder> it = opticsSystem.iterator();

		if (!it.hasNext()) {
			return null;
		}

		MatrixHolder mh= it.next();
		TransferMatrix m= mh.opticMatrix;

		while (it.hasNext()) {
			mh= it.next();
			m= m.multiplay(mh.opticMatrix);
		}

		return m;
	}
	/**
	 *
	 * @return double
	 * @param l double
	 */
	private double checkLength(double l) {
		if (l<minPathLength) minPathLength=l;
		return l;
	}
	/**
	 * <p>clear.</p>
	 */
	public void clear() {
		minPathLength= 100.0;
		treebeard.db.setOpticsLength(0.0);
		opticsLine.clear();
		opticsSystem.clear();
		opticsMatrix=null;
		dh=null;
		mh=null;
		te=null;
	}
	/**
	 * <p>Getter for the field <code>lastUpdateResult</code>.</p>
	 *
	 * @return int
	 */
	public int getLastUpdateResult() {
		return lastUpdateResult;
	}
	/**
	 * <p>getMachineFunctions.</p>
	 *
	 * @return double[][]
	 * @param oe1 AbstractOpticalElement
	 * @param oe2 AbstractOpticalElement
	 * @param count int
	 * @param dataArray an array of {@link double} objects
	 * @throws java.lang.IllegalArgumentException if any.
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 */
	public double[][] getMachineFunctions(double[][] dataArray, AbstractOpticalElement oe1, AbstractOpticalElement oe2, int count) throws IllegalArgumentException, InconsistentDataException {
		if (lastUpdateResult==DBConst.RC_INCONSISTANT_DATA) throw new InconsistentDataException("Orbit solution does not exsists");
		calculate(oe1,oe2,count,al.setDataHolder(dataArray,count));
		return al.da;
	}
	/**
	 * <p>getMachineFunctions.</p>
	 *
	 * @return double[][]
	 * @param oe1 AbstractOpticalElement
	 * @param oe2 AbstractOpticalElement
	 * @param count int
	 * @param dataVectors an array of {@link java.util.List} objects
	 * @throws java.lang.IllegalArgumentException if any.
	 * @throws org.scictrl.mp.orbitcorrect.InconsistentDataException if any.
	 */
	public List<Double>[] getMachineFunctions(List<Double>[] dataVectors, AbstractOpticalElement oe1, AbstractOpticalElement oe2, int count) throws IllegalArgumentException, InconsistentDataException {
		if (lastUpdateResult==DBConst.RC_INCONSISTANT_DATA) throw new InconsistentDataException("Orbit solution does not exsists");
		calculate(oe1,oe2,count,vl.setDataHolder(dataVectors,count));
		return vl.dv;
	}
	/**
	 * <p>Getter for the field <code>minPathLength</code>.</p>
	 *
	 * @return double
	 */
	public double getMinPathLength() {
		return minPathLength;
	}
	/** {@inheritDoc} */
	@Override
	public void invalidateData() {dataInvalidated=true;}
	/** {@inheritDoc} */
	@Override
	public boolean isDataInvalidated() {
		return dataInvalidated;
	}
	/** {@inheritDoc} */
	@Override
	public int update() {

		treebeard.log.debug("UPDATE Optics");


		double e= ent.dataBushInfo.getEnergy();

		if (e==0.0) {
			lastUpdateResult= DBConst.RC_INCONSISTANT_DATA;
			return lastUpdateResult;
		}

		lastUpdateResult= updateDataBushInfo();
		if (lastUpdateResult>DBConst.RC_COMPLETED_SUCCESSFULLY) {
			return lastUpdateResult;
		}
		int i=updateOpticsLine();
		if (i>lastUpdateResult) lastUpdateResult=i;
		ent.dataBushInfo.getQ().set(
		(ent.optics.get(ent.optics.size()-1) instanceof AbstractTransferElement) ? ((AbstractTransferElement)ent.optics.get(ent.optics.size()-1)).getQ2().x() : ent.optics.get(ent.optics.size()-1).getQ().x() ,
		(ent.optics.get(ent.optics.size()-1) instanceof AbstractTransferElement) ? ((AbstractTransferElement)ent.optics.get(ent.optics.size()-1)).getQ2().z() : ent.optics.get(ent.optics.size()-1).getQ().z() );
		return lastUpdateResult;
	}
	private int updateDataBushInfo() {
		int r= DBConst.RC_COMPLETED_SUCCESSFULLY;
		org.scictrl.mp.orbitcorrect.math.TransferMatrix m= calculateSystemMatrix();

		if (m==null) {
			return DBConst.RC_INCONSISTANT_DATA;
		}

		ent.dataBushInfo.setMatrix(m);

	// if true means, tahat values for beta function are for initial position of this transfer line

		if (!ent.dataBushInfo.isOrbitClosed()) {
			return r;
		}

		double xx= 2.0-DBMath.sqr(m.get(0,0))- 2.0*m.get(0,1)*m.get(1,0)-DBMath.sqr(m.get(1,1));
		double zz= 2.0-DBMath.sqr(m.get(2,2))- 2.0*m.get(2,3)*m.get(3,2)-DBMath.sqr(m.get(3,3));

		double bx= 2*Math.abs(m.get(0,1))/Math.sqrt(xx);
		double bz= 2*Math.abs(m.get(2,3))/Math.sqrt(zz);

		(ent.dataBushInfo.getBeta()).set(bx,bz);

		double ax= (m.get(0,0)-m.get(1,1))*bx/m.get(0,1)/2.0;
		double az= (m.get(2,2)-m.get(3,3))*bz/m.get(2,3)/2.0;

		(ent.dataBushInfo.getAlpha()).set(ax,az);

		double dD= ((1.0-m.get(0,0))*m.get(1,4)+m.get(1,0)*m.get(0,4))/(2.0-m.get(0,0)-m.get(1,1));
		double DD= (dD*m.get(0,1)+m.get(0,4))/(1-m.get(0,0));

		ent.dataBushInfo.getDispersion().set(DD,dD);


		if ((xx<=0.0)||(zz<=0.0)||((m.get(0,0)+m.get(1,1))==2.0)||(m.get(0,0)==1)) r= DBConst.RC_INCONSISTANT_DATA;
		return r;

	}
	/**
	 *
	 * @param element DataBushInterface.BendingNut
	 */
	private int updateOpticsLine() {
		BetaMatrix beta= new BetaMatrix(ent.dataBushInfo.getBeta().x(),ent.dataBushInfo.getBeta().z(),ent.dataBushInfo.getAlpha().x(),ent.dataBushInfo.getAlpha().z());
		DispersionVector dis= new DispersionVector(ent.dataBushInfo.getDispersion().d(),ent.dataBushInfo.getDispersion().dp());

		((DataHolder)opticsLine.get(0)).update(0.0,0.0,beta,dis);

		opticsMatrix.updateData(beta,dis);

		DataHolder dh= (DataHolder)opticsLine.get(opticsLine.size()-1);

		if (!ent.dataBushInfo.isOrbitClosed()) {
			if (Double.isInfinite(dh.q.x())||Double.isNaN(dh.q.x())||Double.isInfinite(dh.q.z())||Double.isNaN(dh.q.z())||Double.isInfinite(beta.getBetaX())||Double.isNaN(beta.getBetaX())||Double.isInfinite(beta.getAlphaX())||Double.isNaN(beta.getAlphaX())||Double.isInfinite(beta.getBetaZ())||Double.isNaN(beta.getBetaZ())||Double.isInfinite(beta.getAlphaZ())||Double.isNaN(beta.getAlphaZ())||Double.isInfinite(dis.getDispersion())||Double.isNaN(dis.getDispersionPrime())) {
				return DBConst.RC_INCONSISTANT_DATA;
			}
		} else {
			ent.dataBushInfo.getQ().set(dh.q.x(),dh.q.z());
		}
		return DBConst.RC_COMPLETED_SUCCESSFULLY;

	}
}
