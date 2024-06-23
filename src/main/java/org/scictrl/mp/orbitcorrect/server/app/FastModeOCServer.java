/**
 *
 */
package org.scictrl.mp.orbitcorrect.server.app;

import java.beans.PropertyChangeEvent;

import org.apache.logging.log4j.Logger;
import org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionModel;
import org.scictrl.mp.orbitcorrect.correction.OrbitCorrectionOperator.State;
import org.scictrl.mp.orbitcorrect.correction.automatic.FastModeAutomaticOC;
import org.scictrl.mp.orbitcorrect.mvc.ApplicationEngine;

import si.ijs.anka.config.BootstrapLoader;

/**
 * <p>This is top level application server, loads configuration which binds published PV names with underlying functionality.
 * Among other things loads correct Databush machine physics engine.</p>
 *
 * <p>It has own application name and associated configuration, which is distinct from MachinePhysics engine (Databush).</p>
 *
 * @author igor@scictrl.com
 */
public class FastModeOCServer extends AbstractOCAppServer {

	/** Constant <code>APP_NAME="FastModeOC"</code> */
	public static final String APP_NAME = "FastModeOC";

	static private Logger log;

	private FastModeAutomaticOC automaticOC;

	/**
	 * <p>Constructor for FastModeOCServer.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 */
	public FastModeOCServer(String name) {
		super(name);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>automaticOC</code>.</p>
	 */
	@Override
	public FastModeAutomaticOC getAutomaticOC() {
		if (automaticOC == null) {
			automaticOC = new FastModeAutomaticOC();
			automaticOC.configure(getEngine().getConfiguration());
		}

		return automaticOC;
	}

	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects
	 */
	public static void main(String[] args) {

		try {

			String name= APP_NAME;
			if (args.length==1) {
				String s=args[0];
				if (s!=null) {
					s=s.trim();
				}
				if (s!=null && s.length()>0) {
					name=s;
				}
			}

			BootstrapLoader.checkLogging();

			final FastModeOCServer fastOC= new FastModeOCServer(name);

			fastOC.initialize();

			//System.out.println(fastOC.getDataBush().toString());

			//OrbitCorrectionController ctr= fastOC.getController();

			/*PrintWriter pw= new PrintWriter(System.out);

			ctr.getOrbitMonitor().printReport(pw);
			pw.flush();*/

			if (fastOC.getEngine().getState()!=ApplicationEngine.State.ACTIVE) {
				log.error("EXIT! Failed to start: "+fastOC.getEngine().getState());
				return;
			}

			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {

					if (fastOC.isActive()) {
						log.info("Shutdown initiated from shutdown hook.");
						fastOC.shutdown();
					}
				}
			});


		} catch (Exception e) {
			e.printStackTrace();
			log.error("EXIT! Failed to start: "+e,e);
		}


	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>initialize.</p>
	 */
	@Override
	public void initialize() {
		super.initialize();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Does initialization for specific correction model.
	 */
	@Override
	protected void initializeFor(IOrbitCorrectionModel m) {
		super.initializeFor(m);
	}

	/** {@inheritDoc} */
	@Override
	protected void notifyRecordChange(final PropertyChangeEvent evt) {

		super.notifyRecordChange(evt);

		//final String name= evt.getPropertyName();

		//if (getOperator().getState()==State.INACTIVE) {
		//}

	}

	/** {@inheritDoc} */
	@Override
	protected void notifyRecordWrite(final PropertyChangeEvent evt) {

		super.notifyRecordWrite(evt);

		final String name= evt.getPropertyName();

		log.info("Command '"+name+"' received.");

		try {

			if (getOperator().getState()==State.INACTIVE) {
			}
		} catch (Throwable t) {
			log.error("Command '"+name+"' failed: "+t.toString(), t);
		}
	}



	/** {@inheritDoc} */
	@Override
	protected void configure(IOrbitCorrectionModel model) {
	}

}
