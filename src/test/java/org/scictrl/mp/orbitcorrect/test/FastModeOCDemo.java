/**
 * 
 */
package org.scictrl.mp.orbitcorrect.test;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.core.config.Configurator;
import org.scictrl.csshell.RemoteException;
import org.scictrl.csshell.epics.server.Server;
import org.scictrl.csshell.epics.server.processor.MemoryValueProcessor;
import org.scictrl.mp.orbitcorrect.server.app.FastModeOCServer;

import gov.aps.jca.CAException;

/**
 * <p>FastModeOCDemo class.</p>
 *
 * @author igor@scictrl.com
 */
public class FastModeOCDemo {
	
	private static final String[] PVS= {
			"T:SR:BeamInfo:01",
			};
	
	private static final org.scictrl.csshell.epics.server.Record[] records(String... pvs) {
		org.scictrl.csshell.epics.server.Record[] r= new org.scictrl.csshell.epics.server.Record[pvs.length];
		
		for (int i = 0; i < r.length; i++) {
			r[i] = MemoryValueProcessor.newDoubleProcessor(pvs[i], "dummy", 0.0, false).getRecord();
		}
		
		return r;
	}

	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects
	 */
	public static void main(String[] args) {
		
		try {
			
			final FastModeOCDemo demo= new FastModeOCDemo();
			
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					demo.stop();
				}
			});
			
			demo.start();
			
		} catch (Exception e) {
			System.out.println("FAILED "+e.toString());
			e.printStackTrace();
		}
		
	}
	
	

	private Server server;
	private FastModeOCServer app;

	
	/**
	 * Constructor.
	 */
	public FastModeOCDemo() {
	}
	
	/**
	 * <p>stop.</p>
	 */
	public void stop() {
		
		if (app!=null) {
			app.shutdown();
		}
		
		if (server!=null) {
			server.destroy();
		}
		
	}

	/**
	 * <p>start.</p>
	 *
	 * @throws org.apache.commons.configuration.ConfigurationException if any.
	 * @throws org.scictrl.csshell.RemoteException if any.
	 * @throws gov.aps.jca.CAException if any.
	 */
	public void start() throws ConfigurationException, RemoteException, CAException {
		
		server= new Server();
		server.getDatabase().addAll(records(PVS));
		server.activate();

		System.setProperty("bundle.conf", "./Databush2/config");
		System.setProperty("bundle.home", "./Databush2/config");
		
		Configurator.reconfigure();

		app= new FastModeOCServer(FastModeOCServer.APP_NAME);
		
		app.initialize();
			
	}

}
