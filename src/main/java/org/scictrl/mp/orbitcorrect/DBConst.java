package org.scictrl.mp.orbitcorrect;

import java.text.DecimalFormat;

import org.scictrl.mp.orbitcorrect.model.optics.DataBush;

/**
 * This class holds DataBush constants.
 *
 * @author igor@scictrl.com
 */
public interface DBConst {
	/**
	 * Update mode code for manual update. DataBush updates only when update mode is called.
	 */
	int	UPDATE_MANUAL= 0;
	/**
	 * Update mode code for automatic update. DataBush updates when recive propery change event.
	 */
	int UPDATE_ON_EVENT= 1;

	/**
	 * Return code for DataBush actions (update, apply, connect, disconnect).
	 */
	int RC_COMPLETED_SUCCESSFULLY=0;
	/**
	 * Return code for DataBush actions (update, apply, connect, disconnect).
	 */
	int RC_IS_VIRTUAL= 1;
	/**
	 * Return code for DataBush actions (update, apply, connect, disconnect).
	 */
	int RC_NO_CHANGES= 2;
	/**
	 * Return code for DataBush actions (update, apply, connect, disconnect).
	 */
	int RC_DEVICE_NOT_CONNECTED= 3;
	/**
	 * Return code for DataBush actions (update, apply, connect, disconnect).
	 */
	int RC_REMOTE_EXCEPTION= 4;
	/**
	 * Return code for DataBush actions (update, apply, connect, disconnect).
	 */
	int RC_BIND_FAILED= 5;
	/**
	 * Return code for DataBush actions (update, apply, connect, disconnect).
	 */
	int RC_ABORTED= 9;
	/**
	 * Return code for DataBush actions (update, apply, connect, disconnect).
	 */
	int RC_TEST_FAILED= 8;
	/**
	 * Return code for DataBush actions (update, apply, connect, disconnect).
	 */
	int RC_NOT_INITIALIZED= 7;
	/**
	 * Return code for DataBush actions (update, apply, connect, disconnect).
	 */
	int RC_ILLEGAL_STATE= 8;
	/**
	 * Return code for DataBush actions (update, apply, connect, disconnect).
	 */
	int RC_INCONSISTANT_DATA= 9;

	/**
	 * Status code of DataBush, when DataBush is empty.
	 */
	int DB_EMPTY= 1;
	/**
	 * Status code of DataBush, when DataBush is succesfully initialized with elements.
	 */
	int DB_OPERATIONAL= 2;

	/**
	 * Row index for point positions.
	 * @see DataBush#getMachineFunctions(double[][], org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, int)
	 * @see DataBush#getMachineFunctions(java.util.List[], org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, int)
	 */
	int MF_POSITION= 0;
	/**
	 * Row index for beta function's phase/2 PI on x axis.
	 * @see DataBush#getMachineFunctions(double[][], org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, int)
	 * @see DataBush#getMachineFunctions(java.util.List[], org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, int)
	 */
	int MF_Q_X= 1;
	/**
	 * Row index for beta function's phase/2 PI on z axis.
	 * @see DataBush#getMachineFunctions(double[][], org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, int)
	 * @see DataBush#getMachineFunctions(java.util.List[], org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, int)
	 */
	int MF_Q_Z= 2;
	/**
	 * Row index for beta function values on x axis.
	 * @see DataBush#getMachineFunctions(double[][], org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, int)
	 * @see DataBush#getMachineFunctions(java.util.List[], org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, int)
	 */
	int MF_BETA_X= 3;
	/**
	 * Row index for beta function values on z axis.
	 * @see DataBush#getMachineFunctions(double[][], org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, int)
	 * @see DataBush#getMachineFunctions(java.util.List[], org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, int)
	 */
	int MF_BETA_Z= 4;
	/**
	 * Row index for alpha function values on x axis.
	 * @see DataBush#getMachineFunctions(double[][], org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, int)
	 * @see DataBush#getMachineFunctions(java.util.List[], org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, int)
	 */
	int MF_ALPHA_X= 5;
	/**
	 * Row index for alpha function values on z axis.
	 * @see DataBush#getMachineFunctions(double[][], org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, int)
	 * @see DataBush#getMachineFunctions(java.util.List[], org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, int)
	 */
	int MF_ALPHA_Z= 6;
	/**
	 * Row index for dispersion function values.
	 * @see DataBush#getMachineFunctions(double[][], org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, int)
	 * @see DataBush#getMachineFunctions(java.util.List[], org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, int)
	 */
	int MF_D= 7;
	/**
	 * Row index for dispersion function derivative values.
	 * @see DataBush#getMachineFunctions(double[][], org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, int)
	 * @see DataBush#getMachineFunctions(java.util.List[], org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, org.scictrl.mp.orbitcorrect.model.optics.AbstractOpticalElement, int)
	 */
	int MF_DP= 8;

	/**
	 * Array holds string descriptions for DataBush status codes.
	 */
	String[] DB_STATUS_STRINGS=
	{"",
	 "DB_EMPTY",
	 "DB_OPERATIONAL"};

	/**
	 * Array holds string descriptions for DataBush update codes.
	 */
	String[] UPDATE_MODE_STRINGS=
	{"UPDATE_MANUAL",
	 "UPDATE_ON_EVENT"};

	/**
	 * Array holds string descriptions for DataBush access codes.
	 */
	String[] ACCESS_MODE_STRINGS=
	{"ACCESS_VARIABLE_DATA",
	 "ACCESS_ALL_DATA"};

	/**
	 * Array holds string descriptions for DataBush return codes.
	 */
	String[] RETURN_CODE_STRINGS=
	{"COMPLETED_SUCCESSFULLY",
	 "IS_VIRTUAL",
	 "NO_CHANGES",
	 "IS_LOCKED",
	 "DEVICE_BEAN_EXISTS",
	 "DEVICE_BEAN_DOES_NOT_EXISTS",
	 "R_DEVICE_NOT_CONNECTED",
	 "DATABUSH_NOT_INITIALIZED",
	 "TEST_FAILED",
	 "ABORTED"};

	/** Constant <code>EOL="\r\n"</code> */
	String EOL="\r\n";

	/** Constant <code>ISO_TIME_FORMAT="YYYY-mm-dd'T'HH:mm:ss.SSS"</code> */
	String ISO_TIME_FORMAT="YYYY-mm-dd'T'HH:mm:ss.SSS";

	/** Constant <code>FORMAT_F4</code> */
	DecimalFormat FORMAT_F4= new DecimalFormat("0.0000");
}
