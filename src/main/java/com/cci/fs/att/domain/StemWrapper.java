/*
 * File: $HeadURL: https://svn.cci-dev.com/svn/udpPrototype/foxstream/fs-att/trunk/src/main/java/com/cci/fs/att/domain/StemWrapper.java $ 
 * Last Updated On: $Date: 2016-06-10 07:03:56 +0000 (Fri, 10 Jun 2016) $ 
 * Last Updated By: $Author: rajeev $ 
 * 
 * (c) 2014-2015 Customer Care Inc., L.P. All rights reserved. 
 *
 */
package com.cci.fs.att.domain;

// TODO: Auto-generated Javadoc
/**
 * The Interface StemWrapper.
 *
 * @author CCI
 * @version $Rev: 5078 $
 */
public interface StemWrapper {

	/**
	 * Gets the connection id.
	 *
	 * @return the connection id
	 */
	int getConnectionId();

	/**
	 * Gets the hex connection id.
	 *
	 * @return the hex connection id
	 */
	String getHexConnectionId();

	/**
	 * Gets the sequence.
	 *
	 * @return the sequence
	 */
	long getSequence();

	/**
	 * Gets the start offset.
	 *
	 * @return the start offset
	 */
	int getStartOffset();

	/**
	 * Gets the event type.
	 *
	 * @return the event type
	 */
	StemRecordType getEventType();

}
