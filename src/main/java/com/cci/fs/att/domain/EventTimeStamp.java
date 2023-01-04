/*
 * File: $HeadURL: https://svn.cci-dev.com/svn/udpPrototype/foxstream/fs-att/trunk/src/main/java/com/cci/fs/att/domain/EventTimeStamp.java $ 
 * Last Updated On: $Date: 2022-02-27 20:37:06 +0000 (Sun, 27 Feb 2022) $ 
 * Last Updated By: $Author: rajeev $ 
 * 
 * (c) 2014-2015 Customer Care Inc., L.P. All rights reserved. 
 *
 */
package com.cci.fs.att.domain;

import io.netty.buffer.ByteBuf;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.joda.time.DateTime;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * The Class EventTimeStamp.
 * 
 * @author CCI
 * @version $Rev: 5355 $
 */
public class EventTimeStamp {
	
	/** The hour. */
	private int hour;
	
	/** The min. */
	private int min;
	
	/** The sec. */
	private int sec;
	
	/** The msec. */
	private int msec;
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(EventTimeStamp.class);

	/**
	 * Instantiates a new event time stamp.
	 *
	 * @param bytes the bytes
	 * @param offset the offset
	 * @param isctr the isctr
	 */
	EventTimeStamp(byte[] bytes, int offset, boolean isctr){
		parseTimeStamp(bytes, offset, isctr);
	}
	
	/**
	 * Parses the time stamp.
	 *
	 * @param bytes the bytes
	 * @param offset the offset
	 * @param isctr the isctr
	 */
	private void parseTimeStamp(byte[] bytes, int offset, boolean isctr){
		final int start = offset;
		// hr
        hour = (bytes[start] & 0xFF);
		if (LOGGER.isDebugEnabled()){
			LOGGER.debug(String.format("Hr = %d, hex=%02X", hour, hour));
		}
        // min 
        min = (bytes[start+1] & 0xFF);
		if (LOGGER.isDebugEnabled()){
			LOGGER.debug(String.format("Min = %d, hex=%02X", min, min));
		}
        // sec 
        sec = (bytes[start+2] & 0xFF);
		if (LOGGER.isDebugEnabled()){
			LOGGER.debug(String.format("Sec = %d, hex=%02X", sec, sec));
		}
        // msec 
        if (isctr){
            // msec 2 bytes for CTR
        	msec = ((bytes[start+3] & 0xFF) << 8) | (bytes[start+4] & 0xFF);
        }
        else {
            // msec 10 bits for CTUM
			msec = ((bytes[start + 3] & 0xFF) << 2)
					| ((bytes[start + 4] & 0xFF) >>> 6);
        }
		if (LOGGER.isDebugEnabled()){
			LOGGER.debug(String.format("ms = %d, hex=%04X", msec, msec));
		}
 	}

	/**
	 * Gets the hour.
	 *
	 * @return the hour
	 */
	public int getHour() { 
		return hour;
	}

	/**
	 * Gets the min.
	 *
	 * @return the min
	 */
	public int getMin() {
		return min;
	}

	/**
	 * Gets the sec.
	 *
	 * @return the sec
	 */
	public int getSec() {
		return sec;
	}

	/**
	 * Gets the msec.
	 *
	 * @return the msec
	 */
	public int getMsec() {
		return msec;
	}
	
	/**
	 * To time.
	 *
	 * @return the long
	 */
	public long toTime(){
		return convertToMillis(hour, min, sec, msec);
	}

	/**
	 * Convert to millis.
	 *
	 * @param hour
	 *            the hour
	 * @param min
	 *            the min
	 * @param sec
	 *            the sec
	 * @param msec
	 *            the msec
	 * @return the long
	 */
	private static long convertToMillis(int hour, int min, int sec, int msec) {
		DateTime dt = DateTime.now();
		LOGGER.debug("Current time: {}",dt);
		dt = dt.minusHours(dt.getHourOfDay())
		.minusMinutes(dt.getMinuteOfHour())
		.minusSeconds(dt.getSecondOfMinute());
		dt = dt.minusMillis(dt.getMillisOfSecond());
		dt = dt.plusHours(hour).plusMinutes(min).plusSeconds(sec);
		dt = dt.plusMillis(msec);
		LOGGER.debug("Timestamp time: {}",dt);
		return dt.getMillis();

	}

	/**
	 * Gets the timestamp.
	 *
	 * @param bytes
	 *            the bytes
	 * @param offset
	 *            the offset
	 * @param isctr
	 *            the isctr
	 * @return the timestamp
	 */
	public static long getTimestamp(ByteBuf bytes, int offset, boolean isctr) {
		final int start = offset;
		// hr
		int hour = (bytes.getByte(start) & 0xFF);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Hr = %d, hex=%02X", hour, hour));
		}
		// min
		int min = (bytes.getByte(start + 1) & 0xFF);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Min = %d, hex=%02X", min, min));
		}
		// sec
		int sec = (bytes.getByte(start + 2) & 0xFF);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Sec = %d, hex=%02X", sec, sec));
		}
		int msec = 0;
		// msec
		if (isctr) {
			// msec 2 bytes for CTR
			msec = ((bytes.getByte(start + 3) & 0xFF) << 8)
					| (bytes.getByte(start + 4) & 0xFF);
		} else {
			// msec 10 bits for CTUM
			msec = ((bytes.getByte(start + 3) & 0xFF) << 2)
					| ((bytes.getByte(start + 4) & 0xFF) >> 6);
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("ms = %d, hex=%04X", msec, msec));
		}

		return convertToMillis(hour, min, sec, msec);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
}


