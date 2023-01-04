/*
 * File: $HeadURL: https://svn.cci-dev.com/svn/udpPrototype/foxstream/fs-att/trunk/src/main/java/com/cci/fs/att/domain/CtrEvent.java $ 
 * Last Updated On: $Date: 2022-02-27 20:37:06 +0000 (Sun, 27 Feb 2022) $ 
 * Last Updated By: $Author: rajeev $ 
 * 
 * (c) 2014-2015 Customer Care Inc., L.P. All rights reserved. 
 *
 */
package com.cci.fs.att.domain;

import static com.google.common.base.Preconditions.checkArgument;


import javax.validation.constraints.NotNull;

import com.cci.fs.data.PayloadFieldCoords;
import com.cci.fs.utils.ByteUtils;
import com.cci.fs.utils.MessageUtils;
import com.cci.fs.utils.NullChecks;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import io.netty.buffer.ByteBuf;

/**
 * The Class CtrEvent.
 * 
 * @author CCI
 * @version $Rev: 5355 $
 */
public class CtrEvent {
	
	/** The event id. */
	private int eventId=0;
	
	/** The time stamp. */
	private EventTimeStamp timeStamp;
	
	/**  Scanner id and RBS module. */
	private int scannerId=0;
	
	/** The enodeb id. */
	private int enodebId=0;
	
	/** The market segment. */
	private int marketSegment=0;
	
	/** The cell id. */
	private int cellId=0;
	
	/** The Constant CTR_DST_PORT. */
	public static final int CTR_DST_PORT = 11068;

	/** The Constant logger. */
	private static final Logger logger = LogManager.getLogger(CtrEvent.class);

	/**
	 * Instantiates a new ctr event.
	 *
	 * @param bytes the bytes
	 */
	public CtrEvent(byte[] bytes){
		parseEventRecord(bytes);
	}
	
	/**
	 * Parses the event record.
	 *
	 * @param bytes the bytes
	 */
	private void parseEventRecord(byte[] bytes){
		checkArgument(bytes != null && bytes.length > 0, 
				NullChecks.illegalArgMessage("bytes"));
		final int start = StemCtrWrapper.CTR_OFFSET;
		
		// event id 3 bytes
		eventId = ((bytes[start]   & 0xFF) << 16) | 
				  ((bytes[start+1] & 0xFF) << 8)  |
				   (bytes[start+2] & 0xFF);
		if (logger.isDebugEnabled()){
			logger.debug(String.format("Event id = %d, hex=%06X", eventId, eventId));
		}
		
        // timestamp - hr:min:sec.msec
	    timeStamp = new EventTimeStamp(bytes, start+3, true);
	    
		// scanner id 4 bytes
		scannerId = ((bytes[start+8] & 0xFF) << 24) |
				  ((bytes[start+9] & 0xFF) << 16) | 
				  ((bytes[start+10] & 0xFF) << 8)  |
				   (bytes[start+11] & 0xFF);
		if (logger.isDebugEnabled()){
			logger.debug(String.format("Scanner id = %d, hex=%08X", scannerId, scannerId));
		}
        
       // enodeb id 20 bits
        enodebId = getEnodebId(bytes);
        marketSegment = getMarketSegmentFromEnodebId(enodebId);
        logger.debug("market segment = {}", marketSegment);
        
	    // cell id 8 bits
        cellId = (bytes[start+15] & 0xFF);
		if (logger.isDebugEnabled()){
			logger.debug(String.format("cell id = %d, hex=%02X", cellId, cellId));
		}

	}
	
	/**
	 * Gets the enodeb id.
	 *
	 * @param bytes the bytes
	 * @return the enodeb id
	 */
	static public int getEnodebId(byte[] bytes){
		checkArgument(bytes != null && bytes.length > 0, 
				NullChecks.illegalArgMessage("bytes"));
		final int start = StemCtrWrapper.CTR_OFFSET;
		
        // enodeb id 20 bits
		final int enbid = ((bytes[start+12] & 0x0F) << 16) | 
				     ((bytes[start+13] & 0xFF) << 8)  | 
				      (bytes[start+14] & 0xFF);
		
		if (logger.isDebugEnabled()){
			logger.debug(String.format("enodeb id = %d, hex=%06X",enbid,enbid));
		}
        
		return enbid;
	}
	
