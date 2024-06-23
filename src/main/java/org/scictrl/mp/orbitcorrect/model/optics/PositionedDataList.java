package org.scictrl.mp.orbitcorrect.model.optics;

/**
 * This list contains elements type <code>PositionedData</code>.
 *
 * @see PositionedDataList
 * @see PositionedData
 * @author igor@scictrl.com
 */
public class PositionedDataList extends AbstractProtectedList<PositionedData> {

	/**
	 * This is a default constructor for <code>PositionedDataList</code>.
	 */
	PositionedDataList(DataBushHandler t) {
		super(t);
		type=PositionedData.class;
	}
	/**
	 * This method generate and return array of <code>PositionedData</code>
	 *
	 * @return an array of {@link org.scictrl.mp.orbitcorrect.model.optics.PositionedData} objects
	 */
	public PositionedData[] toPositionedDataArray() {
		Object[] o= dl.toArray();
		PositionedData[] e= new PositionedData[o.length];
		for (int i=0; i<o.length;i++) e[i]=(PositionedData)o[i];
		return e;
	}
}
