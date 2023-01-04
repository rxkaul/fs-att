/*
 * File: $HeadURL: https://svn.cci-dev.com/svn/udpPrototype/foxstream/fs-att/trunk/src/main/java/com/cci/fs/att/domain/StemCtrWrapper.java $ 
 * Last Updated On: $Date: 2016-06-10 07:03:56 +0000 (Fri, 10 Jun 2016) $ 
 * Last Updated By: $Author: rajeev $ 
 * 
 * (c) 2014-2015 Customer Care Inc., L.P. All rights reserved. 
 *
 */
package com.cci.fs.att.domain;

import java.io.IOException;

import javax.validation.constraints.NotNull;

import com.cci.fs.utils.ConvertUtils;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.apache.logging.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.pkts.buffer.Buffer;


// TODO: Auto-generated Javadoc
/**
 * The Class StemWrapper parses the STEM wrapper of the original eNodeB pay load.
 *
 * @author CCI
 * @version $Rev: 5078 $
 */
public class StemGeovWrapper implements StemWrapper {
	
	/** The connection id. */
	private int connectionId;
	
	/** The sequence. */
	private long sequence;
	
	/** The event length. */
	private int eventLength;

	private int syncId;
	private int messageId;
	private int messageType;
	private int messageLength;
	private int cellId;
	private int eNodebId;
	private int eNodebId2;
	private int marketSegment;

	/** The event type. */
	private StemRecordType eventType;

	
	private static final int GEOV_OFFSET=0;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(StemCtrWrapper.class);
	
	
	/**
	 * Instantiates a new stem ctr wrapper.
	 *
	 * @param bytes
	 *            the bytes
	 */
	public StemGeovWrapper(@NotNull final Buffer buffer) {
		parseStemWrapper(buffer);
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
	private void parseStemWrapper(Buffer buffer) {
		try {
			int ind = buffer.getReaderIndex();
			int num = buffer.getReadableBytes();
			if (num < 35) {
				//num35++;
				//Logger.Warn(String.format("Skipping record as it does not have enough [{} < 35] readable bytes.",num));
				return;
			}
			Buffer buff = buffer.readBytes(4);
			ind = buffer.getReaderIndex();
			String hSyncId = ConvertUtils.convertToHex(buff);
			syncId = Integer.parseInt(hSyncId,16);
			sequence = syncId;
			buff = buffer.readBytes(4);
			ind = buffer.getReaderIndex();
			String hLength = ConvertUtils.convertToHex(buff);
			messageLength = Integer.parseInt(hLength,16);
			eventLength = messageLength;
			byte msgType = buffer.readByte();
			messageType = msgType;
			eventType = StemRecordType.GEOV_HDR;

			//if (messageType == 2) {
			//	eventType = StemRecordType.GEOV_EVENT;
			//} 
			/*else if (messageType == 4) {
				eventType = StemRecordType.GEOV_EVENT;
			}*/
			//Integer intMsgType = Integer.valueOf(msgType);
			//msgTypeSet.add(intMsgType);
			ind = buffer.getReaderIndex();
			String hMsgType = String.format("%02x",msgType);
			//msgTypeSet.add(hMsgType);
			buff = buffer.readBytes(2);
			ind = buffer.getReaderIndex();
			String hMsgId = ConvertUtils.convertToHex(buff);
			messageId = Integer.parseInt(hMsgId,16);
			connectionId = messageId;
			buff = buffer.readBytes(2);
			ind = buffer.getReaderIndex();
			String hSeqNum = ConvertUtils.convertToHex(buff);
			buff = buffer.readBytes(14);
			ind = buffer.getReaderIndex();
			String hTimeStamp = ConvertUtils.convertToHex(buff);
	/*num = buffer.getReadableBytes();
			if (num == 0) {
				System.out.println(String.format("MsgType: [%s] No more readable bytes - skipping...",hMsgType));
				return;
			}*/
			/*buff = buffer.readBytes(16);
			ind = buffer.getReaderIndex();*/
			buff = buffer.readBytes(8);
			ind = buffer.getReaderIndex();
			/*String hCellId = ConvertUtils.convertToHex(buff);
			cellId = Integer.parseInt(hCellId,16);
			//cellId = Integer.parseLong(hCellId, 16);
			String hENodebId = hCellId.substring(11);
			if (hENodebId != null && !hENodebId.isEmpty()){
				//enbMsgTypeSet.add(hMsgType);
				eNodebId = Integer.parseInt(hENodebId,16);
				marketSegment = GeovHeader.getMarketSegmentFromEnodebId(eNodebId);
				LOGGER.debug("market segment = {}", marketSegment);
		
			}
			//long eNodebId = Long.parseLong(hENodebId,16);
			hENodebId = hCellId.substring(9,14);
			eNodebId2 = Integer.parseInt(hENodebId,16);
			LOGGER.debug(String.format("SyncId: [%s], Length: [%s], MsgType: [%s], MsgId: [%s], CellId: [%s = %d or %d]",
			hSyncId,hLength,hMsgType,hMsgId,hCellId,eNodebId,eNodebId2));
			*/
			LOGGER.debug(String.format("SyncId: [%s], Length: [%s], MsgType: [%s], MsgId: [%s]",
			hSyncId,hLength,hMsgType,hMsgId));
			if (num >= 51) {
				buff = buffer.readBytes(4);
				ind = buffer.getReaderIndex();
				String hPLMN = ConvertUtils.convertToHex(buff);
				buff = buffer.readBytes(8);
				ind = buffer.getReaderIndex();
				String hTraceId = ConvertUtils.convertToHex(buff);
				buff = buffer.readBytes(4);
				ind = buffer.getReaderIndex();
				String hCRNTI = ConvertUtils.convertToHex(buff);
				LOGGER.debug(String.format("SeqNum: %s, TimeStamp: %s, PLMN: %s, TraceId: %s, CRNTI: %s",hSeqNum,hTimeStamp,hPLMN, hTraceId, hCRNTI));
			}
		}
		catch (IOException e){
			//logger.error(e);
		}           

		/*
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
		*/
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
			eventType = StemRecordType.GEOV_HDR;
			LOGGER.debug("Event record type = CTR header");
		} else if (evttyp == 4) {
			eventType = StemRecordType.GEOV_EVENT;
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
			eventType = StemRecordType.GEOV_HDR;
			LOGGER.debug("Event record type = CTR header");
		} else if (evttyp == 4) {
			eventType = StemRecordType.GEOV_EVENT;
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
		return GEOV_OFFSET;
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
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
}
