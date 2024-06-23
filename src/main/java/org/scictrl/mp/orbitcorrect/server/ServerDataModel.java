/**
 *
 */
package org.scictrl.mp.orbitcorrect.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.scictrl.mp.orbitcorrect.IOrientationMarker;
import org.scictrl.mp.orbitcorrect.Orientation;
import org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionModel;
import org.scictrl.mp.orbitcorrect.model.optics.AbstractCorrector;
import org.scictrl.mp.orbitcorrect.model.optics.BPMonitor;
import org.scictrl.mp.orbitcorrect.model.optics.ElementList;
import org.scictrl.mp.orbitcorrect.mvc.ApplicationModel;


/**
 * Holds references to all data entities and value collections.
 * These are main data with which orbit correction operates.
 * Values here must be managed by OrbitCorrectionApplication.
 *
 * @author igor@scictrl.com
 */
public class ServerDataModel extends ApplicationModel {

	// static names for some of JavaBean bound properties
	/** Constant <code>AVAILABLE_CORRECTION_MODELS_H="availableCorrectionModelsH"</code> */
	public static final String AVAILABLE_CORRECTION_MODELS_H = "availableCorrectionModelsH";
	/** Constant <code>AVAILABLE_CORRECTION_MODELS_V="availableCorrectionModelsV"</code> */
	public static final String AVAILABLE_CORRECTION_MODELS_V = "availableCorrectionModelsV";
	/** Constant <code>CORRECTION_MODEL_V="correctionModelV"</code> */
	public static final String CORRECTION_MODEL_V = "correctionModelV";
	/** Constant <code>CORRECTION_MODEL_H="correctionModelH"</code> */
	public static final String CORRECTION_MODEL_H = "correctionModelH";
	/** Constant <code>AVAILABLE_BPMS_H="availableBpmsH"</code> */
	public static final String AVAILABLE_BPMS_H = "availableBpmsH";
	/** Constant <code>AVAILABLE_BPMS_V="availableBpmsV"</code> */
	public static final String AVAILABLE_BPMS_V = "availableBpmsV";
	/** Constant <code>AVAILABLE_CORRECTORS_H="availableCorrectorsH"</code> */
	public static final String AVAILABLE_CORRECTORS_H = "availableCorrectorsH";
	/** Constant <code>AVAILABLE_CORRECTORS_V="availableCorrectorsV"</code> */
	public static final String AVAILABLE_CORRECTORS_V = "availableCorrectorsV";
	/** Constant <code>SELECTED_BPMS_H="selectedBpmsH"</code> */
	public static final String SELECTED_BPMS_H = "selectedBpmsH";
	/** Constant <code>SELECTED_BPMS_V="selectedBpmsV"</code> */
	public static final String SELECTED_BPMS_V = "selectedBpmsV";
	/** Constant <code>SELECTED_CORRECTORS_H="selectedCorrectorsH"</code> */
	public static final String SELECTED_CORRECTORS_H = "selectedCorrectorsH";
	/** Constant <code>SELECTED_CORRECTORS_V="selectedCorrectorsV"</code> */
	public static final String SELECTED_CORRECTORS_V = "selectedCorrectorsV";
	/** Constant <code>BPMS_H_SELECTION="bpmsHSelection"</code> */
	public static final String BPMS_H_SELECTION = "bpmsHSelection";
	/** Constant <code>BPMS_V_SELECTION="bpmsVSelection"</code> */
	public static final String BPMS_V_SELECTION = "bpmsVSelection";
	/** Constant <code>CORRECTORS_H_SELECTION="correctorsHSelection"</code> */
	public static final String CORRECTORS_H_SELECTION = "correctorsHSelection";
	/** Constant <code>CORRECTORS_V_SELECTION="correctorsVSelection"</code> */
	public static final String CORRECTORS_V_SELECTION = "correctorsVSelection";

