/*
 * File: $HeadURL:$ 
 * Last Updated On: $Date:$ 
 * Last Updated By: $Author: $ 
 * 
 * (c) 2014-2015 Customer Care Inc., L.P. All rights reserved. 
 *
 */
package com.cci.fs.att.domain;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.cci.fs.utils.ByteUtils;

/**
 * The Class StemPcmdWrapper parses the STEM wrapper of the original PCMD pay
 * load.
 *
 * @author CCI
 * @version $Rev:$
 */
public class StemPcmdWrapper implements StemWrapper {
	
	/** The connection id. */
	private String connectionId;
	
	/** The sequence. */
	private long sequence;
	
	/** The Constant PCMD_OFFSET. */
	public static final int PCMD_OFFSET = 12;
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager
			.getLogger(StemPcmdWrapper.class);
	
	/**
	 * Instantiates a new stem wrapper.
	 *
	 * @param bytes the bytes
	 */
	public StemPcmdWrapper(@NotNull final byte[] bytes) {
		parseStemPcmdWrapper(bytes);
	}

	@Override
	/**
	 * Gets the connection id 6 Hex characters MME GUMMEI field includes MCC,
	 * MNC, MMEGI and MMEC in hex.
	 *
	 * @return the connection id
	 */
	public String getHexConnectionId() {
		return connectionId;
	}
	
	/**
	 * Gets the sequence number.
	 *
	 * @return the sequence
	 */
	@Override
	public long getSequence() {
		return sequence;
	}
	/**
	 * Parses the stem wrapper.
	 *
	 * @param bytes
	 *            the bytes
	 */
	private void parseStemPcmdWrapper(@NotNull final byte[] bytes) {
		// conn id 6 bytes
		// STEM specific field. It records MME GUMMEI field including MCC, MNC,
		// MMEGI and MMEC in hex
		byte[] connbytes = new byte[6];
		System.arraycopy(bytes, 0, connbytes, 0, 6);
		connectionId = ByteUtils.encodeHex(connbytes);
		// FsDataUtils.extractString(bytes, 0, 6, "Connection id");


		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Connection id (hex) =%s", connectionId));
			// LOGGER.debug(String.format("Connection id = %d, hex=%s",
			// Long.parseLong(connectionId, 16), connectionId));
		}
        // seq no     6 bytes
		sequence = ((bytes[6] & 0xFF) << 40) | ((bytes[7] & 0xFF) << 32)
				| ((bytes[8] & 0xFF) << 24) | ((bytes[9] & 0xFF) << 16)
				| ((bytes[10] & 0xFF) << 8) | (bytes[11] & 0xFF);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Sequence # = %d, hex=%012X", sequence,
					sequence));
		}
	}

	/* (non-Javadoc)
	 * @see com.cci.fs.att.domain.StemWrapper#getConnectionId()
	 */
	@Override
	public int getConnectionId() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.cci.fs.att.domain.StemWrapper#getStartOffset()
	 */
	@Override
	public int getStartOffset() {
		return PCMD_OFFSET;
	}

	/* (non-Javadoc)
	 * @see com.cci.fs.att.domain.StemWrapper#getEventType()
	 */
	@Override
	public StemRecordType getEventType() {
		return StemRecordType.PCMD_EVENT;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}
