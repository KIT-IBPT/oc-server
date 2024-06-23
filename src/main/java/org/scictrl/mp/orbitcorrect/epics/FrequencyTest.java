package org.scictrl.mp.orbitcorrect.epics;

import org.scictrl.csshell.ConnectorUtilities;

/**
 * <p>
 * FrequencyTest class.
 * </p>
 *
 * @author igor@scictrl.com
 */
public class FrequencyTest {

	/**
	 * <p>
	 * main.
	 * </p>
	 *
	 * @param args an array of {@link java.lang.String} objects
	 */
	public static void main(String[] args) {
		try {
			System.setProperty(ConnectorUtilities.CSSHELL_LOGGING, Boolean.TRUE.toString());

			// String name= "A:TI:SignGen:SR-01:Frequency";

			// EControlSystemEngine engine= new EControlSystemEngine();

			// IDataConnector<Object> p= engine.connect(null);
			// p.waitTillConnected();

			// p.getConnection().addPropertyChangeListener(Connection.PROPERTY_VALUE, new
			// PropertyChangeListener() {
			// @Override
			// public void propertyChange(PropertyChangeEvent evt) {
			// System.out.println(System.currentTimeMillis()+" A:TI:SignGen:SR-01:Frequency
			// => "+evt.getPropertyName()+" "+evt.getNewValue());
			// }
			// });

			// EPICSConnection<Double> con= p.getConnection();

			// EPICSConnector econ= con.getConnector();

			// @SuppressWarnings("unchecked")
			// EPICSConnection<Long> con1= (EPICSConnection<Long>)
			// econ.newConnection("A:TI:SignGen:SR-01:Status:Stepping", DataType.LONG);

//			con1.addPropertyChangeListener(Connection.PROPERTY_VALUE, new PropertyChangeListener() {
//				@Override
//				public void propertyChange(PropertyChangeEvent evt) {
//					System.out.println(System.currentTimeMillis()+" A:TI:SignGen:SR-01:Status:Stepping => "+evt.getPropertyName()+" "+evt.getNewValue());
//				}
//			});
//
//			Channel ch= con.getChannel();
//
//			PutListener l= new PutListener() {
//				@Override
//				public void putCompleted(PutEvent ev) {
//					System.out.println(System.currentTimeMillis()+" ???? => "+ev.toString());
//				}
//			};
//
//			double val= 499.7139;
//			ch.put(val, l);
//
//			System.out.println("done!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private FrequencyTest() {
	}

}
