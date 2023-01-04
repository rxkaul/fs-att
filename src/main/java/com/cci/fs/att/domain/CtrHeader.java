/*
 * File: $HeadURL: https://svn.cci-dev.com/svn/udpPrototype/foxstream/fs-att/trunk/src/main/java/com/cci/fs/att/domain/CtrHeader.java $ 
 * Last Updated On: $Date: 2015-08-01 07:11:49 +0000 (Sat, 01 Aug 2015) $ 
 * Last Updated By: $Author: rajeev $ 
 * 
 * (c) 2014-2015 Customer Care Inc., L.P. All rights reserved. 
 *
 */
package com.cci.fs.att.domain;

import static com.google.common.base.Preconditions.checkArgument;
import io.netty.buffer.ByteBuf;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.cci.fs.data.FsDataUtils;
import com.cci.fs.utils.NullChecks;

/**
 * The Class CtrHeader.
 * 
 * @author CCI
 * @version $Rev: 4808 $
 */
public class CtrHeader {

	/** The file format version. */
	private String fileFormatVersion;
	
	/** The p m recording version. */
	private String pMRecordingVersion;
	
	/** The p m recording revision. */
	private String pMRecordingRevision;

	/** The other fields. */
	private String otherFields;
	
	/** The enodeb name. */
	private String enodebName;

	/**
	 * Instantiates a new ctr header.
	 *
	 * @param bytes the bytes
	 */
	public CtrHeader(byte[] bytes){
		parseCtrHeader(bytes);
	}
	
	/**
	 * Gets the file format version.
	 *
	 * @return the file format version
	 */
	public String getFileFormatVersion() {
		return fileFormatVersion;
	}

	/**
	 * Gets the p m recording version.
	 *
	 * @return the p m recording version
	 */
	public String getpMRecordingVersion() {
		return pMRecordingVersion;
	}

	/**
	 * Gets the p m recording revision.
	 *
	 * @return the p m recording revision
	 */
	public String getpMRecordingRevision() {
		return pMRecordingRevision;
	}

	/**
	 * Gets the other fields.
	 *
	 * @return the other fields
	 */
	public String getOtherFields() {
		return otherFields;
	}

	/**
	 * Gets the enodeb name.
	 *
	 * @return the enodeb name
	 */
	public String getEnodebName() {
		return enodebName;
	}
	
	/**
	 * Parses the ctr header.
	 *
	 * @param bytes the bytes
	 */
	private void parseCtrHeader(byte[] bytes){

		checkArgument(bytes != null && bytes.length > 0, 
				NullChecks.illegalArgMessage("bytes"));
		final int start = StemCtrWrapper.CTR_OFFSET;
		// File format version
        fileFormatVersion = CtrHeader.getFileFormatVersion(bytes);

        // PM recording version
        pMRecordingVersion = CtrHeader.getPMRecordingVersion(bytes);

		// PM recording revision
		pMRecordingRevision = CtrHeader.getPMRecordingRevision(bytes);
        
		// Other fields
		otherFields = FsDataUtils.extractString(bytes, start + 23, 7,
				"Other fields");
        
		// eNodeb Name
		enodebName = FsDataUtils.extractString(bytes, start + 30,
				Math.min(128, bytes.length - start - 30),
				"eNodeb name");
        
	}
	
	/**
	 * Gets the file format version.
	 *
	 * @param bytes
	 *            the bytes
	 * @return the file format version
	 */
	public static String getFileFormatVersion(byte[] bytes){
	    final String fileFormatVersion = FsDataUtils.extractString(bytes, StemCtrWrapper.CTR_OFFSET, 5, "File format version");
		return fileFormatVersion;
	}
	
	/**
	 * Gets the PM recording version.
	 *
	 * @param bytes
	 *            the bytes
	 * @return the PM recording version
	 */
	public static String getPMRecordingVersion(byte[] bytes){
		final String pmRecordingVersion = FsDataUtils.extractString(bytes,
				StemCtrWrapper.CTR_OFFSET + 5, 13, "PM recording version");
		return pmRecordingVersion;
	}

	/**
	 * Gets the PM recording revision.
	 *
	 * @param bytes
	 *            the bytes
	 * @return the PM recording revision
	 */
	public static String getPMRecordingRevision(byte[] bytes) {
		final String pmRecordingRevision = FsDataUtils.extractString(bytes,
				StemCtrWrapper.CTR_OFFSET + 18, 5, "PM recording revision");
		return pmRecordingRevision;
	}

	/**
	 * Gets the combined version.
	 *
	 * @param bytes
	 *            the bytes
	 * @return the combined version
	 */
	public static String getCombinedVersion(byte[] bytes) {

		final String combinedVersion = String.format("%s%s%s",
				CtrHeader.getFileFormatVersion(bytes),
				CtrHeader.getPMRecordingVersion(bytes),
				CtrHeader.getPMRecordingRevision(bytes));

		return combinedVersion;
	}

	/**
	 * Gets the file format version.
	 *
	 * @param bytes
	 *            the bytes
	 * @return the file format version
	 */
	public static String getFileFormatVersion(ByteBuf bytes) {
		final String fileFormatVersion = FsDataUtils.extractString(bytes,
				StemCtrWrapper.CTR_OFFSET, 5, "File format version");
		return fileFormatVersion;
	}

	/**
	 * Gets the PM recording version.
	 *
	 * @param bytes
	 *            the bytes
	 * @return the PM recording version
	 */
	public static String getPMRecordingVersion(ByteBuf bytes) {
		final String pmRecordingVersion = FsDataUtils.extractString(bytes,
				StemCtrWrapper.CTR_OFFSET + 5, 13, "PM recording version");
		return pmRecordingVersion;
	}

	/**
	 * Gets the PM recording revision.
	 *
	 * @param bytes
	 *            the bytes
	 * @return the PM recording revision
	 */
	public static String getPMRecordingRevision(ByteBuf bytes) {
		final String pmRecordingRevision = FsDataUtils.extractString(bytes,
				StemCtrWrapper.CTR_OFFSET + 18, 5, "PM recording revision");
		return pmRecordingRevision;
	}

	/**
	 * Gets the combined version.
	 *
	 * @param bytes
	 *            the bytes
	 * @return the combined version
	 */
	public static String getCombinedVersion(ByteBuf bytes) {

		final String combinedVersion = String.format("%s%s%s",
				CtrHeader.getFileFormatVersion(bytes),
				CtrHeader.getPMRecordingVersion(bytes),
				CtrHeader.getPMRecordingRevision(bytes));

		return combinedVersion;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}	
