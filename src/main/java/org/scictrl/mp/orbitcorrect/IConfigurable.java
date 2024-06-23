/**
 *
 */
package org.scictrl.mp.orbitcorrect;

import org.apache.commons.configuration.Configuration;

/**
 * <p>IConfigurable interface.</p>
 *
 * @author igor@scictrl.com
 */
public interface IConfigurable {

	/**
	 * <p>configure.</p>
	 *
	 * @param conf a {@link org.apache.commons.configuration.Configuration} object
	 */
	public void configure(Configuration conf);

}
