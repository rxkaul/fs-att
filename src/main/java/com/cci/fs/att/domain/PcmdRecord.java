/*
 * File: $HeadURL:$ 
 * Last Updated On: $Date:$ 
 * Last Updated By: $Author: $ 
 * 
 * (c) 2014-2015 Customer Care Inc., L.P. All rights reserved. 
 *
 */
package com.cci.fs.att.domain;

import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.springframework.util.StringUtils;


/**
 * The Class PcmdEvent.
 *
 * @author CCI
 * @version $Rev:$
 */
public class PcmdRecord {

	/** The version. */
	private int version;
	
	/** The enodeb id. */
	private int enodebId=0;
	
	/** The market segment. */
	private int marketSegment=0;

	/** The global cell id index. */
	private int globalCellIdIndex = PCMD_GCELLID_IND;

	/** The Constant UTF8. */
	public static final Charset UTF = Charset.forName("UTF8");

	/** The Constant PCMD_SEP. */
	public static final String PCMD_SEP = ";";

	/** The pcmd version ind. */
	public static final int PCMD_VERSION_IND = 0;

	/** The pcmd gcellid ind. */
	public static final int PCMD_GCELLID_IND = 20;

	/** The Constant PCMD_DST_PORT. */
	public static final int PCMD_DST_PORT = 49323;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager
			.getLogger(PcmdRecord.class);

	/**
	 * Instantiates a new ctum event.
	 *
	 * @param bytes the bytes
	 */
	public PcmdRecord(@NotNull final byte[] bytes) {
		parseEventRecord(bytes);
	}
	
	/**
	 * Instantiates a new pcmd record.
	 *
	 * @param bytes
	 *            the bytes
	 * @param gcellind
	 *            the gcellind
	 */
	public PcmdRecord(@NotNull final byte[] bytes, int gcellind) {
		globalCellIdIndex = gcellind;
		// parseEventRecord(bytes);
		parseEventRecord2(bytes);
	}

	/**
	 * Parses the event record.
	 *
	 * @param bytes
	 *            the bytes
	 */
	private void parseEventRecord(@Nonnull final byte[] bytes) {

		final String[] fields = PcmdRecord.getPcmdFields(bytes);


		// decimal ascii integer (4 bytes)
		version = Integer.parseInt(fields[0]);
		final String globalCellId = fields[PCMD_GCELLID_IND];

		enodebId = PcmdRecord.getEnodebIdFromGlobalId(globalCellId);
		if (enodebId > 0) {
			marketSegment = enodebId / 10000;
			LOGGER.debug("market segment = {}", marketSegment);
		}
	}

	private void parseEventRecord2(@Nonnull final byte[] bytes) {

		// decimal ascii integer (4 bytes)
		// version = Integer.parseInt(fields[0]);
		version = Integer.parseInt(getField(bytes, 12, 0));
		final String globalCellId = getField(bytes, 12, PCMD_GCELLID_IND);

		enodebId = PcmdRecord.getEnodebIdFromGlobalId(globalCellId);
		if (enodebId > 0) {
			marketSegment = enodebId / 10000;
			LOGGER.debug("market segment = {}", marketSegment);
		}
	}
	
	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * Gets the enodeb id.
	 *
	 * @return the enodeb id
	 */
	public int getEnodebId() {
		return enodebId;
	}

	/**
	 * Gets the market segment.
	 *
	 * @return the market segment
	 */
	public int getMarketSegment() {
		return marketSegment;
	}
	
	/**
	 * Gets the pcmd fields.
	 *
	 * @param bytes
	 *            the bytes
	 * @return the pcmd fields
	 */
	public static String[] getPcmdFields(@Nonnull final byte[] bytes) {
		final int start = StemPcmdWrapper.PCMD_OFFSET;

		final int len = bytes.length - start;
		String str = new String(bytes, start, len, UTF);
		if (StringUtils.hasText(str)) {
			str = str.trim();
		}

		final String[] fields = str.split(PCMD_SEP);
		return fields;

	}

	private static String getField(@Nonnull final byte[] bytes, int start,
			int fldpos) {
		int lim = bytes.length;
		char ch;
		int pos = 0;
		String field = "";
		boolean done = false;
		for (int ind = start; ind < lim && !done; ind++) {
			ch = (char) bytes[ind];
			if (ch == ';') {
				pos++;
			} else if (pos == fldpos) {
				field += ch;
			}
			done = pos > fldpos;
		}

		return field;
	}

	/**
	 * Gets the version.
	 *
	 * @param bytes
	 *            the bytes
	 * @param loc
	 *            the loc
	 * @return the version
	 */
	public static int getVersion(@Nonnull final byte[] bytes, int loc) {

		final String[] fields = PcmdRecord.getPcmdFields(bytes);

		// decimal ascii integer
		return Integer.parseInt(fields[loc]);
	}

	/**
	 * Gets the version.
	 *
	 * @param bytes
	 *            the bytes
	 * @return the version
	 */
	public static int getVersion(@Nonnull final byte[] bytes) {

		final int start = StemPcmdWrapper.PCMD_OFFSET;

		// Since we expect the version to be the 1st field, fetching 10 bytes
		// should be enough
		String str = new String(bytes, start, 10, UTF);
		if (StringUtils.hasText(str)) {
			str = str.trim();
		}

		final String[] fields = str.split(PCMD_SEP);

		// decimal ascii integer
		return Integer.parseInt(fields[PCMD_VERSION_IND]);
	}

	/**
	 * Gets the enodeb id from global id.
	 *
	 * @param globalCellId
	 *            the global cell id
	 * @return the enodeb id from global id
	 */
	private static int getEnodebIdFromGlobalId(
			@NotNull final String globalCellId) {
		int enodebId = 0;
		if (StringUtils.hasText(globalCellId)) {

			// 15 bytes
			LOGGER.debug("global cell id = {}", globalCellId);

			final int ind = globalCellId.lastIndexOf(":");

			// 20 bits = 6 hex digits
			final String hexEnbId = globalCellId.substring(ind + 1, ind + 6);
			enodebId = Integer.valueOf(hexEnbId, 16);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("enodeb id = %d, hex=%06X",
						enodebId, enodebId));
			}
		}

		return enodebId;

	}

	/**
	 * Gets the enodeb id.
	 *
	 * @param bytes
	 *            the bytes
	 * @param loc
	 *            the loc
	 * @return the enodeb id
	 */
	public static int getEnodebId(@Nonnull final byte[] bytes, int loc) {
		// final String[] fields = PcmdRecord.getPcmdFields(bytes);
		// String globalCellId = fields[loc];
		int version = Integer.parseInt(getField(bytes, 12, 0));
		final String globalCellId = PcmdRecord.getField(bytes, 12,
				PCMD_GCELLID_IND);
		final int enodebId = PcmdRecord.getEnodebIdFromGlobalId(globalCellId);

		return enodebId;
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


