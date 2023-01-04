/*
 * File: $HeadURL:$ 
 * Last Updated On: $Date:$ 
 * Last Updated By: $Author: $ 
 * 
 * (c) 2014-2015 Customer Care Inc., L.P. All rights reserved. 
 *
 */
package com.cci.fs.att.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;

import com.cci.fs.utils.ByteUtils;
import com.google.polo.pairing.HexDump;

/**
 * The class <code>PcmdEventRecordTest</code> contains tests for the class
 * <code>{@link PcmdEventRecord}</code>.
 *
 * @author CCI
 * @version $Rev:$
 */
public class PcmdDataTest {
	
	/** The Constant HEX_CONN_ID. */
	public static final String HEX_CONN_ID = "311180fffaf9";
	
	/** The Constant SEQ_ID. */
	public static final int SEQ_ID = 1;
	
	/** The Constant VERSION. */
	public static final int VERSION = 8;
	
	/** The Constant CELL_IDS. */
	private static final String[] CELL_IDS = { "2a70b0f", "b0e8f10" };
	
	/** The wrap factory. */
	private final StemWrapperFactory wrapFactory = new StemWrapperFactory(
			CtrEvent.CTR_DST_PORT, CtumEvent.CTUM_DST_PORT,
			PcmdRecord.PCMD_DST_PORT);

	// modified old PCMD records to create sample data for new interface
	// agreement
	/** The Constant PCMD_TEST_DATA. */
	private static final String[] PCMD_TEST_DATA = {
			"8;91827afd;310:410:ff28:01;-05:00;2014:06:03;71992968;1;1;1;7446;1:100:;35713005:257957:03;17404187616;310410658426276;e5025a42;310:410:2a70b0f;198.228.228.236;;;::;310:410:2a70b0f;1;1,2,14,64,154,4,54,55,77,78,41,42,59,60,81,82,28,29,57,56,40,36,37,,,53,1:100:,,::,,,,2,;;1;;;;;;;2;;;;;;;0;,72000082,,8,162,5,::,,,198.228.228.236,phone.mnc410.mcc310.gprs,0;;;10.189.210.86,,,,,",
			"8;058330c7;310:410:ff28:01;-05:00;2014:06:03;71999851;1;1;1;697;1:100:;35655405:139405:04;12317509565;310410617497046;c1103203;310:410:b0e8f10;166.137.101.206;;;::;310:410:b0e8f10;1,1,2,12,62,206,77,41,78,42,59,60,81,82,28,29,57,56,40,36,37,,,,,,53,1:100:,,::,,,,2,;;1;;;;;;;2;;;;;;;0;,72000251,,8,168,5,::,,,166.137.101.206,phone.mnc410.mcc310.gprs,0;;;10.101.128.67,,,,," };

	private static final String PCMD_TEST_DATA_HEX = "311180ffe8f600000007c6e6383b30363031363363393b3331313a3138303a666665383a66363b2d30373a30303b30383a31303b323031353b31313b36393330303537373b31373b353b3135333b313b3130333b3132313b3b30313334333230303a3735303738343a30363b31323135383432303138313b3331313138303030303039323437373b63323430353638303b36393330303538313b3331313a3138303a663639393430323b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3135352e3136352e39382e34313b3b3b3331313a3138303a663639393430323b3b3b3b3b3b3b3b3b3b313b3b333b313b32303b3133333b3133343b36313b35383b36323b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3131393532393b3b31363b36393238393639313b36393330303732383b313b3130323b3b32343b31303b353b333b313b37333b31383b363b303b303b3b3b3b3b3b3b3b3b3b3b3b303b31313b343235373b303b313b303b333b313b303b303b313b3430343b373b353739343b34353b303b2d30373a30303b3b3b3331313a3138303a663639393430323b3b333b3b343b3b3b3b3b36393238393638363b333b373b333537313b303b303b303b303b303b3b3b3b323b3b36393330303538343b3b3b343b3b3b3b323b323b363b3b3b303b3b3b303b3b3b3b313b3b3b3b3b3b3b3b3b3b3b3b3b3b3b303b3b313b3b3b33323736343b3b3b353b303b303b303b303b303b303b303b303b303b3b303b38323b39303b3238363b343b343b303b31303939353b343138383b303b313b303b313b3b3b3b333b3b3b3b3b3b3b3b3b3b3b3b3237323633363b313739323b3b3b3b3b3b3b36393238393635353b36393238393639313b36393238393733333b3b3b3b3b3b3b3b3b3b3b3b303b32343b3b3b3b3b3b3b3b3b3b3b3b303b313b323630363a616530303a323034313a3730303a3a31343b6e787467656e70686f6e652d32632d312e6d6e633138302e6d63633331312e677072733b3b3b323b383b3b353b3b3b3b3b3b3b303b31323b303b303b303b343b323b343b303b3b3b3b3b3b3b3b3b3b303b3b3b31302e3136382e302e3133323b31323b313b32";
	private static final int ENBID = 1010068;
	private static final int SEQID = 509670;
	/**
	 * Run the PcmdEventRecord class tests.
	 */
	@Test
	public void testPcmdEventRecord_1() {
		final byte[] bytes = createPcmdRecordNew(HEX_CONN_ID, SEQ_ID, 0);
		
		final StemPcmdWrapper wrap = new StemPcmdWrapper(bytes);

		assertEquals(HEX_CONN_ID, wrap.getHexConnectionId());

		assertEquals(SEQ_ID, wrap.getSequence());
		
		final PcmdRecord fixture = new PcmdRecord(bytes);
		assertNotNull(fixture);
		
		final int version = fixture.getVersion();
		assertEquals(VERSION, version);

		final int enbid = fixture.getEnodebId();

		assertEquals(Integer.parseInt(CELL_IDS[0].substring(0, 5), 16), enbid);

		final int mseg1 = fixture.getMarketSegment();
		assertEquals(enbid / 10000, mseg1);
		
	}

