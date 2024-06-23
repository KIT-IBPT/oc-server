package org.scictrl.mp.orbitcorrect.epics;

import org.scictrl.csshell.ConnectorUtilities;

/**
 * <p>Test class.</p>
 *
 * @author igor@scictrl.com
 */
public class Test {

	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects
	 */
	public static void main(String[] args) {
		try {

			System.setProperty(ConnectorUtilities.CSSHELL_LOGGING, Boolean.TRUE.toString());

			String[] names= {
					"ACS:RFSG_D.01:frequency",
					//"ACS:PBEND_S.01:current",
					//"ACS:PCH_S1.01:current",
					//"ACS:PCH_S1.02:current",
					//"ACS:PCH_S2.01:current",
					//"ACS:PCH_S2.02:current",
					"ACS:PCV_S1.01:current",
					//"ACS:DBPM_S1.01:verPosition",
					//"ACS:DBPM_S1.01:horPosition",
					//"ACS:DBPM_S1.02:verPosition",
					//"ACS:DBPM_S1.02:horPosition",
					//"ACS:DBPM_S2.01:verPosition",
					//"ACS:DBPM_S2.01:horPosition",
					"SR:BPM:S1:01:SA:X",
					"SR:BPM:S1:02:SA:X",
//					"SR:BPM:S1:03:SA:X",
//					"SR:BPM:S1:04:SA:X",
//					"SR:BPM:S1:05:SA:X",
//					"SR:BPM:S1:06:SA:X",
//					"SR:BPM:S1:07:SA:X",
//					"SR:BPM:S1:08:SA:X",
//					"SR:BPM:S2:01:SA:X",
//					"SR:BPM:S2:02:SA:X",
//					"SR:BPM:S2:03:SA:X",
//					"SR:BPM:S2:04:SA:X",
//					"SR:BPM:S2:05:SA:X",
//					"SR:BPM:S2:06:SA:X",
//					"SR:BPM:S2:07:SA:X",
//					"SR:BPM:S2:08:SA:X",
//					"SR:BPM:S2:09:SA:X",
//					"SR:BPM:S3:01:SA:X",
//					"SR:BPM:S3:02:SA:X",
//					"SR:BPM:S3:03:SA:X",
//					"SR:BPM:S3:04:SA:X",
//					"SR:BPM:S3:05:SA:X",
//					"SR:BPM:S3:06:SA:X",
//					"SR:BPM:S3:07:SA:X",
//					"SR:BPM:S3:08:SA:X",
//					"SR:BPM:S3:09:SA:X",
//					"SR:BPM:S3:10:SA:X",
//					"SR:BPM:S4:02:SA:X",
//					"SR:BPM:S4:03:SA:X",
//					"SR:BPM:S4:04:SA:X",
//					"SR:BPM:S4:05:SA:X",
//					"SR:BPM:S4:06:SA:X",
//					"SR:BPM:S4:07:SA:X",
//					"SR:BPM:S4:08:SA:X",
//					"SR:BPM:S4:09:SA:X",
//					"SR:BPM:S4:10:SA:X",
//					"SR:BPM:S2:09:SA:Y",
//					"SR:BPM:S4:10:SA:Y",
					//"ACS:PBEND_S.01:status",
					//"ACS:PCH_S1.01:status",
			};


			EControlSystemEngine engine= new EControlSystemEngine();

			for (int i = 0; i < names.length; i++) {
				EDataConnectorD p= (EDataConnectorD) engine.connect(names[i],false);
				p.getConnection().waitTillConnected();
				System.out.println("Connected "+names[i]+" "+p.getConnection().isConnected());
				if (p.getConnection().isConnected()) {
					System.out.println("Get "+names[i]+" = "+p.get());
					System.out.println("Get "+names[i]+".min   = "+p.getMinValue());
					System.out.println("Get "+names[i]+".max   = "+p.getMaxValue());
					System.out.println("Get "+names[i]+".format= "+p.getFormat());
				}

				if (i==0) {
					//engine.testRF(p);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.exit(0);
	}
	
	private Test() {
	}

}
