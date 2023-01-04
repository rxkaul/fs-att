/*
 * File: $HeadURL:$ 
 * Last Updated On: $Date:$ 
 * Last Updated By: $Author: $ 
 * 
 * (c) 2014-2015 Customer Care Inc., L.P. All rights reserved. 
 *
 */
package com.cci.fs.att.msg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;

import com.cci.fs.att.domain.StemCtrWrapper;
import com.cci.fs.data.PayloadFieldCoords;
import com.cci.fs.msg.PayloadCoordsCache;
import com.cci.fs.protocols.JetFuel.JFPacket;
import com.google.protobuf.ByteString;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

/**
 * The class <code>CtrMessageHandlerTest</code> contains tests for the class
 * <code>{@link CtrMessageHandler}</code>.
 *
 * @author CCI
 * @version $Rev:$
 */
public class CtrMessageHandlerTest {

	/** The Constant CTR_HDR_A. */
	private static final String CTR_HDR_A = "fe7200000000000101a900015400000000435850393031383530352f3232523541000007de0a120202075754433145454e423136480000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000005754433145454e42313648000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002711000000000000";
	
	/** 7 digit enodebid **/
	private static final String CTR_EVENT_A = "fe72000000000002001c0004000c0c02020600b9020000000f42b4ff8000000002000000";
	/** 5 digit enodebid **/
	private static final String CTR_EVENT_B = "fe72000000000002001c0004000c0c02020600b9020000000138F4ff8000000002000000";
	/** 5 digit enodebid **/
	private static final String CTR_EVENT_C = "fe72000000000002001c0004000c0c02020600b902000000A138F4ff8000000002000000";
	/** The Constant ZK_HOST. */
	private static final String ZK_HOST = "localhost:2581";
	
	/** The Constant VERSION. */
	private static final String VERSION = "TCXP9018505/22R5A";
	
	/** The Constant VERSION_NEW. */
	private static final String VERSION_NEW = "UVXP9018505/22R5A";
	
	/** The Constant CTR_VERSION. */
	private static final String CTR_VERSION = String.format("CTR-%s", VERSION);
	
	/** The enodeb id. */
	private static final int ENODEB_ID = 1000116;
	private static final int ENODEB_ID_B = 80116;
	
	/** The Constant CTR_JSON_FILTER. */
	private static final String CTR_JSON_FILTER = "{\"filters\":[{\"byteOffset\":\"24\",\"bitOffset\":\"4\",\"bitLength\":\"20\",\"filterValue\":\"1000116\"}]}";

	/** The Constant CTR_JSON_FILTER_NO_MATCH. */
	private static final String CTR_JSON_FILTER_NO_MATCH = "{\"filters\":[{\"byteOffset\":\"24\",\"bitOffset\":\"4\",\"bitLength\":\"20\",\"filterValue\":\"1\"}]}";

	/** The Constant CTR_MKT_SEG_FILTER. */
	private static final String CTR_MKT_SEG_FILTER = "25,20,100";

	/** The Constant CTR_MKT_SEG_FILTER_NO_MATCH. */
	private static final String CTR_MKT_SEG_FILTER_NO_MATCH = "25,20,90";
	
	// Mock the PayloadCoordsCache so we do not have to actually set
	// one up.
	/** The cache. */
	private final PayloadCoordsCache cache = mock(PayloadCoordsCache.class);
	
	/** The coords. */
	private PayloadFieldCoords coords = null;
	
	/**
	 * Run the CtrMessageHandler class tests.
	 */
	@Test
	public void testCtrMessageHandler() {
		byte[] bytes = HexBin.decode(CTR_EVENT_A);

		final CtrMessageHandler msgHandler = setupMessageHandler();
		JFPacket jetfuel = msgHandler.buildJFPacket(bytes);
		assertNotNull(jetfuel);
		int enodebId = jetfuel.getEnodeB();
		assertEquals(ENODEB_ID, enodebId);
		
		bytes = HexBin.decode(CTR_EVENT_B);

		jetfuel = msgHandler.buildJFPacket(bytes);
		assertNotNull(jetfuel);
		enodebId = jetfuel.getEnodeB();
		assertEquals(ENODEB_ID_B, enodebId);

		bytes = HexBin.decode(CTR_EVENT_C);

		jetfuel = msgHandler.buildJFPacket(bytes);
		assertNotNull(jetfuel);
		enodebId = jetfuel.getEnodeB();
		assertEquals(ENODEB_ID_B, enodebId);

		final long tstamp = jetfuel.getTimestamp();
		assertTrue(tstamp > 0);
		
		final ByteString bstr = jetfuel.getPayload();
		assertTrue(ArrayUtils.isEquals(bstr.toByteArray(), bytes));

		bytes = HexBin.decode(CTR_HDR_A);

		jetfuel = msgHandler.buildJFPacket(bytes);
		assertNull(jetfuel);

		coords = new PayloadFieldCoords(31, 4, 20);
		when(cache.getCoords()).thenReturn(coords);
		when(cache.fetchCoords(String.format("CTR-%s", VERSION_NEW))).thenReturn(
				coords);
		bytes = changeHeader(bytes);
		jetfuel = msgHandler.buildJFPacket(bytes);

		bytes = HexBin.decode(CTR_EVENT_A);
		bytes = changeEvent(bytes, 31);

		jetfuel = msgHandler.buildJFPacket(bytes);
		assertNotNull(jetfuel);
		final int enodebId2 = jetfuel.getEnodeB();
		assertEquals(ENODEB_ID, enodebId2);

	}

