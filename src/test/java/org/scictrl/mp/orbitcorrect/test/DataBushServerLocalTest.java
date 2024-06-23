/**
 * 
 */
package org.scictrl.mp.orbitcorrect.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scictrl.csshell.epics.server.Server;
import org.scictrl.csshell.epics.server.processor.MemoryValueProcessor;
import org.scictrl.mp.orbitcorrect.DBConst;
import org.scictrl.mp.orbitcorrect.server.DataBushServerLocal;
import org.scictrl.mp.orbitcorrect.server.app.AbstractOCAppServer;
import org.scictrl.mp.orbitcorrect.server.app.FastModeOCServer;

/**
 * <p>DataBushServerLocalTest class.</p>
 *
 * @author igor@scictrl.com
 */
public class DataBushServerLocalTest {
	
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

	private Server server;

	/**
	 * Constructor.
	 */
	public DataBushServerLocalTest() {
	}
	
	/**
	 * <p>setUp.</p>
	 *
	 * @throws java.lang.Exception if fails
	 */
	@Before
	public void setUp() throws Exception {
		
		server= new Server();
		
		server.getDatabase().addAll(records(PVS));
		
		server.activate();
		
	}

	/**
	 * <p>tearDown.</p>
	 *
	 * @throws java.lang.Exception if fails
	 */
	@After
	public void tearDown() throws Exception {
		server.destroy();
	}

	/**
	 * Test of initialization without meaningfull configuration file,
	 * everyting should be initialized, no nullpointer errors, no elements,
	 * perhaps some warnings.
	 */
	@Test
	public void testDummyInitialization() {
		
		Configurator.reconfigure();
		
		DataBushServerLocal dbsl;
		try {
			dbsl = new DataBushServerLocal();
			
			dbsl.initialize();
			
			assertNotNull(dbsl);
			assertNotNull(dbsl.getDBInitializer());
			assertNotNull(dbsl.getDataBush());
			assertNotNull(dbsl.getDataModel());
			
			dbsl.shutdown();

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
		
		
	}
	
	/**
	 * <p>testInitialization.</p>
	 */
	@Test
	public void testInitialization() {
		
		//System.setProperty("bundle.conf", "./Databush2/config");
		//System.setProperty("bundle.home", "./Databush2/config");
		
		Configurator.reconfigure();
		
		DataBushServerLocal dbsl;
		try {
			dbsl = new DataBushServerLocal();
			
			dbsl.initialize();
			
			assertNotNull(dbsl);
			assertNotNull(dbsl.getDBInitializer());
			assertNotNull(dbsl.getDataBush());
			assertNotNull(dbsl.getDataModel());

			assertEquals(dbsl.getDataBush().getStatus(), DBConst.DB_OPERATIONAL);
			
			dbsl.shutdown();
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	/**
	 * <p>testFastModeOCInitialization.</p>
	 */
	@Test
	public void testFastModeOCInitialization() {
		
		System.setProperty("bundle.conf", "./Databush2/config");
		System.setProperty("bundle.home", "./Databush2/config");
		
		Configurator.reconfigure();
		
		try {

			AbstractOCAppServer ser= new FastModeOCServer(FastModeOCServer.APP_NAME);
			
			ser.initialize();
			
			
			assertNotNull(ser);
			assertNotNull(ser.getServer());
			assertEquals(ser.getServer().getClass(), DataBushServerLocal.class);
			assertNotNull(((DataBushServerLocal) ser.getServer()).getDBInitializer());
			assertNotNull(ser.getServer().getDataBush());
			assertNotNull(ser.getServer().getDataModel());

			assertEquals(ser.getServer().getDataBush().getStatus(), DBConst.DB_OPERATIONAL);
	
			ser.shutdown();
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}
}
