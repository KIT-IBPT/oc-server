package org.scictrl.mp.orbitcorrect.utilities;

import org.scictrl.mp.orbitcorrect.DBMath;
import org.scictrl.mp.orbitcorrect.accessories.Utilities;

/**
 * <p>
 * SVDMethod class.
 * </p>
 *
 * @author igor@scictrl.com
 */
public final class SVDMethod {
	/**
	 * <p>
	 * main.
	 * </p>
	 *
	 * @param args java.lang.String[]
	 */
	public static void main(String args[]) {
		/*
		 * double[][] m= { {1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0},
		 * {9.0,1.0,2.0,3.0,4.0,5.0,6.0,7.0}, {8.0,9.0,1.0,2.0,3.0,4.0,5.0,6.0},
		 * {7.0,8.0,9.0,5.0,6.0,7.0,8.0,9.0}, {1.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0},
		 * {1.0,2.0,4.0,5.0,6.0,7.0,8.0,9.0}, {1.0,2.0,3.0,4.0,6.0,7.0,8.0,9.0},
		 * {1.0,2.0,3.0,4.0,5.0,6.0,8.0,9.0}, {1.0,2.0,3.0,4.0,5.0,6.0,7.0,9.0}
		 * 
		 * 
		 * 
		 * };
		 */
		double[][] m = { { Double.NaN, Double.NaN }, { Double.NaN, Double.NaN } };
		try {
			testMethod(m);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 *
	 * @return double
	 * @param a double
	 * @param b double
	 */
	private static double pythag(double a, double b) {
		double absa, absb;
		absa = Math.abs(a);
		absb = Math.abs(b);
		if (absa > absb)
			return absa * Math.sqrt(1.0 + Math.pow(absb / absa, 2.0));
		else
			return (absb == 0.0 ? 0.0 : absb * Math.sqrt(1.0 + Math.pow(absa / absb, 2.0)));
	}

	/**
	 *
	 * @return double
	 * @param a double
	 * @param b double
	 */
	private static double SIGN(double a, double b) {
		return b >= 0.0 ? Math.abs(a) : -Math.abs(a);
	}

	/**
	 * <p>
	 * solve.
	 * </p>
	 *
	 * @param sdvU        an array of {@link double} objects
	 * @param eigenvalues an array of {@link double} objects
	 * @param sdvV        an array of {@link double} objects
	 * @param y           an array of {@link double} objects
	 * @return an array of {@link double} objects
	 */
	public final static double[] solve(double[][] sdvU, double[] eigenvalues, double[][] sdvV, double[] y) {

		double[] tmp = new double[eigenvalues.length];
		double[] res = new double[eigenvalues.length];
		double a;
		for (int j = 0; j < eigenvalues.length; j++) {
			a = 0.0;
			if (eigenvalues[j] != 0.0) {
				for (int i = 0; i < y.length; i++)
					a += sdvU[i][j] * y[i];
				a /= eigenvalues[j];
			}
			tmp[j] = a;
		}

		for (int j = 0; j < eigenvalues.length; j++) {
			res[j] = 0.0;
			for (int i = 0; i < eigenvalues.length; i++)
				res[j] += sdvV[j][i] * tmp[i];
		}

		return res;
	}

	/**
	 * <p>
	 * svdcmp.
	 * </p>
	 *
	 * @param w double[]
	 * @param v double[][]
	 * @param u an array of {@link double} objects
	 */
	public static final void svdcmp(double[][] u, double[] w, double[][] v) {

		if (u == null || w == null || v == null)
			throw new IllegalArgumentException("SVD: argument is null u:" + u + " w:" + w + " v:" + v);
		if (u.length == 0 || w.length == 0 || v.length == 0)
			throw new IllegalArgumentException("SVD: argument is zero size matrix u.length:" + u.length + " w.length:"
					+ w.length + " v.length:" + v.length);
		if (u[0].length != w.length || w.length != v[0].length)
			throw new IllegalArgumentException("SVD: matrix dimensions not matched u[0].length:" + u[0].length
					+ " w.length:" + w.length + " v[0].length:" + v[0].length);
		if (v.length != v[0].length)
			throw new IllegalArgumentException(
					"SVD: matrix dimensions not matched v.length:" + v.length + " v[0].length:" + v[0].length);

//     void svdcmp(float **a, int m, int n, float w[], float **v)
		int m = u.length;
		int n = u[0].length;

//	float pythag(float a, float b);
		boolean flag;
		int i, its, j, jj, k, l, nm;
		double anorm, c, f, g, h, s, scale, x, y, z;
//	rv1=vector(1,n);
		double[] rv1 = new double[n];
		g = scale = anorm = 0.0;
		l = 0; // dodajam
		for (i = 0; i < n; i++) {
			l = i + 2;
			rv1[i] = scale * g;
			g = s = scale = 0.0;
			if (i < m) {
				for (k = i; k < m; k++)
					scale += Math.abs(u[k][i]);
				if (scale != 0.0) {
					for (k = i; k < m; k++) {
						u[k][i] /= scale;
						s += u[k][i] * u[k][i];
					}
					f = u[i][i];
					g = -SIGN(Math.sqrt(s), f);
					h = f * g - s;
					u[i][i] = f - g;
					for (j = l - 1; j < n; j++) {
						for (s = 0.0, k = i; k < m; k++) {
							s += u[k][i] * u[k][j];
						}
						f = s / h;
						for (k = i; k < m; k++) {
							u[k][j] += f * u[k][i];
						}
					}
					for (k = i; k < m; k++) {
						u[k][i] *= scale;
					}
				}
			}
			w[i] = scale * g;
			g = s = scale = 0.0;
			if (i + 1 <= m && i + 1 != n) {
				for (k = l - 1; k < n; k++) {
					scale += Math.abs(u[i][k]);
				}
				if (scale != 0.0) {
					for (k = l - 1; k < n; k++) {
						u[i][k] /= scale;
						s += u[i][k] * u[i][k];
					}
					f = u[i][l - 1];
					g = -SIGN(Math.sqrt(s), f);
					h = f * g - s;
					u[i][l - 1] = f - g;
					for (k = l - 1; k < n; k++) {
						rv1[k] = u[i][k] / h;
					}
					for (j = l - 1; j < m; j++) {
						for (s = 0.0, k = l - 1; k < n; k++) {
							s += u[j][k] * u[i][k];
						}
						for (k = l - 1; k < n; k++) {
							u[j][k] += s * rv1[k];
						}
					}
					for (k = l - 1; k < n; k++) {
						u[i][k] *= scale;
					}
				}
			}
			anorm = Math.max(anorm, (Math.abs(w[i]) + Math.abs(rv1[i])));
		}
		for (i = n - 1; i >= 0; i--) {
			if (i < n - 1) {
				if (g != 0.0) {
					for (j = l; j < n; j++) {
						v[j][i] = (u[i][j] / u[i][l]) / g;
					}
					for (j = l; j < n; j++) {
						for (s = 0.0, k = l; k < n; k++) {
							s += u[i][k] * v[k][j];
						}
						for (k = l; k < n; k++) {
							v[k][j] += s * v[k][i];
						}
					}
				}
				for (j = l; j < n; j++) {
					v[i][j] = v[j][i] = 0.0;
				}
			}
			v[i][i] = 1.0;
			g = rv1[i];
			l = i;
		}
		for (i = Math.min(m, n) - 1; i >= 0; i--) {
			l = i + 1;
			g = w[i];
			for (j = l; j < n; j++) {
				u[i][j] = 0.0;
			}
			if (g != 0.0) {
				g = 1.0 / g;
				for (j = l; j < n; j++) {
					for (s = 0.0, k = l; k < m; k++) {
						s += u[k][i] * u[k][j];
					}
					f = (s / u[i][i]) * g;
					for (k = i; k < m; k++) {
						u[k][j] += f * u[k][i];
					}
				}
				for (j = i; j < m; j++) {
					u[j][i] *= g;
				}
			} else {
				for (j = i; j < m; j++) {
					u[j][i] = 0.0;
				}
			}
			++u[i][i];
		}
		nm = 0;
		for (k = n - 1; k >= 0; k--) {
			for (its = 0; its < 30; its++) {
				flag = true;
				for (l = k; l >= 0; l--) {
					nm = l - 1;
					if (l == 0 || (Math.abs(rv1[l]) + anorm) == anorm) {
						flag = false;
						break;
					}
					if ((Math.abs(w[nm]) + anorm) == anorm) {
						break;
					}
				}
				if (flag) {
					c = 0.0;
					s = 1.0;
					for (i = l; i < k + 1; i++) {
						f = s * rv1[i];
						rv1[i] = c * rv1[i];
						if ((Math.abs(f) + anorm) == anorm)
							break;
						g = w[i];
						h = pythag(f, g);
						w[i] = h;
						h = 1.0 / h;
						c = g * h;
						s = -f * h;
						for (j = 0; j < m; j++) {
							y = u[j][nm];
							z = u[j][i];
							u[j][nm] = y * c + z * s;
							u[j][i] = z * c - y * s;
						}
					}
				}
				z = w[k];
				if (l == k) {
					if (z < 0.0) {
						w[k] = -z;
						for (j = 0; j < n; j++) {
							v[j][k] = -v[j][k];
						}
					}
					break;
				}
				if (its == 29)
					return; // nrerror("no convergence in 30 svdcmp iterations");
				x = w[l];
				nm = k - 1;
				y = w[nm];
				g = rv1[nm];
				h = rv1[k];
				f = ((y - z) * (y + z) + (g - h) * (g + h)) / (2.0 * h * y);
				g = pythag(f, 1.0);
				f = ((x - z) * (x + z) + h * ((y / (f + SIGN(g, f))) - h)) / x;
				c = s = 1.0;
				for (j = l; j <= nm; j++) {
					i = j + 1;
					g = rv1[i];
					y = w[i];
					h = s * g;
					g = c * g;
					z = pythag(f, h);
					rv1[j] = z;
					c = f / z;
					s = h / z;
					f = x * c + g * s;
					g = g * c - x * s;
					h = y * s;
					y *= c;
					for (jj = 0; jj < n; jj++) {
						x = v[jj][j];
						z = v[jj][i];
						v[jj][j] = x * c + z * s;
						v[jj][i] = z * c - x * s;
					}
					z = pythag(f, h);
					w[j] = z;
					if (z != 0.0) {
						z = 1.0 / z;
						c = f * z;
						s = h * z;
					}
					f = c * g + s * y;
					x = c * y - s * g;
					for (jj = 0; jj < m; jj++) {
						y = u[jj][j];
						z = u[jj][i];
						u[jj][j] = y * c + z * s;
						u[jj][i] = z * c - y * s;
					}
				}
				rv1[l] = 0.0;
				rv1[k] = f;
				w[k] = x;
			}
		}
//	free_vector(rv1,1,n);
	}

	/**
	 * <p>
	 * testMethod.
	 * </p>
	 *
	 * @param a an array of {@link double} objects
	 */
	public static void testMethod(double[][] a) {
		int m = a.length;
		int n = a[0].length;

		double[][] U = new double[m][n];

		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				U[i][j] = a[i][j];

		double[] W = new double[n];
		double[][] V = new double[n][n];

		svdcmp(U, W, V);

		System.out.println("*** Method test ***");
		System.out.println("Input");
		System.out.println(Utilities.printMatrix(a));
		System.out.println("U");
		System.out.println(Utilities.printMatrix(U));
		System.out.println("W");
		System.out.println(Utilities.printMatrix(W));
		System.out.println("V");
		System.out.println(Utilities.printMatrix(V));

		double[][] c = new double[n][m];
		c = DBMath.transpond(U);
		for (int i = 0; i < n; i++)
			for (int j = 0; j < m; j++)
				c[i][j] = (W[i] == 0) ? 0 : c[i][j] / W[i];
		c = DBMath.multiply(V, c);
		System.out.println("Inverse");
		System.out.println(Utilities.printMatrix(c));
		System.out.println("Identity?");
		System.out.println(Utilities.printMatrix(DBMath.multiply(a, c)));
		System.out.println("*** Ye End ***");

	}
	
	private SVDMethod() {
	}
}
