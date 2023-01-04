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

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.joda.time.DateTime;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.springframework.util.StringUtils;

import com.cci.fs.att.domain.PcmdRecord;
import com.cci.fs.data.PayloadFieldCoords;
import com.cci.fs.msg.GenericMessageFilter;
import com.cci.fs.msg.MessageHandler;
import com.cci.fs.msg.PayloadCoordsCache;
import com.cci.fs.protocols.JetFuel;
import com.cci.fs.protocols.JetFuel.JFPacket;
import com.google.protobuf.ByteString;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

/**
 * The Class PCMD MessageHandler.
 * 
 * @author CCI
 * @version $Rev$
 */
public class PcmdMessageHandler implements MessageHandler {

	/** The port. */
	private final int port;

	/** The zk host. */
	private final String zkHost;

	/** The PCMD record version. */
	private String pcmdVersion = "PCMD-5";

	/** The CTR eNodeB location cache. */
	private PayloadCoordsCache pcmdEnbLocCache = null;

	/** The coords. */
	private PayloadFieldCoords coords = null;

	/** The Constant PCMD_VERSION_FORMAT. */
	private static final String PCMD_VERSION_FORMAT = "PCMD-%d";

	/** The enable cache. */
	private final boolean enableCache;
	
	/** The mkt seg filter. */
	private MarketSegmentFilter mktSegFilter = null;
	
	/** The filter. */
	private GenericMessageFilter filter = null;
	
	/** The Constant LOGGER. */
    private static final Logger LOGGER = LogManager
            .getLogger(CtrMessageHandler.class);
	
	/**
	 * Instantiates a new ctr message handler.
	 *
	 * @param port            the port
	 * @param zkHost            the zk host
	 * @param ver the ver
	 * @param coords            the coords
	 * @param enableCache the enable cache
	 */
	public PcmdMessageHandler(int port, final String zkHost, final int ver,
			final PayloadFieldCoords coords, boolean enableCache) {
		this.port = port;
		this.zkHost = zkHost;
		this.pcmdVersion = String.format(PCMD_VERSION_FORMAT, ver);
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
	public JetFuel.JFPacket buildJFPacket(@NotNull final byte[] rawMsg) {

		// save initial coords on zookeeper cache
		if (enableCache && null == pcmdEnbLocCache && StringUtils.hasText(zkHost)) {
			pcmdEnbLocCache = createCache();
			pcmdEnbLocCache.saveCoords(pcmdVersion, coords);
		}
    	JetFuel.JFPacket jfpkt = null;
		// PCMD has no header records

		boolean skip = false;
		
		if (enableCache){
			coords = pcmdEnbLocCache.getCoords();
			final int version = PcmdRecord.getVersion(rawMsg);
			updateCoords(version);
		}
		// the index of global cell id field is saved in byteOffset
		// field of the payload coordinate
		final int eNodebId = PcmdRecord.getEnodebId(rawMsg, coords.getByteOffset());
		final int mktseg = PcmdRecord.getMarketSegmentFromEnodebId(eNodebId);
		// First filter by market segment 
		if (mktSegFilter != null){
			skip = mktSegFilter.hasMatching(mktseg);
			if (skip && LOGGER.isDebugEnabled()){
				LOGGER.debug("Filtering PCMD record [{}] for market segment [{}]", HexBin.encode(rawMsg), mktseg);
			}
		}
		// Filter records that match the filter value
		if (!skip && null != filter){
			skip = filter.hasMatchingStrings(PcmdRecord.getPcmdFields(rawMsg));
			if (skip && LOGGER.isDebugEnabled()){
				LOGGER.debug("Filtering PCMD record {}", HexBin.encode(rawMsg));
			}
		}
		if (!skip){
			final JetFuel.JFPacket.Builder builder = JetFuel.JFPacket.newBuilder();
			// :TODO we do not know the location of timestamp field in the PCMD
			// payload
			// For now we are just going to insert the current timestamp
			// final EventTimeStamp ts = pcmd.getTimeStamp();
			// builder.setTimestamp(ts.toTime());
			builder.setTimestamp(DateTime.now().getMillis());
			// The version field is going to determine the location of global cell
			// id from which the enodeb id is derived
			// :TODO need to fetch enodeb id based on location index
			builder.setEnodeB(eNodebId);
			builder.setMessageType(JetFuel.MessageType.PCMD);
			builder.setPayload(ByteString.copyFrom(rawMsg));
	
			jfpkt = builder.build();
		}
		return jfpkt;
    }

	/**
	 * Update eNodeB coords from format in header.
	 *
	 * @param version the version
	 */
	private void updateCoords(int version) {
		LOGGER.info("PCMD version = {}", version);
		final String strVer = String.format(PCMD_VERSION_FORMAT, version);
		if (!strVer.equalsIgnoreCase(pcmdVersion) && null != pcmdEnbLocCache) {
			// fetch eNodeB id field coords for this format from
			// Zookeeper
			final PayloadFieldCoords coords = pcmdEnbLocCache
					.fetchCoords(strVer);
			if (null != coords) {
				LOGGER.info(
						"Changing Enode B id location in PCMD record for version = {}, loc = {}",
						strVer, coords.getByteOffset());
				this.pcmdVersion = strVer;
				this.coords = coords;
			} else {
				LOGGER.error(
						"PCMD version changed from {} to {}, but did not find coords in cache for the new version.",
						this.pcmdVersion, strVer);
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
		// TODO Auto-generated method stub
		return null;
	}

}
