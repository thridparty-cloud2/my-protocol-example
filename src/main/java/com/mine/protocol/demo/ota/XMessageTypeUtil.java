package com.mine.protocol.demo.ota;


import com.x.iot.protocol.support.message.MessageType;

import java.util.Map;

public class XMessageTypeUtil {

    public static Map<String, MessageType> MESSAGE_TYPES;

    static {

        /*
         * OTA
         */
        MESSAGE_TYPES.put("0111", MessageType.OTA);
        MESSAGE_TYPES.put("0112", MessageType.OTA);
        MESSAGE_TYPES.put("0113", MessageType.OTA);
        MESSAGE_TYPES.put("0114", MessageType.OTA);
        MESSAGE_TYPES.put("0115", MessageType.OTA);
    }

    public static MessageType messageType(String cmd) {
        return MESSAGE_TYPES.getOrDefault(cmd, null);
    }

}
