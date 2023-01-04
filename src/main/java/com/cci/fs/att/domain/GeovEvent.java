/*
 * File: $HeadURL: https://svn.cci-dev.com/svn/udpPrototype/foxstream/fs-att/trunk/src/main/java/com/cci/fs/att/domain/GeovEvent.java $ 
 * Last Updated On: $Date: 2016-06-10 07:03:56 +0000 (Fri, 10 Jun 2016) $ 
 * Last Updated By: $Author: rajeev $ 
 * 
 * (c) 2014-2015 Customer Care Inc., L.P. All rights reserved. 
 *
 */
package com.cci.fs.att.domain;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;

import javax.validation.constraints.NotNull;

import com.cci.fs.data.PayloadFieldCoords;
import com.cci.fs.utils.ByteUtils;
import com.cci.fs.utils.MessageUtils;
import com.cci.fs.utils.NullChecks;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import io.netty.buffer.ByteBuf;
import io.pkts.buffer.Buffer;


/**
 * The Class GeovEvent.
 * 
 * @author CCI
 * @version $Rev: 5078 $
 */
public class GeovEvent {
	

	/** The Constant logger. */
	private static final Logger logger = LogManager.getLogger(GeovEvent.class);

	/**
	 * Instantiates a new GeoV event.
	 *
	 * @param Buffer the buffer
	 */
	public GeovEvent(Buffer buffer){
		parseEventRecord(buffer);
	}
	
	/**
	 * Parses the event record.
	 *
	 * @param bytes the bytes
	 */
	private void parseEventRecord(Buffer buffer){
		checkArgument(buffer != null && !buffer.isEmpty(), 
				NullChecks.illegalArgMessage("buffer"));


	    /*
  		int num = 0;
		
		try {
		}
		catch (IOException e){
			logger.error(e);
		}
		*/
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
