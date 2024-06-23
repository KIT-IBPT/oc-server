package org.scictrl.mp.orbitcorrect;

/**
 * Interface that support reading of arrays of file.
 *
 * @author igor@scictrl.com
 */
public interface IMultipleFileReader {
	/**
	 * Open files, specified by file array.
	 *
	 * @param file java.io.File[]
	 * @throws org.scictrl.mp.orbitcorrect.DataBushException if any.
	 */
	void open(java.io.File[] file) throws org.scictrl.mp.orbitcorrect.DataBushException;
	/**
	 * Open files, specified by URL array.
	 *
	 * @param files java.net.URL[]
	 * @throws org.scictrl.mp.orbitcorrect.DataBushException if any.
	 */
	void open(java.net.URL[] files) throws org.scictrl.mp.orbitcorrect.DataBushException;
}
