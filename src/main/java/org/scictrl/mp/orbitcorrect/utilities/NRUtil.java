package org.scictrl.mp.orbitcorrect.utilities;

/* CAUTION: This is the ANSI C (only) version of the Numerical Recipes
   utility file nrutil.c.  Do not confuse this file with the same-named
   file nrutil.c that is supplied in the 'misc' subdirectory.
   *That* file is the one from the book, and contains both ANSI and
   traditional K&R versions, along with #ifdef macros to select the
   correct version.  *This* file contains only ANSI C.               */

/**
 * <p>
 * NRUtil class.
 * </p>
 *
 * @author igor@scictrl.com
 */
public class NRUtil {
	/** Constant <code>NR_END=1</code> */
	public static final int NR_END = 1;
	// private String FREE_ARG;

	/*
	 * unsigned char *cvector(long nl, long nh) /* allocate an unsigned char vector
	 * with subscript range v[nl..nh]
	 */
	/*
	 * { unsigned char *v;
	 * 
	 * v=(unsigned char *)malloc((size_t) ((nh-nl+1+NR_END)*sizeof(unsigned char)));
	 * if (!v) nrerror("allocation failure in cvector()"); return v-nl+NR_END; }
	 * 
	 * unsigned long *lvector(long nl, long nh) /* allocate an unsigned long vector
	 * with subscript range v[nl..nh]
	 */
	/*
	 * { unsigned long *v;
	 * 
	 * v=(unsigned long *)malloc((size_t) ((nh-nl+1+NR_END)*sizeof(long))); if (!v)
	 * nrerror("allocation failure in lvector()"); return v-nl+NR_END; }
	 * 
	 * double *dvector(long nl, long nh) /* allocate a double vector with subscript
	 * range v[nl..nh]
	 */
	/*
	 * { double *v;
	 * 
	 * v=(double *)malloc((size_t) ((nh-nl+1+NR_END)*sizeof(double))); if (!v)
	 * nrerror("allocation failure in dvector()"); return v-nl+NR_END; }
	 * 
	 * float **matrix(long nrl, long nrh, long ncl, long nch) /* allocate a float
	 * matrix with subscript range m[nrl..nrh][ncl..nch]
	 */
	/*
	 * { long i, nrow=nrh-nrl+1,ncol=nch-ncl+1; float **m;
	 * 
	 * /* allocate pointers to rows
	 */
	/*
	 * m=(float **) malloc((size_t)((nrow+NR_END)*sizeof(float*))); if (!m)
	 * nrerror("allocation failure 1 in matrix()"); m += NR_END; m -= nrl;
	 * 
	 * /* allocate rows and set pointers to them
	 */
	/*
	 * m[nrl]=(float *) malloc((size_t)((nrow*ncol+NR_END)*sizeof(float))); if
	 * (!m[nrl]) nrerror("allocation failure 2 in matrix()"); m[nrl] += NR_END;
	 * m[nrl] -= ncl;
	 * 
	 * for(i=nrl+1;i<=nrh;i++) m[i]=m[i-1]+ncol;
	 * 
	 * /* return pointer to array of pointers to rows
	 */
	/*
	 * return m; }
	 * 
	 * double **dmatrix(long nrl, long nrh, long ncl, long nch) /* allocate a double
	 * matrix with subscript range m[nrl..nrh][ncl..nch]
	 */
	/*
	 * { long i, nrow=nrh-nrl+1,ncol=nch-ncl+1; double **m;
	 * 
	 * /* allocate pointers to rows
	 */
	/*
	 * m=(double **) malloc((size_t)((nrow+NR_END)*sizeof(double*))); if (!m)
	 * nrerror("allocation failure 1 in matrix()"); m += NR_END; m -= nrl;
	 * 
	 * /* allocate rows and set pointers to them
	 */
	/*
	 * m[nrl]=(double *) malloc((size_t)((nrow*ncol+NR_END)*sizeof(double))); if
	 * (!m[nrl]) nrerror("allocation failure 2 in matrix()"); m[nrl] += NR_END;
	 * m[nrl] -= ncl;
	 * 
	 * for(i=nrl+1;i<=nrh;i++) m[i]=m[i-1]+ncol;
	 * 
	 * /* return pointer to array of pointers to rows
	 */
	/*
	 * return m; }
	 * 
	 * int **imatrix(long nrl, long nrh, long ncl, long nch) /* allocate a int
	 * matrix with subscript range m[nrl..nrh][ncl..nch]
	 */
	/*
	 * { long i, nrow=nrh-nrl+1,ncol=nch-ncl+1; int **m;
	 * 
	 * /* allocate pointers to rows
	 */
	/*
	 * m=(int **) malloc((size_t)((nrow+NR_END)*sizeof(int*))); if (!m)
	 * nrerror("allocation failure 1 in matrix()"); m += NR_END; m -= nrl;
	 * 
	 * 
	 * /* allocate rows and set pointers to them
	 */
	/*
	 * m[nrl]=(int *) malloc((size_t)((nrow*ncol+NR_END)*sizeof(int))); if (!m[nrl])
	 * nrerror("allocation failure 2 in matrix()"); m[nrl] += NR_END; m[nrl] -= ncl;
	 * 
	 * for(i=nrl+1;i<=nrh;i++) m[i]=m[i-1]+ncol;
	 * 
	 * /* return pointer to array of pointers to rows
	 */
	/*
	 * return m; }
	 * 
	 * float **submatrix(float **a, long oldrl, long oldrh, long oldcl, long oldch,
	 * long newrl, long newcl) /* point a submatrix [newrl..][newcl..] to
	 * a[oldrl..oldrh][oldcl..oldch]
	 */
	/*
	 * { long i,j,nrow=oldrh-oldrl+1,ncol=oldcl-newcl; float **m;
	 * 
	 * /* allocate array of pointers to rows
	 */
	/*
	 * m=(float **) malloc((size_t) ((nrow+NR_END)*sizeof(float*))); if (!m)
	 * nrerror("allocation failure in submatrix()"); m += NR_END; m -= newrl;
	 * 
	 * /* set pointers to rows
	 */
	/*
	 * for(i=oldrl,j=newrl;i<=oldrh;i++,j++) m[j]=a[i]+ncol;
	 * 
	 * /* return pointer to array of pointers to rows
	 */
	/*
	 * return m; }
	 * 
	 * float **convert_matrix(float *a, long nrl, long nrh, long ncl, long nch) /*
	 * allocate a float matrix m[nrl..nrh][ncl..nch] that points to the matrix
	 * declared in the standard C manner as a[nrow][ncol], where nrow=nrh-nrl+1 and
	 * ncol=nch-ncl+1. The routine should be called with the address &a[0][0] as the
	 * first argument.
	 */
	/*
	 * { long i,j,nrow=nrh-nrl+1,ncol=nch-ncl+1; float **m;
	 * 
	 * /* allocate pointers to rows
	 */
	/*
	 * m=(float **) malloc((size_t) ((nrow+NR_END)*sizeof(float*))); if (!m)
	 * nrerror("allocation failure in convert_matrix()"); m += NR_END; m -= nrl;
	 * 
	 * /* set pointers to rows
	 */
	/*
	 * m[nrl]=a-ncl; for(i=1,j=nrl+1;i<nrow;i++,j++) m[j]=m[j-1]+ncol; /* return
	 * pointer to array of pointers to rows
	 */
	/*
	 * return m; }
	 * 
	 * float ***f3tensor(long nrl, long nrh, long ncl, long nch, long ndl, long ndh)
	 * /* allocate a float 3tensor with range t[nrl..nrh][ncl..nch][ndl..ndh]
	 */
	/*
	 * { long i,j,nrow=nrh-nrl+1,ncol=nch-ncl+1,ndep=ndh-ndl+1; float ***t;
	 * 
	 * /* allocate pointers to pointers to rows
	 */
	/*
	 * t=(float ***) malloc((size_t)((nrow+NR_END)*sizeof(float**))); if (!t)
	 * nrerror("allocation failure 1 in f3tensor()"); t += NR_END; t -= nrl;
	 * 
	 * /* allocate pointers to rows and set pointers to them
	 */
	/*
	 * t[nrl]=(float **) malloc((size_t)((nrow*ncol+NR_END)*sizeof(float*))); if
	 * (!t[nrl]) nrerror("allocation failure 2 in f3tensor()"); t[nrl] += NR_END;
	 * t[nrl] -= ncl;
	 * 
	 * /* allocate rows and set pointers to them
	 */
	/*
	 * t[nrl][ncl]=(float *)
	 * malloc((size_t)((nrow*ncol*ndep+NR_END)*sizeof(float))); if (!t[nrl][ncl])
	 * nrerror("allocation failure 3 in f3tensor()"); t[nrl][ncl] += NR_END;
	 * t[nrl][ncl] -= ndl;
	 * 
	 * for(j=ncl+1;j<=nch;j++) t[nrl][j]=t[nrl][j-1]+ndep; for(i=nrl+1;i<=nrh;i++) {
	 * t[i]=t[i-1]+ncol; t[i][ncl]=t[i-1][ncl]+ncol*ndep; for(j=ncl+1;j<=nch;j++)
	 * t[i][j]=t[i][j-1]+ndep; }
	 * 
	 * /* return pointer to array of pointers to rows
	 */
	/**
	 * return t; }
	 *
	 * @param nl a int
	 * @param nh a int
	 * @return an array of {@link int} objects
	 */
	public static int[] ivector(int nl, int nh)
	/* allocate an int vector with subscript range v[nl..nh] */
	{
		return new int[nl + nh + 1];
	}

	/**
	 * <p>
	 * nrerror.
	 * </p>
	 *
	 * @param error_text a {@link java.lang.String} object
	 */
	public static void nrerror(String error_text)
	/* Numerical Recipes standard error handler */
	{
		System.err.println("Java Numerical Recipes run-time error...");
		System.err.println(error_text);
//	System.err.println("...now exiting to system...");
//	exit(1);
	}

	/**
	 * <p>
	 * sign.
	 * </p>
	 *
	 * @param a a double
	 * @param b a double
	 * @return a double
	 */
	public static double sign(double a, double b) {
		return ((b) >= 0.0 ? Math.abs(a) : -Math.abs(a));
	}

	/**
	 * <p>
	 * vector.
	 * </p>
	 *
	 * @param nl a int
	 * @param nh a int
	 * @return an array of {@link double} objects
	 */
	public static double[] vector(int nl, int nh)
	/* allocate a float vector with subscript range v[nl..nh] */
	{
		return new double[nl + nh + 1];
	}
	
	private NRUtil() {
	}
}
