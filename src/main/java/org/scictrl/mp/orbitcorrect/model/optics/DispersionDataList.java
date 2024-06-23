package org.scictrl.mp.orbitcorrect.model.optics;

/**
 * This list contains elements type <code>DispersionData</code>.
 *
 * @see DispersionDataList
 * @see DispersionData
 * @author igor@scictrl.com
 */
public class DispersionDataList extends AbstractProtectedList<DispersionData> {

	/**
	 * This is a default constructor for <code>DispersionDataList</code>.
	 */
	DispersionDataList(DataBushHandler t) {
		super(t);
		type= DispersionData.class;
	}
	/**
	 * This method generate and return array of <code>DispersionData</code>
	 *
	 * @return an array of {@link org.scictrl.mp.orbitcorrect.model.optics.DispersionData} objects
	 */
	public DispersionData[] toDispersionDataArray() {
		Object[] o= dl.toArray();
		DispersionData[] e= new DispersionData[o.length];
		for (int i=0; i<o.length;i++) e[i]=(DispersionData)o[i];
		return e;
	}
}
