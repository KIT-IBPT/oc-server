package org.scictrl.mp.orbitcorrect.utilities;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import org.scictrl.mp.orbitcorrect.DataBushEvent;
import org.scictrl.mp.orbitcorrect.InconsistentDataException;
import org.scictrl.mp.orbitcorrect.Orientation;
import org.scictrl.mp.orbitcorrect.accessories.FileTokenizer;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
import org.scictrl.mp.orbitcorrect.model.optics.HorCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.PositionedData;
import org.scictrl.mp.orbitcorrect.model.optics.VerCorrector;

/**
 * <p>OrbitMonitor class.</p>
 *
 * @author igor@scictrl.com
 */
public final class OrbitMonitor extends PropertyChangeSupportable implements IBeamTraceProvider {

	/**
	 * <p>loadFromFile.</p>
	 *
	 * @param file a {@link java.io.File} object
	 * @param bpms a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @return a {@link org.scictrl.mp.orbitcorrect.utilities.Orbit} object
	 */
	public static final Orbit loadFromFile(File file, ElementList<BPMonitor> bpms) {

		FileTokenizer tok=null;
		HashMap<String, double[]> map= new HashMap<>();
		String s;
		try{
			tok= new FileTokenizer(file);
			tok.wordChars('_','_');
			tok.wordChars(':',':');
			while (tok.nextToken()!=FileTokenizer.TT_EOF) {
				if (tok.ttype!=FileTokenizer.TT_WORD) throw new java.text.ParseException("BPM name expected at "+tok.toString(),0);
				s= tok.sval;
				double[] d= new double[2];
				if (tok.nextToken()!=FileTokenizer.TT_WORD) throw new java.text.ParseException("horizontal displacment expected at "+tok.toString(),0);
				d[0]= Double.valueOf(tok.sval).doubleValue();
				if (tok.nextToken()!=FileTokenizer.TT_WORD) throw new java.text.ParseException("vertical displacment expected at "+tok.toString(),0);
				d[1]= Double.valueOf(tok.sval).doubleValue();
				map.put(s,d);
			}

			double[] x= new double[bpms.size()];
			double[] z= new double[bpms.size()];

			for (int i = 0; i < bpms.size(); i++) {
				BPMonitor b= bpms.get(i);
				double[] d= map.get(b.getName());
				x[i]=d[0];
				z[i]=d[1];
			}

			Orbit orbit= new Orbit(bpms,x,z);

			return orbit;
		} catch (Throwable t) {
			System.out.println("ERROR loading reference orbit from file "+tok.getURL().toString());
			t.printStackTrace();
		} finally {
			if (tok!=null) try {tok.close();} catch (java.io.IOException e) {}
		}
		return null;


	}

	enum Relative {NONE,SAVED,EXTERNAL;}

	/** Constant <code>PROPERTY_ALL_DATA="ALL_DATA"</code> */
	public final static java.lang.String PROPERTY_ALL_DATA = "ALL_DATA";
	/** Constant <code>PROPERTY_BEAM_POSITION="BEAM_POSITION"</code> */
	public final static java.lang.String PROPERTY_BEAM_POSITION = "BEAM_POSITION";
	/** Constant <code>PROPERTY_BEAM_TRACE="BEAM_TRACE"</code> */
	public final static java.lang.String PROPERTY_BEAM_TRACE = "BEAM_TRACE";
	/** Constant <code>PROPERTY_REFERENCE_ORBIT="REFERENCE_ORBIT"</code> */
	public final static java.lang.String PROPERTY_REFERENCE_ORBIT = "REFERENCE_ORBIT";
	/** Constant <code>PROPERTY_SAVED_BEAM_POSITION="SAVED_BEAM_POSITION"</code> */
	public final static java.lang.String PROPERTY_SAVED_BEAM_POSITION = "SAVED_BEAM_POSITION";
	/** Constant <code>PROPERTY_BEAM_STATISTICS="BEAM_STATISTICS"</code> */
	public final static java.lang.String PROPERTY_BEAM_STATISTICS = "BEAM_STATISTICS";

	private Orbit _absolute;
	private Orbit _user;
	private Orbit saved;
	private Orbit savedAbsolute;
	private Orbit externalReference;
	private Orbit reference;
	private Relative relativeTo = Relative.NONE;
	private boolean beamTraceEnabled = false;
	private double[][][] beamTrace= new double [2][2][0];
	private double[] startX = new double[2];
	private double[] startZ = new double[2];
	private DataBush db;
	ElementList<BPMonitor> bpms;
	//private final Logger log= Logger.getLogger(getClass());


