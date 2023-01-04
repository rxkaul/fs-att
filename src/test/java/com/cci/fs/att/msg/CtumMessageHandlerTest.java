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

import com.cci.fs.att.domain.CtumDataTest;
import com.cci.fs.att.domain.StemCtumWrapper;
import com.cci.fs.data.PayloadFieldCoords;
import com.cci.fs.msg.PayloadCoordsCache;
import com.cci.fs.protocols.JetFuel.JFPacket;
import com.google.protobuf.ByteString;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

/**
 * The class <code>CtumMessageHandlerTest</code> contains tests for the class
 * <code>{@link CtumMessageHandler}</code>.
 *
 * @author CCI
 * @version $Rev$
 */
public class CtumMessageHandlerTest {

	/** The Constant ZK_HOST. */
	private static final String ZK_HOST = "localhost:2581";
	
	/** The Constant VERSION. */
	private static final String VERSION = "23";
	
	/** The Constant VERSION_NEW. */
	private static final String VERSION_NEW = "45";
	
	/** The Constant CTUM_VERSION. */
	private static final String CTUM_VERSION = String
			.format("CTUM-%s", VERSION);

	/** The Constant CTUM_JSON_FILTER. */
	private static final String CTUM_JSON_FILTER = "{\"filters\": [ {\"byteOffset\":\"15\",\"bitOffset\":\"3\",\"bitLength\":\"20\",\"filterValue\":\"1000142\"}]}";

	/** The Constant CTUM_JSON_FILTER_NO_MATCH. */
	private static final String CTUM_JSON_FILTER_NO_MATCH = "{\"filters\":[{\"byteOffset\":\"24\",\"bitOffset\":\"4\",\"bitLength\":\"20\",\"filterValue\":\"1\"}]}";

	/** The Constant MKT_SEG_FILTER. */
	private static final String MKT_SEG_FILTER = "25,20,100";

	/** The Constant MKT_SEG_FILTER_NO_MATCH. */
	private static final String MKT_SEG_FILTER_NO_MATCH = "25,20,90";
	
	// Mock the PayloadCoordsCache so we do not have to actually set
	// one up.
	/** The cache. */
	private final PayloadCoordsCache cache = mock(PayloadCoordsCache.class);
	
	/** The coords. */
	private PayloadFieldCoords coords = null;
	
	/**
	 * Run the CtumMessageHandler class tests.
	 */
	@Test
	public void testCtumMessageHandler() {
		byte[] bytes = HexBin.decode(CtumDataTest.CTUM_EVENT_A);

		final CtumMessageHandler msgHandler = setupMessageHandler();
		JFPacket jetfuel = msgHandler.buildJFPacket(bytes);
		assertNotNull(jetfuel);
		final int enodebId = jetfuel.getEnodeB();
		assertEquals(CtumDataTest.ENODEB_ID, enodebId);
		
		final long tstamp = jetfuel.getTimestamp();
		assertTrue(tstamp > 0);
		
		final ByteString bstr = jetfuel.getPayload();
		assertTrue(ArrayUtils.isEquals(bstr.toByteArray(), bytes));


		bytes = HexBin.decode(CtumDataTest.CTUM_HDR_A);

		jetfuel = msgHandler.buildJFPacket(bytes);
		assertNull(jetfuel);

		coords = new PayloadFieldCoords(31, 3, 20);
		when(cache.getCoords()).thenReturn(coords);
		when(cache.fetchCoords(String.format("CTUM-%s", VERSION_NEW)))
				.thenReturn(
				coords);
		bytes = changeHeader(bytes);
		jetfuel = msgHandler.buildJFPacket(bytes);

		bytes = HexBin.decode(CtumDataTest.CTUM_EVENT_A);
		bytes = changeEvent(bytes, 31);

		jetfuel = msgHandler.buildJFPacket(bytes);
		assertNotNull(jetfuel);
		final int enodebId2 = jetfuel.getEnodeB();
		assertEquals(CtumDataTest.ENODEB_ID, enodebId2);

	}


	/**
	 * Test CTUM message handler with filters.
	 */
	@Test
	public void testCumMessageHandlerWithFilters() {
		final byte[] bytes = HexBin.decode(CtumDataTest.CTUM_EVENT_A);

		CtumMessageHandler msgHandler = setupMessageHandler();
		// Set the market filters
		msgHandler.setMarketSegFilter(MKT_SEG_FILTER);
		
		JFPacket jetfuel = msgHandler.buildJFPacket(bytes);
		assertNull(jetfuel);
		
		// Set the generic filters
		msgHandler = setupMessageHandler();
		msgHandler.setFilter(CTUM_JSON_FILTER);
		jetfuel = msgHandler.buildJFPacket(bytes);
		assertNull(jetfuel);
		
		// set up both filters, with only market segment matching
		msgHandler.setMarketSegFilter(MKT_SEG_FILTER);
		msgHandler.setFilter(CTUM_JSON_FILTER_NO_MATCH);
		jetfuel = msgHandler.buildJFPacket(bytes);
		assertNull(jetfuel);

		// set up both filters, with only generic filter matching
		msgHandler.setMarketSegFilter(MKT_SEG_FILTER_NO_MATCH);
		msgHandler.setFilter(CTUM_JSON_FILTER);
		jetfuel = msgHandler.buildJFPacket(bytes);
		assertNull(jetfuel);
		
		// set up both filters, with none matching
		msgHandler.setMarketSegFilter(MKT_SEG_FILTER_NO_MATCH);
		msgHandler.setFilter(CTUM_JSON_FILTER_NO_MATCH);
		jetfuel = msgHandler.buildJFPacket(bytes);
		assertNotNull(jetfuel);
		
		// remove filters
		msgHandler.setMarketSegFilter(null);
		msgHandler.setFilter(null);
		jetfuel = msgHandler.buildJFPacket(bytes);
		assertNotNull(jetfuel);
	}
	/**
	 * Change header.
	 *
	 * @param bytes the bytes
	 * @return the byte[]
	 */
	private byte[] changeHeader(@NotNull final byte[] bytes) {

		final byte[] bytes1 = new byte[2];

		bytes1[0] = 0x04;
		bytes1[1] = 0x05;

		final byte[] bytes2 = ArrayUtils.clone(bytes);

		bytes2[StemCtumWrapper.CTUM_OFFSET] = bytes2[0];
		for (int ind = 0; ind < 2; ind++) {
			bytes2[StemCtumWrapper.CTUM_OFFSET + ind] = bytes1[ind];
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
		bytes1[byteOffset] = bytes[15];
		bytes1[byteOffset + 1] = bytes[16];
		bytes1[byteOffset + 2] = bytes[17];
		return bytes1;
	}
	
	/**
	 * Setup message handler.
	 *
	 * @return the CTUM message handler
	 */
	private CtumMessageHandler setupMessageHandler(){
		// 15:3:20
		//

		coords = new PayloadFieldCoords(15, 3, 20);
		
		when(cache.getCoords()).thenReturn(coords);
		when(cache.fetchCoords(CTUM_VERSION)).thenReturn(coords);
		doNothing().when(cache).saveCoords(CTUM_VERSION, coords);

		final CtumMessageHandler msgHandler = spy(new CtumMessageHandler(8042,
				ZK_HOST, VERSION, coords, true));

		doReturn(cache).when(msgHandler).createCache();
		
		return msgHandler;

	}

	/**
	 * Launch the test.
	 *
	 * @param args the command line arguments
	 *
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(CtumMessageHandlerTest.class);
	}
}