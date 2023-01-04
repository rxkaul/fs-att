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

import com.cci.fs.att.domain.CtumEvent;
import com.cci.fs.att.domain.CtumHeader;
import com.cci.fs.att.domain.EventTimeStamp;
import com.cci.fs.att.domain.StemCtumWrapper;
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
 */
public class CtumMessageHandler implements MessageHandler {

	/** The port. */
	private final int port;

	/** The zk host. */
	private final String zkHost;

	/** The combined version. */
	private String ctumVersion = "CTUM-T";

	/** The CTUM eNodeB location cache. */
	private PayloadCoordsCache ctumEnbLocCache = null;

	/** The coords. */
	private PayloadFieldCoords coords = null;
	
	/** The Constant CTUM_VERSION_FORMAT. */
	private static final String CTUM_VERSION_FORMAT="CTUM-%S";

	/** The enable cache. */
	private final boolean enableCache;

	/** The filter. */
	private GenericMessageFilter filter = null;
	
	/** The mkt seg filter. */
	private MarketSegmentFilter mktSegFilter = null;

	/** The Constant LOGGER. */
    private static final Logger LOGGER = LogManager
			.getLogger(CtumMessageHandler.class);
	
	/**
	 * Instantiates a new ctr message handler.
	 *
	 * @param port            the port
	 * @param zkHost            the zk host
	 * @param ffmtver            the ffmtver
	 * @param coords            the coords
	 * @param enableCache the enable cache
	 */
	public CtumMessageHandler(int port, final String zkHost,
			final String ffmtver, final PayloadFieldCoords coords,
			boolean enableCache) {
		this.port = port;
		this.zkHost = zkHost;
		this.ctumVersion = String.format(CTUM_VERSION_FORMAT, ffmtver);
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
		if (enableCache && null == ctumEnbLocCache
				&& StringUtils.hasText(zkHost)) {
			ctumEnbLocCache = createCache();
			ctumEnbLocCache.saveCoords(ctumVersion, coords);
		}
    	JetFuel.JFPacket jfpkt = null;
		final StemRecordType recType = StemCtumWrapper.getEventType(rawMsg);
        //detect event types discard header types; only process events
		if (recType == StemRecordType.CTUM_EVENT) {
    		boolean skip = false;
    		
			if (enableCache) {
				coords = ctumEnbLocCache.getCoords();
			}
    		final int eNodebId = CtumEvent.getEnodebId(rawMsg, coords);
    		final int mktseg = CtumEvent.getMarketSegmentFromEnodebId(eNodebId);
    		// First filter by market segment 
    		if (mktSegFilter != null){
    			skip = mktSegFilter.hasMatching(mktseg);
    			if (skip && LOGGER.isDebugEnabled()){
    				LOGGER.debug("Filtering CTUM record [{}] for market segment [{}]", HexBin.encode(rawMsg), mktseg);
    			}
    		}
     		// Then filter records that match the generic filter value
    		if (!skip && null != filter){
    			skip = filter.hasMatchingBits(rawMsg);
    			if (skip && LOGGER.isDebugEnabled()){
    				LOGGER.debug("Filtering CTUM record {}", HexBin.encode(rawMsg));
    			}
    		}
    		if (!skip){

	    		final JetFuel.JFPacket.Builder builder = JetFuel.JFPacket.newBuilder();
				final EventTimeStamp ts = CtumEvent.getTimeStamp(rawMsg);
	        	builder.setTimestamp(ts.toTime());
				builder.setEnodeB(eNodebId);
				builder.setMessageType(JetFuel.MessageType.CTUM);
		        builder.setPayload(ByteString.copyFrom(rawMsg));
	
		        jfpkt = builder.build();
    		}
		} else if (enableCache && recType == StemRecordType.CTUM_HDR) {
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
		final CtumHeader ctumHeader = new CtumHeader(rawMsg);
		final String ffmtver = String.format("CTUM-%d%d",
				ctumHeader.getFileFormatVersion(),
				ctumHeader.getFileInfoVersion());
		LOGGER.info("Combined version from CTUM header = {}", ffmtver);
		if (!ctumVersion.equalsIgnoreCase(ffmtver) && null != ctumEnbLocCache) {
			if (StringUtils.hasText(ffmtver)) {
				// fetch eNodeB id field coords for this format from
				// Zookeeper
				final PayloadFieldCoords coords = ctumEnbLocCache
						.fetchCoords(ffmtver);
				if (null != coords) {
					LOGGER.info(
							"Changing Enode B id byte ({}) and bit ({}) offsets for file format version {} in CTUM header.",
							ffmtver, coords.getByteOffset(),
							coords.getBitOffset());
					ctumVersion = ffmtver;
					this.coords = coords;
				} else {
					LOGGER.error(
							"CTUM header format changed from {} to {}, but did not find coords in cache for the new format.",
							ctumVersion, ffmtver);
				}
			} else {
				LOGGER.error("Ignoring empty format version in CTR header. {}",
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
		final CtumHeader ctumHeader = new CtumHeader(rawMsg);
		final String ffmtver = String.format("CTUM-%d%d",
				ctumHeader.getFileFormatVersion(),
				ctumHeader.getFileInfoVersion());
		LOGGER.info("Combined version from CTUM header = {}", ffmtver);
		if (!ctumVersion.equalsIgnoreCase(ffmtver) && null != ctumEnbLocCache) {
			if (StringUtils.hasText(ffmtver)) {
				// fetch eNodeB id field coords for this format from
				// Zookeeper
				final PayloadFieldCoords coords = ctumEnbLocCache
						.fetchCoords(ffmtver);
				if (null != coords) {
					LOGGER.info(
							"Changing Enode B id byte ({}) and bit ({}) offsets for file format version {} in CTUM header.",
							ffmtver, coords.getByteOffset(),
							coords.getBitOffset());
					ctumVersion = ffmtver;
					this.coords = coords;
				} else {
					LOGGER.error(
							"CTUM header format changed from {} to {}, but did not find coords in cache for the new format.",
							ctumVersion, ffmtver);
				}
			} else {
				LOGGER.error("Ignoring empty format version in CTR header. {}",
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
		if (enableCache && null == ctumEnbLocCache
				&& StringUtils.hasText(zkHost)) {
			ctumEnbLocCache = createCache();
			ctumEnbLocCache.saveCoords(ctumVersion, coords);
		}
		JetFuel.JFPacket jfpkt = null;
		final StemRecordType recType = StemCtumWrapper.getEventType(msgBuffer);
		// detect event types discard header types; only process events
		if (recType == StemRecordType.CTUM_EVENT) {
			boolean skip = false;

			if (enableCache) {
				coords = ctumEnbLocCache.getCoords();
			}
			final int eNodebId = CtumEvent.getEnodebId(msgBuffer, coords);
			final int mktseg = CtumEvent.getMarketSegmentFromEnodebId(eNodebId);
			// First filter by market segment
			if (mktSegFilter != null) {
				skip = mktSegFilter.hasMatching(mktseg);
				if (skip && LOGGER.isDebugEnabled()) {
					LOGGER.debug(
							"Filtering CTUM record [{}] for market segment [{}]",
							ByteUtils.encodeHex(msgBuffer), mktseg);
				}
			}
			// Then filter records that match the generic filter value
			if (!skip && null != filter) {
				skip = filter.hasMatchingBits(msgBuffer);
				if (skip && LOGGER.isDebugEnabled()) {
					LOGGER.debug("Filtering CTUM record {}",
							ByteUtils.encodeHex(msgBuffer));
				}
			}
			if (!skip) {

				final JetFuel.JFPacket.Builder builder = JetFuel.JFPacket
						.newBuilder();
				long ts = CtumEvent.getTimeStamp(msgBuffer);
				builder.setTimestamp(ts);
				builder.setEnodeB(eNodebId);
				builder.setMessageType(JetFuel.MessageType.CTUM);
				builder.setPayload(ByteString.copyFrom(msgBuffer.nioBuffer()));

				jfpkt = builder.build();
			}
		} else if (enableCache && recType == StemRecordType.CTUM_HDR) {
			updateCoordsFromHeader(msgBuffer);
		}
		return jfpkt;
	}

}
