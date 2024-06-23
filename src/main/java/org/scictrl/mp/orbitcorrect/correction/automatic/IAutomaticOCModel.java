package org.scictrl.mp.orbitcorrect.correction.automatic;

import org.scictrl.mp.orbitcorrect.IConfigurable;

/**
 * <p>IAutomaticOCModel interface.</p>
 *
 * @author igor@kriznar.com
 */
public interface IAutomaticOCModel extends IConfigurable {
	/**
	 * <p>getName.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	String getName();

	/**
	 * Executes the AOC operation, called from execution thread started by {@link si.ijs.anka.databush2.correction.automatic.AutomaticOrbitCorrectionOperator}.
	 *
	 * @param operator a {@link org.scictrl.mp.orbitcorrect.correction.automatic.AutomaticOrbitCorrectionOperator} object
	 */
	void executeContinuous(AutomaticOrbitCorrectionOperator operator);

	/**
	 * <p>
	 * Executes the AOC operation, called from execution thread started by
	 * {@link si.ijs.anka.databush2.correction.automatic.AutomaticOrbitCorrectionOperator}.
	 * </p>
	 * <p>
	 * The correction loop is performed until correction model decided that can
	 * correct no more. Some correction loops are executed in steps, the number of
	 * steps to go is controlled with steps parameter.
	 * </p>
	 *
	 * @param steps    controls how many steps deep the loop recursion goes. If less
	 *                 then 0, then there is no step limit and loop is executed till
	 *                 end, if more than 0, then can go deeper for given count.
	 * @param operator a
	 *                 {@link org.scictrl.mp.orbitcorrect.correction.automatic.AutomaticOrbitCorrectionOperator}
	 *                 object
	 * @param dryRun   a boolean
	 */
	void executeSingleLoop(AutomaticOrbitCorrectionOperator operator, int steps, boolean dryRun);

}