	// data fields
	private List<IOrbitCorrectionModel> availableCorrectionModelsH=new ArrayList<>();
	private List<IOrbitCorrectionModel> availableCorrectionModelsV=new ArrayList<>();
	private IOrbitCorrectionModel correctionModelV;
	private IOrbitCorrectionModel correctionModelH;
	private ElementList<BPMonitor> availableBpmsH;
	private ElementList<BPMonitor> availableBpmsV;
	private ElementList<AbstractCorrector> availableCorrectorsH;
	private ElementList<AbstractCorrector> availableCorrectorsV;
	private ElementList<BPMonitor> selectedBpmsH;
	private ElementList<BPMonitor> selectedBpmsV;
	private ElementList<AbstractCorrector> selectedCorrectorsH;
	private ElementList<AbstractCorrector> selectedCorrectorsV;
	private boolean[] bpmsHSelection;
	private boolean[] bpmsVSelection;
	private boolean[] correctorsHSelection;
	private boolean[] correctorsVSelection;

	private Set<String> setting=new HashSet<>(8);


	/**
	 * <p>Constructor for ServerDataModel.</p>
	 */
	public ServerDataModel(){}


	/**
	 * <p>Getter for the field <code>availableCorrectionModelsH</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<IOrbitCorrectionModel> getAvailableCorrectionModelsH() {
		return availableCorrectionModelsH;
	}

	/**
	 * <p>Setter for the field <code>availableCorrectionModelsH</code>.</p>
	 *
	 * @param availableCorrectionModelsH a {@link java.util.List} object
	 */
	public void setAvailableCorrectionModelsH(
			List<IOrbitCorrectionModel> availableCorrectionModelsH) {
		handlePropertySet(AVAILABLE_CORRECTION_MODELS_H, availableCorrectionModelsH);
		if (!setting.contains(AVAILABLE_CORRECTION_MODELS_H) && getCorrectionModelH()==null && availableCorrectionModelsH!=null && availableCorrectionModelsH.size()>0) {
			setting.add(AVAILABLE_CORRECTION_MODELS_H);
			setCorrectionModelH(availableCorrectionModelsH.get(0));
			setting.remove(AVAILABLE_CORRECTION_MODELS_H);
		}
	}

	/**
	 * <p>Getter for the field <code>availableCorrectionModelsV</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<IOrbitCorrectionModel> getAvailableCorrectionModelsV() {
		return availableCorrectionModelsV;
	}

	/**
	 * <p>Setter for the field <code>availableCorrectionModelsV</code>.</p>
	 *
	 * @param availableCorrectionModelsV a {@link java.util.List} object
	 */
	public void setAvailableCorrectionModelsV(
			List<IOrbitCorrectionModel> availableCorrectionModelsV) {
		handlePropertySet(AVAILABLE_CORRECTION_MODELS_V, availableCorrectionModelsV);
		if (!setting.contains(AVAILABLE_CORRECTION_MODELS_V) && getCorrectionModelV()==null && availableCorrectionModelsV!=null && availableCorrectionModelsV.size()>0) {
			setting.add(AVAILABLE_CORRECTION_MODELS_V);
			setCorrectionModelV(availableCorrectionModelsV.get(0));
			setting.remove(AVAILABLE_CORRECTION_MODELS_V);
		}
	}

	/**
	 * <p>addAvailableCorrectionModel.</p>
	 *
	 * @param model a {@link org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionModel} object
	 */
	public void addAvailableCorrectionModel(IOrbitCorrectionModel model) {
		if (model.isHorizontal()) {
			List<IOrbitCorrectionModel> l= new ArrayList<>(getAvailableCorrectionModelsH());
			l.add(model);
			if (!setting.contains(AVAILABLE_CORRECTION_MODELS_H) && getCorrectionModelH()==null) {
				setting.add(AVAILABLE_CORRECTION_MODELS_H);
				setCorrectionModelH(model);
				setting.remove(AVAILABLE_CORRECTION_MODELS_H);
			}
			setAvailableCorrectionModelsH(l);
		} else {
			List<IOrbitCorrectionModel> l= new ArrayList<>(getAvailableCorrectionModelsV());
			l.add(model);
			if (!setting.contains(AVAILABLE_CORRECTION_MODELS_V) && getCorrectionModelV()==null) {
				setting.add(AVAILABLE_CORRECTION_MODELS_V);
				setCorrectionModelV(model);
				setting.remove(AVAILABLE_CORRECTION_MODELS_V);
			}
			setAvailableCorrectionModelsV(l);
		}
	}

