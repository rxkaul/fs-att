/*
 * File: $HeadURL:$ 
 * Last Updated On: $Date:$ 
 * Last Updated By: $Author: $ 
 * 
 * (c) 2014-2015 Customer Care Inc., L.P. All rights reserved. 
 *
 */
package com.cci.fs.att.msg;

import java.util.Arrays;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.cci.fs.msg.MessageFilter;

// TODO: Auto-generated Javadoc
/**
 * The Class MarketSegmentFilter.
 * 
 * @author CCI
 * @version $Rev$
 * 
 */
public class MarketSegmentFilter {
	
	/** The market segments. */
	private int[] marketSegments=null;

	/**
	 * Instantiates a new market segment filter.
	 *
	 * @param mktSegFilters the mkt seg filters
	 */
	public MarketSegmentFilter(@NotNull final String mktSegFilters){
		final String[] filters = mktSegFilters.split(",");
		marketSegments = new int[filters.length];
		int ind = 0;
		for (String filter:filters){
			marketSegments[ind++] = Integer.parseInt(filter);
		}
	}
	
	/**
	 * Has matching checks if the filter has value that matches the actual value.
	 *
	 * @param value the value
	 * @return true, if successful
	 */
	public boolean hasMatching(int value) {
		boolean match = false;
		for (int mktseg : marketSegments){
			match = (mktseg == value);
			if (match){
				break;
			}
		}
		return match;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
	

}
