package com.mine.protocol.demo.ota;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.x.iot.protocol.support.message.MessageType;
import lombok.Getter;
import lombok.Setter;

/**
 * 未确定的消息，Base64编码字符串
 *
 * @author zoro.kong
 * @since 1.0.0
 */
public class UndefinedPayloadMessage extends XTTLVMessage<String> {
    private final MessageType type = MessageType.UNKNOWN;

    @Override
    public MessageType getMessageType() {
        return this.type;
    }

    @JsonProperty("data")
    @Setter
    private String message;
    @Getter
    private final String payload;
    @Setter
    private String payloadFormat;

    @Override
    public String message() {
        return this.message;
    }

    @Override
    public String payload() {
        return this.payload;
    }

    @Override
    public String payloadFormat() {
        return this.payloadFormat;
    }

    public UndefinedPayloadMessage(String payload, String payloadFormat) {
        this.payload = payload;
        this.payloadFormat = payloadFormat;
    }
}