	/**
	 * <p>getAvailableCorrectionModels.</p>
	 *
	 * @param o a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @return a {@link java.util.List} object
	 */
	public List<IOrbitCorrectionModel> getAvailableCorrectionModels(Orientation o) {
		return o.isHorizontal() ? availableCorrectionModelsH : availableCorrectionModelsV;
	}

	/**
	 * <p>getAvailableCorrectionModels.</p>
	 *
	 * @param om a {@link org.scictrl.mp.orbitcorrect.IOrientationMarker} object
	 * @return a {@link java.util.List} object
	 */
	public List<IOrbitCorrectionModel> getAvailableCorrectionModels(IOrientationMarker om) {
		return getAvailableCorrectionModels(om.getOrientation());
	}

	/**
	 * <p>setCorrectionModel.</p>
	 *
	 * @param correctionModel a {@link org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionModel} object
	 */
	public void setCorrectionModel(
			IOrbitCorrectionModel correctionModel) {
		if (correctionModel.isHorizontal()) {
			setCorrectionModelH(correctionModel);
		} else {
			setCorrectionModelV(correctionModel);
		}
	}
	/**
	 * <p>Setter for the field <code>correctionModelH</code>.</p>
	 *
	 * @param correctionModel a {@link org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionModel} object
	 */
	public void setCorrectionModelH(
			IOrbitCorrectionModel correctionModel) {
		if (!correctionModel.isHorizontal()) {
			throw new IllegalArgumentException("Not H");
		}
		if (!setting.contains(CORRECTION_MODEL_H) && !availableCorrectionModelsH.contains(correctionModel)) {
			setting.add(CORRECTION_MODEL_H);
			addAvailableCorrectionModel(correctionModel);
			setting.remove(CORRECTION_MODEL_H);
		}
		handlePropertySet(CORRECTION_MODEL_H, correctionModel);
	}

	/**
	 * <p>Getter for the field <code>correctionModelH</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionModel} object
	 */
	public IOrbitCorrectionModel getCorrectionModelH() {
		return correctionModelH;
	}

	/**
	 * <p>Setter for the field <code>correctionModelV</code>.</p>
	 *
	 * @param correctionModel a {@link org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionModel} object
	 */
	public void setCorrectionModelV(
			IOrbitCorrectionModel correctionModel) {
		if (!correctionModel.isVertical()) {
			throw new IllegalArgumentException("Not V");
		}
		if (!setting.contains(CORRECTION_MODEL_V) && !availableCorrectionModelsV.contains(correctionModel)) {
			setting.add(CORRECTION_MODEL_V);
			addAvailableCorrectionModel(correctionModel);
			setting.remove(CORRECTION_MODEL_V);
		}
		handlePropertySet(CORRECTION_MODEL_V, correctionModel);
	}

	/**
	 * <p>Getter for the field <code>correctionModelV</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.correction.IOrbitCorrectionModel} object
	 */
	public IOrbitCorrectionModel getCorrectionModelV() {
		return correctionModelV;
	}

