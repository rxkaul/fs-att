/*
 * File: $HeadURL: https://svn.cci-dev.com/svn/udpPrototype/foxstream/fs-att/trunk/src/test/java/com/cci/fs/att/domain/CtrDataTest.java $ 
 * Last Updated On: $Date: 2015-03-24 23:12:11 +0000 (Tue, 24 Mar 2015) $ 
 * Last Updated By: $Author: rajeev $ 
 * 
 * (c) 2014-2015 Customer Care Inc., L.P. All rights reserved. 
 *
 */
package com.cci.fs.att.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.cci.fs.utils.MessageUtils;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

/**
 * The class <code>CtrEventTest</code> contains tests for the class <code>{@link CtrEvent}</code>.
 *
 * @author CCI
 * @version $Rev: 4426 $
 */
public class CtrDataTest {

	/** The Constant CTR_HDR_A. */
	private static final String CTR_HDR_A = "fe7200000000000101a900015400000000435850393031383530352f3232523541000007de0a120202075754433145454e423136480000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000005754433145454e42313648000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002711000000000000";
	
	/** The Constant CTR_EVENT_A. */
	private static final String CTR_EVENT_A = "fe72000000000002001c0004000c0c02020600b9020000000f42b4ff8000000002000000";
	
	/** The wrap factory. */
	private final StemWrapperFactory wrapFactory = new StemWrapperFactory(
			CtrEvent.CTR_DST_PORT, CtumEvent.CTUM_DST_PORT,
			PcmdRecord.PCMD_DST_PORT);

	/**
	 * Run the CtrEvent class tests.
	 */
	@Test
	public void testCtrEvent_1() {
		final byte[] bytes = HexBin.decode(CTR_EVENT_A);
		
		final StemWrapper wrap = wrapFactory.createStemWrapper(bytes,
				CtrEvent.CTR_DST_PORT);
		// new StemCtrWrapper(bytes, CtrEvent.CTR_DST_PORT);
		final StemRecordType type = wrap.getEventType();
		assertEquals(StemRecordType.CTR_EVENT, type);
		
		final CtrEvent fixture = new CtrEvent(bytes);
		assertNotNull(fixture);
		

		int result = fixture.getEventId();
		assertEquals(3084, result);

		
		result = fixture.getScannerId();
		assertEquals(33554432, result);

		
		EventTimeStamp ts = fixture.getTimeStamp();
		assertNotNull(ts);
		
		final long time1 = ts.toTime();
		
		ts = CtrEvent.getTimeStamp(bytes);
		assertNotNull(ts);
		
		final long time2 = ts.toTime();
		
		assertEquals(time1,time2);
		
		final int enbid = fixture.getEnodebId();
		assertEquals(1000116, enbid);
		
		final int enbid2 = CtrEvent.getEnodebId(bytes);
		assertEquals(enbid, enbid2);
		
		final int enbid3 = MessageUtils.getIntValue(bytes,24,4,20);
		assertEquals(enbid, enbid3);
		
		
		final int mseg1 = fixture.getMarketSegment();
		assertEquals(100, mseg1);
		
		final int mseg2 = CtrEvent.getMarketSegment(bytes);
		assertEquals(mseg1, mseg2);
		
		result = fixture.getCellId();
		assertEquals(255, result);
	}
	
	/**
	 * Run the CtrHeader class tests.
	 */
	@Test
	public void testCtrHeader_1() {
		final byte[] bytes = HexBin.decode(CTR_HDR_A);
		
		final StemCtrWrapper wrap = new StemCtrWrapper(bytes);
		final StemRecordType type = wrap.getEventType();
		assertEquals(StemRecordType.CTR_HDR, type);
		
		final CtrHeader fixture = new CtrHeader(bytes);
		assertNotNull(fixture);
		

		final String fmtver = fixture.getFileFormatVersion();
		assertEquals("T", fmtver);
		
		final String pmver = fixture.getpMRecordingVersion();
		assertEquals("CXP9018505/22", pmver);

		final String pmrev = fixture.getpMRecordingRevision();
		assertEquals("R5A", pmrev);

		final String otherflds = fixture.getOtherFields();
		// assertEquals("18505/2",otherflds);
		assertNotNull(otherflds);
		
		final String combver = CtrHeader.getCombinedVersion(bytes);
		assertEquals(fmtver + pmver + pmrev, combver);

		final String enbName = fixture.getEnodebName();
		assertNotNull(enbName);
		
		assertEquals("WTC1EENB16H", enbName);

	}

	/**
	 * Launch the test.
	 *
	 * @param args the command line arguments
	 *
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(CtrDataTest.class);
	}
}