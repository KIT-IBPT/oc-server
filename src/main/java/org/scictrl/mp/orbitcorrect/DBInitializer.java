package org.scictrl.mp.orbitcorrect;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scictrl.mp.orbitcorrect.model.IControlSystemEngine;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractDataBushElement;
import org.scictrl.mp.orbitcorrect.model.optics.DataBush;
import org.scictrl.mp.orbitcorrect.model.optics.DataBushInfo;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
/**
 * Initializes DataBush object
 *
 * @author igor@scictrl.com
 */
public class DBInitializer {

	/** Constant <code>DBINIT_DATA_FILE="DBinit.data"</code> */
	public static final String DBINIT_DATA_FILE = "DBinit.data";

	static final Logger log = LogManager.getLogger(DBInitializer.class);

	//public boolean allowClosing=true;

	/** Constant <code>R_INPUT_FILE_URL="dbinit.inputfile_url"</code> */
	public static final String R_INPUT_FILE_URL= "dbinit.inputfile_url";
	/** Constant <code>DB_READER="dbinit.dbreader"</code> */
	public static final String DB_READER= "dbinit.dbreader";
	/** Constant <code>R_BASE_URL="dbinit.base_url"</code> */
	public static final String R_BASE_URL= "dbinit.base_url";
	/** Constant <code>MIN_UPDATE_INTERVAL="dbinit.databush.minupdateinterval"</code> */
	public static final String MIN_UPDATE_INTERVAL= "dbinit.databush.minupdateinterval";
	/** Constant <code>POSITION_PRECISION="dbinit.databush.position_precision"</code> */
	public static final String POSITION_PRECISION= "dbinit.databush.position_precision";
	/** Constant <code>BPM_POSITION_PRECISION="dbinit.databush.bpm_position_precision"</code> */
	public static final String BPM_POSITION_PRECISION= "dbinit.databush.bpm_position_precision";
	/** Constant <code>CURRENT_PRECISION="dbinit.databush.current_precision"</code> */
	public static final String CURRENT_PRECISION= "dbinit.databush.current_precision";
	/** Constant <code>BEAM_SIMULATOR="dbinit.databush.beam_simulator"</code> */
	public static final String BEAM_SIMULATOR= "dbinit.databush.beam_simulator";
	/** Constant <code>APPLY_MODEL="dbinit.databush.apply_model"</code> */
	public static final String APPLY_MODEL= "dbinit.databush.apply_model";
	/** Constant <code>CONTROL_SYSTEM="dbinit.databush.control_system"</code> */
	public static final String CONTROL_SYSTEM= "dbinit.databush.control_system";
	/** Constant <code>CALCULATOR_FACTORY="dbinit.databush.calculator_factory"</code> */
	public static final String CALCULATOR_FACTORY= "dbinit.databush.calculator_factory";

	/** Build in defaults. */
	//public Configuration defaults;

	private org.scictrl.mp.orbitcorrect.model.optics.DataBush dataBush;
	private PropertiesConfiguration properties;
	private java.io.File configurationFile = null;

	/**
	 * <p>newInstance.</p>
	 *
	 * @param file a {@link java.io.File} object
	 * @return a {@link org.scictrl.mp.orbitcorrect.DBInitializer} object
	 * @throws java.io.IOException if any.
	 * @throws org.scictrl.mp.orbitcorrect.DataBushException if any.
	 */
	public static final DBInitializer newInstance(File file) throws IOException, DataBushException {
		if (file==null || !file.exists()) {
			log.warn("Config file ("+(file!=null?file.toString():"NONE")+") not valid, dummy initialization.");
			DBInitializer db= new DBInitializer(null);
			return db;
		}
		log.info("properties loaded from resource ("+(file!=null?file.toString():"NONE")+")");
		DBInitializer db= new DBInitializer(file);
		return db;
	}

