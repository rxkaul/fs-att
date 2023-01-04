/*
 * File: $HeadURL:$ 
 * Last Updated On: $Date:$ 
 * Last Updated By: $Author: $ 
 * 
 * (c) 2014-2015 Customer Care Inc., L.P. All rights reserved. 
 *
 */
package com.cci.fs.att.msg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.cci.fs.data.PayloadFieldCoords;
import com.cci.fs.msg.MessageHandler;
import com.cci.fs.msg.MessageHandlerFactory;

/**
 * A factory for creating CtumMessageHandler objects.
 * 
 * @author CCI
 * @version $Rev$
 */
@Component
public class CtumMessageHandlerFactory implements MessageHandlerFactory {

	/** The ports. */
	private final List<Integer> ports = new ArrayList<Integer>();

	/** The file format version. */
	private String fileFormatVersion;

	/** The e nodeb coords. */
	private PayloadFieldCoords eNodebCoords;

	/** The zk host. */
	private String zkHost;

	/** The enable cache. */
	private boolean enableCache;
	
	/** The json filters. */
	private String jsonFilters;
	
	/** The mkt seg filters. */
	private String mktSegFilters;
	
	/** The handlers. */
	private final List<MessageHandler> handlers = new ArrayList<MessageHandler>();

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cci.fs.msg.MessageHandlerFactory#setPorts(java.lang.String)
	 */
	@Override
	public void setPorts(String ports) {
		if (StringUtils.hasText(ports)) {
			for (String port : ports.split(",")) {
				this.ports.add(Integer.parseInt(port));
			}
		}
	}

	/**
	 * Gets the ports.
	 *
	 * @return the ports
	 */
	public List<Integer> getPortList() {
		return ports;
	}

	/**
	 * Sets the enode b coords.
	 *
	 * @param coords
	 *            the new enode b coords
	 */
	public void setEnodeBCoords(String coords) {
		final int pos = coords.indexOf(":");
		fileFormatVersion = coords.substring(0, pos);
		eNodebCoords = PayloadFieldCoords
				.parseCoords(coords.substring(pos + 1));
	}

	/**
	 * Sets the zk host.
	 *
	 * @param zkHost
	 *            the new zk host
	 */
	public void setZkHost(String zkHost) {
		this.zkHost = zkHost;
	}

	/**
	 * Sets the enable cache.
	 *
	 * @param enableCache
	 *            the new enable cache
	 */
	public void setEnableCache(boolean enableCache) {
		this.enableCache = enableCache;
	}

	
	/**
	 * Sets the filters.
	 *
	 * @param filters the new filters
	 */
	public void setFilters(String filters){
		jsonFilters = filters;
	}

	/**
	 * Sets the mkt seg filters.
	 *
	 * @param filters the new mkt seg filters
	 */
	public void setMktSegFilters(String filters){
		mktSegFilters = filters;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cci.fs.msg.MessageHandlerFactory#createMessageHandlers()
	 */
	@Override
	public List<MessageHandler> createMessageHandlers() {
		for (int port : ports) {
			CtumMessageHandler handler = new CtumMessageHandler(port, zkHost,
					fileFormatVersion, eNodebCoords, enableCache);
			handler.setFilter(jsonFilters);
			handler.setMarketSegFilter(mktSegFilters);
			handlers.add(handler);
		}
		return handlers;
	}

	/* (non-Javadoc)
	 * @see com.cci.fs.msg.MessageHandlerFactory#getHandlers()
	 */
	public List<MessageHandler> getHandlers() {
		return handlers;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
