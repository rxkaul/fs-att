/*
 * File: $HeadURL:$ 
 * Last Updated On: $Date:$ 
 * Last Updated By: $Author: $ 
 * 
 * (c) 2014-2015 Customer Care Inc., L.P. All rights reserved. 
 *
 */
package com.cci.fs.att.msg;

import io.netty.buffer.ByteBuf;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.springframework.util.StringUtils;

import com.cci.fs.att.domain.CtrEvent;
import com.cci.fs.att.domain.CtrHeader;
import com.cci.fs.att.domain.EventTimeStamp;
import com.cci.fs.att.domain.StemCtrWrapper;
import com.cci.fs.att.domain.StemRecordType;
import com.cci.fs.data.PayloadFieldCoords;
import com.cci.fs.msg.GenericMessageFilter;
import com.cci.fs.msg.MessageHandler;
import com.cci.fs.msg.PayloadCoordsCache;
import com.cci.fs.protocols.JetFuel;
import com.cci.fs.protocols.JetFuel.JFPacket;
import com.cci.fs.utils.ByteUtils;
import com.google.protobuf.ByteString;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

/**
 * The Class CTRMessageHandler.
 * 
 * @author CCI
 * @version $Rev$
 * 
 */
public class CtrMessageHandler implements MessageHandler {

	/** The port. */
	private final int port;

	/** The zk host. */
	private final String zkHost;

	/** The combined version. */
	private String ctrVersion = "CTR-T";

	/** The CTR eNodeB location cache. */
	private PayloadCoordsCache ctrEnbLocCache = null;

	/** The coords. */
	private PayloadFieldCoords coords = null;

	/** The enable cache. */
	private boolean enableCache = false;
	
	/** The filter. */
	private GenericMessageFilter filter = null;
	
	/** The mkt seg filter. */
	private MarketSegmentFilter mktSegFilter = null;

	/** The Constant LOGGER. */
    private static final Logger LOGGER = LogManager
            .getLogger(CtrMessageHandler.class);
	
	/**
	 * Instantiates a new ctr message handler.
	 *
	 * @param port            the port
	 * @param zkHost            the zk host
	 * @param ffmtver            the ffmtver
	 * @param coords            the coords
	 * @param enableCache the enable cache
	 */
	public CtrMessageHandler(int port, final String zkHost,
			final String ffmtver, final PayloadFieldCoords coords,
			boolean enableCache) {
		this.port = port;
		this.zkHost = zkHost;
		this.ctrVersion = ffmtver;
		this.coords = coords;
		this.enableCache = enableCache;

	}
	
	/* (non-Javadoc)
	 * @see com.cci.fs.msg.MessageHandler#setFilter(java.lang.String)
	 */
	public void setFilter(final String jsonFilter) {
		if (StringUtils.hasText(jsonFilter)){
			filter = GenericMessageFilter.constructFromJson(jsonFilter);
		}
		else {
			filter = null;
		}
	}