	/**
	 * Gets the enodeb id.
	 *
	 * @param bytes the bytes
	 * @param coords the coords
	 * @return the enodeb id
	 */
	static public int getEnodebId(@NotNull final byte[] bytes, @NotNull final PayloadFieldCoords coords){
		checkArgument(bytes != null && bytes.length > 0, 
				NullChecks.illegalArgMessage("bytes"));
	
		return MessageUtils.getIntValue(bytes,
				coords.getByteOffset(), coords.getBitOffset(),
				coords.getBitLength());
	}
	
	/**
	 * Gets the enodeb id.
	 *
	 * @param bytes
	 *            the bytes
	 * @param coords
	 *            the coords
	 * @return the enodeb id
	 */
	static public int getEnodebId(@NotNull final ByteBuf bytes,
			@NotNull final PayloadFieldCoords coords) {
		checkArgument(bytes != null && bytes.readableBytes() > 0,
				NullChecks.illegalArgMessage("bytes"));
		int shift = 32 - coords.getBitLength();
		return (int) ByteUtils.readLongFromByteBuf(bytes,
				coords.getByteOffset(), coords.getBitLength(), shift, shift);
	}

	/**
	 * Gets the market segment from enodeb id.
	 *
	 * @param eNodebId
	 *            the e nodeb id
	 * @return the market segment from enodeb id
	 */
	static public int getMarketSegmentFromEnodebId(int eNodebId){
		return eNodebId/10000;
	}
	/**
	 * Gets the market segment.
	 *
	 * @param bytes the bytes
	 * @return the market segment
	 */
	static public int getMarketSegment(byte[] bytes){
		checkArgument(bytes != null && bytes.length > 0, 
				NullChecks.illegalArgMessage("bytes"));
		final int enbid = getEnodebId(bytes);
		
		final int mktseg = (enbid / 10000);// % 100;
		
		return mktseg;
	}

	/**
	 * Gets the time stamp.
	 *
	 * @return the time stamp
	 */
	public EventTimeStamp getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Gets the enodeb id.
	 *
	 * @return the enodeb id
	 */
	public int getEnodebId() {
		return enodebId;
	}

	/**
	 * Gets the market segment.
	 *
	 * @return the market segment
	 */
	public int getMarketSegment() {
		return marketSegment;
	}

	/**
	 * Gets the cell id.
	 *
	 * @return the cell id
	 */
	public int getCellId() {
		return cellId;
	}

	/**
	 * Gets the event id.
	 *
	 * @return the event id
	 */
	public int getEventId() {
		return eventId;
	}

	/**
	 * Gets the scanner id.
	 *
	 * @return the scanner id
	 */
	public int getScannerId() {
		return scannerId;
	}
	
	/**
	 * Gets the time stamp.
	 *
	 * @param bytes the bytes
	 * @return the time stamp
	 */
	public static EventTimeStamp getTimeStamp(byte[] bytes){
		
        // timestamp - hr:min:sec.msec
		final EventTimeStamp timeStamp = new EventTimeStamp(bytes, StemCtrWrapper.CTR_OFFSET+3, true);

		return timeStamp;
	}

	/**
	 * Gets the time stamp.
	 *
	 * @param bytes
	 *            the bytes
	 * @return the time stamp
	 */
	public static long getTimeStamp(ByteBuf bytes) {

		// timestamp - hr:min:sec.msec
		// 3 byte event id followed by timestamp
		return EventTimeStamp.getTimestamp(bytes,
				StemCtrWrapper.CTR_OFFSET + 4,
				true);
		// return ByteUtils.readLongFromByteBuf(bytes,
		// StemCtrWrapper.CTR_OFFSET,
		// 5, 0, 6);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
