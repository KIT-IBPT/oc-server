package org.scictrl.mp.orbitcorrect.utilities;

import org.scictrl.mp.orbitcorrect.Orientation;

/**
 * <p>IBeamTraceProvider interface.</p>
 *
 * @author igor@scictrl.com
 */
public interface IBeamTraceProvider {
/**
 * Returns beam trace calculation for given orientation
 *
 * @return double[][]
 * @param ori a {@link org.scictrl.mp.orbitcorrect.Orientation} object
 */
double[][] getBeamTrace(Orientation ori);
}