	/**
	 * Constructor
	 * @throws IOException
	 * @throws DataBushException
	 */
	private DBInitializer(File file) throws IOException, DataBushException {
		super();
		configurationFile = file;

		//defaults= new PropertiesConfiguration();
		properties= new PropertiesConfiguration();

		// set meaningful default values
		properties.setProperty(R_BASE_URL, configurationFile.getParentFile().toURI().toURL().toString());
		//defaults.setProperty(R_INPUT_FILE_URL,"databush.txt");
		properties.setProperty(DB_READER,org.scictrl.mp.orbitcorrect.accessories.DefaultDBReader.class.getName());

		if (configurationFile!=null) {
			InputStream in=null;
			try {
				properties.load(in= new BufferedInputStream( new FileInputStream(configurationFile)));
			} catch (Throwable t) {
				log.warn("failed to load configuration: "+t.toString(),t);
			} finally {
				try {
					if (in!=null) in.close();
				} catch (IOException e) {
					//
				}
			}
			try {
				properties.setProperty(R_BASE_URL,new java.net.URL(configurationFile.toURI().toURL(),properties.getString(R_BASE_URL)).toExternalForm());
			} catch (Throwable t) {
				log.warn("failed to localize base URL: "+t.toString(),t);
			}
			log.info("Loaded configuration: "+properties.toString());
		}
		setPropertiesToDataBush(getDataBush());

	}
	/**
	 * <p>getBaseURL.</p>
	 *
	 * @return java.net.URL
	 * @throws java.net.MalformedURLException if any.
	 */
	public URL getBaseURL() throws java.net.MalformedURLException {
		return new URL(properties.getString(R_BASE_URL));
	}
/**
 * <p>clearDB.</p>
 */
public void clearDB() {
	getDataBush().clear();
}
/**
 * <p>Getter for the field <code>configurationFile</code>.</p>
 *
 * @return java.io.File
 */
public java.io.File getConfigurationFile() {
	return configurationFile;
}
	/**
	 * <p>Getter for the field <code>dataBush</code>.</p>
	 *
	 * @return DataBush
	 */
	public synchronized DataBush getDataBush() {
		if (dataBush==null) {
			try {
				dataBush = new DataBush();
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		}
		return dataBush;
	}
	/**
	 * Returns current configuration.
	 *
	 * @return configuration
	 */
	public Configuration getConfiguration() {
		return properties;
	}
	/**
	 * <p>getProperty.</p>
	 *
	 * @return java.lang.String
	 * @param key java.lang.String
	 */
	public String getProperty(String key) {
		return properties.getString(key);
	}
	/**
	 * Called whenever the part throws an exception.
	 * @param exception java.lang.Throwable
	 */
	private void handleException(Throwable exception) {

		/* Uncomment the following lines to print uncaught exceptions to stdout */
		log.warn("DBInitializer> --- UNCAUGHT EXCEPTION ---", exception);
		 exception.printStackTrace();
	}
	/**
	 * <p>initDB.</p>
	 *
	 * @throws org.scictrl.mp.orbitcorrect.DataBushException if any.
	 * @throws java.net.MalformedURLException if any.
	 */
	public void initDB() throws DataBushException, java.net.MalformedURLException {
		initDB(getDataBush(),getInputFileURL());
	}
	/**
	 *
	 */
	private void initDB(DataBush db, java.net.URL inputFile) throws DataBushException {
		ElementList<AbstractDataBushElement> l= readInputFile(inputFile);
		getDataBush().init(l);
	}
	/**
	 * <p>getInputFileURL.</p>
	 *
	 * @return java.net.URL
	 * @throws java.net.MalformedURLException if any.
	 */
	public URL getInputFileURL() throws java.net.MalformedURLException {
		if (properties.containsKey(R_INPUT_FILE_URL)) {
			return new URL(getBaseURL(),properties.getString(R_INPUT_FILE_URL));
		}
		return null;
	}
/**
 * main entrypoint - starts the part when it is run as an application
 *
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	try {
		DBInitializer aDBInitializer= DBInitializer.newInstance(new File ("."+DBINIT_DATA_FILE));
		aDBInitializer.initDB();
	} catch (Throwable exception) {
		log.warn("Exception occurred in main() of java.lang.Object", exception);
	}
}
/**
 * <p>newDBReader.</p>
 *
 * @return IDataBushReader
 * @throws java.lang.ClassNotFoundException if read fails
 * @throws java.lang.InstantiationException if read fails
 * @throws java.lang.IllegalAccessException if read fails
 * @throws java.lang.ClassCastException if read fails
 * @throws java.lang.IllegalArgumentException if read fails
 * @throws java.lang.reflect.InvocationTargetException if read fails
 * @throws java.lang.NoSuchMethodException if read fails
 * @throws java.lang.SecurityException if read fails
 */
public IDataBushReader newDBReader() throws ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
	IDataBushReader r=(IDataBushReader)Class.forName(properties.getString(DB_READER)).getDeclaredConstructor().newInstance();
	r.setProperties(ConfigurationConverter.getProperties(properties));
	return r;
}
	/**
	 * <p>readInputFile.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 * @throws org.scictrl.mp.orbitcorrect.DataBushException if any.
	 * @throws java.net.MalformedURLException if any.
	 */
	public ElementList<AbstractDataBushElement> readInputFile() throws org.scictrl.mp.orbitcorrect.DataBushException, java.net.MalformedURLException {
		return readInputFile(getInputFileURL());
	}
	private ElementList<AbstractDataBushElement> readInputFile(URL input) throws DataBushException {

		if (input==null) {
			log.warn("Input file is NULL, nothing loaded. DataBush will be empty.");
			ElementList<AbstractDataBushElement> l= new ElementList<>();
			l.add(new DataBushInfo("DBInfo.dummy",true,0,true,true,0,0,0,0,0,0,0,0));
			return l;
		}

		log.info("Loading input file "+input);

		IDataBushReader rr;
		try {
			rr= newDBReader();
		} catch (Exception e) {
			throw new DataBushException("[DBInitializer] failed to create IDataBushReader instance from "+properties.getString(DB_READER)+": "+e.toString());
		}

		ElementList<AbstractDataBushElement> l= null;
		try {
			rr.open(input);
			l= rr.read();
		} catch (Throwable e) {
			throw new DataBushException("[DBInitializer] failed to read from "+properties.getProperty(DB_READER)+": "+e.toString());
		} finally {
			try {
				rr.close();
			} catch (Throwable e) {e.printStackTrace();}
		}
		return l;
	}
	/**
	 *
	 * @param db DataBush
	 */
	private void setPropertiesToDataBush(DataBush db) throws DataBushException {
		if (null!=properties.getProperty(BEAM_SIMULATOR)) {
			log.info("Loading IBeamSimulator \""+properties.getProperty(BEAM_SIMULATOR)+"\" ...");
			try {
				IBeamSimulator bs= (IBeamSimulator)Class.forName(properties.getString(BEAM_SIMULATOR)).getDeclaredConstructor().newInstance();
				bs.configure(properties);
				db.setBeamSimulator(bs);
			} catch (Exception e) {
				log.error("Loading filed! "+e.toString(),e);
				e.printStackTrace();
				throw new DataBushException("Loading IBeamSimulator \""+properties.getProperty(BEAM_SIMULATOR)+"\" failed! "+e.toString(),e);
			}
		}
		if (null!=properties.getProperty(APPLY_MODEL)) {
			log.info("Loading ICurrentApplyModel \""+properties.getProperty(APPLY_MODEL)+"\" ...");
			try {
				ICurrentApplyModel cam= (ICurrentApplyModel)Class.forName(properties.getString(APPLY_MODEL)).getDeclaredConstructor().newInstance();
				cam.configure(properties);
				db.setCurrentApplyModel(cam);
			} catch (Exception e) {
				log.error("Loading filed! "+e.toString(),e);
				e.printStackTrace();
				throw new DataBushException("Loading ICurrentApplyModel \""+properties.getProperty(APPLY_MODEL)+"\" failed! "+e.toString(),e);
			}
		}
		if (null!=properties.getProperty(CALCULATOR_FACTORY)) {
			log.info("Loading ICalculatorModelFactory \""+properties.getProperty(CALCULATOR_FACTORY)+"\" ...");
			try {
				ICalculatorModelFactory cmf= (ICalculatorModelFactory)Class.forName(properties.getString(CALCULATOR_FACTORY)).getDeclaredConstructor().newInstance();
				cmf.configure(properties);
				db.setCalculatorModelFactory(cmf);
			} catch (Exception e) {
				log.error("Loading filed! "+e.toString(),e);
				e.printStackTrace();
				throw new DataBushException("Loading ICalculatorModelFactory \""+properties.getProperty(CALCULATOR_FACTORY)+"\" failed! "+e.toString(),e);
			}
		}
		if (null!=properties.getProperty(CONTROL_SYSTEM)) {
			log.info("Loading IControlSystemEngine \""+properties.getProperty(CONTROL_SYSTEM)+"\" ...");
			try {
				IControlSystemEngine cse= (IControlSystemEngine)Class.forName(properties.getString(CONTROL_SYSTEM)).getDeclaredConstructor().newInstance();
				db.setControlSystemEngine(cse);
			} catch (Exception e) {
				log.error("Loading filed! "+e.toString());
				e.printStackTrace();
				throw new DataBushException("Loading IControlSystemEngine \""+properties.getProperty(CONTROL_SYSTEM)+"\" failed! "+e.toString(),e);
			}
		}
		if (null!=properties.getProperty(BPM_POSITION_PRECISION)) {
			db.setBPMPositionPrecision(properties.getDouble(BPM_POSITION_PRECISION));
		}
		if (null!=properties.getProperty(CURRENT_PRECISION)) {
			db.setCurrentPrecision(properties.getDouble(CURRENT_PRECISION));
		}
		if (null!=properties.getProperty(MIN_UPDATE_INTERVAL)) {
			db.setMinUpdateInterval(properties.getLong(MIN_UPDATE_INTERVAL));
		}
		if (null!=properties.getProperty(POSITION_PRECISION)) {
			db.setPositionPrecision(properties.getDouble(POSITION_PRECISION));
		}
	}
}
