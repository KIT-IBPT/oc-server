package org.scictrl.mp.orbitcorrect.accessories;

import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.scictrl.mp.orbitcorrect.DBMath;
import org.scictrl.mp.orbitcorrect.IBeamSimulator;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.HorCorrector;
/**
 * <p>FrequencyBeamSimulator class.</p>
 *
 * @author igor@scictrl.com
 */
public class FrequencyBeamSimulator implements IBeamSimulator{
	private double[] hor = new double[0];
	//private double[] ver = new double[0];
	/**
	 * <p>Constructor for FrequencyBeamSimulator.</p>
	 */
	public FrequencyBeamSimulator() {
		super();
		System.out.println("[FrequencyBeamSimulator] created");
	}
	/**
	 * <p>calculateRelativeElectronEnergyShift.</p>
	 *
	 * @param databush DataBush
	 * @return a double
	 */
	public static double calculateRelativeElectronEnergyShift(DataBush databush) {

	//	double q = databush.getDataBushInfo().getQ().x();
	//	double a=0.0, b=0.0;
	//	double pos=0.0;
		//double dp_po=0.0;
		double C = databush.getOpticsLength();

		//Iterator<BPMonitor> itM = databush.getBPMonitors().iterator();
		Iterator<HorCorrector> itC1;
		//BPMonitor elM;
		AbstractCorrector elC;

		org.scictrl.mp.orbitcorrect.utilities.LinearOpticsCalculatorBean linearOpticsCalculator = new org.scictrl.mp.orbitcorrect.utilities.LinearOpticsCalculatorBean();
		linearOpticsCalculator.init(databush);
	    try {
			linearOpticsCalculator.update();
		} catch (InconsistentDataException e) {
			System.out.println("[FrequencyBeamSimulator] energy shift calculation probably failed: "+e.getMessage());
		}

		//int i=0;

		itC1 = databush.getHorCorrectors().iterator();
		//double konst = linearOpticsCalculator.getMomentumCompactFactor() * C;
		double dl_co = 0.0;
		while (itC1.hasNext()) {
			elC = itC1.next();
			dl_co = dl_co + databush.getDispersionList().get(elC.getName()).d() * elC.getAngle();
		}

		//System.out.println("dl_co "+dl_co);
		double df_fo=calculateRelativeFreqencyShift(databush);
		//System.out.println("df_fo "+df_fo);

		int iteration=10;

		return iterateEnergyShift(
			iteration,
			dl_co,
			C,
			linearOpticsCalculator.getMomentumCompactFactor(),
			df_fo/linearOpticsCalculator.getMomentumCompactFactor(),
			df_fo
		);

	}
	/**
	 * <p>calculateRelativeFreqencyShift.</p>
	 *
	 * @param db a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} object
	 * @return a double
	 */
	public static double calculateRelativeFreqencyShift(DataBush db) {
		if (db.getRFGenerator()==null) return Double.NaN;

	    double h = Math.rint(db.getOpticsLength()*db.getRFGenerator().getFrequency()*1000000.0/DBMath.C); // harmonic number
	    double frev; // revolution frequency in MHz
	    double C = db.getOpticsLength(); // circumference in m

	    double energy = db.getDataBushInfo().getEnergy();

	    frev = (DBMath.C) * Math.sqrt(1 - ( DBMath.MC2 / energy ) * (DBMath.MC2 / energy ))  / C  / 1000000.0;

	    return ((h * frev)-db.getRFGenerator().getFrequency())/h / frev;
	}
	/**
	 *
	 *
	 * @param dp_po_NEW double
	 */
	private static double dl(double sum,double dp_po_NEW,double konst) {
		return - sum - dp_po_NEW * konst;
	}
	/**
	 *
	 *
	 * @return double
	 */
	private static double iterateEnergyShift(double dl, double L, double momentumCompactionFactor, double dp_po, double df_fo) {
		double dp_po_NEW = dp_po + 1/momentumCompactionFactor*(dl/L - df_fo);
		return dp_po_NEW;
	}
	/**
	 *
	 *
	 * @return double
	 */
	private static double iterateEnergyShift(int iteration,  double dl_co, double L, double momentumCompactionFactor, double dp_po, double df_fo) {
		double dp_po_NEW = dp_po;
		//System.out.println("[start]: "+dp_po_NEW);

		for (int i = 0; i < iteration; i++) {
			dp_po_NEW = iterateEnergyShift(dl(dl_co,dp_po_NEW,momentumCompactionFactor*L),L,momentumCompactionFactor,dp_po_NEW,df_fo);
			//System.out.println("["+i+"]: "+dp_po_NEW);
		}

		return dp_po_NEW;
	}
	/**
	 * {@inheritDoc}
	 *
	 * simulateHorizontal method comment.
	 */
	@Override
	public double[] simulateHorizontal(org.scictrl.mp.orbitcorrect.model.optics.DataBush dataBush) {
		if (hor.length!=dataBush.getBPMonitors().size()) hor= new double[dataBush.getBPMonitors().size()];

	//	Thread.dumpStack();

		double q = dataBush.getDataBushInfo().getQ().x();
		double a=0.0, b=0.0;
		double pos=0.0;
		int i=0;

		Iterator<BPMonitor> itM = dataBush.getBPMonitors().iterator();
		Iterator<HorCorrector> itC;
		BPMonitor elM;
		AbstractCorrector elC;

		double dp_po = calculateRelativeElectronEnergyShift(dataBush);

		//System.out.print("dp "+dp_po+" > ");
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
			hor[i++]= pos*b - dataBush.getDispersionList().get(elM.getName()).d()*dp_po;
			//System.out.print((dataBush.getDispersionList().get(elM.getName()).d()*dp_po)+" ");
		}

		//System.out.println("<");
		return hor;
	}
	/**
	 * {@inheritDoc}
	 *
	 * simulateVertical method comment.
	 */
	@Override
	public double[] simulateVertical(org.scictrl.mp.orbitcorrect.model.optics.DataBush dataBush) {
		return DefaultBeamSimulator.s_simulateVertical(dataBush);
	}

	/** {@inheritDoc} */
	@Override
	public void configure(Configuration conf) {
	}
}
