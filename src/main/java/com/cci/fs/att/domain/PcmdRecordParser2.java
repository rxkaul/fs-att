package com.cci.fs.att.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.springframework.util.StringUtils;

public class PcmdRecordParser2 {

	private static final String PCMD_FIELD_POS_REG_EXP = "^(?:[^,]*,){%d}([^,]+)";
	private static final int ENODEB_FIELD_POS = 7;
	private static final Pattern patternENB = Pattern.compile(String.format(
			PCMD_FIELD_POS_REG_EXP, ENODEB_FIELD_POS - 1));
	// Enode B field is in current cell id in this format: d-d
	// enodeb id is the number left of the hyphen
	private static final String ENB_FIELD_REG_EXP = "^(?:[^,]*,){6}([^:-]+)";
	private static final Pattern patternCI = Pattern
			.compile(ENB_FIELD_REG_EXP);
	private static final String EVENT_ID_FIELD_REG_EXP = "^(?:[^,]*,){4}([^:,]+)";
	private static final Pattern patternEID = Pattern
			.compile(EVENT_ID_FIELD_REG_EXP);

	private static final Logger LOGGER = LogManager
			.getLogger(PcmdRecordParser2.class);

	/*
	 * public static Integer parseEnodeB(String pcmdRecord){ Integer eNBid =
	 * null; String fldENB = findField(patternENB,pcmdRecord); if
	 * (StringUtils.hasText(fldENB)){ String lcn = findField(patternLCN,fldENB);
	 * if (StringUtils.hasText(lcn)){
	 * 
	 * } }
	 * 
	 * return eNBid; }
	 */
	public static Integer parseEnodeB(String pcmdRecord) {
		Integer eNBid = null;
		String enbidStr = findField(patternCI, pcmdRecord);
		if (StringUtils.hasText(enbidStr)) {
			// 20 bits = 5 hex digits
			// final String enbidStr = lcn.substring(0, Math.min(5,
			// lcn.length()));
			eNBid = Integer.parseInt(enbidStr);
			/*if (LOGGER.isDebugEnabled()) {
				int mktid = eNBid / 10000;
				if (mktid == 0) {
					LOGGER.debug(
							"Found market id = 0, eNB = {}, for record = {}",
							eNBid, pcmdRecord);
				}
			}*/
		}

		return eNBid;
	}

	public static String parseEventId(String pcmdRecord) {
		String eventId = findField(patternEID, pcmdRecord);
		if (StringUtils.hasText(eventId)) {
			/*if (LOGGER.isDebugEnabled()) {
				if (eventId == 0) {
					LOGGER.debug(
							"Found EBM event id = {}, for record = {}",
							eventId, pcmdRecord);
				}
			}*/
		}

		return eventId;
	}

	public static String findPcmdFieldAtPosition(int pos, String pcmdRecord) {
		Pattern pattern = Pattern.compile(String.format(PCMD_FIELD_POS_REG_EXP,
				pos - 1));

		return findField(pattern, pcmdRecord);

	}

	private static String findField(Pattern pattern, String record) {
		Matcher matcher = pattern.matcher(record);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return null;
		}

	}

}
