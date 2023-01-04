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

import com.cci.fs.att.domain.PcmdDataTest;
import com.cci.fs.att.domain.PcmdRecord;
import com.cci.fs.att.domain.StemPcmdWrapper;
import com.cci.fs.data.FsDataUtils;
import com.cci.fs.data.PayloadFieldCoords;
import com.cci.fs.msg.PayloadCoordsCache;
import com.cci.fs.protocols.JetFuel.JFPacket;
import com.google.protobuf.ByteString;

/**
 * The class <code>PcmdMessageHandlerTest</code> contains tests for the class
 * <code>{@link PcmdMessageHandler}</code>.
 *
 * @author CCI
 * @version $Rev:$
 */
public class PcmdMessageHandlerTest {

	/** The Constant ZK_HOST. */
	private static final String ZK_HOST = "localhost:2581";
	
	/** The Constant VERSION. */
	private static final int VERSION = 5;

	/** The Constant VERSION_NEW. */
	private static final int VERSION_NEW = 8;

	/** The Constant PCMD_VERSION. */
	private static final String PCMD_VERSION = String
			.format("PCMD-%d", VERSION);

	/** The enodeb id. */
	private static final int ENODEB_ID = 173835;
	
	/** The Constant PCMD_JSON_FILTER. */
	private static final String PCMD_JSON_FILTER = "{\"filters\": [ {\"byteOffset\":\"20\",\"bitOffset\":\"0\",\"bitLength\":\"0\",\"filterValue\":\"[0-9]{3}:[0-9]{3}:2a70b0f\"}]}";

	/** The Constant PCMD_JSON_FILTER_NO_MATCH. */
	private static final String PCMD_JSON_FILTER_NO_MATCH = "{\"filters\":[{\"byteOffset\":\"18\",\"bitOffset\":\"0\",\"bitLength\":\"0\",\"filterValue\":\"1\"}]}";

	/** The Constant MKT_SEG_FILTER. */
	private static final String MKT_SEG_FILTER = "25,20,17";

	/** The Constant MKT_SEG_FILTER_NO_MATCH. */
	private static final String MKT_SEG_FILTER_NO_MATCH = "25,20,90";
	
	// Mock the PayloadCoordsCache so we do not have to actually set
	// one up.
	/** The cache. */
	private final PayloadCoordsCache cache = mock(PayloadCoordsCache.class);
	
	/** The coords. */
	private PayloadFieldCoords coords = null;
	

	/**
	 * Run the PcmdMessageHandler class tests.
	 */
	@Test
	public void testPcmdMessageHandler() {
		byte[] bytes = PcmdDataTest.createPcmdRecordNew(
				PcmdDataTest.HEX_CONN_ID, PcmdDataTest.SEQ_ID, 0);

		final PcmdMessageHandler msgHandler = setupMessageHandler();
		JFPacket jetfuel = msgHandler.buildJFPacket(bytes);
		assertNotNull(jetfuel);
		final int enodebId = jetfuel.getEnodeB();
		assertEquals(ENODEB_ID, enodebId);
		
		final long tstamp = jetfuel.getTimestamp();
		assertTrue(tstamp > 0);
		
		final ByteString bstr = jetfuel.getPayload();
		assertTrue(ArrayUtils.isEquals(bstr.toByteArray(), bytes));

		// change the version and location of enodeb id in PCMD record
		final int newLoc = PcmdRecord.PCMD_GCELLID_IND + 2;
		coords = new PayloadFieldCoords(newLoc, 0, 0);
		when(cache.getCoords()).thenReturn(coords);
		when(cache.fetchCoords(String.format("PCMD-%d", VERSION_NEW)))
				.thenReturn(
				coords);
		bytes = changeEvent(bytes, newLoc, VERSION_NEW);

		jetfuel = msgHandler.buildJFPacket(bytes);
		assertNotNull(jetfuel);
		final int enodebId2 = jetfuel.getEnodeB();
		assertEquals(ENODEB_ID, enodebId2);

	}