	/**
	 * Test ctr message handler with filters.
	 */
	@Test
	public void testCtrMessageHandlerWithFilters() {
		final byte[] bytes = HexBin.decode(CTR_EVENT_A);

		CtrMessageHandler msgHandler = setupMessageHandler();
		// Set the market filters
		msgHandler.setMarketSegFilter(CTR_MKT_SEG_FILTER);
		
		JFPacket jetfuel = msgHandler.buildJFPacket(bytes);
		assertNull(jetfuel);
		
		// Set the generic filters
		msgHandler = setupMessageHandler();
		msgHandler.setFilter(CTR_JSON_FILTER);
		jetfuel = msgHandler.buildJFPacket(bytes);
		assertNull(jetfuel);
		
		// set up both filters, with only market segment matching
		msgHandler.setMarketSegFilter(CTR_MKT_SEG_FILTER);
		msgHandler.setFilter(CTR_JSON_FILTER_NO_MATCH);
		jetfuel = msgHandler.buildJFPacket(bytes);
		assertNull(jetfuel);

		// set up both filters, with only generic filter matching
		msgHandler.setMarketSegFilter(CTR_MKT_SEG_FILTER_NO_MATCH);
		msgHandler.setFilter(CTR_JSON_FILTER);
		jetfuel = msgHandler.buildJFPacket(bytes);
		assertNull(jetfuel);
		
		// set up both filters, with none matching
		msgHandler.setMarketSegFilter(CTR_MKT_SEG_FILTER_NO_MATCH);
		msgHandler.setFilter(CTR_JSON_FILTER_NO_MATCH);
		jetfuel = msgHandler.buildJFPacket(bytes);
		assertNotNull(jetfuel);
		
		// remove filters
		msgHandler.setMarketSegFilter(null);
		msgHandler.setFilter(null);
		jetfuel = msgHandler.buildJFPacket(bytes);
		assertNotNull(jetfuel);
	}
	
	/**
	 * Setup message handler.
	 *
	 * @return the ctr message handler
	 */
	private CtrMessageHandler setupMessageHandler(){
		// 24:4:20
		//
		coords = new PayloadFieldCoords(24, 4, 20);
		
		when(cache.getCoords()).thenReturn(coords);
		when(cache.fetchCoords(CTR_VERSION)).thenReturn(coords);
		doNothing().when(cache).saveCoords(CTR_VERSION, coords);

		final CtrMessageHandler msgHandler = spy(new CtrMessageHandler(8042, ZK_HOST,
				VERSION, coords, true));
		

		doReturn(cache).when(msgHandler).createCache();
		
		return msgHandler;

	}
	/**
	 * Change header.
	 *
	 * @param bytes the bytes
	 * @return the byte[]
	 */
	private byte[] changeHeader(@NotNull final byte[] bytes) {
		final byte[] bytes0 = VERSION_NEW.substring(0, 1).getBytes();
		final byte[] bytes1 = VERSION_NEW.substring(1).getBytes();
		final int len1 = bytes1.length;

		final byte[] bytes2 = ArrayUtils.clone(bytes);

		bytes2[StemCtrWrapper.CTR_OFFSET] = bytes0[0];
		for (int ind = 0; ind < len1; ind++) {
			bytes2[StemCtrWrapper.CTR_OFFSET + ind + 5] = bytes1[ind];
		}
		return bytes2;
	}

	/**
	 * Change event.
	 *
	 * @param bytes the bytes
	 * @param byteOffset the byte offset
	 * @return the byte[]
	 */
	private byte[] changeEvent(@NotNull final byte[] bytes, int byteOffset) {
		final byte[] bytes1 = ArrayUtils.clone(bytes);
		//
		// move 3 bytes containing enodebId to a different location
		bytes1[byteOffset] = bytes[24];
		bytes1[byteOffset + 1] = bytes[25];
		bytes1[byteOffset + 2] = bytes[26];
		return bytes1;
	}
	/**
	 * Launch the test.
	 *
	 * @param args the command line arguments
	 *
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(CtrMessageHandlerTest.class);
	}
}