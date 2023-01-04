/*
 * File: $HeadURL: https://svn.cci-dev.com/svn/udpPrototype/foxstream/fs-att/trunk/src/test/java/com/cci/fs/att/domain/CtumDataTest.java $ 
 * Last Updated On: $Date: 2016-07-18 23:56:37 +0000 (Mon, 18 Jul 2016) $ 
 * Last Updated By: $Author: rajeev $ 
 * 
 * (c) 2014-2015 Customer Care Inc., L.P. All rights reserved. 
 *
 */
package com.cci.fs.att.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cci.fs.protocols.JetFuel;
import com.cci.fs.utils.MessageUtils;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

/**
 * The class <code>CtumEventTest</code> contains tests for the class <code>{@link CtumEvent}</code>.
 *
 * @author CCI
 * @version $Rev: 5149 $
 */
public class CtumDataTest {
	
	/** The Constant CTUM_HDR_A. */
	public static final String CTUM_HDR_A = "6501000000000001001c04020307de0a111305300160575443334552434d4d4531340000";
	
	/** The Constant CTUM_EVENT_1. */
	public static final String CTUM_EVENT_A = "6501000000000002002c011304115d1e859d13110800001813f41062620000938050131108ffe10e080930ed0001911311080000";
	public static final String CTUM_EVENT_B = "99600000203b9be1002c010f3a071e507b9913400167464357f15393026052680350134001ff301c1b9339170401721340010000";
	public static final String CTUM_EVENT_C = "088b0000206a60fd002c01112f061ad0069713400158846677f35339206022468960134001ff30160486f53a042da91340010000";
	/** 5 digit enodeb id */
	public static final String CTUM_EVENT_D = "6501000000000002002c011304115d02721c13110800001813f41062620000938050131108ffe10e080930ed0001911311080000";
	public static final String CTUM_EVENT_E = "6501000000000002002c011304115d62721c13110800001813f41062620000938050131108ffe10e080930ed0001911311080000";
	/** The lab market segment. */
	public static final int LAB_MARKET_SEGMENT = 100;
	
	/** The enodeb id. */
	public static final int ENODEB_ID = 1000142;
	public static final int ENODEB_ID_D = 80142;

	/** The wrap factory. */
	private final StemWrapperFactory wrapFactory = new StemWrapperFactory(
			CtrEvent.CTR_DST_PORT, CtumEvent.CTUM_DST_PORT,
			PcmdRecord.PCMD_DST_PORT);

	/**
	 * Run the CtrEvent class tests.
	 */
	@Test
	public void testCtumEvent_1() {
		final byte[] bytes = HexBin.decode(CTUM_EVENT_A);
		
		final StemWrapper wrap = wrapFactory.createStemWrapper(bytes,
				CtumEvent.CTUM_DST_PORT);
		final StemRecordType type = wrap.getEventType();
		assertEquals(StemRecordType.CTUM_EVENT, type);
		
		final CtumEvent fixture = new CtumEvent(bytes);
		assertNotNull(fixture);
		
		EventTimeStamp ts = fixture.getTimeStamp();
		assertNotNull(ts);
		
		final long time1 = ts.toTime();
		
		ts = CtumEvent.getTimeStamp(bytes);
		assertNotNull(ts);
		
		final long time2 = ts.toTime();
		
		assertEquals(time1,time2);
		
		final long enbid = fixture.getEnodebId();
		assertEquals(ENODEB_ID, enbid);
		
		final long enbid2 = CtumEvent.getEnodebId(bytes);
		assertEquals(enbid, enbid2);

		final int enbid3 = MessageUtils.getIntValue(bytes,15,3,20);
		assertEquals(enbid, enbid3);

		final int mseg1 = fixture.getMarketSegment();
		assertEquals(LAB_MARKET_SEGMENT, mseg1);

		final int mseg2 = CtumEvent.getMarketSegment(bytes);
		assertEquals(mseg1, mseg2);
		
		final int cellid = fixture.getCellId();
		assertEquals(137, cellid);

		final byte[] bytes2 = HexBin.decode(CTUM_EVENT_D);
		final long enbid4 = CtumEvent.getEnodebId(bytes2);
		assertEquals(ENODEB_ID_D, enbid4);

		final byte[] bytes3 = HexBin.decode(CTUM_EVENT_E);
		final long enbid5 = CtumEvent.getEnodebId(bytes2);
		assertEquals(ENODEB_ID_D, enbid5);
	}
	
	/**
	 * Run the CtrHeader class tests.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testCtumHeader_1() {
		final byte[] bytes = HexBin.decode(CTUM_HDR_A);
		
		final StemWrapper wrap = wrapFactory.createStemWrapper(bytes,
				CtumEvent.CTUM_DST_PORT);
		final StemRecordType type = wrap.getEventType();
		assertEquals(StemRecordType.CTUM_HDR, type);
		
		final CtumHeader fixture = new CtumHeader(bytes);
		assertNotNull(fixture);
		

		final int ffmtVer = fixture.getFileFormatVersion();
		assertEquals(2, ffmtVer);
		
		final int finfoVer = fixture.getFileInfoVersion();
		assertEquals(3, finfoVer);
	}

	@Test
	public void testCtumEvent_2() {

		final byte[] rawMsg = HexBin.decode(CTUM_EVENT_C);
		byte[] eNBBytesWrapper = new byte[5];
		byte[] timestampWrapper = new byte[5];

		// System.arraycopy(rawMsg, 13, eNBBytesWrapper, 0, 5);
		System.arraycopy(rawMsg, 15, eNBBytesWrapper, 0, 3);
		System.arraycopy(rawMsg, 11, timestampWrapper, 0, 5);

		JetFuel.JFPacket.Builder builder = JetFuel.JFPacket.newBuilder();

		// builder.setTimestamp(new BigInteger(timestampWrapper).longValue() >>>
		// 6);
		// builder.setEnodeB(new BigInteger(eNBBytesWrapper).intValue() << 11
		// >>> 12);
		// builder.setTimestamp(new BigInteger(timestampWrapper).longValue() <<
		// Filter.TimeStampFilter.shiftLeft >>>
		// Filter.TimeStampFilter.shiftRight);
		builder.setTimestamp(System.currentTimeMillis());
		// System.out.println("msg: " + ByteUtils.encodeHex(rawMsg));
		int eNBid = new BigInteger(eNBBytesWrapper).intValue() << 11 >>> 12;

	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 */
	@Before
	public void setUp() {
		// add additional set up code here
	}

	/**
	 * Perform post-test clean-up.
	 *
	 * @throws Exception
	 *         if the clean-up fails for some reason
	 *
	 */
	@After
	public void tearDown() {
		// Add additional tear down code here
	}

	/**
	 * Launch the test.
	 *
	 * @param args the command line arguments
	 *
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(CtumDataTest.class);
	}
}