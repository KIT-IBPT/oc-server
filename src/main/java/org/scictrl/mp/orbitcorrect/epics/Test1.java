package org.scictrl.mp.orbitcorrect.epics;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.scictrl.csshell.Connection;
import org.scictrl.csshell.ConnectorUtilities;

/**
 * <p>Test1 class.</p>
 *
 * @author igor@scictrl.com
 */
public class Test1 {

	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects
	 */
	public static void main(String[] args) {
		try {

			System.setProperty(ConnectorUtilities.CSSHELL_LOGGING, Boolean.TRUE.toString());

			//String name= "ACS:PCV_S1.01:current";
			String name= "SR:BPM:S1:01:SA:X";


			EControlSystemEngine engine= new EControlSystemEngine();

			EDataConnectorD p= (EDataConnectorD)engine.connect(name,false);
			p.getConnection().waitTillConnected();

			p.getConnection().addPropertyChangeListener(Connection.PROPERTY_VALUE, new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					System.out.println(System.currentTimeMillis()+" "+evt.getPropertyName()+" "+evt.getNewValue());

				}
			});




		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private Test1() {
	}

}