	/**
	 * <p>Getter for the field <code>availableBpmsH</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public ElementList<BPMonitor> getAvailableBpmsH() {
		return availableBpmsH;
	}

	/**
	 * <p>Getter for the field <code>availableBpmsV</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public ElementList<BPMonitor> getAvailableBpmsV() {
		return availableBpmsV;
	}

	/**
	 * <p>Getter for the field <code>availableCorrectorsH</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public ElementList<AbstractCorrector> getAvailableCorrectorsH() {
		return availableCorrectorsH;
	}

	/**
	 * <p>Getter for the field <code>availableCorrectorsV</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public ElementList<AbstractCorrector> getAvailableCorrectorsV() {
		return availableCorrectorsV;
	}

	/**
	 * <p>Getter for the field <code>selectedBpmsH</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public ElementList<BPMonitor> getSelectedBpmsH() {
		return selectedBpmsH;
	}

	/**
	 * <p>Getter for the field <code>selectedBpmsV</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public ElementList<BPMonitor> getSelectedBpmsV() {
		return selectedBpmsV;
	}

	/**
	 * <p>Getter for the field <code>selectedCorrectorsH</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public ElementList<AbstractCorrector> getSelectedCorrectorsH() {
		return selectedCorrectorsH;
	}

	/**
	 * <p>Getter for the field <code>selectedCorrectorsV</code>.</p>
	 *
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public ElementList<AbstractCorrector> getSelectedCorrectorsV() {
		return selectedCorrectorsV;
	}

	/**
	 * <p>Getter for the field <code>bpmsHSelection</code>.</p>
	 *
	 * @return an array of {@link boolean} objects
	 */
	public boolean[] getBpmsHSelection() {
		return bpmsHSelection;
	}

	/**
	 * <p>Getter for the field <code>bpmsVSelection</code>.</p>
	 *
	 * @return an array of {@link boolean} objects
	 */
	public boolean[] getBpmsVSelection() {
		return bpmsVSelection;
	}

	/**
	 * <p>Getter for the field <code>correctorsHSelection</code>.</p>
	 *
	 * @return an array of {@link boolean} objects
	 */
	public boolean[] getCorrectorsHSelection() {
		return correctorsHSelection;
	}

	/**
	 * <p>Getter for the field <code>correctorsVSelection</code>.</p>
	 *
	 * @return an array of {@link boolean} objects
	 */
	public boolean[] getCorrectorsVSelection() {
		return correctorsVSelection;
	}

	/**
	 * <p>getAvailableBPMs.</p>
	 *
	 * @param orinetation a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public ElementList<BPMonitor> getAvailableBPMs(Orientation orinetation) {
		return orinetation.isHorizontal() ? availableBpmsH : availableBpmsV;
	}

	/**
	 * <p>getSelectedBPMs.</p>
	 *
	 * @param orinetation a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public ElementList<BPMonitor> getSelectedBPMs(Orientation orinetation) {
		return orinetation.isHorizontal() ? selectedBpmsH : selectedBpmsV;
	}

	/**
	 * <p>getBPMsSelection.</p>
	 *
	 * @param orinetation a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @return an array of {@link boolean} objects
	 */
	public boolean[] getBPMsSelection(Orientation orinetation) {
		return orinetation.isHorizontal() ? bpmsHSelection : bpmsVSelection;
	}

	/**
	 * <p>getAvailableCorrectors.</p>
	 *
	 * @param orientation a {@link org.scictrl.mp.orbitcorrect.Orientation} object
	 * @return a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public ElementList<AbstractCorrector> getAvailableCorrectors(Orientation orientation) {
		return orientation.isHorizontal() ? availableCorrectorsH : availableCorrectorsV;
	}

	/**
	 * <p>Setter for the field <code>bpmsHSelection</code>.</p>
	 *
	 * @param bpmsHSelection an array of {@link boolean} objects
	 */
	public void setBpmsHSelection(boolean[] bpmsHSelection) {
		if (setting.contains(BPMS_H_SELECTION)) {
			return;
		}
		ElementList<BPMonitor> sel= new ElementList<>(bpmsHSelection.length);
		for (int i = 0; i < bpmsHSelection.length; i++) {
			if (bpmsHSelection[i]) {
				sel.add(availableBpmsH.get(i));
			}
		}
		setSelectedBpmsH(sel);
	}

