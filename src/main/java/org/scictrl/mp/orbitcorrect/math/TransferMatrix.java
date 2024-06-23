package org.scictrl.mp.orbitcorrect.math;

import org.scictrl.mp.orbitcorrect.DBMath;
/**
 * Generic beam transfer matrix.
 *
 * @author igor@scictrl.com
 */
public class TransferMatrix extends DoubleMatrix {

	/** Transfer element length. */
	protected double length;

	/**
	 * TransportMatrix constructor comment.
	 */
	public TransferMatrix() {
		super(5);
	}
	/**
	 * TransportMatrix constructor comment.
	 *
	 * @param length a double
	 */
	public TransferMatrix(double length) {
		super(5);
		this.length= length;
	}
	/**
	 * <p>Getter for the field <code>length</code>.</p>
	 *
	 * @return double
	 */
	public double getLength() {
		return length;
	}
	/**
	 * <p>identity.</p>
	 *
	 * @return DoubleMatrix
	 */
	public static TransferMatrix identity() {
		TransferMatrix m= new TransferMatrix();
		for (int i=0; i<5; i++) m.matrix[i][i]=1.0;
		return m;
	}
	/**
	 * <p>matrixPhaseX.</p>
	 *
	 * @return double
	 * @param beta BetaMatrix
	 */
	public double matrixPhaseX(BetaMatrix beta) {
		return DBMath.matrixPhase(matrix,beta.getBetaX(),beta.getAlphaX(),DBMath.X_PART);
	}
	/**
	 * <p>matrixPhaseZ.</p>
	 *
	 * @return double
	 * @param beta BetaMatrix
	 */
	public double matrixPhaseZ(BetaMatrix beta) {
		return DBMath.matrixPhase(matrix,beta.getBetaZ(),beta.getAlphaZ(),DBMath.Z_PART);
	}
	/**
	 * {@inheritDoc}
	 *
	 * Multiplies <code>matrix</code> from right with this matrix and
	 * returns result as new matrix
	 */
	@Override
	public DoubleMatrix multiplay(DoubleMatrix matrix) {
		if (!canMulitplay(matrix)) throw new IllegalDimensionException("Can not mulitply, matrix dimension does not match!");
		if (matrix instanceof TransferMatrix) {
			return this.multiplay((TransferMatrix)matrix);
		} else return super.multiplay(matrix);
	}


	/**
	 * <p>multiplay.</p>
	 *
	 * @param matrix a {@link org.scictrl.mp.orbitcorrect.math.TransferMatrix} object
	 * @return a {@link org.scictrl.mp.orbitcorrect.math.TransferMatrix} object
	 */
	public TransferMatrix multiplay(TransferMatrix matrix) {
		if (!canMulitplay(matrix)) throw new IllegalDimensionException("Can not mulitply, matrix dimension does not match!");
		TransferMatrix m= new TransferMatrix(length+matrix.length);
		DBMath.multiply(matrix.matrix,this.matrix,m.matrix);
		return m;
	}

	/**
	 * <p>setIdentity.</p>
	 */
	public void setIdentity() {
		DBMath.identity(matrix);
	}
	/**
	 * <p>transport.</p>
	 *
	 * @return BetaMatrix
	 * @param beta BetaMatrix
	 */
	public BetaMatrix transport(BetaMatrix beta) {
		return transport(beta,matrix);
	}
	/**
	 * <p>transport.</p>
	 *
	 * @return BetaMatrix
	 * @param beta BetaMatrix
	 * @param matrix an array of {@link double} objects
	 */
	protected BetaMatrix transport(BetaMatrix beta, double[][] matrix) {
		double gx= beta.getGamaX();
		double gz= beta.getGamaZ();
		beta.set(
			matrix[0][0]*matrix[0][0]*beta.getBetaX()-2.0*matrix[0][0]*matrix[0][1]*beta.getAlphaX()+matrix[0][1]*matrix[0][1]*gx,
			matrix[2][2]*matrix[2][2]*beta.getBetaZ()-2.0*matrix[2][2]*matrix[2][3]*beta.getAlphaZ()+matrix[2][3]*matrix[2][3]*gz,
			-matrix[1][0]*matrix[0][0]*beta.getBetaX()+(matrix[1][1]*matrix[0][0]+matrix[0][1]*matrix[1][0])*beta.getAlphaX()-matrix[1][1]*matrix[0][1]*gx,
			-matrix[3][2]*matrix[2][2]*beta.getBetaZ()+(matrix[3][3]*matrix[2][2]+matrix[2][3]*matrix[3][2])*beta.getAlphaZ()-matrix[3][3]*matrix[2][3]*gz
			);
		return beta;
	}
	/**
	 * <p>transport.</p>
	 *
	 * @return BetaMatrix
	 * @param disp a {@link org.scictrl.mp.orbitcorrect.math.DispersionVector} object
	 */
	public DispersionVector transport(DispersionVector disp) {
		return transport(disp,matrix);
	}
	/**
	 * <p>transport.</p>
	 *
	 * @return BetaMatrix
	 * @param disp a {@link org.scictrl.mp.orbitcorrect.math.DispersionVector} object
	 * @param matrix an array of {@link double} objects
	 */
	protected DispersionVector transport(DispersionVector disp, double[][] matrix) {
		disp.set(
			matrix[0][0]*disp.getDispersion()+matrix[0][1]*disp.getDispersionPrime()+matrix[0][4],
			matrix[1][0]*disp.getDispersion()+matrix[1][1]*disp.getDispersionPrime()+matrix[1][4]
			);
		return disp;
	}
}
