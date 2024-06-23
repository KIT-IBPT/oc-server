package org.scictrl.mp.orbitcorrect;


/**
 * Internal class with string constants.
 *
 * @author igor@scictrl.com
 */
public interface DBString {
	/** Constant <code>ERR_CONN="error connecting"</code> */
	String	ERR_CONN= "error connecting";
	/** Constant <code>ERR_AP="error applying"</code> */
	String	ERR_AP= "error applying";
	/** Constant <code>ERR_UP="error updating"</code> */
	String	ERR_UP= "error updating";
	/** Constant <code>ERR_AP_="error applying: "</code> */
	String	ERR_AP_= "error applying: ";
	/** Constant <code>ERR_UP_="error updating: "</code> */
	String	ERR_UP_= "error updating: ";
	/** Constant <code>ERR_CONN_="error connecting: "</code> */
	String	ERR_CONN_= "error connecting: ";
	/** Constant <code>ERR_NOT_CONN="device is not connected"</code> */
	String	ERR_NOT_CONN= "device is not connected";
	/** Constant <code>ERR_AP_NOT_CONN="ERR_AP_+ERR_NOT_CONN"</code> */
	String	ERR_AP_NOT_CONN= ERR_AP_+ERR_NOT_CONN;
	/** Constant <code>ERR_UP_NOT_CONN="ERR_UP_+ERR_NOT_CONN"</code> */
	String	ERR_UP_NOT_CONN= ERR_UP_+ERR_NOT_CONN;
	/** Constant <code>CF_RES_MON="resuming monitor"</code> */
	String	CF_RES_MON= "resuming monitor";
	/** Constant <code>CF_SUS_MON="suspending monitor"</code> */
	String	CF_SUS_MON= "suspending monitor";
	/** Constant <code>CF_SET_MON="setting monitor"</code> */
	String	CF_SET_MON= "setting monitor";
	/** Constant <code>NO_PS="there is no PowerSupply handle"</code> */
	String	NO_PS= "there is no PowerSupply handle";
	/** Constant <code>NO_PS_BEAN="PowerSupplyBean does not exists"</code> */
	String	NO_PS_BEAN= "PowerSupplyBean does not exists";
	/** Constant <code>NO_BPM_BEAN="BeamPositionMonitor does not exists"</code> */
	String	NO_BPM_BEAN= "BeamPositionMonitor does not exists";
	/** Constant <code>NO_CALC="there is no Calculator"</code> */
	String	NO_CALC= "there is no Calculator";
	/** Constant <code>NO_BEND="there is no Bending handle"</code> */
	String	NO_BEND= "there is no Bending handle";

	/** Constant <code>NL="DBConst.EOL"</code> */
	String NL=DBConst.EOL;
	/** Constant <code>BAD_DATA="Unable to change from FULL_ACCESS_MODE "{trunked}</code> */
	String BAD_DATA= "Unable to change from FULL_ACCESS_MODE becouse of inconsistend data: ";
	/** Constant <code>ERROR_CLOSE_CFILE="Error closing calibration file \&quot; {"{trunked}</code> */
	String ERROR_CLOSE_CFILE= "Error closing calibration file \"{0}\"";
	/** Constant <code>ERROR_OPEN_CFILE="Error openning calibration file \&quot; "{trunked}</code> */
	String ERROR_OPEN_CFILE= "Error openning calibration file \"{0}\"";
	/** Constant <code>ERROR_INST_CALC="Error instantieting calculator \&quot; {"{trunked}</code> */
	String ERROR_INST_CALC= "Error instantieting calculator \"{0}\" :{1}";
	/** Constant <code>ERROR_INIT_CALC="Error instializateing calculator \&quot; "{trunked}</code> */
	String ERROR_INIT_CALC= "Error instializateing calculator \"{0}\" :{1}";
	/** Constant <code>NO_PS_FOUND="Reffering PowerSupply \&quot;{0}\&quot; "{trunked}</code> */
	String NO_PS_FOUND="Reffering PowerSupply \"{0}\" was not found";
	/** Constant <code>NO_OP_FOUND="Reffering AbstractOpticalElement \&quot;{0}\&quot; "{trunked}</code> */
	String NO_OP_FOUND="Reffering AbstractOpticalElement \"{0}\" was not found";
	/** Constant <code>BPM_INSIDE="BPMonitor \&quot;{0}\&quot; is found in "{trunked}</code> */
	String BPM_INSIDE="BPMonitor \"{0}\" is found inside AbstractTransferElement \"{1}\"";
	/** Constant <code>WRONG_MARKER_POSITION="Marker \&quot;{0}\&quot; is standing be"{trunked}</code> */
	String WRONG_MARKER_POSITION="Marker \"{0}\" is standing behind element \"{1}\" with higher position";
	/** Constant <code>MARKER_NOT_INSIDE="Marker \&quot;{0}\&quot; is expected to"{trunked}</code> */
	String MARKER_NOT_INSIDE="Marker \"{0}\" is expected to be inside TrensferElement";
	/** Constant <code>TE_OVERPLACED="TrensferElement \&quot;{0}\&quot; is pl"{trunked}</code> */
	String TE_OVERPLACED="TrensferElement \"{0}\" is placed over previous element \"{1}\"";
	/** Constant <code>OVER_BEGINNING="Analizing position of \&quot;{0}\&quot;"{trunked}</code> */
	String OVER_BEGINNING="Analizing position of \"{0}\" leads over beginning";