	/**
	 * Test pcmd event record_2.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testPcmdEventRecord_2() {
		final byte[] bytes = createPcmdRecordNew(HEX_CONN_ID, SEQ_ID, 1);

		final StemWrapper wrap = wrapFactory.createStemWrapper(bytes,
				PcmdRecord.PCMD_DST_PORT);

		assertEquals(HEX_CONN_ID, wrap.getHexConnectionId());

		assertEquals(SEQ_ID, wrap.getSequence());

		final PcmdRecord fixture = new PcmdRecord(bytes, PcmdRecord.PCMD_GCELLID_IND);
		assertNotNull(fixture);

		final int version = fixture.getVersion();
		assertEquals(VERSION, version);

		final int enbid = fixture.getEnodebId();

		assertEquals(Integer.parseInt(CELL_IDS[1].substring(0, 5), 16), enbid);

		final int mseg1 = fixture.getMarketSegment();
		assertEquals(enbid / 10000, mseg1);

	}

	/**
	 * Test pcmd event record_3.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testPcmdEventRecord_3() {
		final byte[] bytes = ByteUtils.decodeHex(PCMD_TEST_DATA_HEX);

		final StemWrapper wrap = wrapFactory.createStemWrapper(bytes,
				PcmdRecord.PCMD_DST_PORT);

		assertEquals(PCMD_TEST_DATA_HEX.substring(0, 12),
				wrap.getHexConnectionId());

		assertEquals(SEQID, wrap.getSequence());

		final PcmdRecord fixture = new PcmdRecord(bytes,
				PcmdRecord.PCMD_GCELLID_IND);
		assertNotNull(fixture);

		final int version = fixture.getVersion();
		assertEquals(VERSION, version);

		final int enbid = fixture.getEnodebId();

		assertEquals(ENBID, enbid);

		final int mseg1 = fixture.getMarketSegment();
		assertEquals(enbid / 10000, mseg1);

	}
/*
	private byte[] createPcmdRecord(String connId, int seqno, int version,
			String cellId) {
		StringBuffer buffer = new StringBuffer();
		byte[] bytes = String.format("%6s", connId).getBytes();
		buffer.append(String.format("%012x", seqno));
		buffer.append(String.format("%08x", version));
		buffer.append(String.format("%032x", 0));

		byte[] bytes1 = HexDump.hexStringToByteArray(buffer.toString());
		bytes = ArrayUtils.addAll(bytes, bytes1);
		byte[] bytes2 = String.format("MCC:MNC:%7s", cellId).getBytes();
		bytes = ArrayUtils.addAll(bytes, bytes2);
		byte[] bytes3 = "adding some more padding".getBytes();
		bytes = ArrayUtils.addAll(bytes, bytes3);

		return bytes;
	}
*/
	/**
 * Creates the new pcmd record.
 *
 * @param connId            the conn id
 * @param seqno            the seqno
 * @param ind the ind
 * @return the byte[]
 */
	public static byte[] createPcmdRecordNew(@NotNull final String connId,
			int seqno,
			int ind) {
		// add wrapper
		// byte[] bytes = String.format("%6s", connId).getBytes();
		byte[] bytes = HexDump.hexStringToByteArray(String.format("%12s",
				connId));
		bytes = ArrayUtils.addAll(bytes,
				HexDump.hexStringToByteArray(String.format("%012x", seqno)));
		// add PCMD record
		bytes = ArrayUtils.addAll(bytes, PCMD_TEST_DATA[ind].getBytes());

		return bytes;
	}
	/**
	 * Launch the test.
	 *
	 * @param args the command line arguments
	 *
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(PcmdDataTest.class);
	}
}