	/**
	 * <p>Constructor for OrbitMonitor.</p>
	 */
	public OrbitMonitor() {
		super();
	}

	/**
	 * BeamAnalizatorBean constructor comment.
	 *
	 * @param db a {@link org.scictrl.mp.orbitcorrect.model.optics.DataBush} object
	 */
	@SuppressWarnings("unchecked")
	public void initialize(final DataBush db) {
		this.db=db;
		db.addDataBushListener(new org.scictrl.mp.orbitcorrect.DataBushAdapter() {
			@Override
			public void beamChanged(DataBushEvent e) {
				updateBeam();
			}
			@Override
			public void fieldChanged(DataBushEvent e) {
				if (beamTraceEnabled) updateBeamTrace();
			}
			@Override
			public void rfChanged(DataBushEvent e) {
				if (beamTraceEnabled) updateBeamTrace();
			}
			@Override
			public void statusChanged(DataBushEvent e) {
				if (OrbitMonitor.this.db.isStatusOperational()) {
					updateAll();
				}
			}
		});
		bpms= (ElementList<BPMonitor>) db.getBPMonitors().toElementList();
		updateAll();
	}

	/**
	 * <p>setBPMs.</p>
	 *
	 * @param bpms a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public void setBPMs(ElementList<BPMonitor> bpms) {
		this.bpms=bpms;
		updateAll();
	}
	/**
	 *
	 *
	 */
	private void _reloadOrbit() {
		_absolute= new Orbit(bpms, null);
		_user= new Orbit(_absolute,reference);
	}
	/**
	 *
	 *
	 */
	private synchronized void _updateBeamTrace(Orientation ori) {
		if (!beamTraceEnabled) {
			return;
		}
		int size= db.getBetaList().size();

		if (beamTrace==null || size!=beamTrace[0][0].length) {
			beamTrace= new double[2][2][size];

			for (int i=0; i<size; i++) beamTrace[0][0][i]= db.getBetaList().get(i).getPosition();
			beamTrace[1][0]= beamTrace[0][0];
		}

		if (!db.hasClosedOrbitSolution()) for (int i=0; i<size; i++) {
			beamTrace[0][1][i]=beamTrace[1][1][i]=0.0;
		}


		Iterator<HorCorrector> ith=null;
		Iterator<VerCorrector> itv=null;
		AbstractCorrector c;

		double q = read(db.getQ(),ori);

		for (int i=0; i<size; i++) {

			beamTrace[ori.ordinal()][1][i]=0.0;

			if (ori.isHorizontal()) ith=db.getHorCorrectors().iterator();
				else itv= db.getVerCorrectors().iterator();

			while ((ori.isHorizontal()) ? ith.hasNext() : itv.hasNext()) {
				c= ((ori.isHorizontal()) ? (AbstractCorrector)ith.next() : (AbstractCorrector)itv.next() );
				beamTrace[ori.ordinal()][1][i] += Math.sqrt(read(c.getBeta(),ori)) * Math.cos((Math.abs(read(db.getQList().get(i),ori) - read(c.getQ(),ori)) * 2.0 - q) * Math.PI) * c.getAngle();
			}

			beamTrace[ori.ordinal()][1][i] *= Math.sqrt(read(db.getBetaList().get(i),ori)) / 2.0 / Math.sin(q * Math.PI); // 1000.0;

		}


	}
	/**
	 *
	 *
	 */
	private synchronized void _updateTransferBeamTrace() {
		if (!beamTraceEnabled) {
			return;
		}

		int size= db.getBetaList().size();

		if (beamTrace==null || size!=beamTrace[0][0].length) {
			beamTrace= new double[2][2][size];

			for (int i=0; i<size; i++) beamTrace[0][0][i]= db.getBetaList().get(i).getPosition();
			beamTrace[1][0]= beamTrace[0][0];
		}


		org.scictrl.mp.orbitcorrect.math.DoubleVector pos= new org.scictrl.mp.orbitcorrect.math.DoubleVector(5);

		pos.set(0,startX[0]);
		pos.set(1,startX[1]);
		pos.set(2,startZ[0]);
		pos.set(3,startZ[1]);

		PositionedData p1,p2;
		Iterator<PositionedData> it= db.getQList().iterator();

		if (!it.hasNext()) return;
		p1=it.next();

		beamTrace[Orientation._HORIZONTAL][1][0]=pos.get(0);
		beamTrace[Orientation._VERTICAL][1][0]= pos.get(2);

		int index=1;

		while(it.hasNext()) {
			p2=it.next();

			try{
				pos= pos.multiplayVector(db.transferMatrix(p1,p2));
			} catch(InconsistentDataException e) {
				e.printStackTrace();
			}

			if ((p2.getSource() instanceof AbstractCorrector)&&(p2.getPosition()==((AbstractCorrector)p2.getSource()).getPosition())) {
				pos.set(((p2.getSource() instanceof HorCorrector) ? 1 : 3), pos.get((p2.getSource() instanceof HorCorrector) ? 1 : 3) + ((AbstractCorrector)p2.getSource()).getAngle());
			}

			beamTrace[Orientation._HORIZONTAL][1][index]=pos.get(0);
			beamTrace[Orientation._VERTICAL][1][index]= pos.get(2);
			index++;

			p1=p2;
		}



	}
	/**
	 * <p>getAbsoluteOrbit.</p>
	 *
	 * @return double[][]
	 */
	public synchronized Orbit getAbsoluteOrbit() {
		if (_absolute==null) {
			_reloadOrbit();
		}
		return _absolute;
	}
	/**
	 * <p>getUserOrbit.</p>
	 *
	 * @return double[][]
	 */
	public synchronized Orbit getUserOrbit() {
		if (_user==null) {
			_reloadOrbit();
		}
		return _user;
	}
	/** {@inheritDoc} */
	@Override
	public double[][] getBeamTrace(Orientation orientation) {
		return beamTrace[orientation.ordinal()];
	}
	/**
	 * <p>isBeamTraceEnabled.</p>
	 *
	 * @return boolean
	 */
	public boolean isBeamTraceEnabled() {
		return beamTraceEnabled;
	}
	/**
	 * <p>isRelativeToReference.</p>
	 *
	 * @return boolean
	 */
	public boolean isRelativeToReference() {
		return relativeTo!=Relative.NONE;
	}
	/**
	 * <p>printReport.</p>
	 *
	 * @return java.lang.String
	 */
	/*public void loadReport(java.io.Reader reader) throws java.io.IOException {
		java.io.StreamTokenizer s= new java.io.StreamTokenizer(reader);
		s.resetSyntax();
		s.wordChars('A','Z');
		s.wordChars('a','z');
		s.wordChars('\u00A0','\u00FF');
		s.wordChars('0','9');
		s.wordChars('-','-');
		s.wordChars('_','_');
		s.wordChars('.','.');
		s.whitespaceChars('\u0000','\u0020');
		s.commentChar('/');
		s.quoteChar('"');
		s.quoteChar('\'');
		s.eolIsSignificant(true);
		s.slashSlashComments(true);
		s.slashStarComments(true);

		while (s.nextToken()!=StreamTokenizer.TT_EOF) {
			if (s.ttype==StreamTokenizer.TT_WORD && s.sval.equals("name")) break;
		}

		int columns=0;
		while (s.nextToken()!=StreamTokenizer.TT_EOF && s.ttype!=StreamTokenizer.TT_EOL) columns++;

		boolean eol=false;
		int i;

		while (s.nextToken()!=StreamTokenizer.TT_EOF && (s.ttype==StreamTokenizer.TT_WORD || s.ttype==StreamTokenizer.TT_EOL)) {
			if (s.ttype==StreamTokenizer.TT_EOL) {
				if (eol) break;
				eol=true;
			} else eol=false;

			if ((i=getDataBush().getBPMonitors().indexOf(getDataBush().getBPMonitors().get(s.sval))) < 0) break;
			if (s.nextToken()!=StreamTokenizer.TT_WORD) break;
			getAbsoluteOrbit(Orientation.HORIZONTAL)[i]= Double.valueOf(s.sval).doubleValue();
			if (s.nextToken()!=StreamTokenizer.TT_WORD) break;
			getAbsoluteOrbit(Orientation.VERTICAL)[i]= Double.valueOf(s.sval).doubleValue();

			if (columns==8) {
				getPositions(Orientation.HORIZONTAL)[i]= getAbsoluteOrbit(Orientation.HORIZONTAL)[i];
				getPositions(Orientation.VERTICAL)[i]= getAbsoluteOrbit(Orientation.VERTICAL)[i];
			} else {
				if (s.nextToken()!=StreamTokenizer.TT_WORD) break;
				getPositions(Orientation.HORIZONTAL)[i]= Double.valueOf(s.sval).doubleValue();
				if (s.nextToken()!=StreamTokenizer.TT_WORD) break;
				getPositions(Orientation.VERTICAL)[i]= Double.valueOf(s.sval).doubleValue();
			}

			out: {
				if (s.nextToken()!=StreamTokenizer.TT_WORD) break out;
				getAveragedPositions(Orientation.HORIZONTAL)[i]= Double.valueOf(s.sval).doubleValue();
				if (s.nextToken()!=StreamTokenizer.TT_WORD) break out;
				getAveragedPositions(Orientation.VERTICAL)[i]= Double.valueOf(s.sval).doubleValue();
				if (s.nextToken()!=StreamTokenizer.TT_WORD) break out;
				getSavedPositions(Orientation.HORIZONTAL)[i]= Double.valueOf(s.sval).doubleValue();
				if (s.nextToken()!=StreamTokenizer.TT_WORD) break out;
				getSavedPositions(Orientation.VERTICAL)[i]= Double.valueOf(s.sval).doubleValue();
				if (s.nextToken()!=StreamTokenizer.TT_WORD) break out;
	//			getAveragedPositions(HORIZONTAL)[i]-getSavedPositions(HORIZONTAL)[i]);
				if (s.nextToken()!=StreamTokenizer.TT_WORD) break out;
	//			getAveragedPositions(VERTICAL)[i]-getSavedPositions(VERTICAL)[i]);
				if (columns==8) {
					positionsSTD[Orientation._HORIZONTAL][i]= 0.0;
					positionsSTD[Orientation._VERTICAL][i]= 0.0;
					referenceOrbit[Orientation._HORIZONTAL][i]= 0.0;
					referenceOrbit[Orientation._VERTICAL][i]= 0.0;
					break out;
				}
				if (s.nextToken()!=StreamTokenizer.TT_WORD) break out;
				positionsSTD[Orientation._HORIZONTAL][i]= Double.valueOf(s.sval).doubleValue();
				if (s.nextToken()!=StreamTokenizer.TT_WORD) break out;
				positionsSTD[Orientation._VERTICAL][i]= Double.valueOf(s.sval).doubleValue();
				if (s.nextToken()!=StreamTokenizer.TT_WORD) break out;
				referenceOrbit[Orientation._HORIZONTAL][i]= Double.valueOf(s.sval).doubleValue();
				if (s.nextToken()!=StreamTokenizer.TT_WORD) break out;
				referenceOrbit[Orientation._VERTICAL][i]= Double.valueOf(s.sval).doubleValue();
			}
			while (s.ttype!=StreamTokenizer.TT_EOF && s.ttype!=StreamTokenizer.TT_EOL) s.nextToken();
			if (s.ttype==StreamTokenizer.TT_EOL) {
				if (eol) break;
				eol=true;
			} else eol=false;
		}

		updateAvgPositionsAsElements(Orientation.HORIZONTAL);
		updateAvgPositionsAsElements(Orientation.VERTICAL);
		calculateStatistics();
		calculateStatisticsForSaved(Orientation.HORIZONTAL);
		calculateStatisticsForSaved(Orientation.VERTICAL);

		firePropertyChange(PROPERTY_BEAM_POSITION);
		firePropertyChange(PROPERTY_SAVED_BEAM_POSITION);
		firePropertyChange(PROPERTY_BEAM_STATISTICS);
	}*/
	public String printReport() {
		java.io.StringWriter s= new java.io.StringWriter();
		java.io.PrintWriter p= new java.io.PrintWriter(s);
		printReport(p);
		return s.toString();
	}
	/**
	 * <p>printReport.</p>
	 *
	 * @param p a {@link java.io.PrintWriter} object
	 */
	public void printReport(java.io.PrintWriter p) {
		p.println("Statistics");
		p.println("orbit\tHOR_AVG\tHOR_RMS\tHOR_STD\tHOR_MAX\tVER_AVG\tVER_RMS\tVER_STD\tVER_MAX");
		p.print("absolute\t");
		Orbit absolute=getAbsoluteOrbit();
		Orbit user=getUserOrbit();
		p.print(absolute.avg[Orientation._HORIZONTAL]);
		p.print("\t");
		p.print(absolute.rms[Orientation._HORIZONTAL]);
		p.print("\t");
		p.print(absolute.std[Orientation._HORIZONTAL]);
		p.print("\t");
		p.print(absolute.max[Orientation._HORIZONTAL]);
		p.print("\t");
		p.print(absolute.avg[Orientation._VERTICAL]);
		p.print("\t");
		p.print(absolute.rms[Orientation._VERTICAL]);
		p.print("\t");
		p.print(absolute.std[Orientation._VERTICAL]);
		p.print("\t");
		p.println(absolute.max[Orientation._VERTICAL]);

		p.print("user\t");
		p.print(user.avg[Orientation._HORIZONTAL]);
		p.print("\t");
		p.print(user.rms[Orientation._HORIZONTAL]);
		p.print("\t");
		p.print(user.std[Orientation._HORIZONTAL]);
		p.print("\t");
		p.print(user.max[Orientation._HORIZONTAL]);
		p.print("\t");
		p.print(user.avg[Orientation._VERTICAL]);
		p.print("\t");
		p.print(user.rms[Orientation._VERTICAL]);
		p.print("\t");
		p.print(user.std[Orientation._VERTICAL]);
		p.print("\t");
		p.println(user.max[Orientation._VERTICAL]);

		if (saved!=null) {
			p.print("seved\t");
			p.print(saved.avg[Orientation._HORIZONTAL]);
			p.print("\t");
			p.print(saved.rms[Orientation._HORIZONTAL]);
			p.print("\t");
			p.print(saved.std[Orientation._HORIZONTAL]);
			p.print("\t");
			p.print(saved.max[Orientation._HORIZONTAL]);
			p.print("\t");
			p.print(saved.avg[Orientation._VERTICAL]);
			p.print("\t");
			p.print(saved.rms[Orientation._VERTICAL]);
			p.print("\t");
			p.print(saved.std[Orientation._VERTICAL]);
			p.print("\t");
			p.println(saved.max[Orientation._VERTICAL]);
		}

	//	p.println("name\tHOR_last_real\tVER_last_real\tHOR_last\tVER_last\tHOR_avg\tVER_avg\tHOR_saved\tVER_saved\tHOR_avg-saved\tVER_avg-saved\tHOR_std\tVER_std\tHOR_reference\tVER_reference");
		p.println("name\tHOR_abs\tVER_abs\tHOR_reference\tVER_reference");

		Iterator<BPMonitor> it = bpms.iterator();
		int i=0;
		while (it.hasNext()) {
			p.print(it.next().getName());
			p.print("\t");
	/*		p.print(getAbsoluteOrbit(Orientation.HORIZONTAL)[i]);
			p.print("\t");
			p.print(getAbsoluteOrbit(Orientation.VERTICAL)[i]);
			p.print("\t");
			p.print(getPositions(Orientation.HORIZONTAL)[i]);
			p.print("\t");
			p.print(getPositions(Orientation.VERTICAL)[i]);
			p.print("\t");*/
			p.print(absolute.positions[Orientation._H][i]);
			p.print("\t");
			p.print(absolute.positions[Orientation._V][i]);
			p.print("\t");
	/*		p.print(getSavedPositions(Orientation.HORIZONTAL)[i]);
			p.print("\t");
			p.print(getSavedPositions(Orientation.VERTICAL)[i]);
			p.print("\t");
			p.print(getAveragedPositions(Orientation.HORIZONTAL)[i]-getSavedPositions(Orientation.HORIZONTAL)[i]);
			p.print("\t");
			p.print(getAveragedPositions(Orientation.VERTICAL)[i]-getSavedPositions(Orientation.VERTICAL)[i]);
			p.print("\t");
			p.print(positionsSTD[Orientation._HORIZONTAL][i]);
			p.print("\t");
			p.print(positionsSTD[Orientation._VERTICAL][i]);
			p.print("\t");*/
			p.print(reference.positions[Orientation._H][i]);
			p.print("\t");
			p.println(reference.positions[Orientation._V][i]);
			i++;
		}
	}
	/**
	 *
	 *
	 * @return double
	 * @param data PositionedData
	 * @param orientation int
	 */
	private final static double read(org.scictrl.mp.orbitcorrect.model.optics.PositionedData data, Orientation orientation) {
		if (orientation==Orientation.VERTICAL) return data.z();
		else return data.x();
	}
	/**
	 * <p>updateAll.</p>
	 */
	protected void updateAll() {
		updateBeam();
		if (beamTraceEnabled) updateBeamTrace();
		firePropertyChange(PROPERTY_ALL_DATA);
	}
	/**
	 * <p>saveOrbit.</p>
	 */
	public void saveOrbit() {

		Orbit user=null;
		Orbit absolute=null;

		synchronized (this) {
			user=getUserOrbit();
			absolute=getAbsoluteOrbit();
		}

		saved=user;
		savedAbsolute=absolute;

		firePropertyChange(PROPERTY_BEAM_STATISTICS);
		firePropertyChange(PROPERTY_SAVED_BEAM_POSITION);

		if (relativeTo==Relative.SAVED) {
			reference=savedAbsolute;

			synchronized (this) {
				_user= new Orbit(_absolute, reference );
			}
			if (savedAbsolute!=null) {
				saved=new Orbit(savedAbsolute, reference);
			}
			firePropertyChange(PROPERTY_BEAM_POSITION);
			firePropertyChange(PROPERTY_REFERENCE_ORBIT);
		}
	}
	/**
	 * <p>Setter for the field <code>beamTraceEnabled</code>.</p>
	 *
	 * @param newBeamTraceEnabled boolean
	 */
	public void setBeamTraceEnabled(boolean newBeamTraceEnabled) {
		beamTraceEnabled = newBeamTraceEnabled;
	}
	/**
	 * <p>Setter for the field <code>externalReference</code>.</p>
	 *
	 * @param ref a {@link org.scictrl.mp.orbitcorrect.utilities.Orbit} object
	 */
	public void setExternalReference(Orbit ref) {
		externalReference = ref;
		if (relativeTo==Relative.EXTERNAL) {
			reference=ref;
			synchronized (this) {
				if (_absolute!=null) {
					_user= new Orbit(_absolute, ref );
				}
			}
			if (savedAbsolute!=null) {
				saved=new Orbit(savedAbsolute, ref);
			}
			firePropertyChange(PROPERTY_BEAM_POSITION);
			firePropertyChange(PROPERTY_BEAM_STATISTICS);
			firePropertyChange(PROPERTY_SAVED_BEAM_POSITION);
			firePropertyChange(PROPERTY_REFERENCE_ORBIT);
		}
	}
	/**
	 * <p>setRelativeToReference.</p>
	 *
	 * @param ref a {@link org.scictrl.mp.orbitcorrect.utilities.OrbitMonitor.Relative} object
	 */
	public void setRelativeToReference(Relative ref) {
		if (relativeTo != ref) {
			relativeTo= ref;

			if (relativeTo==Relative.EXTERNAL) {
				reference=externalReference;
			} else if (relativeTo==Relative.SAVED) {
				reference=savedAbsolute;
			} else {
				reference=null;
			}

			synchronized (this) {
				if (_absolute!=null) {
					_user= new Orbit(getAbsoluteOrbit(), reference);
				}
			}
			if (savedAbsolute!=null) {
				saved=new Orbit(savedAbsolute, reference);
			}

			firePropertyChange(PROPERTY_BEAM_POSITION);
			firePropertyChange(PROPERTY_BEAM_STATISTICS);
			firePropertyChange(PROPERTY_SAVED_BEAM_POSITION);
			firePropertyChange(PROPERTY_REFERENCE_ORBIT);
		}
	}
	/**
	 * <p>setSavedAsReference.</p>
	 */
	public synchronized void setSavedAsReference() {
		setRelativeToReference(Relative.SAVED);
	}
	/**
	 * <p>setExternalAsReference.</p>
	 */
	public synchronized void setExternalAsReference() {
		setRelativeToReference(Relative.EXTERNAL);
	}
	/**
	 * <p>setNoReference.</p>
	 */
	public synchronized void setNoReference() {
		setRelativeToReference(Relative.NONE);
	}
	/**
	 * <p>updateBeam.</p>
	 */
	public void updateBeam() {
		synchronized (this) {
			_user=null;
			_absolute=null;
		}
		firePropertyChange(PROPERTY_BEAM_POSITION);
	}
	/**
	 * <p>updateBeamTrace.</p>
	 */
	public void updateBeamTrace() {
		if (db.getDataBushInfo().isOrbitClosed()) {
			_updateBeamTrace(Orientation.HORIZONTAL);
			_updateBeamTrace(Orientation.VERTICAL);
		} else _updateTransferBeamTrace();
		firePropertyChange(PROPERTY_BEAM_TRACE);
	}
}
