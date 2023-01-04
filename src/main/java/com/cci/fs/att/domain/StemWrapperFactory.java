/*
 * File: $HeadURL:$ 
 * Last Updated On: $Date:$ 
 * Last Updated By: $Author: $ 
 * 
 * (c) 2014-2015 Customer Care Inc., L.P. All rights reserved. 
 *
 */
package com.cci.fs.att.domain;

import javax.validation.constraints.NotNull;
import io.pkts.buffer.Buffer;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * The class StemWrapperFactory is a factory for creating StemWrapper objects.
 * 
 * @author CCI
 * @version $Rev:$
 */
public class StemWrapperFactory {

	/**
	 * The Enum DataType.
	 *
	 * @author CCI
	 * @version $Rev:$
	 */
	public enum DataType {
		/** The ctr. */
		CTR,
		/** The ctum. */
		CTUM,
		/** The geov. */
		GEOV,
		/** The pcmd. */
		PCMD
	};

	/** The dest ports. */
	private final int[] destPorts = new int[DataType.values().length];
	private final int[][] destPortRanges = new int[DataType.values().length][2];

	/**
	 * Instantiates a new stem wrapper factory.
	 *
	 * @param ctrport the ctrport
	 * @param ctumport the ctumport
	 * @param pcmdport the pcmdport
	 */
	public StemWrapperFactory(int ctrport, int ctumport, int pcmdport) {
		destPorts[DataType.CTR.ordinal()] = ctrport;
		destPorts[DataType.CTUM.ordinal()] = ctumport;
		destPorts[DataType.PCMD.ordinal()] = pcmdport;

	}

	/**
	 * Instantiates a new stem wrapper factory.
	 *
	 * @param ctrports the ports for CTR data
	 * @param ctumports the ports for CTUM data
	 * @param geovports the ports for GEOV data
	 */
	public StemWrapperFactory(int[] ctrports, int[] ctumports, int[] geovports) {
		destPortRanges[DataType.CTR.ordinal()] = ctrports;
		destPortRanges[DataType.CTUM.ordinal()] = ctumports;
		destPortRanges[DataType.GEOV.ordinal()] = geovports;

	}
	/**
	 * Creates a new StemWrapper object.
	 *
	 * @param bytes
	 *            the bytes
	 * @param destPort
	 *            the dest port
	 * @return the stem wrapper
	 */
	public StemWrapper createStemWrapper(@NotNull final byte[] bytes,
			int destPort) {
		StemWrapper stemwrap = null;
		int ind = 0;
		boolean fnd = false;
		for (int dport:destPorts){
			if (destPort == dport) {
				fnd = true;
				break;
			}
			else {
				ind++;
			}
		}
		if (fnd) {
			switch (DataType.values()[ind]) {
			case CTR:
				stemwrap = new StemCtrWrapper(bytes);
				break;
			case CTUM:
				stemwrap = new StemCtumWrapper(bytes);
				break;
			case PCMD:
				stemwrap = new StemPcmdWrapper(bytes);
				break;
			default:
				break;
			}
		}

		return stemwrap;
	}

	/**
	 * Creates a new StemWrapper object.
	 *
	 * @param bytes
	 *            the bytes
	 * @param destPort
	 *            the dest port
	 * @return the stem wrapper
	 */
	public StemWrapper createStemWrapper2(@NotNull final Buffer buffer,
			int destPort) {
		StemWrapper stemwrap = null;
		int ind = 0;
		boolean fnd = false;
		for (int[] dportRange:destPortRanges){
			for (int dport=dportRange[0]; dport <= dportRange[1]; dport++){
				if (destPort == dport) {
					fnd = true;
					break;
				}
			}
			if (!fnd){
				ind++;
			}
			else {
				break;
			}
		}
		if (fnd) {
			switch (DataType.values()[ind]) {
			case CTR:
				stemwrap = new StemCtrWrapper(buffer.getArray());
				break;
			case CTUM:
				stemwrap = new StemCtumWrapper(buffer.getArray());
				break;
			case PCMD:
				stemwrap = new StemPcmdWrapper(buffer.getArray());
				break;
		    case GEOV:
				stemwrap = new StemGeovWrapper(buffer);
				break;
			default:
				break;
			}
		}

		return stemwrap;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}
