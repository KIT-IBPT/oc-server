package org.scictrl.mp.orbitcorrect.accessories;

import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.scictrl.mp.orbitcorrect.IBeamSimulator;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.HorCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.VerCorrector;
/**
 * Simulates positions of beam in verical and horizontal plane at BPMonitors.
 *
 * @see org.scictrl.mp.orbitcorrect.IBeamSimulator
 * @author igor@scictrl.com
 */
public class DefaultBeamSimulator implements IBeamSimulator {
	/**
	 * Vertical BPM data.
	 */
	protected double[] ver = new double[0];
	/**
	 * Horizontal BPM data.
	 */
	protected double[] hor = new double[0];
	/**
	 * DefaultBeamSimulator constructor .
	 */
	public DefaultBeamSimulator() {
		super();
		//System.out.println("[DefaultBeamSimulator] created");
	}
	/**
	 * Simulates values of position of electron beam in horizontal plane at BPMonitors.
	 *
	 * @param dataBush a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} object
	 * @return an array of {@link double} objects
	 */
	public static double[] s_simulateHorizontal(org.scictrl.mp.orbitcorrect.model.optics.DataBush dataBush) {

		double[] hor= new double[dataBush.getBPMonitors().size()];

		double q = dataBush.getDataBushInfo().getQ().x();
		double a=0.0, b=0.0;
		double pos=0.0;

		Iterator<BPMonitor> itM = dataBush.getBPMonitors().iterator();
		Iterator<HorCorrector> itC;
		BPMonitor elM;
		AbstractCorrector elC;

		int i=0;

	/*		int i=-1;
			int j=-1;
			int k=-1;

			double[][] rmX= new double[ent.monitors.size()][ent.correctors.size()];
			double[][] rmZ= new double[ent.monitors.size()][ent.correctors.size()];
	*/
		while (itM.hasNext()) {
			elM = itM.next();
			pos= 0.0;
			b = Math.sqrt(elM.getBeta().x()) / 2.0 / Math.sin(q * Math.PI);// / 1000.0;
			itC = dataBush.getHorCorrectors().iterator();
			while (itC.hasNext()) {
				elC = itC.next();
				if (elC.getIndex() > elM.getIndex())
					a = Math.sqrt(elC.getBeta().x()) * Math.cos(((elM.getQ().x() - elC.getQ().x()) * 2.0 + q) * Math.PI);
				else
					a = Math.sqrt(elC.getBeta().x()) * Math.cos(((elM.getQ().x() - elC.getQ().x()) * 2.0 - q) * Math.PI);
				pos+= elC.getAngle() * a;
			}
			hor[i++]= pos*b;
		}

	//	}
		return hor;
	}
	/**
	 * Simulates values of position of electron beam in vertical plane at BPMonitors.
	 *
	 * @param dataBush a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} object
	 * @return an array of {@link double} objects
	 */
	public static double[] s_simulateVertical(org.scictrl.mp.orbitcorrect.model.optics.DataBush dataBush) {

		double[] ver= new double[dataBush.getBPMonitors().size()];

		double q = dataBush.getDataBushInfo().getQ().z();
		double a=0.0, b=0.0;
		double pos=0.0;

		Iterator<BPMonitor> itM = dataBush.getBPMonitors().iterator();
		Iterator<VerCorrector> itC;
		BPMonitor elM;
		AbstractCorrector elC;

		int i=0;

	/*		int i=-1;
			int j=-1;
			int k=-1;

			double[][] rmX= new double[ent.monitors.size()][ent.correctors.size()];
			double[][] rmZ= new double[ent.monitors.size()][ent.correctors.size()];
	*/
		while (itM.hasNext()) {
			elM = itM.next();
			pos= 0.0;
			b = Math.sqrt(elM.getBeta().z()) / 2.0 / Math.sin(q * Math.PI);// / 1000.0;
			itC = dataBush.getVerCorrectors().iterator();
			while (itC.hasNext()) {
				elC = itC.next();
				if (elC.getIndex() > elM.getIndex())
					a = Math.sqrt(elC.getBeta().z()) * Math.cos(((elM.getQ().z() - elC.getQ().z()) * 2.0 + q) * Math.PI);
				else
					a = Math.sqrt(elC.getBeta().z()) * Math.cos(((elM.getQ().z() - elC.getQ().z()) * 2.0 - q) * Math.PI);
				pos+= elC.getAngle() * a;
			}
			ver[i++]= pos*b;
		}

	//	}
		return ver;
	}
	/**
	 * {@inheritDoc}
	 *
	 * Simulates values of position of electron beam in horizontal plane at BPMonitors.
	 */
	@Override
	public double[] simulateHorizontal(org.scictrl.mp.orbitcorrect.model.optics.DataBush dataBush) {
		return hor=s_simulateHorizontal(dataBush);
	}
	/**
	 * {@inheritDoc}
	 *
	 * Simulates values of position of electron beam in vertical plane at BPMonitors.
	 */
	@Override
	public double[] simulateVertical(org.scictrl.mp.orbitcorrect.model.optics.DataBush dataBush) {
		return ver=s_simulateVertical(dataBush);
	}
	/** {@inheritDoc} */
	@Override
	public void configure(Configuration conf) {
	}
}
