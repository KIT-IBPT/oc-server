package org.scictrl.mp.orbitcorrect;


/**
 * <p>IBeamSimulator interface.</p>
 *
 * @author igor@scictrl.com
 */
public interface IBeamSimulator extends IConfigurable {

/**
 * <p>simulateHorizontal.</p>
 *
 * @param dataBush a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} object
 * @return an array of {@link double} objects
 */
double[] simulateHorizontal(org.scictrl.mp.orbitcorrect.model.optics.DataBush dataBush);

/**
 * <p>simulateVertical.</p>
 *
 * @param dataBush a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} object
 * @return an array of {@link double} objects
 */
double[] simulateVertical(org.scictrl.mp.orbitcorrect.model.optics.DataBush dataBush);
}