	/** Constant <code>UPP="update preformed"</code> */
	String UPP= "update preformed";
	/** Constant <code>APP="apply preformed"</code> */
	String APP= "apply preformed";

	/** Constant <code>UP_ON_DB="Update preformed on DataBush"</code> */
	String UP_ON_DB= "Update preformed on DataBush";
	/** Constant <code>UP_ON_BPM="Update preformed on BPMonitor group"</code> */
	String UP_ON_BPM= "Update preformed on BPMonitor group";
	/** Constant <code>UP_ON_PS="Update preformed on PowerSupply group"</code> */
	String UP_ON_PS= "Update preformed on PowerSupply group";
	/** Constant <code>UP_ON_OP="Update preformed on AbstractOpticalElement grou"{trunked}</code> */
	String UP_ON_OP= "Update preformed on AbstractOpticalElement group";
	/** Constant <code>UP_ON_CMA="Update preformed on AbstractCalibratedMagnet gr"{trunked}</code> */
	String UP_ON_CMA= "Update preformed on AbstractCalibratedMagnet group";
	/** Constant <code>UP_ON_COR="Update preformed on Corrector group"</code> */
	String UP_ON_COR= "Update preformed on Corrector group";
	/** Constant <code>UP_ON_MA="Update preformed on AbstractMagnetElement group"</code> */
	String UP_ON_MA= "Update preformed on AbstractMagnetElement group";
	/** Constant <code>UP_ON_IN="Update preformed on DataBushInfo group"</code> */
	String UP_ON_IN= "Update preformed on DataBushInfo group";
	/** Constant <code>UP_ON_BE="Update preformed on Bending group"</code> */
	String UP_ON_BE= "Update preformed on Bending group";
	/** Constant <code>UP_ON_QU="Update preformed on Quadrupole group"</code> */
	String UP_ON_QU= "Update preformed on Quadrupole group";
	/** Constant <code>UP_ON_SE="Update preformed on Sextupole group"</code> */
	String UP_ON_SE= "Update preformed on Sextupole group";

	/** Constant <code>AP_ON_DB="Apply preformed on DataBush"</code> */
	String AP_ON_DB= "Apply preformed on DataBush";
	/** Constant <code>AP_ON_BE="Apply preformed on Bending group"</code> */
	String AP_ON_BE= "Apply preformed on Bending group";
	/** Constant <code>AP_ON_COR="Apply preformed on Corrector group"</code> */
	String AP_ON_COR= "Apply preformed on Corrector group";
	/** Constant <code>AP_ON_QU="Apply preformed on Quadrupole group"</code> */
	String AP_ON_QU= "Apply preformed on Quadrupole group";
	/** Constant <code>AP_ON_SE="Apply preformed on Sextupole group"</code> */
	String AP_ON_SE= "Apply preformed on Sextupole group";
	/** Constant <code>AP_ON_PS="Apply preformed on PowerSupply group"</code> */
	String AP_ON_PS= "Apply preformed on PowerSupply group";
	/** Constant <code>AP_ON_CMA="Apply preformed on AbstractCalibratedMagnet gro"{trunked}</code> */
	String AP_ON_CMA= "Apply preformed on AbstractCalibratedMagnet group";
	/** Constant <code>AP_ON_OP="Apply preformed on AbstractOpticalElement group"</code> */
	String AP_ON_OP= "Apply preformed on AbstractOpticalElement group";


	/** Constant <code>DATABUSH_NOT_INITIALIZED="DATABUSH_NOT_INITIALIZED"</code> */
	String DATABUSH_NOT_INITIALIZED= "DATABUSH_NOT_INITIALIZED";
	/** Constant <code>UNC_EX="UNCAUGHT EXCEPTION"</code> */
	String UNC_EX= "UNCAUGHT EXCEPTION";

// used strings
	/** Constant <code>IAE_WRONG_ACCESS_MODE="Wrong access mode: "</code> */
	String IAE_WRONG_ACCESS_MODE= "Wrong access mode: ";
	/** Constant <code>IAE_WRONG_UPDATE_MODE="Wrong update mode: "</code> */
	String IAE_WRONG_UPDATE_MODE= "Wrong update mode: ";

