package org.scictrl.mp.orbitcorrect.test;

import org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement;
import org.scictrl.mp.orbitcorrect.model.optics.DataBushInfo;
import org.scictrl.mp.orbitcorrect.model.optics.HashList;


/**
 * <p>Scrap class.</p>
 *
 * @author igor@scictrl.com
 */
public class Scrap {

	/**
	 * <p>Constructor for Scrap.</p>
	 */
	public Scrap() {
	}

	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects
	 */
	public static void main(String[] args) {
		
		DataBushInfo info= new DataBushInfo("Dummy");
		
		HashList<AbstractDataBushElement> dl= new HashList<AbstractDataBushElement>();
		
		dl.add(info);
		
		Object dl2= dl.clone();
		
		System.out.println(dl2);
		
		
	}

}
