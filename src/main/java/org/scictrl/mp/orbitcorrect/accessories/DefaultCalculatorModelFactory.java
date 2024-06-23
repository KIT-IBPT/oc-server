package org.scictrl.mp.orbitcorrect.accessories;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.scictrl.mp.orbitcorrect.EntryNotFound;
import org.scictrl.mp.orbitcorrect.IBendingCalculatorModel;
import org.scictrl.mp.orbitcorrect.ICalculatorModelFactory;
import org.scictrl.mp.orbitcorrect.IMagnetCalculatorModel;
/**
 * Sets models which calculations are used to calculate energy, field strength, atc.
 *
 * @see org.scictrl.mp.orbitcorrect.ICalculatorModelFactory
 * @author igor@scictrl.com
 */
public class DefaultCalculatorModelFactory implements ICalculatorModelFactory {
	/** Models map. */
	protected Map<String, AbstractMagnetCalculator> models= new HashMap<>(100);
	/**
	 * DefaultCalculatorModelFactory constructor.
	 */
	public DefaultCalculatorModelFactory() {
		super();
		System.out.println("[DefaultCalculatorModelFactory] created");
	}
	/** {@inheritDoc} */
	@Override
	public IBendingCalculatorModel getBendingCalculatorModel(java.lang.String entry) throws EntryNotFound {
		if (!models.containsKey(entry)||!(models.get(entry) instanceof IBendingCalculatorModel)) {
			double[] d={0.0};
			models.put(entry,new DefaultBendingCalculator(entry, new PolynomialFunction(), new PolynomialFunction(d,d))); //throw new EntryNotFound(entry);
		}
		return (IBendingCalculatorModel)models.get(entry);
	}
	/** {@inheritDoc} */
	@Override
	public IMagnetCalculatorModel getGenericMagnetCalculatorModel(java.lang.String entry) throws EntryNotFound {
		if (!models.containsKey(entry)||!(models.get(entry) instanceof IMagnetCalculatorModel)) models.put(entry,new DefaultGenericMagnetCalculator(entry,new PolynomialFunction())); //throw new EntryNotFound(entry);
		return (IMagnetCalculatorModel)models.get(entry);
	}
	/**
	 * {@inheritDoc}
	 *
	 * Sets given energy to all AbstractMagnetCalculator in Hashmap.
	 */
	@Override
	public void setEnergyToAll(double energy) {
		Iterator<AbstractMagnetCalculator> it= models.values().iterator();
		while (it.hasNext()) it.next().setEnergy(energy);
	}

	/** {@inheritDoc} */
	@Override
	public void configure(Configuration conf) {
	}
}
