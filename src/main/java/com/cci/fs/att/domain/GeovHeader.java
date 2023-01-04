/*
 * File: $HeadURL: https://svn.cci-dev.com/svn/udpPrototype/foxstream/fs-att/trunk/src/main/java/com/cci/fs/att/domain/GeovHeader.java $ 
 * Last Updated On: $Date: 2015-08-01 07:11:49 +0000 (Sat, 01 Aug 2015) $ 
 * Last Updated By: $Author: rajeev $ 
 * 
 * (c) 2014-2015 Customer Care Inc., L.P. All rights reserved. 
 *
 */
package com.cci.fs.att.domain;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;

import com.cci.fs.data.FsDataUtils;
import com.cci.fs.utils.ConvertUtils;
import com.cci.fs.utils.NullChecks;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import io.netty.buffer.ByteBuf;
import io.pkts.buffer.Buffer;

/**
 * The Class GeovHeader.
 * 
 * @author CCI
 * @version $Rev: 4808 $
 */
public class GeovHeader {

	long syncId;
	int messageLength;
	int messageType;
	int messageId;
	String cellId;
	int eNodebId;
	int eNodebId2;
	int marketSegment;

	/** The Constant logger. */
	private static final Logger logger = LogManager.getLogger(GeovHeader.class);

	/**
	 * Instantiates a new ctr header.
	 *
	 * @param bytes the bytes
	 */
	public GeovHeader(Buffer buffer) {
		parseGeovHeader(buffer);
	}

	/**
	 * Gets the file format version.
	 *
	 * @return the file format version
	 */
	public long getSyncId() {
		return syncId;
	}

	/**
	 * Gets the p m recording version.
	 *
	 * @return the p m recording version
	 */
	public int getpMessageLength() {
		return messageLength;
	}

	/**
	 * Gets the p m recording revision.
	 *
	 * @return the p m recording revision
	 */
	public int getpMessageType() {
		return messageType;
	}

	/**
	 * Gets the other fields.
	 *
	 * @return the other fields
	 */
	public int getMessageId() {
		return messageId;
	}

	/**
	 * Gets the cell id.
	 *
	 * @return the cell id
	 */
	public String getCellId() {
		return cellId;
	}

	/**
	 * Gets the enodeb id.
	 *
	 * @return the enodeb id
	 */
	public int getEnodebId() {
		return eNodebId;
	}

	public int getEnodebId2() {
		return eNodebId2;
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
	 * Parses the Geov header.
	 *
	 * @param bytes the bytes
	 */
	private void parseGeovHeader(Buffer buffer) {

		checkArgument(buffer != null && !buffer.isEmpty(),
				NullChecks.illegalArgMessage("buffer"));
		try {
			int ind = buffer.getReaderIndex();
			int num = buffer.getReadableBytes();
			/*
			 * if (num < 35) {
			 * //num35++;
			 * //Logger.Warn(String.
			 * format("Skipping record as it does not have enough [{} < 35] readable bytes."
			 * ,num));
			 * return;
			 * }
			 * Buffer buff = buffer.readBytes(4);
			 * ind = buffer.getReaderIndex();
			 * String hSyncId = ConvertUtils.convertToHex(buff);
			 * syncId = Long.parseLong(hSyncId,16);
			 * buff = buffer.readBytes(4);
			 * ind = buffer.getReaderIndex();
			 * String hLength = ConvertUtils.convertToHex(buff);
			 * messageLength = Integer.parseInt(hLength,16);
			 * byte msgType = buffer.readByte();
			 * messageType = msgType;
			 * //Integer intMsgType = Integer.valueOf(msgType);
			 * //msgTypeSet.add(intMsgType);
			 * ind = buffer.getReaderIndex();
			 * String hMsgType = String.format("%02x",msgType);
			 * //msgTypeSet.add(hMsgType);
			 * buff = buffer.readBytes(2);
			 * ind = buffer.getReaderIndex();
			 * String hMsgId = ConvertUtils.convertToHex(buff);
			 * messageId = Integer.parseInt(hMsgId,16);
			 */
			/*
			 * num = buffer.getReadableBytes();
			 * if (num == 0) {
			 * System.out.println(String.
			 * format("MsgType: [%s] No more readable bytes - skipping...",hMsgType));
			 * return;
			 * }
			 */
			Buffer buff = buffer.readBytes(27);
			ind = buffer.getReaderIndex();
			buff = buffer.readBytes(8);
			ind = buffer.getReaderIndex();
			String hCellId = ConvertUtils.convertToHex(buff);
			cellId = hCellId;
			// cellId = Integer.parseLong(hCellId, 16);
			String hENodebId = hCellId.substring(11);
			if (hENodebId != null && !hENodebId.isEmpty()) {
				// enbMsgTypeSet.add(hMsgType);
				eNodebId2 = Integer.parseInt(hENodebId, 16);
			}
			// long eNodebId = Long.parseLong(hENodebId,16);
			hENodebId = hCellId.substring(9, 14);
			eNodebId = Integer.parseInt(hENodebId, 16);
			marketSegment = GeovHeader.getMarketSegmentFromEnodebId(eNodebId);
			logger.debug(String.format("CellId: [%s = mktSeg: %d, enbId: %d or %d]", hCellId, marketSegment, eNodebId,
					eNodebId2));
		} catch (IOException e) {
			// logger.error(e);
		}
	}

	/**
	 * Gets the market segment from enodeb id.
	 *
	 * @param eNodebId the e nodeb id
	 * @return the market segment from enodeb id
	 */
	static public int getMarketSegmentFromEnodebId(int eNodebId) {
		return eNodebId / 10000;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