	/** Constant <code>ISE_ONDBINFO="This operation not allowed if DataBushI"{trunked}</code> */
	String ISE_ONDBINFO= "This operation not allowed if DataBushInfo is not virtual!";
	/** Constant <code>ISE_EL_NOT_INIT="Element is not initialized in DataBush!"</code> */
	String ISE_EL_NOT_INIT= "Element is not initialized in DataBush!";
	/** Constant <code>ISE_EL_INIT="Element is initialized in DataBush and "{trunked}</code> */
	String ISE_EL_INIT= "Element is initialized in DataBush and locked for this change!";
	/** Constant <code>ISE_DB_UP_MODE="DataBush is not in appropriate update/a"{trunked}</code> */
	String ISE_DB_UP_MODE= "DataBush is not in appropriate update/access mode for manual update!";
	/** Constant <code>ISE_DB_STATE="DataBush is not initialized!"</code> */
	String ISE_DB_STATE= "DataBush is not initialized!";
	/** Constant <code>ISE_CALC_MODEL="Setting CalculatorManagerModel is allow"{trunked}</code> */
	String ISE_CALC_MODEL= "Setting CalculatorManagerModel is allowed only to empty DataBush!";
	/** Constant <code>ISE_CSE="Setting IControlSystemEngine is allowed "{trunked}</code> */
	String ISE_CSE= "Setting IControlSystemEngine is allowed only to empty DataBush!";

	/** Constant <code>RC_DEVICE_NOT_CONNECTED="device is not connected"</code> */
	String RC_DEVICE_NOT_CONNECTED= "device is not connected";
	/** Constant <code>RC_INCONSISTENT_DATA_NO_SOLUTION="Input inconsistent - no optic solution."</code> */
	String RC_INCONSISTENT_DATA_NO_SOLUTION="Input inconsistent - no optic solution.";

	/** Constant <code>BEAM_CHANGE="beam position changed"</code> */
	String BEAM_CHANGE= "beam position changed";
	/** Constant <code>MACHINE_FUNCTIONS_CHANGE="machine funcions changed"</code> */
	String MACHINE_FUNCTIONS_CHANGE= "machine funcions changed";
	/** Constant <code>FIELD_CHANGE="field changed"</code> */
	String FIELD_CHANGE= "field changed";
	/** Constant <code>BENDING_FIELD_CHANGE="bending field changed"</code> */
	String BENDING_FIELD_CHANGE= "bending field changed";
	/** Constant <code>CORRECTOR_FIELD_CHANGE="corrector field changed"</code> */
	String CORRECTOR_FIELD_CHANGE= "corrector field changed";
	/** Constant <code>QUADRUPOLE_FIELD_CHANGE="quadrupole field changed"</code> */
	String QUADRUPOLE_FIELD_CHANGE= "quadrupole field changed";
	/** Constant <code>SEXTUPOLE_FIELD_CHANGE="sextupole field changed"</code> */
	String SEXTUPOLE_FIELD_CHANGE= "sextupole field changed";
	/** Constant <code>ACCESS_MODE_CHANGE="DataBush access mode changed"</code> */
	String ACCESS_MODE_CHANGE= "DataBush access mode changed";
	/** Constant <code>UPDATE_MODE_CHANGE="DataBush update mode changed"</code> */
	String UPDATE_MODE_CHANGE= "DataBush update mode changed";
	/** Constant <code>STATUS_CHANGE="DataBush status changed"</code> */
	String STATUS_CHANGE= "DataBush status changed";

	/** Constant <code>PS_TEST_FAIL="PowerSupplies failed on test"</code> */
	String PS_TEST_FAIL= "PowerSupplies failed on test";

	/** Constant <code>DEV_CONN="device connected"</code> */
	String DEV_CONN= "device connected";
	/** Constant <code>DEV_DISC="device disconnected"</code> */
	String DEV_DISC= "device disconnected";

	/** Constant <code>INCONSISTENT_DATA_IN_ACCESS_ALL="Unable to change from ACCESS_ALL_DATA b"{trunked}</code> */
	String INCONSISTENT_DATA_IN_ACCESS_ALL= "Unable to change from ACCESS_ALL_DATA becouse of inconsistend data: ";
	/** Constant <code>NOT_SUPPORTED="This exception is not supported: "</code> */
	String NOT_SUPPORTED= "This exception is not supported: ";
}
