/*
 * File: $HeadURL: https://svn.cci-dev.com/svn/udpPrototype/foxstream/fs-att/trunk/src/main/java/com/cci/fs/att/domain/StemCtrWrapper.java $ 
 * Last Updated On: $Date: 2022-02-27 20:37:06 +0000 (Sun, 27 Feb 2022) $ 
 * Last Updated By: $Author: rajeev $ 
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
 * @version $Rev: 5355 $
 */
public class StemCtrWrapper implements StemWrapper {
	
	/** The connection id. */
	private int connectionId;
	
	/** The sequence. */
	private long sequence;
	
	/** The event length. */
	private int eventLength;

	/** The event type. */
	private StemRecordType eventType;

	/** The Constant CTR_OFFSET. */
	public static final int CTR_OFFSET = 12;
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(StemCtrWrapper.class);
	
	
	/**
	 * Instantiates a new stem ctr wrapper.
	 *
	 * @param bytes
	 *            the bytes
	 */
	public StemCtrWrapper(@NotNull final byte[] bytes) {
		parseStemWrapper(bytes);
	}

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
		eventType = StemCtrWrapper.getEventType(bytes);
	}
	
	/**
	 * Gets the event type.
	 *
	 * @param bytes the bytes
	 * @return the event type
	 */
	static public StemRecordType getEventType(byte[] bytes) {
        int evttyp = 1;
        StemRecordType eventType = StemRecordType.CTR_EVENT;
		// 2 bytes for CTR
		evttyp = ((bytes[10] & 0xFF) << 8) | (bytes[11] & 0xFF);
		// evttyp = (bytes[10] & 0xFF);
		if (evttyp == 1) {
			eventType = StemRecordType.CTR_HDR;
			LOGGER.debug("Event record type = CTR header");
		} else if (evttyp == 4) {
			eventType = StemRecordType.CTR_EVENT;
			LOGGER.debug("Event record type = CTR event");
		}
        if (LOGGER.isDebugEnabled()){
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
	static public StemRecordType getEventType(ByteBuf bytes) {
		int evttyp = 1;
		StemRecordType eventType = StemRecordType.CTR_EVENT;
		// 2 bytes for CTR
		evttyp = ((bytes.getByte(10) & 0xFF) << 8) | (bytes.getByte(11) & 0xFF);
		// evttyp = (bytes[10] & 0xFF);
		if (evttyp == 1) {
			eventType = StemRecordType.CTR_HDR;
			LOGGER.debug("Event record type = CTR header");
		} else if (evttyp == 4) {
			eventType = StemRecordType.CTR_EVENT;
			LOGGER.debug("Event record type = CTR event");
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
		return CTR_OFFSET;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
}
