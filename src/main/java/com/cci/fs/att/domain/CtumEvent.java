/*
 * File: $HeadURL: https://svn.cci-dev.com/svn/udpPrototype/foxstream/fs-att/trunk/src/main/java/com/cci/fs/att/domain/CtumEvent.java $ 
 * Last Updated On: $Date: 2022-02-27 20:37:06 +0000 (Sun, 27 Feb 2022) $ 
 * Last Updated By: $Author: rajeev $ 
 * 
 * (c) 2014-2015 Customer Care Inc., L.P. All rights reserved. 
 *
 */
package com.cci.fs.att.domain;

import static com.google.common.base.Preconditions.checkArgument;
import io.netty.buffer.ByteBuf;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import com.cci.fs.data.PayloadFieldCoords;
import com.cci.fs.utils.ByteUtils;
import com.cci.fs.utils.MessageUtils;
import com.cci.fs.utils.NullChecks;


/**
 * The Class CtumEvent.
 * 
 * @author CCI
 * @version $Rev: 5355 $
 */
public class CtumEvent {
	
	/** The time stamp. */
	private EventTimeStamp timeStamp;
	
	/** The enodeb id. */
	private int enodebId=0;
	
	/** The market segment. */
	private int marketSegment=0;
	
	/** The cell id. */
	private int cellId=0;
	
	/** The Constant CTUM_DST_PORT. */
	public static final int CTUM_DST_PORT = 39323;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(CtumEvent.class);

	/**
	 * Instantiates a new ctum event.
	 *
	 * @param bytes the bytes
	 */
	public CtumEvent(byte[] bytes){
		parseEventRecord(bytes);
	}
	
	/**
	 * Parses the event record.
	 *
	 * @param bytes the bytes
	 */
	private void parseEventRecord(byte[] bytes){
		final int start = StemCtumWrapper.CTUM_OFFSET;
		checkArgument(bytes != null && bytes.length > 0, 
				NullChecks.illegalArgMessage("bytes"));
		
        // timestamp - hr:min:sec.msec
	    timeStamp = new EventTimeStamp(bytes, start, false);
	    
        // enodeb id 20 bits
        enodebId = CtumEvent.getEnodebId(bytes);
        marketSegment = enodebId/10000;//%100;
        LOGGER.debug("market segment = {}", marketSegment);
        
	    // cell id 8 bits
        cellId = ((bytes[start+6] & 0x01) << 7) | ((bytes[start+7] & 0xFF) >> 1);
		if (LOGGER.isDebugEnabled()){
			LOGGER.debug(String.format("cell id = %d, hex=%02X", cellId, cellId));
		}

	}
	
	/**
	 * Gets the enodeb id.
	 *
	 * @param bytes the bytes
	 * @return the enodeb id
	 */
	static public int getEnodebId(byte[] bytes){
		final int start = StemCtumWrapper.CTUM_OFFSET;
		checkArgument(bytes != null && bytes.length > 0, 
				NullChecks.illegalArgMessage("bytes"));
		
        // enodeb id 20 bits
		final int enbid = ((bytes[start+4] & 0x1F) << 15) | 
				     ((bytes[start+5] & 0xFF) << 7)  | 
				     ((bytes[start+6] & 0xFF) >> 1);
		
		if (LOGGER.isDebugEnabled()){
			LOGGER.debug(String.format("enodeb id = %d, hex=%06X",enbid,enbid));
		}
        
		return enbid;
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

		int shiftR = 32 - coords.getBitLength();
		int shiftL = shiftR - coords.getBitOffset() + 4;
		return (int) ByteUtils.readLongFromByteBuf(bytes,
				coords.getByteOffset(), coords.getBitLength(), shiftL, shiftR);
	}
	
	/**
	 * Gets the market segment from enodeb id.
	 *
	 * @param eNodebId the e nodeb id
	 * @return the market segment from enodeb id
	 */
	static public int getMarketSegmentFromEnodebId(int eNodebId){
		return eNodebId/10000;
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
	 * Gets the time stamp.
	 *
	 * @param bytes the bytes
	 * @return the time stamp
	 */
	public static EventTimeStamp getTimeStamp(byte[] bytes){
		
        // timestamp - hr:min:sec.msec
		final EventTimeStamp timeStamp = new EventTimeStamp(bytes,
				StemCtumWrapper.CTUM_OFFSET, false);

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
		return EventTimeStamp.getTimestamp(bytes,
				StemCtumWrapper.CTUM_OFFSET + 1, false);
		// return ByteUtils.readLongFromByteBuf(bytes,
		// StemCtumWrapper.CTUM_OFFSET + 1, 5, 0, 6);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
}


