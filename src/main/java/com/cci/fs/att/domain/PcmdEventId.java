package com.cci.fs.att.domain;

import java.nio.charset.StandardCharsets;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.springframework.util.StringUtils;

public class PcmdEventId {
	private static final String EBM_EVENT_PREFIX = "EBM";
	private static final String PCMD_EVENT_PREFIX = "RAN";
	private static final Logger LOGGER = LogManager
			.getLogger(PcmdEventId.class);

	public static String getEventId(byte[] bytes) {
	
        // convert bytes to String
        String pcmdPayload = new String(bytes, StandardCharsets.UTF_8);
        return getEventId(pcmdPayload);
	}

	public static String getEventId(String pcmdPayload) {
	
        // convert bytes to String
        //String pcmdPayload = new String(bytes, StandardCharsets.UTF_8);
        String eventId=null;
        if (StringUtils.hasText(pcmdPayload)){
            eventId = PcmdRecordParser2.parseEventId(pcmdPayload);
        }

		return eventId;
	}

    public static boolean IsPcmdPayload(byte[] bytes) {

        String pcmdPayload = new String(bytes, StandardCharsets.UTF_8);

        String eventId = PcmdEventId.getEventId(pcmdPayload);      
        if (StringUtils.hasText(eventId) && 
            (eventId.startsWith(PCMD_EVENT_PREFIX) || 
             eventId.startsWith(EBM_EVENT_PREFIX))){
            Integer enbid = PcmdRecordParser2.parseEnodeB(pcmdPayload);
            if (enbid != null){
                LOGGER.warn("Found PCMD payload eNB id = {}; event id = {}",enbid, eventId);
                return true;
            }
        }
    
        return false;
    }

}
