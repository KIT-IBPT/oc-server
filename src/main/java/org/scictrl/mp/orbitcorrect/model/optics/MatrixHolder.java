package org.scictrl.mp.orbitcorrect.model.optics;

import org.scictrl.mp.orbitcorrect.math.DriftMatrix;
import org.scictrl.mp.orbitcorrect.math.RDipoleMatrix;

/**
 *
 */
class MatrixHolder extends AbstractPositionHolder {
	double length;

	AbstractTransferElement source;

	DataHolder opticStart;

	double opticLength = 0.0;

	MatrixHolder previous;

	DataHolder opticEnd;

	MatrixHolder next;

	org.scictrl.mp.orbitcorrect.math.TransferMatrix opticMatrix;

	boolean isStart = false;

	MatrixHolder(double pos, double l, AbstractTransferElement source,
			MatrixHolder prev, DataHolder end) {
		super(pos);
		length = l;
		this.source = source;
		previous = prev;
		if (previous != null)
			previous.next = this;
		opticEnd = end;
		newMatrix();
	}

	/**
	 *  Creation date: (02.10.2001
	 * 11:36:05)
	 */
	private void newMatrix() {
		if (previous == null)
			return;
		if (source == null) {
			if (previous.opticMatrix instanceof DriftMatrix) {
				opticStart = previous.opticStart;
				opticLength = opticEnd.position - opticStart.position;
			} else {
				isStart = true;
				opticStart = previous.opticEnd;
				opticLength = length;
			}
			// matrix= new DriftMatrix(length);
			opticMatrix = new DriftMatrix(opticLength);
		} else {
			if (previous.opticMatrix.canJoin(source.newMatrix(opticLength))) {
				opticStart = previous.opticStart;
				opticLength = opticEnd.position - opticStart.position;
			} else {
				isStart = true;
				opticStart = previous.opticEnd;
				opticLength = length;
			}
			// matrix= source.newMatrix(length);
			opticMatrix = source.newMatrix(opticLength);
			if (source instanceof RBending) {
				if (opticStart.contains(source.getBeta1())) {
					((RDipoleMatrix)opticMatrix)
							.setRAngle(((RDipoleMatrix)source.getMatrix())
									.getRAngle());
				}
				if (opticEnd.contains(source.getBeta2())) {
					((RDipoleMatrix)opticMatrix)
							.setLAngle(((RDipoleMatrix)source.getMatrix())
									.getLAngle());
				}
			}
			// source.addMatrix(matrix);
			source.addMatrix(opticMatrix);
		}
	}

	/**
	 * <p>newMatrix.</p>
	 *
	 * @return TransferMatrix
	 * @param l
	 *            double
	 */
	public org.scictrl.mp.orbitcorrect.math.TransferMatrix newMatrix(double l) {
		if (source == null)
			return new org.scictrl.mp.orbitcorrect.math.DriftMatrix(l);
		return source.newMatrix(l);
	}

	/**
	 *  Creation date: (02.10.2001
	 * 11:36:05)
	 *
	 * @param start a {@link org.scictrl.mp.orbitcorrect.model.optics.DataHolder} object
	 */
	public void setOpticStart(DataHolder start) {
		isStart = true;
		opticStart = start;
		opticLength = opticEnd.position - opticStart.position;
		if (source == null) {
			opticMatrix = new org.scictrl.mp.orbitcorrect.math.DriftMatrix(opticLength);
			// matrix= new DriftMatrix(length);
		} else {
			if (opticMatrix != null)
				source.removeMatrix(opticMatrix);
			// matrix= source.newMatrix(length);
			opticMatrix = source.newMatrix(opticLength);
			if (source instanceof RBending) {
				if (opticStart.contains(source.getBeta1()))
					((RDipoleMatrix)opticMatrix)
							.setRAngle(((RDipoleMatrix)source.getMatrix())
									.getRAngle());
				if (opticEnd.contains(source.getBeta2()))
					((RDipoleMatrix)opticMatrix)
							.setLAngle(((RDipoleMatrix)source.getMatrix())
									.getLAngle());
			}
			// source.addMatrix(matrix);
			source.addMatrix(opticMatrix);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "MH: " + position + " " + length + " next: "
				+ (position + length) + " " + opticMatrix.getClass();
	}

	/**
	 * <p>updateData.</p>
	 *
	 * @param beta
	 *            BetaMatrix
	 * @param d a {@link org.scictrl.mp.orbitcorrect.math.DispersionVector} object
	 */
	public void updateData(org.scictrl.mp.orbitcorrect.math.BetaMatrix beta,
			org.scictrl.mp.orbitcorrect.math.DispersionVector d) {
		beta.set(opticStart.b.x(), opticStart.b.z(), opticStart.a.x(),
				opticStart.a.z());
		d.set(opticStart.d.d(), opticStart.d.dp());
		double qx = opticMatrix.matrixPhaseX(beta);
		double qz = opticMatrix.matrixPhaseZ(beta);
		//if (opticMatrix instanceof RDipoleMatrix) {
			//RDipoleMatrix rd = (RDipoleMatrix)opticMatrix;

			//System.out.println(qx + " " + qz + " " + rd.getLAngle() + " "
			//		+ rd.getRAngle() + " " + rd.getLength());
		//}
		qx += opticStart.q.x();
		qz += opticStart.q.z();
		opticMatrix.transport(beta);
		opticMatrix.transport(d);
		opticEnd.update(qx, qz, beta, d);
		if (next != null)
			next.updateData(beta, d);
	}
}
