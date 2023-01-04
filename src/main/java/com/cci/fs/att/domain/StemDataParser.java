/*
 * File: $HeadURL: https://svn.cci-dev.com/svn/udpPrototype/foxstream/fs-att/trunk/src/main/java/com/cci/fs/att/domain/StemDataParser.java $ 
 * Last Updated On: $Date: 2022-02-27 20:37:06 +0000 (Sun, 27 Feb 2022) $ 
 * Last Updated By: $Author: rajeev $ 
 * 
 * (c) 2014-2015 Customer Care Inc., L.P. All rights reserved. 
 *
 */
package com.cci.fs.att.domain;

import io.pkts.buffer.Buffer;
import io.pkts.packet.impl.UdpPacketImpl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.google.polo.pairing.HexDump;

// TODO: Auto-generated Javadoc
/**
 * The Class StemDataParser.
 *
 * @author CCI
 * @version $Rev: 5355 $
 */
public class StemDataParser {

	/** The record. */
	private boolean record = false;

	/** The market segments. */
	private final Map<Integer, Integer> marketSegments = new HashMap<Integer, Integer>();

	/** The enodeb names map. */
	private final Map<String, Integer> eNames = new HashMap<String, Integer>();

	/** The e nodeb id map. */
	private final Map<Integer, EnodebVO2> eNodebIdMap = new HashMap<Integer, EnodebVO2>();

	/** The source port map. */
	private final Map<Integer, EnodebVO> sourcePortMap = new HashMap<Integer, EnodebVO>();

	/** The format map. */
	private final Map<String, Integer> formatMap = new HashMap<String, Integer>();
	/** The wrap factory. */
	private StemWrapperFactory wrapFactory = null;

	private final boolean portRange;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(StemDataParser.class);

	/**
	 * Instantiates a new stem data parser.
	 *
	 * @param ctrport
	 *                 the ctrport
	 * @param ctumport
	 *                 the ctumport
	 * @param pcmdport
	 *                 the pcmdport
	 */
	public StemDataParser(int ctrport, int ctumport, int pcmdport) {
		portRange = false;
		wrapFactory = new StemWrapperFactory(ctrport, ctumport, pcmdport);
	}

	public StemDataParser(int[] ctrports, int[] ctumports, int[] geovports) {
		portRange = true;
		wrapFactory = new StemWrapperFactory(ctrports, ctumports, geovports);
	}

	/**
	 * Record metrics.
	 */
	public void recordMetrics() {
		record = true;
	}