	/**
	 * <p>Setter for the field <code>selectedBpmsH</code>.</p>
	 *
	 * @param selectedBpmsH a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public void setSelectedBpmsH(ElementList<BPMonitor> selectedBpmsH) {
		if (setting.contains(SELECTED_BPMS_H) || (this.selectedBpmsH!=null && this.selectedBpmsH.size()-selectedBpmsH.size()>2)) {
			return;
		}
		setting.add(SELECTED_BPMS_H);
		handlePropertySet(SELECTED_BPMS_H, selectedBpmsH);
		boolean[] b= new boolean[availableBpmsH.size()];
		for (int i = 0; i < b.length; i++) {
			b[i]=selectedBpmsH.contains(availableBpmsH.get(i));
		}
		handlePropertySet(BPMS_H_SELECTION, b);
		setting.remove(SELECTED_BPMS_H);
	}

	/**
	 * <p>Setter for the field <code>availableBpmsH</code>.</p>
	 *
	 * @param availableBpmsH a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public void setAvailableBpmsH(ElementList<BPMonitor> availableBpmsH) {
		handlePropertySet(AVAILABLE_BPMS_H, availableBpmsH);
		boolean[] b= new boolean[availableBpmsH.size()];
		Arrays.fill(b, true);
		setBpmsHSelection(b);
	}

	/**
	 * <p>Setter for the field <code>bpmsVSelection</code>.</p>
	 *
	 * @param bpmsVSelection an array of {@link boolean} objects
	 */
	public void setBpmsVSelection(boolean[] bpmsVSelection) {
		if (setting.contains(BPMS_V_SELECTION)) {
			return;
		}
		ElementList<BPMonitor> sel= new ElementList<>(bpmsVSelection.length);
		for (int i = 0; i < bpmsVSelection.length; i++) {
			if (bpmsVSelection[i]) {
				sel.add(availableBpmsV.get(i));
			}
		}
		setSelectedBpmsV(sel);
	}

	/**
	 * <p>Setter for the field <code>selectedBpmsV</code>.</p>
	 *
	 * @param selectedBpmsV a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public void setSelectedBpmsV(ElementList<BPMonitor> selectedBpmsV) {
		if (setting.contains(SELECTED_BPMS_V) || (this.selectedBpmsV!=null && this.selectedBpmsV.size()-selectedBpmsV.size()>2)) {
			return;
		}
		setting.add(SELECTED_BPMS_V);
		handlePropertySet(SELECTED_BPMS_V, selectedBpmsV);
		boolean[] b= new boolean[availableBpmsV.size()];
		for (int i = 0; i < b.length; i++) {
			b[i]=selectedBpmsV.contains(availableBpmsV.get(i));
		}
		handlePropertySet(BPMS_V_SELECTION, b);
		setting.remove(SELECTED_BPMS_V);
	}

	/**
	 * <p>Setter for the field <code>availableBpmsV</code>.</p>
	 *
	 * @param availableBpmsV a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public void setAvailableBpmsV(ElementList<BPMonitor> availableBpmsV) {
		handlePropertySet(AVAILABLE_BPMS_V, availableBpmsV);
		boolean[] b= new boolean[availableBpmsV.size()];
		Arrays.fill(b, true);
		setBpmsVSelection(b);
	}

	/**
	 * <p>Setter for the field <code>correctorsHSelection</code>.</p>
	 *
	 * @param correctorsHSelection an array of {@link boolean} objects
	 */
	public void setCorrectorsHSelection(boolean[] correctorsHSelection) {
		if (setting.contains(CORRECTORS_H_SELECTION)) {
			return;
		}
		ElementList<AbstractCorrector> sel= new ElementList<>(correctorsHSelection.length);
		for (int i = 0; i < correctorsHSelection.length; i++) {
			if (correctorsHSelection[i]) {
				sel.add(availableCorrectorsH.get(i));
			}
		}
		setSelectedCorrectorsH(sel);
	}

