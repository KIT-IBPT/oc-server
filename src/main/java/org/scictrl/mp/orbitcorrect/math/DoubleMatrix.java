package org.scictrl.mp.orbitcorrect.math;

import org.scictrl.mp.orbitcorrect.DBMath;
/**
 * <p>DoubleMatrix class.</p>
 *
 * @author igor@scictrl.com
 */
public class DoubleMatrix implements Cloneable {

	/** Full matrix. */
	protected final double[][] matrix;

	/** Number of columns. */
	protected final int columns;

	/** Number of rows. */
	protected final int rows;


	/**
	 * Creates new square matrix with specified <code>dimension</code>.
	 *
	 * @param dimension a int
	 */
	public DoubleMatrix(int dimension) {
		this(dimension,dimension);
	}

	/**
	 * Creates new matrix with specified number of <code>rows</code>
	 * and <code>columns</code>.
	 *
	 * @param rows a int
	 * @param columns a int
	 */
	public DoubleMatrix(int rows, int columns) {
		super();
		matrix=new double[rows][columns];
		this.columns=columns;
		this.rows=rows;
	}

	/**
	 * Test if can be joined with this matrix.
	 *
	 * @return boolean
	 * @param matrix DoubleMatrix
	 */
	public boolean canJoin(DoubleMatrix matrix) {
		return matrix.columns()==rows;
	}

	/**
	 * Test if can be multiplied with this matrix.
	 *
	 * @return boolean
	 * @param matrix DoubleMatrix
	 */
	public boolean canMulitplay(DoubleMatrix matrix) {
		return matrix.columns()==rows;
	}

	/** {@inheritDoc} */
	@Override
	public Object clone() {
		DoubleMatrix o= new DoubleMatrix(rows,columns);
		for (int i=0; i<rows;i++) {
			System.arraycopy(matrix[i], 0, o.matrix[i], 0, columns);
		}
		return o;
	}
	/**
	 * Nuber of columns in this matrix.
	 *
	 * @return double columns
	 */
	public int columns() {
		return columns;
	}

	/**
	 * Test if dimensions of these matrices match.
	 *
	 * @return boolean dimension equals
	 * @param matrix DoubleMatrix
	 */
	public boolean dimensionEquals(DoubleMatrix matrix) {
		return (matrix.columns()==columns)&&(matrix.rows()==rows);
	}

	/**
	 * Returns value from matrix at (row,column) position.
	 *
	 * @return double value
	 * @param r int row
	 * @param c int column
	 */
	public double get(int r, int c) {
		if (r<0 || r>rows) throw new IllegalDimensionException("No such row");
		if (c<0 || c>columns) throw new IllegalDimensionException("No such column");
		return matrix[r][c];
	}

	/**
	 * <p>identity.</p>
	 *
	 * @return DoubleMatrix
	 * @param dim int
	 */
	public static final DoubleMatrix identity(int dim) {
		DoubleMatrix m= new DoubleMatrix(dim);
		for (int i=0; i<dim; i++) m.matrix[i][i]=1.0;
		return m;
	}
	/**
	 * Multiplies <code>matrix</code> from right with this matrix and
	 * stores result in this matrix. Return Matrix is stih matrix.
	 *
	 * @return DoubleMatrix this matrix =<code>matrix</code> times this matrix
	 * @param matrix DoubleMatrix
	 */
	public DoubleMatrix join(DoubleMatrix matrix) {
		if (!canJoin(matrix)) throw new IllegalDimensionException("Can not join, matrix dimension does not match!");
		DBMath.multiply(matrix.matrix,DBMath.clone(this.matrix),this.matrix);
		return this;
	}

	/**
	 * Multiplies <code>matrix</code> from right with this matrix and
	 * returns result as new matrix
	 *
	 * @return DoubleMatrix result=<code>matrix</code> times this matrix
	 * @param matrix DoubleMatrix
	 */
	public DoubleMatrix multiplay(DoubleMatrix matrix) {
		if (!canMulitplay(matrix)) throw new IllegalDimensionException("Can not mulitply, matrix dimension does not match!");
		DoubleMatrix m= new DoubleMatrix(matrix.rows(),columns);
		DBMath.multiply(matrix.matrix,this.matrix,m.matrix);
		return m;
	}

	/**
	 * Nuber of rows in this matrix.
	 *
	 * @return double rows
	 */
	public int rows() {
		return rows;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		java.io.StringWriter sw= new java.io.StringWriter();
		java.io.PrintWriter pr= new java.io.PrintWriter(sw);
		int i,j;
		for (i=0; i<rows; i++){
			for (j=0; j<columns; j++){
				if ( j!=0 ) pr.print("\t");
				pr.print(matrix[i][j]);
			}
			pr.println();
		}
		return sw.toString();
	}
}
