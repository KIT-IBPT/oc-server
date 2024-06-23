package org.scictrl.mp.orbitcorrect.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.scictrl.mp.orbitcorrect.math.DoubleMatrix;
import org.scictrl.mp.orbitcorrect.math.DriftMatrix;
import org.scictrl.mp.orbitcorrect.math.TransferMatrix;

/**
 * <p>
 * MathTest class.
 * </p>
 *
 * @author igor@scictrl.com
 */
public class MathTest {

	/**
	 * Constructor.
	 */
	public MathTest() {
	}

	/**
	 * <p>
	 * testTransferMatrixMultiply.
	 * </p>
	 */
	@Test
	public void testTransferMatrixMultiply() {

		TransferMatrix tm1 = new TransferMatrix(1);
		TransferMatrix tm2 = new TransferMatrix(2);

		TransferMatrix tm3 = tm1.multiplay(tm2);

		assertNotNull(tm3);
		assertEquals(tm1.getLength() + tm2.getLength(), tm3.getLength(), 0.000001);

		DriftMatrix dm1 = new DriftMatrix(1);
		DriftMatrix dm2 = new DriftMatrix(2);

		// DoubleMatrix dbm1=dm1;
		DoubleMatrix dbm2 = dm2;

		DoubleMatrix dbm3 = dm1.multiplay(dbm2);

		assertNotNull(dbm3);
		assertTrue(dbm3 instanceof DriftMatrix);
		assertEquals(dm1.getLength() + dm2.getLength(), ((DriftMatrix) dbm3).getLength(), 0.000001);

		tm1 = dm1;
		tm2 = dm2;

		tm3 = tm1.multiplay(tm2);

		assertNotNull(tm3);
		assertTrue(tm3 instanceof DriftMatrix);
		assertEquals(tm1.getLength() + tm2.getLength(), tm3.getLength(), 0.000001);

		tm3 = tm1.multiplay(dm2);

		assertNotNull(tm3);
		assertTrue(tm3 instanceof TransferMatrix);
		assertEquals(tm1.getLength() + dm2.getLength(), tm3.getLength(), 0.000001);

	}

}
