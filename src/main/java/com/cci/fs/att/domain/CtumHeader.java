/*
 * File: $HeadURL: https://svn.cci-dev.com/svn/udpPrototype/foxstream/fs-att/trunk/src/main/java/com/cci/fs/att/domain/CtumHeader.java $ 
 * Last Updated On: $Date: 2015-08-01 07:11:49 +0000 (Sat, 01 Aug 2015) $ 
 * Last Updated By: $Author: rajeev $ 
 * 
 * (c) 2014-2015 Customer Care Inc., L.P. All rights reserved. 
 *
 */
package com.cci.fs.att.domain;

import static com.google.common.base.Preconditions.checkArgument;
import io.netty.buffer.ByteBuf;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cci.fs.utils.NullChecks;

/**
 * The Class CtumHeader.
 * 
 * @author CCI
 * @version $Rev: 4808 $
 */
public class CtumHeader {

	/** The file format version. */
	private int fileFormatVersion;
	
	/** The file info version. */
	private int fileInfoVersion;
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CtumHeader.class);
	
	/**
	 * Instantiates a new ctum header.
	 *
	 * @param bytes the bytes
	 */
	public CtumHeader(byte[] bytes){
		parseCtumHeader(bytes);
	}
	
	public CtumHeader(ByteBuf bytes) {
		parseCtumHeader(bytes);
	}
	/**
	 * Gets the file format version.
	 *
	 * @return the file format version
	 */
	public int getFileFormatVersion() {
		return fileFormatVersion;
	}

	/**
	 * Gets the file info version.
	 *
	 * @return the file info version
	 */
	public int getFileInfoVersion() {
		return fileInfoVersion;
	}
	
	/**
	 * Parses the ctum header.
	 *
	 * @param bytes the bytes
	 */
	private void parseCtumHeader(byte[] bytes){

		checkArgument(bytes != null && bytes.length > 0, 
				NullChecks.illegalArgMessage("bytes"));
		// File format version
		fileFormatVersion = (bytes[StemCtumWrapper.CTUM_OFFSET] & 0xFF);
		if (LOGGER.isDebugEnabled()){
			LOGGER.debug(String.format("File format version = %d, hex=%02x", fileFormatVersion, fileFormatVersion));
		}
		// File information version
		fileInfoVersion = (bytes[StemCtumWrapper.CTUM_OFFSET + 1] & 0xFF);
		if (LOGGER.isDebugEnabled()){
			LOGGER.debug(String.format("File information version = %d, hex=%02x",fileInfoVersion, fileInfoVersion));
		}

	}

	/**
	 * Parses the ctum header.
	 *
	 * @param bytes
	 *            the bytes
	 */
	private void parseCtumHeader(ByteBuf bytes) {

		checkArgument(bytes != null && bytes.readableBytes() > 0,
				NullChecks.illegalArgMessage("bytes"));
		// File format version
		fileFormatVersion = (bytes.getByte(StemCtumWrapper.CTUM_OFFSET) & 0xFF);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("File format version = %d, hex=%02x",
					fileFormatVersion, fileFormatVersion));
		}
		// File information version
		fileInfoVersion = (bytes.getByte(StemCtumWrapper.CTUM_OFFSET + 1) & 0xFF);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format(
					"File information version = %d, hex=%02x", fileInfoVersion,
					fileInfoVersion));
		}

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}	
