package org.scictrl.mp.orbitcorrect.model.optics;



/**
 *
 */
class DataHolder extends AbstractPositionHolder {
	AbstractOpticalElement el;
	PositionedData q;
	PositionedData b;
	PositionedData a;
	DispersionData d;
	protected DataHolder next;
private	DataHolder (double pos, AbstractOpticalElement e, PositionedData q, PositionedData b, PositionedData a, DispersionData d) {
		super(pos);
		el= e;
		this.q= q;
		this.b= b;
		this.a= a;
		this.d= d;
		this.q.dataHolder= this;
		this.b.dataHolder= this;
		this.a.dataHolder= this;
		this.d.dataHolder= this;
	}
	DataHolder (AbstractOpticalElement oe) {
		this(oe.getPosition(), oe,oe.getQ(),oe.getBeta(),oe.getAlpha(),oe.getDispersion());
	}
	DataHolder (AbstractTransferElement te,boolean start) {
		this((start) ? te.getPosition()-te.getLength()/2.0 : te.getPosition()+te.getLength()/2.0
			,te
			,(start) ? te.getQ1() : te.getQ2()
			,(start) ? te.getBeta1() : te.getBeta2()
			,(start) ? te.getAlpha1() : te.getAlpha2()
			,(start) ? te.getDispersion1() : te.getDispersion2());
	}
/**
 * <p>add.</p>
 *
 * @param data DataHolder
 */
public void add(DataHolder data) {
	if (next==null) next=data;
	else next.add(data);
	data.q.dataHolder= this;
	data.b.dataHolder= this;
	data.a.dataHolder= this;
	data.d.dataHolder= this;
}
/**
 * <p>contains.</p>
 *
 * @return boolean
 * @param data PositionedData
 */
public boolean contains(DataHolder data) {
	if (this==data) return true;
	if (next!=null) return next.contains(data);
	return false;
}
/**
 * <p>contains.</p>
 *
 * @return boolean
 * @param data PositionedData
 */
public boolean contains(PositionedData data) {
	if (b.equals(data)) return true;
	if (next!=null) return next.contains(data);
	return false;
}
/**
 * <p>setTo.</p>
 *
 * @return BetaMatrix
 * @param beta BetaMatrix
 */
public org.scictrl.mp.orbitcorrect.math.BetaMatrix setTo(org.scictrl.mp.orbitcorrect.math.BetaMatrix beta) {
	beta.set(b.x(),b.z(),a.x(),a.z());
	return beta;
}
/** {@inheritDoc} */
@Override
public String toString() {
	return "DH: "+position + " "+el.getName();
}
/**
 * <p>update.</p>
 *
 * @param qx double
 * @param qz double
 * @param beta double[][]
 * @param dis a {@link org.scictrl.mp.orbitcorrect.math.DispersionVector} object
 */
public void update(double qx, double qz, org.scictrl.mp.orbitcorrect.math.BetaMatrix beta, org.scictrl.mp.orbitcorrect.math.DispersionVector dis) {
	q.set(qx,qz);
	b.set(beta.getBetaX(),beta.getBetaZ());
	a.set(beta.getAlphaX(),beta.getAlphaZ());
	d.set(dis.getDispersion(),dis.getDispersionPrime());
	if (next!=null) next.update(qx, qz, beta, dis);
}
}