	/**
	 * Parses the.
	 *
	 * @param pkt the pkt
	 */
	public int parse(UdpPacketImpl pkt) {
		LOGGER.debug("STEM record:");
		boolean parseSuccess = false;
		final int srcport = pkt.getSourcePort();
		final int dstport = pkt.getDestinationPort();
		LOGGER.debug(String.format("UDP ports - source: %d, destination: %d", srcport, dstport));

		final Buffer buffer = pkt.getPayload();
		// System.out.println(HexDump.dumpHexString(buffer.getArray()));
		LOGGER.debug(HexDump.toHexString(buffer.getArray()));
		LOGGER.debug("record length: {} bytes.", buffer.getArray().length);
		// parse the STEM wrapper

		StemWrapper stemWrapper = null;
		if (portRange) {
			stemWrapper = wrapFactory.createStemWrapper2(
					buffer, dstport);
		} else {
			stemWrapper = wrapFactory.createStemWrapper(
					buffer.getArray(), dstport);
		}

		if (stemWrapper == null) {
			LOGGER.info("Skipping packets with (destination port: {}).", dstport);
			return -1;
		}
		// StemCtrWrapper stemWrapper = new StemCtrWrapper(buffer.getArray(),
		// dstport,
		// dstport == stemCtrDestPort);

		final StemRecordType recType = stemWrapper.getEventType();

		Integer marketSegment = 0;
		String ename = null;
		Integer enodebId = 0;
		CtrHeader ctrHdr = null;
		CtrEvent ctrEvent = null;
		CtumHeader ctumHdr = null;
		CtumEvent ctumEvent = null;
		GeovHeader geovHdr = null;
		GeovEvent geovEvent = null;
		PcmdRecord pcmdRecord = null;
		boolean isPcmd = false;
		final int srcPort = pkt.getSourcePort();
		EnodebVO enbvo = sourcePortMap.get(srcPort);
		if (record) {
			if (enbvo == null) {
				enbvo = new EnodebVO();
				sourcePortMap.put(srcPort, enbvo);
			}
		}
		parseSuccess = true;
		switch (recType) {
			case CTR_HDR:
				ctrHdr = new CtrHeader(buffer.getArray());
				isPcmd = PcmdEventId.IsPcmdPayload(buffer.getArray());
				// recording uniqe enodeb names
				if (record) {
					ename = ctrHdr.getEnodebName();
					if (eNames.containsKey(ename)) {
						eNames.put(ename, eNames.get(ename) + 1);
					} else {
						eNames.put(ename, 1);
					}
					enbvo.headerCount++;
					final String format = String.format("CTR-%s%s%s",
							ctrHdr.getFileFormatVersion(),
							ctrHdr.getpMRecordingVersion(),
							ctrHdr.getpMRecordingRevision());
					Integer count = formatMap.get(format);
					if (null == count) {
						formatMap.put(format, 1);
					} else {
						formatMap.put(format, ++count);
					}
				}
				break;
			case CTR_EVENT:
				ctrEvent = new CtrEvent(buffer.getArray());
				enodebId = ctrEvent.getEnodebId();
				marketSegment = ctrEvent.getMarketSegment();
				isPcmd = PcmdEventId.IsPcmdPayload(buffer.getArray());
				if (record) {
					enbvo.enodebId = ctrEvent.getEnodebId();
					enbvo.eventId = ctrEvent.getEventId();
					enbvo.eventCount++;

					EnodebVO2 enbvo2 = eNodebIdMap.get(enbvo.enodebId);
					if (enbvo2 != null) {
						enbvo2.eventCount++;
					} else {
						enbvo2 = new EnodebVO2();
						enbvo2.eventCount = 1;
					}
					enbvo2.payloadMixup = enbvo2.payloadMixup || isPcmd;
					enbvo2.eventId = enbvo.eventId;
					eNodebIdMap.put(enbvo.enodebId, enbvo2);
				}
				break;
			case CTUM_HDR:
				ctumHdr = new CtumHeader(buffer.getArray());
				if (record) {
					final String format = String.format("CTUM-%d%d",
							ctumHdr.getFileFormatVersion(),
							ctumHdr.getFileInfoVersion());
					Integer count = formatMap.get(format);
					if (null == count) {
						formatMap.put(format, 1);
					} else {
						formatMap.put(format, ++count);
					}
				}
				break;
			case CTUM_EVENT:
				ctumEvent = new CtumEvent(buffer.getArray());
				enodebId = ctumEvent.getEnodebId();
				marketSegment = ctumEvent.getMarketSegment();
				if (record) {
					enbvo.enodebId = enodebId;
					enbvo.eventCount++;
					EnodebVO2 enbvo2 = eNodebIdMap.get(enbvo.enodebId);
					if (enbvo2 != null) {
						enbvo2.eventCount++;
						eNodebIdMap.put(enbvo.enodebId, enbvo2);
					} else {
						enbvo2 = new EnodebVO2();
						enbvo2.eventCount = 1;
						eNodebIdMap.put(enbvo.enodebId, enbvo2);
					}

				}
				break;
			case GEOV_HDR:
				parseSuccess = false;
				if (buffer == null || buffer.isEmpty()) {
					break;
				}
				buffer.setReaderIndex(0);
				int num = buffer.getReadableBytes();
				if (num < 35) {
					// num35++;
					LOGGER.warn(
							String.format("Skipping record as it does not have enough [{} < 35] readable bytes.", num));
					break;
				}
				geovHdr = new GeovHeader(buffer);
				enodebId = geovHdr.getEnodebId();
				if (enodebId <= 0)
					break;
				marketSegment = geovHdr.getMarketSegment();
				parseSuccess = true;
				if (record) {
					/*
					 * final String format = String.format("GEOV-%d%d",
					 * geovHdr.getFileFormatVersion(),
					 * geovHdr.getFileInfoVersion());
					 * Integer count = formatMap.get(format);
					 * if (null == count) {
					 * formatMap.put(format, 1);
					 * } else {
					 * formatMap.put(format, ++count);
					 * }
					 */
					enbvo.enodebId = enodebId;
					enbvo.eventCount++;
					EnodebVO2 enbvo2 = eNodebIdMap.get(enbvo.enodebId);
					if (enbvo2 != null) {
						enbvo2.eventCount++;
						eNodebIdMap.put(enbvo.enodebId, enbvo2);
					} else {
						enbvo2 = new EnodebVO2();
						enbvo2.eventCount = 1;
						eNodebIdMap.put(enbvo.enodebId, enbvo2);
					}
				}
				break;
			case GEOV_EVENT:
				geovEvent = new GeovEvent(buffer);
				if (record) {
				}
				break;
			case PCMD_EVENT:
				pcmdRecord = new PcmdRecord(buffer.getArray());
				marketSegment = pcmdRecord.getMarketSegment();
				enodebId = pcmdRecord.getEnodebId();
				if (record) {
					enbvo.enodebId = enodebId;
					enbvo.eventCount++;
					EnodebVO2 enbvo2 = eNodebIdMap.get(enbvo.enodebId);
					if (enbvo2 != null) {
						enbvo2.eventCount++;
						eNodebIdMap.put(enbvo.enodebId, enbvo2);
					} else {
						enbvo2 = new EnodebVO2();
						enbvo2.eventCount = 1;
						eNodebIdMap.put(enbvo.enodebId, enbvo2);
					}
				}
				break;
			default:
				break;
		}

		// recording unique market segment names
		if (record && marketSegment > 0) {
			if (marketSegments.containsKey(marketSegment)) {
				marketSegments.put(marketSegment, marketSegments.get(marketSegment) + 1);
			} else {
				marketSegments.put(marketSegment, 1);
			}
		}

		if (!record && enodebId > 0) {
			LOGGER.info("type: {}, eNodeb ID: {}, market segment = {}", recType, enodebId, marketSegment);
		}

		return enodebId;

	}