	/**
	 * Test PCMD message handler with filters.
	 */
	@Test
	public void testPcmdMessageHandlerWithFilters() {
		final byte[] bytes = PcmdDataTest.createPcmdRecordNew(
				PcmdDataTest.HEX_CONN_ID, PcmdDataTest.SEQ_ID, 0);


		PcmdMessageHandler msgHandler = setupMessageHandler();
		// Set the market filters
		msgHandler.setMarketSegFilter(MKT_SEG_FILTER);
		
		JFPacket jetfuel = msgHandler.buildJFPacket(bytes);
		assertNull(jetfuel);
		
		// Set the generic filters
		msgHandler = setupMessageHandler();
		msgHandler.setFilter(PCMD_JSON_FILTER);
		jetfuel = msgHandler.buildJFPacket(bytes);
		assertNull(jetfuel);
		
		// set up both filters, with only market segment matching
		msgHandler.setMarketSegFilter(MKT_SEG_FILTER);
		msgHandler.setFilter(PCMD_JSON_FILTER_NO_MATCH);
		jetfuel = msgHandler.buildJFPacket(bytes);
		assertNull(jetfuel);

		// set up both filters, with only generic filter matching
		msgHandler.setMarketSegFilter(MKT_SEG_FILTER_NO_MATCH);
		msgHandler.setFilter(PCMD_JSON_FILTER);
		jetfuel = msgHandler.buildJFPacket(bytes);
		assertNull(jetfuel);
		
		// set up both filters, with none matching
		msgHandler.setMarketSegFilter(MKT_SEG_FILTER_NO_MATCH);
		msgHandler.setFilter(PCMD_JSON_FILTER_NO_MATCH);
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
	 * @return the PCMD message handler
	 */
	private PcmdMessageHandler setupMessageHandler(){

		//
		// assuming the byte offset of the coord will contain the
		// index of the global cell id in PCMD's ";" delimited record
		coords = new PayloadFieldCoords(
				PcmdRecord.PCMD_GCELLID_IND, 0, 0);
		
		when(cache.getCoords()).thenReturn(coords);
		when(cache.fetchCoords(PCMD_VERSION)).thenReturn(coords);
		doNothing().when(cache).saveCoords(PCMD_VERSION, coords);

		final PcmdMessageHandler msgHandler = spy(new PcmdMessageHandler(8042,
				ZK_HOST, VERSION, coords, true));

		doReturn(cache).when(msgHandler).createCache();
		
		return msgHandler;
	}
	/**
	 * Change event.
	 *
	 * @param bytes
	 *            the bytes
	 * @param byteOffset
	 *            the byte offset
	 * @param version
	 *            the version
	 * @return the byte[]
	 */
	private byte[] changeEvent(@NotNull final byte[] bytes, int byteOffset,
			int version) {
		final String buffer = FsDataUtils.extractString(bytes,
				StemPcmdWrapper.PCMD_OFFSET, bytes.length
						- StemPcmdWrapper.PCMD_OFFSET,
				"PCMD data");
		final String[] fields = buffer.split(PcmdRecord.PCMD_SEP);

		// change the version
		fields[0] = String.format("%d", version);
		// moving global cell id to a different location
		fields[byteOffset] = fields[PcmdRecord.PCMD_GCELLID_IND];

		// reassembling the string record
		final StringBuilder builder = new StringBuilder(200);
		int ind = 0;
		for (String field : fields) {
			ind++;
			if (ind > 1){
				builder.append(PcmdRecord.PCMD_SEP);
			}
			builder.append(field);
		}
		final byte[] bytes0 = ArrayUtils.subarray(bytes, 0,
				StemPcmdWrapper.PCMD_OFFSET);
		byte[] bytes1 = builder.toString().getBytes();

		bytes1 = ArrayUtils.addAll(bytes0, bytes1);
		return bytes1;
	}
	/**
	 * Launch the test.
	 *
	 * @param args the command line arguments
	 *
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(PcmdMessageHandlerTest.class);
	}
}