/*
 * File: $HeadURL:$ 
 * Last Updated On: $Date:$ 
 * Last Updated By: $Author: $ 
 * 
 * (c) 2014-2015 Customer Care Inc., L.P. All rights reserved. 
 *
 */
package com.cci.fs.att.domain;

import io.netty.buffer.ByteBuf;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


// TODO: Auto-generated Javadoc
/**
 * The Class StemWrapper parses the STEM wrapper of the original eNodeB pay load.
 *
 * @author CCI
 * @version $Rev:$
 */
public class StemCtumWrapper implements StemWrapper {
	
	/** The connection id. */
	private int connectionId;
	
	/** The sequence. */
	private long sequence;
	
	/** The event length. */
	private int eventLength;

	/** The event type. */
	private StemRecordType eventType;

	/** The Constant CTUM_OFFSET. */
	public static final int CTUM_OFFSET = 11;
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager
			.getLogger(StemCtumWrapper.class);
	
	/**
	 * Gets the connection id.
	 *
	 * @return the connection id
	 */
	@Override
	public int getConnectionId() {
		return connectionId;
	}
	
	@Override
	public String getHexConnectionId() {
		return Integer.toHexString(connectionId);
	}
	/**
	 * Gets the sequence.
	 *
	 * @return the sequence
	 */
	@Override
	public long getSequence() {
		return sequence;
	}
	
	/**
	 * Gets the event length.
	 *
	 * @return the event length
	 */
	public int getEventLength() {
		return eventLength;
	}
	
	/**
	 * Gets the event type.
	 *
	 * @return the event type
	 */
	@Override
	public StemRecordType getEventType() {
		return eventType;
	}

	/**
	 * Instantiates a new stem ctum wrapper.
	 *
	 * @param bytes
	 *            the bytes
	 */
	public StemCtumWrapper(@NotNull final byte[] bytes) {
		parseStemWrapper(bytes);
	}

	/**
	 * Parses the stem wrapper.
	 *
	 * @param bytes the bytes
	 */
	private void parseStemWrapper(byte[] bytes) {
        // conn id    2 bytes
		connectionId = ((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);
        LOGGER.debug(String.format("Connection id = %d, hex=%04X",connectionId,connectionId));
        // seq no     6 bytes
 		sequence = ((bytes[2] & 0xFF) << 40) | ((bytes[3] & 0xFF) << 32)
				  | ((bytes[4] & 0xFF) << 24) | ((bytes[5] & 0xFF) << 16)
		          | ((bytes[6] & 0xFF) << 8)  |  (bytes[7] & 0xFF);
 		LOGGER.debug(String.format("Sequence # = %d, hex=%012X",sequence,sequence));
        // event len  2 bytes
		eventLength = ((bytes[8] & 0xFF) << 8) | (bytes[9] & 0xFF);
		LOGGER.debug(String.format("Event length = %d, hex=%04X", eventLength, eventLength));
        // event type
		eventType = getEventType(bytes);
	}
	
	/**
	 * Gets the event type.
	 *
	 * @param bytes the bytes
	 * @return the event type
	 */
	static public StemRecordType getEventType(@NotNull final byte[] bytes) {
        int evttyp = 1;
		StemRecordType eventType = StemRecordType.CTUM_EVENT;
		// 1 byte for CTUM
		evttyp = (bytes[10] & 0xFF);
		if (evttyp == 4) {
			eventType = StemRecordType.CTUM_HDR;
			LOGGER.debug("Event record type = CTUM header");
		} else if (evttyp == 1) {
			eventType = StemRecordType.CTUM_EVENT;
			LOGGER.debug("Event record type = CTUM event");
		}
		if (LOGGER.isDebugEnabled()) {
	        LOGGER.debug(String.format("Event type = %d, hex=%04X", evttyp, evttyp));
        }
        return eventType;
	}

	/**
	 * Gets the event type.
	 *
	 * @param bytes
	 *            the bytes
	 * @return the event type
	 */
	static public StemRecordType getEventType(@NotNull final ByteBuf bytes) {
		int evttyp = 1;
		StemRecordType eventType = StemRecordType.CTUM_EVENT;
		// 1 byte for CTUM
		evttyp = (bytes.getByte(10) & 0xFF);
		if (evttyp == 4) {
			eventType = StemRecordType.CTUM_HDR;
			LOGGER.debug("Event record type = CTUM header");
		} else if (evttyp == 1) {
			eventType = StemRecordType.CTUM_EVENT;
			LOGGER.debug("Event record type = CTUM event");
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Event type = %d, hex=%04X", evttyp,
					evttyp));
		}
		return eventType;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cci.fs.att.domain.StemWrapper#getStartOffset()
	 */
	/**
	 * Gets the start offset.
	 *
	 * @return the start offset
	 */
	@Override
	public int getStartOffset() {
		return CTUM_OFFSET;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
}