	/**
	 * Dump market segment and enodeb name counts.
	 */
	public void dumpMetrics() {
		if (record) {
			LOGGER.info("Market Segment   Count");
			LOGGER.info("--------------   ------------");
			for (Integer key : marketSegments.keySet()) {
				LOGGER.info(String.format("%8d         %,12d",
						key, marketSegments.get(key)));
			}

			/*
			 * LOGGER.debug("eNodeb Name   ,  Count");
			 * LOGGER.debug("--------------,  -----");
			 * for (String key: eNames.keySet()){
			 * LOGGER.debug(" {},               {}",
			 * key, eNames.get(key));
			 * }
			 */

			LOGGER.debug("Source Port,     EnodebVO");
			LOGGER.debug("--------------,  --------------------------");
			for (Integer key : sourcePortMap.keySet()) {
				LOGGER.debug(" {},               {}", key,
						sourcePortMap.get(key).toAbbrevString());
			}
			LOGGER.debug("Total unique source port count = {}", sourcePortMap
					.keySet()
					.size());

			LOGGER.info("EnodebId         EventId; Count");
			LOGGER.info("--------------   -------------------------------");
			for (Integer key : eNodebIdMap.keySet()) {
				LOGGER.info(String.format("%14s   %s", key, eNodebIdMap.get(key).toAbbrevString()));
			}
			LOGGER.info("Total unique enodeb count = {}", eNodebIdMap.keySet()
					.size());

			LOGGER.debug("Format,          Count");
			LOGGER.debug("--------------,  -----");
			for (String key : formatMap.keySet()) {
				LOGGER.debug(String.format("%14s,  %4d", key, formatMap.get(key)));
			}
			LOGGER.debug("Total unique format count = {}", formatMap.keySet()
					.size());
		}
	}

	/**
	 * The Class EnodebVO.
	 *
	 * @author CCI
	 * @version $Rev: 5355 $
	 */
	static class EnodebVO {

		/** The enodeb id. */
		private int enodebId = 0;

		/** The event count. */
		private int eventCount = 0;

		/** The header count. */
		private int headerCount = 0;

		private int eventId = 0;

		/**
		 * Gets the enodeb id.
		 *
		 * @return the enodeb id
		 */
		public int getEnodebId() {
			return enodebId;
		}

		/**
		 * Sets the enodeb id.
		 *
		 * @param enodebId the new enodeb id
		 */
		public void setEnodebId(int enodebId) {
			this.enodebId = enodebId;
		}

		/**
		 * Gets the event count.
		 *
		 * @return the event count
		 */
		public int getEventCount() {
			return eventCount;
		}

		/**
		 * Sets the event count.
		 *
		 * @param eventCount the new event count
		 */
		public void setEventCount(int eventCount) {
			this.eventCount = eventCount;
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
		 * Sets the event id.
		 *
		 * @param eventId the new event id
		 */
		public void setEventId(int eventId) {
			this.eventId = eventId;
		}

		/**
		 * Gets the header count.
		 *
		 * @return the header count
		 */
		public int getHeaderCount() {
			return headerCount;
		}

		/**
		 * Sets the header count.
		 *
		 * @param headerCount the new header count
		 */
		public void setHeaderCount(int headerCount) {
			this.headerCount = headerCount;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "EnodebVO [enodebId=" + enodebId + ", eventCount="
					+ eventCount + ", headerCount=" + headerCount + "]";
		}

		/**
		 * To abbrev string.
		 *
		 * @return the string
		 */
		public String toAbbrevString() {
			return String.format("%d:%d:%d:%d", enodebId, eventId, eventCount, headerCount);
		}

	}

	/**
	 * The Class EnodebVO2.
	 *
	 * @author CCI
	 * @version $Rev: 5355 $
	 */
	static class EnodebVO2 {

		/** The event count. */
		private int eventCount = 0;

		private int eventId = 0;

		private boolean payloadMixup = false;

		/**
		 * Gets the event count.
		 *
		 * @return the event count
		 */
		public int getEventCount() {
			return eventCount;
		}

		/**
		 * Sets the event count.
		 *
		 * @param eventCount the new event count
		 */
		public void setEventCount(int eventCount) {
			this.eventCount = eventCount;
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
		 * Sets the event id.
		 *
		 * @param eventId the new event id
		 */
		public void setEventId(int eventId) {
			this.eventId = eventId;
		}

		/**
		 * Checks if there is a payload mixup.
		 *
		 * @return the payload mixup flag
		 */
		public boolean isPayloadMixup() {
			return payloadMixup;
		}

		/**
		 * Sets the payload mixup flag
		 *
		 * @param payloadMixup the new status for payload mixup
		 */
		public void setPayloadMixup(boolean mixup) {
			this.payloadMixup = mixup;
		}

		/**
		 * To abbrev string.
		 *
		 * @return the string
		 */
		public String toAbbrevString() {
			return String.format("id: %8d; cnt: %,12d; mixup: %b", eventId, eventCount, payloadMixup);
		}

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