	/* (non-Javadoc)
	 * @see com.cci.fs.msg.MessageHandler#setMarketSegFilter(java.lang.String)
	 */
	public void setMarketSegFilter(final String mktSegFilters) {
		
		if (StringUtils.hasText(mktSegFilters)){
			mktSegFilter = new MarketSegmentFilter(mktSegFilters);
		}
		else {
			mktSegFilter = null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cci.fs.message.IMessageHandler#BuildJFPacket(byte[])
	 */
    @Override
	public JetFuel.JFPacket buildJFPacket(byte[] rawMsg) {

		// save initial coords on zookeeper cache
		if (enableCache && null == ctrEnbLocCache
				&& StringUtils.hasText(zkHost)) {
			ctrEnbLocCache = createCache();
			ctrEnbLocCache.saveCoords(ctrVersion, coords);
		}
    	JetFuel.JFPacket jfpkt = null;
		final StemRecordType recType = StemCtrWrapper.getEventType(rawMsg);
        //detect event types discard header types; only process events
    	if (recType == StemRecordType.CTR_EVENT) {
    		boolean skip = false;
    		
			if (enableCache) {
				coords = ctrEnbLocCache.getCoords();
			}
    		final int eNodebId = CtrEvent.getEnodebId(rawMsg, coords);
    		final int mktseg = CtrEvent.getMarketSegmentFromEnodebId(eNodebId);
    		// First filter by market segment 
    		if (mktSegFilter != null){
    			skip = mktSegFilter.hasMatching(mktseg);
    			if (skip && LOGGER.isDebugEnabled()){
    				LOGGER.debug("Filtering CTR record [{}] for market segment [{}]", HexBin.encode(rawMsg), mktseg);
    			}
    		}
    		
    		// Filter records that match the filter value
    		if (!skip && null != filter){
    			skip = filter.hasMatchingBits(rawMsg);
    			if (skip && LOGGER.isDebugEnabled()){
    				LOGGER.debug("Filtering CTR record {}", HexBin.encode(rawMsg));
    			}
    		}
    		if (!skip){
	    		final JetFuel.JFPacket.Builder builder = JetFuel.JFPacket.newBuilder();
				final EventTimeStamp ts = CtrEvent.getTimeStamp(rawMsg);
	        	builder.setTimestamp(ts.toTime());
		        //builder.setEnodeB(CtrEvent.getEnodebId(rawMsg));
				builder.setEnodeB(eNodebId);
		        builder.setMessageType(JetFuel.MessageType.CTR);
		        builder.setPayload(ByteString.copyFrom(rawMsg));
	
		        jfpkt = builder.build();
    		}

		} else if (recType == StemRecordType.CTR_HDR && enableCache) {
			updateCoordsFromHeader(rawMsg);
    	}
    	return jfpkt;
    }

	/**
	 * Update eNodeB coords from format in header.
	 *
	 * @param rawMsg
	 *            the raw msg
	 */
	private void updateCoordsFromHeader(byte[] rawMsg) {
		final String ffmtver = String.format("CTR-%s",
				CtrHeader.getCombinedVersion(rawMsg));
		LOGGER.info("Combined version from CTR header = {}", ffmtver);
		if (!ctrVersion.equalsIgnoreCase(ffmtver)
				&& null != ctrEnbLocCache) {
			if (StringUtils.hasText(ffmtver)) {
				// fetch eNodeB id field coords for this format from
				// Zookeeper
				final PayloadFieldCoords coords = ctrEnbLocCache
						.fetchCoords(ffmtver);
				if (null != coords) {
					LOGGER.info(
							"Changing Enode B id byte ({}) and bit ({}) offsets for file format version {} in CTR header.",
							coords.getByteOffset(), coords.getBitOffset(),
							ffmtver);
					ctrVersion = ffmtver;
				} else {
					LOGGER.error(
							"CTR header format changed from {} to {}, but did not find coords in cache for the new format.",
							ctrVersion, ffmtver);
				}
			} else {
				LOGGER.error(
						"Ignoring empty file format version in CTR header. {}",
						HexBin.encode(rawMsg));
			}

		}

	}

	/**
	 * Update eNodeB coords from format in header.
	 *
	 * @param rawMsg
	 *            the raw msg
	 */
	private void updateCoordsFromHeader(ByteBuf rawMsg) {
		final String ffmtver = String.format("CTR-%s",
				CtrHeader.getCombinedVersion(rawMsg));
		LOGGER.info("Combined version from CTR header = {}", ffmtver);
		if (!ctrVersion.equalsIgnoreCase(ffmtver) && null != ctrEnbLocCache) {
			if (StringUtils.hasText(ffmtver)) {
				// fetch eNodeB id field coords for this format from
				// Zookeeper
				final PayloadFieldCoords coords = ctrEnbLocCache
						.fetchCoords(ffmtver);
				if (null != coords) {
					LOGGER.info(
							"Changing Enode B id byte ({}) and bit ({}) offsets for file format version {} in CTR header.",
							coords.getByteOffset(), coords.getBitOffset(),
							ffmtver);
					ctrVersion = ffmtver;
				} else {
					LOGGER.error(
							"CTR header format changed from {} to {}, but did not find coords in cache for the new format.",
							ctrVersion, ffmtver);
				}
			} else {
				LOGGER.error(
						"Ignoring empty file format version in CTR header. {}",
						ByteUtils.encodeHex(rawMsg));
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cci.fs.msg.MessageHandler#getPort()
	 */
	@Override
	public int getPort() {
		return port;
	}

	/**
	 * Creates the cache. Helps with test mock objects to create a separate
	 * method for the cache constructor.
	 *
	 * @return the payload coords cache
	 */
	protected PayloadCoordsCache createCache() {
		return new PayloadCoordsCache(zkHost);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	@Override
	public JFPacket buildJFPacket(ByteBuf msgBuffer) {
		// save initial coords on zookeeper cache
		if (enableCache && null == ctrEnbLocCache
				&& StringUtils.hasText(zkHost)) {
			ctrEnbLocCache = createCache();
			ctrEnbLocCache.saveCoords(ctrVersion, coords);
		}
		JetFuel.JFPacket jfpkt = null;
		final StemRecordType recType = StemCtrWrapper.getEventType(msgBuffer);
		// detect event types discard header types; only process events
		if (recType == StemRecordType.CTR_EVENT) {
			boolean skip = false;

			if (enableCache) {
				coords = ctrEnbLocCache.getCoords();
			}
			final int eNodebId = CtrEvent.getEnodebId(msgBuffer, coords);
			final int mktseg = CtrEvent.getMarketSegmentFromEnodebId(eNodebId);
			// First filter by market segment
			if (mktSegFilter != null) {
				skip = mktSegFilter.hasMatching(mktseg);
				if (skip && LOGGER.isDebugEnabled()) {
					LOGGER.debug(
							"Filtering CTR record [{}] for market segment [{}]",
							ByteUtils.encodeHex(msgBuffer), mktseg);
				}
			}

			// Filter records that match the filter value
			if (!skip && null != filter) {
				skip = filter.hasMatchingBits(msgBuffer);
				if (skip && LOGGER.isDebugEnabled()) {
					LOGGER.debug("Filtering CTR record {}",
							ByteUtils.encodeHex(msgBuffer));
				}
			}
			if (!skip) {
				final JetFuel.JFPacket.Builder builder = JetFuel.JFPacket
						.newBuilder();
				long ts = CtrEvent.getTimeStamp(msgBuffer);
				builder.setTimestamp(ts);
				// builder.setEnodeB(CtrEvent.getEnodebId(rawMsg));
				builder.setEnodeB(eNodebId);
				builder.setMessageType(JetFuel.MessageType.CTR);
				builder.setPayload(ByteString.copyFrom(msgBuffer.nioBuffer()));

				jfpkt = builder.build();
			}

		} else if (recType == StemRecordType.CTR_HDR && enableCache) {
			updateCoordsFromHeader(msgBuffer);
		}
		return jfpkt;
	}
	
	
}