	/**
	 * <p>Setter for the field <code>selectedCorrectorsH</code>.</p>
	 *
	 * @param selectedCorrectorsH a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public void setSelectedCorrectorsH(ElementList<AbstractCorrector> selectedCorrectorsH) {
		if (setting.contains(SELECTED_CORRECTORS_H) || (this.selectedCorrectorsH!=null && this.selectedCorrectorsH.size()-selectedCorrectorsH.size()>2)) {
			return;
		}
		setting.add(SELECTED_CORRECTORS_H);
		handlePropertySet(SELECTED_CORRECTORS_H, selectedCorrectorsH);
		boolean[] b= new boolean[availableCorrectorsH.size()];
		for (int i = 0; i < b.length; i++) {
			b[i]=selectedCorrectorsH.contains(availableCorrectorsH.get(i));
		}
		handlePropertySet(CORRECTORS_H_SELECTION, b);
		setting.remove(SELECTED_CORRECTORS_H);
	}

	/**
	 * <p>Setter for the field <code>availableCorrectorsH</code>.</p>
	 *
	 * @param availableCorrectorsH a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public void setAvailableCorrectorsH(ElementList<AbstractCorrector> availableCorrectorsH) {
		handlePropertySet(AVAILABLE_CORRECTORS_H, availableCorrectorsH);
		boolean[] b= new boolean[availableCorrectorsH.size()];
		Arrays.fill(b, true);
		setCorrectorsHSelection(b);
	}

	/**
	 * <p>Setter for the field <code>correctorsVSelection</code>.</p>
	 *
	 * @param correctorsVSelection an array of {@link boolean} objects
	 */
	public void setCorrectorsVSelection(boolean[] correctorsVSelection) {
		if (setting.contains(CORRECTORS_V_SELECTION)) {
			return;
		}
		ElementList<AbstractCorrector> sel= new ElementList<>(correctorsVSelection.length);
		for (int i = 0; i < correctorsVSelection.length; i++) {
			if (correctorsVSelection[i]) {
				sel.add(availableCorrectorsV.get(i));
			}
		}
		setSelectedCorrectorsV(sel);
	}

	/**
	 * <p>Setter for the field <code>selectedCorrectorsV</code>.</p>
	 *
	 * @param selectedCorrectorsV a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public void setSelectedCorrectorsV(ElementList<AbstractCorrector> selectedCorrectorsV) {
		if (setting.contains(SELECTED_CORRECTORS_V) || (this.selectedCorrectorsV!=null && this.selectedCorrectorsV.size()-selectedCorrectorsV.size()>2)) {
			return;
		}
		setting.add(SELECTED_CORRECTORS_V);
		handlePropertySet(SELECTED_CORRECTORS_V, selectedCorrectorsV);
		boolean[] b= new boolean[availableCorrectorsV.size()];
		for (int i = 0; i < b.length; i++) {
			b[i]=selectedCorrectorsV.contains(availableCorrectorsV.get(i));
		}
		handlePropertySet(CORRECTORS_V_SELECTION, b);
		setting.remove(SELECTED_CORRECTORS_V);
	}

	/**
	 * <p>Setter for the field <code>availableCorrectorsV</code>.</p>
	 *
	 * @param availableCorrectorsV a {@link org.scictrl.mp.orbitcorrect.model.optics.ElementList} object
	 */
	public void setAvailableCorrectorsV(ElementList<AbstractCorrector> availableCorrectorsV) {
		handlePropertySet(AVAILABLE_CORRECTORS_V, availableCorrectorsV);
		boolean[] b= new boolean[availableCorrectorsV.size()];
		Arrays.fill(b, true);
		setCorrectorsVSelection(b);
	}

}
